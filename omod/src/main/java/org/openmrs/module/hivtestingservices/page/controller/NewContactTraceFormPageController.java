package org.openmrs.module.hivtestingservices.page.controller;

import org.openmrs.Patient;
import org.openmrs.module.hivtestingservices.api.ContactTrace;
import org.openmrs.module.hivtestingservices.api.PatientContact;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

public class NewContactTraceFormPageController {


    public void controller(@RequestParam("patientContact") PatientContact patientContact,
                           @RequestParam("patientId") Patient patient,
                           @RequestParam(value = "traceId", required = false) ContactTrace contactTrace,
                           @RequestParam("returnUrl") String url,
                           PageModel model) {

        model.addAttribute("patientContact", patientContact);
        model.addAttribute("patient", patient);
        model.addAttribute("returnUrl", url);
        model.addAttribute("contactTrace", contactTrace);

    }
}
