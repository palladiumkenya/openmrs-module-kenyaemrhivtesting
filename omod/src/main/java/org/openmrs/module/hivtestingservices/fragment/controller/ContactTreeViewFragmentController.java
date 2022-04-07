package org.openmrs.module.hivtestingservices.fragment.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.hivtestingservices.api.ContactTrace;
import org.openmrs.module.hivtestingservices.api.HTSService;
import org.openmrs.module.hivtestingservices.api.PatientContact;
import org.openmrs.module.kenyaui.KenyaUiUtils;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ContactTreeViewFragmentController {

    protected static final Log log = LogFactory.getLog(ContactTreeViewFragmentController.class);
    PatientService patientService = Context.getPatientService();
    ConceptService conceptService = Context.getConceptService();
    HTSService htsService = Context.getService(HTSService.class);
    SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    public static final String UNIQUE_PATIENT_NUMBER = "05ee9cf4-7242-4a17-b4d4-00f707265c8a";

    public void controller(@SpringBean KenyaUiUtils kenyaUi, @RequestParam(value = "patientId") Patient patient,
                           FragmentModel model, UiUtils ui) {

        ObjectNode patientNode = getJsonNodeFactory().objectNode();
        ArrayNode patientContacts = getJsonNodeFactory().arrayNode();
        String patientName = patient.getPersonName().toString();
        String patientGender = patient.getGender();
        String iconPathStr = null;

        if (patient.getAge().intValue() <= 5) {
            iconPathStr =  "images/baby_green_person_" ;
        } else if (patient.getAge().intValue() < 15){
            iconPathStr =  "images/youth_green_person_";
        } else {
            iconPathStr =  "images/green_person_";
        }



        String plinkIcon = ui.resourceLink("hivtestingservices", iconPathStr + patientGender.toLowerCase() + ".png");

        patientNode.put("image", plinkIcon);
        ObjectNode pTextNode = getJsonNodeFactory().objectNode();
        pTextNode.put("name", patientName);
        pTextNode.put("title", patient.getAge().toString().concat("Yrs"));
        patientNode.put("text", pTextNode);

        Set<Integer> exludes = new HashSet<Integer>(); // list of persons to be exluded from contacts list. initialize the list with index client
        exludes.add(patient.getPatientId());


        for (Relationship relationship : Context.getPersonService().getRelationshipsByPerson(patient)) {

            // Filter only direct relationships
            if(!getRelationshipTypes().contains(relationship.getRelationshipType())) {
                continue;
            }
            ObjectNode relNode = getJsonNodeFactory().objectNode();
            Person person = null;
            String type = null;
            String age = null;
            PatientIdentifier UPN = null;
            Boolean alive = null;
            Boolean enrolled = false;

            if (patient.equals(relationship.getPersonA())) {
                person = relationship.getPersonB();
                type = relationship.getRelationshipType().getbIsToA();

            }
            else if (patient.equals(relationship.getPersonB())) {
                person = relationship.getPersonA();
                type = relationship.getRelationshipType().getaIsToB();
            }

            String genderCode = person.getGender().toLowerCase();
            String linkUrl, linkIcon;
            age = new StringBuilder().append(type).append(", ").append(person.getAge()).append(" Yrs").toString();
            PatientIdentifierType pit = patientService.getPatientIdentifierTypeByUuid(UNIQUE_PATIENT_NUMBER);
            List<PatientIdentifier> identifierList = patientService.getPatientIdentifiers(null, Arrays.asList(pit), null, Arrays.asList(patientService.getPatient(person.getId())),null);
            if (identifierList.size() > 0) {
                UPN = identifierList.get(0);
            }

            alive = person.isDead();
            String relIconPathString = null;


            if (person.isPatient()) {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("patientId", person.getId());
                linkIcon = ui.resourceLink("hivtestingservices", getPersonIconForAge(person.getAge()) + genderCode + ".png");
            }
            else {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("personId", person.getId());
                linkIcon = ui.resourceLink("hivtestingservices", getPersonIconForAge(person.getAge()) + genderCode + ".png");
            }


            if(UPN != null) { // for patient enrolled in HIV
                enrolled = true;
            }

            exludes.add(person.getPersonId());
            relNode.put("image", linkIcon);
            ObjectNode textNode = getJsonNodeFactory().objectNode();
            textNode.put("name", person.getPersonName().toString());
            textNode.put("title", age.concat(", Status: ").concat((enrolled) ? "In Care" : "Not Enrolled"));
            //textNode.put("desc", (enrolled) ? "In Care" : "Not Enrolled");
            relNode.put("text", textNode);


            // person - relationship person, patient - index client
            Set<Integer> indexClient = new HashSet<Integer>();
            indexClient.add(patient.getPatientId());
            ArrayNode relChildren = getRelationshipContacts(patientService.getPatient(person.getPersonId()), indexClient, ui);
            ArrayNode relContacts = getListedContactsContacts(patientService.getPatient(person.getPersonId()), getAllRelationshipsForPatient(patientService.getPatient(person.getPersonId())), ui);
            if (relContacts.size() > 0) {
                relChildren.addAll(relContacts);
            }
            if (relChildren.size() > 0)
                relNode.put("children", relChildren);
            patientContacts.add(relNode);

        }

        // -- get listed contacts
        ArrayNode listedContacts = getListedContactsContacts(patient, exludes, ui);
        patientContacts.addAll(listedContacts);
        patientNode.put("children", patientContacts);
        model.put("sresponse", patientNode.toString());


    }

    protected ArrayNode getRelationshipContacts(Patient patient, Set<Integer> excludes, UiUtils ui) {

        ArrayNode patientContacts = getJsonNodeFactory().arrayNode();
        for (Relationship relationship : Context.getPersonService().getRelationshipsByPerson(patient)) {

            // Filter only direct relationships
            if(!getRelationshipTypes().contains(relationship.getRelationshipType())) {
                continue;
            }
            ObjectNode relNode = getJsonNodeFactory().objectNode();
            Person person = null;
            Patient personPatient = null;
            String type = null;
            String age = null;
            PatientIdentifier UPN = null;
            Boolean alive = null;
            Boolean enrolled = false;

            if (patient.equals(relationship.getPersonA())) {
                person = relationship.getPersonB();
                type = relationship.getRelationshipType().getbIsToA();

            }
            else if (patient.equals(relationship.getPersonB())) {
                person = relationship.getPersonA();
                type = relationship.getRelationshipType().getaIsToB();
            }

            if (excludes.contains(person.getPersonId())) {
                continue;
            }

            personPatient = patientService.getPatient(person.getPersonId());
            String genderCode = person.getGender().toLowerCase();
            String linkUrl, linkIcon;
            age = new StringBuilder().append(type).append(", ").append(person.getAge()).append(" Yrs").toString();
            PatientIdentifierType pit = patientService.getPatientIdentifierTypeByUuid(UNIQUE_PATIENT_NUMBER);
            List<PatientIdentifier> identifierList = patientService.getPatientIdentifiers(null, Arrays.asList(pit), null, Arrays.asList(personPatient),null);
            if (identifierList.size() > 0) {
                UPN = identifierList.get(0);
            }

            alive = person.isDead();


            if (person.isPatient()) {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("patientId", person.getId());
                if (contactEnrolledInHivProgram(personPatient)) {
                    linkIcon = ui.resourceLink("hivtestingservices", getPersonIconForAge(person.getAge()) + personPatient.getGender().toLowerCase() + ".png");
                } else {
                    linkIcon = ui.resourceLink("hivtestingservices", getPersonIconForAge(person.getAge()) + personPatient.getGender().toLowerCase() + ".png");
                }
            }
            else {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("personId", person.getId());
                linkIcon = ui.resourceLink("hivtestingservices", getPersonIconForAge(person.getAge()) + genderCode + ".png");
            }


            if(UPN != null) { // for patient enrolled in HIV
                enrolled = true;
            }

            relNode.put("image", linkIcon);
            ObjectNode textNode = getJsonNodeFactory().objectNode();
            textNode.put("name", person.getPersonName().toString());
            textNode.put("title", age.concat(", Status: ").concat((enrolled) ? "In Care" : "Not Enrolled"));
            //textNode.put("desc", enrolled ? "In Care" : "Not Enrolled");
            relNode.put("text", textNode);
            patientContacts.add(relNode);

        }
        return patientContacts;
    }

    /**
     *
     * @param patient - the current relationship end being processed
     * @param excludes those already counted under openmrs relationship
     * @param ui
     * @return
     */
    protected ArrayNode getListedContactsContacts(Patient patient, Set<Integer> excludes, UiUtils ui ) {

        ArrayNode patientContacts = getJsonNodeFactory().arrayNode();

        for (PatientContact patientContact : htsService.getPatientContactByPatient(patient)) {// returns a list of contacts listed under a patient


            Patient patientFromContact = patientContact.getPatient();
            if (excludes.size() > 0 && patientFromContact != null && excludes.contains(patientFromContact.getPatientId())) {
                continue;
            }
            String fullName = "";
            ArrayNode childrenOfListedContacts = getJsonNodeFactory().arrayNode();

            ObjectNode relNode = getJsonNodeFactory().objectNode();
            String type = formatRelationshipType(patientContact.getRelationType());
            String age = "";
            String status = "";


            String linkIcon;



            if (patientFromContact != null) {

                fullName = patientFromContact.getPersonName().toString();
                age = new StringBuilder().append(type).append(", ").append(patientFromContact.getAge()).append(" Yrs").toString();
                if (contactEnrolledInHivProgram(patient)) {
                    linkIcon = ui.resourceLink("hivtestingservices", getPersonIconForAge(patientFromContact.getAge()) + patientFromContact.getGender().toLowerCase() + ".png");
                    status = "In Care";
                } else {
                    linkIcon = ui.resourceLink("hivtestingservices", getPersonIconForAge(patientFromContact.getAge()) + patientFromContact.getGender().toLowerCase() + ".png");
                    status = "Not Enrolled";
                }

                ArrayNode relChildren = getRelationshipContacts(patientFromContact, new HashSet<Integer>(), ui);
                ArrayNode relContacts = getListedContactsContacts(patientFromContact, getAllRelationshipsForPatient(patientFromContact), ui);

                if (relChildren.size() > 0)
                    childrenOfListedContacts.addAll(relChildren);
                if (relContacts.size() > 0)
                    childrenOfListedContacts.addAll(relContacts);

            }
            else {
                //params.put("personId", person.getId());
                String cage = patientContact.getBirthDate() != null ? calculateContactAge(patientContact.getBirthDate(), new Date()).toString() : "" ;
                age = new StringBuilder().append(type).append(", ").append(cage).append(" Yrs").toString();
                status = patientContact.getBaselineHivStatus();

                Integer ageOfContact = null;
                if (patientContact.getBirthDate() != null) {
                    ageOfContact = calculateContactAge(patientContact.getBirthDate(), new Date());
                }

                if (ageOfContact != null) {
                    linkIcon = ui.resourceLink("hivtestingservices", getPersonIconForAge(ageOfContact) + patientContact.getSex().toLowerCase() + ".png");

                } else {//patient.getAge().intValue()
                    linkIcon = ui.resourceLink("hivtestingservices", "images/grey_person_" + patientContact.getSex().toLowerCase() + ".png");

                }
                if(patientContact.getFirstName() != null) {
                    fullName+=patientContact.getFirstName();
                }

                if(patientContact.getMiddleName() != null) {
                    fullName+= " " + patientContact.getMiddleName();
                }

                if(patientContact.getLastName() != null) {
                    fullName+= " " + patientContact.getLastName();
                }
            }




            relNode.put("image", linkIcon);
            ObjectNode textNode = getJsonNodeFactory().objectNode();
            textNode.put("name", fullName);
            textNode.put("title", age.concat(", Status: ").concat(status !=null? status : "Uknown"));
            //textNode.put("desc", status);
            relNode.put("text", textNode);
            if (childrenOfListedContacts.size() > 0) {
                relNode.put("children", childrenOfListedContacts);
            }
            patientContacts.add(relNode);

        }
        return patientContacts;
    }

    protected Set<Integer> getAllRelationshipsForPatient(Patient patient) {
        Set<Integer> allRelatedPersons = new HashSet<Integer>();
        for (Relationship relationship : Context.getPersonService().getRelationshipsByPerson(patient)) {
            if (patient.equals(relationship.getPersonA())) {
                allRelatedPersons.add(relationship.getPersonB().getPersonId());

            }
            else if (patient.equals(relationship.getPersonB())) {
                allRelatedPersons.add(relationship.getPersonA().getPersonId());
            }
        }
        return allRelatedPersons;
    }

    protected boolean contactEnrolledInHivProgram(Patient patient) {
        PatientIdentifierType pit = patientService.getPatientIdentifierTypeByUuid(UNIQUE_PATIENT_NUMBER);
        PatientIdentifier UPN = null;
        List<PatientIdentifier> identifierList = patientService.getPatientIdentifiers(null, Arrays.asList(pit), null, Arrays.asList(patient),null);
        if (identifierList.size() > 0) {
            UPN = identifierList.get(0);
        }
        return UPN != null;

    }

    protected String getGenderIconForRelationships(boolean status, int age) {

        if (age <= 5) {
            if (status) {
                return "images/baby_green_person_" ;
            } else {
                return "images/baby_grey_person_";
            }

        }
        if (status) {
            return "images/green_person_" ;
        } else {
            return "images/grey_person_";
        }
    }

    protected String getGenderIconForContacts(String status, int age) {
        if (age <= 5) {
            if (status.equals("Uknown") || status.equals("Positive")) {
                return "images/baby_red_person_" ;
            } else if (status.equals("Negative")){
                return "images/baby_green_person_";
            } else {
                return "images/baby_grey_person_";
            }

        }

        if (status.equals("Uknown") || status.equals("Positive")) {
            return "images/red_person_" ;
        } else if (status.equals("Negative")){
            return "images/green_person_";
        } else {
            return "images/grey_person_";
        }
    }

    protected List<RelationshipType> getRelationshipTypes () {
        List<RelationshipType> directRelationships = Arrays.asList(
                Context.getPersonService().getRelationshipTypeByUuid("8d91a01c-c2cc-11de-8d13-0010c6dffd0f"), // sibling
                Context.getPersonService().getRelationshipTypeByUuid("8d91a210-c2cc-11de-8d13-0010c6dffd0f"), // parent-child
                Context.getPersonService().getRelationshipTypeByUuid("d6895098-5d8d-11e3-94ee-b35a4132a5e3"), // spouse
                Context.getPersonService().getRelationshipTypeByUuid("007b765f-6725-4ae9-afee-9966302bace4"), // partner
                Context.getPersonService().getRelationshipTypeByUuid("2ac0d501-eadc-4624-b982-563c70035d46") // co-wife
        );
        return directRelationships;
    }

    String relationshipConverter (Concept key) {
        Map<Concept, String> relationshipList = new HashMap<Concept, String>();
        relationshipList.put(conceptService.getConcept(970), "Mother");
        relationshipList.put(conceptService.getConcept(971), "Father");
        relationshipList.put(conceptService.getConcept(972), "Sibling");
        relationshipList.put(conceptService.getConcept(1528), "Child");
        relationshipList.put(conceptService.getConcept(5617), "Spouse");
        relationshipList.put(conceptService.getConcept(163565), "Partner");
        relationshipList.put(conceptService.getConcept(162221), "Co-Wife");

        return relationshipList.get(key);
    }

    String statusConverter (Concept key) {
        Map<Concept, String> statusStatusList = new HashMap<Concept, String>();
        statusStatusList.put(conceptService.getConcept(159450), "Current");
        statusStatusList.put(conceptService.getConcept(160432), "Deceased");
        statusStatusList.put(conceptService.getConcept(1067), "Unknown");
        return statusStatusList.get(key);
    }

    String hivStatusConverter (Concept key) {
        Map<Concept, String> hivStatusList = new HashMap<Concept, String>();
        hivStatusList.put(conceptService.getConcept(703), "Positive");
        hivStatusList.put(conceptService.getConcept(664), "Negative");
        hivStatusList.put(conceptService.getConcept(1067), "Unknown");
        return hivStatusList.get(key);
    }

    private String formatRelationshipType(Integer typeId) {
        if (typeId == null) {
            return null;
        } else {
            return relationshipOptions().get(typeId);
        }
    }

    private Map<Integer, String> relationshipOptions () {
        Map<Integer, String> options = new HashMap<Integer, String>();
        options.put(970, "Mother");
        options.put(971, "Father");
        options.put(972, "Sibling");
        options.put(1528, "Child");
        options.put(5617, "Spouse");
        options.put(163565, "Partner");
        options.put(162221, "Co-wife");
        options.put(157351, "Injectable drug user");
        return options;
    }

    private Integer calculateContactAge(Date birthdate, Date onDate) {
        if (birthdate == null) {
            return null;
        }

        // Use default end date as today.
        Calendar today = Calendar.getInstance();
        // But if given, use the given date.
        if (onDate != null) {
            today.setTime(onDate);
        }


        Calendar bday = Calendar.getInstance();
        bday.setTime(birthdate);

        int age = today.get(Calendar.YEAR) - bday.get(Calendar.YEAR);

        // Adjust age when today's date is before the person's birthday
        int todaysMonth = today.get(Calendar.MONTH);
        int bdayMonth = bday.get(Calendar.MONTH);
        int todaysDay = today.get(Calendar.DAY_OF_MONTH);
        int bdayDay = bday.get(Calendar.DAY_OF_MONTH);

        if (todaysMonth < bdayMonth) {
            age--;
        } else if (todaysMonth == bdayMonth && todaysDay < bdayDay) {
            // we're only comparing on month and day, not minutes, etc
            age--;
        }

        return age;
    }

    private JsonNodeFactory getJsonNodeFactory() {
        final JsonNodeFactory factory = JsonNodeFactory.instance;
        return factory;
    }

    private String getPersonIconForAge(Integer age) {
        if (age == null) {
            return null;
        }
        if (age <=5) {
            return "images/baby_grey_person_";
        } else if (age < 15) {
            return "images/youth_black_person_";
        } else {
            return "images/grey_person_";
        }
    }
}



