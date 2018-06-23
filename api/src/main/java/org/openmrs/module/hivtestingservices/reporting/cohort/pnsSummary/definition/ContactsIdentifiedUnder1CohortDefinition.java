package org.openmrs.module.hivtestingservices.reporting.cohort.pnsSummary.definition;

import org.openmrs.module.reporting.cohort.definition.BaseCohortDefinition;
import org.openmrs.module.reporting.common.Localized;
import org.openmrs.module.reporting.definition.configuration.ConfigurationPropertyCachingStrategy;
import org.openmrs.module.reporting.evaluation.caching.Caching;

/**
 * RDQA cohort definition
 */
@Caching(strategy = ConfigurationPropertyCachingStrategy.class)
@Localized("reporting.contactsIdentifiedUnder1")
public class ContactsIdentifiedUnder1CohortDefinition extends BaseCohortDefinition {

}
