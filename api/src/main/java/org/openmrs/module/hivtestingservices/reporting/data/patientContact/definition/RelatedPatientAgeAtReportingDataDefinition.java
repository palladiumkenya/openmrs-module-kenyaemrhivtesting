package org.openmrs.module.hivtestingservices.reporting.data.patientContact.definition;

import org.openmrs.module.reporting.data.BaseDataDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationPropertyCachingStrategy;
import org.openmrs.module.reporting.evaluation.caching.Caching;

/**
 * Index age at reporting
 */
@Caching(strategy=ConfigurationPropertyCachingStrategy.class)
public class RelatedPatientAgeAtReportingDataDefinition extends BaseDataDefinition implements PatientContactDataDefinition {

    public static final long serialVersionUID = 1L;

    /**
     * Default Constructor
     */
    public RelatedPatientAgeAtReportingDataDefinition() {
        super();
    }

    /**
     * Constructor to populate name only
     */
    public RelatedPatientAgeAtReportingDataDefinition(String name) {
        super(name);
    }

    //***** INSTANCE METHODS *****

    /**
     * @see org.openmrs.module.reporting.data.DataDefinition#getDataType()
     */
    public Class<?> getDataType() {
        return Integer.class;
    }
}
