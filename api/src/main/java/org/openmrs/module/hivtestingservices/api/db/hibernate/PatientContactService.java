package org.openmrs.module.hivtestingservices.api.db.hibernate;
import java.util.List;
import org.openmrs.module.hivtestingservices.api.PatientContact;
import org.springframework.transaction.annotation.Transactional;

public interface PatientContactService {
public List<PatientContact> getPatientContacts();
public void persistPatientContact(PatientContact patientContact);
public List<PatientContact> searchPatientContact(String searchName);
public void deletePatientContact (int theId);
}
