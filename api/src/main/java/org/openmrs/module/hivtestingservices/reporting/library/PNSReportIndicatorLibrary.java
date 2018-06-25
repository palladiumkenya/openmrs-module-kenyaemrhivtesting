package org.openmrs.module.hivtestingservices.reporting.library;

import org.openmrs.module.kenyacore.report.ReportUtils;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.openmrs.module.hivtestingservices.reporting.EmrReportingUtils.cohortIndicator;

/**
 * Created by dev on 1/14/17.
 */

/**
 * Library of HIV related indicator definitions. All indicators require parameters ${startDate} and ${endDate}
 */
@Component
public class PNSReportIndicatorLibrary {
    @Autowired
    private PNSReportCohortLibrary pnsReportCohorts;

    /**
     * Number of patients currently in care (includes transfers)
     * @return the indicator
     * Indicator	<1 Yr	1-9 Yrs	 10-14 Male	10-14 Female	 15-19 Male	 15-19 Female	20-24 Male	20-24 Female	 25-29 Male 	 25-29 Female 	30-34 Male	30-34 Female	35-39 Male	35-39 Female	40-44 Male	40-44 Female	45-49 Male	45-49 Female	 > 50 Male	 > 50 Female	Total

     */
    public CohortIndicator htsTested() {
        return cohortIndicator("Total tested clients", ReportUtils.map(pnsReportCohorts.htsTested(), "startDate=${startDate},endDate=${endDate}"));
    }


    public CohortIndicator htsNewlyTested() {
        return cohortIndicator("Newly tested clients", ReportUtils.map(pnsReportCohorts.htsNewlyTested(), "startDate=${startDate},endDate=${endDate}"));
    }

    public CohortIndicator htsNewlyTestedWhoReceivedResults() {
        return cohortIndicator("Newly tested clients who received results", ReportUtils.map(pnsReportCohorts.htsNewlyTestedAndReceivedResults(), "startDate=${startDate},endDate=${endDate}"));
    }

    public CohortIndicator htsTotaltestedWhoReceivedResults() {
        return cohortIndicator("Total tested clients who received results", ReportUtils.map(pnsReportCohorts.htsTotalTestedAndReceivedResults(), "startDate=${startDate},endDate=${endDate}"));
    }

    public CohortIndicator htsTotalPositive() {
        return cohortIndicator("Positive clients", ReportUtils.map(pnsReportCohorts.htsTotalPositive(), "startDate=${startDate},endDate=${endDate}"));
    }

    public CohortIndicator htsNewlyPositive() {
        return cohortIndicator("Newly positive clients", ReportUtils.map(pnsReportCohorts.htsNewlyTestedPositiveResult(), "startDate=${startDate},endDate=${endDate}"));
    }

    // ---------------------------------- PNS report ------------------------------

    public CohortIndicator pnsContactsIdentified() {
        return cohortIndicator("Total Contacts identified", ReportUtils.map(pnsReportCohorts.pnsContactsIdentified(), "startDate=${startDate},endDate=${endDate}"));
    }

    public CohortIndicator pnsContactsKnownPositive() {
        return cohortIndicator("Contacts known positive", ReportUtils.map(pnsReportCohorts.pnsKnownPositiveContacts(), "startDate=${startDate},endDate=${endDate}"));
    }

    public CohortIndicator pnsContactsEligibleForTesting() {
        return cohortIndicator("Contacts eligible for testing", ReportUtils.map(pnsReportCohorts.pnsEligibleContacts(), "startDate=${startDate},endDate=${endDate}"));
    }

    public CohortIndicator pnsContactsTested() {
        return cohortIndicator("Contacts tested", ReportUtils.map(pnsReportCohorts.pnsContactsTested(), "startDate=${startDate},endDate=${endDate}"));
    }

    public CohortIndicator pnsContactsNewlyPositive() {
        return cohortIndicator("Contacts newly positive", ReportUtils.map(pnsReportCohorts.pnsContactsNewlyIdentifiedPositive(), "startDate=${startDate},endDate=${endDate}"));
    }

    public CohortIndicator pnsContactsLinkedToHaart() {
        return cohortIndicator("Contacts linked to HAART", ReportUtils.map(pnsReportCohorts.pnsContactsLinkedToHaart(), "startDate=${startDate},endDate=${endDate}"));
    }

    // ---------------------------------- PNS report ------------------------------

    public CohortIndicator familyTestingContactsIdentified() {
        return cohortIndicator("Total Contacts identified", ReportUtils.map(pnsReportCohorts.familyTestingContactsIdentified(), "startDate=${startDate},endDate=${endDate}"));
    }

    public CohortIndicator familyTestingContactsKnownPositive() {
        return cohortIndicator("Contacts known positive", ReportUtils.map(pnsReportCohorts.familyTestingKnownPositiveContacts(), "startDate=${startDate},endDate=${endDate}"));
    }

    public CohortIndicator familyTestingContactsEligibleForTesting() {
        return cohortIndicator("Contacts eligible for testing", ReportUtils.map(pnsReportCohorts.familyTestingEligibleContacts(), "startDate=${startDate},endDate=${endDate}"));
    }

    public CohortIndicator familyTestingContactsTested() {
        return cohortIndicator("Contacts tested", ReportUtils.map(pnsReportCohorts.familyTestingContactsTested(), "startDate=${startDate},endDate=${endDate}"));
    }

    public CohortIndicator familyTestingContactsNewlyPositive() {
        return cohortIndicator("Contacts newly positive", ReportUtils.map(pnsReportCohorts.familyTestingContactsNewlyIdentifiedPositive(), "startDate=${startDate},endDate=${endDate}"));
    }

    public CohortIndicator familyTestingContactsLinkedToHaart() {
        return cohortIndicator("Contacts linked to HAART", ReportUtils.map(pnsReportCohorts.familyTestingContactsLinkedToHaart(), "startDate=${startDate},endDate=${endDate}"));
    }

}
