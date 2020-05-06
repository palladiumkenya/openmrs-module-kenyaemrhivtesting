package org.openmrs.module.hivtestingservices.api.shr;

import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;

import java.util.Date;

public class ObsUtils {

    public static final String REPORTING_COUNTY = "165197AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String REPORTING_SUB_COUNTY = "161551AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String DETECTION_POINT = "161010AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String POE = "165651AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String COMMUNITY = "163488AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String UNKNOWN = "1067AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String YES_CONCEPT = "1065AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String NO_CONCEPT = "1066AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String DATE_DETECTED = "159948AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String PATIENT_SYMPTOMATIC = "1729AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String DATE_OF_ONSET_OF_SYMPTOMS = "1730AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String PATIENT_STATUS_AT_REPORTING = "159640AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String PATIENT_STATUS_STABLE = "159405AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String PATIENT_STATUS_SEVERELY_ILL = "159407AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String PATIENT_STATUS_DEAD = "160432AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String DATE_OF_DEATH = "1543AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String ADMISSION_TO_HOSPITAL = "163403AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String DATE_OF_ADMISSION_TO_HOSPITAL = "1640AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String NAME_OF_HOSPITAL_ADMITTED = "162724AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String DATE_OF_ISOLATION = "165648AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String WAS_PATIENT_VENTILATED = "165647AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String PATIENT_PREGNANT = "5272AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String PREGNANCY_TRIMESTER = "160665AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String PREGNANCY_FIRST_TRIMESTER = "1721AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String PREGNANCY_SECOND_TRIMESTER = "1722AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String PREGNANCY_THIRD_TRIMESTER = "1723AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String HAS_COMORBIDITIES = "162747AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String HYPERTENSION = "119270AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String DIABETES = "119481AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String LIVER_DISEASE = "6032AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String CHRONIC_NEUROLOGICAL_DISEASE = "165646AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String POST_PARTUM_LESS_THAN_6_WEEKS = "129317AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String IMMUNODEFICIENCY = "117277AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String RENAL_DISEASE = "6033AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String CHRONIC_LUNG_DISEASE = "155569AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String MALIGNANCY = "116031AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String OCCUPATION = "1542AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String HISTORY_OF_TRAVEL = "162619AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String OCCUPATION_STUDENT = "159465AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String OCCUPATION_WORKING_WITH_ANIMALS = "165834AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String OCCUPATION_HCW = "5619AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String OCCUPATION_LAB_WORKER = "164831AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String OTHER_SPECIFY = "160632AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String VISITED_ANIMAL_MARKET = "165844AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String CONTACT_WITH_SUSPECTED_CASE = "162633AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String CONTACT_WITH_RESPIRATORY_INFECTED = "165850AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String VISITED_FACILITY = "162723AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";

    // PATIENT SIGNS
    public static final String TEMPERATURE = "5088AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String PHARYNGEAL_EXUDATE = "1166AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String PHARYNGEAL_EXUDATE_PRESENT = "130305AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String CONJUCTIVAL_INJECTION = "163309AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String CONJUCTIVAL_INJECTION_PRESENT = "517AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String DYSPNEA_TACHYPNEA = "125061AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String ABNORMAL_LUNG_AUSCULTATION = "122496AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String ABNORMAL_LUNG_X_RAY = "12AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String ABNORMAL_LUNG_X_RAY_PRESENT = "154435AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String SEIZURES = "113054AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String COMA = "163043AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String COMA_PRESENT = "144576AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String OTHER_SIGNS = "162737AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String OTHER_SIGNS_PRESENT = "5622AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";

    // PATIENT SYMPTOMS

    public static final String GENERAL_WEAKNESS = "122943AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String HAS_GENERAL_WEAKNESS = "5226AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String RUNNY_NOSE = "163336AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String HAS_RUNNY_NOSE = "113224AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String DIARRHOEA = "142412AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String NAUSEA_VOMITING = "122983AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String HEADACHE = "5219AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String HAS_HEADACHE = "139084AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String IRRITABILITY_CONFUSION = "6023AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String MASCULAR_PAIN = "160388AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String HAS_MASCULAR_PAIN = "133632AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String CHEST_PAIN = "1123AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String HAS_CHEST_PAIN = "120749AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String ABDOMINAL_PAIN = "1125AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String HAS_ABDOMINAL_PAIN = "151AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String JOINT_PAIN = "160687AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String HAS_JOINT_PAIN = "80AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String OTHER_SYMPTOMS = "1838AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String HAS_OTHER_SYMPTOMS = "139548AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String LONGITUDE = "163179AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String LATITUDE = "163178AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String COVID_19_LAB_TEST_CONCEPT = "165611AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String COVID_19_BASELINE_TEST_CONCEPT = "162080AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String COVID_19_1ST_FOLLOWUP_TEST_CONCEPT = "162081AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String COVID_19_2ND_FOLLOWUP_TEST_CONCEPT = "164142AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String COVID_19_3RD_FOLLOWUP_TEST_CONCEPT = "159490AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String COVID_19_4TH_FOLLOWUP_TEST_CONCEPT = "159489AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String COVID_19_5TH_FOLLOWUP_TEST_CONCEPT = "161893AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String NEW_PATIENT_CATEGORY = "164144AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String PATIENT_CATEGORY = "161641AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String QUARANTINE_FACILITY_NAME = "162724AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String REPORTING_WARD = "165195AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String REPORTING_HEALTH_FACILITY = "161550AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";


    /**
     * setup numeric obs
     * @param patient
     * @param qConcept
     * @param ans
     * @param encDate
     * @return
     */
    public static Obs setupNumericObs(Patient patient, String qConcept, Double ans, Date encDate) {
        Obs obs = new Obs();
        obs.setConcept(Context.getConceptService().getConceptByUuid(qConcept));
        obs.setDateCreated(new Date());
        obs.setCreator(Context.getUserService().getUser(1));
        obs.setObsDatetime(encDate);
        obs.setPerson(patient);
        obs.setValueNumeric(ans);
        return obs;
    }

    /**
     * setup text obs
     * @param patient
     * @param qConcept
     * @param ans
     * @param encDate
     * @return
     */
    public static Obs setupTextObs(Patient patient, String qConcept, String ans, Date encDate) {
        Obs obs = new Obs();
        obs.setConcept(Context.getConceptService().getConceptByUuid(qConcept));
        obs.setDateCreated(new Date());
        obs.setCreator(Context.getUserService().getUser(1));
        obs.setObsDatetime(encDate);
        obs.setPerson(patient);
        obs.setValueText(ans);
        return obs;
    }

    /**
     * set up coded obs
     * @param patient
     * @param qConcept
     * @param ans
     * @param encDate
     * @return
     */
    public static Obs setupCodedObs(Patient patient, String qConcept, String ans, Date encDate) {
        Obs obs = new Obs();
        obs.setConcept(Context.getConceptService().getConceptByUuid(qConcept));
        obs.setDateCreated(new Date());
        obs.setCreator(Context.getUserService().getUser(1));
        obs.setObsDatetime(encDate);
        obs.setPerson(patient);
        obs.setValueCoded(Context.getConceptService().getConceptByUuid(ans));
        return obs;
    }

    /**
     *
     * @param patient
     * @param qConcept
     * @param ans
     * @param encDate
     * @return
     */
    public static Obs setupDatetimeObs(Patient patient, String qConcept, Date ans, Date encDate) {
        Obs obs = new Obs();
        obs.setConcept(Context.getConceptService().getConceptByUuid(qConcept));
        obs.setDateCreated(new Date());
        obs.setCreator(Context.getUserService().getUser(1));
        obs.setObsDatetime(encDate);
        obs.setPerson(patient);
        obs.setValueDatetime(ans);
        return obs;
    }
}
