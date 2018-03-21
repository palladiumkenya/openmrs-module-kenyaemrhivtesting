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

import org.openmrs.module.hivtestingservices.api.HTSService;
<<<<<<< HEAD
import org.openmrs.module.hivtestingservices.api.impl.PatientContact;
import java.util.List;

=======
import java.util.List;
import org.openmrs.module.hivtestingservices.api.PatientContact;
>>>>>>> 0720e461083128f6a35a78a9423b8a334c83c83a
/**
 *  Database methods for {@link HTSService}.
 */
public interface HTSDAO {
<<<<<<< HEAD
    void persistPatientContact(PatientContact patientContact);
    List<PatientContact> getPatientContacts();
    void deletePatientContact(int theId);
    List<PatientContact> searchPatientContact(String searchName);
=======
     void persistPatientContact(PatientContact patientContact);
     List<PatientContact> getPatientContact();
     void deletePatientContact(int theId);
     List<PatientContact> searchPatientContact(String searchName);
>>>>>>> 0720e461083128f6a35a78a9423b8a334c83c83a
}