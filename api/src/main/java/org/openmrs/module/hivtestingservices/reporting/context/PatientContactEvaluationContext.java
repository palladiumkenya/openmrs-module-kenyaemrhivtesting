/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.hivtestingservices.reporting.context;

import org.openmrs.OpenmrsData;
import org.openmrs.Visit;
import org.openmrs.module.hivtestingservices.query.patientContact.PatientContactIdSet;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.query.IdSet;
import org.openmrs.module.reporting.query.visit.VisitIdSet;

import java.util.Date;
import java.util.Map;

/**
 * Extends the patient-based EvaluationContext to add an additional PatientContact filter 
 * Note that this cache is cleared whenever any changes are made to baseContacts
 */
public class PatientContactEvaluationContext extends EvaluationContext {

    // ***** PROPERTIES *****

    private PatientContactIdSet baseContacts;

    // ***** CONSTRUCTORS *****

    /**
     * Default Constructor
     */
    public PatientContactEvaluationContext() {
        super();
    }

    /**
     * Constructor which sets the Evaluation Date to a particular date
     */
    public PatientContactEvaluationContext(Date evaluationDate) {
        super(evaluationDate);
    }

    /**
     * Constructs a new PatientContactEvaluationContext given the passed EvaluationContext and VisitIdSet
     */
    public PatientContactEvaluationContext(EvaluationContext context, PatientContactIdSet baseContacts) {
        super(context);
        this.baseContacts = baseContacts;
    }

    /**
     * Constructs a new EvaluationContext given the passed EvaluationContext
     */
    public PatientContactEvaluationContext(PatientContactEvaluationContext context) {
        super(context);
        this.baseContacts = context.baseContacts;
    }

    // *******************
    // INSTANCE METHODS
    // *******************

    @Override
    public Map<Class<? extends OpenmrsData>, IdSet<?>> getAllBaseIdSets() {
        Map<Class<? extends OpenmrsData>, IdSet<?>> ret = super.getAllBaseIdSets();
        if (getBaseContacts() != null) {
            ret.put(Visit.class, getBaseContacts());
        }
        return ret;
    }

    /**
     * @return a shallow copy of the current instance
     */
    @Override
    public PatientContactEvaluationContext shallowCopy() {
        return new PatientContactEvaluationContext(this);
    }

    /**
     * @return the baseContacts
     */
    public PatientContactIdSet getBaseContacts() {
        return baseContacts;
    }

    /**
     * @param baseContacts the baseContacts to set
     */
    public void setBaseContacts(PatientContactIdSet baseContacts) {
        clearCache();
        this.baseContacts = baseContacts;
    }



}
