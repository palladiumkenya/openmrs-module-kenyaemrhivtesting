package org.openmrs.module.hivtestingservices.api.impl;

import java.util.Date;

public class ClientTrace {

    private int id;
    private PatientContact clientId;
    private String uuid;
    private String contactType;
    private String status;
    private String uniquePatientNo;
    private String facilityLinkedTo;
    private String healthWorkerHandedTo;
    private String remarks;
    private Date date;
    private Date dateCreated;
    private int changedBy;
    private Date dateChanged;
    private boolean voided;
    private int voidedBy;
    private Date dateVoided;
    private String voidedReason;


    public ClientTrace() {
    }

    public ClientTrace(String uuid, String contactType, String status, String uniquePatientNo, String facilityLinkedTo, String healthWorkerHandedTo, String remarks, Date date, Date dateCreated, int changedBy, Date dateChanged, boolean voided, int voidedBy, Date dateVoided, String voidedReason) {
        this.uuid = uuid;
        this.contactType = contactType;
        this.status = status;
        this.uniquePatientNo = uniquePatientNo;
        this.facilityLinkedTo = facilityLinkedTo;
        this.healthWorkerHandedTo = healthWorkerHandedTo;
        this.remarks = remarks;
        this.dateCreated = dateCreated;
        this.changedBy = changedBy;
        this.dateChanged = dateChanged;
        this.voided = voided;
        this.voidedBy = voidedBy;
        this.dateVoided = dateVoided;
        this.voidedReason = voidedReason;
        this.date = date;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    public PatientContact getClientId() {
        return clientId;
    }

    public void setClientId(PatientContact clientId) {
        this.clientId = clientId;
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
}

