package org.openmrs.module.hivtestingservices.reporting.data.patientContact.definition;

import org.openmrs.module.reporting.data.BaseDataDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationPropertyCachingStrategy;
import org.openmrs.module.reporting.evaluation.caching.Caching;

/**
 * Visit ID Column
 */
@Caching(strategy=ConfigurationPropertyCachingStrategy.class)
public class PatientContactOccupationDataDefinition extends BaseDataDefinition implements PatientContactDataDefinition {

    public static final long serialVersionUID = 1L;

    /**
     * Default Constructor
     */
    public PatientContactOccupationDataDefinition() {
        super();
    }

    /**
     * Constructor to populate name only
     */
    public PatientContactOccupationDataDefinition(String name) {
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
