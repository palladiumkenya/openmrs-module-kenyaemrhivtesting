/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.hivtestingservices.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.openmrs.api.context.Context;
import org.openmrs.module.hivtestingservices.advice.model.HTSContactListingFormProcessor;
import org.openmrs.module.hivtestingservices.api.shr.CovidLabDataExchange;
import org.openmrs.module.hivtestingservices.api.shr.MhealthDataExchange;
import org.openmrs.scheduler.tasks.AbstractTask;

import java.util.Map;

/**
 * Periodically refreshes ETL tables
 */
public class PushContactsToMhealthTask extends AbstractTask {

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
            String serverUrl = "http://ears-covid.mhealthkenya.co.ke/api/emr/receiver";
            String loginUrl = "http://ears-covid.mhealthkenya.co.ke/api/user/login";
            boolean successful = false;
            CloseableHttpClient loginClient = HttpClients.createDefault();
            String token = null;

            JsonNodeFactory factory = JsonNodeFactory.instance;
            try {
                //Define a postRequest request
                HttpPost loginRequest = new HttpPost(loginUrl);
                ObjectNode loginObject = factory.objectNode();
                loginObject.put("username", "Admin");
                loginObject.put("password", "Admin!@#");

                //Set the API media type in http content-type header
                loginRequest.addHeader("content-type", "application/json");

                //Set the request post body
                StringEntity userEntity = new StringEntity(loginObject.toString());
                loginRequest.setEntity(userEntity);

                //Send the request; It will immediately return the response in HttpResponse object if any
                HttpResponse response = loginClient.execute(loginRequest);

                //verify the valid error code first
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != 200) {
                    throw new RuntimeException("Failed with HTTP error code : " + statusCode);
                }
                HttpEntity entity = response.getEntity();
                String responseString = EntityUtils.toString(entity, "UTF-8");
                Map<String, Object> responseMap = new ObjectMapper().readValue(responseString, Map.class);

                Boolean success = (Boolean) responseMap.get("success");
                token = responseMap.get("token").toString();
                successful = success;

            } finally {
                //Important: Close the connect
                loginClient.close();
            }

            CloseableHttpClient httpClient = HttpClients.createDefault();

            MhealthDataExchange e = new MhealthDataExchange();
            String payload = e.getContacts().toString();
            String API_KEY = token;

            if (successful && API_KEY != null) {
                try {
                    //Define a postRequest request
                    HttpPost postRequest = new HttpPost(serverUrl);

                    //Set the API media type in http content-type header
                    postRequest.addHeader("content-type", "application/json");
                    postRequest.addHeader("Authorization", "Bearer " + API_KEY);
                    //postRequest.addHeader("apikey", API_KEY);

                    //Set the request post body
                    StringEntity userEntity = new StringEntity(payload);
                    postRequest.setEntity(userEntity);

                    //Send the request; It will immediately return the response in HttpResponse object if any
                    HttpResponse response = httpClient.execute(postRequest);

                    //verify the valid error code first
                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode != 200) {
                        throw new RuntimeException("Failed with HTTP error code : " + statusCode);
                    }
                    System.out.println("Successfully executed the task that pushes lab requests");
                    log.info("Successfully executed the task that pushes lab requests");
                } finally {
                    //Important: Close the connect
                    httpClient.close();
                }
            } else {
                System.out.println("Login to the mhealth application was not successful");
                log.info("Login to the mhealth application was not successful");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Mhealth POST task could not be executed!", e);
        }
    }

}
