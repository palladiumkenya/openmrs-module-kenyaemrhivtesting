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

package org.openmrs.module.hivtestingservices.reporting.library.shared;

import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.api.PatientSetService;
import org.openmrs.module.kenyacore.report.ReportUtils;
import org.openmrs.module.reporting.cohort.definition.AgeCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CodedObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.EncounterCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.GenderCohortDefinition;
import org.openmrs.module.reporting.common.SetComparator;
import org.openmrs.module.reporting.common.TimeQualifier;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;

/**
 * Library of common cohort definitions
 */
@Component
public class CommonHtsCohortLibrary {

	/**
	 * Patients who are female
	 * @return the cohort definition
	 */
	public CohortDefinition females() {
		GenderCohortDefinition cd = new GenderCohortDefinition();
		cd.setName("females");
		cd.setFemaleIncluded(true);
		return cd;
	}

	/**
	 * Patients who are male
	 * @return the cohort definition
	 */
	public CohortDefinition males() {
		GenderCohortDefinition cd = new GenderCohortDefinition();
		cd.setName("males");
		cd.setMaleIncluded(true);
		return cd;
	}

	/**
	 * Patients who at most maxAge years old on ${effectiveDate}
	 * @return the cohort definition
	 */
	public CohortDefinition agedAtMost(int maxAge) {
		AgeCohortDefinition cd = new AgeCohortDefinition();
		cd.setName("aged at most " + maxAge);
		cd.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		cd.setMaxAge(maxAge);
		return cd;
	}

	/**
	 * Patients who are at least minAge years old on ${effectiveDate}
	 * @return the cohort definition
	 */
	public CohortDefinition agedAtLeast(int minAge) {
		AgeCohortDefinition cd = new AgeCohortDefinition();
		cd.setName("aged at least " + minAge);
		cd.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		cd.setMinAge(minAge);
		return cd;
	}

	/**
	 * patients who are at least minAge years old and at most years old on ${effectiveDate}
	 * @return CohortDefinition
	 */
	public CohortDefinition agedAtLeastAgedAtMost(int minAge, int maxAge) {
		AgeCohortDefinition cd = new AgeCohortDefinition();
		cd.setName("aged between "+minAge+" and "+maxAge+" years");
		cd.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		cd.setMinAge(minAge);
		cd.setMaxAge(maxAge);
		return cd;
	}

	/**
	 * Patients who are female and at least 18 years old on ${effectiveDate}
	 * @return the cohort definition
	 */
	public CohortDefinition femalesAgedAtLeast18() {
		CompositionCohortDefinition cd = new CompositionCohortDefinition();
		cd.setName("females aged at least 18");
		cd.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		cd.addSearch("females", ReportUtils.map(females()));
		cd.addSearch("agedAtLeast18", ReportUtils.map(agedAtLeast(18), "effectiveDate=${effectiveDate}"));
		cd.setCompositionString("females AND agedAtLeast18");
		return cd;
	}

	/**
	 * Patients who have an encounter between ${onOrAfter} and ${onOrBefore}
	 * @param types the encounter types
	 * @return the cohort definition
	 */
	public CohortDefinition hasEncounter(EncounterType... types) {
		EncounterCohortDefinition cd = new EncounterCohortDefinition();
		cd.setName("has encounter between dates");
		cd.setTimeQualifier(TimeQualifier.ANY);
		cd.addParameter(new Parameter("onOrBefore", "Before Date", Date.class));
		cd.addParameter(new Parameter("onOrAfter", "After Date", Date.class));
		if (types.length > 0) {
			cd.setEncounterTypeList(Arrays.asList(types));
		}
		return cd;
	}

	/**
	 * Patients who have an obs between ${onOrAfter} and ${onOrBefore}
	 * @param question the question concept
	 * @param answers the answers to include
	 * @return the cohort definition
	 */
	public CohortDefinition hasObs(Concept question, Concept... answers) {
		CodedObsCohortDefinition cd = new CodedObsCohortDefinition();
		cd.setName("has obs between dates");
		cd.setQuestion(question);
		cd.setOperator(SetComparator.IN);
		cd.setTimeModifier(PatientSetService.TimeModifier.ANY);
		cd.addParameter(new Parameter("onOrBefore", "Before Date", Date.class));
		cd.addParameter(new Parameter("onOrAfter", "After Date", Date.class));
		if (answers.length > 0) {
			cd.setValueList(Arrays.asList(answers));
		}
		return cd;
	}




}