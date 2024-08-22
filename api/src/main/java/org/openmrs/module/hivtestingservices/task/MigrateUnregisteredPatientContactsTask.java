package org.openmrs.module.hivtestingservices.task;

import org.openmrs.*;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.hivtestingservices.api.HTSService;
import org.openmrs.module.hivtestingservices.api.PatientContact;
import org.openmrs.module.hivtestingservices.wrapper.PatientWrapper;
import org.openmrs.module.idgen.service.IdentifierSourceService;
import org.openmrs.module.kenyaemr.metadata.CommonMetadata;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.openmrs.util.PrivilegeConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class MigrateUnregisteredPatientContactsTask extends AbstractTask {
    private static final Logger log = LoggerFactory.getLogger(MigrateUnregisteredPatientContactsTask.class);

    private static final String UNKNOWN = "1067AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    private static final List<String> CONCEPTS_FOR_OBS = Arrays.asList(
            "1054AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", // CIVIL_STATUS
            "1542AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", // OCCUPATION
            "1712AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", // EDUCATION
            "5629AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", // IN_SCHOOL
            "1174AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", // ORPHAN
            "165657AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"  // COUNTRY
    );

    private final HTSService htsService;
    private final ObsService obsService;
    private final ConceptService conceptService;

    public MigrateUnregisteredPatientContactsTask() {
        htsService = Context.getService(HTSService.class);
        obsService = Context.getObsService();
        conceptService = Context.getConceptService();
    }

    @Override
    public void execute() throws APIException {
        if (!isExecuting) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Starting Contact migration Task...");
                }
                log.info("Started Migrating unregistered contacts");
                startExecuting();
                save();
            } catch (Exception e) {
                log.error("Error occurred during migration", e);
                throw new APIException("Error occurred during migration", e);
            } finally {
                stopExecuting();
            }
        } else {
            log.warn("Migration task is already executing.");
        }
    }

    private void save() {
        List<PatientContact> patientContacts = getPatientContactsToMigrate();
        if (patientContacts.isEmpty()) {
            return;
        }

        Set<Patient> contactPatients = new HashSet<>();
        for (PatientContact pc : patientContacts) {
            Patient patient = (Patient) getPerson(pc);
            contactPatients.add(patient);
        }

        for (Patient toSave : contactPatients) {
            savePatientAndRelatedData(toSave, patientContacts);
        }
    }

    private List<PatientContact> getPatientContactsToMigrate() {
        List<PatientContact> patientContacts = htsService.getPatientContacts();
        if (patientContacts == null) {
            return Collections.emptyList();
        }
        for (PatientContact patientContact : patientContacts) {
            if (patientContact.getPatient() == null && !patientContact.getVoided()) {
                patientContacts.remove(patientContact);
            }
        }
        return patientContacts;
    }

    private Person getPerson(PatientContact pc) {
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

        person.setBirthdate(pc.getBirthDate());
        person.setGender(pc.getSex());
        person.addAddress(address);
        person.setAttributes(personAttributes);
        person.addName(name);

        return person;
    }

    private void savePatientAndRelatedData(Patient toSave, List<PatientContact> patientContacts) {
        PatientWrapper wrapper = new PatientWrapper(toSave);
        wrapper.getPerson();

        PatientIdentifierType openmrsIdType = MetadataUtils.existing(PatientIdentifierType.class, PatientWrapper.OPENMRS_ID);
        ensurePatientIdentifier(toSave, openmrsIdType);

        Patient savedPatient = Context.getPatientService().savePatient(toSave);
        savePatientIdentifiers(toSave);
        saveObservations(savedPatient);

        for (PatientContact contact : patientContacts) {
            addRelationship(contact.getPatientRelatedTo(), savedPatient, contact.getRelationType());
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
        List<Obs> obsToSave = new ArrayList<>();
        for (String conceptUuid : CONCEPTS_FOR_OBS) {
            Obs obs = new Obs();
            obs.setPerson(patient);
            obs.setConcept(conceptService.getConceptByUuid(conceptUuid));
            obs.setObsDatetime(new Date());
            obs.setValueCoded(conceptService.getConceptByUuid(UNKNOWN));
            obs.setLocation(getDefaultLocation());
            obsToSave.add(obs);
        }
        for (Obs obs : obsToSave) {
            obsService.saveObs(obs, "KenyaEMR edit patient");
        }
    }

    private void addRelationship(Person patient, Person contact, Integer relationshipType) {
        PersonService personService = Context.getPersonService();
        RelationshipType type = personService.getRelationshipTypeByUuid(relationshipOptionsToRelTypeMapper(relationshipType));

        Relationship relationship = new Relationship();
        relationship.setRelationshipType(type);
        relationship.setPersonA(contact);
        relationship.setPersonB(patient);

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

    private Location getDefaultLocation() {
        try {
            Context.addProxyPrivilege(PrivilegeConstants.GET_LOCATIONS);
            Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
            String locationUuid = Context.getAdministrationService().getGlobalProperty("kenyaemr.defaultLocation");
            return Context.getLocationService().getLocationByUuid(locationUuid);
        } finally {
            Context.removeProxyPrivilege(PrivilegeConstants.GET_LOCATIONS);
            Context.removeProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
        }
    }

    private PersonAttribute createPersonAttribute(String uuid, String value) {
        PersonAttributeType attributeType = MetadataUtils.existing(PersonAttributeType.class, uuid);
        return new PersonAttribute(attributeType, value);
    }

    private String defaultIfEmpty(String value, String defaultValue) {
        return value == null || value.trim().isEmpty() ? defaultValue : value;
    }

}