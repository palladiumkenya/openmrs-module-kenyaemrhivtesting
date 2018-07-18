package org.openmrs.module.hivtestingservices.query.patientContact.service;

import org.openmrs.module.hivtestingservices.query.patientContact.PatientContactQueryResult;
import org.openmrs.module.hivtestingservices.query.patientContact.definition.PatientContactQuery;
import org.openmrs.module.reporting.definition.service.BaseDefinitionService;
import org.openmrs.module.reporting.definition.service.DefinitionService;
import org.openmrs.module.reporting.evaluation.Definition;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.query.visit.VisitQueryResult;
import org.openmrs.module.reporting.query.visit.definition.VisitQuery;
import org.openmrs.module.reporting.query.visit.service.VisitQueryService;

/**
 * Base Implementation of PatientContactQueryService
 */
public class PatientContactQueryServiceImpl extends BaseDefinitionService<PatientContactQuery> implements PatientContactQueryService {

    /**
     * @see DefinitionService#getDefinitionType()
     */
    public Class<PatientContactQuery> getDefinitionType() {
        return PatientContactQuery.class;
    }

    /**
     * @see DefinitionService#evaluate(Definition, EvaluationContext)
     * @should evaluate an encounter query
     */
    public PatientContactQueryResult evaluate(PatientContactQuery query, EvaluationContext context) throws EvaluationException {
        return (PatientContactQueryResult)super.evaluate(query, context);
    }

    /**
     * @see DefinitionService#evaluate(Mapped, EvaluationContext)
     */
    public PatientContactQueryResult evaluate(Mapped<? extends PatientContactQuery> mappedQuery, EvaluationContext context) throws EvaluationException {
        return (PatientContactQueryResult)super.evaluate(mappedQuery, context);
    }
}
