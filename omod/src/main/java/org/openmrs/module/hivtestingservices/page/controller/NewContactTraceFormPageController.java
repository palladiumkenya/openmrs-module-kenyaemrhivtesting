package org.openmrs.module.hivtestingservices.page.controller;

import org.openmrs.Patient;
import org.openmrs.module.hivtestingservices.api.PatientContact;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

public class NewContactTraceFormPageController {



    public void controller(@RequestParam("patientContact") PatientContact patientContact,
                           @RequestParam("returnUrl") String url,
                           PageModel model) {

        System.out.println("Patient contact id: ==============" + patientContact.getId());
        model.addAttribute("patientContact", patientContact);
        model.addAttribute("returnUrl", url);

    }
}
