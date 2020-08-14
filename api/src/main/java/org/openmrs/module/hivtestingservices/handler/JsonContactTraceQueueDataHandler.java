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
package org.openmrs.module.hivtestingservices.handler;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.hivtestingservices.api.HTSService;
import org.openmrs.module.hivtestingservices.api.PatientContact;
import org.openmrs.module.hivtestingservices.api.ContactTrace;
import org.openmrs.module.hivtestingservices.api.service.RegistrationDataService;
import org.openmrs.module.hivtestingservices.exception.QueueProcessorException;
import org.openmrs.module.hivtestingservices.model.QueueData;
import org.openmrs.module.hivtestingservices.model.RegistrationData;
import org.openmrs.module.hivtestingservices.model.handler.QueueDataHandler;
import org.openmrs.module.hivtestingservices.utils.JsonUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * TODO: Write brief description about the class here.
 */
@Handler(supports = QueueData.class, order = 12)
public class JsonContactTraceQueueDataHandler implements QueueDataHandler {


    private static final String DISCRIMINATOR_VALUE = "json-contacttrace";

    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private final Log log = LogFactory.getLog(JsonContactTraceQueueDataHandler.class);


    private ContactTrace unsavedContactTrace;
    private String payload;
    private QueueProcessorException queueProcessorException;


    @Override
    public void process(final QueueData queueData) throws QueueProcessorException {
        log.info("Processing contact trace form data: " + queueData.getUuid());
        queueProcessorException = new QueueProcessorException();
        try {
            if (validate(queueData)) {
                registerUnsavedContactTrace();
            }
        } catch (Exception e) {
            if (!e.getClass().equals(QueueProcessorException.class)) {
                queueProcessorException.addException(new Exception("Exception while process payload ",e));
            }
        } finally {
            if (queueProcessorException.anyExceptions()) {
                throw queueProcessorException;
            }
        }
    }

    @Override
    public boolean validate(QueueData queueData) {
        log.info("Processing contact trace form data: " + queueData.getUuid());
        queueProcessorException = new QueueProcessorException();
        try {
            payload = queueData.getPayload();
            unsavedContactTrace = new ContactTrace();
            populateUnsavedContactTraceFromPayload();
            return true;
        } catch (Exception e) {
            queueProcessorException.addException(new Exception("Exception while validating payload ",e));
            return false;
        } finally {
            if (queueProcessorException.anyExceptions()) {
                throw queueProcessorException;
            }
        }
    }

    @Override
    public String getDiscriminator() {
        return DISCRIMINATOR_VALUE;
    }

    private void populateUnsavedContactTraceFromPayload() {
        setContactTraceFromPayload();
    }

    private void setContactTraceFromPayload(){

        HTSService contact = Context.getService(HTSService.class);
        Date traceDate = JsonUtils.readAsDate(payload, "$['fields']['group_follow_up']['date_last_contact']");
        String contactType = JsonUtils.readAsString(payload, "$['fields']['group_follow_up']['follow_up_type']");
        String status = JsonUtils.readAsString(payload, "$['fields']['group_follow_up']['status_visit']");
        String reasonUncontacted = JsonUtils.readAsString(payload, "$['fields']['group_follow_up']['is_not_available_reason_other']");
        String uniquePatientNo = JsonUtils.readAsString(payload, "$['fields']['group_follow_up']['unique_patient_number']");
        String facilityLinkedTo = JsonUtils.readAsString(payload, "$['fields']['group_follow_up']['facility_linked_to']");
        String healthWorkerHandedTo = JsonUtils.readAsString(payload, "$['fields']['group_follow_up']['health_care_worker_handed_to']");
        String remarks = JsonUtils.readAsString(payload, "$['fields']['group_follow_up']['remarks']");
        String uuid = JsonUtils.readAsString(payload, "$['_id']");
        Integer contactId = getContactId(JsonUtils.readAsString(payload, "$['fields']['inputs']['contact']['_id']"));
        Boolean voided= false;

        unsavedContactTrace.setDate(traceDate);
        unsavedContactTrace.setContactType(contactType);
        unsavedContactTrace.setStatus(status);
        unsavedContactTrace.setReasonUncontacted(reasonUncontacted);
        unsavedContactTrace.setUniquePatientNo(uniquePatientNo);
        unsavedContactTrace.setFacilityLinkedTo(facilityLinkedTo);
        unsavedContactTrace.setHealthWorkerHandedTo(healthWorkerHandedTo);
        unsavedContactTrace.setRemarks(remarks);
        unsavedContactTrace.setPatientContact(contact.getPatientContactByID(contactId));
        unsavedContactTrace.setUuid(uuid);
        unsavedContactTrace.setVoided(voided);
    }

    private void registerUnsavedContactTrace() {
        HTSService htsService = Context.getService(HTSService.class);
        try {
            htsService.saveClientTrace(unsavedContactTrace);
        } catch (Exception e) {
            e.printStackTrace();


        }
    }

    private Integer getContactId(String uuid) {
        Integer contactId = null;
        HTSService htsService = Context.getService(HTSService.class);
        PatientContact patientContact = htsService.getPatientContactByUuid(uuid);

        System.out.println("patientContact=================>"+patientContact);
        if(patientContact != null) {
            contactId= patientContact.getId();
            }
        return contactId;

    }

    @Override
    public boolean accept(final QueueData queueData) {
        return StringUtils.equals(DISCRIMINATOR_VALUE, queueData.getDiscriminator());
    }
    /**
     * Can't save patients unless they have required OpenMRS IDs
     */

}
