package org.openmrs.module.hivtestingservices.reporting.data.patientContact.evaluator;

import org.openmrs.annotation.Handler;
import org.openmrs.module.hivtestingservices.reporting.data.patientContact.EvaluatedPatientContactData;
import org.openmrs.module.hivtestingservices.reporting.data.patientContact.definition.PatientContactDataDefinition;
import org.openmrs.module.hivtestingservices.reporting.data.patientContact.definition.RelatedPatientPopulationTypeDataDefinition;
import org.openmrs.module.hivtestingservices.reporting.data.patientContact.definition.RelatedPatientTestStrategyDataDefinition;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * Evaluates a VisitIdDataDefinition to produce a VisitData
 */
@Handler(supports=RelatedPatientTestStrategyDataDefinition.class, order=50)
public class RelatedPatientTestStrategyDataEvaluator implements PatientContactDataEvaluator {

    @Autowired
    private EvaluationService evaluationService;

    public EvaluatedPatientContactData evaluate(PatientContactDataDefinition definition, EvaluationContext context) throws EvaluationException {
        EvaluatedPatientContactData c = new EvaluatedPatientContactData(definition, context);

        String qry = "select c.id, (case t.test_strategy\n" +
                "                      when 164163 then 'HP: Hospital Patient Testing'\n" +
                "                      when 164953 then 'NP: HTS for non-patients'\n" +
                "                      when 164954 then 'VI:Integrated VCT Center'\n" +
                "                      when 164955 then 'VS:Stand Alone VCT Center'\n" +
                "                      when 159938 then 'HB:Home Based Testing'\n" +
                "                      when 159939 then 'MO: Mobile Outreach HTS'\n" +
                "                      when 161557 then 'Index testing'\n" +
                "                      when 166606 then 'SNS - Social Networks'\n" +
                "                      when 5622 then 'O:Other'\n" +
                "                      else '' end ) as testStrategy\n" +
                "                    from kenyaemr_hiv_testing_patient_contact c inner join kenyaemr_etl.etl_hts_test t on t.patient_id=c.patient_related_to where c.voided = 0;";

        SqlQueryBuilder queryBuilder = new SqlQueryBuilder();
        queryBuilder.append(qry);
        Map<Integer, Object> data = evaluationService.evaluateToMap(queryBuilder, Integer.class, Object.class, context);
        c.setData(data);
        return c;
    }
}
