package org.openmrs.module.hivtestingservices.api.shr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.CareSetting;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.Person;
import org.openmrs.Provider;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.TestOrder;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PersonService;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.module.hivtestingservices.api.HTSService;
import org.openmrs.module.hivtestingservices.api.PatientContact;
import org.openmrs.module.hivtestingservices.metadata.HTSMetadata;

import java.io.IOException;
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
            String nationalID = contactNode.get("national_id").textValue();
            String passportNo = contactNode.get("passport_number").textValue();
            String alienNo = contactNode.get("alien_id").textValue();

            PatientContact c = null;
            Patient patient = null;

            if (uuid == null && nationalID == null && passportNo == null && alienNo == null) {
                return "The payload has no identification information! Nothing has been processed";
            }
            if (StringUtils.isNotBlank(uuid)) {
                c = htsService.getPatientContactByUuid(uuid);
            }

            patient = SHRUtils.checkIfPatientExists(nationalID, passportNo, alienNo);

            if (c == null && patient == null) {
                return "The payload could not be associated with any individual in the system. Please countercheck the identifying information";
            }

            Patient contactRegistered = null;
            if (c != null && c.getPatient() != null) {
                contactRegistered = c.getPatient();
            } else if (patient != null) {
                contactRegistered = patient;
            }

            ObjectNode traceReport = (ObjectNode) jsonNode.get("trace");
            String encDateStr = traceReport.get("date_last_contacted").textValue();

            Date encounterdate = SHRUtils.parseDateString(encDateStr, "yyyy-MM-dd");
            Double followupSequence = traceReport.get("follow_up_count").doubleValue();
            Double temp = traceReport.get("temperature").doubleValue();
            String cough = traceReport.get("cough").textValue();
            String fever = traceReport.get("fever").textValue();
            String soreThroat = traceReport.get("sore_throat").textValue();
            String difficultyBreathing = traceReport.get("difficulty_breathing").textValue();

            if (contactRegistered != null) {
                saveContactFollowupReport(contactRegistered, encounterdate, temp, fever, cough, difficultyBreathing, followupSequence, soreThroat);
            } else {

                String fName = null;
                String mName = null;

                String lName = contactNode.get("family_name").textValue();
                String givenNames = contactNode.get("given_names").textValue();
                String county = contactNode.get("county").textValue();
                String subCounty = contactNode.get("subcounty").textValue();
                String postalAddress = contactNode.get("postal_address").textValue();
                String phoneNumber = contactNode.get("phone").textValue();
                String dobString = contactNode.get("date_of_birth").textValue();
                Date dob = SHRUtils.parseDateString(dobString, "yyyy-MM-dd");

                String arrGivenNames[] = givenNames.split(" ");
                if (arrGivenNames.length > 1) {
                    fName = arrGivenNames[0];
                    mName = arrGivenNames[1];
                } else if (arrGivenNames.length > 0) {
                    fName = arrGivenNames[0];
                }

                patient = SHRUtils.createPatient(fName, mName, lName, dob, c.getSex(), nationalID, passportNo, alienNo);
                patient = SHRUtils.addPersonAddresses(patient, null, county, subCounty, null, postalAddress);
                patient = SHRUtils.addPersonAttributes(patient, phoneNumber, null, null);
                patient = SHRUtils.savePatient(patient);

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
            Date dob = SHRUtils.parseDateString(dobString, "yyyy-MM-dd");

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


            Patient patient = SHRUtils.checkIfPatientExists(idNumber, passportNumber, alienNumber);
            if (patient == null) {
                patient = SHRUtils.createPatient(fName, mName, lName, dob, sex, idNumber, passportNumber, alienNumber);
                patient = SHRUtils.savePatient(patient);
                patient = SHRUtils.addPersonAddresses(patient, null, county, subCounty, null, postalAddress);
                patient = SHRUtils.addPersonAttributes(patient, phoneNumber, null, null);
            }

            if (patient != null) {

                ObjectNode cif = (ObjectNode) contactObject.get("report");
                if (cif == null) {
                    return "The payload has empty case report!";
                }

                if (!SHRUtils.inProgram(patient, HTSMetadata.COVID_19_CASE_INVESTIGATION_PROGRAM) && cif != null) {
                    enrollForCovidCaseInvestigation(patient, cif);
                } else {
                    return "There is an existing enrolment for the case";
                }
            }
            return "Successfully created a covid case";
        }

        return "There were no case information provided";
    }

    private void enrollForCovidCaseInvestigation(Patient patient, ObjectNode cif) {

        String dateFormat = "yyyy-MM-dd";
        ObjectNode fields = (ObjectNode) cif.get("fields");
        ObjectNode geolocation = (ObjectNode) cif.get("geolocation");
        ObjectNode reportingInfo = (ObjectNode) fields.get("group_reporting_info");
        ObjectNode patientInfo = (ObjectNode) fields.get("group_patient_information");
        ObjectNode clinicalInfo = (ObjectNode) fields.get("group_clinical_information");
        ObjectNode symptomsInfo = (ObjectNode) fields.get("group_patient_symptoms");
        ObjectNode signsInfo = (ObjectNode) fields.get("group_patient_signs");
        ObjectNode comorbidityInfo = (ObjectNode) fields.get("group_conditions_comorbidity");
        ObjectNode exposureInfo = (ObjectNode) fields.get("group_exposure_travel_information");
        ObjectNode labInfo = (ObjectNode) fields.get("group_laboratory_information");


        // geological info
        String longitude = geolocation.has("longitude") ? geolocation.get("longitude").textValue() : null;
        String latitude = geolocation.has("latitude") ? geolocation.get("latitude").textValue() : null;

        String labRequest = labInfo.has("was_specimen_collected") ? labInfo.get("was_specimen_collected").textValue() : null;


        // reporting info
        String reportingDate = reportingInfo.has("date_of_reporting") ? reportingInfo.get("date_of_reporting").textValue() : null;
        String reportingFacility = reportingInfo.has("reporting_facility") ? reportingInfo.get("reporting_facility").textValue() : null;
        String reportingCounty = reportingInfo.has("county") ? reportingInfo.get("county").textValue() : null;
        String reportingSubCounty = reportingInfo.has("subcounty") ? reportingInfo.get("subcounty").textValue() : null;
        String poeDetection = reportingInfo.has("poe_detected") ? reportingInfo.get("poe_detected").textValue() : null;
        String poeDetectionDate = reportingInfo.has("poe_detection_date") ? reportingInfo.get("poe_detection_date").textValue() : null;


        // reporting info
        //String caseIdentifier = patientInfo.get("case_identifier").textValue();

        String symptoms = symptomsInfo.has("patient_symptoms") ? symptomsInfo.get("patient_symptoms").textValue() : null;
        Double temperature = signsInfo.has("temperature") ? signsInfo.get("temperature").doubleValue() : null;
        String signs = signsInfo.has("reported_patient_signs") ? signsInfo.get("reported_patient_signs").textValue() : null;

        // clinical information
        Date encDate = SHRUtils.parseDateString(reportingDate, dateFormat);
        if (encDate == null) {
            return;
        }
        String symptomatic = clinicalInfo.has("patient_condition") ? clinicalInfo.get("patient_condition").textValue() : null;
        String symptomsOnsetDate = clinicalInfo.has("symptoms_onset_date") ? clinicalInfo.get("symptoms_onset_date").textValue() : null;
        String firstAdmissionDate = clinicalInfo.has("first_admission_date") ? clinicalInfo.get("first_admission_date").textValue() : null;
        String hospitalName = clinicalInfo.has("hospital_name") ? clinicalInfo.get("hospital_name").textValue() : null;
        String isolationDate = clinicalInfo.has("isolation_date") ? clinicalInfo.get("isolation_date").textValue() : null;
        String patientVentilated = clinicalInfo.has("patient_ventilated") ? clinicalInfo.get("patient_ventilated").textValue() : null;
        String patientHealthStatus = clinicalInfo.has("patient_health_status") ? clinicalInfo.get("patient_health_status").textValue() : null;
        String deathDate = clinicalInfo.has("death_date") ? clinicalInfo.get("death_date").textValue() : null;


        // exposure
        String patientTravelledPast2Weeks = exposureInfo.has("patient_travelled_past_2_weeks") ? exposureInfo.get("patient_travelled_past_2_weeks").textValue() : null;
        String occupation = exposureInfo.has("occupation") ? exposureInfo.get("occupation").textValue() : null;
        String visitedFacility = exposureInfo.has("has_patient_visited_facility_2_weeks") ? exposureInfo.get("has_patient_visited_facility_2_weeks").textValue() : null;
        String contactWithARI = exposureInfo.has("patient_had_close_contact_ari") ? exposureInfo.get("patient_had_close_contact_ari").textValue() : null;
        String contactWithSuspectedCase = exposureInfo.has("patient_had_close_contact_with_case") ? exposureInfo.get("patient_had_close_contact_with_case").textValue() : null;
        String visitedAnimalMarket = exposureInfo.has("visited_live_animal_market") ? exposureInfo.get("visited_live_animal_market").textValue() : null;

        // comorbidity
        String comorbidities = comorbidityInfo.has("conditions_comorbidity") ? comorbidityInfo.get("conditions_comorbidity").textValue() : null;

        EncounterType encType = Context.getEncounterService().getEncounterTypeByUuid(HTSMetadata.COVID_19_CASE_INVESTIGATION_ENCOUNTER);
        Form form = Context.getFormService().getFormByUuid(HTSMetadata.COVID_19_CASE_INVESTIGATION_FORM);

        Encounter encounter = new Encounter();
        encounter.setEncounterType(encType);
        encounter.setPatient(patient);
        encounter.setEncounterDatetime(encDate);
        encounter.setForm(form);

        // build observations
        if (StringUtils.isNotBlank(latitude)) {
            encounter.addObs(ObsUtils.setupTextObs(patient, ObsUtils.LATITUDE, latitude, encDate));
        }
        if (StringUtils.isNotBlank(longitude)) {
            encounter.addObs(ObsUtils.setupTextObs(patient, ObsUtils.LONGITUDE, longitude, encDate));
        }
        if (StringUtils.isNotBlank(reportingCounty)) {
            encounter.addObs(ObsUtils.setupTextObs(patient, ObsUtils.REPORTING_COUNTY, reportingCounty, encDate));
        }
        if(StringUtils.isNotBlank(reportingSubCounty)) {
            encounter.addObs(ObsUtils.setupTextObs(patient, ObsUtils.REPORTING_SUB_COUNTY, reportingSubCounty, encDate));
        }

        if (StringUtils.isNotBlank(poeDetection) && poeDetection.equals("yes")) {
            encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.DETECTION_POINT, ObsUtils.POE, encDate));
        } if (StringUtils.isNotBlank(poeDetection) && poeDetection.equals("no")) {
            encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.DETECTION_POINT, ObsUtils.COMMUNITY, encDate));
        } else {
            encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.DETECTION_POINT, ObsUtils.UNKNOWN, encDate));
        }

        if(StringUtils.isNotBlank(poeDetectionDate)) {
            encounter.addObs(ObsUtils.setupDatetimeObs(patient, ObsUtils.DATE_DETECTED, SHRUtils.parseDateString(poeDetectionDate, dateFormat), encDate));
        }
        if(StringUtils.isNotBlank(symptomatic)) {
            encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.PATIENT_SYMPTOMATIC, symptomatic.equals("symptomatic") ? ObsUtils.YES_CONCEPT : ObsUtils.NO_CONCEPT, encDate));
        }
        if(StringUtils.isNotBlank(symptomsOnsetDate)) {
            encounter.addObs(ObsUtils.setupDatetimeObs(patient, ObsUtils.DATE_OF_ONSET_OF_SYMPTOMS, SHRUtils.parseDateString(symptomsOnsetDate, dateFormat), encDate));
        }
        if(StringUtils.isNotBlank(firstAdmissionDate) || StringUtils.isNotBlank(hospitalName)) {
            encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.ADMISSION_TO_HOSPITAL, ObsUtils.YES_CONCEPT, encDate));
        } else {
            encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.ADMISSION_TO_HOSPITAL, ObsUtils.NO_CONCEPT, encDate));
        }
        if(StringUtils.isNotBlank(firstAdmissionDate)) {
            encounter.addObs(ObsUtils.setupDatetimeObs(patient, ObsUtils.DATE_OF_ADMISSION_TO_HOSPITAL, SHRUtils.parseDateString(firstAdmissionDate, dateFormat), encDate));
        }
        if(StringUtils.isNotBlank(hospitalName)) {
            encounter.addObs(ObsUtils.setupTextObs(patient, ObsUtils.NAME_OF_HOSPITAL_ADMITTED, hospitalName, encDate));
        }
        if(StringUtils.isNotBlank(isolationDate)) {
            encounter.addObs(ObsUtils.setupDatetimeObs(patient, ObsUtils.DATE_OF_ISOLATION, SHRUtils.parseDateString(isolationDate, dateFormat), encDate));
        }
        if(StringUtils.isNotBlank(patientVentilated)) {
            encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.WAS_PATIENT_VENTILATED, patientVentilated.equals("yes") ? ObsUtils.YES_CONCEPT : ObsUtils.NO_CONCEPT, encDate));
        }
        if(StringUtils.isNotBlank(patientHealthStatus) && patientHealthStatus.equals("stable")) {
            encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.PATIENT_STATUS_AT_REPORTING, ObsUtils.PATIENT_STATUS_STABLE, encDate));
        } else if(StringUtils.isNotBlank(patientHealthStatus) && patientHealthStatus.equals("severely ill")) {
            encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.PATIENT_STATUS_AT_REPORTING, ObsUtils.PATIENT_STATUS_SEVERELY_ILL, encDate));
        } else if(StringUtils.isNotBlank(patientHealthStatus) && patientHealthStatus.equals("dead")) {
            encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.PATIENT_STATUS_AT_REPORTING, ObsUtils.PATIENT_STATUS_DEAD, encDate));
        } else if(StringUtils.isNotBlank(patientHealthStatus)) {
            encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.PATIENT_STATUS_AT_REPORTING, ObsUtils.UNKNOWN, encDate));
        }
        if(StringUtils.isNotBlank(deathDate)) {
            encounter.addObs(ObsUtils.setupDatetimeObs(patient, ObsUtils.DATE_OF_DEATH, SHRUtils.parseDateString(deathDate, dateFormat), encDate));
        }
        if(StringUtils.isNotBlank(patientTravelledPast2Weeks)) {
            encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.HISTORY_OF_TRAVEL, patientTravelledPast2Weeks.equals("yes") ? ObsUtils.YES_CONCEPT : ObsUtils.NO_CONCEPT, encDate));
        }

        if(StringUtils.isNotBlank(contactWithARI)) {
            encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.CONTACT_WITH_RESPIRATORY_INFECTED, contactWithARI.equals("yes") ? ObsUtils.YES_CONCEPT : ObsUtils.NO_CONCEPT, encDate));
        }
        if(StringUtils.isNotBlank(contactWithSuspectedCase)) {
            encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.CONTACT_WITH_SUSPECTED_CASE, contactWithSuspectedCase.equals("yes") ? ObsUtils.YES_CONCEPT : ObsUtils.NO_CONCEPT, encDate));
        }
        if(StringUtils.isNotBlank(visitedAnimalMarket)) {
            encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.VISITED_ANIMAL_MARKET, visitedAnimalMarket.equals("yes") ? ObsUtils.YES_CONCEPT : ObsUtils.NO_CONCEPT, encDate));
        }
        if(StringUtils.isNotBlank(visitedFacility)) {
            encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.VISITED_FACILITY, visitedFacility.equals("yes") ? ObsUtils.YES_CONCEPT : ObsUtils.NO_CONCEPT, encDate));
        }

        // PROCESS SYMPTOMS
        if(StringUtils.isNotBlank(symptoms)) {
            String arrSymptoms[] = symptoms.split(" ");
            if (arrSymptoms.length > 0) {
                for (int i = 0; i < arrSymptoms.length; i++) {
                    String pSymptom = arrSymptoms[i];
                    if (StringUtils.isBlank(pSymptom)) {
                        continue;
                    }

                    if (pSymptom.trim().equals("history_of_fever_chills")) {
                        encounter.addObs(ObsUtils.setupCodedObs(patient, FEVER_CONCEPT, ObsUtils.YES_CONCEPT, encDate));
                    } else if (pSymptom.trim().equals("general_weakness")) {
                        encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.GENERAL_WEAKNESS, ObsUtils.HAS_GENERAL_WEAKNESS, encDate));
                    } else if (pSymptom.trim().equals("cough")) {
                        encounter.addObs(ObsUtils.setupCodedObs(patient, COUGH_CONCEPT, ObsUtils.YES_CONCEPT, encDate));
                    } else if (pSymptom.trim().equals("sore_throat")) {
                        encounter.addObs(ObsUtils.setupCodedObs(patient, SORE_THROAT_CONCEPT, HAS_SORE_THROAT_CONCEPT, encDate));
                    } else if (pSymptom.trim().equals("running_nose")) {
                        encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.RUNNY_NOSE, ObsUtils.HAS_RUNNY_NOSE, encDate));
                    } else if (pSymptom.trim().equals("shortness_of_breath")) {
                        encounter.addObs(ObsUtils.setupCodedObs(patient, DIFFICULTY_BREATHING_CONCEPT, ObsUtils.YES_CONCEPT, encDate));
                    } else if (pSymptom.trim().equals("diarrhoea")) {
                        encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.DIARRHOEA, ObsUtils.YES_CONCEPT, encDate));
                    } else if (pSymptom.trim().equals("nausea_vomiting")) {
                        encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.NAUSEA_VOMITING, ObsUtils.YES_CONCEPT, encDate));
                    } else if (pSymptom.trim().equals("headache")) {
                        encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.HEADACHE, ObsUtils.HAS_HEADACHE, encDate));
                    } else if (pSymptom.trim().equals("irritability_confusion")) {
                        encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.IRRITABILITY_CONFUSION, ObsUtils.YES_CONCEPT, encDate));
                    } else if (pSymptom.trim().equals("pain")) {
                        encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.CHEST_PAIN, ObsUtils.HAS_CHEST_PAIN, encDate));
                    } else {
                        encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.OTHER_SYMPTOMS, ObsUtils.HAS_OTHER_SYMPTOMS, encDate));
                    }
                }
            }

        }

        // PROCESS SIGNS
        if(temperature != null) {
            encounter.addObs(ObsUtils.setupNumericObs(patient, ObsUtils.TEMPERATURE, temperature, encDate));
        }
        if(StringUtils.isNotBlank(signs)) {
            String arrSigns[] = signs.split(" ");
            if (arrSigns.length > 0) {
                for (int i = 0; i < arrSigns.length; i++) {
                    String pSign = arrSigns[i];
                    if (StringUtils.isBlank(pSign)) {
                        continue;
                    }

                    if (pSign.trim().equals("pharyngeal_exudate")) {
                        encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.PHARYNGEAL_EXUDATE, ObsUtils.PHARYNGEAL_EXUDATE_PRESENT, encDate));
                    } else if (pSign.trim().equals("conjunctival_injection")) {
                        encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.CONJUCTIVAL_INJECTION, ObsUtils.CONJUCTIVAL_INJECTION_PRESENT, encDate));
                    } else if (pSign.trim().equals("seizure")) {
                        encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.SEIZURES, YES_CONCEPT, encDate));
                    } else if (pSign.trim().equals("coma")) {
                        encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.COMA, ObsUtils.COMA_PRESENT, encDate));
                    } else if (pSign.trim().equals("dyspnea_tachypnea")) {
                        encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.DYSPNEA_TACHYPNEA, ObsUtils.YES_CONCEPT, encDate));
                    } else if (pSign.trim().equals("abnormal_lung_auscultation")) {
                        encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.ABNORMAL_LUNG_AUSCULTATION, ObsUtils.YES_CONCEPT, encDate));
                    } else if (pSign.trim().equals("abnormal_lung_x-ray_findings")) {
                        encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.ABNORMAL_LUNG_X_RAY, ObsUtils.ABNORMAL_LUNG_X_RAY_PRESENT, encDate));
                    } else {
                        encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.OTHER_SIGNS, ObsUtils.OTHER_SIGNS_PRESENT, encDate));
                    }
                }
            }

        }

        if(StringUtils.isNotBlank(occupation)) {
            String arrOccupation[] = occupation.split(" ");
            if (arrOccupation.length > 0) {
                for (int i = 0; i < arrOccupation.length; i++) {
                    String indOccupation = arrOccupation[i];
                    if (StringUtils.isBlank(indOccupation)) {
                        continue;
                    }

                    if (indOccupation.trim().equals("student")) {
                        encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.OCCUPATION, ObsUtils.OCCUPATION_STUDENT, encDate));
                    } else if (indOccupation.trim().equals("health_care_worker")) {
                        encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.OCCUPATION, ObsUtils.OCCUPATION_HCW, encDate));
                    } else if (indOccupation.trim().equals("working_with_animals")) {
                        encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.OCCUPATION, ObsUtils.OCCUPATION_WORKING_WITH_ANIMALS, encDate));
                    } else if (indOccupation.trim().equals("health_laboratory_worker")) {
                        encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.OCCUPATION, ObsUtils.OCCUPATION_LAB_WORKER, encDate));
                    } else {
                        encounter.addObs(ObsUtils.setupCodedObs(patient, ObsUtils.OCCUPATION, ObsUtils.OTHER_SPECIFY, encDate));
                    }
                }
            }

        }


        if (StringUtils.isNotBlank(comorbidities)) {
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

        Encounter savedEnc = encounterService.saveEncounter(encounter);

        PatientProgram pp = new PatientProgram();
        pp.setPatient(patient);
        pp.setProgram(Context.getProgramWorkflowService().getProgramByUuid(HTSMetadata.COVID_19_CASE_INVESTIGATION_PROGRAM));
        pp.setDateEnrolled(encDate);
        pp.setDateCreated(new Date());
        Context.getProgramWorkflowService().savePatientProgram(pp);

        // order lab
        UserService userService = Context.getUserService();
        ProviderService providerService = Context.getProviderService();
        Provider provider = null;
        List<Provider> providers = (List<Provider>) providerService.getProvidersByPerson(Context.getUserService().getUser(1).getPerson());

        if (!providers.isEmpty()) {
            provider = providers.get(0);
        }
        if (StringUtils.isNotBlank(labRequest) && labRequest.equals("yes") && provider != null) {
            Order anOrder = new TestOrder();
            anOrder.setPatient(patient);
            anOrder.setCareSetting(new CareSetting());
            anOrder.setConcept(conceptService.getConceptByUuid(ObsUtils.COVID_19_LAB_TEST_CONCEPT));
            anOrder.setDateActivated(encDate);
            anOrder.setCommentToFulfiller("Kenyatta National Hospial Lab Nairobi"); //place holder for now
            anOrder.setInstructions("OP and NP Swabs");
            anOrder.setOrderer(provider);
            anOrder.setEncounter(savedEnc);
            anOrder.setCareSetting(Context.getOrderService().getCareSetting(1));
            anOrder.setOrderReason(conceptService.getConceptByUuid(ObsUtils.COVID_19_BASELINE_TEST_CONCEPT));
            Context.getOrderService().saveOrder(anOrder, null);
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

            if (SHRUtils.hasEncounterOnDate(et, form, patient, encDate)) {
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

}
