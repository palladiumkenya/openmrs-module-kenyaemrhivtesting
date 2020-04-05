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
package org.openmrs.module.hivtestingservices.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.hivtestingservices.api.shr.CovidLabDataExchange;
import org.openmrs.module.hivtestingservices.api.shr.MhealthDataExchange;
import org.openmrs.module.hivtestingservices.api.shr.MiddlewareRequest;
import org.openmrs.module.hivtestingservices.api.shr.OutgoingPatientSHR;
import org.openmrs.module.hivtestingservices.api.shr.SHRAuthentication;
import org.openmrs.module.hivtestingservices.api.shr.SHRUtils;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * The main controller.
 */
@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/shr")
public class SHRRestController extends BaseRestController {

	protected final Log log = LogFactory.getLog(getClass());

	/**
	 * gets SHR based on patient/client internal ID
	 * @param request
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/getcasecontacts")
	@ResponseBody
	public Object receiveSHR(HttpServletRequest request) {
		Integer patientID=null;
		String requestBody = null;
		MiddlewareRequest thisRequest = null;
		try {
			requestBody = SHRUtils.fetchRequestBody(request.getReader());
		} catch (IOException e) {
			return new SimpleObject().add("ServerResponse", "Error extracting request body");
		}
		try {
			thisRequest = new ObjectMapper().readValue(requestBody, MiddlewareRequest.class);
		} catch (IOException e) {
			e.printStackTrace();
			return new SimpleObject().add("ServerResponse", "Error reading patient id: " + requestBody);
		}
		patientID=Integer.parseInt(thisRequest.getPatientID());
		if (patientID != 0) {
			OutgoingPatientSHR shr = new OutgoingPatientSHR(patientID);
			return shr.getContactListCht().toString();

		}
		return new SimpleObject().add("identification", "No patient id specified in the request: Got this: => " + request.getParameter("patientID"));
	}


	/**
	 * gets payload with list of contacts for followup
	 * @param request
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/contactlist") // end point for mhealth kenya
	@ResponseBody
	public Object getMhealthContactList(HttpServletRequest request) {

		MhealthDataExchange e = new MhealthDataExchange();
		return e.getContacts().toString();
	}

	/**
	 * Processes SHR read from smart card
	 * @param request
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/processshr")
	@ResponseBody
	public Object prepareSHR(HttpServletRequest request) {

		String encryptedSHR=null;
		try {
			encryptedSHR = SHRUtils.fetchRequestBody(request.getReader());//request.getParameter("encryptedSHR") != null? request.getParameter("encryptedSHR"): null;
		} catch (IOException e) {
			return new SimpleObject().add("ServerResponse", "Error extracting request body");
		}

		/*IncomingPatientSHR shr = new IncomingPatientSHR(encryptedSHR);
		return shr.processIncomingSHR();*/
		return null;
	}

	/**
	 * Return active lab request
	 * @param request
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/getlabrequest")
	@ResponseBody
	public Object getActiveLabRequests(HttpServletRequest request) {
			CovidLabDataExchange e = new CovidLabDataExchange();
			return e.getCovidLabRequests().toString();
	}


	/**
	 * Generates P-Smart SHR based on psmart card serial number
	 * @param request
	 * @param cardSerialNo
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/getshrusingcardserial/{cardSerialNo}")
	@ResponseBody
	public Object getShrUsingCardSerial(HttpServletRequest request, @PathVariable("cardSerialNo") String cardSerialNo) {
		if(cardSerialNo != null) {
			OutgoingPatientSHR shr = new OutgoingPatientSHR(cardSerialNo);
			return shr.patientIdentification().toString();
		}
		return null;
	}


	/**
	 * Handle authentication
	 * @param request
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/authenticateuser")
	@ResponseBody
	public Object userAuthentication(HttpServletRequest request) {
		String requestBody = null;
		String userName=null;
		String pwd = null;
		MiddlewareRequest thisRequest = null;
		try {
			requestBody = SHRUtils.fetchRequestBody(request.getReader());//request.getParameter("encryptedSHR") != null? request.getParameter("encryptedSHR"): null;
		} catch (IOException e) {
			return new SimpleObject().add("ServerResponse", "Error extracting request body");
		}

		try {
			thisRequest = new ObjectMapper().readValue(requestBody, MiddlewareRequest.class);
		} catch (IOException e) {
			e.printStackTrace();
			return new SimpleObject().add("ServerResponse", "Error parsing request body: " + requestBody);
		}
		userName = thisRequest.getUserName();
		pwd = thisRequest.getPwd();

		return SHRAuthentication.authenticateUser(userName.trim(), pwd.trim()).toString();
	}

	/**
	 * processes incoming covid lab results
	 * @param request
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/labresults") // end point for CHAI kenya
	@ResponseBody
	public Object processCovidLabResults(HttpServletRequest request) {
		String requestBody = null;
		try {
			requestBody = SHRUtils.fetchRequestBody(request.getReader());
		} catch (IOException e) {
			return new SimpleObject().add("ServerResponse", "Error extracting request body");
		}

		if (requestBody != null) {
			CovidLabDataExchange shr = new CovidLabDataExchange();
			return shr.processIncomingLabResults(requestBody);

		}
		return new SimpleObject().add("Report", "The request could not be interpreted properly");
	}


	@RequestMapping(method = RequestMethod.POST, value = "/mhealthreport") // end point for CHAI kenya
	@ResponseBody
	public Object processMhealthTraceReports(HttpServletRequest request) {
		String requestBody = null;
		try {
			requestBody = SHRUtils.fetchRequestBody(request.getReader());
		} catch (IOException e) {
			return new SimpleObject().add("ServerResponse", "Error extracting request body");
		}

		if (requestBody != null) {
			MhealthDataExchange shr = new MhealthDataExchange();
			return shr.processMhealthPayload(requestBody);

		}
		return new SimpleObject().add("Contact trace reports", "It seems there are no reports to process");
	}
	/**
	 * processes incoming contact tracing information
	 * @param request
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/contacttracing") // end point mhealth
	@ResponseBody
	public Object processContactTracingInfo(HttpServletRequest request) {
		String requestBody = null;
		try {
			requestBody = SHRUtils.fetchRequestBody(request.getReader());
		} catch (IOException e) {
			return new SimpleObject().add("ServerResponse", "Error extracting request body");
		}

		if (requestBody != null) {
			CovidLabDataExchange shr = new CovidLabDataExchange();
			return shr.processIncomingContactTracingInfo(requestBody);

		}
		return new SimpleObject().add("identification", "No patient id specified in the request: Got this: => " + request.getParameter("patientID"));
	}



	/**
	 * @see BaseRestController#getNamespace()
	 */

	@Override
	public String getNamespace() {
		return "v1/hivtestingservices";
	}

}
