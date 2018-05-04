/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 * <p>
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 * <p>
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.hivtestingservices.api.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.hivtestingservices.advice.model.AOPEncounterEntry;
import org.openmrs.module.hivtestingservices.api.ContactTrace;
import org.openmrs.module.hivtestingservices.api.HTSService;
import org.openmrs.module.hivtestingservices.api.PatientContact;
import org.openmrs.module.hivtestingservices.api.db.hibernate.HibernateHTSDAO;

import java.util.List;

/**
 * It is a default implementation of {@link HTSService}.
 */

public class HTSServiceImpl extends BaseOpenmrsService implements HTSService {

    protected final Log log = LogFactory.getLog(this.getClass());

    private HibernateHTSDAO patientContactDAO;

    @Override
    public List<PatientContact> getPatientContacts() {
        return patientContactDAO.getPatientContacts();
    }

    @Override
    public PatientContact savePatientContact(PatientContact patientContact) {
        return patientContactDAO.savePatientContact(patientContact);
    }

    public void setPatientContactDAO(HibernateHTSDAO patientContactDAO) {
        this.patientContactDAO = patientContactDAO;
    }

    public HibernateHTSDAO getPatientContactDAO() {
        return patientContactDAO;
    }

    public HibernateHTSDAO getDao() {
        return patientContactDAO;
    }

    @Override
    public List<PatientContact> searchPatientContact(String searchName) {

        return patientContactDAO.searchPatientContact(searchName);
    }

    @Override
    public void voidPatientContact(int theId) {
        patientContactDAO.voidPatientContact(theId);
    }

    @Override
    public PatientContact getPatientContactByID(Integer patientContactId) {
        return patientContactDAO.getPatientContactByID(patientContactId);
    }

    @Override
    public List<PatientContact> getPatientContactByPatient(Patient patient) {
        return patientContactDAO.getPatientContactByPatient(patient);
    }

    @Override
    public ContactTrace saveClientTrace(ContactTrace contactTrace) {

        return patientContactDAO.saveClientTrace(contactTrace);
    }

    @Override
    public ContactTrace getPatientContactTraceById(Integer patientContactTraceId) {
        return patientContactDAO.getPatientContactTraceById(patientContactTraceId);
    }

    @Override
    public ContactTrace getLastTraceForPatientContact(PatientContact patientContact) {
        return patientContactDAO.getLastTraceForPatientContact(patientContact);
    }

    @Override
    public AOPEncounterEntry saveAopEncounterEntry(AOPEncounterEntry aopEncounterEntry) {
        return patientContactDAO.saveAopEncounterEntry(aopEncounterEntry);
    }

    @Override
    public AOPEncounterEntry getAopEncounterEntry(Integer entryId) {
        return patientContactDAO.getAopEncounterEntry(entryId);
    }

    @Override
    public List<AOPEncounterEntry> getAopEncounterEntryList() {
        return patientContactDAO.getAopEncounterEntryList();
    }

    @Override
    public List<ContactTrace> getContactTraceByPatientContact(PatientContact patientContact) {

        return patientContactDAO.getContactTraceByPatientContact(patientContact);
    }

    @Override
    public void onStartup() {

    }

    @Override
    public void onShutdown() {

    }


}
