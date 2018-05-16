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

import org.openmrs.Person;
import org.openmrs.module.kenyacore.wrapper.AbstractPersonWrapper;

/**
 * Wrapper class for persons.
 */
public class PersonWrapper extends AbstractPersonWrapper {

	/**
	 * Creates a new wrapper
	 * @param target the target
	 */
	public PersonWrapper(Person target) {
		super(target);
	}

	/**
	 * Gets the telephone contact
	 * @return the telephone number
	 */
	public String getTelephoneContact() {
		return getAsAttribute(PatientWrapper.TELEPHONE_CONTACT);
	}

	/**
	 * Sets the telephone contact
	 * @param value the telephone number
	 */
	public void setTelephoneContact(String value) {
		setAsAttribute(PatientWrapper.TELEPHONE_CONTACT, value);
	}

	/**
	 * Gets the email address
	 * @return the email address
	 */
	public String getEmailAddress() {
		return getAsAttribute(PatientWrapper.EMAIL_ADDRESS);
	}

	/**
	 * Sets the email address
	 * @param value the email address
	 */
	public void setEmailAddress(String value) {
		setAsAttribute(PatientWrapper.EMAIL_ADDRESS, value);
	}
}