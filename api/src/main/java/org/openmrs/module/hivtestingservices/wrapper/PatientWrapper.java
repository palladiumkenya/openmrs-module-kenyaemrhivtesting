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

package org.openmrs.module.hivtestingservices.wrapper;

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.module.kenyacore.wrapper.AbstractPatientWrapper;

/**
 * Wrapper class for patients. Unfortunately this can't extend both AbstractPatientWrapper and PersonWrapper so we add a
 * PersonWrapper as a property.
 */
public class PatientWrapper extends AbstractPatientWrapper {

	/**
	 * Lifted from KenyaEMR common metadata
	 */
	public static final String NEXT_OF_KIN_ADDRESS = "7cf22bec-d90a-46ad-9f48-035952261294";
	public static final String NEXT_OF_KIN_CONTACT = "342a1d39-c541-4b29-8818-930916f4c2dc";
	public static final String NEXT_OF_KIN_NAME = "830bef6d-b01f-449d-9f8d-ac0fede8dbd3";
	public static final String NEXT_OF_KIN_RELATIONSHIP = "d0aa9fd1-2ac5-45d8-9c5e-4317c622c8f5";
	public static final String SUBCHIEF_NAME = "40fa0c9c-7415-43ff-a4eb-c7c73d7b1a7a";
	public static final String TELEPHONE_CONTACT = "b2c38640-2603-4629-aebd-3b54f33f1e3a";
	public static final String EMAIL_ADDRESS = "b8d0b331-1d2d-4a9a-b741-1816f498bdb6";
	public static final String ALTERNATE_PHONE_CONTACT = "94614350-84c8-41e0-ac29-86bc107069be";
	public static final String NEAREST_HEALTH_CENTER = "27573398-4651-4ce5-89d8-abec5998165c";
	public static final String GUARDIAN_FIRST_NAME = "8caf6d06-9070-49a5-b715-98b45e5d427b";
	public static final String GUARDIAN_LAST_NAME = "0803abbd-2be4-4091-80b3-80c6940303df";

	public static final String CWC_NUMBER = "1dc8b419-35f2-4316-8d68-135f0689859b";
	public static final String DISTRICT_REGISTRATION_NUMBER = "d8ee3b8c-a8fc-4d6b-af6a-9423be5f8906";
	public static final String HEI_UNIQUE_NUMBER = "0691f522-dd67-4eeb-92c8-af5083baf338";
	public static final String OPENMRS_ID = "dfacd928-0370-4315-99d7-6ec1c9f7ae76";
	public static final String NATIONAL_ID = "49af6cdc-7968-4abb-bf46-de10d7f4859f";
	public static final String OLD = "8d79403a-c2cc-11de-8d13-0010c6dffd0f";
	public static final String PATIENT_CLINIC_NUMBER = "b4d66522-11fc-45c7-83e3-39a1af21ae0d";
	public static final String UNIQUE_PATIENT_NUMBER = "05ee9cf4-7242-4a17-b4d4-00f707265c8a";
	public static final String NATIONAL_UNIQUE_PATIENT_IDENTIFIER = "f85081e2-b4be-4e48-b3a4-7994b69bb101";
	
	private PersonWrapper person;

	/**
	 * Creates a new wrapper
	 * @param target the target
	 */
	public PatientWrapper(Patient target) {
		super(target);

		this.person = new PersonWrapper(target);
	}

	/**
	 * Gets the person wrapper
	 * @return the wrapper
	 */
	public PersonWrapper getPerson() {
		return person;
	}

	/**
	 * Gets the medical record number
	 * @return the identifier value
	 */
	public String getMedicalRecordNumber() {
		return getAsIdentifier(OPENMRS_ID);
	}

	/**
	 * Gets the patient clinic number
	 * @return the identifier value
	 */
	public String getPatientClinicNumber() {
		return getAsIdentifier(PATIENT_CLINIC_NUMBER);
	}

	/**
	 * Sets the patient clinic number
	 * @param value the identifier value
	 * @param location the identifier location
	 */
	public void setPatientClinicNumber(String value, Location location) {
		setAsIdentifier(PATIENT_CLINIC_NUMBER, value, location);
	}

	/**
	 * Gets the unique patient number
	 * @return the identifier value
	 */
	public String getUniquePatientNumber() {
		return getAsIdentifier(UNIQUE_PATIENT_NUMBER);
	}

	/**
	 * Sets the unique patient number
	 * @param value the identifier value
	 * @param location the identifier location
	 */
	public void setUniquePatientNumber(String value, Location location) {
		setAsIdentifier(UNIQUE_PATIENT_NUMBER, value, location);
	}

	/**
	 * Gets the national id number
	 * @return the identifier value
	 */
	public String getNationalIdNumber() {
		return getAsIdentifier(NATIONAL_ID);
	}

	/**
	 * Sets the national id number
	 * @param value the identifier value
	 * @param location the identifier location
	 */
	public void setNationalIdNumber(String value, Location location) {
		setAsIdentifier(NATIONAL_ID, value, location);
	}

	/**
	 * Gets the address of next of kin
	 * @return the address
	 */
	public String getNextOfKinAddress() {
		return getAsAttribute(NEXT_OF_KIN_ADDRESS);
	}

	/**
	 * Sets the address of next of kin
	 * @param value the address
	 */
	public void setNextOfKinAddress(String value) {
		setAsAttribute(NEXT_OF_KIN_ADDRESS, value);
	}

	/**
	 * Gets the telephone contact of next of kin
	 * @return the telephone number
	 */
	public String getNextOfKinContact() {
		return getAsAttribute(NEXT_OF_KIN_CONTACT);
	}

	/**
	 * Sets the telephone contact of next of kin
	 * @param value telephone number
	 */
	public void setNextOfKinContact(String value) {
		setAsAttribute(NEXT_OF_KIN_CONTACT, value);
	}

	/**
	 * Gets the name of next of kin
	 * @return the name
	 */
	public String getNextOfKinName() {
		return getAsAttribute(NEXT_OF_KIN_NAME);
	}

	/**
	 * Sets the name of next of kin
	 * @param value the name
	 */
	public void setNextOfKinName(String value) {
		setAsAttribute(NEXT_OF_KIN_NAME, value);
	}

	/**
	 * Gets the relationship of next of kin
	 * @return the relationship
	 */
	public String getNextOfKinRelationship() {
		return getAsAttribute(NEXT_OF_KIN_RELATIONSHIP);
	}

	/**
	 * Sets the relationship of next of kin
	 * @param value the relationship
	 */
	public void setNextOfKinRelationship(String value) {
		setAsAttribute(NEXT_OF_KIN_RELATIONSHIP, value);
	}

	/**
	 * Gets the sub chief name
	 * @return the name
	 */
	public String getSubChiefName() {
		return getAsAttribute(SUBCHIEF_NAME);
	}
	/**
	 * Sets the sub chief name
	 * @param value the name
	 */
	public void setSubChiefName(String value) {
		setAsAttribute(SUBCHIEF_NAME, value);
	}

	/**
	 * Gets patient's alternate phone contact
	 * @return phone contact
	 */
	public String getAlternativePhoneContact() {
		return getAsAttribute(ALTERNATE_PHONE_CONTACT);
	}
	/**
	 * Sets patient's alternative phone contact
	 */
	public void setAlternativePhoneContact(String contact) {
		setAsAttribute(ALTERNATE_PHONE_CONTACT, contact);
	}
	/**
	 * Gets patient's alternate phone contact
	 * @return phone contact
	 */
	public String getNearestHealthFacility() {
		return getAsAttribute(NEAREST_HEALTH_CENTER);
	}
	/**
	 * Sets patient's alternative phone contact
	 */
	public void setNearestHealthFacility(String facility) {
		setAsAttribute(NEAREST_HEALTH_CENTER, facility);
	}

	/**
	 * Gets patient's alternate phone contact
	 * @return phone contact
	 */
	public String getEmailAddress() {
		return getAsAttribute(EMAIL_ADDRESS);
	}
	/**
	 * Sets patient's alternative phone contact
	 */
	public void setEmailAddress(String email) {
		setAsAttribute(EMAIL_ADDRESS, email);
	}

	/**
	 * Gets guardian's first name
	 * @return guardian's first name
	 */
	public String getGuardianFirstName() {
		return getAsAttribute(GUARDIAN_FIRST_NAME);
	}
	/**
	 * Sets guardian's first name
	 */
	public void setGuardianFirstName(String value) {
		setAsAttribute(GUARDIAN_FIRST_NAME, value);
	}

	/**
	 * Gets guardian's last name
	 * @return guardian's last name
	 */
	public String getGuardianLastName() {
		return getAsAttribute(GUARDIAN_LAST_NAME);
	}
	/**
	 * Sets guardian's last name
	 */
	public void setGuardianLastName(String value) {
		setAsAttribute(GUARDIAN_LAST_NAME, value);
	}
}