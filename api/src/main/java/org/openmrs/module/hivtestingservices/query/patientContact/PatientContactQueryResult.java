package org.openmrs.module.hivtestingservices.query.patientContact;

import org.openmrs.module.hivtestingservices.query.patientContact.definition.PatientContactQuery;
import org.openmrs.module.reporting.evaluation.Evaluated;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.query.visit.VisitIdSet;
import org.openmrs.module.reporting.query.visit.definition.VisitQuery;

/**
 * Result of an Evaluated PatientContact Query
 */
public class PatientContactQueryResult extends PatientContactIdSet implements Evaluated<PatientContactQuery> {

    //***** PROPERTIES *****

    private PatientContactQuery definition;
    private EvaluationContext context;

    //***** CONSTRUCTORS *****

    /**
     * Default Constructor
     */
    public PatientContactQueryResult() {
        super();
    }

    /**
     * Full Constructor
     */
    public PatientContactQueryResult(PatientContactQuery definition, EvaluationContext context) {
        this.definition = definition;
        this.context = context;
    }

    //***** PROPERTY ACCESS *****

    /**
     * @return the definition
     */
    public PatientContactQuery getDefinition() {
        return definition;
    }

    /**
     * @param definition the definition to set
     */
    public void setDefinition(PatientContactQuery definition) {
        this.definition = definition;
    }

    /**
     * @return the context
     */
    public EvaluationContext getContext() {
        return context;
    }

    /**
     * @param context the context to set
     */
    public void setContext(EvaluationContext context) {
        this.context = context;
    }


}
