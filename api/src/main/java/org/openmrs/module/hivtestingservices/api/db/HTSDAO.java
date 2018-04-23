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
package org.openmrs.module.hivtestingservices.api.db;

import org.openmrs.Patient;
import org.openmrs.module.hivtestingservices.api.ContactTrace;
import org.openmrs.module.hivtestingservices.api.HTSService;
import org.openmrs.module.hivtestingservices.api.PatientContact;
import java.util.List;

/**
 *  Database methods for {@link HTSService}.
 */
public interface HTSDAO {
    public PatientContact savePatientContact(PatientContact patientContact);
    public List<PatientContact> getPatientContactByPatient(Patient patient);
    public List<PatientContact> getPatientContacts();
    public void voidPatientContact(int theId);
    public List<PatientContact> searchPatientContact(String searchName);
    public PatientContact getPatientContactByID (Integer patientContactId);
    public ContactTrace saveClientTrace(ContactTrace contactTrace);
}