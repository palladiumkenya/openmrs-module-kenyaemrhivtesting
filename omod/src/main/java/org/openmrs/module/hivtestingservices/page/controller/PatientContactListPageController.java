package org.openmrs.module.hivtestingservices.page.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.*;
import org.openmrs.api.EncounterService;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.module.hivtestingservices.api.ContactTrace;
import org.openmrs.module.hivtestingservices.api.HTSService;
import org.openmrs.module.hivtestingservices.api.PatientContact;
import org.openmrs.module.hivtestingservices.fragment.controller.PatientContactFormFragmentController;
import org.openmrs.module.kenyaui.KenyaUiUtils;
import org.openmrs.module.kenyaui.annotation.AppPage;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

@AppPage("kenyaemr.hivtesting")
public class PatientContactListPageController {

    protected static final Log log = LogFactory.getLog(PatientContactListPageController.class);
    public static final String HTS = "9c0a7a57-62ff-4f75-babe-5835b0e921b7";
    public static final String HTS_INITIAL_TEST = "402dc5d7-46da-42d4-b2be-f43ea4ad87b0";
    public static final String HTS_CONFIRMATORY_TEST = "b08471f6-0892-4bf7-ab2b-bf79797b8ea4";

    public void controller(@SpringBean KenyaUiUtils kenyaUi,
                           @RequestParam(value = "patientId") Patient patient,
                           UiUtils ui, PageModel model) {

        HTSService htsService = Context.getService(HTSService.class);
        List<PatientContact> patientContacts = htsService.getPatientContactByPatient(patient);
        PatientContactFormFragmentController pc = new PatientContactFormFragmentController();
        PatientContact contactEntry = htsService.getPatientContactEntryForPatient(patient);
        List<ContactTrace> contactTrace = htsService.getContactTraceByPatientContact(contactEntry);

        String lastTraceStatus;
        if (htsService.getLastTraceForPatientContact(contactEntry) != null) {

            lastTraceStatus = htsService.getLastTraceForPatientContact(contactEntry).getStatus();

        } else {
            lastTraceStatus = "";
        }

        model.put("lastTraceStatus", lastTraceStatus);
        model.put("traces", contactTrace);
        model.put("patientContact", contactEntry);
        model.put("contacts", patientContactFormatter(kenyaUi, patientContacts));
        model.put("patient", patient);

    }

    private List<SimpleObject> patientContactFormatter(KenyaUiUtils kenyaUi, List<PatientContact> contacts) {
        List<SimpleObject> objects = new ArrayList<SimpleObject>();

        EncounterService encService = Context.getEncounterService();
        FormService formService = Context.getFormService();
        EncounterType et = encService.getEncounterTypeByUuid(HTS);
        Form initial = formService.getFormByUuid(HTS_INITIAL_TEST);
        Form retest = formService.getFormByUuid(HTS_CONFIRMATORY_TEST);
        String finalResultConceptUUID = "159427AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
        String finalResult = "";

        for (PatientContact contact : contacts) {
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

            // check if contact is registered, and has undertaken hts
            Date dateTested = null;
            if (contact.getPatient() != null) {
                Encounter encounter = lastEncounter(contact.getPatient(), et, Arrays.asList(initial, retest));
                if (encounter != null) {
                    dateTested = encounter.getEncounterDatetime();
                    for (Obs o : encounter.getObs()) {
                        if (o.getConcept().getUuid().equals(finalResultConceptUUID)) {
                            Concept val = o.getValueCoded();
                            if (val != null) {
                                if (val.getConceptId().intValue() == 703) {
                                    finalResult = "Positive";
                                } else if (val.getConceptId().intValue() == 664) {
                                    finalResult = "Negative";
                                } else if (val.getConceptId().intValue() == 1138) {
                                    finalResult = "Inconclusive";
                                }
                            }
                            break;
                        }
                    }
                }

            }
            SimpleObject contactObject = SimpleObject.create(
                    "id", contact.getId(),
                    "fullName", fullName,
                    "sex", contact.getSex(),
                    "physicalAddress", contact.getPhysicalAddress(),
                    "phoneContact", contact.getPhoneContact(),
                    "relationType", formatRelationshipType(contact.getRelationType()),
                    "baselineHivStatus", contact.getBaselineHivStatus(),
                    "appointmentDate", kenyaUi.formatDate(contact.getAppointmentDate()),
                    "birthDate", kenyaUi.formatDate(contact.getBirthDate()),
                    "maritalStatus", formatMaritalStatusOptions(contact.getMaritalStatus()),
                    "pnsApproach", formatpnsApproachOptions(contact.getPnsApproach()),
                    "patient", contact.getPatient(),
                    "contactListingDeclineReason", contact.getContactListingDeclineReason(),
                    "consentedContactListing", contact.getConsentedContactListing(),
                    "dateTested", dateTested != null ? kenyaUi.formatDate(dateTested) : "",
                    "testResult", finalResult
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

    private String formatpnsApproachOptions(Integer typeId) {
        if (typeId == null) {
            return null;
        } else {
            return pnsApproachOptions().get(typeId);
        }
    }

    private String formatMaritalStatusOptions(Integer typeId) {
        if (typeId == null) {
            return null;
        } else {
            return maritalStatusOptions().get(typeId);
        }
    }

    private Map<Integer, String> relationshipOptions() {
        Map<Integer, String> options = new HashMap<Integer, String>();
        options.put(970, "Mother");
        options.put(971, "Father");
        options.put(972, "Sibling");
        options.put(1528, "Child");
        options.put(5617, "Spouse");
        options.put(163565, "Sexual partner");
        options.put(162221, "Co-wife");
        options.put(157351, "Injectable drug user");
        return options;
    }

    private Map<Integer, String> pnsApproachOptions() {
        Map<Integer, String> options = new HashMap<Integer, String>();
        options.put(162284, "Dual referral");
        options.put(160551, "Passive referral");
        options.put(161642, "Contract referral");
        options.put(163096, "Provider referral");
        return options;

    }

    private Map<Integer, String> maritalStatusOptions() {
        Map<Integer, String> options = new HashMap<Integer, String>();
        options.put(1057, "Single");
        options.put(5555, "Married Monogamous");
        options.put(159715, "Married Polygamous");
        options.put(1058, "Divorced");
        options.put(1059, "Widowed");
        return options;
    }

    public static Encounter lastEncounter(Patient patient, EncounterType type, List<Form> forms) {
        List<Encounter> encounters = Context.getEncounterService().getEncounters(patient, null, null, null, forms, Collections.singleton(type), null, null, null, false);
        return encounters.size() > 0 ? encounters.get(encounters.size() - 1) : null;
    }

}



