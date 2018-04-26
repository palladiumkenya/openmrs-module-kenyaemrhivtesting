package org.openmrs.module.hivtestingservices.page.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.hivtestingservices.api.HTSService;
import org.openmrs.module.hivtestingservices.api.PatientContact;
import org.openmrs.module.kenyaui.KenyaUiUtils;
import org.openmrs.module.kenyaui.annotation.AppPage;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.converter.util.ConversionUtil;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.ui.framework.page.PageRequest;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
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

        model.put("contacts", patientContactFormatter(kenyaUi, patientContacts));
        model.put("patient", patient);

    }

    private List<SimpleObject> patientContactFormatter(KenyaUiUtils kenyaUi, List<PatientContact> contacts) {
        List<SimpleObject> objects = new ArrayList<SimpleObject>();

        for(PatientContact contact : contacts) {
            String fullName = "";

            if(contact.getFirstName() != null) {
                fullName+=contact.getFirstName();
            }

            if(contact.getMiddleName() != null) {
                fullName+= " " + contact.getMiddleName();
            }

            if(contact.getLastName() != null) {
                fullName+= " " + contact.getLastName();
            }
            SimpleObject contactObject = SimpleObject.create(
                    "id", contact.getId(),
                    "fullName", fullName,
                    "sex", contact.getSex(),
                    "physicalAddress", contact.getPhysicalAddress(),
                    "phoneContact", contact.getPhoneContact(),
                    "relationType", formatRelationshipType(contact.getRelationType()),
                    "baselineHivStatus", contact.getBaselineHivStatus(),
                    "appointmentDate",  kenyaUi.formatDate(contact.getAppointmentDate()),
                    "birthDate", kenyaUi.formatDate(contact.getBirthDate())
            );
            objects.add(contactObject);

        }

        return objects;
    }

    private String formatRelationshipType(Integer typeId) {
        PersonService personService = Context.getPersonService();
        if (typeId == null) {
            return null;
        } else {
            return personService.getRelationshipType(typeId).getaIsToB();
        }
    }

}



