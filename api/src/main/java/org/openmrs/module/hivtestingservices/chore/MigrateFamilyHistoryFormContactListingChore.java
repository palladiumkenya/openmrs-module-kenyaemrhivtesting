package org.openmrs.module.hivtestingservices.chore;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.module.hivtestingservices.api.HTSService;
import org.openmrs.module.hivtestingservices.api.PatientContact;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MigrateFamilyHistoryFormContactListingChore {

    HTSService htsService = Context.getService(HTSService.class);
    EncounterService encounterService = Context.getEncounterService();
    ObsService obsService = Context.getObsService();
    ConceptService conceptService = Context.getConceptService();


    public void perform() throws APIException {
        String familyHistoryGroupingConcept = "160593AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
        String HIV_FAMILY_HISTORY = "7efa0ee0-6617-4cd7-8310-9f95dfee7a82";

        List<Encounter> familyHistoryEncounters = encounterService.getEncounters(
                null,
                null,
                null,
                null,
                Arrays.asList(Context.getFormService().getFormByUuid(HIV_FAMILY_HISTORY)),
                null,
                null,
                null,
                null,
                false
        );
        // Fetch contact entries

        for(Encounter  enc : familyHistoryEncounters) {
            boolean errorOccured = false;

            // construct object for each contact and process them
            List<Obs> obs = obsService.getObservations(
                    Arrays.asList(Context.getPersonService().getPerson(enc.getPatient().getPersonId())),
                    Arrays.asList(enc),
                    Arrays.asList(conceptService.getConceptByUuid(familyHistoryGroupingConcept)),
                    null,
                    null,
                    null,
                    Arrays.asList("obsId"),
                    null,
                    null,
                    null,
                    null,
                    false
            );
            for(Obs o: obs) {
                if (extractFamilyAndPartnerTestingRows(o.getGroupMembers()) != null) {
                    PatientContact contact = extractFamilyAndPartnerTestingRows(o.getGroupMembers());
                    contact.setObsGroupId(o);
                    contact.setPatientRelatedTo(Context.getPatientService().getPatient(o.getPersonId()));
                    try {
                        htsService.savePatientContact(contact);
                    } catch (Exception e) {
                        errorOccured = true;
                    }
                }
            }
        }
    }

    private PatientContact fillContactName(String fullName) {
        if(fullName != null && !fullName.equals("")) {
            String[] nameParts = StringUtils.split(fullName);
            PatientContact contact = new PatientContact();
            if(nameParts.length == 1) {
                contact.setFirstName(fullName);
            } else if(nameParts.length == 2) {
                contact.setFirstName(nameParts[0]);
                contact.setLastName(nameParts[1]);
            } else {
                contact.setFirstName(nameParts[0]);
                contact.setLastName(nameParts[1]);
                contact.setMiddleName(nameParts[2]);
            }
            return contact;
        }
        return null;
    }

    PatientContact extractFamilyAndPartnerTestingRows (Set<Obs> obsList) {

        Integer contactConcept = 160750;
        Integer	ageConcept = 160617;
        Integer relationshipConcept = 1560;
        Integer baselineHivStatusConcept =1169;
        Integer nextTestingDateConcept = 164400;
        Integer ageUnitConcept = 1732;
        Integer sexConcept = 1533;
        Integer phoneNumberConcept = 159635;
        Integer relationshipStatusConcept = 163607;

        Integer relType = null;
        Integer age = 0;
        String baselineStatus = null;
        Date nextTestDate = null;
        String contactName = null;
        Integer ageUnit = null;
        String sex = null;
        String phoneNumber = null;
        boolean processFormData = false;


        for(Obs obs:obsList) {

            if (obs.getConcept().getConceptId().equals(contactConcept) ) {
                contactName = obs.getValueText();
            } else if (obs.getConcept().getConceptId().equals(ageConcept )) { // get age
                age = obs.getValueNumeric().intValue();
            } else if (obs.getConcept().getConceptId().equals(baselineHivStatusConcept) ) {
                baselineStatus = hivStatusConverter(obs.getValueCoded());
            } else if (obs.getConcept().getConceptId().equals(nextTestingDateConcept )) {
                nextTestDate = obs.getValueDate();
            } else if (obs.getConcept().getConceptId().equals(relationshipConcept) ) {
                relType = obs.getValueCoded().getConceptId();
            } else if (obs.getConcept().getConceptId().equals(ageUnitConcept) ) {
                ageUnit = obs.getValueCoded() != null ? obs.getValueCoded().getConceptId() : 1734;
            } else if (obs.getConcept().getConceptId().equals(sexConcept) ) {
                sex = sexConverter(obs.getValueCoded());
            } else if (obs.getConcept().getConceptId().equals(phoneNumberConcept) ) {
                phoneNumber = obs.getValueText();
            } else if (obs.getConcept().getConceptId().equals(relationshipStatusConcept) ) {
                processFormData = true;
            }
        }

        if (!processFormData) {
            return null;
        }
        if(contactName != null) {
            PatientContact contact = fillContactName(contactName);
            contact.setRelationType(relType);
            contact.setBaselineHivStatus(baselineStatus);
            contact.setAppointmentDate(nextTestDate);
            contact.setBirthDate(calculateDobFromAge(age, ageUnit));
            contact.setSex(sex != null? sex :"Undefined");
            if (phoneNumber != null)
                contact.setPhoneContact(phoneNumber);

            return contact;
        }
        return null;

    }

    private Date calculateDobFromAge(int age, Integer unit) {
        LocalDate now = LocalDate.now(DateTimeZone.forID("Africa/Nairobi"));
        Period agePeriod;
        if(unit == null || unit == 1734) { // age provided in years
            agePeriod = new Period(age, 0, 0, 0, 0, 0, 0, 0);
        } else { // age provided in months
            agePeriod = new Period(0, age, 0, 0, 0, 0, 0, 0);
        }

        //(int years, int months, int weeks, int days, int hours, int minutes, int seconds, int millis)
        LocalDate jodaDob = now.minus(agePeriod);
        return jodaDob.toDateTimeAtStartOfDay().toDate();

    }

    Integer relationshipConverter (Concept key) {
        Map<Concept, Integer> relationshipList = new HashMap<Concept, Integer>();
        relationshipList.put(conceptService.getConcept(970), 3); // parent
        relationshipList.put(conceptService.getConcept(971), 3); //parent
        relationshipList.put(conceptService.getConcept(972), 2); //sibling
        relationshipList.put(conceptService.getConcept(1528), 3); //child
        relationshipList.put(conceptService.getConcept(5617), 6); // spouse
        relationshipList.put(conceptService.getConcept(163565), 7); // partner
        relationshipList.put(conceptService.getConcept(162221), 8); // co-wife
        relationshipList.put(conceptService.getConcept(157351), 9); // Injectable drug user

        return relationshipList.get(key);
    }

    String hivStatusConverter (Concept key) {
        Map<Concept, String> hivStatusList = new HashMap<Concept, String>();
        hivStatusList.put(conceptService.getConcept(703), "Positive");
        hivStatusList.put(conceptService.getConcept(664), "Negative");
        hivStatusList.put(conceptService.getConcept(1067), "Unknown");
        return hivStatusList.get(key);
    }

    String sexConverter (Concept key) {
        Map<Concept, String> sexOptions = new HashMap<Concept, String>();
        sexOptions.put(conceptService.getConcept(1534), "M");
        sexOptions.put(conceptService.getConcept(1535), "F");
        return sexOptions.get(key);
    }

}
