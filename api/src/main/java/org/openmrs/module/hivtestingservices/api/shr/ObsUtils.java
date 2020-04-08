package org.openmrs.module.hivtestingservices.api.shr;

import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;

import java.util.Date;

public class ObsUtils {

    public static final String REPORTING_COUNTY = "165197";
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
    public static final String PREGNANCY_TRIMESTER = "160665";
    public static final String PREGNANCY_FIRST_TRIMESTER = "1721";
    public static final String PREGNANCY_SECOND_TRIMESTER = "1722";
    public static final String PREGNANCY_THIRD_TRIMESTER = "1723";
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
