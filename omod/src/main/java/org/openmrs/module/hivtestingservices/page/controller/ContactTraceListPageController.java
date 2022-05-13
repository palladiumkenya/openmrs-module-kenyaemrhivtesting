
package org.openmrs.module.hivtestingservices.page.controller;

import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.hivtestingservices.api.ContactTrace;
import org.openmrs.module.hivtestingservices.api.HTSService;
import org.openmrs.module.hivtestingservices.api.PatientContact;
import org.openmrs.module.kenyaui.KenyaUiUtils;
import org.openmrs.module.kenyaui.annotation.AppPage;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@AppPage("kenyaemr.hivtesting")
public class ContactTraceListPageController {

    public void controller(@SpringBean KenyaUiUtils kenyaUi,
                           @RequestParam(value = "patientContact") PatientContact patientContact,
                           @RequestParam(value = "patientId") Patient patient,

                           UiUtils ui, PageModel model) {

        HTSService htsService = Context.getService(HTSService.class);
        List<ContactTrace> contactTrace = htsService.getContactTraceByPatientContact(patientContact);

        String lastTraceStatus;
        if (htsService.getLastTraceForPatientContact(patientContact) != null) {

            lastTraceStatus = htsService.getLastTraceForPatientContact(patientContact).getStatus();

        } else {
            lastTraceStatus = "";
        }

        model.put("lastTraceStatus", lastTraceStatus);
        model.put("traces",ContactTraceFormatter(kenyaUi,contactTrace));
        model.put("patientContact", patientContact);
        model.put("currentPatient", patient);

    }

    private List<SimpleObject> ContactTraceFormatter(KenyaUiUtils kenyaUi, List<ContactTrace> traces) {
        List<SimpleObject> objects = new ArrayList<SimpleObject>();

        for(ContactTrace cTrace : traces) {

            SimpleObject contactObject = SimpleObject.create(
                    "id", cTrace.getId(),
                    "date", kenyaUi.formatDate(cTrace.getDate()),
                    "contactType", cTrace.getContactType(),
                    "status", cTrace.getStatus(),
                    "dateBooked", cTrace.getAppointmentDate() != null ? kenyaUi.formatDate(cTrace.getAppointmentDate()) : "",
                    "reasonUncontacted", cTrace.getReasonUncontacted(),
                    "facilityLinkedTo", cTrace.getFacilityLinkedTo(),
                    "healthWorkerHandedTo", cTrace.getHealthWorkerHandedTo(),
                    "remarks",  cTrace.getRemarks()
            );
            objects.add(contactObject);

        }

        return objects;
    }


}

