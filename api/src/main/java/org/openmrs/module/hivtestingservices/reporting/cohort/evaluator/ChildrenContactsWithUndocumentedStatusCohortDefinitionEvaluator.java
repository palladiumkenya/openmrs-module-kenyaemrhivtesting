package org.openmrs.module.hivtestingservices.reporting.cohort.evaluator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.annotation.Handler;
import org.openmrs.module.hivtestingservices.query.patientContact.PatientContactQueryResult;
import org.openmrs.module.hivtestingservices.query.patientContact.definition.PatientContactQuery;
import org.openmrs.module.hivtestingservices.query.patientContact.evaluator.PatientContactQueryEvaluator;
import org.openmrs.module.hivtestingservices.reporting.cohort.definition.ChildrenContactsWithUndocumentedStatusCohortDefinition;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

/**
 * Evaluator for Children contacts with undocumented status. Excludes HEI
 */
@Handler(supports = {ChildrenContactsWithUndocumentedStatusCohortDefinition.class})
public class ChildrenContactsWithUndocumentedStatusCohortDefinitionEvaluator implements PatientContactQueryEvaluator {

    private final Log log = LogFactory.getLog(this.getClass());
	@Autowired
	EvaluationService evaluationService;

	public PatientContactQueryResult evaluate(PatientContactQuery definition, EvaluationContext context) throws EvaluationException {

		context = ObjectUtil.nvl(context, new EvaluationContext());
		PatientContactQueryResult queryResult = new PatientContactQueryResult(definition, context);

		String qry = "select c.patient_id\n" +
				"from (select pc.patient_id,\n" +
				"             pc.patient_related_to,\n" +
				"             h.patient_id  as hei,\n" +
				"             pc.baseline_hiv_status,\n" +
				"             pc.relationship_type,\n" +
				"             pc.date_created,\n" +
				"             pc.voided,\n" +
				"             ht.patient_id as tested_contact\n" +
				"      from kenyaemr_etl.etl_patient_contact pc\n" +
				"               inner join patient p on p.patient_id = pc.patient_related_to and p.voided = 0\n" +
				"               left join (select ht.patient_id,\n" +
				"                                 mid(max(concat(date(ht.visit_date), ht.final_test_result)), 11) as hiv_status\n" +
				"                          from kenyaemr_etl.etl_hts_test ht\n" +
				"                          group by ht.patient_id\n" +
				"                          having hiv_status in ('Negative', 'Positive')) ht on ht.patient_id = pc.patient_id\n" +
				"               left join (select e.patient_id, e.hiv_status_at_exit\n" +
				"                          from kenyaemr_etl.etl_hei_enrollment e\n" +
				"                                   inner join kenyaemr_etl.etl_patient_demographics d on d.patient_id = e.patient_id\n" +
				"                              and timestampdiff(WEEK, d.dob, date(:endDate)) between 6 and 96) h\n" +
				"                         on pc.patient_id = h.patient_id\n" +
				"     ) c\n" +
				"where\n" +
				"  date(c.date_created) <= date(:endDate)\n" +
				"  and c.hei is null\n" +
				"  and (c.baseline_hiv_status is null or c.baseline_hiv_status in ('Unknown','1067'))\n" +
				"  and c.relationship_type = 1528\n" +
				"  and c.voided = 0\n" +
				"  and c.tested_contact is null;";

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
