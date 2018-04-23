package org.openmrs.module.hivtestingservices.api;

import java.util.Date;

public class ContactTrace {

    private Integer id;
    private String uuid;
    private PatientContact patientContact;
    private String contactType;
    private String status;
    private String uniquePatientNo;
    private String facilityLinkedTo;
    private String healthWorkerHandedTo;
    private String remarks;
    private Date date;
    Date dateCreated;
    Integer changedBy;
    Date dateChanged;
    boolean voided;
    Integer voidedBy;
    Date dateVoided;
    String voidedReason;


    public ContactTrace() {
    }

    public ContactTrace(String uuid, String contactType, String status, String uniquePatientNo, String facilityLinkedTo, String healthWorkerHandedTo, String remarks, Date traceDate) {
        this.uuid = uuid;
        this.contactType = contactType;
        this.status = status;
        this.uniquePatientNo = uniquePatientNo;
        this.facilityLinkedTo = facilityLinkedTo;
        this.healthWorkerHandedTo = healthWorkerHandedTo;
        this.remarks = remarks;

        this.date = traceDate;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public PatientContact getPatientContact() {
        return patientContact;
    }

    public void setPatientContact(PatientContact patientContact) {
        this.patientContact = patientContact;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Integer getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(Integer changedBy) {
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

    public Integer getVoidedBy() {
        return voidedBy;
    }

    public void setVoidedBy(Integer voidedBy) {
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
}

