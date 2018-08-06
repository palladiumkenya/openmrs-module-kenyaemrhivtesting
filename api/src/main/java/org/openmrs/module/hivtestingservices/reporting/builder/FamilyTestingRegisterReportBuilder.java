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
import org.openmrs.module.hivtestingservices.reporting.cohort.definition.FamilyTestingCohortDefinition;
import org.openmrs.module.hivtestingservices.reporting.data.patientContact.definition.PatientContactAgeDataDefinition;
import org.openmrs.module.hivtestingservices.reporting.data.patientContact.definition.PatientContactAppointmentForTestDataDefinition;
import org.openmrs.module.hivtestingservices.reporting.data.patientContact.definition.PatientContactBaselineHivStatusDataDefinition;
import org.openmrs.module.hivtestingservices.reporting.data.patientContact.definition.PatientContactDateLinkedToCareDataDefinition;
import org.openmrs.module.hivtestingservices.reporting.data.patientContact.definition.PatientContactFacilityLinkedDataDefinition;
import org.openmrs.module.hivtestingservices.reporting.data.patientContact.definition.PatientContactLastTestDateDataDefinition;
import org.openmrs.module.hivtestingservices.reporting.data.patientContact.definition.PatientContactLastTestDateOutcomeDefinition;
import org.openmrs.module.hivtestingservices.reporting.data.patientContact.definition.PatientContactLinkageCCCNumberDefinition;
import org.openmrs.module.hivtestingservices.reporting.data.patientContact.definition.PatientContactLinkageToCareDataDefinition;
import org.openmrs.module.hivtestingservices.reporting.data.patientContact.definition.PatientContactNameDataDefinition;
import org.openmrs.module.hivtestingservices.reporting.data.patientContact.definition.PatientContactPhoneContactDataDefinition;
import org.openmrs.module.hivtestingservices.reporting.data.patientContact.definition.PatientContactRelationshipDataDefinition;
import org.openmrs.module.hivtestingservices.reporting.data.patientContact.definition.PatientContactScreenedForIpvDataDefinition;
import org.openmrs.module.hivtestingservices.reporting.data.patientContact.definition.PatientContactSexDataDefinition;
import org.openmrs.module.hivtestingservices.reporting.data.patientContact.definition.RelatedPatientCCCNumberDataDefinition;
import org.openmrs.module.hivtestingservices.reporting.data.patientContact.definition.RelatedPatientDOBDataDefinition;
import org.openmrs.module.hivtestingservices.reporting.data.patientContact.definition.RelatedPatientFacilityEnrolledDataDefinition;
import org.openmrs.module.hivtestingservices.reporting.data.patientContact.definition.RelatedPatientGenderDataDefinition;
import org.openmrs.module.hivtestingservices.reporting.data.patientContact.definition.RelatedPatientIdDataDefinition;
import org.openmrs.module.hivtestingservices.reporting.data.patientContact.definition.RelatedPatientInCareDataDefinition;
import org.openmrs.module.hivtestingservices.reporting.data.patientContact.definition.RelatedPatientLandMarkDataDefinition;
import org.openmrs.module.hivtestingservices.reporting.data.patientContact.definition.RelatedPatientMaritalStatusDataDefinition;
import org.openmrs.module.hivtestingservices.reporting.data.patientContact.definition.RelatedPatientNameDataDefinition;
import org.openmrs.module.hivtestingservices.reporting.data.patientContact.definition.RelatedPatientPhoneContactDataDefinition;
import org.openmrs.module.hivtestingservices.reporting.data.patientContact.definition.RelatedPatientPopulationTypeDataDefinition;
import org.openmrs.module.hivtestingservices.reporting.data.patientContact.definition.RelatedPatientTestStrategyDataDefinition;
import org.openmrs.module.hivtestingservices.reporting.data.patientContact.definition.RelatedPatientVisitDateDataDefinition;
import org.openmrs.module.hivtestingservices.reporting.definition.PatientContactDataSetDefinition;
import org.openmrs.module.hivtestingservices.reporting.library.PNSReportIndicatorLibrary;
import org.openmrs.module.hivtestingservices.reporting.library.shared.CommonHtsDimensionLibrary;
import org.openmrs.module.kenyacore.report.ReportDescriptor;
import org.openmrs.module.kenyacore.report.ReportUtils;
import org.openmrs.module.kenyacore.report.builder.AbstractReportBuilder;
import org.openmrs.module.kenyacore.report.builder.Builds;
import org.openmrs.module.reporting.common.SortCriteria;
import org.openmrs.module.reporting.data.converter.DateConverter;
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
@Builds({"kenyaemr.hts.common.report.familyTestingRegister"})
public class FamilyTestingRegisterReportBuilder extends AbstractReportBuilder {
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
                ReportUtils.map(datasetColumns(), "startDate=${startDate},endDate=${endDate}"),
                ReportUtils.map(familyTestingDataSet(), "startDate=${startDate},endDate=${endDate}")

        );
    }

    protected DataSetDefinition datasetColumns() {
        PatientContactDataSetDefinition dsd = new PatientContactDataSetDefinition();
        dsd.setName("PNSRegister");
        dsd.setDescription("Patient Contact information");
        dsd.addSortCriteria("Visit Date", SortCriteria.SortDirection.ASC);
        dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        dsd.addParameter(new Parameter("endDate", "End Date", Date.class));

        String paramMapping = "startDate=${startDate},endDate=${endDate}";

        dsd.addColumn("id", new RelatedPatientIdDataDefinition(), "");
        dsd.addColumn("Name", new RelatedPatientNameDataDefinition(), "");
        dsd.addColumn("Sex", new RelatedPatientGenderDataDefinition(), "");
        dsd.addColumn("Age", new RelatedPatientDOBDataDefinition(), "");
        dsd.addColumn("Visit Date", new RelatedPatientVisitDateDataDefinition(),"", new DateConverter(ENC_DATE_FORMAT));
        dsd.addColumn("Telephone No", new RelatedPatientPhoneContactDataDefinition(), "");

        dsd.addColumn("Marital Status", new RelatedPatientMaritalStatusDataDefinition(), null);
        dsd.addColumn("Land Mark", new RelatedPatientLandMarkDataDefinition(), "");

        dsd.addColumn("Population Type", new RelatedPatientPopulationTypeDataDefinition(), null);
        dsd.addColumn("Test Strategy", new RelatedPatientTestStrategyDataDefinition(), null);
        dsd.addColumn("In Care", new RelatedPatientInCareDataDefinition(), null);
        dsd.addColumn("Facility Enrolled", new RelatedPatientFacilityEnrolledDataDefinition(), null);
        dsd.addColumn("CCC Number", new RelatedPatientCCCNumberDataDefinition(), null);
        dsd.addColumn("Contact Name", new PatientContactNameDataDefinition(), "");
        dsd.addColumn("Contact Age", new PatientContactAgeDataDefinition(), "");
        dsd.addColumn("Contact Sex", new PatientContactSexDataDefinition(), "");
        dsd.addColumn("Contact Relationship", new PatientContactRelationshipDataDefinition(), "");
        dsd.addColumn("Contact Phone Number", new PatientContactPhoneContactDataDefinition(), "");
        dsd.addColumn("Contact Baseline Status", new PatientContactBaselineHivStatusDataDefinition(), "");
        dsd.addColumn("Contact Screened for IPV", new PatientContactScreenedForIpvDataDefinition(), "");

        // test columns
        dsd.addColumn("Contact Booking Date", new PatientContactAppointmentForTestDataDefinition(), "", new DateConverter(ENC_DATE_FORMAT));
        dsd.addColumn("Contact Last Test Date", new PatientContactLastTestDateDataDefinition(), "", new DateConverter(ENC_DATE_FORMAT));
        dsd.addColumn("Contact Last Test Outcome", new PatientContactLastTestDateOutcomeDefinition(), "");
        dsd.addColumn("Contact Linked to Care", new PatientContactLinkageToCareDataDefinition(), "");
        dsd.addColumn("Contact Linkage Date", new PatientContactDateLinkedToCareDataDefinition(), "", new DateConverter(ENC_DATE_FORMAT));
        dsd.addColumn("Contact Linkage Facility", new PatientContactFacilityLinkedDataDefinition(), "");
        dsd.addColumn("Contact Linkage CCC Number", new PatientContactLinkageCCCNumberDefinition(), "");


        FamilyTestingCohortDefinition cd = new FamilyTestingCohortDefinition();
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));

        dsd.addRowFilter(cd, paramMapping);
        return dsd;

    }


    protected DataSetDefinition familyTestingDataSet() {
        CohortIndicatorDataSetDefinition cohortDsd = new CohortIndicatorDataSetDefinition();
        cohortDsd.setName("familyTestingDataset");
        cohortDsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cohortDsd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cohortDsd.addDimension("age", ReportUtils.map(commonDimensions.familyTestingReportAgeGroups(), "onDate=${endDate}"));


        ColumnParameters contacts_0_to_14 = new ColumnParameters(null, "0-14", "age=0-14");
        ColumnParameters contacts_15_to_19 = new ColumnParameters(null, "15-19", "age=15-19");
        ColumnParameters contacts_20_and_above = new ColumnParameters(null, "20+", "age=20+");


        ColumnParameters colTotal = new ColumnParameters(null, "Total", "");

        List<ColumnParameters> allAgeDisaggregation = Arrays.asList(
                contacts_0_to_14, contacts_15_to_19,  contacts_20_and_above, colTotal);


        String indParams = "startDate=${startDate},endDate=${endDate}";

        EmrReportingUtils.addRow(cohortDsd, "FT01", "Family Testing - Contacts identified", ReportUtils.map(htsIndicators.familyTestingContactsIdentified(), indParams), allAgeDisaggregation, Arrays.asList("01", "02", "03", "04"));
        EmrReportingUtils.addRow(cohortDsd, "FT02", "Family Testing - contacts eligible", ReportUtils.map(htsIndicators.familyTestingContactsEligibleForTesting(), indParams), allAgeDisaggregation, Arrays.asList("01", "02", "03", "04"));
        EmrReportingUtils.addRow(cohortDsd, "FT03", "Family Testing - contacts tested", ReportUtils.map(htsIndicators.familyTestingContactsTested(), indParams), allAgeDisaggregation, Arrays.asList("01", "02", "03", "04"));
        EmrReportingUtils.addRow(cohortDsd, "FT04", "Family Testing - contacts newly +ve", ReportUtils.map(htsIndicators.familyTestingContactsNewlyPositive(), indParams), allAgeDisaggregation, Arrays.asList("01", "02", "03", "04"));
        EmrReportingUtils.addRow(cohortDsd, "FT05", "Family Testing - contacts linked", ReportUtils.map(htsIndicators.familyTestingContactsLinkedToHaart(), indParams), allAgeDisaggregation, Arrays.asList("01", "02", "03", "04"));

        return cohortDsd;

    }

}