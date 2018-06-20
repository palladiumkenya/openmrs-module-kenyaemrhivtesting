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
import org.openmrs.module.hivtestingservices.reporting.cohort.definition.HIVDiagnosedZeroContactCohortDefinition;
import org.openmrs.module.hivtestingservices.reporting.cohort.definition.PatientContactListCohortDefinition;
import org.openmrs.module.hivtestingservices.reporting.data.client.definition.HTSLandmarkDataDefinition;
import org.openmrs.module.hivtestingservices.reporting.data.client.definition.HTSMaritalStatusDataDefinition;
import org.openmrs.module.hivtestingservices.reporting.data.client.definition.HTSPopulationTypeDataDefinition;
import org.openmrs.module.hivtestingservices.reporting.data.client.definition.PNSFacilityEnrolledDataDefinition;
import org.openmrs.module.hivtestingservices.reporting.data.client.definition.PNSPatientCCCNumberDataDefinition;
import org.openmrs.module.hivtestingservices.reporting.data.client.definition.PNSPatientInCareDataDefinition;
import org.openmrs.module.hivtestingservices.reporting.data.client.definition.PNSTestStrategyDataDefinition;
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
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.EncounterDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
@Builds({"kenyaemr.hts.common.report.pnsRegister"})
public class PNSRegisterReportBuilder extends AbstractReportBuilder {
    public static final String ENC_DATE_FORMAT = "yyyy/MM/dd";
    public static final String DATE_FORMAT = "dd/MM/yyyy";

    @Override
    protected List<Parameter> getParameters(ReportDescriptor reportDescriptor) {
        return Arrays.asList(
                new Parameter("startDate", "Start Date", Date.class),
                new Parameter("endDate", "End Date", Date.class)
        );
    }

    @Override
    protected List<Mapped<DataSetDefinition>> buildDataSets(ReportDescriptor reportDescriptor, ReportDefinition reportDefinition) {
        return Arrays.asList(
                ReportUtils.map(datasetColumns(), "startDate=${startDate},endDate=${endDate}"),
                ReportUtils.map(contactlessDatasetColumns(), "startDate=${startDate},endDate=${endDate}")
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
}