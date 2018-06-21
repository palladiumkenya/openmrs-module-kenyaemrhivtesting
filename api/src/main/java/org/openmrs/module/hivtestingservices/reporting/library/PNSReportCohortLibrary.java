package org.openmrs.module.hivtestingservices.reporting.library;

import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Created by dev on 1/14/17.
 */

/**
 * Library of cohort definitions used in the PNS report/register
 */
@Component
public class PNSReportCohortLibrary {

    // refined cohorts

    /**
     * Cohort for clients tested
     * @return
     */
    public CohortDefinition htsTested(){
        SqlCohortDefinition cd = new SqlCohortDefinition();
        String sqlQuery = "select patient_id from (select t.patient_id, DATE_FORMAT(FROM_DAYS(DATEDIFF(CURDATE(),d.DOB)), '%Y')+0 as age, d.Gender as Gender\n" +
                " from  kenyaemr_etl.etl_hts_test t \n" +
                " inner join kenyaemr_etl.etl_patient_demographics d on d.patient_id=t.patient_id\n" +
                " where t.voided=0 and date(t.visit_date) between date(:startDate) and date(:endDate) and t.test_type=2\n" +
                " group by t.patient_id) t\n" +
                " ;";
        cd.setName("totalTested");
        cd.setQuery(sqlQuery);
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.setDescription("Total tested");

        return cd;
    }

    /**
     * Cohort for clients newly tested
     * @return
     */
    public CohortDefinition htsNewlyTested(){
        SqlCohortDefinition cd = new SqlCohortDefinition();
        String sqlQuery = "select patient_id from (select t.patient_id, DATE_FORMAT(FROM_DAYS(DATEDIFF(CURDATE(),d.DOB)), '%Y')+0 as age, d.Gender as Gender, min(t.visit_date) as initial_test_date\n" +
                " from  kenyaemr_etl.etl_hts_test t \n" +
                " inner join kenyaemr_etl.etl_patient_demographics d on d.patient_id=t.patient_id\n" +
                " where t.voided=0 and t.test_type=2\n" +
                " group by t.patient_id\n" +
                " having initial_test_date between date(:startDate) and date(:endDate)) t\n" +
                " ;";
        cd.setName("totalTested");
        cd.setQuery(sqlQuery);
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.setDescription("Total tested");

        return cd;
    }

    /**
     * Cohort for clients newly tested and received results
     * @return
     */
    public CohortDefinition htsTestedAndReceivedResults(){
        SqlCohortDefinition cd = new SqlCohortDefinition();
        String sqlQuery = "select patient_id from (select t.patient_id, DATE_FORMAT(FROM_DAYS(DATEDIFF(CURDATE(),d.DOB)), '%Y')+0 as age, d.Gender as Gender, min(t.visit_date) as initial_test_date\n" +
                " from  kenyaemr_etl.etl_hts_test t \n" +
                " inner join kenyaemr_etl.etl_patient_demographics d on d.patient_id=t.patient_id\n" +
                " where t.voided=0 and t.patient_given_result='Yes' and t.test_type=2\n" +
                " group by t.patient_id\n" +
                " having initial_test_date between date(:startDate) and date(:endDate)) t\n" +
                " ;";
        cd.setName("totalTested");
        cd.setQuery(sqlQuery);
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.setDescription("Total tested");

        return cd;
    }

    /**
     * Cohort for clients total tested and received results
     * @return
     */
    public CohortDefinition htsTotalTestedAndReceivedResults(){
        SqlCohortDefinition cd = new SqlCohortDefinition();
        String sqlQuery = "select patient_id from (select t.patient_id, DATE_FORMAT(FROM_DAYS(DATEDIFF(CURDATE(),d.DOB)), '%Y')+0 as age, d.Gender as Gender\n" +
                " from  kenyaemr_etl.etl_hts_test t \n" +
                " inner join kenyaemr_etl.etl_patient_demographics d on d.patient_id=t.patient_id\n" +
                " where t.voided=0 and date(t.visit_date) between date(:startDate) and date(:endDate) and t.patient_given_result='Yes' and t.test_type=2\n" +
                " group by t.patient_id) t\n" +
                " ;";
        cd.setName("totalTested");
        cd.setQuery(sqlQuery);
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.setDescription("Total tested");

        return cd;
    }

    /**
     * Cohort for clients total positive and received results
     * @return
     */
    public CohortDefinition htsTotalPositiveResult(){
        SqlCohortDefinition cd = new SqlCohortDefinition();
        String sqlQuery = "select count(distinct patient_id) from (select t.patient_id, DATE_FORMAT(FROM_DAYS(DATEDIFF(CURDATE(),d.DOB)), '%Y')+0 as age, d.Gender as Gender\n" +
                " from  kenyaemr_etl.etl_hts_test t \n" +
                " inner join kenyaemr_etl.etl_patient_demographics d on d.patient_id=t.patient_id\n" +
                " where t.voided=0 and date(t.visit_date) between date(:startDate) and date(:endDate) and t.test_type=2 and t.final_test_result='Positive'\n" +
                " group by t.patient_id) t\n" +
                " ;";
        cd.setName("totalTested");
        cd.setQuery(sqlQuery);
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.setDescription("Total tested");

        return cd;
    }

    /**
     * Cohort for clients newly tested and positive results
     * @return
     */
    public CohortDefinition htsNewlyTestedPositiveResult(){
        SqlCohortDefinition cd = new SqlCohortDefinition();
        String sqlQuery = "select count(distinct patient_id) from (select t.patient_id, DATE_FORMAT(FROM_DAYS(DATEDIFF(CURDATE(),d.DOB)), '%Y')+0 as age, d.Gender as Gender, min(t.visit_date) as initial_test_date\n" +
                " from  kenyaemr_etl.etl_hts_test t \n" +
                " inner join kenyaemr_etl.etl_patient_demographics d on d.patient_id=t.patient_id\n" +
                " where t.voided=0 and t.patient_given_result='Yes' and t.test_type=2 and t.final_test_result='Positive'\n" +
                " group by t.patient_id\n" +
                " having initial_test_date between date(:startDate) and date(:endDate)) t\n" +
                " ;";
        cd.setName("totalTested");
        cd.setQuery(sqlQuery);
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.setDescription("Total tested");

        return cd;
    }
    // ----------------------------------------------------------------------------------------------------------
    /**
     * Cohort for clients tested
     * @return
     */
    public CohortDefinition testedTotal(){
        SqlCohortDefinition cd = new SqlCohortDefinition();
        String sqlQuery = "select count(distinct patient_id) from (select t.patient_id, DATE_FORMAT(FROM_DAYS(DATEDIFF(CURDATE(),d.DOB)), '%Y')+0 as age, d.Gender as Gender\n" +
                " from  kenyaemr_etl.etl_hts_test t \n" +
                " inner join kenyaemr_etl.etl_patient_demographics d on d.patient_id=t.patient_id\n" +
                " where t.voided=0 and date(t.visit_date) between date(:startDate) and date(:endDate) and t.test_type=2\n" +
                " group by t.patient_id) t\n" +
                " ;";
        cd.setName("totalTested");
        cd.setQuery(sqlQuery);
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.setDescription("Total tested");

        return cd;
    }

    public CohortDefinition testedUnderOne(){
        SqlCohortDefinition cd = new SqlCohortDefinition();
        String sqlQuery = "select count(distinct patient_id) from (select t.patient_id, DATE_FORMAT(FROM_DAYS(DATEDIFF(CURDATE(),d.DOB)), '%Y')+0 as age, d.Gender as Gender\n" +
                " from  kenyaemr_etl.etl_hts_test t \n" +
                " inner join kenyaemr_etl.etl_patient_demographics d on d.patient_id=t.patient_id\n" +
                " where t.voided=0 and date(t.visit_date) between date(:startDate) and date(:endDate) and t.test_type=2\n" +
                " group by t.patient_id\n" +
                " having age = 0) t;";
        cd.setName("testedUnderOne");
        cd.setQuery(sqlQuery);
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.setDescription("Tested under 1");

        return cd;
    }

    public CohortDefinition tested1to9(){
        SqlCohortDefinition cd = new SqlCohortDefinition();
        String sqlQuery = "select count(distinct patient_id) from (select t.patient_id, DATE_FORMAT(FROM_DAYS(DATEDIFF(CURDATE(),d.DOB)), '%Y')+0 as age, d.Gender as Gender\n" +
                " from  kenyaemr_etl.etl_hts_test t \n" +
                " inner join kenyaemr_etl.etl_patient_demographics d on d.patient_id=t.patient_id\n" +
                " where t.voided=0 and date(t.visit_date) between date(:startDate) and date(:endDate) and t.test_type=2\n" +
                " group by t.patient_id\n" +
                " having age between 1 and 9) t;";
        cd.setName("tested1to9");
        cd.setQuery(sqlQuery);
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.setDescription("Tested 1 to 9");

        return cd;
    }

    public CohortDefinition testedAbove50(String gender){
        SqlCohortDefinition cd = new SqlCohortDefinition();
        String sqlQuery = "select count(distinct patient_id) from (select t.patient_id, DATE_FORMAT(FROM_DAYS(DATEDIFF(CURDATE(),d.DOB)), '%Y')+0 as age, d.Gender as Gender\n" +
                " from  kenyaemr_etl.etl_hts_test t \n" +
                " inner join kenyaemr_etl.etl_patient_demographics d on d.patient_id=t.patient_id and d.Gender = ':Gender' \n" +
                " where t.voided=0 and date(t.visit_date) between date(:startDate) and date(:endDate) and t.test_type=2\n" +
                " group by t.patient_id\n" +
                " having age > 50 ) a;";
        sqlQuery.replaceAll(":Gender", gender);
        cd.setName("testedAbove50");
        cd.setQuery(sqlQuery);
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.setDescription("Tested above 50");

        return cd;
    }

    public CohortDefinition testedClientsByAgeAndGender(Integer minAge, Integer maxAge, String gender){
        SqlCohortDefinition cd = new SqlCohortDefinition();
        String sqlQuery = "select count(distinct patient_id) from (select t.patient_id, DATE_FORMAT(FROM_DAYS(DATEDIFF(CURDATE(),d.DOB)), '%Y')+0 as age, d.Gender as Gender\n" +
                " from  kenyaemr_etl.etl_hts_test t \n" +
                " inner join kenyaemr_etl.etl_patient_demographics d on d.patient_id=t.patient_id and d.Gender = ':Gender' \n" +
                " where t.voided=0 and date(t.visit_date) between date(:startDate) and date(:endDate) and t.test_type=2\n" +
                " group by t.patient_id\n" +
                " having age between " + minAge + " and " + maxAge + " ) a;";
        sqlQuery.replaceAll(":Gender", gender);
        cd.setName("testedAbove50");
        cd.setQuery(sqlQuery);
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.setDescription("Tested above 50");

        return cd;
    }


    /**
     * Cohort for clients newly tested
     * @return
     */
    public CohortDefinition newlyTestedTotal(){
        SqlCohortDefinition cd = new SqlCohortDefinition();
        String sqlQuery = "select count(distinct patient_id) from (select t.patient_id, DATE_FORMAT(FROM_DAYS(DATEDIFF(CURDATE(),d.DOB)), '%Y')+0 as age, d.Gender as Gender, min(t.visit_date) as initial_test_date\n" +
                " from  kenyaemr_etl.etl_hts_test t \n" +
                " inner join kenyaemr_etl.etl_patient_demographics d on d.patient_id=t.patient_id\n" +
                " where t.voided=0 and t.test_type=2\n" +
                " group by t.patient_id\n" +
                " having initial_test_date between date(:startDate) and date(:endDate) and age between :minAge and :maxAge) t\n" +
                " ;";
        cd.setName("totalTested");
        cd.setQuery(sqlQuery);
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.setDescription("Total tested");

        return cd;
    }

    public CohortDefinition newlyTestedUnderOne(){
        SqlCohortDefinition cd = new SqlCohortDefinition();
        String sqlQuery = "select count(distinct patient_id) from (select t.patient_id, DATE_FORMAT(FROM_DAYS(DATEDIFF(CURDATE(),d.DOB)), '%Y')+0 as age, d.Gender as Gender, min(t.visit_date) as initial_test_date\n" +
                " from  kenyaemr_etl.etl_hts_test t \n" +
                " inner join kenyaemr_etl.etl_patient_demographics d on d.patient_id=t.patient_id\n" +
                " where t.voided=0 and t.test_type=2\n" +
                " group by t.patient_id\n" +
                " having initial_test_date between date(:startDate) and date(:endDate) and age between :minAge and :maxAge) t;";
        cd.setName("testedUnderOne");
        cd.setQuery(sqlQuery);
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.setDescription("Tested under 1");

        return cd;
    }

    public CohortDefinition newlyTested1to9(){
        SqlCohortDefinition cd = new SqlCohortDefinition();
        String sqlQuery = "select count(distinct patient_id) from (select t.patient_id, DATE_FORMAT(FROM_DAYS(DATEDIFF(CURDATE(),d.DOB)), '%Y')+0 as age, d.Gender as Gender, min(t.visit_date) as initial_test_date\n" +
                " from  kenyaemr_etl.etl_hts_test t \n" +
                " inner join kenyaemr_etl.etl_patient_demographics d on d.patient_id=t.patient_id\n" +
                " where t.voided=0 and t.test_type=2\n" +
                " group by t.patient_id\n" +
                " having initial_test_date between date(:startDate) and date(:endDate) and age between :minAge and :maxAge) t;";
        cd.setName("tested1to9");
        cd.setQuery(sqlQuery);
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.setDescription("Tested 1 to 9");

        return cd;
    }

    public CohortDefinition newlyTestedAbove50(String gender){
        SqlCohortDefinition cd = new SqlCohortDefinition();
        String sqlQuery = "select count(distinct patient_id) from (select t.patient_id, DATE_FORMAT(FROM_DAYS(DATEDIFF(CURDATE(),d.DOB)), '%Y')+0 as age, d.Gender as Gender, min(t.visit_date) as initial_test_date\n" +
                " from  kenyaemr_etl.etl_hts_test t \n" +
                " inner join kenyaemr_etl.etl_patient_demographics d on d.patient_id=t.patient_id and d.Gender = ':Gender'\n" +
                " where t.voided=0 and t.test_type=2\n" +
                " group by t.patient_id\n" +
                " having initial_test_date between date(:startDate) and date(:endDate) and age between :minAge and :maxAge ) a;";
        sqlQuery.replaceAll(":Gender", gender);
        cd.setName("testedAbove50");
        cd.setQuery(sqlQuery);
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.setDescription("Tested above 50");

        return cd;
    }

    public CohortDefinition newlyTestedClientsByAgeAndGender(Integer minAge, Integer maxAge, String gender){
        SqlCohortDefinition cd = new SqlCohortDefinition();
        String sqlQuery = "select count(distinct patient_id) from (select t.patient_id, DATE_FORMAT(FROM_DAYS(DATEDIFF(CURDATE(),d.DOB)), '%Y')+0 as age, d.Gender as Gender, min(t.visit_date) as initial_test_date\n" +
                " from  kenyaemr_etl.etl_hts_test t \n" +
                " inner join kenyaemr_etl.etl_patient_demographics d on d.patient_id=t.patient_id and d.Gender = ':Gender'\n" +
                " where t.voided=0 and t.test_type=2\n" +
                " group by t.patient_id\n" +
                " having initial_test_date between date(:startDate) and date(:endDate) and age between " + minAge + " and " + maxAge + " ) a;";
        sqlQuery.replaceAll(":Gender", gender);
        cd.setName("testedAbove50");
        cd.setQuery(sqlQuery);
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.setDescription("Tested above 50");

        return cd;
    }

    /**
     * Cohort for clients newly tested and received results
     * @return
     */
    public CohortDefinition newlyTestedReceivedResultsTotal(){
        SqlCohortDefinition cd = new SqlCohortDefinition();
        String sqlQuery = "select count(distinct patient_id) from (select t.patient_id, DATE_FORMAT(FROM_DAYS(DATEDIFF(CURDATE(),d.DOB)), '%Y')+0 as age, d.Gender as Gender, min(t.visit_date) as initial_test_date\n" +
                " from  kenyaemr_etl.etl_hts_test t \n" +
                " inner join kenyaemr_etl.etl_patient_demographics d on d.patient_id=t.patient_id\n" +
                " where t.voided=0 and t.test_type=2 and t.patient_given_result='Yes'\n" +
                " group by t.patient_id\n" +
                " having initial_test_date between date(:startDate) and date(:endDate) and age between :minAge and :maxAge) t\n" +
                " ;";
        cd.setName("totalTested");
        cd.setQuery(sqlQuery);
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.setDescription("Total tested");

        return cd;
    }

    public CohortDefinition newlyTestedReceivedResultsUnderOne(){
        SqlCohortDefinition cd = new SqlCohortDefinition();
        String sqlQuery = "select count(distinct patient_id) from (select t.patient_id, DATE_FORMAT(FROM_DAYS(DATEDIFF(CURDATE(),d.DOB)), '%Y')+0 as age, d.Gender as Gender, min(t.visit_date) as initial_test_date\n" +
                " from  kenyaemr_etl.etl_hts_test t \n" +
                " inner join kenyaemr_etl.etl_patient_demographics d on d.patient_id=t.patient_id\n" +
                " where t.voided=0 and t.test_type=2 and t.patient_given_result='Yes'\n" +
                " group by t.patient_id\n" +
                " having initial_test_date between date(:startDate) and date(:endDate) and age between :minAge and :maxAge) t;";
        cd.setName("testedUnderOne");
        cd.setQuery(sqlQuery);
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.setDescription("Tested under 1");

        return cd;
    }

    public CohortDefinition newlyTestedReceivedResults1to9(){
        SqlCohortDefinition cd = new SqlCohortDefinition();
        String sqlQuery = "select count(distinct patient_id) from (select t.patient_id, DATE_FORMAT(FROM_DAYS(DATEDIFF(CURDATE(),d.DOB)), '%Y')+0 as age, d.Gender as Gender, min(t.visit_date) as initial_test_date\n" +
                " from  kenyaemr_etl.etl_hts_test t \n" +
                " inner join kenyaemr_etl.etl_patient_demographics d on d.patient_id=t.patient_id\n" +
                " where t.voided=0 and t.test_type=2 and t.patient_given_result='Yes'\n" +
                " group by t.patient_id\n" +
                " having initial_test_date between date(:startDate) and date(:endDate) and age between :minAge and :maxAge) t;";
        cd.setName("tested1to9");
        cd.setQuery(sqlQuery);
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.setDescription("Tested 1 to 9");

        return cd;
    }

    public CohortDefinition newlyTestedReceivedResultsAbove50(String gender){
        SqlCohortDefinition cd = new SqlCohortDefinition();
        String sqlQuery = "select count(distinct patient_id) from (select t.patient_id, DATE_FORMAT(FROM_DAYS(DATEDIFF(CURDATE(),d.DOB)), '%Y')+0 as age, d.Gender as Gender, min(t.visit_date) as initial_test_date\n" +
                " from  kenyaemr_etl.etl_hts_test t \n" +
                " inner join kenyaemr_etl.etl_patient_demographics d on d.patient_id=t.patient_id and d.Gender = ':Gender'\n" +
                " where t.voided=0 and t.test_type=2 and t.patient_given_result='Yes'\n" +
                " group by t.patient_id\n" +
                " having initial_test_date between date(:startDate) and date(:endDate) and age between :minAge and :maxAge ) a;";
        sqlQuery.replaceAll(":Gender", gender);
        cd.setName("testedAbove50");
        cd.setQuery(sqlQuery);
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.setDescription("Tested above 50");

        return cd;
    }

    public CohortDefinition newlyTestedReceivedResultsClientsByAgeAndGender(Integer minAge, Integer maxAge, String gender){
        SqlCohortDefinition cd = new SqlCohortDefinition();
        String sqlQuery = "select count(distinct patient_id) from (select t.patient_id, DATE_FORMAT(FROM_DAYS(DATEDIFF(CURDATE(),d.DOB)), '%Y')+0 as age, d.Gender as Gender, min(t.visit_date) as initial_test_date\n" +
                " from  kenyaemr_etl.etl_hts_test t \n" +
                " inner join kenyaemr_etl.etl_patient_demographics d on d.patient_id=t.patient_id and d.Gender = ':Gender'\n" +
                " where t.voided=0 and t.test_type=2 and t.patient_given_result='Yes'\n" +
                " group by t.patient_id\n" +
                " having initial_test_date between date(:startDate) and date(:endDate) and age between " + minAge + " and " + maxAge + " ) a;";
        sqlQuery.replaceAll(":Gender", gender);
        cd.setName("testedAbove50");
        cd.setQuery(sqlQuery);
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.setDescription("Tested above 50");

        return cd;
    }

    /**
     * Cohort for clients tested and received results
     * @return
     */
    public CohortDefinition totalTestedReceivedResultsTotal(){
        SqlCohortDefinition cd = new SqlCohortDefinition();
        String sqlQuery = "select count(distinct patient_id) from (select t.patient_id, DATE_FORMAT(FROM_DAYS(DATEDIFF(CURDATE(),d.DOB)), '%Y')+0 as age, d.Gender as Gender\n" +
                " from  kenyaemr_etl.etl_hts_test t \n" +
                " inner join kenyaemr_etl.etl_patient_demographics d on d.patient_id=t.patient_id\n" +
                " where t.voided=0 and date(t.visit_date) between date(:startDate) and date(:endDate) and t.patient_given_result='Yes' and t.test_type=2\n" +
                " group by t.patient_id) t\n" +
                " ;";
        cd.setName("totalTested");
        cd.setQuery(sqlQuery);
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.setDescription("Total tested");

        return cd;
    }

    public CohortDefinition totalTestedReceivedResultsUnderOne(){
        SqlCohortDefinition cd = new SqlCohortDefinition();
        String sqlQuery = "select count(distinct patient_id) from (select t.patient_id, DATE_FORMAT(FROM_DAYS(DATEDIFF(CURDATE(),d.DOB)), '%Y')+0 as age, d.Gender as Gender\n" +
                " from  kenyaemr_etl.etl_hts_test t \n" +
                " inner join kenyaemr_etl.etl_patient_demographics d on d.patient_id=t.patient_id\n" +
                " where t.voided=0 and date(t.visit_date) between date(:startDate) and date(:endDate) and t.patient_given_result='Yes' and t.test_type=2\n" +
                " group by t.patient_id having age < 1) t;";
        cd.setName("testedUnderOne");
        cd.setQuery(sqlQuery);
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.setDescription("Tested under 1");

        return cd;
    }

    public CohortDefinition totalTestedReceivedResults1to9(){
        SqlCohortDefinition cd = new SqlCohortDefinition();
        String sqlQuery = "select count(distinct patient_id) from (select t.patient_id, DATE_FORMAT(FROM_DAYS(DATEDIFF(CURDATE(),d.DOB)), '%Y')+0 as age, d.Gender as Gender\n" +
                " from  kenyaemr_etl.etl_hts_test t \n" +
                " inner join kenyaemr_etl.etl_patient_demographics d on d.patient_id=t.patient_id\n" +
                " where t.voided=0 and date(t.visit_date) between date(:startDate) and date(:endDate) and t.patient_given_result='Yes' and t.test_type=2\n" +
                " group by t.patient_id having age between 1 and 9) t;";
        cd.setName("tested1to9");
        cd.setQuery(sqlQuery);
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.setDescription("Tested 1 to 9");

        return cd;
    }

    public CohortDefinition totalTestedReceivedResultsAbove50(String gender){
        SqlCohortDefinition cd = new SqlCohortDefinition();
        String sqlQuery = "select count(distinct patient_id) from (select t.patient_id, DATE_FORMAT(FROM_DAYS(DATEDIFF(CURDATE(),d.DOB)), '%Y')+0 as age, d.Gender as Gender\n" +
                " from  kenyaemr_etl.etl_hts_test t \n" +
                " inner join kenyaemr_etl.etl_patient_demographics d on d.patient_id=t.patient_id and d.Gender = ':Gender'\n" +
                " where t.voided=0 and date(t.visit_date) between date(:startDate) and date(:endDate) and t.patient_given_result='Yes' and t.test_type=2\n" +
                " group by t.patient_id having age > 50 ) a;";
        sqlQuery.replaceAll(":Gender", gender);
        cd.setName("testedAbove50");
        cd.setQuery(sqlQuery);
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.setDescription("Tested above 50");

        return cd;
    }

    public CohortDefinition totalTestedReceivedResultsClientsByAgeAndGender(Integer minAge, Integer maxAge, String gender){
        SqlCohortDefinition cd = new SqlCohortDefinition();
        String sqlQuery = "select count(distinct patient_id) from (select t.patient_id, DATE_FORMAT(FROM_DAYS(DATEDIFF(CURDATE(),d.DOB)), '%Y')+0 as age, d.Gender as Gender\n" +
                " from  kenyaemr_etl.etl_hts_test t \n" +
                " inner join kenyaemr_etl.etl_patient_demographics d on d.patient_id=t.patient_id and d.Gender = ':Gender'\n" +
                " where t.voided=0 and date(t.visit_date) between date(:startDate) and date(:endDate) and t.patient_given_result='Yes' and t.test_type=2\n" +
                " group by t.patient_id\n" +
                " having age between " + minAge + " and " + maxAge + " ) a;";
        sqlQuery.replaceAll(":Gender", gender);
        cd.setName("testedAbove50");
        cd.setQuery(sqlQuery);
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.setDescription("Tested above 50");

        return cd;
    }

    /**
     * Cohort for clients tested with positive results
     * @return
     */
    public CohortDefinition testedPositiveResultTotal(){
        SqlCohortDefinition cd = new SqlCohortDefinition();
        String sqlQuery = "select count(distinct patient_id) from (select t.patient_id, DATE_FORMAT(FROM_DAYS(DATEDIFF(CURDATE(),d.DOB)), '%Y')+0 as age, d.Gender as Gender\n" +
                " from  kenyaemr_etl.etl_hts_test t \n" +
                " inner join kenyaemr_etl.etl_patient_demographics d on d.patient_id=t.patient_id\n" +
                " where t.voided=0 and date(t.visit_date) between date(:startDate) and date(:endDate) and t.test_type=2 and t.final_test_result='Positive'\n" +
                " group by t.patient_id) t\n" +
                " ;";
        cd.setName("totalTested");
        cd.setQuery(sqlQuery);
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.setDescription("Total tested");

        return cd;
    }

    public CohortDefinition testedPositiveResultUnderOne(){
        SqlCohortDefinition cd = new SqlCohortDefinition();
        String sqlQuery = "select count(distinct patient_id) from (select t.patient_id, DATE_FORMAT(FROM_DAYS(DATEDIFF(CURDATE(),d.DOB)), '%Y')+0 as age, d.Gender as Gender\n" +
                " from  kenyaemr_etl.etl_hts_test t \n" +
                " inner join kenyaemr_etl.etl_patient_demographics d on d.patient_id=t.patient_id\n" +
                " where t.voided=0 and date(t.visit_date) between date(:startDate) and date(:endDate) and t.test_type=2 and t.final_test_result='Positive'\n" +
                " group by t.patient_id having age < 1) t;";
        cd.setName("testedUnderOne");
        cd.setQuery(sqlQuery);
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.setDescription("Tested under 1");

        return cd;
    }

    public CohortDefinition testedPositiveResults1to9(){
        SqlCohortDefinition cd = new SqlCohortDefinition();
        String sqlQuery = "select count(distinct patient_id) from (select t.patient_id, DATE_FORMAT(FROM_DAYS(DATEDIFF(CURDATE(),d.DOB)), '%Y')+0 as age, d.Gender as Gender\n" +
                " from  kenyaemr_etl.etl_hts_test t \n" +
                " inner join kenyaemr_etl.etl_patient_demographics d on d.patient_id=t.patient_id\n" +
                " where t.voided=0 and date(t.visit_date) between date(:startDate) and date(:endDate) and t.test_type=2 and t.final_test_result='Positive'\n" +
                " group by t.patient_id having age between 1 and 9) t;";
        cd.setName("tested1to9");
        cd.setQuery(sqlQuery);
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.setDescription("Tested 1 to 9");

        return cd;
    }

    public CohortDefinition testedPositiveResultsAbove50(String gender){
        SqlCohortDefinition cd = new SqlCohortDefinition();
        String sqlQuery = "select count(distinct patient_id) from (select t.patient_id, DATE_FORMAT(FROM_DAYS(DATEDIFF(CURDATE(),d.DOB)), '%Y')+0 as age, d.Gender as Gender\n" +
                " from  kenyaemr_etl.etl_hts_test t \n" +
                " inner join kenyaemr_etl.etl_patient_demographics d on d.patient_id=t.patient_id and d.Gender = ':Gender'\n" +
                " where t.voided=0 and date(t.visit_date) between date(:startDate) and date(:endDate) and t.test_type=2 and t.final_test_result='Positive'\n" +
                " group by t.patient_id having age > 50 ) a;";
        sqlQuery.replaceAll(":Gender", gender);
        cd.setName("testedAbove50");
        cd.setQuery(sqlQuery);
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.setDescription("Tested above 50");

        return cd;
    }

    public CohortDefinition testedPositiveResultClientsByAgeAndGender(Integer minAge, Integer maxAge, String gender){
        SqlCohortDefinition cd = new SqlCohortDefinition();
        String sqlQuery = "select count(distinct patient_id) from (select t.patient_id, DATE_FORMAT(FROM_DAYS(DATEDIFF(CURDATE(),d.DOB)), '%Y')+0 as age, d.Gender as Gender\n" +
                " from  kenyaemr_etl.etl_hts_test t \n" +
                " inner join kenyaemr_etl.etl_patient_demographics d on d.patient_id=t.patient_id and d.Gender = ':Gender'\n" +
                " where t.voided=0 and date(t.visit_date) between date(:startDate) and date(:endDate) and t.test_type=2 and t.final_test_result='Positive'\n" +
                " group by t.patient_id\n" +
                " having age between " + minAge + " and " + maxAge + " ) a;";
        sqlQuery.replaceAll(":Gender", gender);
        cd.setName("testedAbove50");
        cd.setQuery(sqlQuery);
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.setDescription("Tested above 50");

        return cd;
    }

    /**
     * Cohort for clients tested with positive results
     * @return
     */
    public CohortDefinition newlyTestedPositiveResultTotal(){
        SqlCohortDefinition cd = new SqlCohortDefinition();
        String sqlQuery = "select count(distinct patient_id) from (select t.patient_id, DATE_FORMAT(FROM_DAYS(DATEDIFF(CURDATE(),d.DOB)), '%Y')+0 as age, d.Gender as Gender, min(t.visit_date) as initial_test_date\n" +
                " from  kenyaemr_etl.etl_hts_test t \n" +
                " inner join kenyaemr_etl.etl_patient_demographics d on d.patient_id=t.patient_id\n" +
                " where t.voided=0 and t.patient_given_result='Yes' and t.test_type=2 and t.final_test_result='Positive'\n" +
                " group by t.patient_id initial_test_date between date(:startDate) and date(:endDate)) t\n" +
                " ;";
        cd.setName("totalTested");
        cd.setQuery(sqlQuery);
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.setDescription("Total tested");

        return cd;
    }

    public CohortDefinition newlyTestedPositiveResultUnderOne(){
        SqlCohortDefinition cd = new SqlCohortDefinition();
        String sqlQuery = "select count(distinct patient_id) from (select t.patient_id, DATE_FORMAT(FROM_DAYS(DATEDIFF(CURDATE(),d.DOB)), '%Y')+0 as age, d.Gender as Gender, min(t.visit_date) as initial_test_date\n" +
                " from  kenyaemr_etl.etl_hts_test t \n" +
                " inner join kenyaemr_etl.etl_patient_demographics d on d.patient_id=t.patient_id\n" +
                " where t.voided=0 and t.patient_given_result='Yes' and t.test_type=2 and t.final_test_result='Positive'\n" +
                " group by t.patient_id having initial_test_date between date(:startDate) and date(:endDate) and age < 1) t;";
        cd.setName("testedUnderOne");
        cd.setQuery(sqlQuery);
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.setDescription("Tested under 1");

        return cd;
    }

    public CohortDefinition newlyTestedPositiveResults1to9(){
        SqlCohortDefinition cd = new SqlCohortDefinition();
        String sqlQuery = "select count(distinct patient_id) from (select t.patient_id, DATE_FORMAT(FROM_DAYS(DATEDIFF(CURDATE(),d.DOB)), '%Y')+0 as age, d.Gender as Gender, min(t.visit_date) as initial_test_date\n" +
                " from  kenyaemr_etl.etl_hts_test t \n" +
                " inner join kenyaemr_etl.etl_patient_demographics d on d.patient_id=t.patient_id\n" +
                " where t.voided=0 and t.patient_given_result='Yes' and t.test_type=2 and t.final_test_result='Positive'\n" +
                " group by t.patient_id having initial_test_date between date(:startDate) and date(:endDate) and age between 1 and 9) t;";
        cd.setName("tested1to9");
        cd.setQuery(sqlQuery);
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.setDescription("Tested 1 to 9");

        return cd;
    }

    public CohortDefinition newlyTestedPositiveResultsAbove50(String gender){
        SqlCohortDefinition cd = new SqlCohortDefinition();
        String sqlQuery = "select count(distinct patient_id) from (select t.patient_id, DATE_FORMAT(FROM_DAYS(DATEDIFF(CURDATE(),d.DOB)), '%Y')+0 as age, d.Gender as Gender, min(t.visit_date) as initial_test_date\n" +
                " from  kenyaemr_etl.etl_hts_test t \n" +
                " inner join kenyaemr_etl.etl_patient_demographics d on d.patient_id=t.patient_id and d.Gender = ':Gender'\n" +
                " where t.voided=0 and t.patient_given_result='Yes' and t.test_type=2 and t.final_test_result='Positive'\n" +
                " group by t.patient_id having initial_test_date between date(:startDate) and date(:endDate) and age > 50 ) a;";
        sqlQuery.replaceAll(":Gender", gender);
        cd.setName("testedAbove50");
        cd.setQuery(sqlQuery);
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.setDescription("Tested above 50");

        return cd;
    }

    public CohortDefinition newlyTestedPositiveResultClientsByAgeAndGender(Integer minAge, Integer maxAge, String gender){
        SqlCohortDefinition cd = new SqlCohortDefinition();
        String sqlQuery = "select count(distinct patient_id) from (select t.patient_id, DATE_FORMAT(FROM_DAYS(DATEDIFF(CURDATE(),d.DOB)), '%Y')+0 as age, d.Gender as Gender, min(t.visit_date) as initial_test_date\n" +
                " from  kenyaemr_etl.etl_hts_test t \n" +
                " inner join kenyaemr_etl.etl_patient_demographics d on d.patient_id=t.patient_id and d.Gender = ':Gender'\n" +
                " where t.voided=0 and t.patient_given_result='Yes' and t.test_type=2 and t.final_test_result='Positive'\n" +
                " group by t.patient_id\n" +
                " having initial_test_date between date(:startDate) and date(:endDate) and age between " + minAge + " and " + maxAge + " ) a;";
        sqlQuery.replaceAll(":Gender", gender);
        cd.setName("testedAbove50");
        cd.setQuery(sqlQuery);
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.setDescription("Tested above 50");

        return cd;
    }
}
