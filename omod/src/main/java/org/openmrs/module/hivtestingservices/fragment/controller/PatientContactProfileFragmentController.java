package org.openmrs.module.hivtestingservices.fragment.controller;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.hivtestingservices.api.ContactTrace;
import org.openmrs.module.hivtestingservices.api.HTSService;
import org.openmrs.module.hivtestingservices.api.PatientContact;
import org.openmrs.module.kenyaui.KenyaUiUtils;
import org.openmrs.module.kenyaui.form.AbstractWebForm;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.BindParams;
import org.openmrs.ui.framework.annotation.FragmentParam;
import org.openmrs.ui.framework.annotation.MethodParam;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.converter.util.ConversionUtil;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class PatientContactProfileFragmentController {
    public void controller(@SpringBean KenyaUiUtils kenyaUi, @RequestParam(value = "patientContact") PatientContact patientContact,
                           FragmentModel model) {

        String fullName = "";

        if(patientContact.getFirstName() != null) {
            fullName+=patientContact.getFirstName();
        }

        if(patientContact.getMiddleName() != null) {
            fullName+= " " + patientContact.getMiddleName();
        }

        if(patientContact.getLastName() != null) {
            fullName+= " " + patientContact.getLastName();
        }
        model.addAttribute("patientContact",
                SimpleObject.create(
                        "id", patientContact.getId(),
                        "fullName", fullName,
                        "sex", patientContact.getSex(),
                        "physicalAddress", patientContact.getPhysicalAddress(),
                        "phoneContact", patientContact.getPhoneContact(),
                        "relationType", formatRelationshipType(patientContact.getRelationType()),
                        "baselineHivStatus", patientContact.getBaselineHivStatus(),
                        "appointmentDate",  kenyaUi.formatDate(patientContact.getAppointmentDate()),
                        "birthDate", kenyaUi.formatDate(patientContact.getBirthDate()),
                        "age", calculateContactAge(patientContact.getBirthDate(), new Date())
                ));

    }

    private String formatRelationshipType(Integer typeId) {
        PersonService personService = Context.getPersonService();
        if (typeId == null) {
            return null;
        } else {
            return personService.getRelationshipType(typeId).getaIsToB();
        }
    }

    private Integer calculateContactAge(Date birthdate, Date onDate) {
            if (birthdate == null) {
                return null;
            }

            // Use default end date as today.
            Calendar today = Calendar.getInstance();
            // But if given, use the given date.
            if (onDate != null) {
                today.setTime(onDate);
            }


            Calendar bday = Calendar.getInstance();
            bday.setTime(birthdate);

            int age = today.get(Calendar.YEAR) - bday.get(Calendar.YEAR);

            // Adjust age when today's date is before the person's birthday
            int todaysMonth = today.get(Calendar.MONTH);
            int bdayMonth = bday.get(Calendar.MONTH);
            int todaysDay = today.get(Calendar.DAY_OF_MONTH);
            int bdayDay = bday.get(Calendar.DAY_OF_MONTH);

            if (todaysMonth < bdayMonth) {
                age--;
            } else if (todaysMonth == bdayMonth && todaysDay < bdayDay) {
                // we're only comparing on month and day, not minutes, etc
                age--;
            }

            return age;
        }
}



