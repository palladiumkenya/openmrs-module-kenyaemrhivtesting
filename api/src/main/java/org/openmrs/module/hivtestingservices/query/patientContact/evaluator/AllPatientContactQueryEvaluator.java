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
package org.openmrs.module.hivtestingservices.query.patientContact.evaluator;

import org.openmrs.annotation.Handler;
import org.openmrs.module.hivtestingservices.query.patientContact.PatientContactQueryResult;
import org.openmrs.module.hivtestingservices.query.patientContact.definition.AllPatientContactQuery;
import org.openmrs.module.hivtestingservices.query.patientContact.definition.PatientContactQuery;
import org.openmrs.module.hivtestingservices.reporting.data.PatientContactDataUtil;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.data.visit.VisitDataUtil;
import org.openmrs.module.reporting.evaluation.Definition;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.query.Query;
import org.openmrs.module.reporting.query.visit.VisitQueryResult;
import org.openmrs.module.reporting.query.visit.definition.AllVisitQuery;
import org.openmrs.module.reporting.query.visit.definition.VisitQuery;
import org.openmrs.module.reporting.query.visit.evaluator.VisitQueryEvaluator;

/**
 * The logic that evaluates a {@link AllVisitQuery} and produces an {@link Query}
 */
@Handler(supports=AllPatientContactQuery.class)
public class AllPatientContactQueryEvaluator implements PatientContactQueryEvaluator {

    /**
     * @see VisitQueryEvaluator#evaluate(Definition, EvaluationContext)
     * @should return all of the patientContact ids for all patients in the defined query
     * @should filter results by patient and patientContact given an PatientContactEvaluationContext
     * @should filter results by patient given an EvaluationContext
     */
    public PatientContactQueryResult evaluate(PatientContactQuery definition, EvaluationContext context) throws EvaluationException {
        context = ObjectUtil.nvl(context, new EvaluationContext());
        PatientContactQueryResult queryResult = new PatientContactQueryResult(definition, context);
        queryResult.setMemberIds(PatientContactDataUtil.getPatientContactIdsForContext(context, false));
        return queryResult;
    }
}
