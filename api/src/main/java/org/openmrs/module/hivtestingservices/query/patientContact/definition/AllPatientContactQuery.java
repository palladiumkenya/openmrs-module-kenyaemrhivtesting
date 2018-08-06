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
package org.openmrs.module.hivtestingservices.query.patientContact.definition;

import org.openmrs.Visit;
import org.openmrs.module.hivtestingservices.api.PatientContact;
import org.openmrs.module.reporting.definition.configuration.ConfigurationPropertyCachingStrategy;
import org.openmrs.module.reporting.evaluation.caching.Caching;
import org.openmrs.module.reporting.query.BaseQuery;
import org.openmrs.module.reporting.query.visit.definition.VisitQuery;

/**
 * PatientContact Query for obtaining all patient contacts
 */
@Caching(strategy=ConfigurationPropertyCachingStrategy.class)
public class AllPatientContactQuery extends BaseQuery<PatientContact> implements PatientContactQuery {

    public static final long serialVersionUID = 1L;

    //***** CONSTRUCTORS *****

    /**
     * Default Constructor
     */
    public AllPatientContactQuery() {
        super();
    }

    //***** INSTANCE METHODS *****

    /**
     * @see Object#toString()
     */
    public String toString() {
        return "All Patient Contact Query";
    }
}
