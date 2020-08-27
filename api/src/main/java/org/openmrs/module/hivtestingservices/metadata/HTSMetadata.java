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

import org.openmrs.PatientIdentifierType;
import org.openmrs.module.metadatadeploy.bundle.AbstractMetadataBundle;
import org.springframework.stereotype.Component;

import static org.openmrs.module.metadatadeploy.bundle.CoreConstructors.form;
import static org.openmrs.module.metadatadeploy.bundle.CoreConstructors.globalProperty;
import static org.openmrs.module.metadatadeploy.bundle.CoreConstructors.patientIdentifierType;
import static org.openmrs.module.metadatadeploy.bundle.CoreConstructors.relationshipType;

/**
 * Metadata constants
 */
@Component
public class HTSMetadata extends AbstractMetadataBundle {

	public static final String MODULE_ID = "hivtestingservices";
	public static final String MEDIC_MOBILE_LAST_PATIENT_CONTACT_ENTRY = "medic.lastSavedPatientContact";
	public static final String MEDIC_MOBILE_SERVER_URL = "medic.chtServerUrl";
	public static final String MEDIC_MOBILE_LAST_PATIENT_ENTRY = "medic.lastSavedPersonId";
	public static final String MEDIC_MOBILE_USER = "medic.chtUser";
	public static final String MEDIC_MOBILE_PWD = "medic.chtPwd";
	public static final String TELEPHONE_CONTACT = "b2c38640-2603-4629-aebd-3b54f33f1e3a";


	public static final class _EncounterType {
		public static final String HTS = "9c0a7a57-62ff-4f75-babe-5835b0e921b7";
	}
	public static final class _Form {
		public static final String HTS_SCREENING_FORM = "04295648-7606-11e8-adc0-fa7ae01bbebc";
		public static final String HTS_PROVIDER_REPORTS = "aa923c72-96ed-11e8-9eb6-529269fb1459"; // this is used in mUzima app for provider reports

	}
	public static final class _RelationshipType {
		public static final String SPOUSE = "d6895098-5d8d-11e3-94ee-b35a4132a5e3";
		public static final String GUARDIAN_DEPENDANT = "5f115f62-68b7-11e3-94ee-6bef9086de92";
		public static final String PARTNER = "007b765f-6725-4ae9-afee-9966302bace4";
		public static final String CO_WIFE = "2ac0d501-eadc-4624-b982-563c70035d46";
		public static final String INJECTABLE_DRUG_USER = "58da0d1e-9c89-42e9-9412-275cef1e0429";

	}

	public static final class _PatientIdentifierType {
		public static final String CHT_RECORD_UUID = "c6552b22-f191-4557-a432-1f4df872d473";
	}

	@Override
	public void install() throws Exception {
		// doing this in the scheduled task so that previous value set is preserved
		//install(globalProperty(MODULE_ID +".contactListingMigrationChore", "Migrates contact previously listed using family history form", "false"));
		install(form("HTS Eligibility Screening Form", "Form used to screen clients prior to HIV testing", _EncounterType.HTS, "1", _Form.HTS_SCREENING_FORM));
		install(form("HTS Provider Reports form", "Form used to develop provider reports", _EncounterType.HTS, "1", _Form.HTS_PROVIDER_REPORTS));
		install(relationshipType("Guardian", "Dependant", "One that guards, watches over, or protects", _RelationshipType.GUARDIAN_DEPENDANT));
		install(relationshipType("Spouse", "Spouse", "A spouse is a partner in a marriage, civil union, domestic partnership or common-law marriage a male spouse is a husband and a female spouse is a wife", _RelationshipType.SPOUSE));
		install(relationshipType("Partner", "Partner", "Someone I had sex with for fun without commitment to a relationship", _RelationshipType.PARTNER));
		install(relationshipType("Co-wife", "Co-wife", "Female member spouse in a polygamist household", _RelationshipType.CO_WIFE));
		install(relationshipType("Injectable-drug-user", "Injectable-druguser", "Those who share drug needles", _RelationshipType.INJECTABLE_DRUG_USER));

		install(globalProperty(MEDIC_MOBILE_LAST_PATIENT_CONTACT_ENTRY, "Id for the last case contact entry for CHT", null));
		install(globalProperty(MEDIC_MOBILE_LAST_PATIENT_ENTRY, "Medic last patient entry ID", null));
		install(globalProperty(MEDIC_MOBILE_SERVER_URL, "Server URL for Medic Mobile CHT", null));
		install(globalProperty(MEDIC_MOBILE_USER, "Medic Mobile CHT user", null));
		install(globalProperty(MEDIC_MOBILE_PWD, "Medic Mobile CHT pwd", null));

		install(patientIdentifierType("CHT Record Reference UUID", "Record reference UUID from CHT",
				null, null, null,
				PatientIdentifierType.LocationBehavior.NOT_USED, false, _PatientIdentifierType.CHT_RECORD_UUID));
	}

}
