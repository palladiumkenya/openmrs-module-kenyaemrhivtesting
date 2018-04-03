package org.openmrs.module.hivtestingservices.page.controller;
//import com.fasterxml.jackson.databind.node.ArrayNode;
//import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.hivtestingservices.api.HTSService;
import org.openmrs.module.hivtestingservices.api.impl.PatientContact;
import org.openmrs.module.kenyaui.KenyaUiUtils;
import org.openmrs.module.kenyaui.annotation.AppPage;
import org.openmrs.ui.framework.Model;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.ui.framework.page.PageRequest;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.ui.framework.page.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AppPage("patientContactList")
public class PatientContactListPageController {

    protected static final Log log = LogFactory.getLog(PatientContactListPageController.class);

    @Autowired
    private HTSService htsService;

    public void controller(@RequestParam(value="patientContactId") PatientContact patientContact,
                           PageRequest pageRequest,
                           PageModel model) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("patientContactId", patientContact.getId());
        List<PatientContact> patientContacts =  htsService.getPatientContacts();
        model.addAttribute("contacts",patientContacts);
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



