package org.openmrs.module.hivtestingservices.advice.model;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
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

public class HTSContactListingFormProcessor {

    protected static final Log log = LogFactory.getLog(HTSContactListingFormProcessor.class);

    HTSService htsService = Context.getService(HTSService.class);
    EncounterService encounterService = Context.getEncounterService();
    ObsService obsService = Context.getObsService();
    ConceptService conceptService = Context.getConceptService();

    public void processAOPEncounterEntries() {

        String familyHistoryGroupingConcept = "160593AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";

        // Fetch aop entries
        List<AOPEncounterEntry> aopEntries = htsService.getAopEncounterEntryList();
        for(AOPEncounterEntry aopEntry : aopEntries) {
            Encounter enc = encounterService.getEncounterByUuid(aopEntry.getEncounterUUID());
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
            // update processed aop entry
            if(!errorOccured) {
                aopEntry.setStatus(1);
            } else {
                aopEntry.setStatus(2); // using code 2 for error
            }
            htsService.saveAopEncounterEntry(aopEntry);
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
        Integer ageUnitConcept = 163541;
        Integer sexConcept = 1533;
        Integer phoneNumberConcept = 159635;
        Integer maritalStatusConcept = 1054;
        Integer livingWithPatientConcept = 163607;
        Integer pnsApproachConcept = 164408;
        Integer consentedContactListingConcept = 163089;
        Integer physicalAddressConcept = 159942;


        Integer relType = null;
        Integer age = 0;
        String baselineStatus = null;
        Date nextTestDate = null;
        String contactName = null;
        Integer ageUnit = null;
        String sex = null;
        String phoneNumber = null;
        Integer maritalStatus = null;
        Integer livingWithPatient = null;
        Integer pnsApproach = null;
        Integer consentedContactListing = null;
        String physicalAddress = null;

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
            }
            else if (obs.getConcept().getConceptId().equals(maritalStatusConcept)){
                maritalStatus = obs.getValueCoded().getConceptId();
            }
            else if (obs.getConcept().getConceptId().equals(livingWithPatientConcept)){
                livingWithPatient = obs.getValueCoded().getConceptId();
            }
            else if (obs.getConcept().getConceptId().equals(pnsApproachConcept)){
                pnsApproach = obs.getValueCoded().getConceptId();
            }
            else if (obs.getConcept().getConceptId().equals(consentedContactListingConcept)){
                consentedContactListing = obs.getValueCoded().getConceptId();

            }
            else if (obs.getConcept().getConceptId().equals(physicalAddressConcept)){
                physicalAddress = obs.getValueText();
            }

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
            contact.setMaritalStatus(maritalStatus);
            contact.setLivingWithPatient(livingWithPatient);
            contact.setPnsApproach(pnsApproach);
           /* contact.setContactListingDeclineReason(contactListingDeclineReason);*/
            contact.setConsentedContactListing(consentedContactListing);
            contact.setPhysicalAddress(physicalAddress);
            return contact;
        }
        return null;

    }

    private Date calculateDobFromAge(int age, Integer unit) {
        LocalDate now = LocalDate.now(DateTimeZone.forID("Africa/Nairobi"));
        Period agePeriod;
        if(unit == 1734) { // age provided in years
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

        return relationshipList.get(key);
    }

    String consentedContactListingConverter (Concept key){
        Map<Concept, String> consentList = new HashMap<Concept, String>();
        consentList.put(conceptService.getConcept(1065),"Yes");
        consentList.put(conceptService.getConcept(1066),"No");
        consentList.put(conceptService.getConcept(1067),"Unknown");
        return consentList.get(key);
    }

    String pnsApproachConverter (Concept key){
        Map<Concept, String> pnsApproachList = new HashMap<Concept, String>();
        pnsApproachList.put(conceptService.getConcept(162284),"Dual referral");
        pnsApproachList.put(conceptService.getConcept(163096),"Provider referral");
        pnsApproachList.put(conceptService.getConcept(160551),"Passive referral");
        return pnsApproachList.get(key);
    }

    String hivStatusConverter (Concept key) {
        Map<Concept, String> hivStatusList = new HashMap<Concept, String>();
        hivStatusList.put(conceptService.getConcept(703), "Positive");
        hivStatusList.put(conceptService.getConcept(664), "Negative");
        hivStatusList.put(conceptService.getConcept(1405), "Exposed");
        hivStatusList.put(conceptService.getConcept(1067), "Unknown");
        return hivStatusList.get(key);
    }

    String maritalStatusConverter (Concept key){
        Map<Concept, String> maritalStatusList = new HashMap<Concept, String>();

        maritalStatusList.put(conceptService.getConcept(1057),"Single");
        maritalStatusList.put(conceptService.getConcept(1058),"Divorced");
        maritalStatusList.put(conceptService.getConcept(1059),"Widowed");
        maritalStatusList.put(conceptService.getConcept(5555), "Married Monogamous");
        maritalStatusList.put(conceptService.getConcept(159715),"Married Polygamous");
        return maritalStatusList.get(key);

    }

    String livingWithIndexConverter (Concept key){
        Map<Concept, String> livingWithIndexList = new HashMap<Concept, String>();

        livingWithIndexList.put(conceptService.getConcept(1065),"Yes");
        livingWithIndexList.put(conceptService.getConcept(1066),"No");
        livingWithIndexList.put(conceptService.getConcept(162570),"Declined to answer");
        return livingWithIndexList.get(key);

    }

    String sexConverter (Concept key) {
        Map<Concept, String> sexOptions = new HashMap<Concept, String>();
        sexOptions.put(conceptService.getConcept(1534), "M");
        sexOptions.put(conceptService.getConcept(1535), "F");
        return sexOptions.get(key);
    }

}
