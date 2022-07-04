/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.hivtestingservices.calculation.library.contact;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Program;
import org.openmrs.api.context.Context;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.module.hivtestingservices.api.HTSService;
import org.openmrs.module.hivtestingservices.api.PatientContact;
import org.openmrs.module.hivtestingservices.metadata.HTSMetadata;
import org.openmrs.module.kenyacore.calculation.AbstractPatientCalculation;
import org.openmrs.module.kenyacore.calculation.BooleanResult;
import org.openmrs.module.kenyacore.calculation.Filters;
import org.openmrs.module.kenyacore.calculation.PatientFlagCalculation;
import org.openmrs.module.metadatadeploy.MetadataUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Calculates Contacts with Unknown HIV status
 */
public class UnknownHIVStatusCalculation extends AbstractPatientCalculation implements PatientFlagCalculation {

    HTSService htsService = Context.getService(HTSService.class);

    /**
     * @see PatientFlagCalculation#getFlagMessage()
     */
    @Override
    public String getFlagMessage() {
        return "Has contacts with unknown HIV status";
    }

    /**
     * @should calculate eligibility
     * @see org.openmrs.calculation.patient.PatientCalculation#evaluate(Collection, Map,
     * PatientCalculationContext)
     */
    protected static final Log log = LogFactory.getLog(UnknownHIVStatusCalculation.class);

    @Override
    public CalculationResultMap evaluate(Collection<Integer> cohort, Map<String, Object> parameterValues,
                                         PatientCalculationContext context) {
        Program hivProgram = MetadataUtils.existing(Program.class, HTSMetadata._Program.HIV);
        Set<Integer> alive = Filters.alive(cohort, context);

        Set<Integer> inHivProgram = Filters.inProgram(hivProgram, alive, context);
        CalculationResultMap ret = new CalculationResultMap();

        for (int ptId : cohort) {
            boolean eligible = false;

            if (inHivProgram.contains(ptId)) {
                List<PatientContact> patientContacts;
                patientContacts = htsService.getPatientContactByPatient(Context.getPatientService().getPatient(ptId));

                if (!patientContacts.isEmpty()) {
                    for (PatientContact pc : patientContacts) {
                        if (pc.getVoided().equals(false)) {

                            if (pc.getBaselineHivStatus() == null || !pc.getBaselineHivStatus().equalsIgnoreCase("Positive") && !pc.getBaselineHivStatus().equalsIgnoreCase("Negative")) {
                                eligible = true;
                                break;
                            }
                        }
                    }
                }
                ret.put(ptId, new BooleanResult(eligible, this));
            }
        }
        return ret;
    }
}

