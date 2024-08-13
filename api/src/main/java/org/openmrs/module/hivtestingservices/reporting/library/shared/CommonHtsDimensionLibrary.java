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

import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.dimension.CohortDefinitionDimension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

import static org.openmrs.module.kenyacore.report.ReportUtils.map;

/**
 * Library of common dimension definitions
 */
@Component
public class CommonHtsDimensionLibrary {

	@Autowired
	private CommonHtsCohortLibrary commonCohortLibrary;

	/**
	 * Gender dimension
	 * @return the dimension
	 */
	public CohortDefinitionDimension gender() {
		CohortDefinitionDimension dim = new CohortDefinitionDimension();
		dim.setName("gender");
		dim.addCohortDefinition("M", map(commonCohortLibrary.males()));
		dim.addCohortDefinition("F", map(commonCohortLibrary.females()));
		return dim;
	}

	/**
	 * Gender dimension for patient contact
	 * @return the dimension
	 */
	public CohortDefinitionDimension contactGender() {
		CohortDefinitionDimension dim = new CohortDefinitionDimension();
		dim.setName("gender");
		dim.addCohortDefinition("M", map(commonCohortLibrary.malePatientContacts()));
		dim.addCohortDefinition("F", map(commonCohortLibrary.femalePatientContacts()));
		return dim;
	}

	/**
	 * Dimension of age using the 3 standard age groups
	 * @return the dimension
	 */
	public CohortDefinitionDimension standardAgeGroups() {
		CohortDefinitionDimension dim = new CohortDefinitionDimension();
		dim.setName("age groups (<1, <15, 15+)");
		dim.addParameter(new Parameter("onDate", "Date", Date.class));
		dim.addCohortDefinition("<1", map(commonCohortLibrary.agedAtMost(0), "effectiveDate=${onDate}"));
		dim.addCohortDefinition("<15", map(commonCohortLibrary.agedAtMost(14), "effectiveDate=${onDate}"));
		dim.addCohortDefinition("15+", map(commonCohortLibrary.agedAtLeast(15), "effectiveDate=${onDate}"));
		return dim;
	}

	/**
	 * Dimension of age between
	 * @return Dimension
	 */
	public CohortDefinitionDimension datimAgeGroups() {
		CohortDefinitionDimension dim = new CohortDefinitionDimension();
		dim.setName("standard age between(<1, btw 1 and 9, btw 10 and 14, btw 15 and 19, btw 20 and 24, btw 25 and 49, 50+");
		dim.addParameter(new Parameter("onDate", "Date", Date.class));
		dim.addCohortDefinition("<1", map(commonCohortLibrary.agedAtMost(0), "effectiveDate=${onDate}"));
		dim.addCohortDefinition("1 - 9", map(commonCohortLibrary.agedAtMost(9), "effectiveDate=${onDate}"));
		dim.addCohortDefinition("10 - 14", map(commonCohortLibrary.agedAtMost(14), "effectiveDate=${onDate}"));
		dim.addCohortDefinition("15 - 19", map(commonCohortLibrary.agedAtMost(19), "effectiveDate=${onDate}"));
		dim.addCohortDefinition("20 - 24", map(commonCohortLibrary.agedAtMost(24), "effectiveDate=${onDate}"));
		dim.addCohortDefinition("25 - 49", map(commonCohortLibrary.agedAtMost(49), "effectiveDate=${onDate}"));
		dim.addCohortDefinition("50+", map(commonCohortLibrary.agedAtLeast(50), "effectiveDate=${onDate}"));

		return dim;
	}

	/**
	 * Dimension of age between
	 * @return Dimension
	 */
	public CohortDefinitionDimension htsFineAgeGroups() {
		CohortDefinitionDimension dim = new CohortDefinitionDimension();
		dim.setName("fine age between(<1, btw 1 and 9, btw 10 and 14, btw 15 and 19, btw 20 and 24, btw 25 and 49, 50+");
		dim.addParameter(new Parameter("onDate", "Date", Date.class));
		dim.addCohortDefinition("<1", map(commonCohortLibrary.agedAtMost(0), "effectiveDate=${onDate}"));
		dim.addCohortDefinition("1-4", map(commonCohortLibrary.agedAtLeastAgedAtMost(1, 4), "effectiveDate=${onDate}"));
		dim.addCohortDefinition("5-9", map(commonCohortLibrary.agedAtLeastAgedAtMost(5, 9), "effectiveDate=${onDate}"));
		dim.addCohortDefinition("1-9", map(commonCohortLibrary.agedAtLeastAgedAtMost(1, 9), "effectiveDate=${onDate}"));
		dim.addCohortDefinition("10-14", map(commonCohortLibrary.agedAtLeastAgedAtMost(10, 14), "effectiveDate=${onDate}"));
		dim.addCohortDefinition("15-19", map(commonCohortLibrary.agedAtLeastAgedAtMost(15, 19), "effectiveDate=${onDate}"));
		dim.addCohortDefinition("20-24", map(commonCohortLibrary.agedAtLeastAgedAtMost(20, 24), "effectiveDate=${onDate}"));
		dim.addCohortDefinition("25-49", map(commonCohortLibrary.agedAtLeastAgedAtMost(25, 49), "effectiveDate=${onDate}"));
		// new age disaggregations
		dim.addCohortDefinition("25-29", map(commonCohortLibrary.agedAtLeastAgedAtMost(25, 29), "effectiveDate=${onDate}"));
		dim.addCohortDefinition("30-34", map(commonCohortLibrary.agedAtLeastAgedAtMost(30, 34), "effectiveDate=${onDate}"));
		dim.addCohortDefinition("35-39", map(commonCohortLibrary.agedAtLeastAgedAtMost(35, 39), "effectiveDate=${onDate}"));
		dim.addCohortDefinition("40-45", map(commonCohortLibrary.agedAtLeastAgedAtMost(40, 45), "effectiveDate=${onDate}"));
		dim.addCohortDefinition("40-44", map(commonCohortLibrary.agedAtLeastAgedAtMost(40, 44), "effectiveDate=${onDate}"));
		dim.addCohortDefinition("45-49", map(commonCohortLibrary.agedAtLeastAgedAtMost(45, 49), "effectiveDate=${onDate}"));
		dim.addCohortDefinition("46-49", map(commonCohortLibrary.agedAtLeastAgedAtMost(46, 49), "effectiveDate=${onDate}"));
		// previous one
		dim.addCohortDefinition("50+", map(commonCohortLibrary.agedAtLeast(50), "effectiveDate=${onDate}"));

		return dim;
	}

	/**
	 * Dimension of age between for PNS Report
	 * @return Dimension
	 */
	public CohortDefinitionDimension pnsReportAgeGroups() {
		CohortDefinitionDimension dim = new CohortDefinitionDimension();
		dim.setName("fine age between(<1, btw 1 and 9, btw 10 and 14, btw 15 and 19, btw 20 and 24, 25 to 29, btw 25 and 49, 50+");
		dim.addParameter(new Parameter("onDate", "Date", Date.class));
		dim.addCohortDefinition("<1", map(commonCohortLibrary.agedAtMost(0), "effectiveDate=${onDate}"));
		dim.addCohortDefinition("1-9", map(commonCohortLibrary.agedAtLeastAgedAtMost(1, 9), "effectiveDate=${onDate}"));
		dim.addCohortDefinition("10-14", map(commonCohortLibrary.agedAtLeastAgedAtMost(10, 14), "effectiveDate=${onDate}"));
		dim.addCohortDefinition("15-19", map(commonCohortLibrary.agedAtLeastAgedAtMost(15, 19), "effectiveDate=${onDate}"));
		dim.addCohortDefinition("20-24", map(commonCohortLibrary.agedAtLeastAgedAtMost(20, 24), "effectiveDate=${onDate}"));
		dim.addCohortDefinition("25-29", map(commonCohortLibrary.agedAtLeastAgedAtMost(25, 29), "effectiveDate=${onDate}"));
		dim.addCohortDefinition("30-49", map(commonCohortLibrary.agedAtLeastAgedAtMost(30, 49), "effectiveDate=${onDate}"));
		dim.addCohortDefinition("50+", map(commonCohortLibrary.agedAtLeast(50), "effectiveDate=${onDate}"));

		return dim;
	}

	public CohortDefinitionDimension familyTestingReportAgeGroups() {
		CohortDefinitionDimension dim = new CohortDefinitionDimension();
		dim.setName("fine age <=14 Yrs, 15-19 Yrs, >=20 Yrs");
		dim.addParameter(new Parameter("onDate", "Date", Date.class));
		dim.addCohortDefinition("0-14", map(commonCohortLibrary.agedAtLeastAgedAtMost(0, 14), "effectiveDate=${onDate}"));
		dim.addCohortDefinition("15-19", map(commonCohortLibrary.agedAtLeastAgedAtMost(15, 19), "effectiveDate=${onDate}"));
		dim.addCohortDefinition("20+", map(commonCohortLibrary.agedAtLeast(20), "effectiveDate=${onDate}"));

		return dim;
	}


	/**
	 * Dimension of age between
	 * @return Dimension
	 */
	public CohortDefinitionDimension moh731GreenCardAgeGroups() {
		CohortDefinitionDimension dim = new CohortDefinitionDimension();
		dim.setName("fine age between(<1, btw 1 and 9, btw 10 and 14, btw 15 and 19, btw 20 and 24, 25+");
		dim.addParameter(new Parameter("onDate", "Date", Date.class));
		dim.addCohortDefinition("<1", map(commonCohortLibrary.agedAtMost(0), "effectiveDate=${onDate}"));
		dim.addCohortDefinition("1-9", map(commonCohortLibrary.agedAtLeastAgedAtMost(1, 9), "effectiveDate=${onDate}"));
		dim.addCohortDefinition("0-14", map(commonCohortLibrary.agedAtMost(14), "effectiveDate=${onDate}"));
		dim.addCohortDefinition("15+", map(commonCohortLibrary.agedAtLeast(15), "effectiveDate=${onDate}"));
		dim.addCohortDefinition("10-14", map(commonCohortLibrary.agedAtLeastAgedAtMost(10, 14), "effectiveDate=${onDate}"));
		dim.addCohortDefinition("15-19", map(commonCohortLibrary.agedAtLeastAgedAtMost(15, 19), "effectiveDate=${onDate}"));
		dim.addCohortDefinition("20-24", map(commonCohortLibrary.agedAtLeastAgedAtMost(20, 24), "effectiveDate=${onDate}"));
		dim.addCohortDefinition("25+", map(commonCohortLibrary.agedAtLeast(25), "effectiveDate=${onDate}"));

		return dim;
	}
}