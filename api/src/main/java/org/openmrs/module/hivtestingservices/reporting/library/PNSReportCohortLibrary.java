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
        String sqlQuery = "select patient_id from (select t.patient_id\n" +
                " from  kenyaemr_etl.etl_hts_test t \n" +
                " inner join kenyaemr_etl.etl_patient_demographics d on d.patient_id=t.patient_id\n" +
                " where t.voided=0 and date(t.visit_date) between date(:startDate) and date(:endDate) and t.test_type=1\n" +
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
        String sqlQuery = "select t.patient_id from  kenyaemr_etl.etl_hts_test t\n" +
                "  inner join kenyaemr_etl.etl_patient_demographics d on d.patient_id=t.patient_id\n" +
                "  where t.voided=0 and t.test_type=1 and ((t.ever_tested_for_hiv = 'Yes' and months_since_last_test >=12) or t.ever_tested_for_hiv = 'No')\n" +
                "                   and date(t.visit_date) between date(:startDate) and date(:endDate)\n" +
                "  group by t.patient_id;";
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
    public CohortDefinition htsNewlyTestedAndReceivedResults(){
        SqlCohortDefinition cd = new SqlCohortDefinition();
        String sqlQuery = "select patient_id from (select t.patient_id, DATE_FORMAT(FROM_DAYS(DATEDIFF(CURDATE(),d.DOB)), '%Y')+0 as age, d.Gender as Gender, min(t.visit_date) as initial_test_date\n" +
                " from  kenyaemr_etl.etl_hts_test t \n" +
                " inner join kenyaemr_etl.etl_patient_demographics d on d.patient_id=t.patient_id\n" +
                " where t.voided=0 and t.patient_given_result='Yes' and t.test_type=1\n" +
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
                " where t.voided=0 and date(t.visit_date) between date(:startDate) and date(:endDate) and t.patient_given_result='Yes' and t.test_type=1\n" +
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
    public CohortDefinition htsTotalPositive(){
        SqlCohortDefinition cd = new SqlCohortDefinition();
        String sqlQuery = "select patient_id from (select t.patient_id, DATE_FORMAT(FROM_DAYS(DATEDIFF(CURDATE(),d.DOB)), '%Y')+0 as age, d.Gender as Gender\n" +
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
        String sqlQuery = "select patient_id from (select t.patient_id, DATE_FORMAT(FROM_DAYS(DATEDIFF(CURDATE(),d.DOB)), '%Y')+0 as age, d.Gender as Gender, min(t.visit_date) as initial_test_date\n" +
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

    // ------------------------------------- PNS Cohorts -------------------------------------------------------
    /**
     * Cohort for contacts identified
     * @return
     */
    public CohortDefinition pnsContactsIdentified(){
        SqlCohortDefinition cd = new SqlCohortDefinition();
        String sqlQuery = "select id from (select c.id\n" +
                " from kenyaemr_hiv_testing_patient_contact c inner join kenyaemr_etl.etl_hts_test t on c.patient_related_to = t.patient_id and t.test_type=2\n" +
                " where t.voided=0 and c.relationship_type in(163565, 5617) and c.voided = 0 and date(t.visit_date) between date(:startDate) and date(:endDate)\n" +
                " group by c.id ) t\n" +
                " ;";
        cd.setName("totalContactsIdentified");
        cd.setQuery(sqlQuery);
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.setDescription("Total tested");

        return cd;
    }

    /**
     * Cohort for known hiv positive contacts
     * @return
     */
    public CohortDefinition pnsKnownPositiveContacts(){
        SqlCohortDefinition cd = new SqlCohortDefinition();
        String sqlQuery = "select id from (select c.id\n" +
                " from kenyaemr_hiv_testing_patient_contact c inner join kenyaemr_etl.etl_hts_test t on c.patient_related_to = t.patient_id and t.test_type=1\n" +
                " where t.voided=0 and c.relationship_type in(163565, 5617) and c.voided = 0 and date(t.visit_date) between date(:startDate) and date(:endDate) and c.baseline_hiv_status='Positive'\n" +
                " group by c.id  ) t\n" +
                " ;";
        cd.setName("knownPositiveContacts");
        cd.setQuery(sqlQuery);
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.setDescription("Total tested");

        return cd;
    }

    /**
     * Cohort for contacts eligible for testing
     * @return
     */
    public CohortDefinition pnsEligibleContacts(){
        SqlCohortDefinition cd = new SqlCohortDefinition();
        String sqlQuery = "select id from (select c.id\n" +
                " from kenyaemr_hiv_testing_patient_contact c inner join kenyaemr_etl.etl_hts_test t on c.patient_related_to = t.patient_id and t.test_type=2\n" +
                " where t.voided=0 and c.relationship_type in(163565, 5617) and c.voided = 0 and date(t.visit_date) between date(:startDate) and date(:endDate) and c.baseline_hiv_status !='Positive'\n" +
                " group by c.id  ) t\n" +
                " ;";
        cd.setName("contactsEligibleForTesting");
        cd.setQuery(sqlQuery);
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.setDescription("Eligible contacts");

        return cd;
    }

    /**
     * Cohort for contacts tested
     * @return
     */
    public CohortDefinition pnsContactsTested(){
        SqlCohortDefinition cd = new SqlCohortDefinition();
        String sqlQuery = "select id from (select c.id\n" +
                " from kenyaemr_hiv_testing_patient_contact c inner join kenyaemr_etl.etl_hts_test t on c.patient_id = t.patient_id\n" +
                " where t.voided=0 and c.voided = 0 and date(t.visit_date) between date(:startDate) and date(:endDate)\n" +
                " group by c.id ) t\n" +
                " ;";
        cd.setName("contactsTested");
        cd.setQuery(sqlQuery);
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.setDescription("Contacts tested");

        return cd;
    }

    /**
     * Cohort for contacts newly identified positives
     * @return
     */
    public CohortDefinition pnsContactsNewlyIdentifiedPositive(){
        SqlCohortDefinition cd = new SqlCohortDefinition();
        String sqlQuery = "select id from (select c.id\n" +
                " from kenyaemr_hiv_testing_patient_contact c inner join kenyaemr_etl.etl_hts_test t on c.patient_id = t.patient_id\n" +
                " where t.voided=0 and t.test_type=2 and c.voided = 0 and date(t.visit_date) between date(:startDate) and date(:endDate) and t.final_test_result = 'Positive' \n" +
                " group by c.id ) t\n" +
                " ;";
        cd.setName("contactsNewlyIdentifiedPositive");
        cd.setQuery(sqlQuery);
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.setDescription("Contacts who tested positive");

        return cd;
    }

    /**
     * Cohort for contacts newly identified positives
     * @return
     */
    public CohortDefinition pnsContactsLinkedToHaart(){
        SqlCohortDefinition cd = new SqlCohortDefinition();
        String sqlQuery = "select id from (select c.id\n" +
                " from kenyaemr_hiv_testing_patient_contact c inner join kenyaemr_etl.etl_hts_test t on c.patient_id = t.patient_id \n" +
                " inner join kenyaemr_etl.etl_hts_referral_and_linkage l on l.patient_id=c.patient_id and l.voided=0 and date(l.visit_date) between date(:startDate) and date(:endDate)\n" +
                " where t.voided=0 and c.voided = 0 and date(t.visit_date) between date(:startDate) and date(:endDate)\n" +
                " group by c.id ) t\n" +
                " ;";
        cd.setName("contactsLinkedToHaart");
        cd.setQuery(sqlQuery);
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.setDescription("Contacts linked to HAART");

        return cd;
    }

    // ----------------------------------------------------------------------------------------------------------

    // ------------------------------------- Family Testing Cohorts -------------------------------------------------------
    /**
     * Cohort for contacts identified
     * @return
     */
    public CohortDefinition familyTestingContactsIdentified(){
        SqlCohortDefinition cd = new SqlCohortDefinition();
        String sqlQuery = "select id from (select c.id\n" +
                " from kenyaemr_hiv_testing_patient_contact c inner join kenyaemr_etl.etl_hts_test t on c.patient_related_to = t.patient_id and t.test_type=2\n" +
                " where t.voided=0 and c.voided = 0 and c.relationship_type in(971, 972, 1528, 162221, 970, 5617) and date(t.visit_date) between date(:startDate) and date(:endDate)\n" +
                " group by c.id ) t\n" +
                " ;";
        cd.setName("totalContactsIdentified");
        cd.setQuery(sqlQuery);
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.setDescription("Total tested");

        return cd;
    }

    /**
     * Cohort for known hiv positive contacts
     * @return
     */
    public CohortDefinition familyTestingKnownPositiveContacts(){
        SqlCohortDefinition cd = new SqlCohortDefinition();
        String sqlQuery = "select id from (select c.id\n" +
                " from kenyaemr_hiv_testing_patient_contact c inner join kenyaemr_etl.etl_hts_test t on c.patient_related_to = t.patient_id and t.test_type=2\n" +
                " where t.voided=0 and c.voided = 0 and c.relationship_type in(971, 972, 1528, 162221, 970, 5617) and date(t.visit_date) between date(:startDate) and date(:endDate) and c.baseline_hiv_status='Positive'\n" +
                " group by c.id  ) t\n" +
                " ;";
        cd.setName("knownPositiveContacts");
        cd.setQuery(sqlQuery);
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.setDescription("Total tested");

        return cd;
    }

    /**
     * Cohort for contacts eligible for testing
     * @return
     */
    public CohortDefinition familyTestingEligibleContacts(){
        SqlCohortDefinition cd = new SqlCohortDefinition();
        String sqlQuery = "select id from (select c.id\n" +
                " from kenyaemr_hiv_testing_patient_contact c inner join kenyaemr_etl.etl_hts_test t on c.patient_related_to = t.patient_id and t.test_type=2\n" +
                " where t.voided=0 and c.voided = 0 and c.relationship_type in(971, 972, 1528, 162221, 970, 5617) and date(t.visit_date) between date(:startDate) and date(:endDate) and c.baseline_hiv_status !='Positive'\n" +
                " group by c.id  ) t\n" +
                " ;";
        cd.setName("contactsEligibleForTesting");
        cd.setQuery(sqlQuery);
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.setDescription("Eligible contacts");

        return cd;
    }

    /**
     * Cohort for contacts tested
     * @return
     */
    public CohortDefinition familyTestingContactsTested(){
        SqlCohortDefinition cd = new SqlCohortDefinition();
        String sqlQuery = "select id from (select c.id\n" +
                " from kenyaemr_hiv_testing_patient_contact c inner join kenyaemr_etl.etl_hts_test t on c.patient_id = t.patient_id\n" +
                " where t.voided=0 and c.voided =0 and c.relationship_type in(971, 972, 1528, 162221, 970, 5617) and date(t.visit_date) between date(:startDate) and date(:endDate)\n" +
                " group by c.id ) t\n" +
                " ;";
        cd.setName("contactsTested");
        cd.setQuery(sqlQuery);
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.setDescription("Contacts tested");

        return cd;
    }

    /**
     * Cohort for contacts newly identified positives
     * @return
     */
    public CohortDefinition familyTestingContactsNewlyIdentifiedPositive(){
        SqlCohortDefinition cd = new SqlCohortDefinition();
        String sqlQuery = "select id from (select c.id\n" +
                " from kenyaemr_hiv_testing_patient_contact c inner join kenyaemr_etl.etl_hts_test t on c.patient_id = t.patient_id\n" +
                " where t.voided=0 and c.voided =0 and c.relationship_type in(971, 972, 1528, 162221, 970, 5617) and t.test_type=2 and date(t.visit_date) between date(:startDate) and date(:endDate) and t.final_test_result = 'Positive' \n" +
                " group by c.id ) t\n" +
                " ;";
        cd.setName("contactsNewlyIdentifiedPositive");
        cd.setQuery(sqlQuery);
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.setDescription("Contacts who tested positive");

        return cd;
    }

    /**
     * Cohort for contacts newly identified positives
     * @return
     */
    public CohortDefinition familyTestingContactsLinkedToHaart(){
        SqlCohortDefinition cd = new SqlCohortDefinition();
        String sqlQuery = "select id from (select c.id\n" +
                " from kenyaemr_hiv_testing_patient_contact c inner join kenyaemr_etl.etl_hts_test t on c.patient_id = t.patient_id \n" +
                " inner join kenyaemr_etl.etl_hts_referral_and_linkage l on l.patient_id=c.patient_id and l.voided=0 and date(l.visit_date) between date(:startDate) and date(:endDate)\n" +
                " where t.voided=0 and c.voided =0 and c.relationship_type in(971, 972, 1528, 162221, 970, 5617) and date(t.visit_date) between date(:startDate) and date(:endDate)\n" +
                " group by c.id ) t\n" +
                " ;";
        cd.setName("contactsLinkedToHaart");
        cd.setQuery(sqlQuery);
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.setDescription("Contacts linked to HAART");

        return cd;
    }



}
