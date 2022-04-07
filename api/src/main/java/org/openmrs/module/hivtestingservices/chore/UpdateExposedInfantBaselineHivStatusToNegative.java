/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.hivtestingservices.chore;

import org.openmrs.api.context.Context;
import org.openmrs.module.kenyacore.chore.AbstractChore;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;

/**
 * Update exposed infant baseline hiv status to Negative
 *
 */
@Component("kenyaemr.chore.updateExposedInfantBaselineHivStatusToNegative")
public class UpdateExposedInfantBaselineHivStatusToNegative extends AbstractChore {
    /**
     * @see AbstractChore#perform(PrintWriter)
     */

    @Override
    public void perform(PrintWriter out) {
        String updateSql = "update  kenyaemr_hiv_testing_patient_contact set baseline_hiv_status = 'Negative' where baseline_hiv_status='Exposed Infant';";
        Context.getAdministrationService().executeSQL(updateSql, false);
        out.println("Updated exposed infant baseline hiv status to Negative");

    }
}
