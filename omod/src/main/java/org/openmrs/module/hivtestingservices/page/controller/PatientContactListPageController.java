package org.openmrs.module.hivtestingservices.page.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.hivtestingservices.api.HTSService;
import org.openmrs.module.hivtestingservices.api.PatientContact;
import org.openmrs.module.kenyaui.KenyaUiUtils;
import org.openmrs.module.kenyaui.annotation.AppPage;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.ui.framework.page.PageRequest;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@AppPage("kenyaemrpatientcontact.home")
public class PatientContactListPageController {

    protected static final Log log = LogFactory.getLog(PatientContactListPageController.class);

    public void controller(@SpringBean KenyaUiUtils kenyaUi,
                           @RequestParam(value = "patientId") Patient patient,
                           UiUtils ui, PageModel model) {

        HTSService htsService = Context.getService(HTSService.class);
        List<PatientContact> patientContacts = htsService.getPatientContactByPatient(patient);

        model.put("contacts", patientContacts);
        model.put("patient", patient);

    }
}



