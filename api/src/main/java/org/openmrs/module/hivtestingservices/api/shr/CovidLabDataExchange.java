package org.openmrs.module.hivtestingservices.api.shr;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;


import com.thoughtworks.xstream.core.util.PresortedSet;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.GlobalProperty;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.hivtestingservices.metadata.HTSMetadata;
import org.openmrs.module.hivtestingservices.api.ContactTrace;
import org.openmrs.module.hivtestingservices.api.HTSService;
import org.openmrs.module.hivtestingservices.api.PatientContact;
import org.openmrs.module.idgen.service.IdentifierSourceService;
import org.openmrs.module.metadatadeploy.MetadataUtils;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import static org.openmrs.module.hivtestingservices.wrapper.PatientWrapper.OPENMRS_ID;
import static org.openmrs.util.LocationUtility.getDefaultLocation;

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
    Concept covidTestConcept = conceptService.getConcept(165611);
    Concept covidPosConcept = conceptService.getConcept(703);
    Concept covidNegConcept = conceptService.getConcept(664);
    Concept covidIndeterminateConcept = conceptService.getConcept(1138);
    EncounterType labEncounterType = encounterService.getEncounterTypeByUuid(LAB_ENCOUNTER_TYPE_UUID);
    HTSService htsService = Context.getService(HTSService.class);




    /**
     * Returns a list of active lab requests
     * @return
     */
    public ObjectNode getCovidLabRequests() {

        JsonNodeFactory factory = OutgoingPatientSHR.getJsonNodeFactory();
        ArrayNode activeRequests = factory.arrayNode();
        ObjectNode requestWrapper = factory.objectNode();
        Set<Integer> allPatients = getPatientsWithOrders();
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
    private ObjectNode getPatientAddress(Patient patient) {
        Set<PersonAddress> addresses = patient.getAddresses();
        //patient address
        ObjectNode patientAddressNode = OutgoingPatientSHR.getJsonNodeFactory().objectNode();
        ObjectNode physicalAddressNode = OutgoingPatientSHR.getJsonNodeFactory().objectNode();
        String postalAddress = "";
        String county = "";
        String sub_county = "";
        String ward = "";
        String landMark = "";

        for (PersonAddress address : addresses) {
            if (address.getAddress1() != null) {
                postalAddress = address.getAddress1();
            }
            if (address.getCountry() != null) {
                county = address.getCountry() != null ? address.getCountry() : "";
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

        for (PatientIdentifier identifier : identifierList) {
            PatientIdentifierType identifierType = identifier.getIdentifierType();

            if (identifierType.equals(NATIONAL_ID_TYPE)) {
                patientIdentifiers.put("type", 1);
                patientIdentifiers.put("identifier", identifier.getIdentifier());
                return patientIdentifiers;

            } else if (identifierType.equals(ALIEN_NUMBER_TYPE)) {
                patientIdentifiers.put("type", 3);
                patientIdentifiers.put("identifier", identifier.getIdentifier());
                return patientIdentifiers;


            } else if (identifierType.equals(PASSPORT_NUMBER_TYPE)) {
                patientIdentifiers.put("type", 2);
                patientIdentifiers.put("identifier", identifier.getIdentifier());
                return patientIdentifiers;

            } else if (identifierType.equals(CASE_ID_TYPE) || identifierType.equals(OPENMRS_ID_TYPE)) { // use this to track those with no documented identifier
                patientIdentifiers.put("type", 4);
                patientIdentifiers.put("identifier", identifier.getIdentifier());
                return patientIdentifiers;
            }

        }
        return patientIdentifiers;
    }

    /**
     * Returns object lab request for patients
     * @param patient
     * @return
     */
    protected ArrayNode getActiveLabRequestsForPatient(Patient patient, ArrayNode labTests) {

        ObjectNode cifInfo = getCovidEnrollmentDetails(patient);
        ObjectNode address = getPatientAddress(patient);
        ArrayNode blankArray = OutgoingPatientSHR.getJsonNodeFactory().arrayNode();
        OrderService orderService = Context.getOrderService();
        Integer caseId = patient.getPatientId();
        ObjectNode idMap = getPatientIdentifier(patient);
        //Check whether client has active covid order
        OrderType patientLabOrders = orderService.getOrderTypeByUuid(TEST_ORDER_TYPE_UUID);
        String dob = patient.getBirthdate() != null ? OutgoingPatientSHR.getSimpleDateFormat("yyyy-MM-dd").format(patient.getBirthdate()) : "";
        String deathDate = patient.getDeathDate() != null ? OutgoingPatientSHR.getSimpleDateFormat("yyyy-MM-dd").format(patient.getDeathDate()) : "";

        String fullName = "";

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
                    test.put("justification", "");
                    test.put("county", cifInfo.get("county"));
                    test.put("subcounty", cifInfo.get("subCounty"));
                    test.put("ward", "");
                    test.put("residence", address.get("POSTAL_ADDRESS"));
                    test.put("sex", patient.getGender());
                    test.put("health_status", cifInfo.get("healthStatus"));
                    test.put("date_symptoms", "");
                    test.put("date_admission", "");
                    test.put("specimen_id", o.getOrderId());
                    test.put("patient_id", patient.getPatientId());
                    test.put("date_isolation", "");
                    test.put("date_death", deathDate);
                    test.put("dob", dob);
                    test.put("lab_id", getRequestLab(o.getCommentToFulfiller()));
                    test.put("test_type_id",o.getOrderReason() != null ? getOrderReasonCode(o.getOrderReason().getConceptId()) : "");
                    test.put("occupation", "");
                    test.put("temperature", cifInfo.get("temp"));
                    test.put("sample_type", o.getInstructions() != null ? getSampleTypeCode(o.getInstructions()) : "");
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
        if (lab.equals("NPHL")) {
            code = 7;
        } else if (lab.equals("KEMRI Nairobi")) {
            code = 1;
        }  else if (lab.equals("KEMRI Kilifi")) {
            code = 12;
        } else if (lab.equals("KEMRI CDC Kisumu")) {
            code = 2;
        } else if (lab.equals("KEMRI Walter Reed Kericho")) {
            code = 4;
        }
        return code.toString();
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
        } else if (type.equals("NP Swab")) {
            code = 1;
        } else {
            code = 6;
        }
       return code.toString();
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
        Concept healthStatusConcept = conceptService.getConcept(159640);
        Concept tempConcept = conceptService.getConcept(5088);
        ObjectNode enrollmentObj = OutgoingPatientSHR.getJsonNodeFactory().objectNode();


        String county = "", subCounty = "";
        Integer healthStatus = null;
        Double temp = null;


        EncounterType covid_enc_type = encounterService.getEncounterTypeByUuid(COVID_19_CASE_INVESTIGATION);
        Encounter lastEncounter = lastEncounter(patient, covid_enc_type);

        List<Concept> questionConcepts = Arrays.asList(countyConcept, subCountyConcept, healthStatusConcept, tempConcept);
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
            } else if (o.getConcept().equals(healthStatusConcept)) {
                if (o.getValueCoded().getConceptId().equals(159405)) {
                    //healthStatus = "Stable";
                    healthStatus =1;
                } else if (o.getValueCoded().getConceptId().equals(159407)) {
                    //healthStatus = "Severely ill";
                    healthStatus = 2;

                } else if (o.getValueCoded().getConceptId().equals(160432)) {
                    //healthStatus = "Dead";
                    healthStatus = 3;
                } else if (o.getValueCoded().getConceptId().equals(1067)) {
                    //healthStatus = "Unknown";
                    healthStatus =4;
                }
            } else if (o.getConcept().equals(tempConcept)) {
                temp = o.getValueNumeric();
            }
        }

        enrollmentObj.put("county", county);
        enrollmentObj.put("subCounty", subCounty);
        enrollmentObj.put("healthStatus", healthStatus != null ? healthStatus.toString() : "");
        enrollmentObj.put("temp", temp != null ? temp.toString() : "");
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
     */
    protected Set<Integer> getPatientsWithOrders() {

        Set<Integer> patientWithActiveLabs = new HashSet<Integer>();
        GlobalProperty lastLabEntry = Context.getAdministrationService().getGlobalPropertyObject(HTSMetadata.LAST_LAB_ORDER_ENTRY);
        String lastOrdersql = "select max(order_id) last_id from orders where voided=0;";
        List<List<Object>> lastOrderId = Context.getAdministrationService().executeSQL(lastOrdersql, true);
        Integer lastId = (Integer) lastOrderId.get(0).get(0);
        String sql = "";
        if (lastLabEntry != null) {
            Integer lastEntry = Integer.parseInt(lastLabEntry.getValue().toString());
            sql = "select patient_id from orders where order_id >" + lastEntry + " and order_action='NEW' and instructions is not null and comment_to_fulfiller is not null and voided=0;";
        } else {
            lastLabEntry = new GlobalProperty();
            lastLabEntry.setProperty(HTSMetadata.LAST_LAB_ORDER_ENTRY);
            lastLabEntry.setDescription("Id of the last order entry");
            sql = "select patient_id from orders where order_id <= " + lastId + " and order_action='NEW' and instructions is not null and comment_to_fulfiller is not null and voided=0;";

        }
        lastLabEntry.setPropertyValue(lastId.toString());

        List<List<Object>> activeOrders = Context.getAdministrationService().executeSQL(sql, true);
        if (!activeOrders.isEmpty()) {
            for (List<Object> res : activeOrders) {
                Integer patientId = (Integer) res.get(0);
                patientWithActiveLabs.add(patientId);
            }
        }

        Context.getAdministrationService().saveGlobalProperty(lastLabEntry);
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
                Integer specimenId = o.get("specimen_id").intValue();
                Integer specimenReceivedStatus = o.get("receivedstatus").intValue();// 1-received, 2-rejected
                String specimenRejectedReason = o.get("rejectedreason").textValue();
                Integer results = o.get("result").intValue(); //1 - negative, 2 - positive, 5 - inconclusive
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
    /**
     * processes contact registration
     * @param resultPayload this should be an object
     * @return
     */
    public String processIncomingContactRegistrationInfo(String resultPayload) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonNode = null;
        try {
            jsonNode = (ObjectNode) mapper.readTree(resultPayload);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (jsonNode != null) {
            boolean errorOccured = false;

            String familyName = jsonNode.get("family_name").textValue();
            String middleName = jsonNode.get("given_names").textValue();
            String lastName = jsonNode.get("name").textValue();
            String patientName = jsonNode.get("patient_name").textValue();
            String dob = jsonNode.get("date_of_birth").textValue();
            String sex = jsonNode.get("sex").textValue();
            String phone = jsonNode.get("phone").textValue();
            String primaryPhone = jsonNode.get("primary_phone").textValue();
            String alternatePhone = jsonNode.get("alternate_phone").textValue();
            String email = jsonNode.get("email").textValue();
            String county = jsonNode.get("county").textValue();
            String country = jsonNode.get("country_of_residence").textValue();
            String subCounty = jsonNode.get("subcounty").textValue();
            String postalAddress = jsonNode.get("postal_address").textValue();
            String patient_id = jsonNode.get("patient_id").textValue();
            String uuid = jsonNode.get("_id").textValue();

            PatientContact patientContact = htsService.getPatientContactByUuid(uuid);

//Register patient
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
            Patient patient = new Patient();
            patient = new Patient();
            // Add gender
            if (sex != null) {
                if (sex.equals("male")) {
                    patient.setGender("M");
                } else if (sex.equals("female")) {
                    patient.setGender("F");
                }else{patient.setGender("");}
            }
            // Add names
            PersonName personName = new PersonName();
            SortedSet<PersonName> names = new PresortedSet();
            personName.setGivenName(lastName != null ? lastName : "");
            personName.setMiddleName(middleName != null ? middleName : "");
            personName.setFamilyName(familyName != null ? familyName : "");
            names.add(personName);
            patient.setNames(names);

            //patient.addName(new PersonName(familyName, middleName, lastName));
            // Add dob
            if (dob != null) {
                try {
                    patient.setBirthdate(formatter.parse(dob));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            // Make sure everyone gets an OpenMRS ID
            PatientIdentifierType openmrsIdType = MetadataUtils.existing(PatientIdentifierType.class, OPENMRS_ID);
            PatientIdentifier openmrsId = patient.getPatientIdentifier(openmrsIdType);

            if (openmrsId == null) {
                String generated = Context.getService(IdentifierSourceService.class).generateIdentifier(openmrsIdType, "Registration");
                openmrsId = new PatientIdentifier(generated, openmrsIdType, getDefaultLocation());
                patient.addIdentifier(openmrsId);

                if (!patient.getPatientIdentifier().isPreferred()) {
                    openmrsId.setPreferred(true);
                }
            }

            // Add county, sub county and postal address
            SortedSet<PersonAddress> addresses = new PresortedSet();
            PersonAddress personAddress = new PersonAddress();
            personAddress.setPreferred(true);
            personAddress.setStateProvince(subCounty != null ? subCounty : "");
            personAddress.setCountyDistrict(county != null ? county : "");
            personAddress.setCountry(country != null ? country : "");
            personAddress.setAddress1(postalAddress != null ? postalAddress : "");
            addresses.add(personAddress);
            patient.setAddresses(addresses);

            //        Process phone number as an attribute

            if (primaryPhone != null) {
                PersonAttribute phoneAttribute = new PersonAttribute();
                PersonAttributeType phoneAttributeType = Context.getPersonService().getPersonAttributeTypeByUuid("b2c38640-2603-4629-aebd-3b54f33f1e3a");
                phoneAttribute.setAttributeType(phoneAttributeType);
                phoneAttribute.setValue(primaryPhone);
                patient.addAttribute(phoneAttribute);
            }

            // Save patient
            try {

                patient = Context.getPatientService().savePatient(patient);

            } catch (Exception e) {
                e.printStackTrace();
                errorOccured = true;
            }
        }
        return "Contact registered/created successfully";
    }


    private void updateOrder(Integer orderId, Integer result, Integer receivedStatus, String rejectedReason) {

        Order od = Context.getOrderService().getOrder(orderId);
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
