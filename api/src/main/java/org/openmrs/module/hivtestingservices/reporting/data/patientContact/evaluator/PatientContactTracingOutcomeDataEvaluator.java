package org.openmrs.module.hivtestingservices.reporting.data.patientContact.evaluator;

import org.openmrs.annotation.Handler;
import org.openmrs.module.hivtestingservices.reporting.data.patientContact.EvaluatedPatientContactData;
import org.openmrs.module.hivtestingservices.reporting.data.patientContact.definition.PatientContactDataDefinition;
import org.openmrs.module.hivtestingservices.reporting.data.patientContact.definition.PatientContactTracingOutcomeDataDefinition;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.Map;

/**
 * EvaluatesPatientContactTracingOutcomeDataDefinition
 */
@Handler(supports= PatientContactTracingOutcomeDataDefinition.class, order=50)
public class PatientContactTracingOutcomeDataEvaluator implements PatientContactDataEvaluator {

    @Autowired
    private EvaluationService evaluationService;
    public EvaluatedPatientContactData evaluate(PatientContactDataDefinition definition, EvaluationContext context) throws EvaluationException {
        EvaluatedPatientContactData c = new EvaluatedPatientContactData(definition, context);

        String qry = "select c.id, t.outcome\n" +
                "from kenyaemr_hiv_testing_patient_contact c\n" +
                "         left join\n" +
                "     (select t.client_id, mid(max(concat(date(t.date_created), t.status)), 11) as outcome\n" +
                "      from kenyaemr_hiv_testing_client_trace t\n" +
                "      where DATE(t.date_created) <= date(CURRENT_DATE)\n" +
                "      group by t.client_id) t\n" +
                "     on c.id = t.client_id;";

        SqlQueryBuilder queryBuilder = new SqlQueryBuilder();
        queryBuilder.append(qry);
        Date endDate = (Date)context.getParameterValue("endDate");
        queryBuilder.addParameter("endDate", endDate);
        Map<Integer, Object> data = evaluationService.evaluateToMap(queryBuilder, Integer.class, Object.class, context);
        c.setData(data);
        return c;
    }
}
