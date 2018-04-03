package org.openmrs.module.hivtestingservices.api;


import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.openmrs.patient.*;
import org.openmrs.Patient;
import org.openmrs.User;
import javax.persistence.*;
import java.util.Date;
import org.openmrs.Obs;

public class PatientContact {

    private int id;
    private String uuid;
    private Obs obsGroupId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String sex;
    private Date birthDate;
    private String physicalAddress;
    private String phoneContact;
    private Patient patientRelatedTo;
    private String relationType;
    private Date appointmentDate;
    private int baselineHivStatus;
    private int ipvOutcome;
    private Patient patient;
    private Date dateCreated;
    private int changedBy;
    private Date dateChanged;
    private boolean voided;
    private int voidedBy;
    private Date dateVoided;
    private String voidedReason;

    public PatientContact() {
    }

    public PatientContact(String uuid,/* int obsGroupId, */String firstName, String middleName, String lastName, String sex, Date birthDate, String physicalAddress, String phoneContact, /*int patientRelatedTo,*/ String relationType, Date appointmentDate, int baselineHivStatus, int ipvOutcome, Date dateCreated, int changedBy, Date dateChanged, boolean voided, int voidedBy, Date dateVoided, String voidedReason) {
        this.uuid = uuid;
       // this.obsGroupId = obsGroupId;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.sex = sex;
        this.birthDate = birthDate;
        this.physicalAddress = physicalAddress;
        this.phoneContact = phoneContact;
       // this.patientRelatedTo = patientRelatedTo;
        this.relationType = relationType;
        this.appointmentDate = appointmentDate;
        this.baselineHivStatus = baselineHivStatus;
        this.ipvOutcome = ipvOutcome;
        this.dateCreated = dateCreated;
        this.changedBy = changedBy;
        this.dateChanged = dateChanged;
        this.voided = voided;
        this.voidedBy = voidedBy;
        this.dateVoided = dateVoided;
        this.voidedReason = voidedReason;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid)
    {
        this.uuid = uuid;
    }

    public Obs getObsGroupId() {
        return obsGroupId;
    }

   /* public int getObsGroupId() {
        return obsGroupId;
    }

    public void setObsGroupId(int obsGroupId){
        this.obsGroupId = obsGroupId;
    }*/
   public void setObsGroupId(Obs obsGroupId) {
       this.obsGroupId = obsGroupId;
   }

    public Patient getPatientRelatedTo() {
        return patientRelatedTo;
    }

    public void setPatientRelatedTo(Patient patientRelatedTo) {
        this.patientRelatedTo = patientRelatedTo;
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

    /*public int getPatientRelatedTo() {
        return patientRelatedTo;
    }

    public void setPatientRelatedTo(int patientRelatedTo) {
        this.patientRelatedTo = patientRelatedTo;
    }*/

    public String getRelationType() {
        return relationType;
    }

    public void setRelationType(String relationType) {
        this.relationType = relationType;
    }

    public Date getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(Date appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public int getBaselineHivStatus() {
        return baselineHivStatus;
    }

    public void setBaselineHivStatus(int baselineHivStatus) {
        this.baselineHivStatus = baselineHivStatus;
    }

    public int getIpvOutcome() {
        return ipvOutcome;
    }

    public void setIpvOutcome(int ipvOutcome) {
        this.ipvOutcome = ipvOutcome;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public int getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(int changedBy) {
        this.changedBy = changedBy;
    }

    public Date getDateChanged() {
        return dateChanged;
    }

    public void setDateChanged(Date dateChanged) {
        this.dateChanged = dateChanged;
    }

    public boolean isVoided() {
        return voided;
    }

    public void setVoided(boolean voided) {
        this.voided = voided;
    }

    public int getVoidedBy() {
        return voidedBy;
    }

    public void setVoidedBy(int voidedBy) {
        this.voidedBy = voidedBy;
    }

    public Date getDateVoided() {
        return dateVoided;
    }

    public void setDateVoided(Date dateVoided) {
        this.dateVoided = dateVoided;
    }

    public String getVoidedReason() {
        return voidedReason;
    }

    public void setVoidedReason(String voidedReason) {
        this.voidedReason = voidedReason;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }
}

