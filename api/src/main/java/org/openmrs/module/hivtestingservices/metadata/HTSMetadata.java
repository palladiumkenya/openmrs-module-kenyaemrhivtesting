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

package org.openmrs.module.hivtestingservices.metadata;

import org.openmrs.module.metadatadeploy.bundle.AbstractMetadataBundle;
import org.springframework.stereotype.Component;

import static org.openmrs.module.metadatadeploy.bundle.CoreConstructors.form;

/**
 * Metadata constants
 */
@Component
public class HTSMetadata extends AbstractMetadataBundle {

	public static final String MODULE_ID = "hivtestingservices";

	public static final class _EncounterType {
		public static final String HTS = "9c0a7a57-62ff-4f75-babe-5835b0e921b7";
	}
	public static final class _Form {
		public static final String HTS_SCREENING_FORM = "04295648-7606-11e8-adc0-fa7ae01bbebc";
		public static final String HTS_PROVIDER_REPORTS = "aa923c72-96ed-11e8-9eb6-529269fb1459"; // this is used in mUzima app for provider reports

	}

	@Override
	public void install() throws Exception {
		// doing this in the scheduled task so that previous value set is preserved
		//install(globalProperty(MODULE_ID +".contactListingMigrationChore", "Migrates contact previously listed using family history form", "false"));
		install(form("HTS Screening Form", "Form used to screen clients prior to HIV testing", _EncounterType.HTS, "1", _Form.HTS_SCREENING_FORM));
		install(form("HTS Provider Reports", "Form used to develop provider reports", _EncounterType.HTS, "1", _Form.HTS_PROVIDER_REPORTS));

	}
}
