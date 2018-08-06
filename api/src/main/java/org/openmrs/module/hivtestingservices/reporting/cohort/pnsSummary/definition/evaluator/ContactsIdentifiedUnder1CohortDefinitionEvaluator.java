package org.openmrs.module.hivtestingservices.reporting.cohort.pnsSummary.definition.evaluator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.hivtestingservices.reporting.cohort.pnsSummary.definition.ContactsIdentifiedUnder1CohortDefinition;
import org.openmrs.module.hivtestingservices.reporting.library.PNSReportCohortLibrary;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.evaluator.CohortDefinitionEvaluator;
import org.openmrs.module.reporting.cohort.definition.service.CohortDefinitionService;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * Evaluator for patients eligible for RDQAPatientsOnCTXCohortDefinition
 */
@Handler(supports = {ContactsIdentifiedUnder1CohortDefinition.class})
public class ContactsIdentifiedUnder1CohortDefinitionEvaluator implements CohortDefinitionEvaluator {

    private final Log log = LogFactory.getLog(this.getClass());

    @Autowired
    private PNSReportCohortLibrary moh731Cohorts;


    @Override
    public EvaluatedCohort evaluate(CohortDefinition cohortDefinition, EvaluationContext context) throws EvaluationException {
        ContactsIdentifiedUnder1CohortDefinition definition = (ContactsIdentifiedUnder1CohortDefinition) cohortDefinition;

        if (definition == null)
            return null;

        String qry = "select kenyaemr_etl.patient_id from kenyaemr_etl.etl_hts_test\n" +
                " ;";

        SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition(qry);
        sqlCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
        sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
        Cohort results = Context.getService(CohortDefinitionService.class).evaluate(sqlCohortDefinition, context);
        
        return new EvaluatedCohort(results, definition, context);
    }


}
