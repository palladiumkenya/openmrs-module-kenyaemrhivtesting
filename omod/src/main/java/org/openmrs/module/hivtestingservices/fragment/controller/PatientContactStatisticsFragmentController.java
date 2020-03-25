/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.hivtestingservices.fragment.controller;

import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.hivtestingservices.api.ContactTrace;
import org.openmrs.module.hivtestingservices.api.HTSService;
import org.openmrs.module.hivtestingservices.api.PatientContact;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.FragmentParam;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.openmrs.ui.framework.page.PageRequest;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Patient summary fragment
 */
public class PatientContactStatisticsFragmentController {
	
	static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
	
	public void controller(@FragmentParam("patient") Patient patient,PageRequest pageRequest, UiUtils ui, FragmentModel model) {

		HTSService service = Context.getService(HTSService.class);
		List<PatientContact> contactList = service.getPatientContactByPatient(patient);

		int totalContacts = contactList.size();
		int contacted = 0;
		int notContacted = 0;
		int registered = 0;
		int tested = 0;
		int confirmed = 0;

		for (PatientContact pc : contactList) {
			ContactTrace lastTrace = service.getLastTraceForPatientContact(pc);
			if (lastTrace != null) {
				if(!lastTrace.getStatus().trim().equals("Not Contacted")) {
					contacted++;
				}
			} else {
				notContacted++;
			}

			if (pc.getPatient() != null) {
				registered++;
			}
		}

		model.addAttribute("totalContacts", totalContacts);
		model.addAttribute("patient", patient);
		model.addAttribute("contacted", contacted);
		model.addAttribute("notContacted", notContacted);
		model.addAttribute("registered", registered);
	}
}
