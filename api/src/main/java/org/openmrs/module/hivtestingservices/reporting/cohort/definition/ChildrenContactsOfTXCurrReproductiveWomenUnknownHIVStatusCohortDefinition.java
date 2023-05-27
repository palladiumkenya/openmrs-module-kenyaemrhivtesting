package org.openmrs.module.hivtestingservices.reporting.cohort.definition;

import org.openmrs.module.hivtestingservices.api.PatientContact;
import org.openmrs.module.hivtestingservices.query.patientContact.definition.PatientContactQuery;
import org.openmrs.module.reporting.common.Localized;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;
import org.openmrs.module.reporting.definition.configuration.ConfigurationPropertyCachingStrategy;
import org.openmrs.module.reporting.evaluation.caching.Caching;
import org.openmrs.module.reporting.query.BaseQuery;

import java.util.Date;

/**
 * Children contacts of TX CURR reproductive women cohort definition
 */
@Caching(strategy = ConfigurationPropertyCachingStrategy.class)
@Localized("reporting.ChildrenContactsOfTXCurrReproductiveWomenUnknownHIVStatusCohortDefinition")
public class ChildrenContactsOfTXCurrReproductiveWomenUnknownHIVStatusCohortDefinition extends BaseQuery<PatientContact> implements PatientContactQuery {

    @ConfigurationProperty
    private Date startDate;

    @ConfigurationProperty
    private Date endDate;

    public ChildrenContactsOfTXCurrReproductiveWomenUnknownHIVStatusCohortDefinition() {
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
