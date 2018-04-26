package org.openmrs.module.hivtestingservices.page.controller;

import org.openmrs.Patient;
import org.openmrs.module.hivtestingservices.api.PatientContact;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

public class NewEditPatientContactFormPageController {


    public void controller(@RequestParam("patientId") Patient patient,
                           @RequestParam(value = "patientContactId", required = false) PatientContact patientContact,
                           @RequestParam("returnUrl") String url,
                           PageModel model) {

        model.addAttribute("patient", patient);
        model.addAttribute("patientContact", patientContact);
        model.addAttribute("returnUrl", url);

    }
}

