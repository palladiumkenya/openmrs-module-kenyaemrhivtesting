package org.openmrs.module.hivtestingservices.reporting.cohort.evaluator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.annotation.Handler;
import org.openmrs.module.hivtestingservices.query.patientContact.PatientContactQueryResult;
import org.openmrs.module.hivtestingservices.query.patientContact.definition.PatientContactQuery;
import org.openmrs.module.hivtestingservices.query.patientContact.evaluator.PatientContactQueryEvaluator;
import org.openmrs.module.hivtestingservices.reporting.cohort.definition.ChildrenContactsOfTXCurrWRACohortDefinition;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

/**
 * Evaluator for Children contacts of TX Curr reproductive omen
 */
@Handler(supports = {ChildrenContactsOfTXCurrWRACohortDefinition.class})
public class ChildrenContactsOfTXCurrWRACohortDefinitionEvaluator implements PatientContactQueryEvaluator {

    private final Log log = LogFactory.getLog(this.getClass());
	@Autowired
	EvaluationService evaluationService;

	public PatientContactQueryResult evaluate(PatientContactQuery definition, EvaluationContext context) throws EvaluationException {

		context = ObjectUtil.nvl(context, new EvaluationContext());
		PatientContactQueryResult queryResult = new PatientContactQueryResult(definition, context);

		String qry = "select t.contact_id\n" +
				"from (select fup.visit_date,\n" +
				"             fup.patient_id,\n" +
				"             c.patient_id                                                                   as contact_id,\n" +
				"             max(e.visit_date)                                                      as enroll_date,\n" +
				"             greatest(max(fup.visit_date), ifnull(max(d.visit_date), '0000-00-00')) as latest_vis_date,\n" +
				"             greatest(mid(max(concat(fup.visit_date, fup.next_appointment_date)), 11),\n" +
				"                      ifnull(max(d.visit_date), '0000-00-00'))                      as latest_tca,\n" +
				"             d.patient_id                                                           as disc_patient,\n" +
				"             d.effective_disc_date                                                  as effective_disc_date,\n" +
				"             max(d.visit_date)                                                      as date_discontinued,\n" +
				"             de.patient_id                                                          as started_on_drugs\n" +
				"      from kenyaemr_etl.etl_patient_contact c\n" +
				"               join kenyaemr_etl.etl_patient_hiv_followup fup on fup.patient_id = c.patient_related_to\n" +
				"               join kenyaemr_etl.etl_patient_demographics p on p.patient_id = fup.patient_id and timestampdiff(YEAR, date(p.dob), date(:endDate)) between 15 and 49\n" +
				"               join kenyaemr_etl.etl_patient_demographics p1 on c.patient_id = p1.patient_id and timestampdiff(YEAR, date(p1.dob), date(:endDate)) < 15\n" +
				"               join kenyaemr_etl.etl_hiv_enrollment e on fup.patient_id = e.patient_id\n" +
				"               left outer join kenyaemr_etl.etl_drug_event de on e.patient_id = de.patient_id and de.program = 'HIV' and\n" +
				"                                                                 date(date_started) <= date(:endDate)\n" +
				"               left outer JOIN\n" +
				"           (select patient_id,\n" +
				"                   coalesce(date(effective_discontinuation_date), visit_date) visit_date,\n" +
				"                   max(date(effective_discontinuation_date)) as               effective_disc_date\n" +
				"            from kenyaemr_etl.etl_patient_program_discontinuation\n" +
				"            where date(visit_date) <= date(:endDate)\n" +
				"              and program_name = 'HIV'\n" +
				"            group by patient_id) d on d.patient_id = fup.patient_id\n" +
				"      where fup.visit_date <= date(:endDate)\n" +
				"        and p.gender = 'F'\n" +
				"        and date(c.date_created) <= date(:endDate)\n" +
				"        and c.relationship_type = 3\n" +
				"        and c.voided = 0\n" +
				"        and date(c.date_created) <= date(:endDate)\n" +
				"      group by c.patient_id\n" +
				"      having (started_on_drugs is not null and started_on_drugs <> '')\n" +
				"         and (\n" +
				"          (\n" +
				"                  ((timestampdiff(DAY, date(latest_tca), date(:endDate)) <= 30) and\n" +
				"                   ((date(d.effective_disc_date) > date(:endDate) or date(enroll_date) > date(d.effective_disc_date)) or\n" +
				"                    d.effective_disc_date is null))\n" +
				"                  and\n" +
				"                  (date(latest_vis_date) >= date(date_discontinued) or date(latest_tca) >= date(date_discontinued) or\n" +
				"                   disc_patient is null)\n" +
				"              )\n" +
				"          )) t;";

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
