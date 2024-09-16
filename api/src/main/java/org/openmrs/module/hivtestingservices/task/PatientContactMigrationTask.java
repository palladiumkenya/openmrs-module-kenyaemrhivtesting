package org.openmrs.module.hivtestingservices.task;

import org.openmrs.api.context.Context;
import org.openmrs.module.hivtestingservices.api.PatientContact;
import org.openmrs.module.hivtestingservices.util.PatientContactMigrationService;
import org.openmrs.module.hivtestingservices.util.PatientMigrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class PatientContactMigrationTask {

	PatientContactMigrationService patientContactService = Context.getService(PatientContactMigrationService .class);

	public void execute() {
		ExecutorService executor = Executors.newCachedThreadPool();
		executor.submit(() -> {
			List<PatientContact> patientContacts = patientContactService.getPatientContactsToMigrate();
			System.out.println("-- here are the contacts to migrate----"+patientContacts.size());
			patientContactService.processPatientContactsInBatches(patientContacts, 20);
		});
		executor.shutdown();
	}

}