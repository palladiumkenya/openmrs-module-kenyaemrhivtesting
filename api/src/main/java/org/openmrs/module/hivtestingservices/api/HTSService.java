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
package org.openmrs.module.hivtestingservices.api;

import org.openmrs.Cohort;
import org.openmrs.Patient;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.hivtestingservices.advice.model.AOPEncounterEntry;
import org.openmrs.module.reporting.common.DurationUnit;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * This service exposes module's core functionality. It is a Spring managed bean which is configured in moduleApplicationContext.xml.
 * <p>
 * It can be accessed only via Context:<br>
 * <code>
 * Context.getService(HTSService.class).someMethod();
 * </code>
 *
 * @see org.openmrs.api.context.Context
 */
@Transactional
@Component
public interface HTSService extends OpenmrsService {
    public List<PatientContact> getPatientContacts();
    public PatientContact savePatientContact(PatientContact patientContact);
    public List<PatientContact> searchPatientContact(String searchName);
    public void voidPatientContact(int theId);
    public PatientContact getPatientContactByID (Integer patientContactId);
    public List<PatientContact> getPatientContactByPatient(Patient patient);
    public ContactTrace saveClientTrace(ContactTrace contactTrace);
    public List<ContactTrace> getContactTraceByPatientContact(PatientContact patientContact);
    public ContactTrace getPatientContactTraceById (Integer patientContactId);
    public ContactTrace getLastTraceForPatientContact (PatientContact patientContact);
    public AOPEncounterEntry saveAopEncounterEntry(AOPEncounterEntry aopEncounterEntry);
    public AOPEncounterEntry getAopEncounterEntry(Integer entryId);
    public List<AOPEncounterEntry> getAopEncounterEntryList();
    public PatientContact getPatientContactEntryForPatient(Patient patient);
    public Cohort getPatientsWithGender(boolean includeMales, boolean includeFemales, boolean includeUnknownGender);
    public Cohort getPatientsWithAgeRange(Integer minAge, DurationUnit minAgeUnit, Integer maxAge, DurationUnit maxAgeUnit, boolean unknownAgeIncluded, Date effectiveDate);
    public PatientContact getPatientContactByUuid (String uuid);
    public List<PatientContact> getPatientContactListForRegistration();
    public List<PatientContact> getPatientContactsTracedAndBooked();


}
