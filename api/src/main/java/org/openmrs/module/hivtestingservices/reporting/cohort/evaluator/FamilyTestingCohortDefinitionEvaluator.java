package org.openmrs.module.hivtestingservices.reporting.cohort.evaluator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.annotation.Handler;
import org.openmrs.module.hivtestingservices.query.patientContact.PatientContactQueryResult;
import org.openmrs.module.hivtestingservices.query.patientContact.definition.PatientContactQuery;
import org.openmrs.module.hivtestingservices.query.patientContact.evaluator.PatientContactQueryEvaluator;
import org.openmrs.module.hivtestingservices.reporting.cohort.definition.FamilyTestingCohortDefinition;
import org.openmrs.module.hivtestingservices.reporting.cohort.definition.PatientContactListCohortDefinition;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

/**
 * Evaluator for patients for HTS Register
 */
@Handler(supports = {FamilyTestingCohortDefinition.class})
public class FamilyTestingCohortDefinitionEvaluator implements PatientContactQueryEvaluator {

    private final Log log = LogFactory.getLog(this.getClass());
	@Autowired
	EvaluationService evaluationService;

	public PatientContactQueryResult evaluate(PatientContactQuery definition, EvaluationContext context) throws EvaluationException {

		context = ObjectUtil.nvl(context, new EvaluationContext());
		PatientContactQueryResult queryResult = new PatientContactQueryResult(definition, context);

		String qry = "SELECT c.id from kenyaemr_hiv_testing_patient_contact c inner join kenyaemr_etl.etl_hts_test t on c.patient_related_to = t.patient_id\n" +
				" where t.voided=0 and t.test_type=2 and c.relationship_type in(971, 972, 1528, 162221, 970, 5617) and date(t.visit_date) between date(:startDate) and date(:endDate) and t.final_test_result = 'Positive'; ";

		SqlQueryBuilder builder = new SqlQueryBuilder();
		builder.append(qry);
		Date startDate = (Date)context.getParameterValue("startDate");
		Date endDate = (Date)context.getParameterValue("endDate");
		builder.addParameter("endDate", endDate);
		builder.addParameter("startDate", startDate);

		List<Integer> results = evaluationService.evaluateToList(builder, Integer.class, context);
		queryResult.getMemberIds().addAll(results);
		return queryResult;
	}

}
