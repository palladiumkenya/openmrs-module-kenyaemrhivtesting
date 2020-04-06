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

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.openmrs.api.context.Context;
import org.openmrs.module.hivtestingservices.api.shr.MedicMobileDataExchange;
import org.openmrs.scheduler.tasks.AbstractTask;

/**
 * Periodically refreshes ETL tables
 */
public class PushContactsToMedicMobileTask extends AbstractTask {

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
            String serverUrl = "https://covid-demo-ke.dev.medicmobile.org/medic/_bulk_docs";

            CloseableHttpClient httpClient = HttpClients.createDefault();
            /*CredentialsProvider provider = new BasicCredentialsProvider();
            provider.setCredentials(
                    AuthScope.ANY,
                    new UsernamePasswordCredentials("national_tracer", "Test@user!")
            );
            CloseableHttpClient httpClient = HttpClientBuilder.create()
                    .setDefaultCredentialsProvider(provider)
                    .build();*/

            MedicMobileDataExchange e = new MedicMobileDataExchange();
            String payload = e.getContacts().toString();

                try {
                    //Define a postRequest request
                    HttpPost postRequest = new HttpPost(serverUrl);

                    //Set the API media type in http content-type header
                    postRequest.addHeader("content-type", "application/json");

                    String auth = "national_tracer" + ":" + "Test@user!";
                    byte[] encodedAuth = Base64.encodeBase64(
                            auth.getBytes("UTF-8"));
                    String authHeader = "Basic " + new String(encodedAuth);
                    postRequest.setHeader(HttpHeaders.AUTHORIZATION, authHeader);

                    //Set the request post body
                    StringEntity userEntity = new StringEntity(payload);
                    postRequest.setEntity(userEntity);

                    //Send the request; It will immediately return the response in HttpResponse object if any
                    HttpResponse response = httpClient.execute(postRequest);

                    //verify the valid error code first
                    int statusCode = response.getStatusLine().getStatusCode();

                    if (statusCode != 200 && statusCode != 201) {
                        throw new RuntimeException("Failed with HTTP error code : " + statusCode);
                    }
                    System.out.println("Successfully pushed contacts to Medic Mobile CHT");
                    log.info("Successfully pushed contacts to Medic Mobile CHT");
                } finally {
                    //Important: Close the connect
                    httpClient.close();
                }
        } catch (Exception e) {
            throw new IllegalArgumentException("Medic Mobile contact list POST task could not be executed!", e);
        }
    }

}
