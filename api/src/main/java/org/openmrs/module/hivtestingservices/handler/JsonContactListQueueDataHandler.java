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

import net.minidev.json.JSONArray;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.*;
import org.openmrs.annotation.Handler;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.hivtestingservices.api.HTSService;
import org.openmrs.module.hivtestingservices.api.PatientContact;
import org.openmrs.module.hivtestingservices.api.service.RegistrationDataService;
import org.openmrs.module.hivtestingservices.exception.QueueProcessorException;
import org.openmrs.module.hivtestingservices.model.QueueData;
import org.openmrs.module.hivtestingservices.model.RegistrationData;
import org.openmrs.module.hivtestingservices.model.handler.QueueDataHandler;
import org.openmrs.module.hivtestingservices.utils.JsonUtils;
import org.openmrs.module.hivtestingservices.utils.PatientSearchUtils;
import org.openmrs.module.idgen.service.IdentifierSourceService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * TODO: Write brief description about the class here.
 */
@Handler(supports = QueueData.class, order = 11)
public class JsonContactListQueueDataHandler implements QueueDataHandler {


    private static final String DISCRIMINATOR_VALUE = "json-contactlist";

    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private final Log log = LogFactory.getLog(JsonContactListQueueDataHandler.class);


    private PatientContact unsavedPatientContact;
    private String payload;
    private QueueProcessorException queueProcessorException;


    @Override
    public void process(final QueueData queueData) throws QueueProcessorException {
        log.info("Processing patient contact form data: " + queueData.getUuid());
        queueProcessorException = new QueueProcessorException();
        try {
            if (validate(queueData)) {
                registerUnsavedPatientContact();
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
        log.info("Processing contact list form data: " + queueData.getUuid());
        queueProcessorException = new QueueProcessorException();
        try {
            payload = queueData.getPayload();
            unsavedPatientContact = new PatientContact();
            populateUnsavedPatientContactFromPayload();
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

    private void populateUnsavedPatientContactFromPayload() {
        setPatientContactFromPayload();
    }

    private void setPatientContactFromPayload(){
        String givenName = JsonUtils.readAsString(payload, "$['s_name']");
        String middleName = JsonUtils.readAsString(payload, "$['f_name']");
        String familyName = JsonUtils.readAsString(payload, "$['o_name']");
        Integer relType = relationshipTypeConverter(JsonUtils.readAsString(payload, "$['contact_relationship']"));
        String baselineStatus = JsonUtils.readAsString(payload, "$['baseline_hiv_status']");
        Date nextTestDate = JsonUtils.readAsDate(payload, "$['booking_date']");
        Date birthDate = JsonUtils.readAsDate(payload, "$['date_of_birth']");
        String sex = gender(JsonUtils.readAsString(payload, "$['sex']"));
        String phoneNumber = JsonUtils.readAsString(payload, "$['phone']");
        Integer maritalStatus = maritalStatusConverter(JsonUtils.readAsString(payload, "$['marital_status']"));
        Integer livingWithPatient = livingWithPartnerConverter(JsonUtils.readAsString(payload, "$['living_with_client']"));
        Integer pnsApproach = pnsApproachConverter(JsonUtils.readAsString(payload, "$['pns_approach']"));
        //String consentedContactListing = JsonUtils.readAsString(resultPayload, "$['sex']");
        String physicalAddress = JsonUtils.readAsString(payload, "$['physical_address']");
        Integer patientRelatedTo = getPatientRelatedToContact(JsonUtils.readAsString(payload, "$['parent']['_id']"));
        Boolean voided= false;

        unsavedPatientContact.setFirstName(givenName);
        unsavedPatientContact.setMiddleName(middleName);
        unsavedPatientContact.setLastName(familyName);
        unsavedPatientContact.setRelationType(relType);
        unsavedPatientContact.setBaselineHivStatus(baselineStatus);
        unsavedPatientContact.setAppointmentDate(nextTestDate);
        unsavedPatientContact.setBirthDate(birthDate);
        unsavedPatientContact.setSex(sex);
        unsavedPatientContact.setPhoneContact(phoneNumber);
        unsavedPatientContact.setMaritalStatus(maritalStatus);
        unsavedPatientContact.setLivingWithPatient(livingWithPatient);
        unsavedPatientContact.setPnsApproach(pnsApproach);
        unsavedPatientContact.setConsentedContactListing(1065);
        unsavedPatientContact.setPhysicalAddress(physicalAddress);
        unsavedPatientContact.setPatientRelatedTo(Context.getPatientService().getPatient(patientRelatedTo));
        unsavedPatientContact.setVoided(voided);
    }

    private void registerUnsavedPatientContact() {
        HTSService htsService = Context.getService(HTSService.class);
        RegistrationDataService registrationDataService = Context.getService(RegistrationDataService.class);
        String temporaryUuid = getPatientContactUuidFromPayload();
        RegistrationData registrationData = registrationDataService.getRegistrationDataByTemporaryUuid(temporaryUuid);
        if (registrationData == null) {
            registrationData = new RegistrationData();
            registrationData.setTemporaryUuid(temporaryUuid);
            try {
                htsService.savePatientContact(unsavedPatientContact);
            } catch (Exception e) {
                e.printStackTrace();


            }
            String assignedUuid = unsavedPatientContact.getUuid();
            registrationData.setAssignedUuid(assignedUuid);
            registrationDataService.saveRegistrationData(registrationData);
        }else {
            log.info("Unable to save, same contact already exist for the patient");
        }
    }

    private String getPatientContactUuidFromPayload(){
        return JsonUtils.readAsString(payload, "$['_id']");
    }

    private Integer getPatientRelatedToContact(String uuid) {
        Integer patientId = null;
        RegistrationDataService regDataService = Context.getService(RegistrationDataService.class);
        RegistrationData regData = regDataService.getRegistrationDataByTemporaryUuid(uuid);
        if(regData != null) {
            Patient p = Context.getPatientService().getPatientByUuid(regData.getAssignedUuid());
            if (p !=null){
                 patientId= p.getPatientId();
            }

        }
        return patientId;

    }

    private Integer relationshipTypeConverter(String relType) {
        Integer relTypeConverter = null;
        if(relType.equalsIgnoreCase("partner")){
            relTypeConverter =163565;
        }else if(relType.equalsIgnoreCase("parent")){
            relTypeConverter =970;
        }else if(relType.equalsIgnoreCase("sibling")){
            relTypeConverter =972;
        }else if(relType.equalsIgnoreCase("child")){
            relTypeConverter =1528;
        }else if(relType.equalsIgnoreCase("spouse")){
            relTypeConverter =5617;
        }else if(relType.equalsIgnoreCase("co-wife")){
            relTypeConverter =162221;
        }else if(relType.equalsIgnoreCase("Injectable drug user")){
            relTypeConverter =157351;
        }
        return relTypeConverter;
    }
    private Integer maritalStatusConverter(String marital_status) {
        Integer civilStatusConverter = null;
        if(marital_status.equalsIgnoreCase("Single")){
            civilStatusConverter =1057;
        }else if(marital_status.equalsIgnoreCase("Divorced")){
            civilStatusConverter =1058;
        }else if(marital_status.equalsIgnoreCase("Widowed")){
            civilStatusConverter =1059;
        }else if(marital_status.equalsIgnoreCase("Married Monogamous")){
            civilStatusConverter =5555;
        }else if(marital_status.equalsIgnoreCase("Married Polygamous")){
            civilStatusConverter =159715;
        }else if(marital_status.equalsIgnoreCase("cohabiting")){
            civilStatusConverter =1060;
        }
        return civilStatusConverter;
    }
    private Integer livingWithPartnerConverter(String livingWithPatient) {
        Integer livingWithPatientConverter = null;
        if(livingWithPatient.equalsIgnoreCase("no")){
            livingWithPatientConverter =1066;
        }else if(livingWithPatient.equalsIgnoreCase("yes")){
            livingWithPatientConverter =1065;
        }else if(livingWithPatient.equalsIgnoreCase("Declined to answer")){
            livingWithPatientConverter =162570;
        }
        return livingWithPatientConverter;
    }

    private Integer pnsApproachConverter(String pns_approach) {
        Integer pnsApproach = null;
        if(pns_approach.equalsIgnoreCase("dual_referral")){
            pnsApproach =162284;
        }else if(pns_approach.equalsIgnoreCase("provider_referral")){
            pnsApproach =163096;
        }else if(pns_approach.equalsIgnoreCase("contract_referral")){
            pnsApproach =161642;
        } else if(pns_approach.equalsIgnoreCase("passive_referral")){
            pnsApproach =160551;
        }
        return pnsApproach;
    }

    private String gender(String gender) {
        String abbriviateGender = null;
        if(gender.equalsIgnoreCase("male")){
            abbriviateGender ="M";
        }
        if(gender.equalsIgnoreCase("female")) {
            abbriviateGender ="F";
        }
        return abbriviateGender;
    }


    @Override
    public boolean accept(final QueueData queueData) {
        return StringUtils.equals(DISCRIMINATOR_VALUE, queueData.getDiscriminator());
    }
    /**
     * Can't save patients unless they have required OpenMRS IDs
     */

}
