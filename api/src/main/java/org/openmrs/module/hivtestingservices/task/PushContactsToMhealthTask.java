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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.hivtestingservices.api.shr.MhealthDataExchange;
import org.openmrs.module.hivtestingservices.metadata.HTSMetadata;
import org.openmrs.scheduler.tasks.AbstractTask;

import java.util.List;
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

            GlobalProperty gpLoginUrl = Context.getAdministrationService().getGlobalPropertyObject(HTSMetadata.MHEALTH_LOGIN_URL);
            GlobalProperty gpLoginUser = Context.getAdministrationService().getGlobalPropertyObject(HTSMetadata.MHEALTH_USER);
            GlobalProperty gpLoginPwd = Context.getAdministrationService().getGlobalPropertyObject(HTSMetadata.MHEALTH_PWD);

            GlobalProperty gpPostContactUrl = Context.getAdministrationService().getGlobalPropertyObject(HTSMetadata.MHEALTH_POST_CONTACT_URL);

            String loginUrl = gpLoginUrl.getPropertyValue();
            String user = gpLoginUser.getPropertyValue();
            String pwd = gpLoginPwd.getPropertyValue();

            String serverUrl = gpPostContactUrl.getPropertyValue();

            if (StringUtils.isBlank(loginUrl) || StringUtils.isBlank(user) || StringUtils.isBlank(pwd) || StringUtils.isBlank(serverUrl)) {
                System.out.println("No credentials for posting contacts to Mhealth application");
                return;
            }

            GlobalProperty lastPatientEntry = Context.getAdministrationService().getGlobalPropertyObject(HTSMetadata.MHEALTH_LAST_PATIENT_ENTRY);
            String lastQuarantineIdsql = "select max(patient_id) last_id from patient where voided=0;";
            List<List<Object>> lastQuarantineRs = Context.getAdministrationService().executeSQL(lastQuarantineIdsql, true);
            Integer lastPatientId = (Integer) lastQuarantineRs.get(0).get(0);
            lastPatientId = lastPatientId != null ? lastPatientId : 0;

            GlobalProperty lastContactEntry = Context.getAdministrationService().getGlobalPropertyObject(HTSMetadata.MHEALTH_LAST_PATIENT_CONTACT_ENTRY);
            String lastContactsql = "select max(id) last_id from kenyaemr_hiv_testing_patient_contact where voided=0;";
            List<List<Object>> lastContactIdResult = Context.getAdministrationService().executeSQL(lastContactsql, true);
            Integer lastContactId = (Integer) lastContactIdResult.get(0).get(0);
            lastContactId = lastContactId != null ? lastContactId : 0;

            String lastContactIdStr = lastContactEntry != null && lastContactEntry.getValue() != null ? lastContactEntry.getValue().toString() : "";
            Integer gpLastContactId = StringUtils.isNotBlank(lastContactIdStr) ? Integer.parseInt(lastContactIdStr) : 0;

            String lastPatientIdStr = lastPatientEntry != null && lastPatientEntry.getValue() != null ? lastPatientEntry.getValue().toString() : "";
            Integer gpLastPatientId = StringUtils.isNotBlank(lastPatientIdStr) ? Integer.parseInt(lastPatientIdStr) : 0;

            MhealthDataExchange e = new MhealthDataExchange();
            ObjectNode payload = e.getContacts(gpLastPatientId, lastPatientId, gpLastContactId, lastContactId);

            ArrayNode contacts = (ArrayNode) payload.get("contacts");

            if (contacts.size() < 1) {
                System.out.println("No records found for mhealth contact tracing. Skipping the post operation");
                return;

            }

            boolean successful = false;
            CloseableHttpClient loginClient = HttpClients.createDefault();
            String token = null;

            JsonNodeFactory factory = JsonNodeFactory.instance;
            try {
                //Define a postRequest request
                HttpPost loginRequest = new HttpPost(loginUrl);
                ObjectNode loginObject = factory.objectNode();
                loginObject.put("username", user.trim());
                loginObject.put("password", pwd.trim());

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

            String API_KEY = token;

            if (successful && API_KEY != null) {
                try {
                    //Define a postRequest request
                    HttpPost postRequest = new HttpPost(serverUrl);

                    //Set the API media type in http content-type header
                    postRequest.addHeader("content-type", "application/json");
                    postRequest.addHeader("Authorization", "Bearer " + API_KEY);

                    //Set the request post body
                    StringEntity userEntity = new StringEntity(payload.toString());
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

                lastContactEntry.setPropertyValue(lastContactId.toString());
                lastPatientEntry.setPropertyValue(lastPatientId.toString());
                Context.getAdministrationService().saveGlobalProperty(lastPatientEntry);
                Context.getAdministrationService().saveGlobalProperty(lastContactEntry);

            } else {
                System.out.println("Login to the mhealth application was not successful");
                log.info("Login to the mhealth application was not successful");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Mhealth POST task could not be executed!", e);
        }
    }

}
