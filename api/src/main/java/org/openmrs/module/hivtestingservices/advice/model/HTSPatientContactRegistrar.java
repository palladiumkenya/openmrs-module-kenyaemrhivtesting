package org.openmrs.module.hivtestingservices.advice.model;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.openmrs.Concept;
import org.openmrs.Encounter;
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
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.hivtestingservices.api.HTSService;
import org.openmrs.module.hivtestingservices.api.PatientContact;
import org.openmrs.module.hivtestingservices.wrapper.PatientWrapper;
import org.openmrs.module.idgen.service.IdentifierSourceService;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.util.PrivilegeConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HTSPatientContactRegistrar {

    protected static final Log log = LogFactory.getLog(HTSPatientContactRegistrar.class);

    HTSService htsService = Context.getService(HTSService.class);
    PersonService personService = Context.getPersonService();

    String siblingRelType = "8d91a01c-c2cc-11de-8d13-0010c6dffd0f";
    String parentChildRelType = "8d91a210-c2cc-11de-8d13-0010c6dffd0f";
    String spouseRelType = "d6895098-5d8d-11e3-94ee-b35a4132a5e3";
    String partnerRelType = "007b765f-6725-4ae9-afee-9966302bace4";
    String cowifeRelType = "2ac0d501-eadc-4624-b982-563c70035d46";
    String injectableDrugUserRelType = "58da0d1e-9c89-42e9-9412-275cef1e0429";

    public void registerBookedPatientContacts() {


        // Fetch entries
        List<PatientContact> eligibleContacts = htsService.getPatientContactListForRegistration();
        for(PatientContact pc : eligibleContacts) {

            String sex = pc.getSex();
            String fName = pc.getFirstName();
            String mName = pc.getMiddleName();
            String lName = pc.getLastName();
            /*if (pc.getSex() != null) {
                sex = sex.equals("male") ? "M" : "F";
            } else {
                sex = "U";
            }*/
            Patient toSave = new Patient(); // Creating a new patient and person
            toSave.setGender(sex);
            toSave.setBirthdate(pc.getBirthDate());
            toSave.setBirthdateEstimated(true);
            //toSave.setDead(dead);
            //toSave.setDeathDate(deathDate);
            //toSave.setCauseOfDeath(dead ? conceptService.getConceptByUuid(CAUSE_OF_DEATH_PLACEHOLDER) : null);
            PersonName pn = new PersonName();
            pn.setGivenName(fName);
            pn.setFamilyName(lName);
            if (mName != null && !mName.equals("")) {
                pn.setMiddleName(mName);
            }

            toSave.addName(pn);
            toSave.addAddress(addPersonAddresses(null, null, null, null, null, pc.getPhysicalAddress()));


            PatientWrapper wrapper = new PatientWrapper(toSave);
            wrapper.getPerson();

            wrapper.getPerson().setTelephoneContact(pc.getPhoneContact());
            /*wrapper.setNationalIdNumber(nationalIdNumber, location);
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
            wrapper.setGuardianLastName(guardianLastName);*/

            // Make sure everyone gets an OpenMRS ID
            PatientIdentifierType openmrsIdType = MetadataUtils.existing(PatientIdentifierType.class, PatientWrapper.OPENMRS_ID);
            PatientIdentifier openmrsId = toSave.getPatientIdentifier(openmrsIdType);

            if (openmrsId == null) {
                String generated = Context.getService(IdentifierSourceService.class).generateIdentifier(openmrsIdType, "Registration");
                openmrsId = new PatientIdentifier(generated, openmrsIdType, getDefaultLocation());
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
            /*List<Obs> obsToSave = new ArrayList<Obs>();
            List<Obs> obsToVoid = new ArrayList<Obs>();

            handleOncePerPatientObs(ret, obsToSave, obsToVoid, conceptService.getConceptByUuid(CIVIL_STATUS), null, maritalStatus);
            handleOncePerPatientObs(ret, obsToSave, obsToVoid, conceptService.getConceptByUuid(OCCUPATION), null, occupation);
            handleOncePerPatientObs(ret, obsToSave, obsToVoid, conceptService.getConceptByUuid(EDUCATION), null, education);
            handleOncePerPatientObs(ret, obsToSave, obsToVoid, conceptService.getConceptByUuid(IN_SCHOOL), null, inSchool);
            handleOncePerPatientObs(ret, obsToSave, obsToVoid, conceptService.getConceptByUuid(ORPHAN), null, orphan);


            for (Obs o : obsToSave) {
                Context.getObsService().saveObs(o, "KenyaEMR edit patient");
            }
*/
            // add relationship and update PatientContact record
            addRelationship(pc.getPatientRelatedTo(), ret, pc.getRelationType());
            pc.setPatient(ret);
            Context.getService(HTSService.class).savePatientContact(pc);

        }
    }

    /**
     * set up person address
     * @param nationality
     * @param county
     * @param subCounty
     * @param ward
     * @param postaladdress
     * @return
     */
    private PersonAddress addPersonAddresses(String nationality, String county, String subCounty, String ward, String postaladdress, String landmark) {

        PersonAddress pa = new PersonAddress();
        if (nationality != null) {
            pa.setCountry(nationality);
        }
        if (county != null) {
            pa.setCountyDistrict(county);
        }
        if (subCounty != null) {
            pa.setStateProvince(subCounty);
        }
        if (ward != null) {
            pa.setAddress4(ward);
        }

        if (postaladdress != null) {
            pa.setAddress1(postaladdress);
        }

        if (landmark != null) {
            pa.setAddress2(landmark);
        }
        return pa;
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

    public Location getDefaultLocation() {
        try {
            Context.addProxyPrivilege(PrivilegeConstants.VIEW_LOCATIONS);
            Context.addProxyPrivilege(PrivilegeConstants.VIEW_GLOBAL_PROPERTIES);
            String GP_DEFAULT_LOCATION = "kenyaemr.defaultLocation";
            GlobalProperty gp = Context.getAdministrationService().getGlobalPropertyObject(GP_DEFAULT_LOCATION);
            return gp != null ? ((Location) gp.getValue()) : null;
        }
        finally {
            Context.removeProxyPrivilege(PrivilegeConstants.VIEW_LOCATIONS);
            Context.removeProxyPrivilege(PrivilegeConstants.VIEW_GLOBAL_PROPERTIES);
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
        options.put(157351, injectableDrugUserRelType);
        return options.get(relType);
    }

}
