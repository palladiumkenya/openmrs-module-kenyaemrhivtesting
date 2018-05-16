package org.openmrs.module.hivtestingservices.advice.model;

import org.openmrs.BaseOpenmrsData;

import java.util.Date;

public class AOPEncounterEntry extends BaseOpenmrsData {

    private Integer id;
    private String encounterUUID;
    private String formUUID;
    private String targetModule;
    private Integer status;
    private Date dateCreated;

    public AOPEncounterEntry() {
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public String getEncounterUUID() {
        return encounterUUID;
    }

    public void setEncounterUUID(String encounterUUID) {
        this.encounterUUID = encounterUUID;
    }

    public String getFormUUID() {
        return formUUID;
    }

    public void setFormUUID(String formUUID) {
        this.formUUID = formUUID;
    }

    public String getTargetModule() {
        return targetModule;
    }

    public void setTargetModule(String targetModule) {
        this.targetModule = targetModule;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public Date getDateCreated() {
        return dateCreated;
    }

    @Override
    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }
}
