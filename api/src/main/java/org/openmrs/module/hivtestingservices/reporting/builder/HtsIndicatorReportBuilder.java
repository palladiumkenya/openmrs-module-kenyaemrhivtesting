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

import org.openmrs.module.hivtestingservices.reporting.ColumnParameters;
import org.openmrs.module.hivtestingservices.reporting.EmrReportingUtils;
import org.openmrs.module.hivtestingservices.reporting.library.PNSReportIndicatorLibrary;
import org.openmrs.module.hivtestingservices.reporting.library.shared.CommonHtsDimensionLibrary;
import org.openmrs.module.kenyacore.report.ReportDescriptor;
import org.openmrs.module.kenyacore.report.ReportUtils;
import org.openmrs.module.kenyacore.report.builder.AbstractReportBuilder;
import org.openmrs.module.kenyacore.report.builder.Builds;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
@Builds({"kenyaemr.hts.common.report.htsIndicatorReport"})
public class HtsIndicatorReportBuilder extends AbstractReportBuilder {
    public static final String ENC_DATE_FORMAT = "yyyy/MM/dd";
    public static final String DATE_FORMAT = "dd/MM/yyyy";

    @Autowired
    private CommonHtsDimensionLibrary commonDimensions;

    @Autowired
    private PNSReportIndicatorLibrary htsIndicators;

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
                ReportUtils.map(htsDataSet(), "startDate=${startDate},endDate=${endDate}")

        );
    }


    protected DataSetDefinition htsDataSet() {
        CohortIndicatorDataSetDefinition cohortDsd = new CohortIndicatorDataSetDefinition();
        cohortDsd.setName("htsSummary");
        cohortDsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cohortDsd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cohortDsd.addDimension("age", ReportUtils.map(commonDimensions.htsFineAgeGroups(), "onDate=${endDate}"));
        cohortDsd.addDimension("gender", ReportUtils.map(commonDimensions.gender()));

        ColumnParameters colInfants = new ColumnParameters(null, "<1", "age=<1");

        ColumnParameters maleInfants = new ColumnParameters(null, "<1, Male", "gender=M|age=<1");
        ColumnParameters femaleInfants = new ColumnParameters(null, "<1, Female", "gender=F|age=<1");

        ColumnParameters children_1_to_9 = new ColumnParameters(null, "1-9", "age=1-9");

        ColumnParameters m_1_to_4 = new ColumnParameters(null, "1-4, Male", "gender=M|age=1-4");
        ColumnParameters f_1_to_4 = new ColumnParameters(null, "1-4, Female", "gender=F|age=1-4");

        ColumnParameters m_5_to_9 = new ColumnParameters(null, "5-9, Male", "gender=M|age=5-9");
        ColumnParameters f_5_to_9 = new ColumnParameters(null, "5-9, Female", "gender=F|age=5-9");

        ColumnParameters m_10_to_14 = new ColumnParameters(null, "10-14, Male", "gender=M|age=10-14");
        ColumnParameters f_10_to_14 = new ColumnParameters(null, "10-14, Female", "gender=F|age=10-14");

        ColumnParameters m_15_to_19 = new ColumnParameters(null, "15-19, Male", "gender=M|age=15-19");
        ColumnParameters f_15_to_19 = new ColumnParameters(null, "15-19, Female", "gender=F|age=15-19");

        ColumnParameters m_20_to_24 = new ColumnParameters(null, "20-24, Male", "gender=M|age=20-24");
        ColumnParameters f_20_to_24 = new ColumnParameters(null, "20-24, Female", "gender=F|age=20-24");

        ColumnParameters m_25_to_49 = new ColumnParameters(null, "25-49, Male", "gender=M|age=25-49");
        ColumnParameters f_25_to_49 = new ColumnParameters(null, "25-49, Female", "gender=F|age=25-49");

        // incorporating new age groups
        ColumnParameters m_25_to_29 = new ColumnParameters(null, "25-29, Male", "gender=M|age=25-29");
        ColumnParameters f_25_to_29 = new ColumnParameters(null, "25-29, Female", "gender=F|age=25-29");

        ColumnParameters m_30_to_34 = new ColumnParameters(null, "30-34, Male", "gender=M|age=30-34");
        ColumnParameters f_30_to_34 = new ColumnParameters(null, "30-34, Female", "gender=F|age=30-34");

        ColumnParameters m_35_to_39 = new ColumnParameters(null, "35-39, Male", "gender=M|age=35-39");
        ColumnParameters f_35_to_39 = new ColumnParameters(null, "35-39, Female", "gender=F|age=35-39");

        ColumnParameters m_40_to_44 = new ColumnParameters(null, "40-44, Male", "gender=M|age=40-44");
        ColumnParameters f_40_to_44 = new ColumnParameters(null, "40-44, Female", "gender=F|age=40-44");

        ColumnParameters m_45_to_49 = new ColumnParameters(null, "45-49, Male", "gender=M|age=45-49");
        ColumnParameters f_45_to_49 = new ColumnParameters(null, "45-49, Female", "gender=F|age=45-49");

        ColumnParameters m_50_and_above = new ColumnParameters(null, "50+, Male", "gender=M|age=50+");
        ColumnParameters f_50_and_above = new ColumnParameters(null, "50+, Female", "gender=F|age=50+");

        ColumnParameters colTotal = new ColumnParameters(null, "Total", "");

        List<ColumnParameters> allAgeDisaggregation = Arrays.asList(
                colInfants, children_1_to_9,  m_10_to_14, f_10_to_14,m_15_to_19, f_15_to_19,
                m_20_to_24,f_20_to_24,m_25_to_29, f_25_to_29, m_30_to_34, f_30_to_34, m_35_to_39, f_35_to_39, m_40_to_44, f_40_to_44, m_45_to_49, f_45_to_49 ,m_50_and_above,f_50_and_above , colTotal);


        String indParams = "startDate=${startDate},endDate=${endDate}";

        EmrReportingUtils.addRow(cohortDsd, "PNS01", "Tested", ReportUtils.map(htsIndicators.htsTested(), indParams), allAgeDisaggregation, Arrays.asList("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21"));
        EmrReportingUtils.addRow(cohortDsd, "PNS02", "Newly Tested", ReportUtils.map(htsIndicators.htsNewlyTested(), indParams), allAgeDisaggregation, Arrays.asList("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21"));
        EmrReportingUtils.addRow(cohortDsd, "PNS03", "Newly Tested who received results", ReportUtils.map(htsIndicators.htsNewlyTestedWhoReceivedResults(), indParams), allAgeDisaggregation, Arrays.asList("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21"));
        EmrReportingUtils.addRow(cohortDsd, "PNS04", "Total Tested who received results", ReportUtils.map(htsIndicators.htsTotaltestedWhoReceivedResults(), indParams), allAgeDisaggregation, Arrays.asList("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21"));
        EmrReportingUtils.addRow(cohortDsd, "PNS05", "Positives", ReportUtils.map(htsIndicators.htsTotalPositive(), indParams), allAgeDisaggregation, Arrays.asList("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21"));
        EmrReportingUtils.addRow(cohortDsd, "PNS06", "Newly Positive", ReportUtils.map(htsIndicators.htsNewlyPositive(), indParams), allAgeDisaggregation, Arrays.asList("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21"));

        return cohortDsd;

    }

}