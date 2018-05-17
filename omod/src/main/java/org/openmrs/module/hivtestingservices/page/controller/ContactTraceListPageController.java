
package org.openmrs.module.hivtestingservices.page.controller;

import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.hivtestingservices.api.ContactTrace;
import org.openmrs.module.hivtestingservices.api.HTSService;
import org.openmrs.module.hivtestingservices.api.PatientContact;
import org.openmrs.module.kenyaui.KenyaUiUtils;
import org.openmrs.module.kenyaui.annotation.AppPage;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


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
        model.put("traces",contactTrace);
        model.put("patientContact", patientContact);
        model.put("currentPatient", patient);

    }

}

