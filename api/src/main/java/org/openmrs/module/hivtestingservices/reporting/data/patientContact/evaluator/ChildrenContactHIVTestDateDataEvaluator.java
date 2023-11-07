package org.openmrs.module.hivtestingservices.reporting.data.patientContact.evaluator;

import org.openmrs.annotation.Handler;
import org.openmrs.module.hivtestingservices.reporting.data.patientContact.EvaluatedPatientContactData;
import org.openmrs.module.hivtestingservices.reporting.data.patientContact.definition.ChildrenContactHIVTestDateDataDefinition;
import org.openmrs.module.hivtestingservices.reporting.data.patientContact.definition.PatientContactDataDefinition;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.Map;

/**
 * Evaluates ChildrenContactHIVTestDateDataDefinition
 */
@Handler(supports= ChildrenContactHIVTestDateDataDefinition.class, order=50)
public class ChildrenContactHIVTestDateDataEvaluator implements PatientContactDataEvaluator {

    @Autowired
    private EvaluationService evaluationService;

    public EvaluatedPatientContactData evaluate(PatientContactDataDefinition definition, EvaluationContext context) throws EvaluationException {
        EvaluatedPatientContactData c = new EvaluatedPatientContactData(definition, context);

        String qry = "select c.id,\n" +
                "       greatest(ifnull(left(max(concat(t.date_tested, t.latest_hts_test_results)), 10), ''),\n" +
                "                ifnull(left(max(concat(l.lab_date, l.latest_lab_test_results)), 10), ''),\n" +
                "                ifnull(left(max(concat(\n" +
                "                        date(cast(c.reported_test_date AS CHAR CHARACTER SET utf8) COLLATE utf8_unicode_ci),\n" +
                "                        cast(c.baseline_hiv_status AS CHAR CHARACTER SET utf8) COLLATE utf8_unicode_ci)), 10),\n" +
                "                       '')) as latest_date\n" +
                "from kenyaemr_hiv_testing_patient_contact c\n" +
                "         left join (select t.patient_id,\n" +
                "                           max(t.visit_date)                                             as date_tested,\n" +
                "                           mid(max(concat(date(t.visit_date), t.final_test_result)), 11) as latest_hts_test_results\n" +
                "                    from kenyaemr_etl.etl_hts_test t\n" +
                "                    where date(t.visit_date) <= date(:endDate)\n" +
                "                    group by t.patient_id) t on c.patient_id = t.patient_id\n" +
                "         left join (select l.patient_id,\n" +
                "                           max(l.date_test_requested)                                       as lab_date,\n" +
                "                           mid(max(concat(date(l.date_test_requested), l.test_result)), 11) as latest_lab_test_results\n" +
                "                    from kenyaemr_etl.etl_laboratory_extract l\n" +
                "                    where date(l.date_test_requested) <= date(:endDate)\n" +
                "                      and l.lab_test in (1030, 163722)\n" +
                "                    group by l.patient_id) l on c.patient_id = l.patient_id\n" +
                "where date(c.date_created) <= date(:endDate) and c.voided = 0\n" +
                "group by c.id;";

        SqlQueryBuilder queryBuilder = new SqlQueryBuilder();
        queryBuilder.append(qry);
        Date endDate = (Date)context.getParameterValue("endDate");
        queryBuilder.addParameter("endDate", endDate);
        Map<Integer, Object> data = evaluationService.evaluateToMap(queryBuilder, Integer.class, Object.class, context);
        c.setData(data);
        return c;
    }
}
