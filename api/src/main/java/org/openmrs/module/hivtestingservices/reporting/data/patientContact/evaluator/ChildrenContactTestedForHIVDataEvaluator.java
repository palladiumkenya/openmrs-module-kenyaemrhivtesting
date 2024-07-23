package org.openmrs.module.hivtestingservices.reporting.data.patientContact.evaluator;

import org.openmrs.annotation.Handler;
import org.openmrs.module.hivtestingservices.reporting.data.patientContact.EvaluatedPatientContactData;
import org.openmrs.module.hivtestingservices.reporting.data.patientContact.definition.ChildrenContactTestedForHIVDataDefinition;
import org.openmrs.module.hivtestingservices.reporting.data.patientContact.definition.PatientContactDataDefinition;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.Map;

/**
 * Evaluates ChildrenContactTestedForHIVDataDefinition
 */
@Handler(supports= ChildrenContactTestedForHIVDataDefinition.class, order=50)
public class ChildrenContactTestedForHIVDataEvaluator implements PatientContactDataEvaluator {

    @Autowired
    private EvaluationService evaluationService;

    public EvaluatedPatientContactData evaluate(PatientContactDataDefinition definition, EvaluationContext context) throws EvaluationException {
        EvaluatedPatientContactData c = new EvaluatedPatientContactData(definition, context);

        String qry = "select c.patient_id,\n" +
                "       if((t.patient_id is not null and t.patient_id <> '') or (l.patient_id is not null and l.patient_id <> '') or\n" +
                "          (c.baseline_hiv_status in ('Positive', 'Negative') and c.baseline_hiv_status is not null),\n" +
                "          'Yes', 'No') as ever_tested_for_hiv\n" +
                "from kenyaemr_etl.etl_patient_contact c\n" +
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
                "                    group by l.patient_id\n" +
                "                    having latest_lab_test_results is not null) l on c.patient_id = l.patient_id\n" +
                "where date(c.date_created) <= date(:endDate) and c.voided = 0;";

        SqlQueryBuilder queryBuilder = new SqlQueryBuilder();
        queryBuilder.append(qry);
        Date endDate = (Date) context.getParameterValue("endDate");
        queryBuilder.addParameter("endDate", endDate);
        Map<Integer, Object> data = evaluationService.evaluateToMap(queryBuilder, Integer.class, Object.class, context);
        c.setData(data);
        return c;
    }
}
