package org.openmrs.module.hivtestingservices.reporting.library;

import org.openmrs.module.kenyacore.report.ReportUtils;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.openmrs.module.hivtestingservices.reporting.EmrReportingUtils.cohortIndicator;
import static org.openmrs.module.kenyacore.report.ReportUtils.map;

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
    public CohortIndicator testedTotal() {
        return cohortIndicator("Total tested clients", ReportUtils.map(pnsReportCohorts.testedTotal(), "startDate=${startDate},endDate=${endDate}"));
    }

    public  CohortIndicator testedUnder1() {
        return cohortIndicator("Under 1s tested", ReportUtils.map(pnsReportCohorts.testedUnderOne(), "startDate=${startDate},endDate=${endDate}"));
    }


    /**
     * Number of patients who are currently on ART
     * @return the indicator
     */
    public CohortIndicator tested1to9() {
        return cohortIndicator("Tested 1 to 9", ReportUtils.map(pnsReportCohorts.tested1to9(), "startDate=${startDate},endDate=${endDate}"));
    }

    /**
     * Number of patients who are ART revisits
     * @return the indicator
     */
    public CohortIndicator tested10to14M() {
        return cohortIndicator("Tested 10-14 Male", ReportUtils.map(pnsReportCohorts.testedClientsByAgeAndGender(10, 14, "M"), "startDate=${startDate},endDate=${endDate}"));
    }

    /**
     * Number of patients who are ART revisits
     * @return the indicator
     */
    public CohortIndicator tested10to14F() {
        return cohortIndicator("Tested 10-14 Female", ReportUtils.map(pnsReportCohorts.testedClientsByAgeAndGender(10, 14, "F"), "startDate=${startDate},endDate=${endDate}"));
    }

    /**
     * Number of patients who are ART revisits
     * @return the indicator
     */
    public CohortIndicator tested15to19M() {
        return cohortIndicator("Tested 15-19 Male", ReportUtils.map(pnsReportCohorts.testedClientsByAgeAndGender(15, 19, "M"), "startDate=${startDate},endDate=${endDate}"));
    }

    /**
     * Number of patients who are ART revisits
     * @return the indicator
     */
    public CohortIndicator tested15to19F() {
        return cohortIndicator("Tested 15-19 Female", ReportUtils.map(pnsReportCohorts.testedClientsByAgeAndGender(15, 19, "F"), "startDate=${startDate},endDate=${endDate}"));
    }

    /**
     * Number of patients who are ART revisits
     * @return the indicator
     */
    public CohortIndicator tested20to24M() {
        return cohortIndicator("Tested 20-24 Male", ReportUtils.map(pnsReportCohorts.testedClientsByAgeAndGender(20, 24, "M"), "startDate=${startDate},endDate=${endDate}"));
    }

    /**
     * Number of patients who are ART revisits
     * @return the indicator
     */
    public CohortIndicator tested20to24F() {
        return cohortIndicator("Tested 20-24 Female", ReportUtils.map(pnsReportCohorts.testedClientsByAgeAndGender(20, 24, "F"), "startDate=${startDate},endDate=${endDate}"));
    }

    /**
     * Number of patients who are ART revisits
     * @return the indicator
     */
    public CohortIndicator tested25to29M() {
        return cohortIndicator("Tested 20-24 Male", ReportUtils.map(pnsReportCohorts.testedClientsByAgeAndGender(20, 24, "M"), "startDate=${startDate},endDate=${endDate}"));
    }

    /**
     * Number of patients who are ART revisits
     * @return the indicator
     */
    public CohortIndicator tested25to29F() {
        return cohortIndicator("Tested 20-24 Female", ReportUtils.map(pnsReportCohorts.testedClientsByAgeAndGender(20, 24, "F"), "startDate=${startDate},endDate=${endDate}"));
    }

}
