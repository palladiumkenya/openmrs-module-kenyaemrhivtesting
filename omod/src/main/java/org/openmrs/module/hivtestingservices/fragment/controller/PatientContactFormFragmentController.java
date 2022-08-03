package org.openmrs.module.hivtestingservices.fragment.controller;

import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.context.Context;
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

import java.util.*;
import java.util.logging.Logger;

import static java.awt.SystemColor.info;

/**
 * Controller for adding and editing Patient Contacts
 */
public class PatientContactFormFragmentController {

    public void controller(@FragmentParam(value = "patientContact", required = false) PatientContact patientContact,
                           @RequestParam(value = "patientId", required = true) Patient patient,
                           PageModel model) {

        PatientContact exists = patientContact != null ? patientContact : null;

        model.addAttribute("command", newEditPatientContactForm(exists, patient));
        model.addAttribute("relationshipTypeOptions", getRelationshipTypeOptions());
        model.addAttribute("hivStatusOptions", hivStatusOptions());
        model.addAttribute("ipvOutcomeOptions", ipvOutcomeOptions());
        model.addAttribute("livingWithPatientOptions", getLivingWithPatientOptions());
        model.addAttribute("preferredPNSApproachOptions", getPreferredPNSApproachOptions());
        model.addAttribute("maritalStatusOptions", getMaritalStatusOptions());

    }

    private Map<Integer, String> createLivingWithPatientOptionsFromConcepts() {
        Map<Integer, String> options = new HashMap<Integer, String>();
        options.put(1065, "Yes");
        options.put(1066, "No");
        options.put(162570, "Declined to Answer");
        return options;
    }

    private Map<Integer, String> createPreferredPNSApproachOptionsFromConcepts() {
        Map<Integer, String> options = new HashMap<Integer, String>();
        options.put(162284,"Dual referral");
        options.put(160551,"Passive referral");
        options.put(161642,"Contract referral");
        options.put(163096,"Provider referral");
        return options;

    }

    private Map<Integer, String> createMaritalStatusOptionsFromConcepts() {
        Map<Integer, String> options = new HashMap<Integer, String>();
        options.put(1057, "Single");
        options.put(5555, "Married Monogamous");
        options.put(159715, "Married Polygamous");
        options.put(1058, "Divorced");
        options.put(1059, "Widowed");
        return options;
    }

    private List<String> hivStatusOptions() {
        return Arrays.asList("Unknown", "Positive", "Negative");
    }

    private List<String> ipvOutcomeOptions() {
        return Arrays.asList("True", "False");
    }

    protected List<SimpleObject> getRelationshipTypeOptions() {
        List<SimpleObject> options = new ArrayList<SimpleObject>();

        /*for (RelationshipType type : Context.getPersonService().getAllRelationshipTypes()) {
            if (type.getaIsToB().equals(type.getbIsToA())) {
                options.add(SimpleObject.create("value", type.getId(), "label", type.getaIsToB()));
            }
            else {
                options.add(SimpleObject.create("value", type.getId(), "label", type.getaIsToB()));
            }
        }*/
        for (Map.Entry<Integer, String> option : createRelationshipOptionsFromConcepts().entrySet())
            options.add(SimpleObject.create("value", option.getKey(), "label", option.getValue()));

        return options;
    }

    protected List<SimpleObject> getMaritalStatusOptions() {
        List<SimpleObject> options = new ArrayList<SimpleObject>();
        for (Map.Entry<Integer, String> option : createMaritalStatusOptionsFromConcepts().entrySet())
            options.add(SimpleObject.create("value", option.getKey(), "label", option.getValue()));

        return options;
    }

    protected List<SimpleObject> getLivingWithPatientOptions() {
        List<SimpleObject> options = new ArrayList<SimpleObject>();
        for (Map.Entry<Integer, String> option : createLivingWithPatientOptionsFromConcepts().entrySet())
            options.add(SimpleObject.create("value", option.getKey(), "label", option.getValue()));

        return options;
    }


    protected List<SimpleObject> getPreferredPNSApproachOptions() {
        List<SimpleObject> options = new ArrayList<SimpleObject>();
        for (Map.Entry<Integer, String> option : createPreferredPNSApproachOptionsFromConcepts().entrySet())
            options.add(SimpleObject.create("value", option.getKey(), "label", option.getValue()));

        return options;
    }

    private Map<Integer, String> createRelationshipOptionsFromConcepts() {
        Map<Integer, String> options = new HashMap<Integer, String>();
        options.put(970, "Mother");
        options.put(971, "Father");
        options.put(972, "Sibling");
        options.put(1528, "Child");
        options.put(5617, "Spouse");
        options.put(163565, "Sexual partner");
        options.put(162221, "Co-wife");
        options.put(157351, "Injectable drug user");
        options.put(166606, "SNS");
        return options;
    }


    public SimpleObject savePatientContact(@MethodParam("newEditPatientContactForm") @BindParams EditPatientContactForm form, UiUtils ui) {
        ui.validate(form, form, null);
        PatientContact patientContact = form.save();

        return SimpleObject.create("id", patientContact.getPatientRelatedTo().getId());
    }

    public EditPatientContactForm newEditPatientContactForm(@RequestParam(value = "id", required = false) PatientContact patientContact, @RequestParam(value = "patientRelatedTo", required = true) Patient patient) {
        if (patientContact != null) {
            return new EditPatientContactForm(patientContact, patient);
        } else {
            return new EditPatientContactForm(patient);
        }
    }

    public class EditPatientContactForm extends AbstractWebForm {

        private PatientContact original;
        private String firstName;
        private String middleName;
        private String lastName;
        private String sex;
        private Date birthDate;
        private String physicalAddress;
        private String phoneContact;
        private Patient patientRelatedTo;
        private Integer relationType;
        private Date reportedTestDate;
        private Date appointmentDate;
        private Date listingDate;
        private String baselineHivStatus;
        private String ipvOutcome;
        private Integer maritalStatus;
        private String landmark;
        private Integer livingWithPatient;
        private Integer pnsApproach;
        private String contactListingDeclineReason;
        private Integer consentedContactListing;
        private Boolean voided = false;
        private User voidedBy;
        private Date dateVoided;
        private String voidedReason;


        public EditPatientContactForm() {
        }

        public EditPatientContactForm(Patient patient) {
            this.patientRelatedTo = patient;
        }

        public EditPatientContactForm(PatientContact patientContact, Patient patient) {

            this.original = patientContact;
            this.firstName = patientContact.getFirstName();
            this.middleName = patientContact.getMiddleName();
            this.lastName = patientContact.getLastName();
            this.sex = patientContact.getSex();
            this.birthDate = patientContact.getBirthDate();
            this.physicalAddress = patientContact.getPhysicalAddress();
            this.phoneContact = patientContact.getPhoneContact();
            this.patientRelatedTo = patient;
            this.relationType = patientContact.getRelationType();
            this.reportedTestDate = patientContact.getReportedTestDate();
            this.appointmentDate = patientContact.getAppointmentDate();
            this.listingDate = patientContact.getListingDate();
            this.baselineHivStatus = patientContact.getBaselineHivStatus();
            this.ipvOutcome = patientContact.getIpvOutcome();
            this.maritalStatus = patientContact.getMaritalStatus();
            this.livingWithPatient = patientContact.getLivingWithPatient();
            this.pnsApproach = patientContact.getPnsApproach();
            this.contactListingDeclineReason = patientContact.getContactListingDeclineReason();
            this.voided = patientContact.getVoided();
            this.voidedBy = patientContact.getVoidedBy();
            this.dateVoided = patientContact.getDateVoided();
            this.voidedReason = patientContact.getVoidReason();

        }

        public PatientContact save() {
            PatientContact toSave;
            if (original != null) {
                toSave = original;
            } else {
                toSave = new PatientContact();
            }

            toSave.setListingDate(listingDate);
            toSave.setFirstName(firstName);
            toSave.setMiddleName(middleName);
            toSave.setLastName(lastName);
            toSave.setSex(sex);
            toSave.setBirthDate(birthDate);
            toSave.setPatientRelatedTo(patientRelatedTo);
            toSave.setRelationType(relationType);
            toSave.setPhysicalAddress(physicalAddress);
            toSave.setPhoneContact(phoneContact);
            toSave.setAppointmentDate(appointmentDate);
            toSave.setReportedTestDate(reportedTestDate);
            toSave.setBaselineHivStatus(baselineHivStatus);
            toSave.setIpvOutcome(ipvOutcome);
            toSave.setMaritalStatus(maritalStatus);
            toSave.setLivingWithPatient(livingWithPatient);
            toSave.setPnsApproach(pnsApproach);
            toSave.setConsentedContactListing(consentedContactListing);
            toSave.setContactListingDeclineReason(contactListingDeclineReason);
            toSave.setVoided(voided);
            toSave.setVoidedBy(voidedBy);
            toSave.setDateVoided(dateVoided);
            toSave.setVoidReason(voidedReason);
            PatientContact pc = Context.getService(HTSService.class).savePatientContact(toSave);
            return pc;
        }

        @Override
        public void validate(Object o, Errors errors) {
            require(errors, "sex");
            require(errors, "birthDate");
            if (birthDate != null) {
                if (birthDate.after(new Date())) {
                    errors.rejectValue("birthDate", "Cannot be a future date");
                } else {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(new Date());
                    calendar.add(Calendar.YEAR, -120);
                    if (birthDate.before(calendar.getTime())) {
                        errors.rejectValue("birthDate", "error.date.invalid");
                    }
                }
            }
            require(errors, "listingDate");
            if (listingDate != null) {
                if (listingDate.after(new Date())) {
                    errors.rejectValue("listingDate", "Cannot be a future date");
                } else {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(new Date());
                    calendar.add(Calendar.YEAR, -120);
                    if (listingDate.before(calendar.getTime())) {
                        errors.rejectValue("listingDate", "error.date.invalid");
                    }
                }
            }

            if (appointmentDate != null && !DateUtils.isSameDay(appointmentDate, listingDate)) {

                if (appointmentDate.before(listingDate))
                    errors.rejectValue("appointmentDate", "Cannot be before contact listing date");
            }

            require(errors, "relationType");
            if (relationType == null) {
                errors.rejectValue("relationType", "Relationship to Patient is required");
            }

            if ((baselineHivStatus.equals("Positive") || baselineHivStatus.equals("Negative")) && reportedTestDate == null) {
                    errors.rejectValue("reportedTestDate", "Date tested is required");
            }

            if (reportedTestDate != null) {
                if (reportedTestDate.after(new Date())) {
                    errors.rejectValue("reportedTestDate", "Cannot be a future date");
                } else {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(new Date());
                    calendar.add(Calendar.YEAR, -120);
                    if (reportedTestDate.before(calendar.getTime())) {
                        errors.rejectValue("reportedTestDate", "error.date.invalid");
                    }
                }
            }
        }

        public PatientContact getOriginal() {
            return original;
        }

        public void setOriginal(PatientContact original) {
            this.original = original;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getMiddleName() {
            return middleName;
        }

        public void setMiddleName(String middleName) {
            this.middleName = middleName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getSex() {
            return sex;
        }

        public void setSex(String sex) {
            this.sex = sex;
        }

        public Date getBirthDate() {
            return birthDate;
        }

        public void setBirthDate(Date birthDate) {
            this.birthDate = birthDate;
        }

        public String getPhysicalAddress() {
            return physicalAddress;
        }

        public void setPhysicalAddress(String physicalAddress) {
            this.physicalAddress = physicalAddress;
        }

        public String getPhoneContact() {
            return phoneContact;
        }

        public void setPhoneContact(String phoneContact) {
            this.phoneContact = phoneContact;
        }

        public Patient getPatientRelatedTo() {
            return patientRelatedTo;
        }

        public void setPatientRelatedTo(Patient patientRelatedTo) {
            this.patientRelatedTo = patientRelatedTo;
        }

        public Integer getRelationType() {
            return relationType;
        }

        public void setRelationType(Integer relationType) {
            this.relationType = relationType;
        }

        public Date getReportedTestDate() {
            return reportedTestDate;
        }

        public void setReportedTestDate(Date reportedTestDate) {
            this.reportedTestDate = reportedTestDate;
        }

        public Date getAppointmentDate() {
            return appointmentDate;
        }

        public void setAppointmentDate(Date appointmentDate) {
            this.appointmentDate = appointmentDate;
        }

        public String getBaselineHivStatus() {
            return baselineHivStatus;
        }

        public void setBaselineHivStatus(String baselineHivStatus) {
            this.baselineHivStatus = baselineHivStatus;
        }

        public String getIpvOutcome() {
            return ipvOutcome;
        }

        public void setIpvOutcome(String ipvOutcome) {
            this.ipvOutcome = ipvOutcome;
        }

        public Integer getMaritalStatus() {
            return maritalStatus;
        }

        public void setMaritalStatus(Integer maritalStatus) {
            this.maritalStatus = maritalStatus;
        }

        public String getLandmark() {
            return landmark;
        }

        public void setLandmark(String landmark) {
            this.landmark = landmark;
        }

        public Integer getLivingWithPatient() {
            return livingWithPatient;
        }

        public void setLivingWithPatient(Integer livingWithPatient) {
            this.livingWithPatient = livingWithPatient;
        }

        public Integer getPnsApproach() {
            return pnsApproach;
        }

        public void setPnsApproach(Integer pnsApproach) {
            this.pnsApproach = pnsApproach;
        }

        public String getContactListingDeclineReason() {
            return contactListingDeclineReason;
        }

        public void setContactListingDeclineReason(String contactListingDeclineReason) {
            this.contactListingDeclineReason = contactListingDeclineReason;
        }

        public Integer getConsentedContactListing() {
            return consentedContactListing;
        }

        public void setConsentedContactListing(Integer consentedContactListing) {
            this.consentedContactListing = consentedContactListing;
        }

        public Date getListingDate() {
            return listingDate;
        }

        public void setListingDate(Date listingDate) {
            this.listingDate = listingDate;
        }
    }

}