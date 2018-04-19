package org.openmrs.module.hivtestingservices.fragment.controller;

import org.openmrs.Patient;
import org.openmrs.module.hivtestingservices.api.ClientTrace;
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
import org.openmrs.api.context.Context;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ContactTracingFormFragmentController {
    public void controller(@FragmentParam(value = "clientTrace", required = false) ClientTrace clientTrace,
                           @RequestParam(value = "patientContactId", required = true) PatientContact patientContact,
                           PageModel model) {
        ClientTrace exists = clientTrace != null ? clientTrace : null;
        model.addAttribute("command", newContactTraceForm(exists, patientContact));

    }

    public SimpleObject saveClientTrace(@MethodParam("newContactTraceForm") @BindParams ContactTraceForm
                                                form, UiUtils ui) {
        ui.validate(form, form, null);
        ClientTrace clientTrace = form.save();
        return SimpleObject.create("id", clientTrace.getClientId().getId());
    }

    public ContactTraceForm newContactTraceForm(@RequestParam(value = "id", required = false) ClientTrace
                                                        clientTrace, @RequestParam(value = "traceRelatedContact", required = true) PatientContact patientContact) {
        if (clientTrace !=null){
            return new ContactTraceForm(clientTrace, patientContact);
        }
        else {
            return new ContactTraceForm(patientContact);
        }
    }

    public class ContactTraceForm extends AbstractWebForm{
        private ClientTrace original;
        private PatientContact traceRelatedContact;
        private String contactType;
        private String status;
        private String uniquePatientNo;
        private String facilityLinkedTo;
        private String healthWorkerHandedTo;
        private String remarks;
        private Date date;

        public ContactTraceForm() {
        }

        public ContactTraceForm(PatientContact patientContact) {
            this.traceRelatedContact = patientContact;
        }

        public ContactTraceForm(ClientTrace clientTrace, PatientContact patientContact) {
            this.original = clientTrace;
            this.contactType = clientTrace.getContactType();
            this.status = clientTrace.getStatus();
            this.uniquePatientNo = clientTrace.getUniquePatientNo();
            this.facilityLinkedTo = clientTrace.getFacilityLinkedTo();
            this.healthWorkerHandedTo = clientTrace.getHealthWorkerHandedTo();
            this.remarks = clientTrace.getRemarks();

        }
        public ClientTrace save(){
            ClientTrace toSave;
            if (original !=null){

                toSave = original;
            }
            else{
                toSave = new ClientTrace();
            }
            toSave.setContactType(contactType);
            toSave.setStatus(status);
            toSave.setUniquePatientNo(uniquePatientNo);
            toSave.setFacilityLinkedTo(facilityLinkedTo);
            toSave.setHealthWorkerHandedTo(healthWorkerHandedTo);
            toSave.setRemarks(remarks);
            ClientTrace cTrace = Context.getService(HTSService.class).saveClientTrace(toSave);
            return cTrace;

        }

        @Override
        public void validate(Object o, Errors errors) {
            require(errors, "contactType");
            require(errors, "status");
            require(errors, "uniquePatientNo");
            require(errors, "facilityLinkedTo");
            require(errors, "healthWorkerHandedTo");
            require(errors, "remarks");

        }

        public ClientTrace getOriginal() {
            return original;
        }

        public void setOriginal(ClientTrace original) {
            this.original = original;
        }

        public PatientContact getTraceRelatedContact() {
            return traceRelatedContact;
        }

        public void setTraceRelatedContact(PatientContact traceRelatedContact) {
            this.traceRelatedContact = traceRelatedContact;
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
    }
}



