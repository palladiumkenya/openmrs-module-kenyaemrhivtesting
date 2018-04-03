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
                           UiUtils ui, PageModel model) {

        HTSService service = Context.getService(HTSService.class);
        System.out.println("Testing service ================ " + service.getPatientContacts().size());
        List<PatientContact> patientContacts = service.getPatientContacts();
        model.put("contacts", patientContacts);
    }

  /*  @RequestMapping("/patientContactList")
    public String listPatientContacts(Model model) {
        //get patient contacts from the service
        List<PatientContact> patientContacts = htsService.getPatientContacts();
        model.addAttribute("patientContacts", patientContacts);
        return "patientContactList";

    }*/

 /*   @RequestMapping("/addPatientContact")
    public String addPatientContact(Model model){

        //Model attribute to bind the form data
        PatientContact patientContact = new PatientContact();
        model.addAttribute("patientContact",patientContact);
        return "addPatientContact";
    }

  /*  @PostMapping("/savePatientContact"){

        public String savePatientContact(@ModelAttribute("patientContact") PatientContact patientContact){
    //save patient contact using the service
            htsService.persistPatientContact(patientContact);
        }*/
    }



