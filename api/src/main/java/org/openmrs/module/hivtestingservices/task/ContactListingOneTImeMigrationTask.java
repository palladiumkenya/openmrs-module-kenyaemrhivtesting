/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.hivtestingservices.task;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.hivtestingservices.advice.model.HTSContactListingFormProcessor;
import org.openmrs.module.hivtestingservices.chore.MigrateFamilyHistoryFormContactListingChore;
import org.openmrs.scheduler.tasks.AbstractTask;

/**
 * Periodically refreshes ETL tables
 */
public class ContactListingOneTImeMigrationTask extends AbstractTask {

	private Log log = LogFactory.getLog(getClass());

	/**
	 * @see AbstractTask#execute()
	 */
	public void execute() {
		Context.openSession();
		try {

			/*if (!Context.isAuthenticated()) {
				authenticate();
			}*/
			// check if the task had executed
			AdministrationService service = Context.getAdministrationService();
			GlobalProperty choreStatus = service.getGlobalPropertyObject("hivtestingservices.contactListingMigrationChore");

			// create global property if none exists.
			if (choreStatus == null) {
				System.out.println("Creating property and running chore for contact listing");
				MigrateFamilyHistoryFormContactListingChore processor = new MigrateFamilyHistoryFormContactListingChore();
				processor.perform();
				GlobalProperty property = new GlobalProperty("hivtestingservices.contactListingMigrationChore", "true");
				property.setDescription("Migrates contact previously listed using family history form");
				service.saveGlobalProperty(property);

			} else if (!"true".equals(choreStatus.getPropertyValue())) {
				System.out.println("the task should run.");
				MigrateFamilyHistoryFormContactListingChore processor = new MigrateFamilyHistoryFormContactListingChore();
				processor.perform();
				choreStatus.setPropertyValue("true");
				service.saveGlobalProperty(choreStatus);
			}
		}
		catch (Exception e) {
			throw new IllegalArgumentException("Unable to execute task for contact list migration from family history form", e);
		}
	}
	
}
