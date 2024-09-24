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

import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Form;
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
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.hivtestingservices.api.HTSService;
import org.openmrs.module.hivtestingservices.api.PatientContact;
import org.openmrs.module.hivtestingservices.util.Utils;
import org.openmrs.module.hivtestingservices.wrapper.PatientWrapper;
import org.openmrs.module.idgen.service.IdentifierSourceService;
import org.openmrs.module.kenyacore.chore.AbstractChore;
import org.openmrs.module.kenyaemr.metadata.CommonMetadata;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

import static org.openmrs.util.LocationUtility.getDefaultLocation;

/**
 * handles migration of unregistered patient contacts
 */
@Component("hivtestingservices.chore.MigrateUnregisteredPatientContacts")
public class MigrateUnregisteredPatientContacts extends AbstractChore {

    private static final String UNKNOWN = "1067AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    private static final List<String> CONCEPTS_FOR_OBS = Arrays.asList(
            "1054AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", // CIVIL_STATUS
            "1542AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", // OCCUPATION
            "1712AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", // EDUCATION
            "5629AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", // IN_SCHOOL
            "1174AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", // ORPHAN
            "165657AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"  // COUNTRY
    );
    private static final Pattern VALID_NAME_PATTERN = Pattern.compile("[^a-zA-Z ]");
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public void perform(PrintWriter out) {

        out.println("Starting migrating patient contacts");
        List<PatientContact> patientContacts = getPatientContactsToMigrate();
        if (patientContacts.isEmpty()) {
            out.println("No patient contacts to migrate");
            return;
        }
        int counter = 0;
        List<PatientContact> contactsWithExceptions = new ArrayList<>();
        for (PatientContact pc : patientContacts) {
            Patient newPatient;
            newPatient = composePerson(pc);

            if (newPatient != null) {
                handlePersonAttributes(newPatient, pc);
                addRelationship(pc.getPatientRelatedTo(), newPatient, pc.getRelationType());
                Encounter encounter = createRegistrationEncounter(newPatient);
                saveObservations(newPatient, encounter);
            } else {
                contactsWithExceptions.add(pc);

            }
            counter++;
            if (counter % 500 == 0) {
                Context.flushSession();
                Context.clearSession();
                counter = 0;
            }
        }

        Context.flushSession();
        Context.clearSession();
        if (!contactsWithExceptions.isEmpty()) {
            out.println("Finished migrating patient contacts, with exceptions");
        } else {
            out.println("Finished migrating patient contacts");
        }
    }

    public List<PatientContact> getPatientContactsToMigrate() {

        HTSService htsService = Context.getService(HTSService.class);
        try {
            List<PatientContact> patientContacts = htsService.getPatientContacts();
            if (patientContacts == null) {
                System.out.println("No patient contacts to migrate");
                return Collections.emptyList();
            }
            patientContacts.removeIf(patientContact -> patientContact.getPatient() != null || patientContact.getVoided());
            return patientContacts;
        } catch (Exception e) {
            System.out.println("Error getPatientContactsToMigrate: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private void saveObservations(Person person, Encounter encounter) {
        ConceptService conceptService = Context.getService(ConceptService.class);
        ObsService obsService = Context.getService(ObsService.class);
        try {
            for (String conceptUuid : CONCEPTS_FOR_OBS) {
                Obs obs = new Obs();
                obs.setPerson(person);
                obs.setConcept(conceptService.getConceptByUuid(conceptUuid));
                obs.setObsDatetime(new Date());
                obs.setValueCoded(conceptService.getConceptByUuid(UNKNOWN));
                obs.setLocation(Utils.getDefaultLocation());
                obs.setEncounter(encounter);
                obsService.saveObs(obs, "KenyaEMR new patient");
            }
        } catch (Exception e) {
            System.err.println("Error saving observations: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addRelationship(Patient patient, Patient contact, Integer relationshipType) {

        try {
            PersonService personService;
            personService = Context.getService(PersonService.class);
            RelationshipType relType;
            try {
                String relTypeUuid = relationshipOptionsToRelTypeMapper(relationshipType);
                if (relTypeUuid == null) {
                    return;
                }
            } catch (Exception e) {
                System.err.println("Error fetching relationship from relationship map: " + e.getMessage());
                e.printStackTrace();
                return;
            }
            try {
                String relTypeUuid = relationshipOptionsToRelTypeMapper(relationshipType);
                relType = personService.getRelationshipTypeByUuid(relTypeUuid);
            } catch (Exception e) {
                System.err.println("Error fetching RelationshipType: " + e.getMessage());
                e.printStackTrace();
                return;
            }
            try {
                Relationship relationship = new Relationship();
                relationship.setRelationshipType(relType);
                relationship.setPersonA(patient);
                relationship.setPersonB(contact);
                relationship.setStartDate(new Date());

                personService.saveRelationship(relationship);
            } catch (Exception e) {
                System.err.println("Error saving relationship: " + e.getMessage());
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String relationshipOptionsToRelTypeMapper(Integer relType) {
        try {
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
        } catch (Exception e) {
            System.err.println("Error mapping relationship type: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public Patient composePerson(PatientContact pc) {

        Patient toSave = new Patient();
        Patient savedPatient = new Patient();
        try {
            toSave.setBirthdateEstimated(false);
            toSave.setDead(false);
            toSave.setDeathDate(null);
            toSave.setCauseOfDeath(null);

            if (pc.getBirthDate() != null) {
                toSave.setBirthdate(sdf.parse(sdf.format(pc.getBirthDate())));
            }
            toSave.setGender(pc.getSex());

            PersonName name = new PersonName(
                    defaultIfEmpty(cleanName(pc.getFirstName()), "Unknown"),
                    defaultIfEmpty(cleanName(pc.getMiddleName()), "Unknown"),
                    defaultIfEmpty(cleanName(pc.getLastName()), "Unknown")
            );
            toSave.addName(name);

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse birthdate: " + e.getMessage(), e);
        }
        try {
            SortedSet<PersonAddress> addresses = new TreeSet<>();
            PersonAddress address = new PersonAddress();

            address.setAddress1(defaultIfEmpty(pc.getPhysicalAddress(), "Unknown"));
            address.setAddress2("Unknown");
            address.setCountyDistrict("Unknown");
            address.setStateProvince("Unknown");
            address.setAddress4("Unknown");
            address.setCityVillage("Unknown");
            address.setCountry("Unknown");

            addresses.add(address);
            toSave.setAddresses(addresses);

            PatientWrapper wrapper = new PatientWrapper(toSave);

            wrapper.getPerson().setTelephoneContact(pc.getPhoneContact());
            wrapper.setNearestHealthFacility("Unknown");
            wrapper.setEmailAddress("Unknown");
            wrapper.setGuardianFirstName("Unknown");
            wrapper.setGuardianLastName("Unknown");

            PatientIdentifierType openmrsIdType = MetadataUtils.existing(PatientIdentifierType.class, PatientWrapper.OPENMRS_ID);

            PatientIdentifier openmrsId = toSave.getPatientIdentifier(openmrsIdType);

            if (openmrsId == null) {
                String generated = Context.getService(IdentifierSourceService.class).generateIdentifier(openmrsIdType, "Registration");
                openmrsId = new PatientIdentifier(generated, openmrsIdType, getDefaultLocation());
                toSave.addIdentifier(openmrsId);

                if (!toSave.getPatientIdentifier().isPreferred()) {
                    openmrsId.setPreferred(true);
                }
            }

        } catch (Exception e) {
            System.err.println("Error adding address and identifiers: " + e.getMessage());
            e.printStackTrace();
        }
        try {
            savedPatient = Context.getPatientService().savePatient(toSave);
            Person savedPerson = savedPatient;
        } catch (Exception e) {
            System.err.println("Error saving person: " + e.getMessage());
            e.printStackTrace();
        }

        return savedPatient;
    }

    private void handlePersonAttributes(Person savedPatient, PatientContact pc) {
        try {
            Map<String, String> attributes = new LinkedHashMap<>();
            attributes.put(CommonMetadata._PersonAttributeType.TELEPHONE_CONTACT, pc.getPhoneContact());
            attributes.put(CommonMetadata._PersonAttributeType.PNS_APPROACH,
                    pc.getPnsApproach() != null ? pc.getPnsApproach().toString() : null);
            attributes.put(CommonMetadata._PersonAttributeType.PNS_PATIENT_CONTACT_BASELINE_HIV_STATUS,
                    pc.getBaselineHivStatus());
            attributes.put(CommonMetadata._PersonAttributeType.PNS_PATIENT_CONTACT_LIVING_WITH_PATIENT,
                    pc.getLivingWithPatient() != null ? pc.getLivingWithPatient().toString() : null);
            attributes.put(CommonMetadata._PersonAttributeType.PNS_PATIENT_CONTACT_IPV_OUTCOME, pc.getIpvOutcome());
            attributes.put(CommonMetadata._PersonAttributeType.PNS_PATIENT_CONTACT_REGISTRATION_SOURCE, "1065");
            attributes.put(CommonMetadata._PersonAttributeType.NEAREST_HEALTH_CENTER, "Unknown");

            attributes.forEach((attributeTypeUuid, value) -> {
                try {
                    PersonAttributeType attributeType = MetadataUtils.existing(PersonAttributeType.class, attributeTypeUuid);
                    if (attributeType != null && value != null && !value.trim().isEmpty()) {
                        PersonAttribute attribute = new PersonAttribute(attributeType, value);
                        savedPatient.addAttribute(attribute);
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Failed to add person attribute: " + attributeTypeUuid + " - " + e.getMessage(), e);
                }
            });

        } catch (Exception e) {
            throw new RuntimeException("Failed to handle person attributes: " + e.getMessage(), e);
        }
    }

    private Encounter createRegistrationEncounter(Patient contact) {
        EncounterService encounterService = Context.getEncounterService();
        EncounterType encounterType = encounterService.getEncounterTypeByUuid(CommonMetadata._EncounterType.REGISTRATION);
        Encounter encounter = new Encounter();
        encounter.setEncounterType(encounterType);
        encounter.setEncounterType(encounterType);
        encounter.setEncounterDatetime(new Date());
        encounter.setPatient(contact);
        encounter.setLocation(getDefaultLocation());
        encounter.setForm(MetadataUtils.existing(Form.class, CommonMetadata._Form.BASIC_REGISTRATION));
        encounterService.saveEncounter(encounter);
        return encounter;
    }

    private String defaultIfEmpty(String value, String defaultValue) {
        return value == null || value.trim().isEmpty() ? defaultValue : value;
    }

    public static String cleanName(String name) {
        if (name == null) {
            return null;
        }
        return VALID_NAME_PATTERN.matcher(name).replaceAll("");
    }
}
