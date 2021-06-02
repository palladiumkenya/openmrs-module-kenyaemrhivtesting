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

import org.openmrs.PersonAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.module.hivtestingservices.reporting.ColumnParameters;
import org.openmrs.module.hivtestingservices.reporting.EmrReportingUtils;
import org.openmrs.module.hivtestingservices.reporting.cohort.definition.HIVDiagnosedZeroContactCohortDefinition;
import org.openmrs.module.hivtestingservices.reporting.cohort.definition.PatientContactListCohortDefinition;
import org.openmrs.module.hivtestingservices.reporting.data.converter.KPTypeConverter;
import org.openmrs.module.hivtestingservices.reporting.data.client.definition.HTSLandmarkDataDefinition;
import org.openmrs.module.hivtestingservices.reporting.data.client.definition.HTSMaritalStatusDataDefinition;
import org.openmrs.module.hivtestingservices.reporting.data.client.definition.HTSPopulationTypeDataDefinition;
import org.openmrs.module.hivtestingservices.reporting.data.client.definition.PNSFacilityEnrolledDataDefinition;
import org.openmrs.module.hivtestingservices.reporting.data.client.definition.PNSPatientCCCNumberDataDefinition;
import org.openmrs.module.hivtestingservices.reporting.data.client.definition.PNSPatientInCareDataDefinition;
import org.openmrs.module.hivtestingservices.reporting.data.client.definition.PNSTestStrategyDataDefinition;
import org.openmrs.module.hivtestingservices.reporting.data.converter.MaritalStatusConverter;
import org.openmrs.module.hivtestingservices.reporting.data.patientContact.definition.*;
import org.openmrs.module.hivtestingservices.reporting.definition.PatientContactDataSetDefinition;
import org.openmrs.module.hivtestingservices.reporting.library.PNSReportIndicatorLibrary;
import org.openmrs.module.hivtestingservices.reporting.library.shared.CommonHtsDimensionLibrary;
import org.openmrs.module.kenyacore.report.ReportDescriptor;
import org.openmrs.module.kenyacore.report.ReportUtils;
import org.openmrs.module.kenyacore.report.builder.AbstractReportBuilder;
import org.openmrs.module.kenyacore.report.builder.Builds;
import org.openmrs.module.reporting.common.SortCriteria;
import org.openmrs.module.reporting.common.TimeQualifier;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.data.converter.DateConverter;
import org.openmrs.module.reporting.data.converter.ObjectFormatter;
import org.openmrs.module.reporting.data.encounter.definition.EncounterDatetimeDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientIdDataDefinition;
import org.openmrs.module.reporting.data.person.definition.AgeDataDefinition;
import org.openmrs.module.reporting.data.person.definition.ConvertedPersonDataDefinition;
import org.openmrs.module.reporting.data.person.definition.GenderDataDefinition;
import org.openmrs.module.reporting.data.person.definition.ObsForPersonDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PersonAttributeDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PreferredNameDataDefinition;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.EncounterDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
@Builds({"kenyaemr.hts.common.report.pnsRegister"})
public class PNSRegisterReportBuilder extends AbstractReportBuilder {
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
                ReportUtils.map(htsDataSet(), "startDate=${startDate},endDate=${endDate}"),
                ReportUtils.map(pnsDataSet(), "startDate=${startDate},endDate=${endDate}")

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

        dsd.addColumn("Marital Status", new RelatedPatientMaritalStatusDataDefinition(), null, new MaritalStatusConverter());
        dsd.addColumn("Occupation", new RelatedPatientOccupationDataDefinition(), null);
        dsd.addColumn("Index Client Type", new RelatedIndexClientTypeDataDefinition(), null);
        dsd.addColumn("Land Mark", new RelatedPatientLandMarkDataDefinition(), "");

        dsd.addColumn("Key or Priority PoP", new RelatedPatientKeyOrPriorityPopulationDataDefinition(), null);
        dsd.addColumn("Population Type", new RelatedPatientPopulationTypeDataDefinition(), null,new KPTypeConverter());
        dsd.addColumn("Test Strategy", new RelatedPatientTestStrategyDataDefinition(), null);
        dsd.addColumn("In Care", new RelatedPatientInCareDataDefinition(), null);
        dsd.addColumn("Facility Enrolled", new RelatedPatientFacilityEnrolledDataDefinition(), null);
        dsd.addColumn("CCC Number", new RelatedPatientCCCNumberDataDefinition(), null);
        dsd.addColumn("Contact Name", new PatientContactNameDataDefinition(), "");
        dsd.addColumn("Contact Age", new PatientContactAgeDataDefinition(), "");
        dsd.addColumn("Contact Sex", new PatientContactSexDataDefinition(), "");
        dsd.addColumn("Contact Relationship", new PatientContactRelationshipDataDefinition(), "");
        dsd.addColumn("Contact Occupation", new PatientContactOccupationDataDefinition(), null);
        dsd.addColumn("Contact Phone Number", new PatientContactPhoneContactDataDefinition(), "");
        dsd.addColumn("Contact Baseline Status", new PatientContactBaselineHivStatusDataDefinition(), "");
        dsd.addColumn("Contact Preferred PNS Approach", new PatientContactPNSApproachDataDefinition(), "");
        dsd.addColumn("Contact Screened for IPV", new PatientContactScreenedForIpvDataDefinition(), "");

        // test columns
        dsd.addColumn("Contact Booking Date", new PatientContactAppointmentForTestDataDefinition(), "", new DateConverter(ENC_DATE_FORMAT));
        dsd.addColumn("Contact Consented Testing", new PatientContactConsentedTestingDataDefinition(), "");
        dsd.addColumn("Contact Tested", new PatientContactTestedDataDefinition(), "");
        dsd.addColumn("Contact Last Test Date", new PatientContactLastTestDateDataDefinition(), "", new DateConverter(ENC_DATE_FORMAT));
        dsd.addColumn("Contact Last Test Outcome", new PatientContactLastTestDateOutcomeDefinition(), "");
        dsd.addColumn("Contact Linked to Care", new PatientContactLinkageToCareDataDefinition(), "");
        dsd.addColumn("Contact Linkage Date", new PatientContactDateLinkedToCareDataDefinition(), "", new DateConverter(ENC_DATE_FORMAT));
        dsd.addColumn("Contact Linkage Facility", new PatientContactFacilityLinkedDataDefinition(), "");
        dsd.addColumn("Contact Linkage CCC Number", new PatientContactLinkageCCCNumberDefinition(), "");


        PatientContactListCohortDefinition cd = new PatientContactListCohortDefinition();
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));

        dsd.addRowFilter(cd, paramMapping);
        return dsd;

    }

    protected DataSetDefinition contactlessDatasetColumns() {
        EncounterDataSetDefinition dsd = new EncounterDataSetDefinition();
        dsd.setName("clientsWithNoContacts");
        dsd.setDescription("Clients tested with no contacts listed");
        dsd.addSortCriteria("Visit Date", SortCriteria.SortDirection.ASC);
        dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        dsd.addParameter(new Parameter("endDate", "End Date", Date.class));

        String paramMapping = "startDate=${startDate},endDate=${endDate}";

        DataConverter nameFormatter = new ObjectFormatter("{familyName}, {givenName} {middleName}");
        DataDefinition nameDef = new ConvertedPersonDataDefinition("name", new PreferredNameDataDefinition(), nameFormatter);
        /*PatientIdentifierType upn = MetadataUtils.existing(PatientIdentifierType.class, HivMetadata._PatientIdentifierType.UNIQUE_PATIENT_NUMBER);
        DataConverter identifierFormatter = new ObjectFormatter("{identifier}");
        DataDefinition identifierDef = new ConvertedPatientDataDefinition("identifier", new PatientIdentifierDataDefinition(upn.getName(), upn), identifierFormatter);
*/
        String TELEPHONE_CONTACT = "b2c38640-2603-4629-aebd-3b54f33f1e3a";
        PersonAttributeType phoneNumber = Context.getPersonService().getPersonAttributeTypeByUuid(TELEPHONE_CONTACT);;

        dsd.addColumn("id", new PatientIdDataDefinition(), "");
        dsd.addColumn("Name", nameDef, "");
        dsd.addColumn("Age", new AgeDataDefinition(), "");
        dsd.addColumn("Sex", new GenderDataDefinition(), "");
        dsd.addColumn("Visit Date", new EncounterDatetimeDataDefinition(),"", new DateConverter(ENC_DATE_FORMAT));
        dsd.addColumn("Telephone No", new PersonAttributeDataDefinition(phoneNumber), "");
        dsd.addColumn("Marital Status", new HTSMaritalStatusDataDefinition(), null);
        dsd.addColumn("Occupation", new ObsForPersonDataDefinition("Occupation", TimeQualifier.LAST, Context.getConceptService().getConcept(1542), null, null), "", null);
        dsd.addColumn("Land Mark", new HTSLandmarkDataDefinition(), "");

        dsd.addColumn("Population Type", new HTSPopulationTypeDataDefinition(), null);
        dsd.addColumn("Test Strategy", new PNSTestStrategyDataDefinition(), null);
        dsd.addColumn("In Care", new PNSPatientInCareDataDefinition(), null);
        dsd.addColumn("Facility Enrolled", new PNSFacilityEnrolledDataDefinition(), null);
        dsd.addColumn("CCC Number", new PNSPatientCCCNumberDataDefinition(), null);


        HIVDiagnosedZeroContactCohortDefinition cd = new HIVDiagnosedZeroContactCohortDefinition();
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));

        dsd.addRowFilter(cd, paramMapping);
        return dsd;

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
                colInfants, children_1_to_9,  f_10_to_14, m_10_to_14,f_15_to_19, m_15_to_19,
                f_20_to_24,m_20_to_24,f_25_to_29, m_25_to_29, f_30_to_34, m_30_to_34, f_35_to_39, m_35_to_39, f_40_to_44, m_40_to_44, f_45_to_49, m_45_to_49 ,f_50_and_above,m_50_and_above , colTotal);


        String indParams = "startDate=${startDate},endDate=${endDate}";

        EmrReportingUtils.addRow(cohortDsd, "PNS01", "Tested", ReportUtils.map(htsIndicators.htsTested(), indParams), allAgeDisaggregation, Arrays.asList("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21"));
        EmrReportingUtils.addRow(cohortDsd, "PNS02", "Newly Tested", ReportUtils.map(htsIndicators.htsNewlyTested(), indParams), allAgeDisaggregation, Arrays.asList("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21"));
        EmrReportingUtils.addRow(cohortDsd, "PNS03", "Newly Tested who received results", ReportUtils.map(htsIndicators.htsNewlyTestedWhoReceivedResults(), indParams), allAgeDisaggregation, Arrays.asList("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21"));
        EmrReportingUtils.addRow(cohortDsd, "PNS04", "Total Tested who received results", ReportUtils.map(htsIndicators.htsTotaltestedWhoReceivedResults(), indParams), allAgeDisaggregation, Arrays.asList("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21"));
        EmrReportingUtils.addRow(cohortDsd, "PNS05", "Positives", ReportUtils.map(htsIndicators.htsTotalPositive(), indParams), allAgeDisaggregation, Arrays.asList("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21"));
        EmrReportingUtils.addRow(cohortDsd, "PNS06", "Newly Positive", ReportUtils.map(htsIndicators.htsNewlyPositive(), indParams), allAgeDisaggregation, Arrays.asList("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21"));

        return cohortDsd;

    }

    protected DataSetDefinition pnsDataSet() {
        CohortIndicatorDataSetDefinition cohortDsd = new CohortIndicatorDataSetDefinition();
        cohortDsd.setName("pnsSummary");
        cohortDsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cohortDsd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cohortDsd.addDimension("age", ReportUtils.map(commonDimensions.pnsReportAgeGroups(), "onDate=${endDate}"));
        cohortDsd.addDimension("gender", ReportUtils.map(commonDimensions.contactGender()));


        ColumnParameters colInfants = new ColumnParameters(null, "<1", "age=<1");
        ColumnParameters children_1_to_9 = new ColumnParameters(null, "1-9", "age=1-9");

        ColumnParameters m_10_to_14 = new ColumnParameters(null, "10-14, Male", "gender=M|age=10-14");
        ColumnParameters f_10_to_14 = new ColumnParameters(null, "10-14, Female", "gender=F|age=10-14");

        ColumnParameters m_15_to_19 = new ColumnParameters(null, "15-19, Male", "gender=M|age=15-19");
        ColumnParameters f_15_to_19 = new ColumnParameters(null, "15-19, Female", "gender=F|age=15-19");

        ColumnParameters m_20_to_24 = new ColumnParameters(null, "20-24, Male", "gender=M|age=20-24");
        ColumnParameters f_20_to_24 = new ColumnParameters(null, "20-24, Female", "gender=F|age=20-24");

        ColumnParameters m_25_to_29 = new ColumnParameters(null, "25-29, Male", "gender=M|age=25-29");
        ColumnParameters f_25_to_29 = new ColumnParameters(null, "25-29, Female", "gender=F|age=25-29");

        ColumnParameters m_30_to_49 = new ColumnParameters(null, "30-49, Male", "gender=M|age=30-49");
        ColumnParameters f_30_to_49 = new ColumnParameters(null, "30-49, Female", "gender=F|age=30-49");

        ColumnParameters m_50_and_above = new ColumnParameters(null, "50+, Male", "gender=M|age=50+");
        ColumnParameters f_50_and_above = new ColumnParameters(null, "50+, Female", "gender=F|age=50+");

        ColumnParameters colTotal = new ColumnParameters(null, "Total", "");

        List<ColumnParameters> allAgeDisaggregation = Arrays.asList(
                colInfants, children_1_to_9,  f_10_to_14, m_10_to_14,f_15_to_19, m_15_to_19,
                f_20_to_24,m_20_to_24,f_25_to_29, m_25_to_29, f_30_to_49, m_30_to_49, f_50_and_above,m_50_and_above , colTotal);

        String indParams = "startDate=${startDate},endDate=${endDate}";

        EmrReportingUtils.addRow(cohortDsd, "PNS01", "Contacts Identified", ReportUtils.map(htsIndicators.pnsContactsIdentified(), indParams), allAgeDisaggregation, Arrays.asList("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15"));
        EmrReportingUtils.addRow(cohortDsd, "PNS02", "Contacts Known Positive", ReportUtils.map(htsIndicators.pnsContactsKnownPositive(), indParams), allAgeDisaggregation, Arrays.asList("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15"));
        EmrReportingUtils.addRow(cohortDsd, "PNS03", "Contacts Eligible", ReportUtils.map(htsIndicators.pnsContactsEligibleForTesting(), indParams), allAgeDisaggregation, Arrays.asList("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15"));
        EmrReportingUtils.addRow(cohortDsd, "PNS04", "Contacts Tested", ReportUtils.map(htsIndicators.pnsContactsTested(), indParams), allAgeDisaggregation, Arrays.asList("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15"));
        EmrReportingUtils.addRow(cohortDsd, "PNS05", "Contacts who turned positive", ReportUtils.map(htsIndicators.pnsContactsNewlyPositive(), indParams), allAgeDisaggregation, Arrays.asList("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15"));
        EmrReportingUtils.addRow(cohortDsd, "PNS06", "Contacts Linked to HAART", ReportUtils.map(htsIndicators.pnsContactsLinkedToHaart(), indParams), allAgeDisaggregation, Arrays.asList("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15"));
        return cohortDsd;

    }
}