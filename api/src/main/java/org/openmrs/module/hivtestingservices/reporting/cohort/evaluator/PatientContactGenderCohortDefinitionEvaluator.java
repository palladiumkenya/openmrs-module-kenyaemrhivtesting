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
package org.openmrs.module.hivtestingservices.reporting.cohort.evaluator;

import org.openmrs.Cohort;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.hivtestingservices.api.HTSService;
import org.openmrs.module.hivtestingservices.reporting.cohort.definition.PatientContactGenderCohortDefinition;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.GenderCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.evaluator.CohortDefinitionEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;

/**
 * Evaluates an PatientCharacteristicCohortDefinition and produces a Cohort
 */
@Handler(supports={PatientContactGenderCohortDefinition.class})
public class PatientContactGenderCohortDefinitionEvaluator implements CohortDefinitionEvaluator {

	/**
	 * Default Constructor
	 */
	public PatientContactGenderCohortDefinitionEvaluator() {}
	
	/**
     * @see CohortDefinitionEvaluator#evaluate(CohortDefinition, EvaluationContext)
     * 
     * @should return all non voided patients when all are included
     * @should return male patients when males are included
     * @should return female patients when females are included
     * @should return patients with unknown gender when unknown are included
     * @should return no patients when none are included
     */
    public EvaluatedCohort evaluate(CohortDefinition cohortDefinition, EvaluationContext context) {
		PatientContactGenderCohortDefinition gcd = (PatientContactGenderCohortDefinition) cohortDefinition;
    	HTSService cqs = Context.getService(HTSService.class);
    	Cohort c = cqs.getPatientsWithGender(gcd.isMaleIncluded(), gcd.isFemaleIncluded(), gcd.isUnknownGenderIncluded());
    	return new EvaluatedCohort(c, cohortDefinition, context);
    }
}