package org.openmrs.module.hivtestingservices.util;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Provider;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemr.api.KenyaEmrService;

import org.openmrs.module.hivtestingservices.api.HTSService;
import org.openmrs.module.hivtestingservices.api.service.DataService;
import org.openmrs.module.hivtestingservices.api.service.MedicQueData;
import org.openmrs.module.hivtestingservices.model.DataSource;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class MedicDataExchange {
    HTSService htsService = Context.getService(HTSService.class);
    DataService dataService = Context.getService(DataService.class);
    static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
    private Integer locationId = Context.getService(KenyaEmrService.class).getDefaultLocation().getLocationId();




    /**
     * processes results from cht     *
     * @param resultPayload this should be an object
     * @return
     */
    public String processIncomingFormData(String resultPayload) {
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

            ObjectNode formNode =  processFormPayload(jsonNode);
            String payload = formNode.toString();
            String discriminator = formNode.path("discriminator").path("discriminator").getTextValue();
            String formDataUuid = formNode.path("encounter").path("encounter.form_uuid").getTextValue();
            String patientUuid = formNode.path("patient").path("patient.uuid").getTextValue();
            Integer locationId = Integer.parseInt(formNode.path("encounter").path("encounter.location_id").getTextValue());
            String providerString = formNode.path("encounter").path("encounter.provider_id").getTextValue();

            saveMedicDataQueue(payload,locationId,providerString,patientUuid,discriminator,formDataUuid);

        }
        return "Data queue form created successfully";
    }

    public String processIncomingRegistration(String resultPayload) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonNode = null;
        try {
            jsonNode = (ObjectNode) mapper.readTree(resultPayload);

        } catch (IOException e) {
            e.printStackTrace();
        }
        if (jsonNode != null) {
            ObjectNode registrationNode = processRegistrationPayload(jsonNode);
            String payload = registrationNode.toString();
            String discriminator = registrationNode.path("discriminator").path("discriminator").getTextValue();
            String formDataUuid = registrationNode.path("encounter").path("encounter.form_uuid").getTextValue();
            String patientUuid = registrationNode.path("patient").path("patient.uuid").getTextValue();
            Integer locationId = Integer.parseInt(registrationNode.path("encounter").path("encounter.location_id").getTextValue());
            String providerString = registrationNode.path("encounter").path("encounter.provider_id").getTextValue();

            saveMedicDataQueue(payload,locationId,providerString,patientUuid,discriminator,formDataUuid);

        }
        return "Data queue registration created successfully";
    }

    private void saveMedicDataQueue(String payload, Integer locationId, String providerString, String patientUuid, String discriminator,
                                    String formUuid) {
        DataSource dataSource = dataService.getDataSource(1);
        Provider provider = Context.getProviderService().getProviderByIdentifier(providerString);
        Location location = Context.getLocationService().getLocation(locationId);
        Form form = Context.getFormService().getFormByUuid(formUuid);

        MedicQueData medicQueData = new MedicQueData();
        if(form !=null && form.getName() !=null) { medicQueData.setFormName(form.getName());
        }else {
            medicQueData.setFormName("Unknown name");
        }
        medicQueData.setPayload(payload);
        medicQueData.setDiscriminator(discriminator);
        medicQueData.setPatientUuid(patientUuid);
        medicQueData.setFormDataUuid(formUuid);
        medicQueData.setProvider(provider);
        medicQueData.setLocation(location);
        medicQueData.setDataSource(dataSource);
        htsService.saveQueData(medicQueData);

    }

    private ObjectNode processRegistrationPayload (ObjectNode jNode) {

        ObjectNode jsonNode = (ObjectNode) jNode.get("registration");
        ObjectNode patientNode = JsonNodeFactory.instance.objectNode();
        ObjectNode obs = JsonNodeFactory.instance.objectNode();
        ObjectNode tmp = JsonNodeFactory.instance.objectNode();
        ObjectNode discriminator = JsonNodeFactory.instance.objectNode();
        ObjectNode encounter = JsonNodeFactory.instance.objectNode();
        ObjectNode identifier = JsonNodeFactory.instance.objectNode();
        ObjectNode registrationWrapper = JsonNodeFactory.instance.objectNode();

        String patientDobKnown = jsonNode.get("patient_dobKnown") !=null ? jsonNode.get("patient_dobKnown").getTextValue():"";
        String dateOfBirth= null;
        if(patientDobKnown !=null && patientDobKnown.equalsIgnoreCase("_1066_No_99DCT") && jsonNode.get("patient_birthDate") != null) {
            dateOfBirth = jsonNode.get("patient_birthDate").getTextValue();
            patientNode.put("patient.birthdate_estimated", "true");
            patientNode.put("patient.birth_date", formatStringDate(dateOfBirth));

        }

        if(patientDobKnown !=null && patientDobKnown.equalsIgnoreCase("_1065_Yes_99DCT") && jsonNode.get("patient_dateOfBirth") != null) {
            dateOfBirth = jsonNode.get("patient_dateOfBirth").getTextValue();
            patientNode.put("patient.birth_date", formatStringDate(dateOfBirth));
        }

        String identifierProvided = jsonNode.get("patient_nationalIdnumber") != null ? jsonNode.get("patient_nationalIdnumber").getTextValue() : jsonNode.get("patient_passportNumber") != null ? jsonNode.get("patient_passportNumber").getTextValue() : "";

        identifier.put("identifier_type_name","National ID");
        identifier.put("identifier_value", identifierProvided);
        identifier.put("confirm_other_identifier_value","");
        //identifier.put("confirm_other_identifier_value",jsonNode.get("patient_nationalIdnumber").getTextValue());
        patientNode.put("patient.uuid",jsonNode.get("_id").getTextValue());
        patientNode.put("patient.family_name",jsonNode.get("patient_familyName") != null ? jsonNode.get("patient_familyName").getTextValue():"");
        patientNode.put("patient.given_name",jsonNode.get("patient_firstName") != null ? jsonNode.get("patient_firstName").getTextValue():"");
        patientNode.put("patient.middle_name",jsonNode.get("patient_middleName") != null ? jsonNode.get("patient_middleName").getTextValue(): "");
        // patientNode.put("patient.mothers_name",jsonNode.get("patient_familyName").getTextValue());
        // patientNode.put("patient.medical_record_number","337");
        patientNode.put("patient.sex",gender(jsonNode.get("patient_sex").getTextValue()));
        patientNode.put("patient.county",jsonNode.get("patient_county") != null ? jsonNode.get("patient_county").getTextValue():"");
        patientNode.put("patient.sub_county",jsonNode.get("patient_subcounty") != null ? jsonNode.get("patient_subcounty").getTextValue():"");
        patientNode.put("patient.ward",jsonNode.get("patient_ward") !=null ? jsonNode.get("patient_ward").getTextValue():"");
        patientNode.put("patient.sub_location",jsonNode.get("patient_sublocation") !=null ? jsonNode.get("patient_sublocation").getTextValue():"");
        patientNode.put("patient.location",jsonNode.get("patient_location") !=null ? jsonNode.get("patient_location").getTextValue():"");
        patientNode.put("patient.village",jsonNode.get("patient_village") != null ? jsonNode.get("patient_village").getTextValue() :"");
        patientNode.put("patient.landmark",jsonNode.get("patient_landmark") != null ? jsonNode.get("patient_landmark").getTextValue():"");
        patientNode.put("patient.phone_number",jsonNode.get("patient_telephone") != null ? jsonNode.get("patient_telephone").getTextValue():"");
        patientNode.put("patient.alternate_phone_contact",jsonNode.get("patient_alternatePhone").getTextValue());
        patientNode.put("patient.postal_address",jsonNode.get("patient_alternatePhone") != null ? jsonNode.get("patient_alternatePhone").getTextValue():"");
        patientNode.put("patient.next_of_kin_name",jsonNode.get("patient_nextofkin") != null ? jsonNode.get("patient_nextofkin").getTextValue() : "");
        patientNode.put("patient.next_of_kin_relationship",jsonNode.get("patient_nextofkinRelationship").getTextValue());
        patientNode.put("patient.next_of_kin_contact",jsonNode.get("patient_nextOfKinPhonenumber") != null ? jsonNode.get("patient_nextOfKinPhonenumber").getTextValue():"");
        patientNode.put("patient.next_of_kin_address",jsonNode.get("patient_nextOfKinPostaladdress") !=  null ? jsonNode.get("patient_nextOfKinPostaladdress").getTextValue():"");
        patientNode.put("patient.otheridentifier",getIdentifierTypes(jsonNode));

        obs.put("1054^CIVIL STATUS^99DCT",jsonNode.get("patient_marital_status") != null && !jsonNode.get("patient_marital_status").getTextValue().equalsIgnoreCase("") ? jsonNode.get("patient_marital_status").getTextValue().replace("_","^").substring(1):"");
        obs.put("1542^OCCUPATION^99DCT",jsonNode.get("patient_occupation") != null && !jsonNode.get("patient_occupation").getTextValue().equalsIgnoreCase("") ? jsonNode.get("patient_occupation").getTextValue().replace("_","^").substring(1): "");
        obs.put("1712^HIGHEST EDUCATION LEVEL^99DCT",jsonNode.get("patient_education_level") !=null && !jsonNode.get("patient_education_level").getTextValue().equalsIgnoreCase("") ? jsonNode.get("patient_education_level").getTextValue().replace("_","^").substring(1):"");

        tmp.put("tmp.birthdate_type","age");
        tmp.put("tmp.age_in_years", jsonNode.get("patient_ageYears") != null ? jsonNode.get("patient_ageYears").getTextValue() : "");
        discriminator.put("discriminator","json-registration");

        encounter.put("encounter.location_id",locationId != null ? locationId.toString() : "1");
        encounter.put("encounter.provider_id_select","admin");
        encounter.put("encounter.provider_id","admin");
        encounter.put("encounter.encounter_datetime",convertTime(jsonNode.get("reported_date").getLongValue()));
        encounter.put("encounter.form_uuid","8898c6e1-5df1-409f-b8ed-c88e6e0f24e9");
        encounter.put("encounter.user_system_id","admin");
        encounter.put("encounter.device_time_zone","Africa\\/Nairobi");
        encounter.put("encounter.setup_config_uuid","2107eab5-5b3a-4de8-9e02-9d97bce635d2");

        registrationWrapper.put("patient",patientNode);
        registrationWrapper.put("observation",obs);
        registrationWrapper.put("tmp",tmp);
        registrationWrapper.put("discriminator",discriminator);
        registrationWrapper.put("encounter",encounter);





        return registrationWrapper;
    }

    private ObjectNode processFormPayload (ObjectNode jNode) {
        ObjectNode jsonNode = (ObjectNode) jNode.get("encData");
        ObjectNode formsNode = JsonNodeFactory.instance.objectNode();
        ObjectNode discriminator = JsonNodeFactory.instance.objectNode();
        ObjectNode encounter = JsonNodeFactory.instance.objectNode();
        ObjectNode patientNode = JsonNodeFactory.instance.objectNode();
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode obsNodes = null;
        ObjectNode jsonNodes = null;
        String json = null;
        try {
            jsonNodes = (ObjectNode) mapper.readTree(jsonNode.path("fields").path("observation").toString());
            json = new ObjectMapper().writeValueAsString(jsonNodes);
            if(json != null) {
                obsNodes = (ObjectNode) mapper.readTree(json.replace("_","^"));
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

        String encounterDate = jsonNode.path("fields").path("encounter_date").getTextValue() != null && !jsonNode.path("fields").path("encounter_date").getTextValue().equalsIgnoreCase("") ? formatStringDate(jsonNode.path("fields").path("encounter_date").getTextValue()) : convertTime(jsonNode.get("reported_date").getLongValue());

        discriminator.put("discriminator","json-encounter");
        encounter.put("encounter.location_id",locationId != null ? locationId.toString(): "1");
        encounter.put("encounter.provider_id_select","admin");
        encounter.put("encounter.provider_id","admin");
        encounter.put("encounter.encounter_datetime",encounterDate);
        encounter.put("encounter.form_uuid",jsonNode.path("fields").path("form_uuid").getTextValue());
        encounter.put("encounter.user_system_id","admin");
        encounter.put("encounter.device_time_zone","Africa\\/Nairobi");
        encounter.put("encounter.setup_config_uuid",jsonNode.path("fields").path("encounter_type_uuid").getTextValue());
        patientNode.put("patient.uuid",jsonNode.path("fields").path("inputs").path("contact").path("_id").getTextValue());

        List<String> keysToRemove = new ArrayList<String>();
        if(obsNodes != null){
            Iterator<Map.Entry<String,JsonNode>> iterator = obsNodes.getFields();
            while (iterator.hasNext()) {
                Map.Entry<String, JsonNode> entry = iterator.next();
                if(entry.getKey().contains("MULTISELECT")) {
                    if (entry.getValue() != null && !"".equals(entry.getValue().toString()) && !"".equals(entry.getValue().toString())) {
                        obsNodes.put(entry.getKey(), handleMultiSelectFields(entry.getValue().toString().replace(" ",",")));
                    } else {
                        //obsNodes.remove(entry.getKey());
                        keysToRemove.add(entry.getKey());
                    }
                }
            }

        }

        if (keysToRemove.size() > 0) {
            for (String key : keysToRemove) {
                obsNodes.remove(key);
            }
        }
        formsNode.put("patient", patientNode);
        formsNode.put("observation", obsNodes);
        formsNode.put("discriminator",discriminator);
        formsNode.put("encounter",encounter);

        return   formsNode;

    }

    public String addContactListToDataqueue(String resultPayload) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonNode = null;
        try {
            jsonNode = (ObjectNode) mapper.readTree(resultPayload);
            jsonNode = (ObjectNode) jsonNode.get("formData");

        } catch (IOException e) {
            e.printStackTrace();
        }
        if (jsonNode != null) {
            String payload = jsonNode.toString();
            String discriminator = "json-patientcontact";
            String patientContactUuid = jsonNode.get("_id").getTextValue();
            Integer locationId = Context.getService(KenyaEmrService.class).getDefaultLocation().getLocationId();
            String providerString = "admin";

            saveMedicDataQueue(payload,locationId,providerString,patientContactUuid,discriminator,"");

        }
        return "Queue data for contact created successfully";
    }

    public String addContactTraceToDataqueue(String resultPayload) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonNode = null;
        try {
            jsonNode = (ObjectNode) mapper.readTree(resultPayload);
            jsonNode = (ObjectNode) jsonNode.get("traceData");



        } catch (IOException e) {
            e.printStackTrace();
        }
        if (jsonNode != null) {
            String discriminator = "json-contacttrace";
            String payload = jsonNode.toString();
            String patientContactUuid = jsonNode.get("_id").getTextValue();
            Integer locationId = Context.getService(KenyaEmrService.class).getDefaultLocation().getLocationId();
            String providerString = "admin";

            saveMedicDataQueue(payload,locationId,providerString,patientContactUuid,discriminator,"");

        }
        return "Queue data for contact trace created successfully";
    }

    private ArrayNode handleMultiSelectFields(String listOfItems){
        ArrayNode arrNode = JsonNodeFactory.instance.arrayNode();
        if (listOfItems !=null && org.apache.commons.lang3.StringUtils.isNotBlank(listOfItems)) {
            for (String s : listOfItems.split(",")) {
                arrNode.add(s.substring(1,s.length()-1));
            }
        }
        return arrNode;
    }
    private ArrayNode getIdentifierTypes(ObjectNode jsonNode) {
        ArrayNode identifierTypes = JsonNodeFactory.instance.arrayNode();

        Iterator<Map.Entry<String,JsonNode>> iterator = jsonNode.getFields();
        ObjectNode iden = null;
        while (iterator.hasNext()) {
            Map.Entry<String, JsonNode> entry = iterator.next();
            if(entry.getKey().contains("patient_identifierType")) {
                iden = handleMultipleIdentifiers(entry.getKey(),entry.getValue().getTextValue());
                if(iden !=null && iden.size() !=0 ) {
                    identifierTypes.add(iden);
                }
            }
        }
        return identifierTypes;

    }

    private ObjectNode handleMultipleIdentifiers(String identifierName,String identifierValue) {
        ArrayNode arrNodeName = JsonNodeFactory.instance.arrayNode();
        ObjectNode identifiers = JsonNodeFactory.instance.objectNode();
        if (identifierName !=null) {

            for (String s : identifierName.split("_")) {
                arrNodeName.add(s);
            }
            PatientIdentifierType identifierTypeName = null;
            if (arrNodeName.get(arrNodeName.size()-1) != null) {
                identifierTypeName = Context.getPatientService()
                        .getPatientIdentifierTypeByUuid(arrNodeName.get(arrNodeName.size()-1).getTextValue());
            }
            if(identifierTypeName != null && !identifierTypeName.getName().equalsIgnoreCase("") && !identifierValue.equalsIgnoreCase("")) {
                identifiers.put("identifier_type_uuid", arrNodeName.get(arrNodeName.size() - 1));
                identifiers.put("identifier_value", identifierValue);
                identifiers.put("identifier_type_name", identifierTypeName.getName());
            }
        }

        return  identifiers;
    }

    private String gender(String gender) {
        String abbriviateGender = null;
        if(gender.equalsIgnoreCase("male")){
            abbriviateGender ="M";
        }
        if(gender.equalsIgnoreCase("female")) {
            abbriviateGender ="F";
        }
        return abbriviateGender;
    }

    private Integer sexConverter(String sex) {
        Integer sexConcept = null;
        if(sex.equalsIgnoreCase("male")){
            sexConcept =1534;
        }
        if(sex.equalsIgnoreCase("female")) {
            sexConcept =1535;
        }
        return sexConcept;
    }

    private  String formatStringDate(String dob)  {
        String date = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
            date =sdf.format(sdf2.parse(dob));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    private String convertTime(long time){
        Date date = new Date(time);
        return DATE_FORMAT.format(date);
    }

    /**
     * Get a list of contacts for tracing
     * @return
     * @param lastContactEntry
     * @param lastContactId
     * @param gpLastPatient
     * @param lastPatientId
     */
    public ObjectNode getContacts(Integer lastContactEntry, Integer lastContactId, Integer gpLastPatient, Integer lastPatientId) {

        JsonNodeFactory factory = getJsonNodeFactory();
        ArrayNode patientContactNode = getJsonNodeFactory().arrayNode();
        ObjectNode responseWrapper = factory.objectNode();

        HTSService htsService = Context.getService(HTSService.class);
        //Set<Integer> listedContacts = getListedContacts(lastContactEntry, lastContactId);
        Map<Integer, ArrayNode> contactMap = new HashMap<Integer, ArrayNode>();

/*        if (listedContacts != null && listedContacts.size() > 0) {

            for (Integer pc : listedContacts) {
                PatientContact c = htsService.getPatientContactByID(pc);
                Patient covidCase = c.getPatientRelatedTo();
                ArrayNode contacts = null;

                ObjectNode contact = factory.objectNode();

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


                if (c.getSex() != null) {
                    if (c.getSex().equals("M")) {
                        sex = "male";
                    } else {
                        sex = "female";
                    }
                }
                contact.put("role", "covid_contact");
                contact.put("_id", c.getUuid());
                //contact.put("case_id", covidCase.getPatientIdentifier(SHRUtils.CASE_ID_TYPE) != null ? covidCase.getPatientIdentifier(SHRUtils.CASE_ID_TYPE).getIdentifier() : "");
                contact.put("case_name", covidCase.getGivenName());
                contact.put("type_of_contact", c.getContactListingDeclineReason() != null ? c.getContactListingDeclineReason() : "");
                contact.put("type_of_exposure", c.getPnsApproach() != null ? getContactType().get(c.getPnsApproach()) : "");
                contact.put("relation_to_case", c.getRelationType() != null ? getContactRelation().get(c.getRelationType()) : "");

                contact.put("national_id", "");
                contact.put("passport_number", "");
                contact.put("alien_number", "");
                contact.put("s_name",c.getLastName() != null ? c.getLastName() : "");
                contact.put("f_name",c.getFirstName() != null ? c.getFirstName() : "");
                contact.put("o_name",c.getMiddleName() != null ? c.getMiddleName() : "");
                contact.put("name", fullName);
                contact.put("sex",sex);
                contact.put("date_of_birth", c.getBirthDate() != null ? MedicDataExchange.getSimpleDateFormat(dateFormat).format(c.getBirthDate()) : "");
                contact.put("dob_known", "no");
                contact.put("marital_status", "");

                contact.put("occupation", "");
                contact.put("occupation_other", "");
                contact.put("healthcare_worker", c.getMaritalStatus() != null && c.getMaritalStatus().equals(1065) ? "yes" : "no");
                contact.put("education", "");
                contact.put("deceased", "");
                contact.put("nationality", "");
                contact.put("phone", c.getPhoneContact() != null ? c.getPhoneContact() : "");
                contact.put("alternate_phone", "");
                contact.put("postal_address", c.getPhysicalAddress() != null ? c.getPhysicalAddress() : "");
                contact.put("email_address", "");
                contact.put("county", "");
                //contact.put("subcounty", c.getSubcounty() != null ? c.getSubcounty() : "");
                contact.put("ward", "");
                contact.put("location", "");
                contact.put("sub_location","");
                contact.put("village", c.getPhysicalAddress() != null ? c.getPhysicalAddress() : "");
                contact.put("landmark", "");
                //contact.put("residence", c.getTown() != null ? c.getTown() : "");
                contact.put("nearest_health_center", "");
                contact.put("kin_name", "");
                contact.put("kin_relationship", "");
                contact.put("kin_phone_number", "");
                contact.put("kin_postal_address", "");

                contact.put("reported_date", c.getDateCreated().getTime());
                contact.put("patient_id", covidCase.getPatientId().toString());

                if (contactMap.keySet().contains(covidCase.getPatientId())) {
                    contacts = contactMap.get(covidCase.getPatientId());
                    contacts.add(contact);
                } else {
                    contacts = factory.arrayNode();
                    contacts.add(contact);
                    contactMap.put(covidCase.getPatientId(), contacts);
                }
            }

            for (Map.Entry<Integer, ArrayNode> entry : contactMap.entrySet()) {
                Integer caseId = entry.getKey();
                ArrayNode contacts = entry.getValue();

                Patient patient = Context.getPatientService().getPatient(caseId);
                ObjectNode contactWrapper = buildPatientNode(patient);
                contactWrapper.put("contacts", contacts);
                patientContactNode.add(contactWrapper);

            }
        }*/

        // add for cases under investigation but with no contacts
        ArrayNode emptyContactNode = factory.arrayNode();
        Set<Integer> patientList = getClientsForTestingAndContactListing(gpLastPatient, lastPatientId);
        if (patientList.size() > 0) {
            for (Integer ptId : patientList) {
                if (!contactMap.keySet().contains(ptId)) {
                    Patient patient = Context.getPatientService().getPatient(ptId);
                    ObjectNode contactWrapper = buildPatientNode(patient);
                    contactWrapper.put("contacts", emptyContactNode);
                    patientContactNode.add(contactWrapper);
                }
            }
        }

        responseWrapper.put("docs", patientContactNode);
        return responseWrapper;
    }

    private ObjectNode buildPatientNode(Patient patient) {
        JsonNodeFactory factory = getJsonNodeFactory();
        ObjectNode objectWrapper = factory.objectNode();
        ObjectNode fields = factory.objectNode();

        String sex = "";
        String dateFormat = "yyyy-MM-dd";

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



        if (patient.getGender() != null) {
            if (patient.getGender().equals("M")) {
                sex = "male";
            } else {
                sex = "female";
            }
        }

        objectWrapper.put("_id",patient.getUuid());
        objectWrapper.put("type","data_record");
        objectWrapper.put("form","case_information");
        objectWrapper.put("content_type","xml");
        objectWrapper.put("reported_date",patient.getDateCreated().getTime());


        PatientIdentifier nationalId = patient.getPatientIdentifier(Utils.NATIONAL_ID);

        /*PatientIdentifier caseId = patient.getPatientIdentifier(SHRUtils.CASE_ID_TYPE);
        PatientIdentifier nationalId = patient.getPatientIdentifier(SHRUtils.NATIONAL_ID_TYPE);
        PatientIdentifier passportNumber = patient.getPatientIdentifier(SHRUtils.PASSPORT_NUMBER_TYPE);
        PatientIdentifier alienNumber = patient.getPatientIdentifier(SHRUtils.ALIEN_NUMBER_TYPE);
        PatientIdentifier chtReference = patient.getPatientIdentifier(SHRUtils.CHT_REFERENCE_UUID);
*/
        // get address

        /*ObjectNode address = SHRUtils.getPatientAddress(patient);
        ObjectNode physicalAddress = (ObjectNode) address.get("PHYSICAL_ADDRESS");
        String nationality = physicalAddress.get("NATIONALITY").textValue();

        String postalAddress = address.get("POSTAL_ADDRESS").textValue();
        String county = physicalAddress.get("COUNTY").textValue();
        String subCounty = physicalAddress.get("SUB_COUNTY").textValue();
        String ward = physicalAddress.get("WARD").textValue();
        String landMark = physicalAddress.get("NEAREST_LANDMARK").textValue();*/


        fields.put("needs_sign_off",false);
        fields.put("case_id",patient.getUuid());
        //fields.put("cht_ref_uuid",chtReference != null ? chtReference.getIdentifier() : "");
        fields.put("patient_identifierType_nationalId_49af6cdc-7968-4abb-bf46-de10d7f4859f", nationalId != null ? nationalId.getIdentifier() : "");

        fields.put("patient_familyName",patient.getFamilyName() != null ? patient.getFamilyName() : "");
        fields.put("patient_firstName",patient.getGivenName() != null ? patient.getGivenName() : "");
        fields.put("patient_middleName",patient.getMiddleName() != null ? patient.getMiddleName() : "");
        fields.put("name", fullName);
        fields.put("patient_name", fullName);
        fields.put("sex",sex);
        fields.put("date_of_birth", patient.getBirthdate() != null ? getSimpleDateFormat(dateFormat).format(patient.getBirthdate()) : "");
        fields.put("patient_dobKnown", "_1066_No_99DCT");
        fields.put("patient_telephone", "");
        fields.put("patient_nationality", "");
        fields.put("place_id", "3b1fc65e-ca03-473a-bd59-54c0104408f8");
        fields.put("patient_county", "Nairobi");
        fields.put("patient_subcounty","Westlands");
        fields.put("patient_ward", "Mavoko");
        fields.put("location", "");
        fields.put("sub_location","");
        fields.put("patient_village", "");
        fields.put("patient_landmark", "");
        fields.put("patient_residence", "");
        fields.put("patient_nearesthealthcentre", "");
        fields.put("assignee","");

        objectWrapper.put("fields", fields);
        return objectWrapper;

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

    private Integer getContactRelationConcept(String code) {
        Integer concept = null;
        if (code == null) {
            return null;
        }

        if (code.equals("co-worker")) {
            concept = 160237;
        } else if (code.equals("mother")) {
            concept = 970;
        } else if (code.equals("father")) {
            concept = 971;
        } else if (code.equals("sibling")) {
            concept = 972;
        } else if (code.equals("child")) {
            concept = 1528;
        } else if (code.equals("spouse")) {
            concept = 5617;
        } else if (code.equals("partner")) {
            concept = 163565;
        } else { // map everything to traveled together
            concept = 165656;
        }


        return concept;
    }

    private Map<Integer, String> getContactType() {
        Map<Integer, String> options = new HashMap<Integer, String>();
        options.put(160237,"Working together with a nCoV patient");
        options.put(165656,"Traveling together with a nCoV patient");
        options.put(1060,"Living together with a nCoV patient");
        options.put(117163,"Health care associated exposure");
        return options;

    }

    private Integer getContactTypeConcept(Integer code) {
        if (code == null) {
            return null;
        }
        Integer concept = null;
        if (code == 1) {
            concept = 117163;
        } else if (code == 2) {
            concept = 160237;
        } else if (code == 3) {
            concept = 165656;
        } else if (code == 4) {
            concept = 1060;
        }
        return concept;

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
            sql = "select id from kenyaemr_hiv_testing_patient_contact where id >" + lastContactEntry + " and patient_id is null and voided=0 and (ipv_outcome !='CHT' or ipv_outcome is null);"; // get contacts not registered
        } else {
            sql = "select id from kenyaemr_hiv_testing_patient_contact where id <= " + lastId + " and patient_id is null and voided=0 and (ipv_outcome !='CHT' or ipv_outcome is null);";

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

    protected Set<Integer> getClientsForTestingAndContactListing(Integer lastPatientEntry, Integer lastId) {

        Set<Integer> eligibleList = new HashSet<Integer>();

        String sql = "";
        if (lastPatientEntry != null && lastPatientEntry > 0) {
            /*sql = "select pp.patient_id from patient_program pp \n" +
                    "inner join (select program_id from program where uuid='e7ee7548-6958-4361-bed9-ee2614423947') p on pp.program_id = p.program_id\n" +
                    "inner join obs o on o.person_id = pp.patient_id and o.concept_id=165611 and o.value_coded=703\n" +
                    "where o.obs_id >" + lastPatientEntry + " and pp.voided=0 and o.voided=0;";*/
            sql = "select patient_id from patient where voided=0 limit 5";
        } else {

            /*sql = "select pp.patient_id from patient_program pp \n" +
                    "inner join (select program_id from program where uuid='e7ee7548-6958-4361-bed9-ee2614423947') p on pp.program_id = p.program_id\n" +
                    "inner join obs o on o.person_id = pp.patient_id and o.concept_id=165611 and o.value_coded=703\n" +
                    "where o.obs_id <= " + lastId + " and pp.voided=0 and o.voided=0;";*/
            sql = "select patient_id from patient where voided=0 limit 5";

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

    private JsonNodeFactory getJsonNodeFactory() {
        final JsonNodeFactory factory = JsonNodeFactory.instance;
        return factory;
    }

    public static SimpleDateFormat getSimpleDateFormat(String pattern) {
        return new SimpleDateFormat(pattern);
    }


}
