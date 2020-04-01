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
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.openmrs.api.context.Context;
import org.openmrs.module.hivtestingservices.advice.model.HTSContactListingFormProcessor;
import org.openmrs.module.hivtestingservices.api.shr.CovidLabDataExchange;
import org.openmrs.scheduler.tasks.AbstractTask;

/**
 * Periodically refreshes ETL tables
 */
public class PushLabRequestsTask extends AbstractTask {

	private Log log = LogFactory.getLog(getClass());

	/**
	 * @see AbstractTask#execute()
	 */
	public void execute() {
		Context.openSession();
		try {

			if (!Context.isAuthenticated()) {
				authenticate();
			}
			String serverUrl = "http://lab-2.test.nascop.org/api/covid/save_multiple";
			CloseableHttpClient httpClient = HttpClients.createDefault();

			CovidLabDataExchange e = new CovidLabDataExchange();
			String payload = e.getCovidLabRequests().toString();

			try
			{
				//Define a postRequest request
				HttpPost postRequest = new HttpPost(serverUrl);

				//Set the API media type in http content-type header
				postRequest.addHeader("content-type", "application/json");

				//Set the request post body
				StringEntity userEntity = new StringEntity(payload);
				postRequest.setEntity(userEntity);

				//Send the request; It will immediately return the response in HttpResponse object if any
				HttpResponse response = httpClient.execute(postRequest);

				//verify the valid error code first
				int statusCode = response.getStatusLine().getStatusCode();
				if (statusCode != 201)
				{
					throw new RuntimeException("Failed with HTTP error code : " + statusCode);
				}
			}
			finally
			{
				//Important: Close the connect
				httpClient.close();
			}

		}
		catch (Exception e) {
			throw new IllegalArgumentException("Unable to execute task that pushes lab requests", e);
		}
	}
	
}
