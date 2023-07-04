package org.openmrs.module.hivtestingservices.reporting.data.patientContact.evaluator;

import org.openmrs.annotation.Handler;
import org.openmrs.module.hivtestingservices.reporting.data.patientContact.EvaluatedPatientContactData;
import org.openmrs.module.hivtestingservices.reporting.data.patientContact.definition.PatientContactDataDefinition;
import org.openmrs.module.hivtestingservices.reporting.data.patientContact.definition.PatientContactDateTestedDataDefinition;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.Map;

/**
 * Evaluates PatientContactDateTestedDataDefinition
 */
@Handler(supports = PatientContactDateTestedDataDefinition.class, order = 50)
public class PatientContactDateTestedForHIVDataEvaluator implements PatientContactDataEvaluator {

    @Autowired
    private EvaluationService evaluationService;

    public EvaluatedPatientContactData evaluate(PatientContactDataDefinition definition, EvaluationContext context) throws EvaluationException {
        EvaluatedPatientContactData c = new EvaluatedPatientContactData(definition, context);

        String qry = "select c.id,\n" +
                "       coalesce(if(t.test_results in ('Positive', 'Negative'), t.date_tested, null),\n" +
                "                if(c.baseline_hiv_status in ('Positive', 'Negative'), c.reported_test_date, null)) as date_tested\n" +
                "from openmrs.kenyaemr_hiv_testing_patient_contact c\n" +
                "         left join (select t.patient_id,\n" +
                "                           max(date(t.visit_date))                                       as date_tested,\n" +
                "                           mid(max(concat(date(t.visit_date), t.final_test_result)), 11) as test_results\n" +
                "                    from kenyaemr_etl.etl_hts_test t\n" +
                "                    where date(t.visit_date) <= date(CURRENT_DATE)\n" +
                "                    group by t.patient_id) t on c.patient_id = t.patient_id\n" +
                "where date(c.date_created) <= date(:endDate);";

        SqlQueryBuilder queryBuilder = new SqlQueryBuilder();
        queryBuilder.append(qry);
        Date endDate = (Date)context.getParameterValue("endDate");
        queryBuilder.addParameter("endDate", endDate);
        Map<Integer, Object> data = evaluationService.evaluateToMap(queryBuilder, Integer.class, Object.class, context);
        c.setData(data);
        return c;
    }
}
