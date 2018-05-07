/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 * <p>
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 * <p>
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.module.hivtestingservices.page.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.FormService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.hivtestingservices.api.ContactTrace;
import org.openmrs.module.hivtestingservices.api.HTSService;
import org.openmrs.module.hivtestingservices.api.PatientContact;
import org.openmrs.module.kenyaui.KenyaUiUtils;
import org.openmrs.ui.framework.Link;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.ui.framework.page.PageRequest;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class FamilyAndPartnerTestingPageController {

    protected static final Log log = LogFactory.getLog(FamilyAndPartnerTestingPageController.class);
    PatientService patientService = Context.getPatientService();
    EncounterService encounterService = Context.getEncounterService();
    SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    PersonService personService = Context.getPersonService();
    ObsService obsService = Context.getObsService();
    ConceptService conceptService = Context.getConceptService();

    String UNIQUE_PATIENT_NUMBER = "05ee9cf4-7242-4a17-b4d4-00f707265c8a";
    String HTS_ENCOUNTER_TYPE = "9c0a7a57-62ff-4f75-babe-5835b0e921b7";
    String HTS_INITIAL_TEST = "402dc5d7-46da-42d4-b2be-f43ea4ad87b0";
    String HTS_CONFIRMATORY_TEST = "b08471f6-0892-4bf7-ab2b-bf79797b8ea4";
    String REFERRAL_AND_LINKAGE = "050a7f12-5c52-4cad-8834-863695af335d";

    public void controller(@RequestParam(value = "patientId") Patient patient,
                           @RequestParam("returnUrl") String returnUrl,
                           @SpringBean KenyaUiUtils kenyaUi,
                           UiUtils ui,
                           PageRequest pageRequest,
                           PageModel model) {

        // Get all relationships as simple objects
        // patient id, name, sex, age, relation, test date, test result, enrolled, art number, initiated, status


        // list of direct relations
        /*	Mother, Father, Sibling, Child, Spouse, Partner, Co-Wife */
        List<SimpleObject> enrolledRelationships = new ArrayList<SimpleObject>();
        List<SimpleObject> registeredContacts = new ArrayList<SimpleObject>();
        List<SimpleObject> otherConctacts;
        Integer date_enrolled_in_care = 160555;
        Integer date_confirmed_positive = 160554;
        Integer hivFinalResultConcept = 159427;
        Integer facilityLinkedToConcept = 162724;
        Integer upnConcept = 162053;
        Integer contactStatusConcept = 159811;
        String HIV_ENROLLMENT_ENCOUNTER = "de78a6be-bfc5-4634-adc3-5f1a280455cc";
        HTSService htsService = Context.getService(HTSService.class);
        List<PatientContact> patientContacts = htsService.getPatientContactByPatient(patient);


        List<RelationshipType> directRelationships = Arrays.asList(
                Context.getPersonService().getRelationshipTypeByUuid("8d91a01c-c2cc-11de-8d13-0010c6dffd0f"), // sibling
                Context.getPersonService().getRelationshipTypeByUuid("8d91a210-c2cc-11de-8d13-0010c6dffd0f"), // parent-child
                Context.getPersonService().getRelationshipTypeByUuid("d6895098-5d8d-11e3-94ee-b35a4132a5e3"), // spouse
                Context.getPersonService().getRelationshipTypeByUuid("007b765f-6725-4ae9-afee-9966302bace4"), // partner
                Context.getPersonService().getRelationshipTypeByUuid("2ac0d501-eadc-4624-b982-563c70035d46") // co-wife
        );

        for (Relationship relationship : Context.getPersonService().getRelationshipsByPerson(patient)) {

            // Filter only direct relationships
            if (!directRelationships.contains(relationship.getRelationshipType())) {
                continue;
            }
            Person person = null;
            String type = null;
            String age = null;
            PatientIdentifier UPN = null;
            Boolean alive = null;

            if (patient.equals(relationship.getPersonA())) {
                person = relationship.getPersonB();
                type = relationship.getRelationshipType().getbIsToA();

            } else if (patient.equals(relationship.getPersonB())) {
                person = relationship.getPersonA();
                type = relationship.getRelationshipType().getaIsToB();
            }

            String genderCode = person.getGender().toLowerCase();
            String linkUrl, linkIcon;
            age = new StringBuilder().append(person.getAge()).append(" Years").toString();
            PatientIdentifierType pit = patientService.getPatientIdentifierTypeByUuid(UNIQUE_PATIENT_NUMBER);
            List<PatientIdentifier> identifierList = patientService.getPatientIdentifiers(null, Arrays.asList(pit), null, Arrays.asList(patientService.getPatient(person.getId())), null);
            if (identifierList.size() > 0) {
                UPN = identifierList.get(0);
            }

            alive = person.isDead();


            if (person.isPatient()) {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("patientId", person.getId());
                params.put("appId", "kenyaemr.medicalEncounter");
                params.put("returnUrl", returnUrl);
                linkUrl = ui.pageLink(pageRequest.getProviderName(), pageRequest.getPageName(), params);
                linkIcon = ui.resourceLink("kenyaui", "images/glyphs/patient_" + genderCode + ".png");
            } else {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("personId", person.getId());
                params.put("appId", "kenyaemr.medicalEncounter");
                params.put("returnUrl", returnUrl);
                linkUrl = ui.pageLink("kenyaemr", "admin/editAccount", params);
                linkIcon = ui.resourceLink("kenyaui", "images/glyphs/person_" + genderCode + ".png");
            }

            Link link = new Link(kenyaUi.formatPersonName(person), linkUrl, linkIcon);

            if (UPN != null) { // for patient enrolled in HIV
                String dateConfirmed = null;
                String dateEnrolled = null;
                Set<Obs> enrollmentObs = null;
                String first_encounter_date = null;

                Encounter initialEnrollment = firstEncounter(patientService.getPatient(person.getId()), encounterService.getEncounterTypeByUuid(HIV_ENROLLMENT_ENCOUNTER));

                // Extract last test information for negative patients
                if (initialEnrollment != null) {
                    enrollmentObs = initialEnrollment.getAllObs();

                    first_encounter_date = DATE_FORMAT.format(initialEnrollment.getEncounterDatetime());
                    for (Obs o : enrollmentObs) {
                        if (o.getConcept().getConceptId().equals(date_enrolled_in_care)) {
                            dateEnrolled = DATE_FORMAT.format(o.getValueDate());
                        } else if (o.getConcept().getConceptId().equals(date_confirmed_positive)) {
                            dateConfirmed = DATE_FORMAT.format(o.getValueDate());
                        }
                    }
                }
                enrolledRelationships.add(SimpleObject.create(
                        "relationshipId", relationship.getId(),
                        "type", type,
                        "personLink", link,
                        "age", age,
                        "dateEnrolled", dateEnrolled != null ? dateEnrolled : first_encounter_date,
                        "dateConfirmed", dateConfirmed != null ? dateConfirmed : first_encounter_date,
                        "status", alive ? "Dead" : "Alive",
                        "art_no", UPN != null ? UPN.toString() : ""
                ));
            } else { // this is for contacts registered but not enrolled in HIV program

                EncounterType hts_enc_type = encounterService.getEncounterTypeByUuid(HTS_ENCOUNTER_TYPE);
                Encounter contactLastEncounter = lastEncounter(patientService.getPatient(person.getId()), hts_enc_type);

                String lastTestDate = null;
                String lastTestResult = null;
                String linkageStatus = null;
                String upn = null;
                String facilityLinked = null;
                List<Obs> testObs = null;

                // Extract last test information for negative patients
                if (contactLastEncounter != null) {

                    //check which form
                    String encounterFormUuid = contactLastEncounter.getForm().getUuid();
                    lastTestDate = DATE_FORMAT.format(contactLastEncounter.getEncounterDatetime());

                    if (encounterFormUuid.equals(HTS_INITIAL_TEST) || encounterFormUuid.equals(HTS_CONFIRMATORY_TEST)) {

                        testObs = getObsForEncounter(Context.getPersonService().getPerson(contactLastEncounter.getPatient().getPersonId()), contactLastEncounter, Arrays.asList(conceptService.getConcept(hivFinalResultConcept)) );

                    } else if (encounterFormUuid.equals(REFERRAL_AND_LINKAGE)) {

                        List<Concept> referralConcepts = Arrays.asList(
                                conceptService.getConcept(contactStatusConcept),
                                conceptService.getConcept(upnConcept),
                                conceptService.getConcept(facilityLinkedToConcept)
                        );

                        Patient client = contactLastEncounter.getPatient();
                        testObs = getObsForEncounter(Context.getPersonService().getPerson(contactLastEncounter.getPatient().getPersonId()), contactLastEncounter, referralConcepts);

                        //get last hts form:
                        FormService formService = Context.getFormService();
                        Encounter lastHtsEnc = lastEncounter(client, hts_enc_type, Arrays.asList(
                                formService.getFormByUuid(HTS_INITIAL_TEST),
                                formService.getFormByUuid(HTS_CONFIRMATORY_TEST)
                        ));

                        List<Obs> lastHtsObs = getObsForEncounter(Context.getPersonService().getPerson(contactLastEncounter.getPatient().getPersonId()), lastHtsEnc, Arrays.asList(conceptService.getConcept(hivFinalResultConcept)) );
                        if (lastHtsObs != null && lastHtsObs.size() > 0) {
                            testObs.addAll(lastHtsObs);
                        }
                    }


                    for(Obs o: testObs) {
                        if (o.getConcept().getConceptId().equals(hivFinalResultConcept) ) {
                            lastTestResult = hivStatusConverter(o.getValueCoded());
                            lastTestDate = DATE_FORMAT.format(o.getObsDatetime());
                        } else if (o.getConcept().getConceptId().equals(contactStatusConcept)) {
                            linkageStatus = o.getValueCoded().getConceptId().equals(1065) ? "Yes" : "No";
                        } else if (o.getConcept().getConceptId().equals(facilityLinkedToConcept)) {
                            facilityLinked = o.getValueText();
                        } else if (o.getConcept().getConceptId().equals(upnConcept)) {
                            NumberFormat nf = DecimalFormat.getInstance();
                            nf.setMaximumFractionDigits(0);
                            upn = String.valueOf(o.getValueNumeric().intValue());
                        }
                    }
                }

                registeredContacts.add(SimpleObject.create(
                        "relationshipId", relationship.getId(),
                        "type", type,
                        "personLink", link,
                        "age" , age,
                        "status", alive? "Dead": "Alive",
                        "lastTestDate", lastTestDate != null? lastTestDate: "",
                        "lastTestResult", lastTestResult != null? lastTestResult: "",
                        "inCare", linkageStatus != null? linkageStatus: "",
                        "upn", upn != null? upn : "",
                        "facilityLinked", facilityLinked != null ? facilityLinked : ""
                ));
            }

        }

        otherConctacts = patientContactFormatter(kenyaUi, patientContacts);

        model.addAttribute("patient", patient);
        model.addAttribute("enrolledRelationships", enrolledRelationships);
        model.addAttribute("registeredContacts", registeredContacts);
        model.addAttribute("stats", getTestingStatistics(enrolledRelationships, registeredContacts, otherConctacts));
        model.addAttribute("returnUrl", returnUrl);
        model.put("otherContacts", otherConctacts);
    }


    SimpleObject getTestingStatistics(List<SimpleObject> enrolledPatients, List<SimpleObject> registeredRelationships, List<SimpleObject> externalPatients) {
        int totalContacts;
        int knownStatus = 0; // tested or known positives
        int positiveContacts = 0; // known positives + newly testing positive
        int linkedPatients = 0; // those with ART number

        //compute total contacts
        totalContacts = enrolledPatients.size() + externalPatients.size() + registeredRelationships.size();

        // compute linked patients
        // add all enrolled contacts to linked patients
        linkedPatients = linkedPatients + enrolledPatients.size();
        for (SimpleObject row : externalPatients) {

            if ((row.get("inCare") != null && row.get("inCare").equals("Yes"))) {
                linkedPatients++;
            }
        }

        /**
         * add those linked but still not enrolled
         */

        for (SimpleObject row : registeredRelationships) {

            if ((row.get("inCare") != null && row.get("inCare").equals("Yes"))) {
                linkedPatients++;
            }
        }

		/*
			compute known status
		 */

        // include all enrolled patients to known status
        knownStatus = knownStatus + enrolledPatients.size();

        for (SimpleObject row : externalPatients) {
            if ((row.get("baselineHivStatus") != null && !row.get("baselineHivStatus").equals("Unknown"))) {
                knownStatus++;
            }
        }

        // add for registered contacts
        for (SimpleObject row : registeredRelationships) {
            if ((row.get("lastTestResult") != null && (row.get("lastTestResult").equals("Positive")) || row.get("lastTestResult").equals("Negative"))) {
                knownStatus++;
            }
        }

		/*
				Positive contacts
		 */
        // add enrolled patients
        positiveContacts = positiveContacts + enrolledPatients.size();

        // add external contacts
        for (SimpleObject row : externalPatients) {
            if ((row.get("baselineHivStatus") != null && row.get("baselineHivStatus").equals("Positive")) || (row.get("testResult") != null && row.get("testResult").equals("Positive"))) {
                positiveContacts++;
            }
        }

        // add for registered contacts
        for (SimpleObject row : registeredRelationships) {
            if ((row.get("lastTestResult") != null && row.get("lastTestResult").equals("Positive"))) {
                positiveContacts++;
            }
        }



        return SimpleObject.create(
                "totalContacts", totalContacts,
                "knownPositives", knownStatus,
                "positiveContacts", positiveContacts,
                "linkedPatients", linkedPatients
        );
    }


    /**
     * Finds the first encounter during the program enrollment with the given encounter type
     *
     * @param type the encounter type
     *
     * @return the encounter
     */
    public Encounter firstEncounter(Patient patient, EncounterType type) {
        List<Encounter> encounters = Context.getEncounterService().getEncounters(patient, null, null, null, null, Collections.singleton(type), null, null, null, false);
        return encounters.size() > 0 ? encounters.get(0) : null;
    }


    private List<SimpleObject> patientContactFormatter(KenyaUiUtils kenyaUi, List<PatientContact> contacts) {
        List<SimpleObject> objects = new ArrayList<SimpleObject>();
        HTSService htsService = Context.getService(HTSService.class);

        for (PatientContact contact : contacts) {
            //skip if this is already registered in the system. this is to avoid double counting
            if (contact.getPatient() != null)
                continue;

            String fullName = "";


            if (contact.getFirstName() != null) {
                fullName += contact.getFirstName();
            }

            if (contact.getMiddleName() != null) {
                fullName += " " + contact.getMiddleName();
            }

            if (contact.getLastName() != null) {
                fullName += " " + contact.getLastName();
            }

            // get contact traces
            String upn = "";
            String facilityLinkedto = "";
            String linkageStatus = "";
            ContactTrace lastTrace = htsService.getLastTraceForPatientContact(contact);

            if (lastTrace != null) {
                linkageStatus = lastTrace.getStatus();
                upn = lastTrace.getUniquePatientNo();
                facilityLinkedto = lastTrace.getFacilityLinkedTo();
            }

            SimpleObject contactObject = SimpleObject.create(
                    "id", contact.getId(),
                    "fullName", fullName,
                    "sex", contact.getSex(),
                    "physicalAddress", contact.getPhysicalAddress(),
                    "phoneContact", contact.getPhoneContact(),
                    "relationType", formatRelationshipType(contact.getRelationType()),
                    "baselineHivStatus", contact.getBaselineHivStatus() != null? contact.getBaselineHivStatus() : "",
                    "appointmentDate", kenyaUi.formatDate(contact.getAppointmentDate()),
                    "age", calculateContactAge(contact.getBirthDate(), new Date()),
                    "inCare", (!linkageStatus.equals("") && linkageStatus.equals("Contacted and Linked")) ? "Yes" : "",
                    "upn", upn,
                    "facilityLinkedTo", facilityLinkedto
            );
            objects.add(contactObject);

        }

        return objects;
    }

    private String formatRelationshipType(Integer typeId) {
        if (typeId == null) {
            return null;
        } else {
            return relationshipOptions().get(typeId);
        }
    }

    private Map<Integer, String> relationshipOptions() {
        Map<Integer, String> options = new HashMap<Integer, String>();
        options.put(970, "Mother");
        options.put(971, "Father");
        options.put(972, "Sibling");
        options.put(1528, "Child");
        options.put(5617, "Spouse");
        options.put(163565, "Partner");
        options.put(162221, "Co-wife");
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
     * Finds the last encounter during the program enrollment with the given encounter type
     *
     * @param type the encounter type
     *
     * @return the encounter
     */
    public Encounter lastEncounter(Patient patient, EncounterType type, List<Form> forms) {
        List<Encounter> encounters = Context.getEncounterService().getEncounters(patient, null, null, null, forms, Collections.singleton(type), null, null, null, false);
        return encounters.size() > 0 ? encounters.get(encounters.size() - 1) : null;
    }

    public List<Obs> getObsForEncounter(Person patient, Encounter encounter, List<Concept> questions) {
        return Context.getObsService().getObservations(Arrays.asList(patient), Arrays.asList(encounter), questions,
                null, null, null, null, null, null, null, null, false );

    }

    String hivStatusConverter (Concept key) {
        Map<Concept, String> hivStatusList = new HashMap<Concept, String>();
        hivStatusList.put(conceptService.getConcept(703), "Positive");
        hivStatusList.put(conceptService.getConcept(664), "Negative");
        hivStatusList.put(conceptService.getConcept(1405), "Exposed");
        hivStatusList.put(conceptService.getConcept(1067), "Unknown");
        hivStatusList.put(conceptService.getConcept(1138), "Inconclusive");
        return hivStatusList.get(key);
    }

}