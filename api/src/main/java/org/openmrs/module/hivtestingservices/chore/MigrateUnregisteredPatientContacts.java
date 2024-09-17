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

import org.openmrs.*;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.openmrs.util.LocationUtility.getDefaultLocation;

/**
 * handles migration of unregistered patient contacts
 */
@Component("hivtestingservices.chore.MigrateUnregisteredPatientContacts")
public class MigrateUnregisteredPatientContacts extends AbstractChore {

    @Autowired
    HTSService htsService;
    @Autowired
    ConceptService conceptService;
    @Autowired
    ObsService obsService;

/*    HTSService htsService = Context.getService(HTSService.class);
    ConceptService conceptService = Context.getService(ConceptService.class);
    ObsService obsService = Context.getService(ObsService.class);*/

    private static final String UNKNOWN = "1067AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    private static final List<String> CONCEPTS_FOR_OBS = Arrays.asList(
            "1054AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", // CIVIL_STATUS
            "1542AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", // OCCUPATION
            "1712AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", // EDUCATION
            "5629AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", // IN_SCHOOL
            "1174AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", // ORPHAN
            "165657AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"  // COUNTRY
    );

    @Override
    public void perform(PrintWriter out) {

        out.println("----Starting migrating contacts");
        List<PatientContact> patientContacts = getPatientContactsToMigrate();
        System.out.println("-- here are the contacts to migrate----" + patientContacts.size());
        processPatientContactsInBatches(patientContacts, 20);

        out.println("--Successfully completed migrating contacts");

    }

    @PostConstruct
    public List<PatientContact> getPatientContactsToMigrate() {

        List<PatientContact> patientContacts = htsService.getPatientContacts();
        if (patientContacts == null) {
            return Collections.emptyList();
        }
        patientContacts.removeIf(patientContact -> patientContact.getPatient() != null || patientContact.getVoided());
        return patientContacts;
    }

    public void processPatientContactsInBatches(List<PatientContact> patientContacts, int batchSize) {
        for (int i = 0; i < patientContacts.size(); i += batchSize) {
            List<PatientContact> batch = patientContacts.subList(i, Math.min(i + batchSize, patientContacts.size()));
            processBatch(batch);
        }
    }

    private void processBatch(List<PatientContact> batch) {
        Set<Patient> contactPatients = new HashSet<>();
        for (PatientContact pc : batch) {
            Patient patient = (Patient) composePatientFromContact(pc);
            contactPatients.add(patient);
        }
        for (Patient toSave : contactPatients) {
            savePatientAndRelatedData(toSave, batch);
        }
    }

    public Person composePatientFromContact(PatientContact pc) {
        return composePerson(pc);
    }

    protected void savePatientAndRelatedData(Patient toSave, List<PatientContact> patientContacts) {
	/*	PatientWrapper wrapper = new PatientWrapper(toSave);
		wrapper.getPerson();*/

        PatientIdentifierType openmrsIdType = MetadataUtils.existing(PatientIdentifierType.class, PatientWrapper.OPENMRS_ID);
        ensurePatientIdentifier(toSave, openmrsIdType);

        try {
            // Attempt to save the patient
            System.out.println("---------Attempting to sav ethe patient: " + toSave);
            Patient savedPatient = Context.getPatientService().savePatient(toSave);
            System.out.println(" --------We succeeded saving patient with ID: " + savedPatient.getPatientId());
            // If successful, save the patient identifiers
            savePatientIdentifiers(savedPatient);

            // Continue with saving observations and relationships
            saveObservations(savedPatient);

            for (PatientContact contact : patientContacts) {
                addRelationship(contact.getPatientRelatedTo(), savedPatient, contact.getRelationType());
            }

        } catch (Exception e) {
            // Handle the exception if saving the patient fails
            throw new RuntimeException("---Failed to save patient data: " + e.getMessage(), e);
        }
    }

    private void ensurePatientIdentifier(Patient toSave, PatientIdentifierType idType) {
        PatientIdentifier id = toSave.getPatientIdentifier(idType);
        if (id == null) {
            String generatedId = Context.getService(IdentifierSourceService.class).generateIdentifier(idType, "Registration");
            id = new PatientIdentifier(generatedId, idType, getDefaultLocation());
            id.setPreferred(true);
            toSave.addIdentifier(id);
        }
    }

    private void savePatientIdentifiers(Patient toSave) {
        for (PatientIdentifier identifier : toSave.getIdentifiers()) {
            Context.getPatientService().savePatientIdentifier(identifier);
        }
    }

    private void saveObservations(Patient patient) {

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

    public Person composePerson(PatientContact pc) {
        Person person = new Person();
        PersonName name = new PersonName(
                defaultIfEmpty(pc.getLastName(), "Unknown"),
                defaultIfEmpty(pc.getFirstName(), "Unknown"),
                defaultIfEmpty(pc.getMiddleName(), "Unknown")
        );

        PersonAddress address = new PersonAddress();
        address.setAddress1(defaultIfEmpty(pc.getPhysicalAddress(), "Unknown"));
        address.setAddress2("Unknown");
        address.setCityVillage("Unknown");
        address.setCountry("Unknown");

        Set<PersonAttribute> personAttributes = new HashSet<>(Arrays.asList(
                createPersonAttribute(CommonMetadata._PersonAttributeType.TELEPHONE_CONTACT, pc.getPhoneContact()),
                createPersonAttribute(CommonMetadata._PersonAttributeType.PNS_APPROACH, pc.getPnsApproach().toString()),
                createPersonAttribute(CommonMetadata._PersonAttributeType.PNS_PATIENT_CONTACT_BASELINE_HIV_STATUS, pc.getBaselineHivStatus()),
                createPersonAttribute(CommonMetadata._PersonAttributeType.PNS_PATIENT_CONTACT_LIVING_WITH_PATIENT, pc.getLivingWithPatient().toString()),
                createPersonAttribute(CommonMetadata._PersonAttributeType.PNS_PATIENT_CONTACT_IPV_OUTCOME, pc.getIpvOutcome()),
                createPersonAttribute(CommonMetadata._PersonAttributeType.PNS_PATIENT_CONTACT_REGISTRATION_SOURCE, "1065"),
                createPersonAttribute(CommonMetadata._PersonAttributeType.NEAREST_HEALTH_CENTER, "Unknown")
        ));
        // Format birthdate if it's not null
        String formattedBirthdate = null;
        if (pc.getBirthDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            formattedBirthdate = sdf.format(pc.getBirthDate());
        }
        person.setBirthdate(formattedBirthdate != null ? new Date(formattedBirthdate) : null);
        person.setGender(pc.getSex());
        person.addName(name);
        person.addAddress(address);
        person.setAttributes(personAttributes);
        System.out.println("---------------------Person: " + person.getFamilyName());
        return person;
    }

    private PersonAttribute createPersonAttribute(String uuid, String value) {
        PersonAttributeType attributeType = MetadataUtils.existing(PersonAttributeType.class, uuid);
        return new PersonAttribute(attributeType, value);
    }

    private String defaultIfEmpty(String value, String defaultValue) {
        return value == null || value.trim().isEmpty() ? defaultValue : value;
    }
}
