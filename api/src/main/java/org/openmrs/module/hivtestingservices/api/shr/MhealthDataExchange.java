package org.openmrs.module.hivtestingservices.api.shr;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Attributable;
import org.openmrs.Encounter;
import org.openmrs.GlobalProperty;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PatientProgram;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.hivtestingservices.api.HTSService;
import org.openmrs.module.hivtestingservices.api.PatientContact;
import org.openmrs.module.hivtestingservices.metadata.HTSMetadata;
import org.openmrs.module.idgen.service.IdentifierSourceService;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.util.StringUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MhealthDataExchange {

    protected static final Log log = LogFactory.getLog(MhealthDataExchange.class);
    public static final String COVID_QUARANTINE_ENROLLMENT_ENCOUNTER = "33a3a55c-73ae-11ea-bc55-0242ac130003";
    public static final String COVID_QUARANTINE_ENROLLMENT_FORM = "9a5d57b6-739a-11ea-bc55-0242ac130003";
    public static final String COVID_QUARANTINE_PROGRAM = "9a5d555e-739a-11ea-bc55-0242ac130003";
    public static final String COVID_QUARANTINE_FOLLOWUP_ENCOUNTER = "33a3a8e0-73ae-11ea-bc55-0242ac130003";
    public static final String COVID_QUARANTINE_FOLLOWUP_FORM = "33a3aab6-73ae-11ea-bc55-0242ac130003";
    public static final String FEVER_CONCEPT = "140238AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String COUGH_CONCEPT = "143264AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String DIFFICULTY_BREATHING_CONCEPT = "164441AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String SORE_THROAT_CONCEPT = "162737AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String CONTACT_HAS_SORE_THROAT_CONCEPT = "158843AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String YES_CONCEPT = "1065AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String NO_CONCEPT = "1066AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String FOLLOWUP_SEQUENCE_CONCEPT = "165416AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String TELEPHONE_CONTACT = "b2c38640-2603-4629-aebd-3b54f33f1e3a";



    private static Patient createPatient(String fullName, Integer age, String sex, String idNo) {

        Patient patient = null;
        String PASSPORT_NUMBER = "e1e80daa-6d7e-11ea-bc55-0242ac130003";

        fullName = fullName.replace(".","");
        fullName = fullName.replace(",", "");
        fullName = fullName.replace("  ", " ");

        String fName = "", mName = "", lName = "";
        if (fullName != null && !fullName.equals("")) {

            String [] nameParts = fullName.trim().split(" ");
            fName = nameParts[0].trim();
            if (nameParts.length > 1) {
                lName = nameParts[1].trim();
            } else {
                lName = nameParts[0].trim();
            }
            if (nameParts.length > 2) {
                mName = nameParts[2].trim();
            }

            fName = fName != null && !fName.equals("") ? fName : "";
            mName = mName != null && !mName.equals("") ? mName : "";
            lName = lName != null && !lName.equals("") ? lName : "";
            patient = new Patient();
            if (sex == null || sex.equals("") || StringUtils.isEmpty(sex)) {
                sex = "U";
            }
            patient.setGender(sex);
            PersonName pn = new PersonName();//Context.getPersonService().parsePersonName(fullName);
            pn.setGivenName(fName);
            pn.setFamilyName(lName);
            if (mName != null && !mName.equals("")) {
                pn.setMiddleName(mName);
            }
            System.out.print("Person name: " + pn);

            patient.addName(pn);

            if (age == null) {
                age = 100;
            }
            Calendar effectiveDate = Calendar.getInstance();
            effectiveDate.set(2020, 3, 1, 0, 0);

            Calendar computedDob = Calendar.getInstance();
            computedDob.setTimeInMillis(effectiveDate.getTimeInMillis());
            computedDob.add(Calendar.YEAR, -age);

            if (computedDob != null) {
                patient.setBirthdate(computedDob.getTime());
            }

            patient.setBirthdateEstimated(true);

            System.out.println(", ID No: " + idNo);

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

    private static Patient addPersonAttributes(Patient patient, String phone, String nokName, String nokPhone) {

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

    private static Patient addPersonAddresses(Patient patient, String nationality, String county, String subCounty, String ward, String postaladdress) {

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

    private static Patient saveAndenrollPatientInCovidQuarantine(Patient patient, Date admissionDate, String quarantineCenter) {

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

        Context.getPatientService().savePatient(patient);
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

    private static Patient saveQuarantineFollowupReports(Patient patient, Date admissionDate, ArrayNode reports) {


       if (reports.size() < 1) {
           return patient;
       }

       for (int i=0; i < reports.size(); i++) {
           ObjectNode report = (ObjectNode) reports.get(i);

           String followupDate = report.get("FOLLOWUP_DATE").textValue();
           Integer sequenceNumber = report.get("DAY_OF_FOLLOWUP").intValue();
           Double temp = report.get("TEMPERATURE").doubleValue();
           String cough = report.get("COUGH").textValue();
           String fever = report.get("FEVER").textValue();
           String difficultyBreathing = report.get("DIFFICULTY_BREATHING").textValue();
           String soreThroat = report.get("SORE_THROAT").textValue();
           String comment = report.get("COMMENT").textValue();


       }

        Encounter enc = new Encounter();
        enc.setEncounterType(Context.getEncounterService().getEncounterTypeByUuid(COVID_QUARANTINE_FOLLOWUP_ENCOUNTER));
        enc.setEncounterDatetime(admissionDate);
        enc.setPatient(patient);
        enc.addProvider(Context.getEncounterService().getEncounterRole(1), Context.getProviderService().getProvider(1));
        enc.setForm(Context.getFormService().getFormByUuid(COVID_QUARANTINE_FOLLOWUP_FORM));


        // set quarantine center
        ConceptService conceptService = Context.getConceptService();
        Obs o = new Obs();
        o.setConcept(conceptService.getConcept("162724"));
        o.setDateCreated(new Date());
        o.setCreator(Context.getUserService().getUser(1));
        o.setLocation(enc.getLocation());
        o.setObsDatetime(admissionDate);
        o.setPerson(patient);
        //o.setValueText(quarantineCenter);

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

        Context.getPatientService().savePatient(patient);
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
     * processes results from mhealth     *
     * @param resultPayload this should be an array
     * @return
     */
    public static String processMhealthPayload(String resultPayload) {

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
            }
        }
        return "Results updated successfully";
    }

    public ArrayNode getContacts() {

        ArrayNode patientContactNode = OutgoingPatientSHR.getJsonNodeFactory().arrayNode();
        HTSService htsService = Context.getService(HTSService.class);
        PatientService patientService = Context.getPatientService();
        PersonService personService = Context.getPersonService();

        Set<Integer> listedContacts = getListedContacts();
        Set<Integer> quarantinedContacts = getContactsInQuarantineProgram();

        if (listedContacts != null && listedContacts.size() > 0) {
            JsonNodeFactory factory = OutgoingPatientSHR.getJsonNodeFactory();

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
            JsonNodeFactory factory = OutgoingPatientSHR.getJsonNodeFactory();

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

        return patientContactNode;

    }

    private static String getContactPhoneNumber(Patient patient, PersonService personService) {
        PersonAttributeType phoneNumberAttrType = personService.getPersonAttributeTypeByUuid(TELEPHONE_CONTACT);
        return patient.getAttribute(phoneNumberAttrType) != null ? patient.getAttribute(phoneNumberAttrType).getValue() : "";
    }
    protected Set<Integer> getListedContacts() {

        Set<Integer> eligibleList = new HashSet<Integer>();
        GlobalProperty lastContactEntry = Context.getAdministrationService().getGlobalPropertyObject(HTSMetadata.MHEALTH_LAST_PATIENT_CONTACT_ENTRY);
        String lastOrdersql = "select max(id) last_id from kenyaemr_hiv_testing_patient_contact where voided=0;";
        List<List<Object>> lastOrderId = Context.getAdministrationService().executeSQL(lastOrdersql, true);
        Integer lastId = (Integer) lastOrderId.get(0).get(0);
        lastId = lastId != null ? lastId : 0;
        String sql = "";
        if (lastContactEntry != null) {
            Integer lastEntry = Integer.parseInt(lastContactEntry.getValue().toString());
            sql = "select id from kenyaemr_hiv_testing_patient_contact where id >" + lastEntry + " and patient_id is null and voided=0;"; // get contacts not registered
        } else {
            lastContactEntry = new GlobalProperty();
            lastContactEntry.setProperty(HTSMetadata.MHEALTH_LAST_PATIENT_CONTACT_ENTRY);
            lastContactEntry.setDescription("Id for the last case contact entry ");
            sql = "select id from kenyaemr_hiv_testing_patient_contact where id <= " + lastId + " and patient_id is null and voided=0;";

        }
        lastContactEntry.setPropertyValue(lastId.toString());

        List<List<Object>> activeList = Context.getAdministrationService().executeSQL(sql, true);
        if (!activeList.isEmpty()) {
            for (List<Object> res : activeList) {
                Integer patientId = (Integer) res.get(0);
                eligibleList.add(patientId);
            }
        }

        Context.getAdministrationService().saveGlobalProperty(lastContactEntry);
        return eligibleList;
    }

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

    private static PatientIdentifier generateOpenMRSID() {
        PatientIdentifierType openmrsIDType = Context.getPatientService().getPatientIdentifierTypeByUuid("dfacd928-0370-4315-99d7-6ec1c9f7ae76");
        String generated = Context.getService(IdentifierSourceService.class).generateIdentifier(openmrsIDType, "Registration");
        PatientIdentifier identifier = new PatientIdentifier(generated, openmrsIDType, getDefaultLocation());
        return identifier;
    }

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
