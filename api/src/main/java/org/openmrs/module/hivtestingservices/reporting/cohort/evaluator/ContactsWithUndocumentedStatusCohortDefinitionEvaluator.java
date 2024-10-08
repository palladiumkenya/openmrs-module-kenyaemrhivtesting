package org.openmrs.module.hivtestingservices.reporting.cohort.evaluator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.annotation.Handler;
import org.openmrs.module.hivtestingservices.query.patientContact.PatientContactQueryResult;
import org.openmrs.module.hivtestingservices.query.patientContact.definition.PatientContactQuery;
import org.openmrs.module.hivtestingservices.query.patientContact.evaluator.PatientContactQueryEvaluator;
import org.openmrs.module.hivtestingservices.reporting.cohort.definition.ContactsWithUndocumentedStatusCohortDefinition;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Evaluator for contacts with undocumented status
 */
@Handler(supports = {ContactsWithUndocumentedStatusCohortDefinition.class})
public class ContactsWithUndocumentedStatusCohortDefinitionEvaluator implements PatientContactQueryEvaluator {

    private final Log log = LogFactory.getLog(this.getClass());
	@Autowired
	EvaluationService evaluationService;

	public PatientContactQueryResult evaluate(PatientContactQuery definition, EvaluationContext context) throws EvaluationException {

		context = ObjectUtil.nvl(context, new EvaluationContext());
		PatientContactQueryResult queryResult = new PatientContactQueryResult(definition, context);

		String qry = "select pc.patient_id from kenyaemr_etl.etl_patient_contact pc\n" +
				"                      inner join kenyaemr_etl.etl_patient_demographics p on p.patient_id = pc.patient_related_to and p.voided = 0\n" +
				"                      left join (select ht.patient_id, mid(max(concat(date(ht.visit_date),ht.final_test_result)),11) as hiv_status\n" +
				"                                 from kenyaemr_etl.etl_hts_test ht group by ht.patient_id\n" +
				"                                 having hiv_status in ('Negative','Positive'))ht on ht.patient_id = pc.patient_id and pc.voided = 0\n" +
				"where (pc.baseline_hiv_status is null or pc.baseline_hiv_status in ('Unknown','1067')) and pc.voided = 0 and ht.patient_id is null;";

		SqlQueryBuilder builder = new SqlQueryBuilder();
		builder.append(qry);

		List<Integer> results = evaluationService.evaluateToList(builder, Integer.class, context);
		queryResult.getMemberIds().addAll(results);
		return queryResult;
	}

}
