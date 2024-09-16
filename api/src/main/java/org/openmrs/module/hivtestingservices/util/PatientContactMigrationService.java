package org.openmrs.module.hivtestingservices.util;

import org.openmrs.Patient;
import org.openmrs.module.hivtestingservices.api.HTSService;
import org.openmrs.module.hivtestingservices.api.PatientContact;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PatientContactMigrationService {

	@Autowired
	private PatientMigrationService patientMigrationService;

	@Autowired
	private HTSService htsService;

	public List<PatientContact> getPatientContactsToMigrate() {
		List<PatientContact> patientContacts = htsService.getPatientContacts();
		if (patientContacts == null) {
			return Collections.emptyList();
		}
		for (Iterator<PatientContact> iterator = patientContacts.iterator(); iterator.hasNext(); ) {
			PatientContact patientContact = iterator.next();
			if (patientContact.getPatient() == null && !patientContact.getVoided()) {
				iterator.remove();
			}
		}
		return patientContacts;
	}

	public void processPatientContactsInBatches(List<PatientContact> patientContacts, int batchSize) {
		for (int i = 0; i < patientContacts.size(); i += batchSize) {
			List<PatientContact> batch = patientContacts.subList(i, Math.min(i + batchSize, patientContacts.size()));
			processBatch(batch);
		}
	}

	private void processBatch(List<PatientContact> batch) {
		Set<Patient> contactPatients = new HashSet<>();
		for (PatientContact pc : batch) {
			Patient patient = (Patient) patientMigrationService.composePatientFromContact(pc);
			contactPatients.add(patient);
		}
		for (Patient toSave : contactPatients) {
			patientMigrationService.savePatientAndRelatedData(toSave, batch);
		}
	}
	}