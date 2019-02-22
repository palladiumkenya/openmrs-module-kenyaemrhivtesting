package org.openmrs.module.hivtestingservices.query.patientContact.service;

import org.openmrs.module.hivtestingservices.query.patientContact.PatientContactQueryResult;
import org.openmrs.module.hivtestingservices.query.patientContact.definition.PatientContactQuery;
import org.openmrs.module.reporting.definition.service.DefinitionService;
import org.openmrs.module.reporting.evaluation.Definition;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.query.visit.VisitQueryResult;
import org.openmrs.module.reporting.query.visit.definition.VisitQuery;

/**
 * Interface for methods used to manage and evaluate Visit Queries
 */
public interface PatientContactQueryService extends DefinitionService<PatientContactQuery> {

    /**
     * @see DefinitionService#evaluate(Definition, EvaluationContext)
     */
    public PatientContactQueryResult evaluate(PatientContactQuery query, EvaluationContext context) throws EvaluationException;

    /**
     * @see DefinitionService#evaluate(Mapped, EvaluationContext)
     */
    public PatientContactQueryResult evaluate(Mapped<? extends PatientContactQuery> mappedQuery, EvaluationContext context) throws EvaluationException;

}
