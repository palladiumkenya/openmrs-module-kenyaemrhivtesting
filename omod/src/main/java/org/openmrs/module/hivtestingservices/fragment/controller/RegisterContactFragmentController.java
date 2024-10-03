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

package org.openmrs.module.hivtestingservices.fragment.controller;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Concept;
import org.openmrs.GlobalProperty;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.hivtestingservices.api.HTSService;
import org.openmrs.module.hivtestingservices.api.PatientContact;
import org.openmrs.module.hivtestingservices.util.Utils;
import org.openmrs.module.hivtestingservices.validator.TelephoneNumberValidator;
import org.openmrs.module.hivtestingservices.wrapper.PatientWrapper;
import org.openmrs.module.idgen.service.IdentifierSourceService;
import org.openmrs.module.kenyaui.form.AbstractWebForm;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.BindParams;
import org.openmrs.ui.framework.annotation.FragmentParam;
import org.openmrs.ui.framework.annotation.MethodParam;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.RequestParam;
//import org.openmrs.module.kenyaemr.Dictionary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Controller for creating and editing patients in the registration app
 */
public class RegisterContactFragmentController {

	// We don't record cause of death, but data model requires a concept
	private static final String CAUSE_OF_DEATH_PLACEHOLDER = "1067AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	private static final String CIVIL_STATUS = "1054AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	private static final String OCCUPATION = "1542AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	private static final String EDUCATION = "1712AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	private static final String PRIMARY_EDUCATION = "1713AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	private static final String SECONDARY_EDUCATION = "1714AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	private static final String COLLEGE_UNIVERSITY_POLYTECHNIC = "159785AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	private static final String NONE = "1107AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	private static final String FARMER = "1538AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	private static final String TRADER = "1539AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	private static final String EMPLOYEE = "1540AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	private static final String STUDENT = "159465AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	private static final String DRIVER = "159466AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	private static final String OTHER_NON_CODED = "5622AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	private static final String IN_SCHOOL = "5629AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	private static final String ORPHAN = "1174AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	private static final String COUNTRY = "165657AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";

	static final String MARRIED_POLYGAMOUS = "159715AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	static final String MARRIED_MONOGAMOUS = "5555AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	static final String DIVORCED = "1058AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	static final String WIDOWED = "1059AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	static final String LIVING_WITH_PARTNER = "1060AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	static final String NEVER_MARRIED = "1057AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	static final String UNKNOWN = "1067AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	static final String YES = "1065AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	static final String NO = "1066AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	ConceptService conceptService = Context.getConceptService();
	PersonService personService = Context.getPersonService();

	String siblingRelType = "8d91a01c-c2cc-11de-8d13-0010c6dffd0f";
	String parentChildRelType = "8d91a210-c2cc-11de-8d13-0010c6dffd0f";
	String spouseRelType = "d6895098-5d8d-11e3-94ee-b35a4132a5e3";
	String partnerRelType = "007b765f-6725-4ae9-afee-9966302bace4";
	String cowifeRelType = "2ac0d501-eadc-4624-b982-563c70035d46";
	String injectableDrugUserRelType = "58da0d1e-9c89-42e9-9412-275cef1e0429";
	String snsRelType = "76edc1fe-c5ce-4608-b326-c8ecd1020a73";

	/**
	 * Main controller method
	 * @param patientContact the patientContact
	 * @param person the person (may be null)
	 * @param model the model
	 */
	public void controller(@FragmentParam(value = "patientContact") PatientContact patientContact,
						   @FragmentParam(value = "person", required = false) Person person,
						   FragmentModel model) {

		model.addAttribute("patientRelatedTo", patientContact.getPatientRelatedTo());
		model.addAttribute("patientContact", patientContact);
		model.addAttribute("command", newPatientContactForm(patientContact));

		model.addAttribute("civilStatusConcept", conceptService.getConceptByUuid(CIVIL_STATUS));
		model.addAttribute("occupationConcept", conceptService.getConceptByUuid(OCCUPATION));
		model.addAttribute("educationConcept", conceptService.getConceptByUuid(EDUCATION));

		// create list of counties

		List<String> countyList = new ArrayList<String>();
		List<Location> locationList = Context.getLocationService().getAllLocations();
		for(Location loc: locationList) {
			String locationCounty = loc.getCountyDistrict();
			if(!StringUtils.isEmpty(locationCounty) && !StringUtils.isBlank(locationCounty)) {
				countyList.add(locationCounty);
			}
		}

		Set<String> uniqueCountyList = new HashSet<String>(countyList);
		model.addAttribute("countyList", uniqueCountyList);

		//create list of countries
		List<Concept> countryList = new ArrayList<Concept>();
		for(Concept countryConcept : conceptService.getConcept(165657).getSetMembers()) {
			countryList.add(countryConcept);
		}

		model.addAttribute("countryOptions", countryList);

		// create list of next of kin relationship

		List<String> nextOfKinRelationshipOptions = Arrays.asList(
			new String("Partner"),
			new String("Spouse"),
			new String("Father"),
			new String("Mother"),
			new String("Grandmother"),
			new String("Grandfather"),
			new String("Sibling"),
			new String("Child"),
			new String("Aunt"),
			new String("Uncle"),
			new String("Guardian"),
			new String("Friend"),
			new String("Co-worker")
		);

		model.addAttribute("nextOfKinRelationshipOptions", nextOfKinRelationshipOptions);

		// Create list of education answer concepts
		List<Concept> educationOptions = new ArrayList<Concept>();
		educationOptions.add(conceptService.getConceptByUuid(NONE));
		educationOptions.add(conceptService.getConceptByUuid(PRIMARY_EDUCATION));
		educationOptions.add(conceptService.getConceptByUuid(SECONDARY_EDUCATION));
		educationOptions.add(conceptService.getConceptByUuid(COLLEGE_UNIVERSITY_POLYTECHNIC));
		model.addAttribute("educationOptions", educationOptions);

		/*Create list of occupation answer concepts  */
		List<Concept> occupationOptions = new ArrayList<Concept>();
		occupationOptions.add(conceptService.getConceptByUuid(FARMER));
		occupationOptions.add(conceptService.getConceptByUuid(TRADER));
		occupationOptions.add(conceptService.getConceptByUuid(EMPLOYEE));
		occupationOptions.add(conceptService.getConceptByUuid(STUDENT));
		occupationOptions.add(conceptService.getConceptByUuid(DRIVER));
		occupationOptions.add(conceptService.getConceptByUuid(NONE));
		occupationOptions.add(conceptService.getConceptByUuid(OTHER_NON_CODED));
		model.addAttribute("occupationOptions", occupationOptions);


		// Create a list of marital status answer concepts
		List<Concept> maritalStatusOptions = new ArrayList<Concept>();
		maritalStatusOptions.add(conceptService.getConceptByUuid(MARRIED_POLYGAMOUS));
		maritalStatusOptions.add(conceptService.getConceptByUuid(MARRIED_MONOGAMOUS));
		maritalStatusOptions.add(conceptService.getConceptByUuid(DIVORCED));
		maritalStatusOptions.add(conceptService.getConceptByUuid(WIDOWED));
		maritalStatusOptions.add(conceptService.getConceptByUuid(LIVING_WITH_PARTNER));
		maritalStatusOptions.add(conceptService.getConceptByUuid(NEVER_MARRIED));
		model.addAttribute("maritalStatusOptions", maritalStatusOptions);

		// Create a list of cause of death answer concepts
		List<Concept> causeOfDeathOptions = new ArrayList<Concept>();
		causeOfDeathOptions.add(conceptService.getConceptByUuid(UNKNOWN));
		model.addAttribute("causeOfDeathOptions", causeOfDeathOptions);

		// Create a list of yes_no options
		List<Concept> yesNoOptions = new ArrayList<Concept>();
		yesNoOptions.add(conceptService.getConceptByUuid(YES));
		yesNoOptions.add(conceptService.getConceptByUuid(NO));
		model.addAttribute("yesNoOptions", yesNoOptions);
	}

	/**
	 * Saves the patient being edited by this form
	 * @param form the edit patient form
	 * @param ui the UI utils
	 * @return a simple object { patientId }
	 */
	public SimpleObject savePatient(@MethodParam("newPatientContactForm") @BindParams EditPatientForm form, UiUtils ui) {
		ui.validate(form, form, null);

		Patient patient = form.save();

		// if this patient is the current user i need to refresh the current user
		if (patient.getPersonId().equals(Context.getAuthenticatedUser().getPerson().getPersonId())) {
			Context.refreshAuthenticatedUser();
		}

		return SimpleObject.create("id", patient.getId());
	}

	/**
	 * Creates an edit patient form
	 * @param patientContact the PatientContact
	 * @return the form
	 */
	public EditPatientForm newPatientContactForm(@RequestParam(value = "patientContact") PatientContact patientContact) {

			return new EditPatientForm(patientContact); // For creating patient and person from scratch

	}

	/**
	 * The form command object for editing patients
	 */
	public class EditPatientForm extends AbstractWebForm {

		private Person original;
		private Patient patientRelatedTo;
		private PatientContact patientContact;
		private Location location;
		private PersonName personName;
		private Date birthdate;
		private Boolean birthdateEstimated;
		private String gender;
		private PersonAddress personAddress;
		private Concept maritalStatus;
		private Concept occupation;
		private Concept education;
		private Concept inSchool;
		private Concept orphan;
		private Boolean dead = false;
		private Date deathDate;
		private String nationalIdNumber;
		private String patientClinicNumber;
		private String uniquePatientNumber;
		private String telephoneContact;
		private String nameOfNextOfKin;
		private String nextOfKinRelationship;
		private String nextOfKinContact;
		private String nextOfKinAddress;
		private String subChiefName;
		private String alternatePhoneContact;
		private String nearestHealthFacility;
		private String emailAddress;
		private String guardianFirstName;
		private String guardianLastName;
		private Concept country;
		//private Obs savedCountry;

		/**
		 * Creates an edit form for a new patient
		 */
		public EditPatientForm() {
			location = getDefaultLocation();
			original = new Person();
			personName = new PersonName();
			personAddress = new PersonAddress();
		}

		/**
		 * Creates an edit form for an existing person
		 */
		public EditPatientForm(PatientContact contact) {
			this();

			if (contact.getLastName() != null)
				personName.setFamilyName(contact.getLastName());
			if (contact.getFirstName() != null)
				personName.setGivenName(contact.getFirstName());
			if (contact.getMiddleName() != null)
				personName.setMiddleName(contact.getMiddleName());

			personName.setPerson(original);

			if (contact.getPhysicalAddress() != null) {
				personAddress.setAddress1(contact.getPhysicalAddress());
				personAddress.setPerson(original);
			}
			if (contact.getPhoneContact() != null) {
				telephoneContact = contact.getPhoneContact();
			}

			if (contact.getMaritalStatus() != null) {
				maritalStatus = conceptService.getConcept(contact.getMaritalStatus());
			}

			gender = contact.getSex();
			birthdate = contact.getBirthDate();
			birthdateEstimated = true;
			dead = false;

			// set related patient
			patientRelatedTo = contact.getPatientRelatedTo();

		}

		/**
		 * @see org.springframework.validation.Validator#validate(java.lang.Object,
		 *      Errors)
		 */
		@Override
		public void validate(Object target, Errors errors) {
			require(errors, "personName.givenName");
			require(errors, "personName.familyName");
			require(errors, "gender");
			require(errors, "birthdate");

			require(errors, "maritalStatus");
			require(errors, "occupation");
			require(errors, "education");
			require(errors, "personAddress.cityVillage");
			require(errors, "telephoneContact");
			require(errors, "personAddress.countyDistrict");
			require(errors, "personAddress.stateProvince");
			require(errors, "personAddress.address4");
			require(errors, "country");

			// Require death details if patient is deceased
			if (dead) {
				require(errors, "deathDate");

				if (deathDate != null) {
					if (birthdate != null && deathDate.before(birthdate)) {
						errors.rejectValue("deathDate", "Cannot be before birth date");
					}
					if (deathDate.after(new Date())) {
						errors.rejectValue("deathDate", "Cannot be in the future");
					}
				}
			} else if (deathDate != null) {
				errors.rejectValue("deathDate", "Must be empty if patient not deceased");
			}

			if (StringUtils.isNotBlank(telephoneContact)) {
				validateField(errors, "telephoneContact", new TelephoneNumberValidator());
			}
			if (StringUtils.isNotBlank(nextOfKinContact)) {
				validateField(errors, "nextOfKinContact", new TelephoneNumberValidator());
			}

			validateField(errors, "personAddress");

			validateIdentifierField(errors, "nationalIdNumber", Utils.NATIONAL_ID);
			validateIdentifierField(errors, "patientClinicNumber", Utils.PATIENT_CLINIC_NUMBER);
			validateIdentifierField(errors, "uniquePatientNumber", Utils.UNIQUE_PATIENT_NUMBER);

			// check birth date against future dates and really old dates
			if (birthdate != null) {
				if (birthdate.after(new Date()))
					errors.rejectValue("birthdate", "error.date.future");
				else {
					Calendar c = Calendar.getInstance();
					c.setTime(new Date());
					c.add(Calendar.YEAR, -120); // person cannot be older than 120 years old
					if (birthdate.before(c.getTime())) {
						errors.rejectValue("birthdate", "error.date.nonsensical");
					}
				}
			}
		}

		/**
		 * Validates an identifier field
		 * @param errors
		 * @param field
		 * @param idTypeUuid
		 */
		protected void validateIdentifierField(Errors errors, String field, String idTypeUuid) {
			String value = (String) errors.getFieldValue(field);

			if (StringUtils.isNotBlank(value)) {
				PatientIdentifierType idType = Context.getPatientService().getPatientIdentifierTypeByUuid(idTypeUuid);
				if (!value.matches(idType.getFormat())) {
					errors.rejectValue(field, idType.getFormatDescription());
				}

				PatientIdentifier stub = new PatientIdentifier(value, idType, null);

				if (original != null && original.isPatient()) { // Editing an existing patient
					stub.setPatient((Patient) original);
				}

				if (Context.getPatientService().isIdentifierInUseByAnotherPatient(stub)) {
					errors.rejectValue(field, "In use by another patient");
				}
			}
		}

		/**
		 * @see AbstractWebForm#save()
		 */
		@Override
		public Patient save() {

			Patient toSave = new Patient(); // Creating a new patient and person
			toSave.setGender(gender);
			toSave.setBirthdate(birthdate);
			toSave.setBirthdateEstimated(birthdateEstimated);
			toSave.setDead(dead);
			toSave.setDeathDate(deathDate);
			toSave.setCauseOfDeath(dead ? conceptService.getConceptByUuid(CAUSE_OF_DEATH_PLACEHOLDER) : null);
			toSave.addName(personName);
			toSave.addAddress(personAddress);


			PatientWrapper wrapper = new PatientWrapper(toSave);
			wrapper.getPerson();

			wrapper.getPerson().setTelephoneContact(telephoneContact);
			wrapper.setNationalIdNumber(nationalIdNumber, location);
			wrapper.setPatientClinicNumber(patientClinicNumber, location);
			wrapper.setUniquePatientNumber(uniquePatientNumber, location);
			wrapper.setNextOfKinName(nameOfNextOfKin);
			wrapper.setNextOfKinRelationship(nextOfKinRelationship);
			wrapper.setNextOfKinContact(nextOfKinContact);
			wrapper.setNextOfKinAddress(nextOfKinAddress);
			wrapper.setSubChiefName(subChiefName);
			wrapper.setAlternativePhoneContact(alternatePhoneContact);
			wrapper.setNearestHealthFacility(nearestHealthFacility);
			wrapper.setEmailAddress(emailAddress);
			wrapper.setGuardianFirstName(guardianFirstName);
			wrapper.setGuardianLastName(guardianLastName);

			// Make sure everyone gets an OpenMRS ID
			PatientIdentifierType openmrsIdType = MetadataUtils.existing(PatientIdentifierType.class, PatientWrapper.OPENMRS_ID);
			PatientIdentifier openmrsId = toSave.getPatientIdentifier(openmrsIdType);

			if (openmrsId == null) {
				String generated = Context.getService(IdentifierSourceService.class).generateIdentifier(openmrsIdType, "Registration");
				openmrsId = new PatientIdentifier(generated, openmrsIdType, location);
				toSave.addIdentifier(openmrsId);

				if (!toSave.getPatientIdentifier().isPreferred()) {
					openmrsId.setPreferred(true);
				}
			}

			Patient ret = Context.getPatientService().savePatient(toSave);

			// Explicitly save all identifier objects including voided
			for (PatientIdentifier identifier : toSave.getIdentifiers()) {
				Context.getPatientService().savePatientIdentifier(identifier);
			}


			// Save remaining fields as obs
			List<Obs> obsToSave = new ArrayList<Obs>();
			List<Obs> obsToVoid = new ArrayList<Obs>();

			handleOncePerPatientObs(ret, obsToSave, obsToVoid, conceptService.getConceptByUuid(CIVIL_STATUS), null, maritalStatus);
			handleOncePerPatientObs(ret, obsToSave, obsToVoid, conceptService.getConceptByUuid(OCCUPATION), null, occupation);
			handleOncePerPatientObs(ret, obsToSave, obsToVoid, conceptService.getConceptByUuid(EDUCATION), null, education);
			handleOncePerPatientObs(ret, obsToSave, obsToVoid, conceptService.getConceptByUuid(IN_SCHOOL), null, inSchool);
			handleOncePerPatientObs(ret, obsToSave, obsToVoid, conceptService.getConceptByUuid(ORPHAN), null, orphan);
			handleOncePerPatientObs(ret, obsToSave, obsToVoid, conceptService.getConceptByUuid(COUNTRY), null, country);


			for (Obs o : obsToSave) {
				Context.getObsService().saveObs(o, "KenyaEMR edit patient");
			}

			// add relationship and update PatientContact record
			addRelationship(patientRelatedTo, ret, patientContact.getRelationType());
			patientContact.setPatient(ret);
			Context.getService(HTSService.class).savePatientContact(patientContact);


			return ret;
		}

		/**
		 * Handles saving a field which is stored as an obs
		 * @param patient the patient being saved
		 * @param obsToSave
		 * @param obsToVoid
		 * @param question
		 * @param savedObs
		 * @param newValue
		 */
		protected void handleOncePerPatientObs(Patient patient, List<Obs> obsToSave, List<Obs> obsToVoid, Concept question,
                                               Obs savedObs, Concept newValue) {
			if (!OpenmrsUtil.nullSafeEquals(savedObs != null ? savedObs.getValueCoded() : null, newValue)) {
				// there was a change
				if (savedObs != null && newValue == null) {
					// treat going from a value to null as voiding all past civil status obs
					obsToVoid.addAll(Context.getObsService().getObservationsByPersonAndConcept(patient, question));
				}
				if (newValue != null) {
					Obs o = new Obs();
					o.setPerson(patient);
					o.setConcept(question);
					o.setObsDatetime(new Date());
					o.setLocation(getDefaultLocation());
					o.setValueCoded(newValue);
					obsToSave.add(o);
				}
			}
		}

		/**
		 * Handles saving a field which is stored as an obs whose value is boolean
		 * @param patient the patient being saved
		 * @param obsToSave
		 * @param obsToVoid
		 * @param question
		 * @param savedObs
		 * @param newValue
		 */
		protected void handleOncePerPatientObs(Patient patient, List<Obs> obsToSave, List<Obs> obsToVoid, Concept question,
                                               Obs savedObs, Boolean newValue) {
			if (!OpenmrsUtil.nullSafeEquals(savedObs != null ? savedObs.getValueBoolean() : null, newValue)) {
				// there was a change
				if (savedObs != null && newValue == null) {
					// treat going from a value to null as voiding all past civil status obs
					obsToVoid.addAll(Context.getObsService().getObservationsByPersonAndConcept(patient, question));
				}
				if (newValue != null) {
					Obs o = new Obs();
					o.setPerson(patient);
					o.setConcept(question);
					o.setObsDatetime(new Date());
					o.setLocation(getDefaultLocation());
					o.setValueBoolean(newValue);
					obsToSave.add(o);
				}
			}
		}


		public Location getDefaultLocation() {
			try {
				Context.addProxyPrivilege(PrivilegeConstants.GET_LOCATIONS);
				Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
				String GP_DEFAULT_LOCATION = "kenyaemr.defaultLocation";
				GlobalProperty gp = Context.getAdministrationService().getGlobalPropertyObject(GP_DEFAULT_LOCATION);
				return gp != null ? ((Location) gp.getValue()) : null;
			}
			finally {
				Context.removeProxyPrivilege(PrivilegeConstants.GET_LOCATIONS);
				Context.removeProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
			}

		}

		private void addRelationship(Person patient, Person contact, Integer relationshipType) {

			Person personA, personB;
			RelationshipType type;

			if (relationshipType == 970 || relationshipType == 971) {
				personA = contact;
				personB = patient;
				type = personService.getRelationshipTypeByUuid(parentChildRelType);
			} else if (relationshipType == 1528) {
				personA = patient;
				personB = contact;
				type = personService.getRelationshipTypeByUuid(parentChildRelType);
			} else {
				personA = contact;
				personB = patient;
				type = personService.getRelationshipTypeByUuid(relationshipOptionsToRelTypeMapper(relationshipType));
			}

/*+----------------------+--------------------------------------+------------+--------------+
| relationship_type_id | uuid                                 | a_is_to_b  | b_is_to_a    |
+----------------------+--------------------------------------+------------+--------------+
|                    1 | 8d919b58-c2cc-11de-8d13-0010c6dffd0f | Doctor     | Patient      |
|                    2 | 8d91a01c-c2cc-11de-8d13-0010c6dffd0f | Sibling    | Sibling      |
|                    3 | 8d91a210-c2cc-11de-8d13-0010c6dffd0f | Parent     | Child        |
|                    4 | 8d91a3dc-c2cc-11de-8d13-0010c6dffd0f | Aunt/Uncle | Niece/Nephew |
|                    5 | 5f115f62-68b7-11e3-94ee-6bef9086de92 | Guardian   | Dependant    |
|                    6 | d6895098-5d8d-11e3-94ee-b35a4132a5e3 | Spouse     | Spouse       |
|                    7 | 007b765f-6725-4ae9-afee-9966302bace4 | Partner    | Partner      |
|                    8 | 2ac0d501-eadc-4624-b982-563c70035d46 | Co-wife    | Co-wife      |
|                    9 | 58da0d1e-9c89-42e9-9412-275cef1e0429 | Injectable Drug User| Injectable Drug User|
+----------------------+--------------------------------------+------------+--------------+
*/

			Relationship rel = new Relationship();
			rel.setRelationshipType(type);
			rel.setPersonA(personA);
			rel.setPersonB(personB);

			Context.getPersonService().saveRelationship(rel);
		}

		private String relationshipOptionsToRelTypeMapper (Integer relType) {
			Map<Integer, String> options = new HashMap<Integer, String>();

			options.put(970, parentChildRelType);
			options.put(971, parentChildRelType);
			options.put(972, siblingRelType);
			options.put(1528, parentChildRelType);
			options.put(5617, spouseRelType);
			options.put(163565, partnerRelType);
			options.put(162221, cowifeRelType);
			options.put(166606, snsRelType);
			
			return options.get(relType);
		}
		/**
		 * @return the original
		 */
		public Person getOriginal() {
			return original;
		}

		/**
		 * @param original the original to set
		 */
		public void setOriginal(Patient original) {
			this.original = original;
		}

		public Patient getPatientRelatedTo() {
			return patientRelatedTo;
		}

		public void setPatientRelatedTo(Patient patientRelatedTo) {
			this.patientRelatedTo = patientRelatedTo;
		}

		public PatientContact getPatientContact() {
			return patientContact;
		}

		public void setPatientContact(PatientContact patientContact) {
			this.patientContact = patientContact;
		}

		/**
		 * @return the personName
		 */
		public PersonName getPersonName() {
			return personName;
		}

		/**
		 * @param personName the personName to set
		 */
		public void setPersonName(PersonName personName) {
			this.personName = personName;
		}

		/**
		 * @return the patientClinicNumber
		 */
		public String getPatientClinicNumber() {
			return patientClinicNumber;
		}

		/**
		 * @param patientClinicNumber the patientClinicNumber to set
		 */
		public void setPatientClinicNumber(String patientClinicNumber) {
			this.patientClinicNumber = patientClinicNumber;
		}

		/**
		 * @return the hivIdNumber
		 */
		public String getUniquePatientNumber() {
			return uniquePatientNumber;
		}

		/**
		 * @param uniquePatientNumber the uniquePatientNumber to set
		 */
		public void setUniquePatientNumber(String uniquePatientNumber) {
			this.uniquePatientNumber = uniquePatientNumber;
		}

		public Concept getCountry() {
			return country;
		}

		public void setCountry(Concept country) {
			this.country = country;
		}

		/**
		 * @return the nationalIdNumber
		 */
		public String getNationalIdNumber() {
			return nationalIdNumber;
		}

		/**
		 * @param nationalIdNumber the nationalIdNumber to set
		 */
		public void setNationalIdNumber(String nationalIdNumber) {

			this.nationalIdNumber = nationalIdNumber;
		}

		/**
		 * @return the birthdate
		 */
		public Date getBirthdate() {
			return birthdate;
		}

		/**
		 * @param birthdate the birthdate to set
		 */
		public void setBirthdate(Date birthdate) {
			this.birthdate = birthdate;
		}

		/**
		 * @return the birthdateEstimated
		 */
		public Boolean getBirthdateEstimated() {
			return birthdateEstimated;
		}

		/**
		 * @param birthdateEstimated the birthdateEstimated to set
		 */
		public void setBirthdateEstimated(Boolean birthdateEstimated) {
			this.birthdateEstimated = birthdateEstimated;
		}

		/**
		 * @return the gender
		 */
		public String getGender() {
			return gender;
		}

		/**
		 * @param gender the gender to set
		 */
		public void setGender(String gender) {
			this.gender = gender;
		}

		/**
		 * @return the personAddress
		 */
		public PersonAddress getPersonAddress() {
			return personAddress;
		}

		/**
		 * @param personAddress the personAddress to set
		 */
		public void setPersonAddress(PersonAddress personAddress) {
			this.personAddress = personAddress;
		}

		/**
		 * @return the maritalStatus
		 */
		public Concept getMaritalStatus() {
			return maritalStatus;
		}

		/**
		 * @param maritalStatus the maritalStatus to set
		 */
		public void setMaritalStatus(Concept maritalStatus) {
			this.maritalStatus = maritalStatus;
		}

		/**
		 * @return the education
		 */
		public Concept getEducation() {
			return education;
		}

		/**
		 * @param education the education to set
		 */
		public void setEducation(Concept education) {
			this.education = education;
		}

		/**
		 * @return the occupation
		 */
		public Concept getOccupation() {
			return occupation;
		}

		/**
		 * @param occupation the occupation to set
		 */
		public void setOccupation(Concept occupation) {
			this.occupation = occupation;
		}

		/**
		 * @return the telephoneContact
		 */
		public String getTelephoneContact() {
			return telephoneContact;
		}

		/**
		 * @param telephoneContact the telephoneContact to set
		 */
		public void setTelephoneContact(String telephoneContact) {
			this.telephoneContact = telephoneContact;
		}

		public Boolean getDead() {
			return dead;
		}

		public void setDead(Boolean dead) {
			this.dead = dead;
		}
		/*  greencard  */

		public Concept getInSchool() {
			return inSchool;
		}

		public void setInSchool(Concept inSchool) {
			this.inSchool = inSchool;
		}

		public Concept getOrphan() {
			return orphan;
		}

		public void setOrphan(Concept orphan) {
			this.orphan = orphan;
		}

		/*  .greencard   */
		public Date getDeathDate() {
			return deathDate;
		}

		public void setDeathDate(Date deathDate) {
			this.deathDate = deathDate;
		}

		/**
		 * @return the nameOfNextOfKin
		 */
		public String getNameOfNextOfKin() {
			return nameOfNextOfKin;
		}

		/**
		 * @param nameOfNextOfKin the nameOfNextOfKin to set
		 */
		public void setNameOfNextOfKin(String nameOfNextOfKin) {
			this.nameOfNextOfKin = nameOfNextOfKin;
		}

		/**
		 * @return the nextOfKinRelationship
		 */
		public String getNextOfKinRelationship() {
			return nextOfKinRelationship;
		}

		/**
		 * @param nextOfKinRelationship the nextOfKinRelationship to set
		 */
		public void setNextOfKinRelationship(String nextOfKinRelationship) {
			this.nextOfKinRelationship = nextOfKinRelationship;
		}

		/**
		 * @return the nextOfKinContact
		 */
		public String getNextOfKinContact() {
			return nextOfKinContact;
		}

		/**
		 * @param nextOfKinContact the nextOfKinContact to set
		 */
		public void setNextOfKinContact(String nextOfKinContact) {
			this.nextOfKinContact = nextOfKinContact;
		}

		/**
		 * @return the nextOfKinAddress
		 */
		public String getNextOfKinAddress() {
			return nextOfKinAddress;
		}

		/**
		 * @param nextOfKinAddress the nextOfKinAddress to set
		 */
		public void setNextOfKinAddress(String nextOfKinAddress) {
			this.nextOfKinAddress = nextOfKinAddress;
		}

		/**
		 * @return the subChiefName
		 */
		public String getSubChiefName() {
			return subChiefName;
		}

		/**
		 * @param subChiefName the subChiefName to set
		 */
		public void setSubChiefName(String subChiefName) {
			this.subChiefName = subChiefName;
		}

		public String getAlternatePhoneContact() {
			return alternatePhoneContact;
		}

		public void setAlternatePhoneContact(String alternatePhoneContact) {
			this.alternatePhoneContact = alternatePhoneContact;
		}

		public String getNearestHealthFacility() {
			return nearestHealthFacility;
		}

		public void setNearestHealthFacility(String nearestHealthFacility) {
			this.nearestHealthFacility = nearestHealthFacility;
		}

		public String getEmailAddress() {
			return emailAddress;
		}

		public void setEmailAddress(String emailAddress) {
			this.emailAddress = emailAddress;
		}

		public String getGuardianFirstName() {
			return guardianFirstName;
		}

		public void setGuardianFirstName(String guardianFirstName) {
			this.guardianFirstName = guardianFirstName;
		}

		public String getGuardianLastName() {
			return guardianLastName;
		}

		public void setGuardianLastName(String guardianLastName) {
			this.guardianLastName = guardianLastName;
		}
	}
}