package org.openmrs.module.hivtestingservices.api.shr;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.GlobalProperty;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PatientProgram;
import org.openmrs.Person;
import org.openmrs.PersonAttributeType;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PersonService;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.context.Context;
import org.openmrs.module.hivtestingservices.api.HTSService;
import org.openmrs.module.hivtestingservices.api.PatientContact;
import org.openmrs.module.idgen.service.IdentifierSourceService;
import org.openmrs.util.PrivilegeConstants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MhealthDataExchange {

    protected static final Log log = LogFactory.getLog(MhealthDataExchange.class);
    public static final String COVID_QUARANTINE_ENROLLMENT_ENCOUNTER = "33a3a55c-73ae-11ea-bc55-0242ac130003";
    public static final String COVID_QUARANTINE_ENROLLMENT_FORM = "9a5d57b6-739a-11ea-bc55-0242ac130003";
    public static final String COVID_QUARANTINE_PROGRAM = "9a5d555e-739a-11ea-bc55-0242ac130003";
    public static final String COVID_QUARANTINE_FOLLOWUP_ENCOUNTER = "33a3a8e0-73ae-11ea-bc55-0242ac130003";
    public static final String COVID_QUARANTINE_FOLLOWUP_FORM = "33a3aab6-73ae-11ea-bc55-0242ac130003";
    public static final String TEMPERATURE_CONCEPT = "5088AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String FEVER_CONCEPT = "140238AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String COUGH_CONCEPT = "143264AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String DIFFICULTY_BREATHING_CONCEPT = "164441AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String SORE_THROAT_CONCEPT = "162737AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String HAS_SORE_THROAT_CONCEPT = "158843AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String COMMENT_CONCEPT = "160632AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String YES_CONCEPT = "1065AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String NO_CONCEPT = "1066AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String FOLLOWUP_SEQUENCE_CONCEPT = "165416AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String TELEPHONE_CONTACT = "b2c38640-2603-4629-aebd-3b54f33f1e3a";
    public static final String COVID_19_CONTACT_TRACING_FORM = "37ef8f3c-6cd2-11ea-bc55-0242ac130003";
    public static final String COVID_19_CONTACT_TRACING_ENCOUNTER = "6dd1ace2-6ce2-11ea-bc55-0242ac130003";



    ConceptService conceptService = Context.getConceptService();
    EncounterService encounterService = Context.getEncounterService();
    ProgramWorkflowService programWorkflowService = Context.getProgramWorkflowService();
    HTSService htsService = Context.getService(HTSService.class);
    PersonService personService = Context.getPersonService();

    String healthCareExposureRelType = "8ea99662-6ed3-11ea-bc55-0242ac130003";
    String coworkerRelType = "8ea9902c-6ed3-11ea-bc55-0242ac130003";
    String traveledTogetherRelType = "8ea992ac-6ed3-11ea-bc55-0242ac130003";
    String livingTogetherRelType = "8ea993ba-6ed3-11ea-bc55-0242ac130003";


    /**
     * processes results from mhealth     *
     * @param resultPayload this should be an array
     * @return
     */
    public String processMhealthPayload(String resultPayload) {

        Integer statusCode;
        String statusMsg;
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode payload = null;
        ArrayNode resultsObj = null;
        try {
            JsonNode actualObj = mapper.readTree(resultPayload);
            payload = (ObjectNode) actualObj;
        } catch (JsonProcessingException e) {
            statusCode = 400;
            statusMsg = "The payload could not be understood. An array is expected!";
            e.printStackTrace();
            return statusMsg;
        }

        resultsObj = (ArrayNode) payload.get("contacts");
        if (resultsObj.size() > 0) {
            for (int i = 0; i < resultsObj.size(); i++) {
                ObjectNode o = (ObjectNode) resultsObj.get(i);
                processContactPayload(o);
            }
        }
        return "Results updated successfully";
    }

    /**
     * Gets all patient identifers from a payload
     * @param contactObj
     * @return
     */
    private ObjectNode extractIdentifiers(ObjectNode contactObj) {

        ObjectNode patientIdentificatonNode = (ObjectNode) contactObj.get("PATIENT_IDENTIFICATION");
        ArrayNode arrIdentifiers = (ArrayNode) patientIdentificatonNode.get("INTERNAL_PATIENT_ID");
        String nationalId = null;
        String alienNumber = null;
        String passportNumber = null;
        //PASSPORT_NUMBER,NATIONAL_ID,
        if (arrIdentifiers.size() > 0) {
            for (int i =0; i < arrIdentifiers.size(); i++) {
                ObjectNode identifierNode = (ObjectNode) arrIdentifiers.get(i);
                String identifier = identifierNode.get("ID").textValue();
                String identifierType = identifierNode.get("IDENTIFIER_TYPE").textValue();
                if (StringUtils.isNotBlank(identifier) && StringUtils.isNotBlank(identifierType)) {
                    if (identifierType.equals("NATIONAL_ID")) {
                        nationalId = identifier;
                    } else if (identifierType.equals("PASSPORT_NUMBER")) {
                        passportNumber = identifier;
                    } else {
                        alienNumber = identifier;
                    }
                }
            }
        }

        ObjectNode ids = OutgoingPatientSHR.getJsonNodeFactory().objectNode();
        ids.put("national_id_number", nationalId);
        ids.put("passport_number", passportNumber);
        ids.put("alien_number", alienNumber);
        return ids;

    }

    /**
     * Processes payload from Mhealth system
     * Should get a contact/person from contact_uuid in payload or any provided identifiers
     * Should create new person and update followup details if none exists
     * Should update followup details for listed/enrolled contacts in the system
     * @param contactObj
     */
    private void processContactPayload(ObjectNode contactObj) {

        String contactUuid = contactObj.get("CONTACT_UUID").textValue();
        ObjectNode patientIdentificatonNode = (ObjectNode) contactObj.get("PATIENT_IDENTIFICATION");
        ObjectNode identifiersObj = extractIdentifiers(contactObj);
        String nationalId = null;
        String alienNumber = null;
        String passportNumber = null;
        Patient existingPatient = null;

        if (identifiersObj != null) {
            nationalId = identifiersObj.has("national_id_number") ? identifiersObj.get("national_id_number").textValue() : null;
            passportNumber = identifiersObj.has("passport_number") ? identifiersObj.get("passport_number").textValue() : null;
            alienNumber = identifiersObj.has("alien_number") ? identifiersObj.get("alien_number").textValue() : null;
        }

        existingPatient = SHRUtils.checkIfPatientExists(nationalId, passportNumber, alienNumber);

        PatientContact patientContact = null;
        Patient patientFromEmr = null;
        if (contactUuid != null) { // check if is contact in KenyaEMR
            patientContact = htsService.getPatientContactByUuid(contactUuid);
        }

        if (contactUuid != null) { // check if is patient from KenyaEMR
            patientFromEmr = Context.getPatientService().getPatientByUuid(contactUuid);
        }

        Patient contactRegistered = null;
        if (patientContact != null && patientContact.getPatient() != null) {
            contactRegistered = patientContact.getPatient();
        } else if (patientFromEmr != null) {
            contactRegistered = patientFromEmr;
        } else if (existingPatient != null) {
            contactRegistered = existingPatient;
        }

        JsonNode followups = contactObj.get("PATIENT_FOLLOWUPS");

        if (contactRegistered != null) {

            // check if contact is already registered -- may be after contacts had long been sent out
            // to mhealth system
            if (contactRegistered != null && inQuarantineProgram(contactRegistered)) {
                saveQuarantineFollowupReports(contactRegistered, (ArrayNode) followups);
            } else if (contactRegistered != null) {
                saveContactFollowupReports(contactRegistered, (ArrayNode) followups);
            }
        }  else {

            ObjectNode quarantineDetailsNode = (ObjectNode) patientIdentificatonNode.get("QUARANTINE_DETAILS");
            String dateQuarantined = quarantineDetailsNode.get("DATE_QUARANTINED").textValue();
            String placeQuarantined = quarantineDetailsNode.get("PLACE_OF_QUARANTINE").textValue();

            ObjectNode nameNode = (ObjectNode) patientIdentificatonNode.get("PATIENT_NAME");
            ObjectNode addressNode = (ObjectNode) patientIdentificatonNode.get("PATIENT_ADDRESS");
            ObjectNode physicalAddressNode = (ObjectNode) addressNode.get("PHYSICAL_ADDRESS");

            String fName = nameNode.get("FIRST_NAME").textValue();
            String mName = nameNode.get("MIDDLE_NAME").textValue();
            String lName = nameNode.get("LAST_NAME").textValue();
            String county = physicalAddressNode.get("COUNTY").textValue();
            String subCounty = physicalAddressNode.get("SUB_COUNTY").textValue();
            String ward = physicalAddressNode.get("WARD").textValue();
            String dobString = patientIdentificatonNode.get("DATE_OF_BIRTH").textValue();
            String sex = patientIdentificatonNode.get("SEX").textValue();
            String phoneNumber = patientIdentificatonNode.get("PHONE_NUMBER").textValue();

            Patient p = SHRUtils.createPatient(fName, mName, lName, SHRUtils.parseDateString(dobString, "yyyyMMdd"), sex, nationalId, passportNumber, alienNumber);
            if (p != null) {
                p = SHRUtils.addPersonAddresses(p, null, county, subCounty, null, null);
                p = SHRUtils.addPersonAttributes(p, phoneNumber, null, null);
                p = SHRUtils.savePatient(p);
            }

            if (patientContact != null) { // contact from the EMR
                patientContact.setPatient(p); // link contact to the created person
                Context.getService(HTSService.class).savePatientContact(patientContact);
                //establish relationship between new person and case
                Patient covidCase = patientContact.getPatientRelatedTo();
                addRelationship(covidCase, p, patientContact.getPnsApproach());
            }

            // date of quarantine is sufficient to denote a contact is in quarantine program
            // needs enrollment to the program
            if (dateQuarantined != null && !dateQuarantined.equals("") && StringUtils.isNotBlank(placeQuarantined)) {
                p = enrollPatientInCovidQuarantine(p, SHRUtils.parseDateString(dateQuarantined, "yyyyMMdd"), placeQuarantined);
                saveQuarantineFollowupReports(p, (ArrayNode) followups);

            } else { // just update followup
                saveContactFollowupReports(p, (ArrayNode) followups);
            }

        }
    }

    /**
     * Creates relationship for a contact and case (patient)
     * @param patient
     * @param contact
     * @param relationshipType
     */
    private void addRelationship(Person patient, Person contact, Integer relationshipType) {

        Person personA = null, personB = null;
        RelationshipType type = null;

        if (relationshipType != null) {
            personA = contact;
            personB = patient;
            type = personService.getRelationshipTypeByUuid(relationshipOptionsToRelTypeMapper(relationshipType));
        }

        Relationship rel = new Relationship();
        rel.setRelationshipType(type);
        rel.setPersonA(personA);
        rel.setPersonB(personB);

        Context.getPersonService().saveRelationship(rel);
    }

    /**
     * Gets mappings for relationship
     * @param relType
     * @return
     */
    private String relationshipOptionsToRelTypeMapper (Integer relType) {
        Map<Integer, String> options = new HashMap<Integer, String>();

        options.put(160237, coworkerRelType);
        options.put(165656, traveledTogetherRelType);
        options.put(1060, livingTogetherRelType);
        options.put(117163, healthCareExposureRelType);
        return options.get(relType);
    }

    /**
     * Checks if a contact is enrolled in quarantine program
     * @param patient
     * @return
     */
    public boolean inQuarantineProgram(Patient patient) {
        List<PatientProgram> programs = programWorkflowService.getPatientPrograms(patient, programWorkflowService.getProgramByUuid(COVID_QUARANTINE_PROGRAM), null, null, null,null, true);
        return programs.size() > 0;
    }

    /**
     * Complete creation of a patient and enrollment into quarantine program
     * @param patient
     * @param admissionDate
     * @param quarantineCenter
     * @return
     */
    private Patient enrollPatientInCovidQuarantine(Patient patient, Date admissionDate, String quarantineCenter) {

        Encounter enc = new Encounter();
        enc.setEncounterType(Context.getEncounterService().getEncounterTypeByUuid(COVID_QUARANTINE_ENROLLMENT_ENCOUNTER));
        enc.setEncounterDatetime(admissionDate);
        enc.setPatient(patient);
        enc.addProvider(Context.getEncounterService().getEncounterRole(1), Context.getProviderService().getProvider(1));
        enc.setForm(Context.getFormService().getFormByUuid(COVID_QUARANTINE_ENROLLMENT_FORM));

        // set quarantine center
        if (StringUtils.isNotBlank(quarantineCenter)) {
            Obs qCenterObs = ObsUtils.setupTextObs(patient, ObsUtils.QUARANTINE_FACILITY_NAME, quarantineCenter, admissionDate);
            enc.addObs(qCenterObs);
        }

        Obs admissionTypeObs = ObsUtils.setupCodedObs(patient, ObsUtils.PATIENT_CATEGORY, ObsUtils.NEW_PATIENT_CATEGORY, admissionDate);
        enc.addObs(admissionTypeObs);

        Context.getEncounterService().saveEncounter(enc);
        // enroll in quarantine program
        PatientProgram pp = new PatientProgram();
        pp.setPatient(patient);
        pp.setProgram(Context.getProgramWorkflowService().getProgramByUuid(COVID_QUARANTINE_PROGRAM));
        pp.setDateEnrolled(admissionDate);
        pp.setDateCreated(new Date());
        Context.getProgramWorkflowService().savePatientProgram(pp);

        return patient;
    }

    /**
     * saves a patient's quarantine followup reports
     * @param patient patient
     * @param reports list of reports
     */
    private void saveQuarantineFollowupReports(Patient patient, ArrayNode reports) {

        if (reports.size() < 1) {
            return ;
        }

        if (null == patient) {
            return;
        }

        for (int i=0; i < reports.size(); i++) {
            ObjectNode report = (ObjectNode) reports.get(i);
            String followupDate = report.get("FOLLOWUP_DATE").textValue();
            String sequenceNumberStr = report.get("DAY_OF_FOLLOWUP").textValue();
            Double sequenceNumber = Double.parseDouble(sequenceNumberStr);
            String tempStr = report.get("TEMPERATURE").textValue();
            Double temp = Double.parseDouble(tempStr);
            String cough = report.get("COUGH").textValue();
            String fever = report.get("FEVER").textValue();
            String difficultyBreathing = report.get("DIFFICULTY_BREATHING").textValue();
            //String soreThroat = report.get("SORE_THROAT").textValue();
            String comment = report.get("COMMENT").textValue();
            Date encDate = SHRUtils.parseDateString(followupDate, "yyyyMMdd");

            EncounterType et = Context.getEncounterService().getEncounterTypeByUuid(COVID_QUARANTINE_FOLLOWUP_ENCOUNTER);
            Form form = Context.getFormService().getFormByUuid(COVID_QUARANTINE_FOLLOWUP_FORM);

            if (SHRUtils.hasEncounterOnDate(et, form, patient, encDate)) {
                continue;
            }

            Encounter enc = new Encounter();
            enc.setEncounterType(et);
            enc.setEncounterDatetime(encDate);
            enc.setPatient(patient);
            enc.addProvider(Context.getEncounterService().getEncounterRole(1), Context.getProviderService().getProvider(1));
            enc.setForm(form);

            // set temp obs
            if (sequenceNumber != null) {
                Obs followUpSequenceObs = ObsUtils.setupNumericObs(patient, FOLLOWUP_SEQUENCE_CONCEPT, sequenceNumber, encDate);
                enc.addObs(followUpSequenceObs);
            }

            if (temp != null) {
                Obs tempObs = ObsUtils.setupNumericObs(patient, TEMPERATURE_CONCEPT, temp, encDate);
                enc.addObs(tempObs);
            }

            if (fever != null) {
                Obs feverObs = ObsUtils.setupCodedObs(patient, FEVER_CONCEPT, (fever.equals("YES") ? YES_CONCEPT : NO_CONCEPT), encDate);
                enc.addObs(feverObs);
            }

            if (cough != null) {
                Obs coughObs = ObsUtils.setupCodedObs(patient, COUGH_CONCEPT, (cough.equals("YES") ? YES_CONCEPT : NO_CONCEPT), encDate);
                enc.addObs(coughObs);
            }

            if (difficultyBreathing != null) {
                Obs dbObs = ObsUtils.setupCodedObs(patient, DIFFICULTY_BREATHING_CONCEPT, (difficultyBreathing.equals("YES") ? YES_CONCEPT : NO_CONCEPT), encDate);
                enc.addObs(dbObs);
            }

            if (comment != null) {
                Obs commentObs = ObsUtils.setupTextObs(patient, COMMENT_CONCEPT, comment, encDate);
                enc.addObs(commentObs);
            }
            encounterService.saveEncounter(enc);
        }

    }

    /**
     * saves a contact's quarantine followup reports
     * @param patient patient
     * @param reports list of reports
     */
    private void saveContactFollowupReports(Patient patient, ArrayNode reports) {

        if (reports.size() < 1) {
            return ;
        }

        if (null == patient) {
            return;
        }

        for (int i=0; i < reports.size(); i++) {
            ObjectNode report = (ObjectNode) reports.get(i);
            String followupDate = report.get("FOLLOWUP_DATE").textValue();
            String sequenceNumberStr = report.get("DAY_OF_FOLLOWUP").textValue();
            Double sequenceNumber = Double.parseDouble(sequenceNumberStr);
            String tempStr = report.get("TEMPERATURE").textValue();
            Double temp = Double.parseDouble(tempStr);
            String cough = report.get("COUGH").textValue();
            String fever = report.get("FEVER").textValue();
            String difficultyBreathing = report.get("DIFFICULTY_BREATHING").textValue();
            //String soreThroat = report.get("SORE_THROAT").textValue();
            String comment = report.get("COMMENT").textValue();
            Date encDate = null;
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
            try {
                encDate = df.parse(followupDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            EncounterType et = Context.getEncounterService().getEncounterTypeByUuid(COVID_19_CONTACT_TRACING_ENCOUNTER);
            Form form = Context.getFormService().getFormByUuid(COVID_19_CONTACT_TRACING_FORM);

            if (SHRUtils.hasEncounterOnDate(et, form, patient, encDate)) {
                continue;
            }
            Encounter enc = new Encounter();
            enc.setEncounterType(et);
            enc.setEncounterDatetime(encDate);
            enc.setPatient(patient);
            enc.addProvider(Context.getEncounterService().getEncounterRole(1), Context.getProviderService().getProvider(1));
            enc.setForm(form);

            // set temp obs
            if (sequenceNumber != null) {
                Obs followUpSequenceObs = ObsUtils.setupNumericObs(patient, FOLLOWUP_SEQUENCE_CONCEPT, sequenceNumber, encDate);
                enc.addObs(followUpSequenceObs);
            }

            if (temp != null) {
                Obs tempObs = ObsUtils.setupNumericObs(patient, TEMPERATURE_CONCEPT, temp, encDate);
                enc.addObs(tempObs);
            }

            if (fever != null) {
                Obs feverObs = ObsUtils.setupCodedObs(patient, FEVER_CONCEPT, (fever.equals("YES") ? YES_CONCEPT : NO_CONCEPT), encDate);
                enc.addObs(feverObs);
            }

            if (cough != null) {
                Obs coughObs = ObsUtils.setupCodedObs(patient, COUGH_CONCEPT, (cough.equals("YES") ? YES_CONCEPT : NO_CONCEPT), encDate);
                enc.addObs(coughObs);
            }

            if (difficultyBreathing != null) {
                Obs dbObs = ObsUtils.setupCodedObs(patient, DIFFICULTY_BREATHING_CONCEPT, (difficultyBreathing.equals("YES") ? YES_CONCEPT : NO_CONCEPT), encDate);
                enc.addObs(dbObs);
            }

            if (comment != null) {
                Obs commentObs = ObsUtils.setupTextObs(patient, COMMENT_CONCEPT, comment, encDate);
                enc.addObs(commentObs);
            }
            encounterService.saveEncounter(enc);
        }
    }

    /**
     * Get a list of contacts for tracing
     * @param gpLastPatientId
     * @param lastPatientId
     * @param gpLastContactId
     * @param lastContactId
     * @return
     */
    public ObjectNode getContacts(Integer gpLastPatientId, Integer lastPatientId, Integer gpLastContactId, Integer lastContactId) {

        JsonNodeFactory factory = OutgoingPatientSHR.getJsonNodeFactory();
        ArrayNode patientContactNode = OutgoingPatientSHR.getJsonNodeFactory().arrayNode();
        ObjectNode responseWrapper = factory.objectNode();

        HTSService htsService = Context.getService(HTSService.class);
        PersonService personService = Context.getPersonService();

        Set<Integer> listedContacts = getListedContacts(gpLastContactId, lastContactId);
        Set<Integer> quarantinedContacts = getContactsInQuarantineProgram(gpLastPatientId, lastPatientId);

        if (listedContacts != null && listedContacts.size() > 0) {

            for (Integer pc : listedContacts) {
                PatientContact c = htsService.getPatientContactByID(pc);
                ObjectNode contact = factory.objectNode();
                contact.put("CONTACT_UUID", c.getUuid());
                contact.put("CONTACT_STATE", "LISTED");
                contact.put("FIRST_NAME", c.getFirstName() != null ? c.getFirstName() : "");
                contact.put("MIDDLE_NAME", c.getMiddleName() != null ? c.getMiddleName() : "");
                contact.put("LAST_NAME", c.getLastName() != null ? c.getLastName() : "");
                contact.put("DATE_OF_BIRTH", c.getBirthDate() != null ? OutgoingPatientSHR.getSimpleDateFormat(OutgoingPatientSHR.getSHRDateFormat()).format(c.getBirthDate()) : "");
                contact.put("SEX", c.getSex() != null ? c.getSex() : "");
                contact.put("PHYSICAL_ADDRESS", c.getPhysicalAddress() != null ? c.getPhysicalAddress() : "");
                contact.put("PHONE_NUMBER", c.getPhoneContact() != null ? c.getPhoneContact() : "");
                patientContactNode.add(contact);

            }
        }

        if (quarantinedContacts != null && quarantinedContacts.size() > 0) {

            for (Integer pc : quarantinedContacts) {
                Patient c = Context.getPatientService().getPatient(pc);
                ObjectNode address = CovidLabDataExchange.getPatientAddress(c);

                ObjectNode contact = factory.objectNode();
                contact.put("CONTACT_UUID", c.getUuid());
                contact.put("CONTACT_STATE", "ENROLLED");
                contact.put("FIRST_NAME", c.getGivenName() != null ? c.getGivenName() : "");
                contact.put("MIDDLE_NAME", c.getMiddleName() != null ? c.getMiddleName() : "");
                contact.put("LAST_NAME", c.getFamilyName() != null ? c.getFamilyName() : "");
                contact.put("DATE_OF_BIRTH", c.getBirthdate() != null ? OutgoingPatientSHR.getSimpleDateFormat(OutgoingPatientSHR.getSHRDateFormat()).format(c.getBirthdate()) : "");
                contact.put("SEX", c.getGender() != null ? c.getGender() : "");
                contact.put("PHYSICAL_ADDRESS", address.get("POSTAL_ADDRESS"));
                contact.put("PHONE_NUMBER", getContactPhoneNumber(c,personService) != null ? getContactPhoneNumber(c,personService) : "");
                patientContactNode.add(contact);

            }
        }
        responseWrapper.put("contacts", patientContactNode);
        return responseWrapper;
    }

    /**
     * get a patient's phone contact
     * @param patient
     * @param personService
     * @return
     */
    private static String getContactPhoneNumber(Patient patient, PersonService personService) {
        PersonAttributeType phoneNumberAttrType = personService.getPersonAttributeTypeByUuid(TELEPHONE_CONTACT);
        return patient.getAttribute(phoneNumberAttrType) != null ? patient.getAttribute(phoneNumberAttrType).getValue() : "";
    }

    /**
     * Retrieves contacts listed under a case and needs follow up
     * Filters out contacts who have been registered in the system as person/patient
     * @return
     * @param lastContactEntry
     * @param lastContactId
     */
    protected Set<Integer> getListedContacts(Integer lastContactEntry, Integer lastContactId) {

        Set<Integer> eligibleList = new HashSet<Integer>();

        String sql = "";
        if (lastContactEntry != null) {
            sql = "select id from kenyaemr_hiv_testing_patient_contact where id >" + lastContactEntry + " and patient_id is null and voided=0;"; // get contacts not registered
        } else {
            sql = "select id from kenyaemr_hiv_testing_patient_contact where id <= " + lastContactId + " and patient_id is null and voided=0;";
        }

        List<List<Object>> activeList = Context.getAdministrationService().executeSQL(sql, true);
        if (!activeList.isEmpty()) {
            for (List<Object> res : activeList) {
                Integer patientId = (Integer) res.get(0);
                eligibleList.add(patientId);
            }
        }

        return eligibleList;
    }

    /**
     * Retrieves a list of those enrolled in the quarantine program
     * Should exclude those already enrolled for case investigation (COVID program)
     * @return
     */
    protected Set<Integer> getContactsInQuarantineProgram(Integer lastPatientEntry, Integer lastId) {

        Set<Integer> eligibleList = new HashSet<Integer>();

        String sql = "";
        if (lastPatientEntry != null && lastPatientEntry > 0) {
            sql = "select pp.patient_id from patient_program pp \n" +
                    "inner join (select program_id from program where uuid='9a5d555e-739a-11ea-bc55-0242ac130003') p on pp.program_id = p.program_id\n" +
                    "where pp.patient_program_id >" + lastPatientEntry + " and pp.voided=0;";
        } else {

            sql = "select pp.patient_id from patient_program pp \n" +
                    "inner join (select program_id from program where uuid='9a5d555e-739a-11ea-bc55-0242ac130003') p on pp.program_id = p.program_id\n" +
                    "where pp.patient_program_id <= " + lastId + " and pp.voided=0;";

        }

        List<List<Object>> activeList = Context.getAdministrationService().executeSQL(sql, true);
        if (!activeList.isEmpty()) {
            for (List<Object> res : activeList) {
                Integer patientId = (Integer) res.get(0);
                eligibleList.add(patientId);
            }
        }

        return eligibleList;
    }

    /**
     * Generates an OpenMRS ID using the idgen module
     *
     * @return
     */
    private static PatientIdentifier generateOpenMRSID() {
        PatientIdentifierType openmrsIDType = Context.getPatientService().getPatientIdentifierTypeByUuid("dfacd928-0370-4315-99d7-6ec1c9f7ae76");
        String generated = Context.getService(IdentifierSourceService.class).generateIdentifier(openmrsIDType, "Registration");
        PatientIdentifier identifier = new PatientIdentifier(generated, openmrsIDType, getDefaultLocation());
        return identifier;
    }

    /**
     * Gets the KenyaEMR default location
     * TODO: can idgen generate ID without location attribute?
     * @return
     */
    public static Location getDefaultLocation() {
        try {
            Context.addProxyPrivilege(PrivilegeConstants.VIEW_LOCATIONS);
            Context.addProxyPrivilege(PrivilegeConstants.VIEW_GLOBAL_PROPERTIES);
            String GP_DEFAULT_LOCATION = "kenyaemr.defaultLocation";
            GlobalProperty gp = Context.getAdministrationService().getGlobalPropertyObject(GP_DEFAULT_LOCATION);
            return gp != null ? ((Location) gp.getValue()) : null;
        }
        finally {
            Context.removeProxyPrivilege(PrivilegeConstants.VIEW_LOCATIONS);
            Context.removeProxyPrivilege(PrivilegeConstants.VIEW_GLOBAL_PROPERTIES);
        }

    }
}
