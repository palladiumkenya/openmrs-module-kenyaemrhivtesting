package org.openmrs.module.hivtestingservices.util;

import org.openmrs.*;
import org.openmrs.api.*;
import org.openmrs.api.context.Context;
import org.openmrs.module.hivtestingservices.api.PatientContact;
import org.openmrs.module.hivtestingservices.util.PersonComposer;
import org.openmrs.module.hivtestingservices.wrapper.PatientWrapper;
import org.openmrs.module.idgen.service.IdentifierSourceService;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static org.openmrs.util.LocationUtility.getDefaultLocation;

@Service
public class PatientMigrationService {

	@Autowired
	private ConceptService conceptService;
	@Autowired
	private ObsService obsService;
	private static final String UNKNOWN = "1067AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	private static final List<String> CONCEPTS_FOR_OBS = Arrays.asList(
			"1054AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", // CIVIL_STATUS
			"1542AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", // OCCUPATION
			"1712AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", // EDUCATION
			"5629AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", // IN_SCHOOL
			"1174AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", // ORPHAN
			"165657AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"  // COUNTRY
	);

	public Person composePatientFromContact(PatientContact pc) {
		return new PersonComposer().composePerson(pc);
	}

	protected void savePatientAndRelatedData(Patient toSave, List<PatientContact> patientContacts) {
	/*	PatientWrapper wrapper = new PatientWrapper(toSave);
		wrapper.getPerson();*/

		PatientIdentifierType openmrsIdType = MetadataUtils.existing(PatientIdentifierType.class, PatientWrapper.OPENMRS_ID);
		ensurePatientIdentifier(toSave, openmrsIdType);

		try {
			// Attempt to save the patient
			Patient savedPatient = Context.getPatientService().savePatient(toSave);

			// If successful, save the patient identifiers
			savePatientIdentifiers(savedPatient);

			// Continue with saving observations and relationships
			saveObservations(savedPatient);

			for (PatientContact contact : patientContacts) {
				addRelationship(contact.getPatientRelatedTo(), savedPatient, contact.getRelationType());
			}

		} catch (Exception e) {
			// Handle the exception if saving the patient fails
			throw new RuntimeException("Failed to save patient data: " + e.getMessage(), e);
		}
	}
		private void ensurePatientIdentifier(Patient toSave, PatientIdentifierType idType) {
			PatientIdentifier id = toSave.getPatientIdentifier(idType);
			if (id == null) {
				String generatedId = Context.getService(IdentifierSourceService.class).generateIdentifier(idType, "Registration");
				id = new PatientIdentifier(generatedId, idType, getDefaultLocation());
				id.setPreferred(true);
				toSave.addIdentifier(id);
			}
		}

		private void savePatientIdentifiers(Patient toSave) {
			for (PatientIdentifier identifier : toSave.getIdentifiers()) {
				Context.getPatientService().savePatientIdentifier(identifier);
			}
		}

		private void saveObservations(Patient patient) {

			for (String conceptUuid : CONCEPTS_FOR_OBS) {
				Obs obs = new Obs();
				obs.setPerson(patient);
				obs.setConcept(conceptService.getConceptByUuid(conceptUuid));
				obs.setObsDatetime(new Date());
				obs.setValueCoded(conceptService.getConceptByUuid(UNKNOWN));
				obs.setLocation(getDefaultLocation());
				obsService.saveObs(obs,"KenyaEMR edit patient");
			}
		}
	private void addRelationship(Person patient, Person contact, Integer relationshipType) {
		PersonService personService = Context.getPersonService();
		RelationshipType type = personService.getRelationshipTypeByUuid(relationshipOptionsToRelTypeMapper(relationshipType));

		Relationship relationship = new Relationship();
		relationship.setRelationshipType(type);
		relationship.setPersonA(patient);
		relationship.setPersonB(contact);

		personService.saveRelationship(relationship);
	}
	private String relationshipOptionsToRelTypeMapper(Integer relType) {
		Map<Integer, String> options = new HashMap<>();
		options.put(970, "8d91a210-c2cc-11de-8d13-0010c6dffd0f");
		options.put(971, "8d91a210-c2cc-11de-8d13-0010c6dffd0f");
		options.put(972, "8d91a01c-c2cc-11de-8d13-0010c6dffd0f");
		options.put(1528, "8d91a210-c2cc-11de-8d13-0010c6dffd0f");
		options.put(5617, "d6895098-5d8d-11e3-94ee-b35a4132a5e3");
		options.put(163565, "007b765f-6725-4ae9-afee-9966302bace4");
		options.put(162221, "2ac0d501-eadc-4624-b982-563c70035d46");
		options.put(166606, "76edc1fe-c5ce-4608-b326-c8ecd1020a73");
		return options.get(relType);
	}
	}
