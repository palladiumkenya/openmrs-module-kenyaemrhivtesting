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

package org.openmrs.module.hivtestingservices.reporting.builder;

import org.openmrs.module.hivtestingservices.reporting.cohort.definition.ChildrenContactsOfTXCurrWRACohortDefinition;
import org.openmrs.module.hivtestingservices.reporting.cohort.definition.ChildrenContactsOfTXCurrWRAUnknownHIVStatusCohortDefinition;
import org.openmrs.module.hivtestingservices.reporting.data.patientContact.definition.*;
import org.openmrs.module.hivtestingservices.reporting.definition.PatientContactDataSetDefinition;
import org.openmrs.module.kenyacore.report.HybridReportDescriptor;
import org.openmrs.module.kenyacore.report.ReportDescriptor;
import org.openmrs.module.kenyacore.report.ReportUtils;
import org.openmrs.module.kenyacore.report.builder.AbstractHybridReportBuilder;
import org.openmrs.module.kenyacore.report.builder.Builds;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
@Builds({"kenyaemr.hts.common.report.pmtctrrichildrencontacts"})
public class TXCurrReproductiveWomenChildrenContactReportBuilder extends AbstractHybridReportBuilder {
    public static final String ENC_DATE_FORMAT = "yyyy/MM/dd";

    @Override
    protected List<Parameter> getParameters(ReportDescriptor reportDescriptor) {
        return Arrays.asList(
                new Parameter("startDate", "Start Date", Date.class),
                new Parameter("endDate", "End Date", Date.class),
                new Parameter("dateBasedReporting", "", String.class)
        );
    }

    @Override
    protected List<Mapped<DataSetDefinition>> buildDataSets(ReportDescriptor reportDescriptor, ReportDefinition reportDefinition) {
        return Arrays.asList(
                ReportUtils.map(ChildrenContacts(), "startDate=${startDate},endDate=${endDate}"),
                ReportUtils.map(ChildrenContactsUndocumentedHIVStatus(), "startDate=${startDate},endDate=${endDate}")
        );
    }

    @Override
    protected Mapped<CohortDefinition> buildCohort(HybridReportDescriptor hybridReportDescriptor, PatientDataSetDefinition patientDataSetDefinition) {
        return null;
    }

    protected DataSetDefinition ChildrenContacts() {
        PatientContactDataSetDefinition dsd = new PatientContactDataSetDefinition();
        dsd.setName("ChildrenContacts");
        dsd.setDescription("Children Contact information");

        dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
        String paramMapping = "startDate=${startDate},endDate=${endDate}";
        PatientContactAgeAtReportingDataDefinition ageAtReportingDataDefinition = new PatientContactAgeAtReportingDataDefinition();
        ageAtReportingDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
        RelatedPatientAgeAtReportingDataDefinition motherAge = new RelatedPatientAgeAtReportingDataDefinition();
        motherAge.addParameter(new Parameter("endDate", "End Date", Date.class));
        ChildrenContactTestedForHIVDataDefinition testedForHIV = new ChildrenContactTestedForHIVDataDefinition();
        testedForHIV.addParameter(new Parameter("endDate", "End Date", Date.class));
        ChildrenContactHIVTestDateDataDefinition hivTestDate = new ChildrenContactHIVTestDateDataDefinition();
        hivTestDate.addParameter(new Parameter("endDate", "End Date", Date.class));
        ChildrenContactHIVTestResultsDataDefinition hivTestResults = new ChildrenContactHIVTestResultsDataDefinition();
        hivTestResults.addParameter(new Parameter("endDate", "End Date", Date.class));

        dsd.addColumn("Child's Name", new PatientContactNameDataDefinition(), "");
        dsd.addColumn("Age", ageAtReportingDataDefinition, "endDate=${endDate}");
        dsd.addColumn("Sex", new PatientContactSexDataDefinition(), "");
        dsd.addColumn("Tested for HIV", testedForHIV, "endDate=${endDate}");
        dsd.addColumn("Date Tested for HIV",hivTestDate, "endDate=${endDate}");
        dsd.addColumn("HIV test results", hivTestResults, "endDate=${endDate}");

        dsd.addColumn("Mother's Name", new RelatedPatientNameDataDefinition(), "");
        dsd.addColumn("Mother's CCC Number", new ChildIndexCCCNumberDataDefinition(), null);
        dsd.addColumn("Mother's Age", motherAge, "endDate=${endDate}");
        dsd.addColumn("Phone Number", new RelatedPatientPhoneContactDataDefinition(), "");

        ChildrenContactsOfTXCurrWRACohortDefinition cd = new ChildrenContactsOfTXCurrWRACohortDefinition();
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));

        dsd.addRowFilter(cd, paramMapping);
        return dsd;
    }
    protected DataSetDefinition ChildrenContactsUndocumentedHIVStatus() {
        PatientContactDataSetDefinition dsd = new PatientContactDataSetDefinition();
        dsd.setName("UndocumentedHIVStatus");
        dsd.setDescription("Children Contact with undocumented HIV status");

        dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
        String paramMapping = "startDate=${startDate},endDate=${endDate}";
        PatientContactAgeAtReportingDataDefinition ageAtReportingDataDefinition = new PatientContactAgeAtReportingDataDefinition();
        ageAtReportingDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
        RelatedPatientAgeAtReportingDataDefinition motherAge = new RelatedPatientAgeAtReportingDataDefinition();
        motherAge.addParameter(new Parameter("endDate", "End Date", Date.class));

        dsd.addColumn("Child's Name", new PatientContactNameDataDefinition(), "");
        dsd.addColumn("Age", ageAtReportingDataDefinition, "endDate=${endDate}");
        dsd.addColumn("Sex", new PatientContactSexDataDefinition(), "");
        dsd.addColumn("Mother's Name", new RelatedPatientNameDataDefinition(), "");
        dsd.addColumn("Mother's CCC Number", new ChildIndexCCCNumberDataDefinition(), null);
        dsd.addColumn("Mother's Age", motherAge, "endDate=${endDate}");
        dsd.addColumn("Phone Number", new RelatedPatientPhoneContactDataDefinition(), "");

        ChildrenContactsOfTXCurrWRAUnknownHIVStatusCohortDefinition cd = new ChildrenContactsOfTXCurrWRAUnknownHIVStatusCohortDefinition();
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));

        dsd.addRowFilter(cd, paramMapping);
        return dsd;
    }

}