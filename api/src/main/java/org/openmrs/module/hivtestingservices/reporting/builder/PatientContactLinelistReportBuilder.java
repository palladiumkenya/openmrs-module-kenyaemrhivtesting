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

import org.openmrs.module.hivtestingservices.reporting.cohort.definition.ContactsWithUndocumentedStatusCohortDefinition;
import org.openmrs.module.hivtestingservices.reporting.data.patientContact.definition.PatientContactAgeDataDefinition;
import org.openmrs.module.hivtestingservices.reporting.data.patientContact.definition.PatientContactAppointmentForTestDataDefinition;
import org.openmrs.module.hivtestingservices.reporting.data.patientContact.definition.PatientContactBaselineHivStatusDataDefinition;
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
import org.openmrs.module.kenyacore.report.HybridReportDescriptor;
import org.openmrs.module.kenyacore.report.ReportDescriptor;
import org.openmrs.module.kenyacore.report.ReportUtils;
import org.openmrs.module.kenyacore.report.builder.AbstractHybridReportBuilder;
import org.openmrs.module.kenyacore.report.builder.Builds;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.common.SortCriteria;
import org.openmrs.module.reporting.data.converter.DateConverter;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@Builds({"kenyaemr.hts.common.report.allContactsWithUndocumentedStatus"})
public class PatientContactLinelistReportBuilder extends AbstractHybridReportBuilder {
    public static final String ENC_DATE_FORMAT = "yyyy/MM/dd";




    @Override
    protected List<Mapped<DataSetDefinition>> buildDataSets(ReportDescriptor reportDescriptor, ReportDefinition reportDefinition) {
        return Arrays.asList(
                ReportUtils.map(datasetColumns(), "")
        );
    }

    @Override
    protected Mapped<CohortDefinition> buildCohort(HybridReportDescriptor hybridReportDescriptor, PatientDataSetDefinition patientDataSetDefinition) {
        return null;
    }

    protected DataSetDefinition datasetColumns() {
        PatientContactDataSetDefinition dsd = new PatientContactDataSetDefinition();
        dsd.setName("AllUndocumentedContacts");
        dsd.setDescription("All Patient Contact information");

        dsd.addColumn("Contact Name", new PatientContactNameDataDefinition(), "");
        dsd.addColumn("Contact Age", new PatientContactAgeDataDefinition(), "");
        dsd.addColumn("Sex", new PatientContactSexDataDefinition(), "");
        dsd.addColumn("Contact Relationship", new PatientContactRelationshipDataDefinition(), "");
        dsd.addColumn("Contact Phone Number", new PatientContactPhoneContactDataDefinition(), "");
        dsd.addColumn("Contact Baseline Status", new PatientContactBaselineHivStatusDataDefinition(), "");
        dsd.addColumn("Contact Screened for IPV", new PatientContactScreenedForIpvDataDefinition(), "");
        dsd.addColumn("Contact Booking Date", new PatientContactAppointmentForTestDataDefinition(), "", new DateConverter(ENC_DATE_FORMAT));

        dsd.addColumn("id", new RelatedPatientIdDataDefinition(), "");
        dsd.addColumn("Name", new RelatedPatientNameDataDefinition(), "");
        dsd.addColumn("Index Sex", new RelatedPatientGenderDataDefinition(), "");
        dsd.addColumn("Index Age", new RelatedPatientDOBDataDefinition(), "");
        dsd.addColumn("Index Telephone No", new RelatedPatientPhoneContactDataDefinition(), "");

        dsd.addColumn("Index Marital Status", new RelatedPatientMaritalStatusDataDefinition(), null);

        dsd.addColumn("Index Population Type", new RelatedPatientPopulationTypeDataDefinition(), null);
        dsd.addColumn("Index CCC Number", new RelatedPatientCCCNumberDataDefinition(), null);


        ContactsWithUndocumentedStatusCohortDefinition cd = new ContactsWithUndocumentedStatusCohortDefinition();

        dsd.addRowFilter(cd, "");
        return dsd;

    }

}