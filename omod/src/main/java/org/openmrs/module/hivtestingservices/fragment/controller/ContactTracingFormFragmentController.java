package org.openmrs.module.hivtestingservices.fragment.controller;

import org.openmrs.api.context.Context;
import org.openmrs.module.hivtestingservices.api.ContactTrace;
import org.openmrs.module.hivtestingservices.api.HTSService;
import org.openmrs.module.hivtestingservices.api.PatientContact;
import org.openmrs.module.kenyaui.form.AbstractWebForm;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.BindParams;
import org.openmrs.ui.framework.annotation.FragmentParam;
import org.openmrs.ui.framework.annotation.MethodParam;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ContactTracingFormFragmentController {
    public void controller(@FragmentParam(value = "id", required = false) ContactTrace contactTrace,
                           @RequestParam(value = "returnUrl") String returnUrl,
                           @RequestParam(value = "patientContact") PatientContact patientContact,
                           PageModel model) {

        ContactTrace exists = contactTrace != null ? contactTrace : null;
        model.addAttribute("patientContact", patientContact);
        model.addAttribute("command", newContactTraceForm(exists, patientContact));
        model.addAttribute("contactOptions", contactTypeList());
        model.addAttribute("tracingOutcomeOptions", tracingOutcomeList());


    }

    private List<String> tracingOutcomeList() {
        return Arrays.asList(
                new String("Contacted and Reached"),
                new String("Contacted and not Reached"),
                new String("Not Contacted"),
                new String("Contacted and Linked")
        );
    }
    private List<String> contactTypeList() {
        return Arrays.asList(
                new String("Physical"),
                new String("Phone"),
                new String("Escorted")
        );
    }

    public SimpleObject saveClientTrace(@MethodParam("newContactTraceForm") @BindParams ContactTraceForm
                                                form,
                                        UiUtils ui) {
        ui.validate(form, form, null);
        ContactTrace contactTrace = form.save();
        return SimpleObject.create(
                "patientContactId", contactTrace.getPatientContact().getId(),
                "patientId", contactTrace.getPatientContact().getPatientRelatedTo().getPatientId()
        );
    }

    public ContactTraceForm newContactTraceForm(@RequestParam(value = "id", required = false) ContactTrace
                                                        contactTrace, @RequestParam(value = "patientContact") PatientContact patientContact) {
        if (contactTrace !=null){

            return new ContactTraceForm(contactTrace, patientContact);
        }
        else {
            return new ContactTraceForm(patientContact);
        }
    }

    public class ContactTraceForm extends AbstractWebForm{
        private ContactTrace original;
        private PatientContact patientContact;
        private String contactType;
        private String status;
        private String uniquePatientNo;
        private String facilityLinkedTo;
        private String healthWorkerHandedTo;
        private String remarks;
        private Date date;
        private  Date appointmentDate;

        public ContactTraceForm() {
        }

        public ContactTraceForm(PatientContact patientContact) {
            this.patientContact = patientContact;
        }

        public ContactTraceForm(ContactTrace contactTrace, PatientContact patientContact) {

            this.original = contactTrace;
            this.contactType = contactTrace.getContactType();
            this.status = contactTrace.getStatus();
            this.uniquePatientNo = contactTrace.getUniquePatientNo();
            this.facilityLinkedTo = contactTrace.getFacilityLinkedTo();
            this.healthWorkerHandedTo = contactTrace.getHealthWorkerHandedTo();
            this.remarks = contactTrace.getRemarks();
            this.date = contactTrace.getDate();
            this.appointmentDate = patientContact.getAppointmentDate();

        }
        public ContactTrace save(){
            ContactTrace toSave;
            if (original !=null){

                toSave = original;
            }
            else{
                toSave = new ContactTrace();
            }
            toSave.setPatientContact(patientContact);
            toSave.setDate(date);
            toSave.setContactType(contactType);
            toSave.setStatus(status);
            toSave.setUniquePatientNo(uniquePatientNo);
            toSave.setFacilityLinkedTo(facilityLinkedTo);
            toSave.setHealthWorkerHandedTo(healthWorkerHandedTo);
            toSave.setRemarks(remarks);
            toSave.setAppointmentDate(appointmentDate);
            ContactTrace cTrace = Context.getService(HTSService.class).saveClientTrace(toSave);
            return cTrace;

        }

        @Override
        public void validate(Object o, Errors errors) {
            require(errors, "contactType");
            require(errors, "status");
            require(errors, "date");

            if (date != null) {
                if (date.after(new Date())) {
                    errors.rejectValue("date", "Cannot be in the future");
                } else {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(new Date());
                    calendar.add(Calendar.YEAR, -120);
                    if (date.before(calendar.getTime())) {
                        errors.rejectValue("date", " error.date.invalid");
                    }
                }
            }
            if (appointmentDate != null) {
                if (appointmentDate.before(new Date())) {
                    errors.rejectValue("appointmentDate", "Cannot be in the past");
                } else {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(new Date());
                    calendar.add(Calendar.YEAR, -120);
                    if (appointmentDate.before(calendar.getTime())) {
                        errors.rejectValue("appointmentDate", " error.date.invalid");
                    }
                }
            }
        }

        public ContactTrace getOriginal() {
            return original;
        }

        public void setOriginal(ContactTrace original) {
            this.original = original;
        }

        public PatientContact getpatientContact() {
            return patientContact;
        }

        public void setpatientContact(PatientContact patientContact) {
            this.patientContact = patientContact;
        }

        public String getContactType() {
            return contactType;
        }

        public void setContactType(String contactType) {
            this.contactType = contactType;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getUniquePatientNo() {
            return uniquePatientNo;
        }

        public void setUniquePatientNo(String uniquePatientNo) {
            this.uniquePatientNo = uniquePatientNo;
        }

        public String getFacilityLinkedTo() {
            return facilityLinkedTo;
        }

        public void setFacilityLinkedTo(String facilityLinkedTo) {
            this.facilityLinkedTo = facilityLinkedTo;
        }

        public String getHealthWorkerHandedTo() {
            return healthWorkerHandedTo;
        }

        public void setHealthWorkerHandedTo(String healthWorkerHandedTo) {
            this.healthWorkerHandedTo = healthWorkerHandedTo;
        }

        public String getRemarks() {
            return remarks;
        }

        public void setRemarks(String remarks) {
            this.remarks = remarks;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public Date getAppointmentDate() {
            return appointmentDate;
        }

        public void setAppointmentDate(Date appointmentDate) {
            this.appointmentDate = appointmentDate;
        }
    }


}



