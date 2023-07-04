package org.openmrs.module.hivtestingservices.reporting.data.patientContact.definition;

import org.openmrs.module.reporting.data.BaseDataDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationPropertyCachingStrategy;
import org.openmrs.module.reporting.evaluation.caching.Caching;

/**
 * Patient contact traced Column
 */
@Caching(strategy=ConfigurationPropertyCachingStrategy.class)
public class PatientContactTracedDataDefinition extends BaseDataDefinition implements PatientContactDataDefinition {

    public static final long serialVersionUID = 1L;

    /**
     * Default Constructor
     */
    public PatientContactTracedDataDefinition() {
        super();
    }

    /**
     * Constructor to populate name only
     */
    public PatientContactTracedDataDefinition(String name) {
        super(name);
    }

    //***** INSTANCE METHODS *****

    /**
     * @see org.openmrs.module.reporting.data.DataDefinition#getDataType()
     */
    public Class<?> getDataType() {
        return String.class;
    }
}
