package org.openmrs.module.hivtestingservices.api;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.openmrs.patient.*;
import org.openmrs.Patient;
import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name="PATIENT_CONTACT)")
@SQLDelete(sql="update patient_contact SET voided ='true' where id=?")
@Where(clause="voided !='true'")
public class PatientContact {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id")
    private int id;
    @Column(name="uuid")
    private String uuid;
    @Column(name="obs_group_id")
    private int obsGroupId;
    @Column(name="first_name")
    private String firstName;
    @Column(name="middle_name")
    private String middleName;
    @Column(name="last_name")
    private String lastName;
    @Column(name="sex")
    private int sex;
    @Column(name="birth_date")
    private Date birthDate;
    @Column(name="physical_address")
    private String physicalAddress;
    @Column(name="phone_contact")
    private String phoneContact;
    @Column(name="patient_related_to")
    private int patientRelatedTo;
    @Column(name="relationship_type")
    private String relationType;
    @Column(name="appointment_date")
    private Date appointmentDate;
    @Column(name="baseline_hiv_status")
    private int baselineHivStatus;
    @Column(name="ipv_outcome")
    private int ipvOutcome;
    @OneToMany(cascade={CascadeType.DETACH,CascadeType.MERGE,CascadeType.PERSIST,CascadeType.REFRESH})
    @Column(name="patient_id")
    private int patientId;
    @Column(name="date_created")
    private Date dateCreated;
    @Column(name="changed_by")
    private int changedBy;
    @Column(name="date_changed")
    private Date dateChanged;
    @Column(name="last_name")
    private boolean voided;
    @Column(name="voided_by")
    private int voidedBy;
    @Column(name="date_voided")
    private Date dateVoided;
    @Column(name="voided_reason")
    private String voidedReason;
    @OneToOne(cascade={CascadeType.DETACH,CascadeType.MERGE,CascadeType.PERSIST,CascadeType.REFRESH})
    @JoinColumn(name="patient_Id")
    private Patient patient_id;

    public PatientContact() {
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getObsGroupId() {
        return obsGroupId;
    }

    public void setObsGroupId(int obsGroupId) {
        this.obsGroupId = obsGroupId;
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

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
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

    public int getPatientRelatedTo() {
        return patientRelatedTo;
    }

    public void setPatientRelatedTo(int patientRelatedTo) {
        this.patientRelatedTo = patientRelatedTo;
    }

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

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
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
