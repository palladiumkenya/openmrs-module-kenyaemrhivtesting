package org.openmrs.module.hivtestingservices.api.shr;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.JSONPObject;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.hivtestingservices.api.HTSService;
import org.openmrs.module.hivtestingservices.api.PatientContact;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OutgoingPatientSHR {

    private Integer patientID;
    private Patient patient;
    private PersonService personService;
    private PatientService patientService;
/*    private ObsService obsService;
    private ConceptService conceptService;
    private AdministrationService administrationService;
    private EncounterService encounterService;*/
    private String patientIdentifier;

    String TELEPHONE_CONTACT = "b2c38640-2603-4629-aebd-3b54f33f1e3a";
    String HEI_UNIQUE_NUMBER = "0691f522-dd67-4eeb-92c8-af5083baf338";
    String NATIONAL_ID = "49af6cdc-7968-4abb-bf46-de10d7f4859f";
    String UNIQUE_PATIENT_NUMBER = "05ee9cf4-7242-4a17-b4d4-00f707265c8a";


    public OutgoingPatientSHR() {
    }

    public OutgoingPatientSHR(Integer patientID) {
        this.patientID = patientID;
        this.patientService = Context.getPatientService();
        this.patient = patientService.getPatient(patientID);
        this.personService = Context.getPersonService();

        /*this.obsService = Context.getObsService();
        this.administrationService = Context.getAdministrationService();
        this.conceptService = Context.getConceptService();
        this.encounterService = Context.getEncounterService();*/

    }

    public OutgoingPatientSHR(String patientIdentifier) {
        this.patientIdentifier = patientIdentifier;
        this.patientService = Context.getPatientService();
        this.personService = Context.getPersonService();
        /*this.obsService = Context.getObsService();
        this.administrationService = Context.getAdministrationService();
        this.conceptService = Context.getConceptService();
        this.encounterService = Context.getEncounterService();*/
        setPatientUsingIdentifier();
    }

    private JsonNodeFactory getJsonNodeFactory() {
        final JsonNodeFactory factory = JsonNodeFactory.instance;
        return factory;
    }

    private ObjectNode getPatientName() {
        PersonName pn = patient.getPersonName();
        ObjectNode nameNode = getJsonNodeFactory().objectNode();
        nameNode.put("FIRST_NAME", pn.getGivenName());
        nameNode.put("MIDDLE_NAME", pn.getMiddleName());
        nameNode.put("LAST_NAME", pn.getFamilyName());
        return nameNode;
    }

    private String getSHRDateFormat() {
        return "yyyyMMdd";
    }

    private SimpleDateFormat getSimpleDateFormat(String pattern) {
        return new SimpleDateFormat(pattern);
    }

    private String getPatientPhoneNumber() {
        PersonAttributeType phoneNumberAttrType = personService.getPersonAttributeTypeByUuid(TELEPHONE_CONTACT);
        return patient.getAttribute(phoneNumberAttrType) != null ? patient.getAttribute(phoneNumberAttrType).getValue() : "";
    }

    public void setPatientUsingIdentifier() {

        if (patientIdentifier != null) {
            PatientIdentifierType HEI_NUMBER_TYPE = patientService.getPatientIdentifierTypeByUuid(HEI_UNIQUE_NUMBER);
            PatientIdentifierType CCC_NUMBER_TYPE = patientService.getPatientIdentifierTypeByUuid(UNIQUE_PATIENT_NUMBER);
            PatientIdentifierType NATIONAL_ID_TYPE = patientService.getPatientIdentifierTypeByUuid(NATIONAL_ID);

            List<Patient> patientsListWithIdentifier = patientService.getPatients(null, patientIdentifier.trim(),
                    Arrays.asList(HEI_NUMBER_TYPE, CCC_NUMBER_TYPE, NATIONAL_ID_TYPE), false);
            if (patientsListWithIdentifier.size() > 0) {
                this.patient = patientsListWithIdentifier.get(0);
            }

        }
    }

    private ObjectNode getPatientAddress() {

        /**
         * county: personAddress.country
         * sub-county: personAddress.stateProvince
         * ward: personAddress.address4
         * landmark: personAddress.address2
         * postal address: personAddress.address1
         */

        Set<PersonAddress> addresses = patient.getAddresses();
        //patient address
        ObjectNode patientAddressNode = getJsonNodeFactory().objectNode();
        ObjectNode physicalAddressNode = getJsonNodeFactory().objectNode();
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


    public ObjectNode patientIdentification() {

        JsonNodeFactory factory = getJsonNodeFactory();
        ObjectNode patientSHR = factory.objectNode();
        if (patient != null) {

            /*PatientIdentifierType NATIONAL_ID_TYPE = patientService.getPatientIdentifierTypeByUuid(SHRConstants.NATIONAL_ID);
            PatientIdentifierType ALIEN_NUMBER_TYPE = patientService.getPatientIdentifierTypeByUuid(SHRConstants.ALIEN_NUMBER);
            PatientIdentifierType PASSPORT_NUMBER_TYPE = patientService.getPatientIdentifierTypeByUuid(SHRConstants.PASSPORT_NUMBER);

            List<PatientIdentifier> identifierList = patientService.getPatientIdentifiers(null, Arrays.asList(NATIONAL_ID_TYPE, NATIONAL_ID_TYPE, ALIEN_NUMBER_TYPE, PASSPORT_NUMBER_TYPE), null, Arrays.asList(patient), null);


            Map<String, String> patientIdentifiers = new HashMap<String, String>();

            ObjectNode patientIdentificationNode = factory.objectNode();
            ArrayNode internalIdentifiers = factory.arrayNode();
            ObjectNode externalIdentifiers = factory.objectNode();

            for (PatientIdentifier identifier : identifierList) {
                PatientIdentifierType identifierType = identifier.getIdentifierType();

                ObjectNode element = factory.objectNode();
                if (identifierType.equals(NATIONAL_ID_TYPE)) {
                    patientIdentifiers.put("NATIONAL_ID", identifier.getIdentifier());
                    element.put("ID", identifier.getIdentifier());
                    element.put("IDENTIFIER_TYPE", "NATIONAL_ID");
                } else if (identifierType.equals(ALIEN_NUMBER_TYPE)) {
                    patientIdentifiers.put("ALIEN_NUMBER", identifier.getIdentifier());
                    element.put("ID", identifier.getIdentifier());
                    element.put("IDENTIFIER_TYPE", "ALIEN_NUMBER");

                } else if (identifierType.equals(PASSPORT_NUMBER_TYPE)) {
                    patientIdentifiers.put("PASSPORT_NUMBER", identifier.getIdentifier());
                    element.put("ID", identifier.getIdentifier());
                    element.put("IDENTIFIER_TYPE", "PASSPORT_NUMBER");
                }
                if (!element.isEmpty(null)) {
                    internalIdentifiers.add(element);
                }
            }

            // get other patient details

            String dob = getSimpleDateFormat(getSHRDateFormat()).format(this.patient.getBirthdate());
            String dobPrecision = patient.getBirthdateEstimated() ? "ESTIMATED" : "EXACT";
            String sex = patient.getGender();



            patientIdentificationNode.put("INTERNAL_PATIENT_ID", internalIdentifiers);
            patientIdentificationNode.put("PATIENT_NAME", getPatientName());
            patientIdentificationNode.put("DATE_OF_BIRTH", dob);
            patientIdentificationNode.put("DATE_OF_BIRTH_PRECISION", dobPrecision);
            patientIdentificationNode.put("SEX", sex);
            patientIdentificationNode.put("PATIENT_ADDRESS", getPatientAddress());
            patientIdentificationNode.put("PHONE_NUMBER", getPatientPhoneNumber());
            patientSHR.put("VERSION", "1.0.0");
            patientSHR.put("PATIENT_IDENTIFICATION", patientIdentificationNode);
            patientSHR.put("PATIENT_CONTACTS", getPatientContacts());*/

            /*HTSService htsService = Context.getService(HTSService.class);
            List<PatientContact> patientContacts = htsService.getPatientContactByPatient(this.patient);

            PatientContact pc = null;
            if (patientContacts != null && patientContacts.size() > 0) {
                pc = patientContacts.get(0);
                patientSHR = buildContactPayload(pc);
            }*/

            return patientSHR;
        } else {
            return patientSHR;
        }
    }

    public ArrayNode getContactListCht() {

        JsonNodeFactory factory = getJsonNodeFactory();
        ArrayNode patientSHR = factory.arrayNode();
        if (patient != null) {
            patientSHR = buildContactPayload();
            return patientSHR;
        } else {
            return patientSHR;
        }
    }

    private ArrayNode getPatientContacts() {

        ArrayNode patientContactNode = getJsonNodeFactory().arrayNode();
        HTSService htsService = Context.getService(HTSService.class);
        List<PatientContact> patientContacts = htsService.getPatientContactByPatient(this.patient);

        if (patientContacts != null && patientContacts.size() > 0) {
            JsonNodeFactory factory = getJsonNodeFactory();

            for (PatientContact c : patientContacts) {
                ObjectNode contact = factory.objectNode();
                contact.put("CONTACT_UUID", c.getUuid());
                contact.put("FIRST_NAME", c.getFirstName() != null ? c.getFirstName() : "");
                contact.put("MIDDLE_NAME", c.getMiddleName() != null ? c.getMiddleName() : "");
                contact.put("LAST_NAME", c.getLastName() != null ? c.getLastName() : "");
                contact.put("DATE_OF_BIRTH", c.getBirthDate() != null ? getSimpleDateFormat(getSHRDateFormat()).format(c.getBirthDate()) : "");
                contact.put("SEX", c.getSex() != null ? c.getSex() : "");
                contact.put("PHYSICAL_ADDRESS", c.getPhysicalAddress() != null ? c.getPhysicalAddress() : "");
                contact.put("PHONE_NUMBER", c.getPhoneContact() != null ? c.getPhoneContact() : "");
                patientContactNode.add(contact);

            }
        }

        return patientContactNode;

    }

    public ArrayNode getMotherIdentifiers(Patient patient) {

        PatientIdentifierType NATIONAL_ID_TYPE = patientService.getPatientIdentifierTypeByUuid(SHRConstants.NATIONAL_ID);
        PatientIdentifierType ALIEN_NUMBER_TYPE = patientService.getPatientIdentifierTypeByUuid(SHRConstants.ALIEN_NUMBER);
        PatientIdentifierType PASSPORT_NUMBER_TYPE = patientService.getPatientIdentifierTypeByUuid(SHRConstants.PASSPORT_NUMBER);

        List<PatientIdentifier> identifierList = patientService.getPatientIdentifiers(null, Arrays.asList(NATIONAL_ID_TYPE, NATIONAL_ID_TYPE, ALIEN_NUMBER_TYPE, PASSPORT_NUMBER_TYPE), null, Arrays.asList(patient), null);
        Map<String, String> patientIdentifiers = new HashMap<String, String>();

        JsonNodeFactory factory = getJsonNodeFactory();
        ArrayNode internalIdentifiers = factory.arrayNode();

        for (PatientIdentifier identifier : identifierList) {
            PatientIdentifierType identifierType = identifier.getIdentifierType();
            ObjectNode element = factory.objectNode();

            if (identifierType.equals(NATIONAL_ID_TYPE)) {
                patientIdentifiers.put("NATIONAL_ID", identifier.getIdentifier());
                element.put("ID", identifier.getIdentifier());
                element.put("IDENTIFIER_TYPE", "NATIONAL_ID");
            } else if (identifierType.equals(ALIEN_NUMBER_TYPE)) {
                patientIdentifiers.put("ALIEN_NUMBER", identifier.getIdentifier());
                element.put("ID", identifier.getIdentifier());
                element.put("IDENTIFIER_TYPE", "ALIEN_NUMBER");

            } else if (identifierType.equals(PASSPORT_NUMBER_TYPE)) {
                patientIdentifiers.put("PASSPORT_NUMBER", identifier.getIdentifier());
                element.put("ID", identifier.getIdentifier());
                element.put("IDENTIFIER_TYPE", "PASSPORT_NUMBER");
            }

            internalIdentifiers.add(element);

        }

        return internalIdentifiers;
    }

    private ObjectNode getMotherDetails() {

        // get relationships
        // mother name
        String motherName = "";
        ObjectNode mothersNameNode = getJsonNodeFactory().objectNode();
        ObjectNode motherDetails = getJsonNodeFactory().objectNode();
        ArrayNode motherIdenfierNode = getJsonNodeFactory().arrayNode();
        RelationshipType type = getParentChildType();

        List<Relationship> parentChildRel = personService.getRelationships(null, patient, getParentChildType());
        if (parentChildRel.isEmpty() && parentChildRel.size() == 0) {
            // try getting this from person attribute
            if (patient.getAttribute(4) != null) {
                motherName = patient.getAttribute(4).getValue();
                mothersNameNode.put("FIRST_NAME", motherName);
                mothersNameNode.put("MIDDLE_NAME", "");
                mothersNameNode.put("LAST_NAME", "");
            } else {
                mothersNameNode.put("FIRST_NAME", "");
                mothersNameNode.put("MIDDLE_NAME", "");
                mothersNameNode.put("LAST_NAME", "");
            }

        }

        // check if it is mothers
        Person mother = null;
        // a_is_to_b = 'Parent' and b_is_to_a = 'Child'
        for (Relationship relationship : parentChildRel) {

            if (patient.equals(relationship.getPersonB())) {
                if (relationship.getPersonA().getGender().equals("F")) {
                    mother = relationship.getPersonA();
                    break;
                }
            } else if (patient.equals(relationship.getPersonA())) {
                if (relationship.getPersonB().getGender().equals("F")) {
                    mother = relationship.getPersonB();
                    break;
                }
            }
        }
        if (mother != null) {
            //get mother name
            mothersNameNode.put("FIRST_NAME", mother.getGivenName());
            mothersNameNode.put("MIDDLE_NAME", mother.getMiddleName());
            mothersNameNode.put("LAST_NAME", mother.getFamilyName());

            // get identifiers
            motherIdenfierNode = getMotherIdentifiers(patientService.getPatient(mother.getPersonId()));
        }

        motherDetails.put("MOTHER_NAME", mothersNameNode);
        motherDetails.put("MOTHER_IDENTIFIER", motherIdenfierNode);

        return motherDetails;
    }

    protected RelationshipType getParentChildType() {
        return personService.getRelationshipTypeByUuid("8d91a210-c2cc-11de-8d13-0010c6dffd0f");

    }

    private ArrayNode buildContactPayload() {

        ArrayNode patientContactNode = getJsonNodeFactory().arrayNode();
        HTSService htsService = Context.getService(HTSService.class);
        List<PatientContact> patientContacts = htsService.getPatientContactByPatient(this.patient);

        if (patientContacts != null && patientContacts.size() > 0) {

            for (PatientContact c : patientContacts) {
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
                ObjectNode contact = getJsonNodeFactory().objectNode();
                ObjectNode parentNode = getJsonNodeFactory().objectNode();
                parentNode.put("_id", "a452eebc-00a3-4c03-bc2b-43df627bf0f1");

                contact.put("_id", c.getUuid());
                contact.put("parent", parentNode);
                contact.put("given_names", givenNames);
                contact.put("role", "covid_contact");
                contact.put("name", fullName);
                contact.put("country_of_residence", "Kenya");
                contact.put("date_of_birth", c.getBirthDate() != null ? getSimpleDateFormat(dateFormat).format(c.getBirthDate()) : "");
                contact.put("sex", sex);
                contact.put("primary_phone", c.getPhoneContact() != null ? c.getPhoneContact() : "");
                contact.put("alternate_phone", "");
                contact.put("email", "");
                contact.put("type", "person");
                contact.put("reported_date", c.getDateCreated().getTime());
                contact.put("patient_id", c.getPatientRelatedTo().getPatientId().toString());
                contact.put("phone", "");// this could be patient phone
                contact.put("date_of_last_contact", c.getAppointmentDate() != null ? getSimpleDateFormat(dateFormat).format(c.getAppointmentDate()) : "");
                contact.put("outbreak_case_id", "1X000");
                contact.put("relation_to_case", c.getRelationType() != null ? getContactRelation().get(c.getRelationType()) : "");
                contact.put("type_of_contact", c.getPnsApproach() != null ? getContactType().get(c.getPnsApproach()) : "");
                contact.put("household_head", c.getLivingWithPatient() != null && c.getLivingWithPatient().equals(1065) ? givenNames : "");
                contact.put("subcounty", c.getSubcounty() != null ? c.getSubcounty() : "");
                contact.put("town", c.getTown() != null ? c.getTown() : "");
                contact.put("address", c.getPhysicalAddress() != null ? c.getPhysicalAddress() : "");
                contact.put("healthcare_worker", c.getMaritalStatus() != null && c.getMaritalStatus().equals(1065) ? "true" : "false");
                contact.put("facility", c.getFacility() != null ? c.getFacility() : "");
                patientContactNode.add(contact);

            }
        }

        return patientContactNode;

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

    private JSONPObject getPatientIdentifiers() {
        return null;
    }

    private JSONPObject getHivTestDetails() {
        return null;
    }

    private JSONPObject getImmunizationDetails() {
        return null;
    }

    public int getPatientID() {
        return patientID;
    }

    public void setPatientID(int patientID) {
        this.patientID = patientID;
    }

    public String getPatientIdentifier() {
        return patientIdentifier;
    }

    public void setPatientIdentifier(String patientIdentifier) {
        this.patientIdentifier = patientIdentifier;
    }


}
