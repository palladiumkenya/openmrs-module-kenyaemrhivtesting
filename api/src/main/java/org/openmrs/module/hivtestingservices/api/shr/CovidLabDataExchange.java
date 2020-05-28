package org.openmrs.module.hivtestingservices.api.shr;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;




import org.apache.commons.lang3.StringUtils;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.hivtestingservices.api.ContactTrace;
import org.openmrs.module.hivtestingservices.api.HTSService;
import org.openmrs.module.hivtestingservices.api.PatientContact;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CovidLabDataExchange {

    PersonService personService = Context.getPersonService();
    PatientService patientService = Context.getPatientService();
    ObsService obsService = Context.getObsService();
    ConceptService conceptService = Context.getConceptService();
    EncounterService encounterService = Context.getEncounterService();
    OrderService orderService = Context.getOrderService();

    String TELEPHONE_CONTACT = "b2c38640-2603-4629-aebd-3b54f33f1e3a";
    String TEST_ORDER_TYPE_UUID = "52a447d3-a64a-11e3-9aeb-50e549534c5e";
    String LAB_ENCOUNTER_TYPE_UUID = "e1406e88-e9a9-11e8-9f32-f2801f1b9fd1";
    String COVID_19_CASE_INVESTIGATION = "a4414aee-6832-11ea-bc55-0242ac130003";
    String COVID_19_QUARANTINE_ENROLLMENT = "33a3a55c-73ae-11ea-bc55-0242ac130003";
    Concept covidTestConcept = conceptService.getConcept(165611);
    Concept covidPosConcept = conceptService.getConcept(703);
    Concept covidNegConcept = conceptService.getConcept(664);
    Concept covidIndeterminateConcept = conceptService.getConcept(1138);
    EncounterType labEncounterType = encounterService.getEncounterTypeByUuid(LAB_ENCOUNTER_TYPE_UUID);
    HTSService htsService = Context.getService(HTSService.class);




    /**
     * Returns a list of active lab requests
     * @return
     * @param gpLastOrderId
     * @param lastId
     */
    public ObjectNode getCovidLabRequests(Integer gpLastOrderId, Integer lastId) {

        JsonNodeFactory factory = OutgoingPatientSHR.getJsonNodeFactory();
        ArrayNode activeRequests = factory.arrayNode();
        ObjectNode requestWrapper = factory.objectNode();
        Set<Integer> allPatients = getPatientsWithOrders(gpLastOrderId, lastId);
        Integer patientsFound = 0;
        if (!allPatients.isEmpty()) {
            patientsFound = allPatients.size();
            for (Integer ptId : allPatients) {
                Patient p = patientService.getPatient(ptId);
                activeRequests = getActiveLabRequestForPatient(p, activeRequests);
            }
        }
        System.out.println("Preparing lab requests for " + patientsFound + " cases for the lab system");
        requestWrapper.put("samples",activeRequests);
        return requestWrapper;

    }

    /**
     * Returns active lab requests for a patient
     * @param patient
     * @return
     */
    public ArrayNode getActiveLabRequestForPatient(Patient patient, ArrayNode requests) {

        JsonNodeFactory factory = OutgoingPatientSHR.getJsonNodeFactory();
        ObjectNode patientSHR = factory.objectNode();


        if (patient != null) {
            return getActiveLabRequestsForPatient(patient, requests);

        } else {
            return requests;
        }
    }

    /**
     * Returns a person's phone number attribute
     * @param patient
     * @return
     */
    private String getPatientPhoneNumber(Patient patient) {
        PersonAttributeType phoneNumberAttrType = personService.getPersonAttributeTypeByUuid(TELEPHONE_CONTACT);
        return patient.getAttribute(phoneNumberAttrType) != null ? patient.getAttribute(phoneNumberAttrType).getValue() : "";
    }

    /**
     * Returns a patient's address
     * @param patient
     * @return
     */
    public static ObjectNode getPatientAddress(Patient patient) {
        Set<PersonAddress> addresses = patient.getAddresses();
        //patient address
        ObjectNode patientAddressNode = OutgoingPatientSHR.getJsonNodeFactory().objectNode();
        ObjectNode physicalAddressNode = OutgoingPatientSHR.getJsonNodeFactory().objectNode();
        String postalAddress = "";
        String nationality = "";
        String county = "";
        String sub_county = "";
        String ward = "";
        String landMark = "";

        for (PersonAddress address : addresses) {
            if (address.getAddress1() != null) {
                postalAddress = address.getAddress1();
            }
            if (address.getCountry() != null) {
                nationality = address.getCountry() != null ? address.getCountry() : "";
            }

            if (address.getCountyDistrict() != null) {
                county = address.getCountyDistrict() != null ? address.getCountyDistrict() : "";
            }

            if (address.getStateProvince() != null) {
                sub_county = address.getStateProvince() != null ? address.getStateProvince() : "";
            }

            if (address.getAddress4() != null) {
                ward = address.getAddress4() != null ? address.getAddress4() : "";
            }
            if (address.getAddress2() != null) {
                landMark = address.getAddress2() != null ? address.getAddress2() : "";
            }

        }

        physicalAddressNode.put("NATIONALITY", nationality);
        physicalAddressNode.put("COUNTY", county);
        physicalAddressNode.put("SUB_COUNTY", sub_county);
        physicalAddressNode.put("WARD", ward);
        physicalAddressNode.put("NEAREST_LANDMARK", landMark);

        //combine all addresses
        patientAddressNode.put("PHYSICAL_ADDRESS", physicalAddressNode);
        patientAddressNode.put("POSTAL_ADDRESS", postalAddress);

        return patientAddressNode;
    }

    /**
     * Returns patient name
     * @param patient
     * @return
     */
    private ObjectNode getPatientName(Patient patient) {
        PersonName pn = patient.getPersonName();
        ObjectNode nameNode = OutgoingPatientSHR.getJsonNodeFactory().objectNode();
        nameNode.put("FIRST_NAME", pn.getGivenName());
        nameNode.put("MIDDLE_NAME", pn.getMiddleName());
        nameNode.put("LAST_NAME", pn.getFamilyName());
        return nameNode;
    }

    private ObjectNode getPatientIdentifier(Patient patient) {

        PatientIdentifierType NATIONAL_ID_TYPE = patientService.getPatientIdentifierTypeByUuid(SHRConstants.NATIONAL_ID);
        PatientIdentifierType ALIEN_NUMBER_TYPE = patientService.getPatientIdentifierTypeByUuid(SHRConstants.ALIEN_NUMBER);
        PatientIdentifierType PASSPORT_NUMBER_TYPE = patientService.getPatientIdentifierTypeByUuid(SHRConstants.PASSPORT_NUMBER);
        PatientIdentifierType CASE_ID_TYPE = patientService.getPatientIdentifierTypeByUuid(SHRConstants.PATIENT_CLINIC_NUMBER);
        PatientIdentifierType OPENMRS_ID_TYPE = patientService.getPatientIdentifierTypeByUuid(SHRConstants.MEDICAL_RECORD_NUMBER);

        List<PatientIdentifier> identifierList = patientService.getPatientIdentifiers(null, Arrays.asList(NATIONAL_ID_TYPE, NATIONAL_ID_TYPE, ALIEN_NUMBER_TYPE, PASSPORT_NUMBER_TYPE), null, Arrays.asList(patient), null);

        ObjectNode patientIdentifiers = OutgoingPatientSHR.getJsonNodeFactory().objectNode();
        String pIdentifier = null;
        Integer idCode = null;

        for (PatientIdentifier identifier : identifierList) {
            PatientIdentifierType identifierType = identifier.getIdentifierType();

            if (identifierType.equals(NATIONAL_ID_TYPE)) {
                pIdentifier = identifier.getIdentifier();
                idCode = 1;
                patientIdentifiers.put("type", 1);
                patientIdentifiers.put("identifier", identifier.getIdentifier());
                return patientIdentifiers;

            } else if (identifierType.equals(ALIEN_NUMBER_TYPE)) {
                pIdentifier = identifier.getIdentifier();
                idCode = 3;
                patientIdentifiers.put("type", 3);
                patientIdentifiers.put("identifier", identifier.getIdentifier());
                return patientIdentifiers;


            } else if (identifierType.equals(PASSPORT_NUMBER_TYPE)) {
                pIdentifier = identifier.getIdentifier();
                idCode = 2;
                patientIdentifiers.put("type", 2);
                patientIdentifiers.put("identifier", identifier.getIdentifier());
                return patientIdentifiers;

            } else if (identifierType.equals(CASE_ID_TYPE)) { // use this to track those with no documented identifier
                pIdentifier = identifier.getIdentifier();
                idCode = 4;
                patientIdentifiers.put("type", 4);
                patientIdentifiers.put("identifier", identifier.getIdentifier());
                return patientIdentifiers;
            }

        }

        if (idCode == null || pIdentifier == null) {
            PatientIdentifier openmrsId = patient.getPatientIdentifier(OPENMRS_ID_TYPE);
            pIdentifier = openmrsId.getIdentifier();
            idCode = 4;
        }

        patientIdentifiers.put("type", idCode);
        patientIdentifiers.put("identifier", pIdentifier);
        return patientIdentifiers;
    }

    /**
     * Returns object lab request for patients
     * @param patient
     * @return
     */
    protected ArrayNode getActiveLabRequestsForPatient(Patient patient, ArrayNode labTests) {

        ObjectNode cifInfo = getCovidEnrollmentDetails(patient);
        ObjectNode quarantineInfo = getQuarantineEnrollmentDetails(patient);
        ObjectNode address = getPatientAddress(patient);
        ObjectNode physicalAddress = (ObjectNode) address.get("PHYSICAL_ADDRESS");
        ArrayNode blankArray = OutgoingPatientSHR.getJsonNodeFactory().arrayNode();
        OrderService orderService = Context.getOrderService();
        Integer caseId = patient.getPatientId();
        ObjectNode idMap = getPatientIdentifier(patient);
        //Check whether client has active covid order
        OrderType patientLabOrders = orderService.getOrderTypeByUuid(TEST_ORDER_TYPE_UUID);
        String dob = patient.getBirthdate() != null ? OutgoingPatientSHR.getSimpleDateFormat("yyyy-MM-dd").format(patient.getBirthdate()) : "";
        String deathDate = patient.getDeathDate() != null ? OutgoingPatientSHR.getSimpleDateFormat("yyyy-MM-dd").format(patient.getDeathDate()) : "";

        String fullName = "";
        Integer justification = null;
        String poe = cifInfo.get("poe").textValue();
        String contactWithCase = cifInfo.get("contactWithCase").textValue();
        String quarantineCenter = quarantineInfo.get("quarantineCenter").textValue();
        String nationality = physicalAddress.get("NATIONALITY").textValue();
        Integer nationalityCode = null;
        if (StringUtils.isNotBlank(nationality)) {
            if (nationality.toLowerCase().contains("kenya")) {
                nationalityCode = 1;
            } else {
                nationalityCode = 6;
            }
        }
        if (StringUtils.isNotBlank(contactWithCase)) {
            if (contactWithCase.equalsIgnoreCase("Yes")) {
                justification = 1;//"Contact with a case";
            }
        }

        if (justification == null && StringUtils.isNotBlank(poe)) {
            if (poe.equalsIgnoreCase("Point of entry")) {
                justification = 4; // point of entry
            } else if (poe.equalsIgnoreCase("Detected in Community")) {
                justification = 7; // surveillance and quarantine
            }
        }

        if (justification == null) {
            justification = 6;// other
        }


        if (patient.getGivenName() != null) {
            fullName += patient.getGivenName();
        }

        if (patient.getMiddleName() != null) {
            fullName += " " + patient.getMiddleName();
        }

        if (patient.getFamilyName() != null) {
            fullName += " " + patient.getFamilyName();
        }

        //ArrayNode labTests = OutgoingPatientSHR.getJsonNodeFactory().arrayNode();
        if (patientLabOrders != null) {
            //Get active lab orders
            List<Order> activeVLTestOrders = orderService.getActiveOrders(patient, patientLabOrders, null, null);
            if (activeVLTestOrders.size() > 0) {
                for (Order o : activeVLTestOrders) {
                    ObjectNode test = OutgoingPatientSHR.getJsonNodeFactory().objectNode();
                    test.put("case_id", caseId);
                    test.put("identifier_type", idMap.get("type"));
                    test.put("identifier", idMap.get("identifier"));
                    test.put("patient_name", fullName);
                    test.put("justification", justification);
                    if (nationalityCode != null) {
                        test.put("nationality", nationalityCode);
                    } else {
                        test.put("nationality", "");
                    }
                    test.put("county", cifInfo.get("county"));
                    test.put("subcounty", cifInfo.get("subCounty"));
                    test.put("ward", cifInfo.get("ward"));
                    test.put("residence", address.get("POSTAL_ADDRESS"));
                    test.put("sex", patient.getGender());
                    test.put("health_status", cifInfo.get("healthStatus"));
                    test.put("date_symptoms", "");
                    test.put("date_admission", cifInfo.get("admissionDate"));
                    test.put("specimen_id", o.getOrderNumber());
                    test.put("patient_id", patient.getPatientId());
                    test.put("date_isolation", "");
                    test.put("isolation_center", StringUtils.isNotBlank(quarantineCenter) ? quarantineCenter : cifInfo.get("healthFacility").textValue());
                    test.put("date_death", deathDate);
                    test.put("dob", dob);
                    test.put("lab_id", "");
                    //test.put("lab_id", getRequestLab(o.getCommentToFulfiller()));
                    test.put("test_type",o.getOrderReason() != null ? getOrderReasonCode(o.getOrderReason().getConceptId()) : "");
                    test.put("occupation", cifInfo.get("occupation"));
                    test.put("temperature", cifInfo.get("temp"));
                    test.put("sample_type", o.getInstructions() != null ? getSampleTypeCode(o.getInstructions()) : "");
                    test.put("datecollected", OutgoingPatientSHR.getSimpleDateFormat("yyyy-MM-dd").format(o.getDateActivated()));
                    test.put("symptoms", blankArray);
                    test.put("observed_signs", blankArray);
                    test.put("underlying_conditions", blankArray);
                    labTests.add(test);
                }
            }
        }

        return labTests;
    }

    private String getRequestLab(String lab) {

        if (lab == null) {
            return "";
        }
        Integer code= null;
        if (lab.equalsIgnoreCase("KEMRI Nairobi")) {
            code = 1;
        } else if (lab.equalsIgnoreCase("KEMRI CDC Kisumu")) {
            code = 2;
        } else if (lab.equalsIgnoreCase("KEMRI Alupe HIV Lab")) {
            code = 3;
        } else if (lab.equalsIgnoreCase("KEMRI Walter Reed Kericho")) {
            code = 4;
        } else if (lab.equalsIgnoreCase("AMPATH Care Lab Eldoret")) {
            code = 5;
        } else if (lab.equalsIgnoreCase("Coast Provincial General Hospital Molecular Lab")) {
            code = 6;
        } else if (lab.equalsIgnoreCase("NPHL")) {
            code = 7;
        } else if (lab.equalsIgnoreCase("Nyumbani Diagnostic Lab")) {
            code = 8;
        } else if (lab.equalsIgnoreCase("Kenyatta National Hospial Lab Nairobi")) {
            code = 9;
        } else if (lab.equalsIgnoreCase("EDARP Nairobi")) {
            code = 10;
        } else if (lab.equalsIgnoreCase("NIC")) {
            code = 11;
        }  else if (lab.equalsIgnoreCase("KEMRI Kilifi")) {
            code = 12;
        } else if (lab.equalsIgnoreCase("Aga Khan")) {
            code = 13;
        } else if (lab.equalsIgnoreCase("Lancet")) {
            code = 14;
        }

        return code != null ? code.toString() : "";
    }
    private String getSampleTypeCode(String type) {

        if (type == null) {
            return "";
        }
        Integer code = null;
        if (type.equals("Blood")) {
            code = 3;
        } else if (type.equals("OP Swab")) {
            code = 2;
        }  else if (type.equals("Tracheal Aspirate")) {
            code = 5;
        } else if (type.equals("Sputum")) {
            code = 4;
        } else if (type.equals("OP and NP Swabs")) {
            code = 1;
        } else {
            code = 6;
        }
       return code != null ? code.toString() : "";
    }

    /**
     * Converter for concept to lab system code
     * @param orderReason
     * @return
     */
    private String getOrderReasonCode(Integer orderReason) {

        if (orderReason == null)
            return "";

        Integer code = null;
        if (orderReason.equals(162080)) { // baseline
            code =1;
        } else if (orderReason.equals(162081)) { // 1st followup
            code = 2;
        } else if (orderReason.equals(164142)) { // 2nd followup
            code = 3;
        } else if (orderReason.equals(159490)) { // 3rd followup
            code = 4;
        } else if (orderReason.equals(159489)) { // 4th followup
            code = 5;
        } else if (orderReason.equals(161893)) { // 5th followup
            code = 6;
        }
       return code != null ? code.toString() : "";
    }


    private ObjectNode getCovidEnrollmentDetails(Patient patient) {

        Concept countyConcept = conceptService.getConcept(165197);
        Concept subCountyConcept = conceptService.getConcept(161551);
        Concept wardConcept = conceptService.getConcept(165195);
        Concept healthStatusConcept = conceptService.getConcept(159640);
        Concept tempConcept = conceptService.getConcept(5088);
        Concept admissionDateConcept = conceptService.getConcept(1640);
        Concept poeConcept = conceptService.getConcept(161010);
        Concept contactWithCaseConcept = conceptService.getConcept(162633);
        Concept occupationConcept = conceptService.getConcept(1542);
        Concept occupationOtherConcept = conceptService.getConcept(161011);
        Concept healthFacilityConcept = conceptService.getConcept(161550);
        ObjectNode enrollmentObj = OutgoingPatientSHR.getJsonNodeFactory().objectNode();


        String county = "", subCounty = "", ward = "", poe = "", contactWithCase = "", occupation = "", healthFacility = "";
        Integer healthStatus = null;
        Double temp = null;
        Date admissionDate = null;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");


        EncounterType covid_enc_type = encounterService.getEncounterTypeByUuid(COVID_19_CASE_INVESTIGATION);
        Encounter lastEncounter = lastEncounter(patient, covid_enc_type);

        List<Concept> questionConcepts = Arrays.asList(countyConcept, wardConcept, healthFacilityConcept, admissionDateConcept, poeConcept, subCountyConcept, healthStatusConcept, tempConcept, occupationConcept, occupationOtherConcept);
        List<Obs> enrollmentData = obsService.getObservations(
                Collections.singletonList(patient.getPerson()),
                Collections.singletonList(lastEncounter),
                questionConcepts,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false
        );

        for(Obs o: enrollmentData) {
            if (o.getConcept().equals(countyConcept) ) {
                county = o.getValueText();
            } else if (o.getConcept().equals(subCountyConcept)) {
                subCounty = o.getValueText();
            } else if (o.getConcept().equals(wardConcept)) {
                ward = o.getValueText();
            } else if (o.getConcept().equals(healthFacilityConcept)) {
                healthFacility = o.getValueText();
            } else if (o.getConcept().equals(admissionDateConcept)) {
                admissionDate = o.getValueDate();
            } else if (o.getConcept().equals(occupationOtherConcept)) {
                occupation = StringUtils.isNotBlank(occupation) ? occupation + "," + o.getValueText() : o.getValueText();
            } else if (o.getConcept().equals(healthStatusConcept)) {
                if (o.getValueCoded().getConceptId().equals(159405)) {
                    healthStatus =1;
                } else if (o.getValueCoded().getConceptId().equals(159407)) {
                    healthStatus = 2;
                } else if (o.getValueCoded().getConceptId().equals(160432)) {
                    healthStatus = 3;
                } else if (o.getValueCoded().getConceptId().equals(1067)) {
                    healthStatus =4;
                }
            } else if (o.getConcept().equals(poeConcept)) {
                if (o.getValueCoded().getConceptId().equals(165651)) {
                    poe ="Point of entry";
                } else if (o.getValueCoded().getConceptId().equals(163488)) {
                    poe = "Detected in Community";
                } else if (o.getValueCoded().getConceptId().equals(1067)) {
                    poe = "Unknown";
                }
            } else if (o.getConcept().equals(contactWithCaseConcept)) {
                if (o.getValueCoded().getConceptId().equals(1065)) {
                    contactWithCase ="Yes";
                } else if (o.getValueCoded().getConceptId().equals(1066)) {
                    contactWithCase = "No";
                } else if (o.getValueCoded().getConceptId().equals(1067)) {
                    contactWithCase = "Unknown";
                }
            } else if (o.getConcept().equals(occupationConcept)) {
                if (o.getValueCoded().getConceptId().equals(159465)) {
                    occupation = StringUtils.isNotBlank(occupation) ? occupation + ",Student" : "Student";
                } else if (o.getValueCoded().getConceptId().equals(165834)) {
                    occupation = StringUtils.isNotBlank(occupation) ? occupation + ",Working with animals" : "Working with animals";
                } else if (o.getValueCoded().getConceptId().equals(5619)) {
                    occupation = StringUtils.isNotBlank(occupation) ? occupation + ",Health care worker" : "Health care worker";
                } else if (o.getValueCoded().getConceptId().equals(164831)) {
                    occupation = StringUtils.isNotBlank(occupation) ? occupation + ",Health laboratory worker" : "Health laboratory worker";
                }
            } else if (o.getConcept().equals(tempConcept)) {
                temp = o.getValueNumeric();
            }
        }

        enrollmentObj.put("county", county);
        enrollmentObj.put("subCounty", subCounty);
        enrollmentObj.put("ward", ward);
        enrollmentObj.put("healthFacility", healthFacility);
        enrollmentObj.put("poe", poe);
        enrollmentObj.put("contactWithCase", contactWithCase);
        enrollmentObj.put("occupation", occupation);
        enrollmentObj.put("admissionDate", admissionDate != null ? df.format(admissionDate) : "");
        enrollmentObj.put("healthStatus", healthStatus != null ? healthStatus.toString() : "");
        enrollmentObj.put("temp", temp != null ? temp.toString() : "");
        return enrollmentObj;
    }

    private ObjectNode getQuarantineEnrollmentDetails(Patient patient) {

        Concept quarantineCenterConcept = conceptService.getConcept(162724);
        ObjectNode enrollmentObj = OutgoingPatientSHR.getJsonNodeFactory().objectNode();


        String quarantineCenter = "";


        EncounterType covid_enc_type = encounterService.getEncounterTypeByUuid(COVID_19_QUARANTINE_ENROLLMENT);
        Encounter lastEncounter = lastEncounter(patient, covid_enc_type);

        List<Concept> questionConcepts = Arrays.asList(quarantineCenterConcept);
        List<Obs> enrollmentData = obsService.getObservations(
                Collections.singletonList(patient.getPerson()),
                Collections.singletonList(lastEncounter),
                questionConcepts,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false
        );

        for(Obs o: enrollmentData) {
            if (o.getConcept().equals(quarantineCenterConcept) ) {
                quarantineCenter = o.getValueText();
            }
        }

        enrollmentObj.put("quarantineCenter", quarantineCenter);
        return enrollmentObj;
    }
    /**
     * Finds the last encounter during the program enrollment with the given encounter type
     *
     * @param type the encounter type
     *
     * @return the encounter
     */
    public Encounter lastEncounter(Patient patient, EncounterType type) {
        List<Encounter> encounters = Context.getEncounterService().getEncounters(patient, null, null, null, null, Collections.singleton(type), null, null, null, false);
        return encounters.size() > 0 ? encounters.get(encounters.size() - 1) : null;
    }

    /**
     * Returns a list of patients with active lab orders
     * @return
     * @param lastLabEntry
     * @param lastId
     */
    protected Set<Integer> getPatientsWithOrders(Integer lastLabEntry, Integer lastId) {

        Set<Integer> patientWithActiveLabs = new HashSet<Integer>();
        String sql = "";
        if (lastLabEntry != null && lastLabEntry > 0) {
            sql = "select patient_id from orders where order_id >" + lastLabEntry + " and order_action='NEW' and instructions is not null and comment_to_fulfiller is not null and voided=0 and date_stopped is null;";
        } else {
            sql = "select patient_id from orders where order_id <= " + lastId + " and order_action='NEW' and instructions is not null and comment_to_fulfiller is not null and voided=0 and date_stopped is null;";

        }

        List<List<Object>> activeOrders = Context.getAdministrationService().executeSQL(sql, true);
        if (!activeOrders.isEmpty()) {
            for (List<Object> res : activeOrders) {
                Integer patientId = (Integer) res.get(0);
                patientWithActiveLabs.add(patientId);
            }
        }

        return patientWithActiveLabs;
    }

    /**
     * processes results from lab     *
     * @param resultPayload this should be an array
     * @return
     */
    public String processIncomingLabResults(String resultPayload) {

        Integer statusCode;
        String statusMsg;
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode resultsObj = null;
        try {
            JsonNode actualObj = mapper.readTree(resultPayload);
            resultsObj = (ArrayNode) actualObj;
        } catch (JsonProcessingException e) {
            statusCode = 400;
            statusMsg = "The payload could not be understood. An array is expected!";
            e.printStackTrace();
            return statusMsg;
        }

        if (resultsObj.size() > 0) {
            for (int i = 0; i < resultsObj.size(); i++) {
                ObjectNode o = (ObjectNode) resultsObj.get(i);
                String specimenId = o.get("specimen_id").textValue();
                Integer specimenReceivedStatus = o.get("receivedstatus").intValue();// 1-received, 2-rejected
                String specimenRejectedReason = o.get("rejectedreason").textValue();
                //String testingLab = o.has("lab_name") ? o.get("lab_name").textValue() : null;
                Integer results = o.get("result").intValue(); //1 - negative, 2 - positive, 5 - inconclusive
                //updateOrder(specimenId, results, specimenReceivedStatus, specimenRejectedReason, testingLab);
                updateOrder(specimenId, results, specimenReceivedStatus, specimenRejectedReason);
            }
        }
        return "Results updated successfully";
    }

    /**
     * processes contact tracing
     * @param resultPayload this should be an object
     * @return
     */
    public String processIncomingContactTracingInfo(String resultPayload) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        Integer statusCode;
        String statusMsg;
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonNode = null;
        try {
             jsonNode = (ObjectNode) mapper.readTree(resultPayload);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (jsonNode != null) {
            Date appointmentDate = null;
            Date encounterdate = null;
            String contactType = jsonNode.get("follow_up_type").textValue();
            String status = "";
            String reasonUncontacted = "";
            String facilityLinkedTo = "";
            String uuid = jsonNode.get("_id").textValue();
            PatientContact patientContact = htsService.getPatientContactByUuid(uuid);
            String remarks ="";


            try {

                encounterdate = df.parse(jsonNode.get("date_last_contacted").textValue());

            } catch (ParseException e) {
                e.printStackTrace();
            }

            saveTrace(contactType,status,reasonUncontacted,facilityLinkedTo,patientContact,remarks,encounterdate);

        }
        return "Contact trace created successfully";
    }


    private void updateOrder(String orderId, Integer result, Integer receivedStatus, String rejectedReason) {

        //Order od = Context.getOrderService().getOrder(orderId);
        Order od = Context.getOrderService().getOrderByOrderNumber(orderId);

        if (od != null && od.isActive()) {

            if (receivedStatus == 2 || StringUtils.isNotBlank(rejectedReason)) {
                try {
                    orderService.discontinueOrder(od, rejectedReason != null ? rejectedReason : "Rejected order", new Date(), od.getOrderer(),
                            od.getEncounter());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Encounter enc = new Encounter();
                enc.setEncounterType(labEncounterType);
                enc.setEncounterDatetime(new Date());
                enc.setPatient(od.getPatient());
                enc.setCreator(Context.getUserService().getUser(1));

                Obs o = new Obs();
                o.setConcept(covidTestConcept);
                o.setDateCreated(new Date());
                o.setCreator(Context.getUserService().getUser(1));
                o.setObsDatetime(new Date());
                o.setPerson(od.getPatient());
                o.setOrder(od);
                o.setValueCoded(result == 1 ? covidNegConcept : result == 2 ? covidPosConcept : covidIndeterminateConcept);
                enc.addObs(o);

                try {
                    encounterService.saveEncounter(enc);
                    orderService.discontinueOrder(od, "Results received", new Date(), od.getOrderer(),
                            od.getEncounter());
                } catch (Exception e) {

                    e.printStackTrace();
                }
            }
        }
    }


    private void saveTrace(String contactType,String status,String reasonUncontacted,String facilityLinkedTo,
                      PatientContact patientContact,String remarks,Date encounterdate    ) {

        ContactTrace contactTrace = new ContactTrace();
        contactTrace.setPatientContact(patientContact);
        contactTrace.setDate(encounterdate);
        contactTrace.setContactType(contactType);
        contactTrace.setStatus(status);
        contactTrace.setReasonUncontacted(reasonUncontacted);
        contactTrace.setFacilityLinkedTo(facilityLinkedTo);
        contactTrace.setRemarks(remarks);
    //    contactTrace.setAppointmentDate(appointmentDate);
        htsService.saveClientTrace(contactTrace);

    }



}
