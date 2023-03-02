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
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.module.hivtestingservices.api.HTSService;
import org.openmrs.module.hivtestingservices.api.PatientContact;
import org.openmrs.module.kenyacore.calculation.AbstractPatientCalculation;
import org.openmrs.module.kenyacore.calculation.BooleanResult;
import org.openmrs.module.kenyacore.calculation.PatientFlagCalculation;
import org.openmrs.module.kenyaemr.util.EmrUtils;
import org.openmrs.module.kenyaemr.util.HtsConstants;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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

        EncounterService encounterService = Context.getEncounterService();

        CalculationResultMap ret = new CalculationResultMap();

        for (int ptId : cohort) {
            boolean eligible = false;
            List<PatientContact> patientContacts;
            patientContacts = htsService.getPatientContactByPatient(Context.getPatientService().getPatient(ptId));

            if (!patientContacts.isEmpty()) {

                ConceptService cs = Context.getConceptService();
                Concept htsFinalTestQuestion = cs.getConcept(HtsConstants.HTS_FINAL_TEST_CONCEPT_ID);
                Concept htsPositiveResult = cs.getConcept(HtsConstants.HTS_POSITIVE_RESULT_CONCEPT_ID);
                Concept htsNegativeResult = cs.getConcept(HtsConstants.HTS_NEGATIVE_RESULT_CONCEPT_ID);

                for (PatientContact pc : patientContacts) {
                    int count = 0;
                    if (pc.getVoided().equals(false)) {

                        //Checking for HTS encounters for valid test results (Positive or Negative)
                        Patient patient = pc.getPatient();
                        List<Encounter> htsEncounters = encounterService.getEncounters(patient, null, null, null, Arrays.asList(HtsConstants.htsInitialForm, HtsConstants.htsRetestForm), null, null, null, null, false);

                        Encounter lastHtsInitialEnc = EmrUtils.lastEncounter(patient, HtsConstants.htsEncType, HtsConstants.htsInitialForm);
                        Encounter lastHtsRetestEnc = EmrUtils.lastEncounter(patient, HtsConstants.htsEncType, HtsConstants.htsRetestForm);

                        Encounter lastHtsEnc = null;

                        boolean patientHasNegativeTestResult = false;
                        boolean patientHasPositiveTestResult = false;

                        if (patient != null) {

                            if (lastHtsInitialEnc != null && lastHtsRetestEnc == null) {
                                lastHtsEnc = lastHtsInitialEnc;
                            } else if (lastHtsInitialEnc == null && lastHtsRetestEnc != null) {
                                lastHtsEnc = lastHtsRetestEnc;
                            } else if (lastHtsInitialEnc != null && lastHtsRetestEnc != null) {
                                if (lastHtsInitialEnc.getEncounterDatetime().after(lastHtsRetestEnc.getEncounterDatetime())) {
                                    lastHtsEnc = lastHtsInitialEnc;
                                } else {
                                    lastHtsEnc = lastHtsRetestEnc;
                                }
                            }
                            patientHasNegativeTestResult = htsEncounters.size() > 0 && lastHtsEnc != null ? EmrUtils.encounterThatPassCodedAnswer(lastHtsEnc, htsFinalTestQuestion, htsNegativeResult) : false;
                            patientHasPositiveTestResult = htsEncounters.size() > 0 && lastHtsEnc != null ? EmrUtils.encounterThatPassCodedAnswer(lastHtsEnc, htsFinalTestQuestion, htsPositiveResult) : false;
                        }
                        if (patient != null && lastHtsEnc != null) {
                            if ((pc.getBaselineHivStatus() == null || (!pc.getBaselineHivStatus().equalsIgnoreCase("Positive") && !pc.getBaselineHivStatus().equalsIgnoreCase("Negative"))) && !patientHasNegativeTestResult && !patientHasPositiveTestResult) {
                                ++count;
                            }
                        }

                        //Checking for Unknown status in Patient contact for non-registered and untested contacts
                        else if ((pc.getBaselineHivStatus() == null || (!pc.getBaselineHivStatus().equalsIgnoreCase("Positive") && !pc.getBaselineHivStatus().equalsIgnoreCase("Negative")))) {
                            ++count;
                        }
                    }
                    if (count > 0)
                        eligible = true;
                }
            }
            ret.put(ptId, new BooleanResult(eligible, this));
        }
        return ret;
    }
}

