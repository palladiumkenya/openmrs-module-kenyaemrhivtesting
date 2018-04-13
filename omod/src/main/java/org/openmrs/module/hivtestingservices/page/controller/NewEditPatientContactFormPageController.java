package org.openmrs.module.hivtestingservices.page.controller;

import org.openmrs.Patient;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

public class NewEditPatientContactFormPageController {


    public void controller(@RequestParam("patientId") Patient patient,
                           @RequestParam("returnUrl") String url,
                           PageModel model) {

        model.addAttribute("patient", patient);
        model.addAttribute("returnUrl", url);

    }
}

