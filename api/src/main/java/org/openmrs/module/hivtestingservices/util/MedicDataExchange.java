package org.openmrs.module.hivtestingservices.util;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Provider;
import org.openmrs.api.context.Context;
import org.openmrs.module.hivtestingservices.api.HTSService;
import org.openmrs.module.hivtestingservices.api.PatientContact;
import org.openmrs.module.hivtestingservices.api.service.DataService;
import org.openmrs.module.hivtestingservices.api.service.MedicQueData;
import org.openmrs.module.hivtestingservices.model.DataSource;
import org.openmrs.module.hivtestingservices.utils.JsonUtils;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class MedicDataExchange {
    HTSService htsService = Context.getService(HTSService.class);
    DataService dataService = Context.getService(DataService.class);
    static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");


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

        String identifierProvided = jsonNode.get("patient_nationalIdnumber") != null ? jsonNode.get("patient_nationalIdnumber").getTextValue() : jsonNode.get("patient_passportNumber") != null ? jsonNode.get("patient_passportNumber").getTextValue() : "";

        identifier.put("identifier_type_name","National ID");
        identifier.put("identifier_value", identifierProvided);
        identifier.put("confirm_other_identifier_value","");
        //identifier.put("confirm_other_identifier_value",jsonNode.get("patient_nationalIdnumber").getTextValue());

        patientNode.put("patient.uuid",jsonNode.get("_id").getTextValue());
        patientNode.put("patient.family_name",jsonNode.get("patient_familyName").getTextValue());
        patientNode.put("patient.given_name",jsonNode.get("patient_firstName").getTextValue());
        patientNode.put("patient.middle_name",jsonNode.get("patient_middleName").getTextValue());
        // patientNode.put("patient.mothers_name",jsonNode.get("patient_familyName").getTextValue());
        // patientNode.put("patient.medical_record_number","337");
        patientNode.put("patient.sex",gender(jsonNode.get("patient_sex").getTextValue()));
        patientNode.put("patient.birth_date",formatStringDate(jsonNode.get("patient_birthDate").getTextValue()));
        // patientNode.put("patient.birthdate_estimated",jsonNode.get("patient_familyName").getTextValue());
        patientNode.put("patient.county",jsonNode.get("patient_county").getTextValue());
        patientNode.put("patient.sub_county",jsonNode.get("patient_subcounty").getTextValue());
        patientNode.put("patient.ward",jsonNode.get("patient_ward").getTextValue());
        patientNode.put("patient.village",jsonNode.get("patient_village").getTextValue());
        patientNode.put("patient.landmark",jsonNode.get("patient_landmark").getTextValue());
        patientNode.put("patient.phone_number",jsonNode.get("patient_telephone").getTextValue());
        patientNode.put("patient.alternate_phone_contact",jsonNode.get("patient_alternatePhone").getTextValue());
        patientNode.put("patient.postal_address",jsonNode.get("patient_alternatePhone").getTextValue());
        patientNode.put("patient.next_of_kin_name",jsonNode.get("patient_nextofkin").getTextValue());
        patientNode.put("patient.next_of_kin_relationship",jsonNode.get("patient_nextofkinRelationship").getTextValue());
        patientNode.put("patient.next_of_kin_contact",jsonNode.get("patient_nextOfKinPhonenumber").getTextValue());
        patientNode.put("patient.next_of_kin_address",jsonNode.get("patient_nextOfKinPostaladdress").getTextValue());
        patientNode.put("patient.otheridentifier",identifier);

        obs.put("identifier_type_name","National ID");
        obs.put("1054^CIVIL STATUS^99DCT",jsonNode.get("patient_marital_status").getTextValue().replace("_","^").substring(1));
        obs.put("1542^OCCUPATION^99DCT",jsonNode.get("patient_occupation").getTextValue().replace("_","^").substring(1));
        obs.put("1712^HIGHEST EDUCATION LEVEL^99DCT",jsonNode.get("patient_education_level").getTextValue().replace("_","^").substring(1));

        tmp.put("tmp.birthdate_type","age");
        tmp.put("tmp.age_in_years", jsonNode.get("patient_ageYears") != null ? jsonNode.get("patient_ageYears").getTextValue() : "");
        discriminator.put("discriminator","json-registration");

        encounter.put("encounter.location_id","7185");
        encounter.put("encounter.provider_id_select","admin");
        encounter.put("encounter.provider_id","admin");
        encounter.put("encounter.encounter_datetime",convertTime(jsonNode.get("reported_date").getLongValue()));
        encounter.put("encounter.form_uuid","8898c6e1-5df1-409f-b8ed-c88e6e0f24e9");
        encounter.put("encounter.user_system_id",jsonNode.path("meta").path("created_by").getTextValue());
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

        discriminator.put("discriminator","json-encounter");
        encounter.put("encounter.location_id","7185");
        encounter.put("encounter.provider_id_select","admin");
        encounter.put("encounter.provider_id","admin");
        encounter.put("encounter.encounter_datetime",convertTime(jsonNode.get("reported_date").getLongValue()));
        encounter.put("encounter.form_uuid",jsonNode.path("fields").path("form_uuid").getTextValue());
        encounter.put("encounter.user_system_id","admin");
        encounter.put("encounter.device_time_zone","Africa\\/Nairobi");
        encounter.put("encounter.setup_config_uuid",jsonNode.path("fields").path("encounter_type_uuid").getTextValue());
        patientNode.put("patient.uuid",jsonNode.path("fields").path("inputs").path("contact").path("_id").getTextValue());

        if(obsNodes != null){
            Iterator<Map.Entry<String,JsonNode>> iterator = obsNodes.getFields();
            while (iterator.hasNext()) {
                Map.Entry<String, JsonNode> entry = iterator.next();
                if(entry.getKey().contains("MULTISELECT")) {
                    obsNodes.put(entry.getKey(), handleMultiSelectFields(entry.getValue().toString().replace(" ",",")));
                }
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
            Integer locationId = 715;
            String providerString = "admin";

            saveMedicDataQueue(payload,locationId,providerString,patientContactUuid,discriminator,"");

        }
        return "Data queue contact created successfully";
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
            Integer locationId = 715;
            String providerString = "admin";

            saveMedicDataQueue(payload,locationId,providerString,patientContactUuid,discriminator,"");

        }
        return "Data queue contact trace created successfully";
    }

    private ArrayNode handleMultiSelectFields(String listOfItems){
        ArrayNode arrNode = JsonNodeFactory.instance.arrayNode();
        if (listOfItems !=null) {
            for (String s : listOfItems.split(",")) {
                arrNode.add(s.substring(1,s.length()-1));
            }
        }
        return arrNode;
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


}
