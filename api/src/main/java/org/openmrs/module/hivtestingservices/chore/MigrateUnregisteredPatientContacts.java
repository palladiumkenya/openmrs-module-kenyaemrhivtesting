/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.hivtestingservices.chore;

import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.hivtestingservices.api.HTSService;
import org.openmrs.module.hivtestingservices.api.PatientContact;
import org.openmrs.module.hivtestingservices.wrapper.PatientWrapper;
import org.openmrs.module.idgen.service.IdentifierSourceService;
import org.openmrs.module.kenyacore.chore.AbstractChore;
import org.openmrs.module.kenyaemr.metadata.CommonMetadata;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.openmrs.util.LocationUtility.getDefaultLocation;

/**
 * handles migration of unregistered patient contacts
 */
@Component("hivtestingservices.chore.MigrateUnregisteredPatientContacts")
public class MigrateUnregisteredPatientContacts extends AbstractChore {

    private static final String UNKNOWN = "1067AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    private static final String CAUSE_OF_DEATH_PLACEHOLDER = "1067AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    private static final List<String> CONCEPTS_FOR_OBS = Arrays.asList(
            "1054AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", // CIVIL_STATUS
            "1542AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", // OCCUPATION
            "1712AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", // EDUCATION
            "5629AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", // IN_SCHOOL
            "1174AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", // ORPHAN
            "165657AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"  // COUNTRY
    );
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public void perform(PrintWriter out) {

        out.println("----Starting migrating contacts");
        List<PatientContact> patientContacts = getPatientContactsToMigrate();
        System.out.println("-- here are the contacts to migrate----" + patientContacts.size());
        if (patientContacts.isEmpty()) {
            out.println("No patient contacts to migrate");
            return;
        }
        int counter = 0;
        for (PatientContact pc : patientContacts) {
            composePerson(pc);
            counter++;
            if(counter % 100 == 0){
                out.println("---Flushing and clearing session after "+counter+ " contacts");
                Context.flushSession();
                Context.clearSession();
                counter = 0;
            }
        }
        Context.flushSession();
        Context.clearSession();
        out.println("--Successfully completed migrating contacts");

    }

    public List<PatientContact> getPatientContactsToMigrate() {

        HTSService htsService = Context.getService(HTSService.class);
        try {
            List<PatientContact> patientContacts = htsService.getPatientContacts();
            if (patientContacts == null) {
                System.out.println("-----No patient contacts found to migrate");
                return Collections.emptyList();
            }
            patientContacts.removeIf(patientContact -> patientContact.getPatient() != null || patientContact.getVoided());
            return patientContacts;
        } catch (Exception e) {
            System.out.println("Error in getPatientContactsToMigrate: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private void saveObservations(Patient patient) {
        ConceptService conceptService = Context.getService(ConceptService.class);
        ObsService obsService = Context.getService(ObsService.class);

        for (String conceptUuid : CONCEPTS_FOR_OBS) {
            Obs obs = new Obs();
            obs.setPerson(patient);
            obs.setConcept(conceptService.getConceptByUuid(conceptUuid));
            obs.setObsDatetime(new Date());
            obs.setValueCoded(conceptService.getConceptByUuid(UNKNOWN));
            obs.setLocation(getDefaultLocation());
            obsService.saveObs(obs, "KenyaEMR edit patient");
        }
    }

    private void addRelationship(Person patient, Person contact, Integer relationshipType) {
        PersonService personService = Context.getPersonService();
        RelationshipType type = personService.getRelationshipTypeByUuid(relationshipOptionsToRelTypeMapper(relationshipType));

        Relationship relationship = new Relationship();
        relationship.setRelationshipType(type);
        relationship.setPersonA(patient);
        relationship.setPersonB(contact);

        personService.saveRelationship(relationship);
    }

    private String relationshipOptionsToRelTypeMapper(Integer relType) {
        Map<Integer, String> options = new HashMap<>();
        options.put(970, "8d91a210-c2cc-11de-8d13-0010c6dffd0f");
        options.put(971, "8d91a210-c2cc-11de-8d13-0010c6dffd0f");
        options.put(972, "8d91a01c-c2cc-11de-8d13-0010c6dffd0f");
        options.put(1528, "8d91a210-c2cc-11de-8d13-0010c6dffd0f");
        options.put(5617, "d6895098-5d8d-11e3-94ee-b35a4132a5e3");
        options.put(163565, "007b765f-6725-4ae9-afee-9966302bace4");
        options.put(162221, "2ac0d501-eadc-4624-b982-563c70035d46");
        options.put(166606, "76edc1fe-c5ce-4608-b326-c8ecd1020a73");
        return options.get(relType);
    }

    public void composePerson(PatientContact pc) {

        Patient toSave = new Patient();
        Patient savedPatient;

        toSave.setBirthdateEstimated(false);
        toSave.setDead(false);
        toSave.setDeathDate(null);
        toSave.setCauseOfDeath(null);

        try {
            if (pc.getBirthDate() != null) {
                toSave.setBirthdate(sdf.parse(sdf.format(pc.getBirthDate())));
            }
            toSave.setGender(pc.getSex());

            PersonName name = new PersonName(
                    defaultIfEmpty(pc.getLastName(), "Unknown"),
                    defaultIfEmpty(pc.getFirstName(), "Unknown"),
                    defaultIfEmpty(pc.getMiddleName(), "Unknown")
            );
            toSave.addName(name);
        } catch (ParseException e) {
            throw new RuntimeException("Failed to parse birthdate: " + e.getMessage(), e);
        }
        try {
            PersonAddress address = new PersonAddress();
            address.setAddress1(defaultIfEmpty(pc.getPhysicalAddress(), "Unknown"));
            address.setAddress2("Unknown");
            address.setCountyDistrict("Unknown");
            address.setStateProvince("Unknown");
            address.setAddress4("Unknown");
            address.setCityVillage("Unknown");
            address.setCountry("Unknown");

            toSave.addAddress(address);

            PatientWrapper wrapper = new PatientWrapper(toSave);
            wrapper.getPerson();

            wrapper.getPerson().setTelephoneContact(pc.getPhoneContact());
            wrapper.setNearestHealthFacility("Unknown");
            wrapper.setEmailAddress("Unknown");
            wrapper.setGuardianFirstName("Unknown");
            wrapper.setGuardianLastName("Unknown");

            PatientIdentifierType openmrsIdType = MetadataUtils.existing(PatientIdentifierType.class, PatientWrapper.OPENMRS_ID);
            System.out.println("====OpenMRS ID Type: " + openmrsIdType.toString());

            PatientIdentifier openmrsId = toSave.getPatientIdentifier(openmrsIdType);
            System.out.println("------------ID: " + openmrsId);

            if (openmrsId == null) {
                String generated = Context.getService(IdentifierSourceService.class).generateIdentifier(openmrsIdType, "Registration");
                openmrsId = new PatientIdentifier(generated, openmrsIdType, getDefaultLocation());
                toSave.addIdentifier(openmrsId);

                if (!toSave.getPatientIdentifier().isPreferred()) {
                    openmrsId.setPreferred(true);
                }
            }

            // Attempt to save the patient
            System.out.println("---------Attempting to save the patient: " + toSave);
            savedPatient = Context.getPatientService().savePatient(toSave);
            System.out.println("---------Successfully saved patient with ID: " + savedPatient.getPatientId());

            for (PatientIdentifier identifier : toSave.getIdentifiers()) {
                Context.getPatientService().savePatientIdentifier(identifier);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to handle address and identifiers: " + e.getMessage(), e);
        }
        try {
            SortedSet<PersonAttribute> personAttributes = new TreeSet<>();
            addPersonAttribute(personAttributes, CommonMetadata._PersonAttributeType.TELEPHONE_CONTACT, pc.getPhoneContact());
            addPersonAttribute(personAttributes, CommonMetadata._PersonAttributeType.PNS_APPROACH, pc.getPnsApproach().toString());
            addPersonAttribute(personAttributes, CommonMetadata._PersonAttributeType.PNS_PATIENT_CONTACT_BASELINE_HIV_STATUS, pc.getBaselineHivStatus());
            addPersonAttribute(personAttributes, CommonMetadata._PersonAttributeType.PNS_PATIENT_CONTACT_LIVING_WITH_PATIENT, pc.getLivingWithPatient().toString());
            addPersonAttribute(personAttributes, CommonMetadata._PersonAttributeType.PNS_PATIENT_CONTACT_IPV_OUTCOME, pc.getIpvOutcome());
            addPersonAttribute(personAttributes, CommonMetadata._PersonAttributeType.PNS_PATIENT_CONTACT_REGISTRATION_SOURCE, "1065");
            addPersonAttribute(personAttributes, CommonMetadata._PersonAttributeType.NEAREST_HEALTH_CENTER, "Unknown");

            savedPatient.setAttributes(personAttributes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to handle attributes: " + e.getMessage(), e);
        }
        saveObservations(savedPatient);

        // Add relationship
        addRelationship(pc.getPatientRelatedTo(), savedPatient, pc.getRelationType());
    }

    private void addPersonAttribute(SortedSet<PersonAttribute> personAttributes, String attributeTypeUuid, String value) {
        PersonAttributeType attributeType = MetadataUtils.existing(PersonAttributeType.class, attributeTypeUuid);
        if (attributeType != null && value != null && !value.trim().isEmpty()) {
            PersonAttribute attribute = new PersonAttribute(attributeType, value);
            personAttributes.add(attribute);
        }
    }

    private String defaultIfEmpty(String value, String defaultValue) {
        return value == null || value.trim().isEmpty() ? defaultValue : value;
    }
}
