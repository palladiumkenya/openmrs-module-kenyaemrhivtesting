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
import org.openmrs.ui.framework.fragment.action.SuccessResult;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                        "physicalAddress", patientContact.getPhysicalAddress() != null ? patientContact.getPhysicalAddress() : "",
                        "town", patientContact.getTown() != null ? patientContact.getTown() : "",
                        "subcounty", patientContact.getSubcounty() != null ? patientContact.getSubcounty() : "",
                        "facility", patientContact.getFacility() != null ? patientContact.getFacility() : "",
                        "phoneContact", patientContact.getPhoneContact() != null ? patientContact.getPhoneContact() : "",
                        "relationType", formatRelationshipType(patientContact.getRelationType()),
                        "baselineHivStatus", patientContact.getBaselineHivStatus() != null ? patientContact.getBaselineHivStatus() : "",
                        "appointmentDate",  patientContact.getAppointmentDate() != null ? kenyaUi.formatDate(patientContact.getAppointmentDate()) : "",
                        "birthDate", patientContact.getBirthDate() != null ? kenyaUi.formatDate(patientContact.getBirthDate()) : "",
                        "age", patientContact.getBirthDate() != null ? calculateContactAge(patientContact.getBirthDate(), new Date()) : "",
                        "maritalStatus", formatMaritalStatus(patientContact.getMaritalStatus()),
                        "livingWithPatient",formatLivingWithPatient(patientContact.getLivingWithPatient()),
                        "pnsApproach",formatpnsApproach(patientContact.getPnsApproach())

                        ));

    }

    private String formatRelationshipType(Integer typeId) {
        if (typeId == null) {
            return null;
        } else {
            return relationshipOptions().get(typeId);
        }
    }

    private String formatpnsApproach(Integer typeId) {
        if (typeId == null) {
            return "";
        } else {
            return pnsApproachOptions().get(typeId);
        }
    }

    private String formatMaritalStatus(Integer typeId) {
        if (typeId == null) {
            return "";
        } else {
            return maritalStatusOptions().get(typeId);
        }
    }

    private String formatLivingWithPatient(Integer typeId){
        if (typeId == null) {
            return "";
        } else {
            return livingWithPatientOptions().get(typeId);
        }

    }

    private Map<Integer, String> pnsApproachOptions() {
        Map<Integer, String> options = new HashMap<Integer, String>();
        options.put(160237,"Working together with a nCoV patient");
        options.put(165656,"Traveling together with a nCoV patient");
        options.put(1060,"Living together with a nCoV patient");
        options.put(117163,"Health care associated exposure");
        return options;

    }

    private Map<Integer, String> livingWithPatientOptions() {
        Map<Integer, String> options = new HashMap<Integer, String>();
        options.put(1065, "Yes");
        options.put(1066, "No");
        options.put(162570, "Declined to Answer");
        return options;
    }

    private Map<Integer, String> maritalStatusOptions() {
        Map<Integer, String> options = new HashMap<Integer, String>();
        options.put(1057, "Single");
        options.put(5555, "Married Monogamous");
        options.put(159715, "Married Polygamous");
        options.put(1058, "Divorced");
        options.put(1059, "Widowed");
        return options;
    }

    private Map<Integer, String> relationshipOptions () {
        Map<Integer, String> options = new HashMap<Integer, String>();
        options.put(160237, "Co-worker");
        options.put(114319, "Passanger in aircraft");
        options.put(130728, "Passanger in vehicle");
        options.put(970, "Mother");
        options.put(971, "Father");
        options.put(972, "Sibling");
        options.put(1528, "Child");
        options.put(5617, "Spouse");
        options.put(163565, "Partner");
        options.put(162221, "Co-wife");
        options.put(157351, "Injectable drug user");
        return options;
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
    /**
     * Voids the given patient contact
     * @param contact the contact
     * @return the simplified contact
     */
    public SuccessResult voidContact(@RequestParam("relationshipId") PatientContact contact) {
        Context.getService(HTSService.class).getPatientContactByID(contact.getId()).setVoided(true);
        return new SuccessResult("Patient contact voided");
    }
}



