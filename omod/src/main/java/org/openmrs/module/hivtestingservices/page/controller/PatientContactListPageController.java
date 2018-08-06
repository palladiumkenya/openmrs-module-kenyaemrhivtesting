package org.openmrs.module.hivtestingservices.page.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.hivtestingservices.api.ContactTrace;
import org.openmrs.module.hivtestingservices.api.HTSService;
import org.openmrs.module.hivtestingservices.api.PatientContact;
import org.openmrs.module.kenyaui.KenyaUiUtils;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PatientContactListPageController {

    protected static final Log log = LogFactory.getLog(PatientContactListPageController.class);

    public void controller(@SpringBean KenyaUiUtils kenyaUi,
                           @RequestParam(value = "patientId") Patient patient,
                           UiUtils ui, PageModel model) {

        HTSService htsService = Context.getService(HTSService.class);
        List<PatientContact> patientContacts = htsService.getPatientContactByPatient(patient);

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

        for(PatientContact contact : contacts) {
            String fullName = "";

            if(contact.getFirstName() != null) {
                fullName+=contact.getFirstName();
            }

            if(contact.getMiddleName() != null) {
                fullName+= " " + contact.getMiddleName();
            }

            if(contact.getLastName() != null) {
                fullName+= " " + contact.getLastName();
            }
            SimpleObject contactObject = SimpleObject.create(
                    "id", contact.getId(),
                    "fullName", fullName,
                    "sex", contact.getSex(),
                    "physicalAddress", contact.getPhysicalAddress(),
                    "phoneContact", contact.getPhoneContact(),
                    "relationType", formatRelationshipType(contact.getRelationType()),
                    "baselineHivStatus", contact.getBaselineHivStatus(),
                    "appointmentDate",  kenyaUi.formatDate(contact.getAppointmentDate()),
                    "birthDate", kenyaUi.formatDate(contact.getBirthDate()),
                    "contactListingDeclineReason",contact.getContactListingDeclineReason(),
                    "consentedContactListing",contact.getConsentedContactListing()
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

    private Map<Integer, String> relationshipOptions () {
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

}



