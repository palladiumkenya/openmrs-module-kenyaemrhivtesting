package org.openmrs.module.hivtestingservices.api.service;

import org.openmrs.BaseOpenmrsData;
import org.openmrs.Location;
import org.openmrs.Provider;
import org.openmrs.module.hivtestingservices.model.DataSource;

import java.util.UUID;

public class MedicQueData extends BaseOpenmrsData {

    private Integer id;
    private String uuid;
    private Location location;
    private Provider provider;
    private String discriminator;
    private DataSource dataSource;
    private String payload;
    private String formName;
    private String patientUuid;
    private String formDataUuid;

    public MedicQueData() {
        prePersist();
    }

    public MedicQueData(String discriminator, String payload,
                        String formName, String patientUuid, String formDataUuid, DataSource dataSource, Location location, Provider provider) {
        this.location = location;
        this.discriminator = discriminator;
        this.dataSource = dataSource;
        this.payload = payload;
        this.provider = provider;
        this.formName = formName;
        this.patientUuid = patientUuid;
        this.formDataUuid = formDataUuid;
    }

    public void prePersist() {

        if (null == getUuid())
            setUuid(UUID.randomUUID().toString());
    }
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public String getDiscriminator() {
        return discriminator;
    }

    public void setDiscriminator(String discriminator) { this.discriminator = discriminator;  }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) { this.payload = payload; }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public String getFormName() {
        return formName;
    }

    public void setFormName(String formName) {
        this.formName = formName;
    }

    public String getPatientUuid() {
        return patientUuid;
    }

    public void setPatientUuid(String patientUuid) {
        this.patientUuid = patientUuid;
    }
    public String getFormDataUuid() {
        return formDataUuid;
    }

    public void setFormDataUuid(String formDataUuid) {
        this.formDataUuid = formDataUuid;
    }

}


