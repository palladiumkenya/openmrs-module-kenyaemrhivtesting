package org.openmrs.module.hivtestingservices.page.controller;

import org.openmrs.module.hivtestingservices.api.PatientContact;
import org.openmrs.module.kenyaui.annotation.AppPage;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;
@AppPage("kenyaemr.surveillance")
public class RegisterContactPageController {


    public void controller(@RequestParam(value = "patientContact") PatientContact patientContact,
                           @RequestParam("returnUrl") String url,
                           PageModel model) {

        model.addAttribute("patientContact", patientContact);
        model.addAttribute("returnUrl", url);

    }
}

