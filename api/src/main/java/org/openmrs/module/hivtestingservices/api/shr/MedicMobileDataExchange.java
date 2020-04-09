package org.openmrs.module.hivtestingservices.api.shr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Attributable;
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
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.context.Context;
import org.openmrs.module.hivtestingservices.api.HTSService;
import org.openmrs.module.hivtestingservices.api.PatientContact;
import org.openmrs.module.hivtestingservices.metadata.HTSMetadata;
import org.openmrs.module.idgen.service.IdentifierSourceService;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MedicMobileDataExchange {

    protected static final Log log = LogFactory.getLog(MedicMobileDataExchange.class);
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
     * processes payload posted from CHT
     * @param resultPayload
     * @return
     */
    public String processTraceReport(String resultPayload) {

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonNode = null;
        try {
            jsonNode = (ObjectNode) mapper.readTree(resultPayload);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (jsonNode != null) {

            ObjectNode contactNode = (ObjectNode) jsonNode.get("contact");

            String uuid = contactNode.get("_id").textValue();
            PatientContact c = htsService.getPatientContactByUuid(uuid);
            if (c == null) {
                return "Invalid request. There is no such contact in the system" ;
            }
            Patient contactRegistered = c.getPatient();
            String fName = c.getFirstName();
            String mName = c.getMiddleName();
            String lName = c.getLastName();

            String county = contactNode.get("county").textValue();
            String subCounty = contactNode.get("subcounty").textValue();
            String postalAddress = contactNode.get("postal_address").textValue();
            String phoneNumber = contactNode.get("phone").textValue();
            String dobString = contactNode.get("date_of_birth").textValue();
            String idNumber = contactNode.get("national_id").textValue();
            Date dob = parseDateString(dobString, "yyyy-MM-dd");

            ObjectNode traceReport = (ObjectNode) jsonNode.get("trace");
            String encDateStr = traceReport.get("date_last_contacted").textValue();

            Date encounterdate = parseDateString(encDateStr, "yyyy-MM-dd");
            Double followupSequence = traceReport.get("follow_up_count").doubleValue();
            Double temp = traceReport.get("temperature").doubleValue();
            String cough = traceReport.get("cough").textValue();
            String fever = traceReport.get("fever").textValue();
            String soreThroat = traceReport.get("sore_throat").textValue();
            String difficultyBreathing = traceReport.get("difficulty_breathing").textValue();

            if (contactRegistered != null) {
                saveContactFollowupReport(contactRegistered, encounterdate, temp, fever, cough, difficultyBreathing, followupSequence, soreThroat);
            } else {
                Patient patient = createPatient(fName, mName, lName, dob, c.getSex(), idNumber);
                patient = addPersonAddresses(patient, null, county, subCounty, null, postalAddress);
                patient = addPersonAttributes(patient, phoneNumber, null, null);
                patient = savePatient(patient);

                saveContactFollowupReport(patient, encounterdate, temp, fever, cough, difficultyBreathing, followupSequence, soreThroat);
                c.setPatient(patient); // link contact to the created person
                Context.getService(HTSService.class).savePatientContact(c);
                //establish relationship between new person and case
                Patient covidCase = c.getPatientRelatedTo();
                addRelationship(covidCase, patient, c.getPnsApproach());
            }
        }
        return "Contact followup created successfully";
    }


    private void processContactObject(ObjectNode contact) {
        String patientStatus = contact.get("CONTACT_STATE").textValue();

        if (patientStatus !=null && patientStatus.equals("case_investigation_form")) {
            processNewRegistration(contact);
        } else if (patientStatus != null && patientStatus.equals("trace_report")) {
            processFollowupReports(contact);
        } else {

        }
    }

    /**
     * Processes followup
     * @param contact
     */
    private void processFollowupReports(ObjectNode contact) {

    }

    public String processCaseReport(String resultPayload) {

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonNode = null;
        try {
            jsonNode = (ObjectNode) mapper.readTree(resultPayload);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String result = processNewRegistration(jsonNode);
        return result;
    }
    private String processNewRegistration(ObjectNode contactObject) {
        if (contactObject == null) {
            return "An empty payload was encountered!";
        }

        ObjectNode contactNode = (ObjectNode) contactObject.get("contact");

        if (contactNode != null) {
            String fName = null;
            String mName = null;

            String lName = contactNode.get("family_name").textValue();
            String givenNames = contactNode.get("given_names").textValue();
            String county = contactNode.get("county").textValue();
            String subCounty = contactNode.get("subcounty").textValue();
            String postalAddress = contactNode.get("postal_address").textValue();
            String location = contactNode.get("place_location").textValue();
            String sex = contactNode.get("sex").textValue();
            String estate = contactNode.get("sublocation_estate").textValue();
            String phoneNumber = contactNode.get("phone").textValue();
            String dobString = contactNode.get("date_of_birth").textValue();
            String idNumber = contactNode.get("national_id").textValue();
            String passportNumber = contactNode.get("passport_number").textValue();
            String alienNumber = contactNode.get("alien_id").textValue();
            Date dob = parseDateString(dobString, "yyyy-MM-dd");

            String arrGivenNames[] = givenNames.split(" ");
            if (arrGivenNames.length > 1) {
                fName = arrGivenNames[0];
                mName = arrGivenNames[1];
            } else if (arrGivenNames.length > 0) {
                fName = arrGivenNames[0];
            }

            if(org.apache.commons.lang3.StringUtils.isNotBlank(sex)) {
                sex = sex.equals("male") ? "M" : sex.equals("female") ? "F" : "U";
            } else {
                sex = "U";
            }

            Patient patient = createPatient(fName, mName, lName, dob, sex, idNumber);
            patient = savePatient(patient);
            patient = addPersonAddresses(patient, null, county, subCounty, null, postalAddress);
            patient = addPersonAttributes(patient, phoneNumber, null, null);


            if (patient != null) {

                ObjectNode cif = (ObjectNode) contactObject.get("report");

                if (cif != null) {
                    enrollForCovidCaseInvestigation(patient, cif);
                }
            }
            return "Successfully created a covid case";

        }

        return "There were no case information provided";
    }

    private void enrollForCovidCaseInvestigation(Patient patient, ObjectNode cif) {

        ObjectNode fields = (ObjectNode) cif.get("fields");
        ObjectNode reportingInfo = (ObjectNode) fields.get("group_reporting_info");
        ObjectNode patientInfo = (ObjectNode) fields.get("group_patient_information");
        ObjectNode clinicalInfo = (ObjectNode) fields.get("group_clinical_information");
        ObjectNode symptomsInfo = (ObjectNode) fields.get("group_patient_symptoms");
        ObjectNode comorbidityInfo = (ObjectNode) fields.get("group_conditions_comorbidity");
        ObjectNode exposureInfo = (ObjectNode) fields.get("group_exposure_travel_information");


        // reporting info
        String reportingDate = reportingInfo.get("date_of_reporting").textValue();
        String reportingFacility = reportingInfo.get("reporting_facility").textValue();
        String reportingCounty = reportingInfo.get("county").textValue();
        String reportingSubCounty = reportingInfo.get("subcounty").textValue();
        String poeDetection = reportingInfo.get("poe_detected").textValue();
        String poeDetectionDate = reportingInfo.get("poe_detection_date").textValue();


        // reporting info
        String caseIdentifier = patientInfo.get("case_identifier").textValue();

        // clinical information
        String symptomatic = clinicalInfo.get("patient_condition").textValue();
        String symptomsOnsetDate = clinicalInfo.get("symptoms_onset_date").textValue();
        String firstAdmissionDate = clinicalInfo.get("first_admission_date").textValue();
        String hospitalName = clinicalInfo.get("hospital_name").textValue();
        String isolationDate = clinicalInfo.get("isolation_date").textValue();
        String patientVentilated = clinicalInfo.get("patient_ventilated").textValue();
        String patientHealthStatus = clinicalInfo.get("patient_health_status").textValue();
        String deathDate = clinicalInfo.get("death_date").textValue();
        Date encDate = parseDateString(reportingDate, "yyyyMMdd");

        // exposure
        String patientTravelledPast2Weeks = exposureInfo.get("patient_travelled_past_2_weeks").textValue();
        String occupation = exposureInfo.get("occupation").textValue();

        // comorbidity
        String comorbidities = comorbidityInfo.get("conditions_comorbidity").textValue();

        EncounterType encType = Context.getEncounterService().getEncounterTypeByUuid(HTSMetadata.COVID_19_CASE_INVESTIGATION_ENCOUNTER);
        Form form = Context.getFormService().getFormByUuid(HTSMetadata.COVID_19_CASE_INVESTIGATION_FORM);
        Encounter encounter = new Encounter();
        encounter.setEncounterType(encType);
        encounter.setPatient(patient);
        encounter.setEncounterDatetime(encDate);
        encounter.setForm(form);

        // build observations
        if (reportingCounty != null) {
            encounter.addObs(ObsUtils.setupTextObs(patient, ObsUtils.REPORTING_COUNTY, reportingCounty, encDate));
        }
        if(reportingSubCounty != null) {
            encounter.addObs(ObsUtils.setupTextObs(patient, ObsUtils.REPORTING_SUB_COUNTY, reportingSubCounty, encDate));
        }
        if (poeDetection != null) {
            encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.DETECTION_POINT, poeDetection.equals("yes") ? ObsUtils.POE : ObsUtils.COMMUNITY, encDate));
        } else {
            encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.DETECTION_POINT, ObsUtils.UNKNOWN, encDate));
        }
        if(poeDetectionDate != null) {
            encounter.addObs(ObsUtils.setupDatetimeObs(patient, ObsUtils.DATE_DETECTED, parseDateString(poeDetectionDate, "yyyyMMdd"), encDate));
        }
        if(symptomatic != null) {
            encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.PATIENT_SYMPTOMATIC, symptomatic.equals("symptomatic") ? ObsUtils.YES_CONCEPT : ObsUtils.NO_CONCEPT, encDate));
        }
        if(symptomsOnsetDate != null) {
            encounter.addObs(ObsUtils.setupDatetimeObs(patient, ObsUtils.DATE_OF_ONSET_OF_SYMPTOMS, parseDateString(symptomsOnsetDate, "yyyyMMdd"), encDate));
        }
        if(firstAdmissionDate != null || hospitalName != null) {
            encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.ADMISSION_TO_HOSPITAL, ObsUtils.YES_CONCEPT, encDate));
        } else {
            encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.ADMISSION_TO_HOSPITAL, ObsUtils.NO_CONCEPT, encDate));
        }
        if(firstAdmissionDate != null) {
            encounter.addObs(ObsUtils.setupDatetimeObs(patient, ObsUtils.DATE_OF_ADMISSION_TO_HOSPITAL, parseDateString(firstAdmissionDate, "yyyyMMdd"), encDate));
        }
        if(hospitalName != null) {
            encounter.addObs(ObsUtils.setupTextObs(patient, ObsUtils.NAME_OF_HOSPITAL_ADMITTED, hospitalName, encDate));
        }
        if(isolationDate != null) {
            encounter.addObs(ObsUtils.setupDatetimeObs(patient, ObsUtils.DATE_OF_ISOLATION, parseDateString(isolationDate, "yyyyMMdd"), encDate));
        }
        if(patientVentilated != null) {
            encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.WAS_PATIENT_VENTILATED, patientVentilated.equals("yes") ? ObsUtils.YES_CONCEPT : ObsUtils.NO_CONCEPT, encDate));
        }
        if(patientHealthStatus != null && patientHealthStatus.equals("stable")) {
            encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.PATIENT_STATUS_AT_REPORTING, ObsUtils.PATIENT_STATUS_STABLE, encDate));
        } else if(patientHealthStatus != null && patientHealthStatus.equals("severely ill")) {
            encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.PATIENT_STATUS_AT_REPORTING, ObsUtils.PATIENT_STATUS_SEVERELY_ILL, encDate));
        } else if(patientHealthStatus != null && patientHealthStatus.equals("dead")) {
            encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.PATIENT_STATUS_AT_REPORTING, ObsUtils.PATIENT_STATUS_DEAD, encDate));
        } else {
            encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.PATIENT_STATUS_AT_REPORTING, ObsUtils.UNKNOWN, encDate));
        }
        if(deathDate != null) {
            encounter.addObs(ObsUtils.setupDatetimeObs(patient, ObsUtils.DATE_OF_DEATH, parseDateString(deathDate, "yyyyMMdd"), encDate));
        }
        if(patientTravelledPast2Weeks != null) {
            encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.HISTORY_OF_TRAVEL, patientTravelledPast2Weeks.equals("yes") ? ObsUtils.YES_CONCEPT : ObsUtils.NO_CONCEPT, encDate));
        }
        if(occupation != null) {
            encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.OCCUPATION, occupation.equals("yes") ? ObsUtils.YES_CONCEPT : ObsUtils.NO_CONCEPT, encDate));
        }

        if (comorbidities != null && !comorbidities.equals("")) {
            String comorbitiesArr[] = comorbidities.split(" ");
            if (comorbitiesArr.length > 0) {
                encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.HAS_COMORBIDITIES, ObsUtils.YES_CONCEPT, encDate));

                for (int i = 0; i < comorbitiesArr.length; i++) {
                    String condition = comorbitiesArr[i];
                    if (condition == null || condition.equals("")) {
                        continue;
                    }

                    if (condition.equals("diabetes")) {
                        encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.DIABETES, ObsUtils.YES_CONCEPT, encDate));
                    } else if (condition.equals("liver_disease")) {
                        encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.LIVER_DISEASE, ObsUtils.YES_CONCEPT, encDate));
                    } else if (condition.equals("renal_disease")) {
                        encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.RENAL_DISEASE, ObsUtils.YES_CONCEPT, encDate));
                    } else if (condition.equals("hypertension")) {
                        encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.HYPERTENSION, ObsUtils.YES_CONCEPT, encDate));
                    } else if (condition.equals("chronic_neurological_disease")) {
                        encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.CHRONIC_NEUROLOGICAL_DISEASE, ObsUtils.YES_CONCEPT, encDate));
                    } else if (condition.equals("post_partum_less_than_6_weeks")) {
                        encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.POST_PARTUM_LESS_THAN_6_WEEKS, ObsUtils.YES_CONCEPT, encDate));
                    } else if (condition.equals("chronic_lung_disease")) {
                        encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.CHRONIC_LUNG_DISEASE, ObsUtils.YES_CONCEPT, encDate));
                    } else if (condition.equals("immunodeficiency")) {
                        encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.IMMUNODEFICIENCY, ObsUtils.YES_CONCEPT, encDate));
                    } else if (condition.equals("malignancy")) {
                        encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.MALIGNANCY, ObsUtils.YES_CONCEPT, encDate));
                    }
                }
            } else {
                encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.HAS_COMORBIDITIES, ObsUtils.NO_CONCEPT, encDate));
            }
        } else {
            encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.HAS_COMORBIDITIES, ObsUtils.UNKNOWN, encDate));
        }

        encounterService.saveEncounter(encounter);

        PatientProgram pp = new PatientProgram();
        pp.setPatient(patient);
        pp.setProgram(Context.getProgramWorkflowService().getProgramByUuid(HTSMetadata.COVID_19_CASE_INVESTIGATION_PROGRAM));
        pp.setDateEnrolled(encDate);
        pp.setDateCreated(new Date());
        Context.getProgramWorkflowService().savePatientProgram(pp);

    }


    private void processListedContacts(ObjectNode contactObj) {

        String contactUuid = contactObj.get("CONTACT_UUID").textValue();
        PatientContact patientContact = htsService.getPatientContactByUuid(contactUuid);
        JsonNode followups = contactObj.get("PATIENT_FOLLOWUPS");

        // check if contact is already registered -- may be after contacts had long been pushed out
        // to mhealth system
        if (patientContact.getPatient() != null && inQuarantineProgram(patientContact.getPatient())) {
            saveQuarantineFollowupReports(patientContact.getPatient(), (ArrayNode) followups);
        } else if (patientContact.getPatient() != null){
            saveContactFollowupReports(patientContact.getPatient(), (ArrayNode) followups);
        } else {
            // check if quarantine details exist
            ObjectNode patientIdentificatonNode = (ObjectNode) contactObj.get("PATIENT_IDENTIFICATION");
            ObjectNode quarantineDetailsNode = (ObjectNode) patientIdentificatonNode.get("QUARANTINE_DETAILS");
            String dateQuarantined = quarantineDetailsNode.get("DATE_QUARANTINED").textValue();
            String placeQuarantined = quarantineDetailsNode.get("PLACE_OF_QUARANTINE").textValue();

            ObjectNode nameNode = (ObjectNode) patientIdentificatonNode.get("PATIENT_NAME");
            ObjectNode addressNode = (ObjectNode) patientIdentificatonNode.get("PATIENT_ADDRESS");
            ArrayNode identifierNode = (ArrayNode) patientIdentificatonNode.get("INTERNAL_PATIENT_ID");
            ObjectNode physicalAddressNode = (ObjectNode) addressNode.get("PHYSICAL_ADDRESS");

            String fName = nameNode.get("FIRST_NAME").textValue();
            String mName = nameNode.get("MIDDLE_NAME").textValue();
            String lName = nameNode.get("LAST_NAME").textValue();
            String county = physicalAddressNode.get("COUNTY").textValue();
            String subCounty = physicalAddressNode.get("SUB_COUNTY").textValue();
            String ward = physicalAddressNode.get("WARD").textValue();
            ObjectNode idNoNode = (ObjectNode) identifierNode.get(0);
            String idNo = idNoNode.get("ID").textValue();
            String dobString = patientIdentificatonNode.get("DATE_OF_BIRTH").textValue();
            String sex = patientIdentificatonNode.get("SEX").textValue();
            String phoneNumber = patientIdentificatonNode.get("PHONE_NUMBER").textValue();

            Patient p = createPatient(fName, mName, lName, parseDateString(dobString,"yyyyMMdd"), sex, idNo);
            p = addPersonAddresses(p, null, county, subCounty, null, null);
            p = addPersonAttributes(p, phoneNumber, null, null);
            p = savePatient(p);

            patientContact.setPatient(p); // link contact to the created person
            //establish relationship between new person and case
            Patient covidCase = patientContact.getPatientRelatedTo();
            addRelationship(covidCase, p, patientContact.getPnsApproach());
            // date of quarantine is sufficient to denote a contact is in quarantine program
            // needs enrollment to the program
            if (dateQuarantined != null && !dateQuarantined.equals("")) {

                p = enrollPatientInCovidQuarantine(p, parseDateString(dateQuarantined, "yyyyMMdd"), placeQuarantined);
                saveQuarantineFollowupReports(p, (ArrayNode) followups);


            } else { // just update followup

                saveContactFollowupReports(p, (ArrayNode) followups);
            }

        }
    }

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

    private String relationshipOptionsToRelTypeMapper (Integer relType) {
        Map<Integer, String> options = new HashMap<Integer, String>();

        options.put(160237, coworkerRelType);
        options.put(165656, traveledTogetherRelType);
        options.put(1060, livingTogetherRelType);
        options.put(117163, healthCareExposureRelType);
        return options.get(relType);
    }
    /**
     * Process reports of those registered in the system
     * @param traceReports
     */
    private void processRegisteredContact(ObjectNode traceReports) {
        PatientService patientService = Context.getPatientService();
        String patientUuid = traceReports.get("CONTACT_UUID").textValue();
        // check if patient is enrolled in quarantine program

        Patient patient = patientService.getPatientByUuid(patientUuid);
        JsonNode followups = traceReports.get("PATIENT_FOLLOWUPS");
        if (inQuarantineProgram(patient)) { // update quarantine followup form
            saveQuarantineFollowupReports(patient, (ArrayNode) followups);
        } else {
            saveContactFollowupReports(patient, (ArrayNode) followups);
        }
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
     * Create a new patient
     * @param
     * @param dob
     * @param sex
     * @param idNo
     * @return
     */
    private Patient createPatient(String fName, String mName, String lName, Date dob, String sex, String idNo) {

        Patient patient = null;
        String PASSPORT_NUMBER = "e1e80daa-6d7e-11ea-bc55-0242ac130003";

        if (fName != null && !fName.equals("") && lName != null && !lName.equals("") ) {

            patient = new Patient();
            if (sex == null || sex.equals("") || StringUtils.isEmpty(sex)) {
                sex = "U";
            }
            patient.setGender(sex);
            PersonName pn = new PersonName();
            pn.setGivenName(fName);
            pn.setFamilyName(lName);
            if (mName != null && !mName.equals("")) {
                pn.setMiddleName(mName);
            }

            patient.addName(pn);
            patient.setBirthdate(dob);
            patient.setBirthdateEstimated(true);

            PatientIdentifier openMRSID = generateOpenMRSID();

            if (idNo != null && !idNo.equals("")) {
                PatientIdentifierType upnType = Context.getPatientService().getPatientIdentifierTypeByUuid(PASSPORT_NUMBER);

                PatientIdentifier upn = new PatientIdentifier();
                upn.setIdentifierType(upnType);
                upn.setIdentifier(idNo);
                upn.setPreferred(true);
                patient.addIdentifier(upn);
            } else {
                openMRSID.setPreferred(true);
            }
            patient.addIdentifier(openMRSID);

        }
        return patient;
    }

    private Patient savePatient(Patient patient) {
        Patient p = Context.getPatientService().savePatient(patient);
        return p;

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
        ConceptService conceptService = Context.getConceptService();
        Obs o = new Obs();
        o.setConcept(conceptService.getConcept("162724"));
        o.setDateCreated(new Date());
        o.setCreator(Context.getUserService().getUser(1));
        o.setLocation(enc.getLocation());
        o.setObsDatetime(admissionDate);
        o.setPerson(patient);
        o.setValueText(quarantineCenter);

        // default all admissions type to new
        Obs o1 = new Obs();
        o1.setConcept(conceptService.getConcept("161641"));
        o1.setDateCreated(new Date());
        o1.setCreator(Context.getUserService().getUser(1));
        o1.setLocation(enc.getLocation());
        o1.setObsDatetime(admissionDate);
        o1.setPerson(patient);
        o1.setValueCoded(conceptService.getConcept("164144"));
        enc.addObs(o);
        enc.addObs(o1);

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

        for (int i=0; i < reports.size(); i++) {
            ObjectNode report = (ObjectNode) reports.get(i);
            String followupDate = report.get("FOLLOWUP_DATE").textValue();
            Double sequenceNumber = report.get("DAY_OF_FOLLOWUP").doubleValue();
            Double temp = report.get("TEMPERATURE").doubleValue();
            String cough = report.get("COUGH").textValue();
            String fever = report.get("FEVER").textValue();
            String difficultyBreathing = report.get("DIFFICULTY_BREATHING").textValue();
            //String soreThroat = report.get("SORE_THROAT").textValue();
            String comment = report.get("COMMENT").textValue();
            Date encDate = parseDateString(followupDate, "yyyyMMdd");

            EncounterType et = Context.getEncounterService().getEncounterTypeByUuid(COVID_QUARANTINE_FOLLOWUP_ENCOUNTER);
            Form form = Context.getFormService().getFormByUuid(COVID_QUARANTINE_FOLLOWUP_FORM);

            if (hasEncounterOnDate(et, form, patient, encDate)) {
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
                Obs followUpSequenceObs = setupNumericObs(patient, FOLLOWUP_SEQUENCE_CONCEPT, sequenceNumber, encDate);
                enc.addObs(followUpSequenceObs);
            }

            if (temp != null) {
                Obs tempObs = setupNumericObs(patient, TEMPERATURE_CONCEPT, temp, encDate);
                enc.addObs(tempObs);
            }

            if (fever != null) {
                Obs feverObs = setupCodedObs(patient, FEVER_CONCEPT, (fever.equals("YES") ? YES_CONCEPT : NO_CONCEPT), encDate);
                enc.addObs(feverObs);
            }

            if (cough != null) {
                Obs coughObs = setupCodedObs(patient, COUGH_CONCEPT, (fever.equals("YES") ? YES_CONCEPT : NO_CONCEPT), encDate);
                enc.addObs(coughObs);
            }

            if (difficultyBreathing != null) {
                Obs dbObs = setupCodedObs(patient, DIFFICULTY_BREATHING_CONCEPT, (fever.equals("YES") ? YES_CONCEPT : NO_CONCEPT), encDate);
                enc.addObs(dbObs);
            }

            if (comment != null) {
                Obs commentObs = setupTextObs(patient, COMMENT_CONCEPT, comment, encDate);
                enc.addObs(commentObs);
            }
            encounterService.saveEncounter(enc);
        }

    }

    private Date parseDateString(String dateString, String format) {
        if (dateString.equals("") || dateString == null) {
            return null;
        }
        SimpleDateFormat df = new SimpleDateFormat(format);
        Date date = null;
        try {
            date = df.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
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

        for (int i=0; i < reports.size(); i++) {
            ObjectNode report = (ObjectNode) reports.get(i);
            String followupDate = report.get("FOLLOWUP_DATE").textValue();
            Double sequenceNumber = report.get("DAY_OF_FOLLOWUP").doubleValue();
            Double temp = report.get("TEMPERATURE").doubleValue();
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

            if (hasEncounterOnDate(et, form, patient, encDate)) {
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
                Obs followUpSequenceObs = setupNumericObs(patient, FOLLOWUP_SEQUENCE_CONCEPT, sequenceNumber, encDate);
                enc.addObs(followUpSequenceObs);
            }

            if (temp != null) {
                Obs tempObs = setupNumericObs(patient, TEMPERATURE_CONCEPT, temp, encDate);
                enc.addObs(tempObs);
            }

            if (fever != null) {
                Obs feverObs = setupCodedObs(patient, FEVER_CONCEPT, (fever.equals("YES") ? YES_CONCEPT : NO_CONCEPT), encDate);
                enc.addObs(feverObs);
            }

            if (cough != null) {
                Obs coughObs = setupCodedObs(patient, COUGH_CONCEPT, (fever.equals("YES") ? YES_CONCEPT : NO_CONCEPT), encDate);
                enc.addObs(coughObs);
            }

            if (difficultyBreathing != null) {
                Obs dbObs = setupCodedObs(patient, DIFFICULTY_BREATHING_CONCEPT, (fever.equals("YES") ? YES_CONCEPT : NO_CONCEPT), encDate);
                enc.addObs(dbObs);
            }

            if (comment != null) {
                Obs commentObs = setupTextObs(patient, COMMENT_CONCEPT, comment, encDate);
                enc.addObs(commentObs);
            }
            encounterService.saveEncounter(enc);
        }

    }

    /**
     * Saves individual trace report from CHT
     * @param patient
     * @param encDate
     * @param temp
     * @param fever
     * @param cough
     * @param difficultyBreathing
     * @param followupSequence
     */
    private void saveContactFollowupReport(
            Patient patient,
            Date encDate,
            Double temp, String fever, String cough, String difficultyBreathing, Double followupSequence, String soreThroat) {

            EncounterType et = Context.getEncounterService().getEncounterTypeByUuid(COVID_19_CONTACT_TRACING_ENCOUNTER);
            Form form = Context.getFormService().getFormByUuid(COVID_19_CONTACT_TRACING_FORM);

            if (hasEncounterOnDate(et, form, patient, encDate)) {
                return;
            }
            Encounter enc = new Encounter();
            enc.setEncounterType(et);
            enc.setEncounterDatetime(encDate);
            enc.setPatient(patient);
            enc.addProvider(Context.getEncounterService().getEncounterRole(1), Context.getProviderService().getProvider(1));
            enc.setForm(form);

            // set temp obs
            if (followupSequence != null) {
                Obs followUpSequenceObs = setupNumericObs(patient, FOLLOWUP_SEQUENCE_CONCEPT, followupSequence, encDate);
                enc.addObs(followUpSequenceObs);
            }

            if (temp != null) {
                Obs tempObs = setupNumericObs(patient, TEMPERATURE_CONCEPT, temp, encDate);
                enc.addObs(tempObs);
            }

            if (fever != null) {
                Obs feverObs = setupCodedObs(patient, FEVER_CONCEPT, (fever.equals("yes") ? YES_CONCEPT : NO_CONCEPT), encDate);
                enc.addObs(feverObs);
            }

            if (cough != null) {
                Obs coughObs = setupCodedObs(patient, COUGH_CONCEPT, (cough.equals("yes") ? YES_CONCEPT : NO_CONCEPT), encDate);
                enc.addObs(coughObs);
            }

            if (difficultyBreathing != null) {
                Obs dbObs = setupCodedObs(patient, DIFFICULTY_BREATHING_CONCEPT, (difficultyBreathing.equals("yes") ? YES_CONCEPT : NO_CONCEPT), encDate);
                enc.addObs(dbObs);
            }

        if (soreThroat != null) {
            Obs dbObs = setupCodedObs(patient, SORE_THROAT_CONCEPT, (soreThroat.equals("yes") ? HAS_SORE_THROAT_CONCEPT : NO_CONCEPT), encDate);
            enc.addObs(dbObs);
        }
            encounterService.saveEncounter(enc);

    }

    /**
     * setup numeric obs
     * @param patient
     * @param qConcept
     * @param ans
     * @param encDate
     * @return
     */
    private Obs setupNumericObs(Patient patient, String qConcept, Double ans, Date encDate) {
        Obs obs = new Obs();
        obs.setConcept(conceptService.getConceptByUuid(qConcept));
        obs.setDateCreated(new Date());
        obs.setCreator(Context.getUserService().getUser(1));
        obs.setObsDatetime(encDate);
        obs.setPerson(patient);
        obs.setValueNumeric(ans);
        return obs;
    }

    /**
     * setup text obs
     * @param patient
     * @param qConcept
     * @param ans
     * @param encDate
     * @return
     */
    private Obs setupTextObs(Patient patient, String qConcept, String ans, Date encDate) {
        Obs obs = new Obs();
        obs.setConcept(conceptService.getConceptByUuid(qConcept));
        obs.setDateCreated(new Date());
        obs.setCreator(Context.getUserService().getUser(1));
        obs.setObsDatetime(encDate);
        obs.setPerson(patient);
        obs.setValueText(ans);
        return obs;
    }

    /**
     * set up coded obs
     * @param patient
     * @param qConcept
     * @param ans
     * @param encDate
     * @return
     */
    private Obs setupCodedObs(Patient patient, String qConcept, String ans, Date encDate) {
        Obs obs = new Obs();
        obs.setConcept(conceptService.getConceptByUuid(qConcept));
        obs.setDateCreated(new Date());
        obs.setCreator(Context.getUserService().getUser(1));
        obs.setObsDatetime(encDate);
        obs.setPerson(patient);
        obs.setValueCoded(conceptService.getConceptByUuid(ans));
        return obs;
    }

    /**
     * Set patient attributes
     * @param patient
     * @param phone
     * @param nokName
     * @param nokPhone
     * @return
     */
    private Patient addPersonAttributes(Patient patient, String phone, String nokName, String nokPhone) {

        String NEXT_OF_KIN_CONTACT = "342a1d39-c541-4b29-8818-930916f4c2dc";
        String NEXT_OF_KIN_NAME = "830bef6d-b01f-449d-9f8d-ac0fede8dbd3";
        String TELEPHONE_CONTACT = "b2c38640-2603-4629-aebd-3b54f33f1e3a";


        PersonAttributeType phoneType = Context.getPersonService().getPersonAttributeTypeByUuid(TELEPHONE_CONTACT);
        PersonAttributeType nokNametype = Context.getPersonService().getPersonAttributeTypeByUuid(NEXT_OF_KIN_NAME);
        PersonAttributeType nokContacttype = Context.getPersonService().getPersonAttributeTypeByUuid(NEXT_OF_KIN_CONTACT);

        if (phone != null) {
            PersonAttribute attribute = new PersonAttribute(phoneType, phone);

            try {
                Object hydratedObject = attribute.getHydratedObject();
                if (hydratedObject == null || "".equals(hydratedObject.toString())) {
                    // if null is returned, the value should be blanked out
                    attribute.setValue("");
                } else if (hydratedObject instanceof Attributable) {
                    attribute.setValue(((Attributable) hydratedObject).serialize());
                } else if (!hydratedObject.getClass().getName().equals(phoneType.getFormat())) {
                    // if the classes doesn't match the format, the hydration failed somehow
                    // TODO change the PersonAttribute.getHydratedObject() to not swallow all errors?
                    throw new APIException();
                }
            } catch (APIException e) {
                //.warn("Got an invalid value: " + value + " while setting personAttributeType id #" + paramName, e);
                // setting the value to empty so that the user can reset the value to something else
                attribute.setValue("");
            }
            patient.addAttribute(attribute);
        }

        if (nokName != null) {
            PersonAttribute attribute = new PersonAttribute(nokNametype, nokName);

            try {
                Object hydratedObject = attribute.getHydratedObject();
                if (hydratedObject == null || "".equals(hydratedObject.toString())) {
                    // if null is returned, the value should be blanked out
                    attribute.setValue("");
                } else if (hydratedObject instanceof Attributable) {
                    attribute.setValue(((Attributable) hydratedObject).serialize());
                } else if (!hydratedObject.getClass().getName().equals(nokNametype.getFormat())) {
                    // if the classes doesn't match the format, the hydration failed somehow
                    // TODO change the PersonAttribute.getHydratedObject() to not swallow all errors?
                    throw new APIException();
                }
            } catch (APIException e) {
                //.warn("Got an invalid value: " + value + " while setting personAttributeType id #" + paramName, e);
                // setting the value to empty so that the user can reset the value to something else
                attribute.setValue("");
            }
            patient.addAttribute(attribute);
        }

        if (nokPhone != null) {
            PersonAttribute attribute = new PersonAttribute(nokContacttype, nokPhone);

            try {
                Object hydratedObject = attribute.getHydratedObject();
                if (hydratedObject == null || "".equals(hydratedObject.toString())) {
                    // if null is returned, the value should be blanked out
                    attribute.setValue("");
                } else if (hydratedObject instanceof Attributable) {
                    attribute.setValue(((Attributable) hydratedObject).serialize());
                } else if (!hydratedObject.getClass().getName().equals(nokContacttype.getFormat())) {
                    // if the classes doesn't match the format, the hydration failed somehow
                    // TODO change the PersonAttribute.getHydratedObject() to not swallow all errors?
                    throw new APIException();
                }
            } catch (APIException e) {
                //.warn("Got an invalid value: " + value + " while setting personAttributeType id #" + paramName, e);
                // setting the value to empty so that the user can reset the value to something else
                attribute.setValue("");
            }
            patient.addAttribute(attribute);
        }
        return patient;
    }

    /**
     * set up person address
     * @param patient
     * @param nationality
     * @param county
     * @param subCounty
     * @param ward
     * @param postaladdress
     * @return
     */
    private Patient addPersonAddresses(Patient patient, String nationality, String county, String subCounty, String ward, String postaladdress) {

        Set<PersonAddress> patientAddress = patient.getAddresses();
        if (patientAddress.size() > 0) {
            for (PersonAddress address : patientAddress) {
                if (nationality != null) {
                    address.setCountry(nationality);
                }
                if (county != null) {
                    address.setCountyDistrict(county);
                }
                if (subCounty != null) {
                    address.setStateProvince(subCounty);
                }
                if (ward != null) {
                    address.setAddress4(ward);
                }

                if (postaladdress != null) {
                    address.setAddress1(postaladdress);
                }
                patient.addAddress(address);
            }
        } else {
            PersonAddress pa = new PersonAddress();
            if (nationality != null) {
                pa.setCountry(nationality);
            }
            if (county != null) {
                pa.setCountyDistrict(county);
            }
            if (subCounty != null) {
                pa.setStateProvince(subCounty);
            }
            if (ward != null) {
                pa.setAddress4(ward);
            }

            if (postaladdress != null) {
                pa.setAddress1(postaladdress);
            }
            patient.addAddress(pa);
        }
        return patient;
    }



    /**
     * Get a list of contacts for tracing
     * @return
     * @param lastContactEntry
     * @param lastId
     */
    public ObjectNode getContacts(Integer lastContactEntry, Integer lastId) {

        JsonNodeFactory factory = OutgoingPatientSHR.getJsonNodeFactory();
        ArrayNode patientContactNode = OutgoingPatientSHR.getJsonNodeFactory().arrayNode();
        ObjectNode responseWrapper = factory.objectNode();

        HTSService htsService = Context.getService(HTSService.class);
        Set<Integer> listedContacts = getListedContacts(lastContactEntry, lastId);

        if (listedContacts != null && listedContacts.size() > 0) {

            for (Integer pc : listedContacts) {
                PatientContact c = htsService.getPatientContactByID(pc);
                ObjectNode contact = factory.objectNode();
                ObjectNode parentNode = factory.objectNode();
                ObjectNode fieldNode = factory.objectNode();
                ObjectNode inputsNode = factory.objectNode();
                ObjectNode contactNode = factory.objectNode();
                ObjectNode report = factory.objectNode();

                String givenNames = "";
                String sex = "";
                String dateFormat = "yyyy-MM-dd";

                String fullName = "";

                if (c.getFirstName() != null) {
                    fullName += c.getFirstName();
                }

                if (c.getMiddleName() != null) {
                    fullName += " " + c.getMiddleName();
                }

                if (c.getLastName() != null) {
                    fullName += " " + c.getLastName();
                }

                if (c.getFirstName() != null && c.getMiddleName() != null) {
                    givenNames += c.getFirstName();
                    givenNames += " " + c.getMiddleName();
                } else {
                    if (c.getFirstName() != null) {
                        givenNames += c.getFirstName();
                    }

                    if (c.getMiddleName() != null) {
                        givenNames += c.getMiddleName();
                    }
                }

                if (c.getSex() != null) {
                    if (c.getSex().equals("M")) {
                        sex = "male";
                    } else {
                        sex = "female";
                    }
                }
                parentNode.put("_id", "a452eebc-00a3-4c03-bc2b-43df627bf0f1");
                contact.put("_id", c.getUuid());
                contact.put("parent", parentNode);
                contact.put("given_names", givenNames);
                contact.put("role", "covid_contact");
                contact.put("name", fullName);
                contact.put("country_of_residence", "Kenya");
                contact.put("date_of_birth", c.getBirthDate() != null ? OutgoingPatientSHR.getSimpleDateFormat(dateFormat).format(c.getBirthDate()) : "");
                contact.put("sex", sex);
                contact.put("primary_phone", c.getPhoneContact() != null ? c.getPhoneContact() : "");
                contact.put("alternate_phone", "");
                contact.put("email", "");
                contact.put("type", "person");
                contact.put("reported_date", c.getDateCreated().getTime());
                contact.put("patient_id", c.getPatientRelatedTo().getPatientId().toString());
                contact.put("phone", "");// this could be patient phone
                contact.put("date_of_last_contact", c.getAppointmentDate() != null ? OutgoingPatientSHR.getSimpleDateFormat(dateFormat).format(c.getAppointmentDate()) : "");
                contact.put("outbreak_case_id", "1X000");
                contact.put("relation_to_case", c.getRelationType() != null ? getContactRelation().get(c.getRelationType()) : "");
                contact.put("type_of_contact", c.getPnsApproach() != null ? getContactType().get(c.getPnsApproach()) : "");
                contact.put("household_head", c.getLivingWithPatient() != null && c.getLivingWithPatient().equals(1065) ? givenNames : "");
                contact.put("subcounty", c.getSubcounty() != null ? c.getSubcounty() : "");
                contact.put("town", c.getTown() != null ? c.getTown() : "");
                contact.put("address", c.getPhysicalAddress() != null ? c.getPhysicalAddress() : "");
                contact.put("healthcare_worker", c.getMaritalStatus() != null && c.getMaritalStatus().equals(1065) ? "true" : "false");
                contact.put("facility", c.getFacility() != null ? c.getFacility() : "");
                contactNode.put("_id",c.getUuid());
                inputsNode.put("contact",contactNode);
                fieldNode.put("patient_id",c.getUuid());
                fieldNode.put("case_number","");
                fieldNode.put("case_confirmation_date","");
                fieldNode.put("inputs",inputsNode);
                report.put("form","case_information");
                report.put("type","data_record");
                report.put("content_type","xml");
                report.put("reported_date",c.getDateCreated().getTime());
                report.put("fields",fieldNode);
                patientContactNode.add(contact);
                patientContactNode.add(report);

            }
        }

        responseWrapper.put("docs", patientContactNode);
        return responseWrapper;
    }

    private Map<Integer, String> getContactRelation() {
        Map<Integer, String> options = new HashMap<Integer, String>();
        options.put(160237, "Co-worker");
        options.put(165656,"Traveled together");
        options.put(970, "Mother");
        options.put(971, "Father");
        options.put(972, "Sibling");
        options.put(1528, "Child");
        options.put(5617, "Spouse");
        options.put(163565, "Sexual partner");
        options.put(162221, "Co-wife");

        return options;
    }

    private Map<Integer, String> getContactType() {
        Map<Integer, String> options = new HashMap<Integer, String>();
        options.put(160237,"Working together with a nCoV patient");
        options.put(165656,"Traveling together with a nCoV patient");
        options.put(1060,"Living together with a nCoV patient");
        options.put(117163,"Health care associated exposure");
        return options;

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
     */
    protected Set<Integer> getListedContacts(Integer lastContactEntry, Integer lastId) {

        Set<Integer> eligibleList = new HashSet<Integer>();
        String sql = "";
        if (lastContactEntry != null && lastContactEntry > 0) {
            sql = "select id from kenyaemr_hiv_testing_patient_contact where id >" + lastContactEntry + " and patient_id is null and voided=0;"; // get contacts not registered
        } else {
            sql = "select id from kenyaemr_hiv_testing_patient_contact where id <= " + lastId + " and patient_id is null and voided=0;";

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
    protected Set<Integer> getContactsInQuarantineProgram() {

        Set<Integer> eligibleList = new HashSet<Integer>();
        GlobalProperty lastPatientEntry = Context.getAdministrationService().getGlobalPropertyObject(HTSMetadata.MHEALTH_LAST_PATIENT_ENTRY);
        String lastOrdersql = "select max(patient_id) last_id from patient where voided=0;";
        List<List<Object>> lastOrderId = Context.getAdministrationService().executeSQL(lastOrdersql, true);
        Integer lastId = (Integer) lastOrderId.get(0).get(0);
        lastId = lastId != null ? lastId : 0;

        String sql = "";
        if (lastPatientEntry != null) {
            Integer lastEntry = Integer.parseInt(lastPatientEntry.getValue().toString());
            sql = "select pp.patient_id from patient_program pp \n" +
                    "inner join (select program_id from program where uuid='9a5d555e-739a-11ea-bc55-0242ac130003') p on pp.program_id = p.program_id\n" +
                    "where pp.patient_id >" + lastEntry + " and pp.voided=0;";
        } else {
            lastPatientEntry = new GlobalProperty();
            lastPatientEntry.setProperty(HTSMetadata.MHEALTH_LAST_PATIENT_ENTRY);
            lastPatientEntry.setDescription("Id for the last patient entry");
            sql = "select pp.patient_id from patient_program pp \n" +
                    "inner join (select program_id from program where uuid='9a5d555e-739a-11ea-bc55-0242ac130003') p on pp.program_id = p.program_id\n" +
                    "where pp.patient_id <= " + lastId + " and pp.voided=0;";

        }
        lastPatientEntry.setPropertyValue(lastId.toString());

        List<List<Object>> activeList = Context.getAdministrationService().executeSQL(sql, true);
        if (!activeList.isEmpty()) {
            for (List<Object> res : activeList) {
                Integer patientId = (Integer) res.get(0);
                eligibleList.add(patientId);
            }
        }

        Context.getAdministrationService().saveGlobalProperty(lastPatientEntry);
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

    /**
     * Checks if a patient already has encounter of same type and form on same date
     * @param enctype
     * @param form
     * @param patient
     * @param date
     * @return
     */
    public boolean hasEncounterOnDate(EncounterType enctype, Form form, Patient patient, Date date) {
        List<Encounter> encounters = Context.getEncounterService().getEncounters(patient, null, date, date, Collections.singleton(form), Collections.singleton(enctype), null, null, null, false);
        Integer size = encounters.size();
        log.info("Checking for enc type=" + enctype + ", form=" + form + "date, "+ date + " result=" + size);
        System.out.println("Checking for enc type=" + enctype + ", form=" + form + "date, "+ date + " result=" + size);
        return encounters.size() > 0;

    }
}
