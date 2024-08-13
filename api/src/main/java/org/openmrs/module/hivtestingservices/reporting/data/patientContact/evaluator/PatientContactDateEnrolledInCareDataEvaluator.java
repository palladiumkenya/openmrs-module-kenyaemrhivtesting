package org.openmrs.module.hivtestingservices.reporting.data.patientContact.evaluator;

import org.openmrs.annotation.Handler;
import org.openmrs.module.hivtestingservices.reporting.data.patientContact.EvaluatedPatientContactData;
import org.openmrs.module.hivtestingservices.reporting.data.patientContact.definition.PatientContactDataDefinition;
import org.openmrs.module.hivtestingservices.reporting.data.patientContact.definition.PatientContactDateEnrolledToCareDataDefinition;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.Map;

/**
 * Evaluates PatientContactDateEnrolledToCareDataDefinition
 */
@Handler(supports = PatientContactDateEnrolledToCareDataDefinition.class, order = 50)
public class PatientContactDateEnrolledInCareDataEvaluator implements PatientContactDataEvaluator {

    @Autowired
    private EvaluationService evaluationService;

    public EvaluatedPatientContactData evaluate(PatientContactDataDefinition definition, EvaluationContext context) throws EvaluationException {
        EvaluatedPatientContactData c = new EvaluatedPatientContactData(definition, context);

        String qry = "select c.patient_id,\n" +
                "                  if(coalesce(t.test_results,\n" +
                "                              c.baseline_hiv_status) = 'Positive' and l.ccc_number is not null, l.date_enrolled,\n" +
                "                     null) as date_enrolled_in_hiv_program\n" +
                "           from kenyaemr_etl.etl_patient_contact c\n" +
                "                    left join (select r.client_id                                                     as client_id,\n" +
                "                                      mid(max(concat(date(r.date_created), r.unique_patient_no)), 11) as unique_patient_no\n" +
                "                               from kenyaemr_etl.etl_client_trace r\n" +
                "                               where DATE(r.date_created) <= date(CURRENT_DATE)\n" +
                "                               group by r.client_id) r on c.patient_id = r.client_id\n" +
                "                    left join (select t.patient_id,\n" +
                "                                      max(date(t.visit_date))                                       as date_tested,\n" +
                "                                      mid(max(concat(date(t.visit_date), t.final_test_result)), 11) as test_results\n" +
                "                               from kenyaemr_etl.etl_hts_test t\n" +
                "                               where date(t.visit_date) <= date(CURRENT_DATE)\n" +
                "                               group by t.patient_id) t on c.patient_id = t.patient_id\n" +
                "                    left join (select p.patient_id as patient_id,\n" +
                "                                      coalesce(p.unique_patient_no, l.ccc_number)                 as ccc_number,\n" +
                "                                      coalesce(e.enrollment_date, l.date_enrolled_other_facility) as date_enrolled\n" +
                "                               from kenyaemr_etl.etl_patient_demographics p\n" +
                "                                        left join (select l.patient_id,\n" +
                "                                                          mid(max(concat(date(l.visit_date), date(l.enrollment_date))),\n" +
                "                                                              11) as date_enrolled_other_facility,\n" +
                "                                                          mid(max(concat(date(l.visit_date), l.ccc_number)),\n" +
                "                                                              11) as ccc_number\n" +
                "                                                   from kenyaemr_etl.etl_hts_referral_and_linkage l\n" +
                "                                                   where date(l.visit_date) <= date(CURRENT_DATE)\n" +
                "                                                   group by l.patient_id) l on p.patient_id = l.patient_id\n" +
                "                                        left join (select e.patient_id, max(date(visit_date)) as enrollment_date\n" +
                "                                                   from kenyaemr_etl.etl_hiv_enrollment e\n" +
                "                                                   where date(e.visit_date) <= date(CURRENT_DATE)\n" +
                "                                                   group by e.patient_id) e on p.patient_id = e.patient_id\n" +
                "                               group by p.patient_id) l on c.patient_id = l.patient_id\n" +
                "           where date(c.date_created) <= date(:endDate) and c.voided = 0;";

        SqlQueryBuilder queryBuilder = new SqlQueryBuilder();
        queryBuilder.append(qry);
        Date endDate = (Date)context.getParameterValue("endDate");
        queryBuilder.addParameter("endDate", endDate);
        Map<Integer, Object> data = evaluationService.evaluateToMap(queryBuilder, Integer.class, Object.class, context);
        c.setData(data);
        return c;
    }
}
