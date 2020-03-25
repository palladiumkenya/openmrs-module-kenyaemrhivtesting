package org.openmrs.module.hivtestingservices.api.shr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;

public class IncomingPatientSHR {

    private Patient patient;
    private PersonService personService;
    private PatientService patientService;
    private ObsService obsService;
    private ConceptService conceptService;
    private AdministrationService administrationService;
    private EncounterService encounterService;
    private String incomingSHR;

    String TELEPHONE_CONTACT = "b2c38640-2603-4629-aebd-3b54f33f1e3a";
    String CIVIL_STATUS_CONCEPT = "1054AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    String PSMART_ENCOUNTER_TYPE_UUID = "9bc15e94-2794-11e8-b467-0ed5f89f718b";
    String HEI_UNIQUE_NUMBER = "0691f522-dd67-4eeb-92c8-af5083baf338";
    String NATIONAL_ID = "49af6cdc-7968-4abb-bf46-de10d7f4859f";
    String UNIQUE_PATIENT_NUMBER = "05ee9cf4-7242-4a17-b4d4-00f707265c8a";
    String ANC_NUMBER = "161655AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    String HTS_CONFIRMATORY_TEST_FORM_UUID = "b08471f6-0892-4bf7-ab2b-bf79797b8ea4";
    String HTS_INITIAL_TEST_FORM_UUID = "402dc5d7-46da-42d4-b2be-f43ea4ad87b0";
    String IMMUNIZATION_FORM_UUID = "b4f3859e-861c-4a63-bdff-eb7392030d47";

    protected Log logger = LogFactory.getLog(getClass());


/*    public IncomingPatientSHR(String shr) {

        this.patientService = Context.getPatientService();
        this.personService = Context.getPersonService();
        this.obsService = Context.getObsService();
        this.administrationService = Context.getAdministrationService();
        this.conceptService = Context.getConceptService();
        this.encounterService = Context.getEncounterService();
        this.incomingSHR = shr;
    }

    public IncomingPatientSHR(Integer patientID) {

        this.patientService = Context.getPatientService();
        this.personService = Context.getPersonService();
        this.obsService = Context.getObsService();
        this.administrationService = Context.getAdministrationService();
        this.conceptService = Context.getConceptService();
        this.encounterService = Context.getEncounterService();
        this.patient = patientService.getPatient(patientID);
    }

    public String processIncomingSHR() {
        String msg = "";
        Patient patient = checkIfPatientExists();
        if (patient != null) {
            this.patient = patient;
            addOpenMRSIdentifier(true);
        } else {
            createOrUpdatePatient();
            addOpenMRSIdentifier(false);
        }

        savePersonAddresses();
        savePersonAttributes();
        addOtherPatientIdentifiers();
        addNextOfKinDetails();

        try {
            patientService.savePatient(this.patient);

            try {
                saveHivTestData();
                try {
                    saveImmunizationData();
                    saveMotherDetails();
                    return "Successfully processed P-Smart data";
                } catch (Exception e) {
                    e.printStackTrace();
                    return "There was an error processing immunization data";
                }

                //checkinPatient();
            } catch (Exception ex) {
                ex.printStackTrace();
                msg = "There was an error processing P-Smart HIV Test Data";
            }

        } catch (Exception e) {
            e.printStackTrace();
            msg = "There was an error processing patient SHR";
        }

        return msg;
    }

    public String patientExists() {
        return checkIfPatientExists() != null ? checkIfPatientExists().getGivenName().concat(" ").concat(checkIfPatientExists().getFamilyName()) : "Client doesn't exist in the system";
    }

    private void checkinPatient() {
        Visit newVisit = new Visit();
        newVisit.setPatient(patient);
        newVisit.setStartDatetime(new Date());
        newVisit.setVisitType(MetadataUtils.existing(VisitType.class, SmartCardMetadata._VisitType.OUTPATIENT));
        Context.getVisitService().saveVisit(newVisit);

    }

    public String assignCardSerialIdentifier(String identifier, String encryptedSHR) {
        PatientIdentifierType SMART_CARD_SERIAL_NUMBER_TYPE = patientService.getPatientIdentifierTypeByUuid(SmartCardMetadata._PatientIdentifierType.SMART_CARD_SERIAL_NUMBER);

        if (identifier != null) {

            // check if no other patient has same identifier
            List<Patient> patientsAssignedId = patientService.getPatients(null, identifier.trim(), Arrays.asList(SMART_CARD_SERIAL_NUMBER_TYPE), false);
            if (patientsAssignedId.size() > 0) {
                return "Identifier already assigned";
            }

            // check if patient already has the identifier
            List<PatientIdentifier> existingIdentifiers = patient.getPatientIdentifiers(SMART_CARD_SERIAL_NUMBER_TYPE);

            boolean found = false;
            for (PatientIdentifier id : existingIdentifiers) {
                if (id.getIdentifier().equals(identifier.trim())) {
                    found = true;
                    return "Client already assigned the card serial";
                }
            }


            if (!found) {
                PatientIdentifier patientIdentifier = new PatientIdentifier();
                patientIdentifier.setIdentifierType(SMART_CARD_SERIAL_NUMBER_TYPE);
                patientIdentifier.setLocation(Utils.getDefaultLocation());
                patientIdentifier.setIdentifier(identifier.trim());
                patient.addIdentifier(patientIdentifier);
                patientService.savePatient(patient);
                OutgoingPatientSHR shr = new OutgoingPatientSHR(patient.getPatientId());
                return shr.patientIdentification().toString();
            }
        }
        return "No identifier provided";
    }


    public Patient checkIfPatientExists() {

        PatientIdentifierType HEI_NUMBER_TYPE = patientService.getPatientIdentifierTypeByUuid(HEI_UNIQUE_NUMBER);
        PatientIdentifierType CCC_NUMBER_TYPE = patientService.getPatientIdentifierTypeByUuid(UNIQUE_PATIENT_NUMBER);
        PatientIdentifierType NATIONAL_ID_TYPE = patientService.getPatientIdentifierTypeByUuid(NATIONAL_ID);
        PatientIdentifierType SMART_CARD_SERIAL_NUMBER_TYPE = patientService.getPatientIdentifierTypeByUuid(SmartCardMetadata._PatientIdentifierType.SMART_CARD_SERIAL_NUMBER);
        PatientIdentifierType HTS_NUMBER_TYPE = patientService.getPatientIdentifierTypeByUuid(SmartCardMetadata._PatientIdentifierType.HTS_NUMBER);
        PatientIdentifierType GODS_NUMBER_TYPE = patientService.getPatientIdentifierTypeByUuid(SmartCardMetadata._PatientIdentifierType.GODS_NUMBER);

        String shrGodsNumber = SHRUtils.getSHR(incomingSHR).pATIENT_IDENTIFICATION.eXTERNAL_PATIENT_ID.iD;
        if (shrGodsNumber != null && !shrGodsNumber.isEmpty()) {
            List<Patient> patientsAssignedGodsNumber = patientService.getPatients(null, shrGodsNumber.trim(), Arrays.asList(GODS_NUMBER_TYPE), false);
            if (patientsAssignedGodsNumber.size() > 0) {
                return patientsAssignedGodsNumber.get(0);
            }
        }
        for (int x = 0; x < SHRUtils.getSHR(this.incomingSHR).pATIENT_IDENTIFICATION.iNTERNAL_PATIENT_ID.length; x++) {

            String idType = SHRUtils.getSHR(this.incomingSHR).pATIENT_IDENTIFICATION.iNTERNAL_PATIENT_ID[x].iDENTIFIER_TYPE;
            PatientIdentifierType identifierType = null;

            String identifier = SHRUtils.getSHR(this.incomingSHR).pATIENT_IDENTIFICATION.iNTERNAL_PATIENT_ID[x].iD;

            if (idType.equals("ANC_NUMBER")) {
                // get patient with the identifier

                List<Obs> obs = obsService.getObservations(
                        null,
                        null,
                        Arrays.asList(conceptService.getConceptByUuid(ANC_NUMBER)),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        false
                );
                for (Obs ancNo : obs) {
                    if (ancNo.getValueText().equals(identifier.trim()))
                        return (Patient) ancNo.getPerson();
                }

            } else {
                if (idType.equals("HEI_NUMBER")) {
                    identifierType = HEI_NUMBER_TYPE;
                } else if (idType.equals("CCC_NUMBER")) {
                    identifierType = CCC_NUMBER_TYPE;
                } else if (idType.equals("NATIONAL_ID")) {
                    identifierType = NATIONAL_ID_TYPE;
                } else if (idType.equals("CARD_SERIAL_NUMBER")) {
                    identifierType = SMART_CARD_SERIAL_NUMBER_TYPE;
                } else if (idType.equals("HTS_NUMBER")) {
                    identifierType = HTS_NUMBER_TYPE;
                }

                if (identifierType != null && identifier != null) {
                    List<Patient> patientsAlreadyAssigned = patientService.getPatients(null, identifier.trim(), Arrays.asList(identifierType), false);
                    if (patientsAlreadyAssigned.size() > 0) {
                        return patientsAlreadyAssigned.get(0);
                    }
                }
            }

        }


        return null;
    }

    public Patient checkIfMotherIsEnrolledInFacility() {

        PatientIdentifierType HEI_NUMBER_TYPE = patientService.getPatientIdentifierTypeByUuid(HEI_UNIQUE_NUMBER);
        PatientIdentifierType CCC_NUMBER_TYPE = patientService.getPatientIdentifierTypeByUuid(UNIQUE_PATIENT_NUMBER);
        PatientIdentifierType NATIONAL_ID_TYPE = patientService.getPatientIdentifierTypeByUuid(NATIONAL_ID);
        PatientIdentifierType SMART_CARD_SERIAL_NUMBER_TYPE = patientService.getPatientIdentifierTypeByUuid(SmartCardMetadata._PatientIdentifierType.SMART_CARD_SERIAL_NUMBER);
        PatientIdentifierType HTS_NUMBER_TYPE = patientService.getPatientIdentifierTypeByUuid(SmartCardMetadata._PatientIdentifierType.HTS_NUMBER);
        PatientIdentifierType GODS_NUMBER_TYPE = patientService.getPatientIdentifierTypeByUuid(SmartCardMetadata._PatientIdentifierType.GODS_NUMBER);


        for (int x = 0; x < SHRUtils.getSHR(this.incomingSHR).pATIENT_IDENTIFICATION.mOTHER_DETAILS.mOTHER_IDENTIFIER.length; x++) {

            String idType = SHRUtils.getSHR(this.incomingSHR).pATIENT_IDENTIFICATION.mOTHER_DETAILS.mOTHER_IDENTIFIER[x].iDENTIFIER_TYPE;
            PatientIdentifierType identifierType = null;

            String identifier = SHRUtils.getSHR(this.incomingSHR).pATIENT_IDENTIFICATION.mOTHER_DETAILS.mOTHER_IDENTIFIER[x].iD;

            if (idType.equals("ANC_NUMBER")) {
                // get patient with the identifier

                List<Obs> obs = obsService.getObservations(
                        null,
                        null,
                        Arrays.asList(conceptService.getConceptByUuid(ANC_NUMBER)),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        false
                );
                for (Obs ancNo : obs) {
                    if (ancNo.getValueText().equals(identifier.trim()))
                        return (Patient) ancNo.getPerson();
                }

            } else {
                if (idType.equals("GODS_NUMBER")) {
                    identifierType = GODS_NUMBER_TYPE;
                } else if (idType.equals("HEI_NUMBER")) {
                    identifierType = HEI_NUMBER_TYPE;
                } else if (idType.equals("CCC_NUMBER")) {
                    identifierType = CCC_NUMBER_TYPE;
                } else if (idType.equals("NATIONAL_ID")) {
                    identifierType = NATIONAL_ID_TYPE;
                } else if (idType.equals("CARD_SERIAL_NUMBER")) {
                    identifierType = SMART_CARD_SERIAL_NUMBER_TYPE;
                } else if (idType.equals("HTS_NUMBER")) {
                    identifierType = HTS_NUMBER_TYPE;
                }

                if (identifierType != null && identifier != null) {
                    List<Patient> patientsAlreadyAssigned = patientService.getPatients(null, identifier.trim(), Arrays.asList(identifierType), false);
                    if (patientsAlreadyAssigned.size() > 0) {
                        return patientsAlreadyAssigned.get(0);
                    }
                }
            }

        }


        return null;
    }

    private Person getClientMother() {

        // get relationships
        RelationshipType type = getParentChildType();
        List<Relationship> parentChildRel = personService.getRelationships(null, patient, getParentChildType());

        // check if it is mothers
        Person mother = null;
        // a_is_to_b = 'Parent' and b_is_to_a = 'Child'
        for (Relationship relationship : parentChildRel) {

            if (patient.equals(relationship.getPersonB())) {
                if (relationship.getPersonA().getGender().equals("F")) {
                    mother = relationship.getPersonA();
                    break;
                }
            } else if (patient.equals(relationship.getPersonA())) {
                if (relationship.getPersonB().getGender().equals("F")) {
                    mother = relationship.getPersonB();
                    break;
                }
            }
        }
        return mother;
    }

    protected RelationshipType getParentChildType() {
        return personService.getRelationshipTypeByUuid("8d91a210-c2cc-11de-8d13-0010c6dffd0f");

    }

    private void saveMotherDetails() {
        Patient motherFromSHR = checkIfMotherIsEnrolledInFacility();
        Person motherFromFacilityDb = getClientMother();
        if(motherFromSHR != null) {
            if(motherFromFacilityDb == null) { // create relationship
                Relationship rel = new Relationship();
                rel.setRelationshipType(getParentChildType());
                // a_is_to_b = 'Parent' and b_is_to_a = 'Child'
                rel.setPersonA(motherFromSHR);
                rel.setPersonB(patient);
                personService.saveRelationship(rel);
            }
        }

    }
    public boolean checkIfPatientHasIdentifier(Patient patient, PatientIdentifierType identifierType, String identifier) {

        List<Patient> patientsWithIdentifierList = patientService.getPatients(null, identifier.trim(), Arrays.asList(identifierType), false);
        if (patientsWithIdentifierList.size() > 0) {
            return patientsWithIdentifierList.get(0).equals(patient);
        }

        return false;
    }

    private void createOrUpdatePatient() {

        String fName = SHRUtils.getSHR(this.incomingSHR).pATIENT_IDENTIFICATION.pATIENT_NAME.fIRST_NAME;
        String mName = SHRUtils.getSHR(this.incomingSHR).pATIENT_IDENTIFICATION.pATIENT_NAME.mIDDLE_NAME;
        String lName = SHRUtils.getSHR(this.incomingSHR).pATIENT_IDENTIFICATION.pATIENT_NAME.lAST_NAME;
        String dobString = SHRUtils.getSHR(this.incomingSHR).pATIENT_IDENTIFICATION.dATE_OF_BIRTH;
        String dobPrecision = SHRUtils.getSHR(this.incomingSHR).pATIENT_IDENTIFICATION.dATE_OF_BIRTH_PRECISION;
        Date dob = null;
        try {
            dob = new SimpleDateFormat("yyyyMMdd").parse(dobString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String gender = SHRUtils.getSHR(this.incomingSHR).pATIENT_IDENTIFICATION.sEX;

        this.patient = new Patient();
        this.patient.setGender(gender);
        this.patient.addName(new PersonName(fName, mName, lName));
        if (dob != null) {
            this.patient.setBirthdate(dob);
        }

        if (dobPrecision != null && dobPrecision.equals("ESTIMATED")) {
            this.patient.setBirthdateEstimated(true);
        } else if (dobPrecision != null && dobPrecision.equals("EXACT")) {
            this.patient.setBirthdateEstimated(false);
        }

    }

    private void addOpenMRSIdentifier(boolean patientExists) {
        PatientIdentifier openMRSID = generateOpenMRSID(patientExists);
        patient.addIdentifier(openMRSID);
    }

    private void addOtherPatientIdentifiers() {

        PatientIdentifierType HEI_NUMBER_TYPE = patientService.getPatientIdentifierTypeByUuid(HEI_UNIQUE_NUMBER);
        PatientIdentifierType CCC_NUMBER_TYPE = patientService.getPatientIdentifierTypeByUuid(UNIQUE_PATIENT_NUMBER);
        PatientIdentifierType NATIONAL_ID_TYPE = patientService.getPatientIdentifierTypeByUuid(NATIONAL_ID);
        PatientIdentifierType SMART_CARD_SERIAL_NUMBER_TYPE = patientService.getPatientIdentifierTypeByUuid(SmartCardMetadata._PatientIdentifierType.SMART_CARD_SERIAL_NUMBER);
        PatientIdentifierType HTS_NUMBER_TYPE = patientService.getPatientIdentifierTypeByUuid(SmartCardMetadata._PatientIdentifierType.HTS_NUMBER);
        PatientIdentifierType GODS_NUMBER_TYPE = patientService.getPatientIdentifierTypeByUuid(SmartCardMetadata._PatientIdentifierType.GODS_NUMBER);
        PatientIdentifierType KIP_ID = patientService.getPatientIdentifierTypeByUuid(SmartCardMetadata._PatientIdentifierType.KIP_ID);

        // extract GOD's Number
        String shrGodsNumber = SHRUtils.getSHR(incomingSHR).pATIENT_IDENTIFICATION.eXTERNAL_PATIENT_ID.iD;
        if (shrGodsNumber != null && !shrGodsNumber.isEmpty() && !"".equals(shrGodsNumber)) {
            if (!checkIfPatientHasIdentifier(this.patient, GODS_NUMBER_TYPE, shrGodsNumber.trim())) {
                String godsNumberAssigningFacility = SHRUtils.getSHR(incomingSHR).pATIENT_IDENTIFICATION.eXTERNAL_PATIENT_ID.aSSIGNING_FACILITY;
                PatientIdentifier godsNumber = new PatientIdentifier();
                godsNumber.setIdentifierType(GODS_NUMBER_TYPE);
                godsNumber.setIdentifier(shrGodsNumber);
                godsNumber.setLocation(Utils.getLocationFromMFLCode(godsNumberAssigningFacility) != null ? Utils.getLocationFromMFLCode(godsNumberAssigningFacility) : Utils.getDefaultLocation());
                patient.addIdentifier(godsNumber);
            }

        }

        // OpenMRS ID
*//*        List<PatientIdentifier> openMRSIdentifiers = Utils.getOpenMRSIdentifiers(patient);
        PatientIdentifier openMRSId = null;
        if (openMRSIdentifiers != null && openMRSIdentifiers.size() > 0) {
            openMRSId = openMRSIdentifiers.get(0);
        } else {
            addOpenMRSIdentifier();
        }*//*


        // process internal identifiers

        for (int x = 0; x < SHRUtils.getSHR(this.incomingSHR).pATIENT_IDENTIFICATION.iNTERNAL_PATIENT_ID.length; x++) {

            String idType = SHRUtils.getSHR(this.incomingSHR).pATIENT_IDENTIFICATION.iNTERNAL_PATIENT_ID[x].iDENTIFIER_TYPE;
            PatientIdentifierType identifierType = null;
            String identifier = SHRUtils.getSHR(this.incomingSHR).pATIENT_IDENTIFICATION.iNTERNAL_PATIENT_ID[x].iD;

            if("".equals(identifier.trim()) || identifier.trim().isEmpty()) {
                continue;
            }
            if (idType.equals("ANC_NUMBER")) {
                // first save patient
               *//* patientService.savePatient(this.patient);
                Obs ancNumberObs = new Obs();
                ancNumberObs.setConcept(conceptService.getConceptByUuid(ANC_NUMBER));
                ancNumberObs.setValueText(identifier);
                ancNumberObs.setPerson(this.patient);
                ancNumberObs.setObsDatetime(new Date());
                obsService.(ancNumberObs, null);*//*

            } else {
                if (idType.equals("HEI_NUMBER")) {
                    identifierType = HEI_NUMBER_TYPE;
                } else if (idType.equals("CCC_NUMBER")) {
                    identifierType = CCC_NUMBER_TYPE;
                } else if (idType.equals("NATIONAL_ID")) {
                    identifierType = NATIONAL_ID_TYPE;
                } else if (idType.equals("CARD_SERIAL_NUMBER")) {
                    identifierType = SMART_CARD_SERIAL_NUMBER_TYPE;
                } else if (idType.equals("HTS_NUMBER")) {
                    identifierType = HTS_NUMBER_TYPE;
                } else if (idType.equals("KIP_ID")) {
                    identifierType = KIP_ID;
                } else {
                    continue;
                }

                if (!checkIfPatientHasIdentifier(this.patient, identifierType, identifier)) {
                    PatientIdentifier patientIdentifier = new PatientIdentifier();
                    String assigningFacility = SHRUtils.getSHR(this.incomingSHR).pATIENT_IDENTIFICATION.iNTERNAL_PATIENT_ID[x].aSSIGNING_FACILITY;
                    patientIdentifier.setIdentifierType(identifierType);
                    patientIdentifier.setIdentifier(identifier);
                    patientIdentifier.setLocation(Utils.getDefaultLocation());
                    patientIdentifier.setLocation(Utils.getLocationFromMFLCode(assigningFacility) != null ? Utils.getLocationFromMFLCode(assigningFacility) : Utils.getDefaultLocation());

                    patient.addIdentifier(patientIdentifier);

                }
            }

        }
        Iterator<PatientIdentifier> pIdentifiers = patient.getIdentifiers().iterator();
        PatientIdentifier currentIdentifier = null;
        PatientIdentifier preferredIdentifier = null;
        while (pIdentifiers.hasNext()) {
            currentIdentifier = pIdentifiers.next();
            if (currentIdentifier.isPreferred()) {
                if (preferredIdentifier != null) { // if there's a preferred address already exists, make it preferred=false
                    preferredIdentifier.setPreferred(false);
                }
                preferredIdentifier = currentIdentifier;
            }
        }
        if ((preferredIdentifier == null) && (currentIdentifier != null)) { // No preferred identifier. Make the last identifier entry as preferred.
            currentIdentifier.setPreferred(true);
        }


    }

    Concept testTypeConverter(String key) {
        Map<String, Concept> testTypeList = new HashMap<String, Concept>();
        testTypeList.put("SCREENING", conceptService.getConcept(162080));
        testTypeList.put("CONFIRMATORY", conceptService.getConcept(162082));
        return testTypeList.get(key);

    }

    String testTypeToStringConverter(Concept key) {
        Map<Concept, String> testTypeList = new HashMap<Concept, String>();
        testTypeList.put(conceptService.getConcept(162080), "SCREENING");
        testTypeList.put(conceptService.getConcept(162082), "CONFIRMATORY");
        return testTypeList.get(key);

    }

    Concept hivStatusConverter(String key) {
        Map<String, Concept> hivStatusList = new HashMap<String, Concept>();
        hivStatusList.put("POSITIVE", conceptService.getConcept(703));
        hivStatusList.put("NEGATIVE", conceptService.getConcept(664));
        hivStatusList.put("INCONCLUSIVE", conceptService.getConcept(1138));
        return hivStatusList.get(key);
    }

    Concept testStrategyConverter(String key) {
        Map<String, Concept> hivTestStrategyList = new HashMap<String, Concept>();
        hivTestStrategyList.put("HP", conceptService.getConcept(164163));
        hivTestStrategyList.put("NP", conceptService.getConcept(164953));
        hivTestStrategyList.put("VI", conceptService.getConcept(164954));
        hivTestStrategyList.put("VS", conceptService.getConcept(164955));
        hivTestStrategyList.put("HB", conceptService.getConcept(159938));
        hivTestStrategyList.put("MO", conceptService.getConcept(159939));
        return hivTestStrategyList.get(key);
    }

    private void savePersonAttributes() {
        String tELEPHONE = SHRUtils.getSHR(this.incomingSHR).pATIENT_IDENTIFICATION.pHONE_NUMBER;
        PersonAttributeType type = personService.getPersonAttributeTypeByUuid(TELEPHONE_CONTACT);
        if (tELEPHONE != null) {
            PersonAttribute attribute = new PersonAttribute(type, tELEPHONE);

            try {
                Object hydratedObject = attribute.getHydratedObject();
                if (hydratedObject == null || "".equals(hydratedObject.toString())) {
                    // if null is returned, the value should be blanked out
                    attribute.setValue("");
                } else if (hydratedObject instanceof Attributable) {
                    attribute.setValue(((Attributable) hydratedObject).serialize());
                } else if (!hydratedObject.getClass().getName().equals(type.getFormat())) {
                    // if the classes doesn't match the format, the hydration failed somehow
                    // TODO change the PersonAttribute.getHydratedObject() to not swallow all errors?
                    throw new APIException();
                }
            } catch (APIException e) {
                //.warn("Got an invalid value: " + value + " while setting personAttributeType id #" + paramName, e);
                // setting the value to empty so that the user can reset the value to something else
                attribute.setValue("");
            }
            patient.addAttribute(attribute);
        }
    }

    private void savePersonAddresses() {
        *//**
         * county: personAddress.country
         * sub-county: personAddress.stateProvince
         * ward: personAddress.address4
         * landmark: personAddress.address2
         * postal address: personAddress.address1
         *//*

        String postaladdress = SHRUtils.getSHR(this.incomingSHR).pATIENT_IDENTIFICATION.pATIENT_ADDRESS.pOSTAL_ADDRESS;
        String vILLAGE = SHRUtils.getSHR(this.incomingSHR).pATIENT_IDENTIFICATION.pATIENT_ADDRESS.pHYSICAL_ADDRESS.vILLAGE;
        String wARD = SHRUtils.getSHR(this.incomingSHR).pATIENT_IDENTIFICATION.pATIENT_ADDRESS.pHYSICAL_ADDRESS.wARD;
        String sUBCOUNTY = SHRUtils.getSHR(this.incomingSHR).pATIENT_IDENTIFICATION.pATIENT_ADDRESS.pHYSICAL_ADDRESS.sUB_COUNTY;
        String cOUNTY = SHRUtils.getSHR(this.incomingSHR).pATIENT_IDENTIFICATION.pATIENT_ADDRESS.pHYSICAL_ADDRESS.cOUNTY;
        String nEAREST_LANDMARK = SHRUtils.getSHR(this.incomingSHR).pATIENT_IDENTIFICATION.pATIENT_ADDRESS.pHYSICAL_ADDRESS.nEAREST_LANDMARK;

        Set<PersonAddress> patientAddress = patient.getAddresses();
        if (patientAddress.size() > 0) {
            for (PersonAddress address : patientAddress) {
                if (cOUNTY != null) {
                    address.setCountry(cOUNTY);
                }
                if (sUBCOUNTY != null) {
                    address.setStateProvince(sUBCOUNTY);
                }
                if (wARD != null) {
                    address.setAddress4(wARD);
                }
                if (nEAREST_LANDMARK != null) {
                    address.setAddress2(nEAREST_LANDMARK);
                }
                if (vILLAGE != null) {
                    address.setAddress2(vILLAGE);
                }
                if (postaladdress != null) {
                    address.setAddress1(postaladdress);
                }
                patient.addAddress(address);
            }
        } else {
            PersonAddress pa = new PersonAddress();
            if (cOUNTY != null) {
                pa.setCountry(cOUNTY);
            }
            if (sUBCOUNTY != null) {
                pa.setStateProvince(sUBCOUNTY);
            }
            if (wARD != null) {
                pa.setAddress4(wARD);
            }
            if (nEAREST_LANDMARK != null) {
                pa.setAddress2(nEAREST_LANDMARK);
            }
            if (vILLAGE != null) {
                pa.setAddress2(vILLAGE);
            }
            if (postaladdress != null) {
                pa.setAddress1(postaladdress);
            }
            patient.addAddress(pa);
        }
    }

    private void saveHivTestData() {

        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        Set<SmartCardHivTest> incomingTests = new HashSet<SmartCardHivTest>();
        Set<SmartCardHivTest> existingTests = new HashSet<SmartCardHivTest>(getHivTests());

        if (ArrayUtils.isNotEmpty(SHRUtils.getSHR(this.incomingSHR).hIV_TEST) && SHRUtils.getSHR(this.incomingSHR).hIV_TEST != null) {
            for (int i = 0; i < SHRUtils.getSHR(this.incomingSHR).hIV_TEST.length; i++) {

                String dateStr = SHRUtils.getSHR(this.incomingSHR).hIV_TEST[i].dATE;
                String result = SHRUtils.getSHR(this.incomingSHR).hIV_TEST[i].rESULT;
                String type = SHRUtils.getSHR(this.incomingSHR).hIV_TEST[i].tYPE;
                String facility = SHRUtils.getSHR(this.incomingSHR).hIV_TEST[i].fACILITY;
                String strategy = SHRUtils.getSHR(this.incomingSHR).hIV_TEST[i].sTRATEGY;
                String providerDetails = SHRUtils.getSHR(this.incomingSHR).hIV_TEST[i].pROVIDER_DETAILS.nAME;
                String providerId = SHRUtils.getSHR(this.incomingSHR).hIV_TEST[i].pROVIDER_DETAILS.iD;

                Date date = null;

                try {
                    date = new SimpleDateFormat("yyyyMMdd").parse(dateStr);
                } catch (ParseException ex) {

                    ex.printStackTrace();
                }
                // skip all tests done in the facility
                if (facility.trim().equals(Utils.getDefaultLocationMflCode(Utils.getDefaultLocation()))) {//temp value for this facility
                    continue;
                }

                // drop any entry with missing information
                if (hivStatusConverter(result.trim()) != null && testStrategyConverter(strategy.trim()) != null && date != null
                        && facility != null && providerDetails != null && providerId != null && testTypeConverter(type.trim()) != null) {
                    incomingTests.add(new SmartCardHivTest(hivStatusConverter(result.trim()),
                            facility.trim(),
                            testStrategyConverter(strategy.trim()), date, type.trim(), providerDetails, providerId));
                }
            }
        }
        Iterator<SmartCardHivTest> ite = incomingTests.iterator();
        while (ite.hasNext()) {
            SmartCardHivTest value = ite.next();
            for (SmartCardHivTest db : existingTests) {
                if (db.equals(value)) {
                    ite.remove();
                    break;
                }
            }
        }

        for (SmartCardHivTest thisTest : incomingTests) {

            Encounter enc = new Encounter();
            Location location = Utils.getDefaultLocation();
            enc.setLocation(location);
            enc.setEncounterType(Context.getEncounterService().getEncounterTypeByUuid(SmartCardMetadata._EncounterType.EXTERNAL_PSMART_DATA));
            enc.setEncounterDatetime(thisTest.getDateTested());
            enc.setPatient(patient);
            enc.addProvider(Context.getEncounterService().getEncounterRole(1), Context.getProviderService().getProvider(1));
            enc.setForm(Context.getFormService().getFormByUuid(SmartCardMetadata._Form.PSMART_HIV_TEST));


            // build observations
            setEncounterObs(enc, thisTest);
        }

        patientService.savePatient(patient);
    }

    private void setEncounterObs(Encounter enc, SmartCardHivTest hivTest) {

        Integer finalHivTestResultConcept = 159427;
        Integer testTypeConcept = 162084;
        Integer testStrategyConcept = 164956;
        Integer healthProviderConcept = 1473;
        Integer healthFacilityNameConcept = 162724;
        Integer healthProviderIdentifierConcept = 163161;
        // test result
        Obs o = new Obs();
        o.setConcept(conceptService.getConcept(finalHivTestResultConcept));
        o.setDateCreated(new Date());
        o.setCreator(Context.getUserService().getUser(1));
        o.setLocation(enc.getLocation());
        o.setObsDatetime(enc.getEncounterDatetime());
        o.setPerson(this.patient);
        o.setValueCoded(hivTest.getResult());

        // test type
        Obs o1 = new Obs();
        o1.setConcept(conceptService.getConcept(testTypeConcept));
        o1.setDateCreated(new Date());
        o1.setCreator(Context.getUserService().getUser(1));
        o1.setLocation(enc.getLocation());
        o1.setObsDatetime(enc.getEncounterDatetime());
        o1.setPerson(this.patient);
        o1.setValueCoded(testTypeConverter(hivTest.getType().trim()));

        // test strategy
        Obs o2 = new Obs();
        o2.setConcept(conceptService.getConcept(testStrategyConcept));
        o2.setDateCreated(new Date());
        o2.setCreator(Context.getUserService().getUser(1));
        o2.setLocation(enc.getLocation());
        o2.setObsDatetime(enc.getEncounterDatetime());
        o2.setPerson(this.patient);
        o2.setValueCoded(hivTest.getStrategy());

        // test provider
        // only do this if provider details is not null

        Obs o3 = new Obs();
        o3.setConcept(conceptService.getConcept(healthProviderConcept));
        o3.setDateCreated(new Date());
        o3.setCreator(Context.getUserService().getUser(1));
        o3.setLocation(enc.getLocation());
        o3.setObsDatetime(enc.getEncounterDatetime());
        o3.setPerson(this.patient);
        o3.setValueText(hivTest.getProviderName().trim());

        // test provider id
        Obs o5 = new Obs();
        o5.setConcept(conceptService.getConcept(healthProviderIdentifierConcept));
        o5.setDateCreated(new Date());
        o5.setCreator(Context.getUserService().getUser(1));
        o5.setLocation(enc.getLocation());
        o5.setObsDatetime(enc.getEncounterDatetime());
        o5.setPerson(this.patient);
        o5.setValueText(hivTest.getProviderId().trim());

        // test facility
        Obs o4 = new Obs();
        o4.setConcept(conceptService.getConcept(healthFacilityNameConcept));
        o4.setDateCreated(new Date());
        o4.setCreator(Context.getUserService().getUser(1));
        o4.setLocation(enc.getLocation());
        o4.setObsDatetime(enc.getEncounterDatetime());
        o4.setPerson(this.patient);
        o4.setValueText(hivTest.getFacility().trim());


        enc.addObs(o);
        enc.addObs(o1);
        enc.addObs(o2);
        enc.addObs(o3);
        enc.addObs(o4);
        enc.addObs(o5);
        encounterService.saveEncounter(enc);
    }

    private void saveImmunizationData() {
        Set<ImmunizationWrapper> immunizationData = new HashSet<ImmunizationWrapper>(processImmunizationDataFromSHR());
        Set<ImmunizationWrapper> existingImmunizationData = new HashSet<ImmunizationWrapper>(getAllImmunizationDataFromDb());

        if (immunizationData.size() > 0) {
            Iterator<ImmunizationWrapper> ite = immunizationData.iterator();
            while (ite.hasNext()) {
                ImmunizationWrapper value = ite.next();
                for (ImmunizationWrapper db : existingImmunizationData) {
                    if (db.equals(value)) {
                        ite.remove();
                        break;
                    }
                }
            }
        }
        if (immunizationData.size() > 0) {
            saveImmunizationData(immunizationData);
        }
    }

    private void saveImmunizationData(Set<ImmunizationWrapper> data) {

        EncounterType pSmartDataEncType = encounterService.getEncounterTypeByUuid(SmartCardMetadata._EncounterType.EXTERNAL_PSMART_DATA);
        Form pSmartImmunizationForm = Context.getFormService().getFormByUuid(SmartCardMetadata._Form.PSMART_IMMUNIZATION);

        // organize data according to date
        Map<Date, List<ImmunizationWrapper>> organizedImmunizations = new HashMap<Date, List<ImmunizationWrapper>>();
        for (ImmunizationWrapper immunization : data) {
            Date vaccineDate = immunization.getVaccineDate();
            if (!organizedImmunizations.containsKey(vaccineDate)) {
                organizedImmunizations.put(vaccineDate, new ArrayList<ImmunizationWrapper>());

            }
            organizedImmunizations.get(vaccineDate).add(immunization);
        }

        // loop through different dates

        for (Map.Entry<Date, List<ImmunizationWrapper>> entry : organizedImmunizations.entrySet()) {

            Date key = entry.getKey();
            List<ImmunizationWrapper> immunizationList = entry.getValue();

            // build encounter
            Encounter enc = new Encounter();
            Location location = Utils.getDefaultLocation();
            enc.setLocation(location);
            enc.setEncounterType(pSmartDataEncType);
            enc.setEncounterDatetime(key);
            enc.setPatient(patient);
            enc.addProvider(Context.getEncounterService().getEncounterRole(1), Context.getProviderService().getProvider(1));
            enc.setForm(pSmartImmunizationForm);

            // build obs and add to encounter
            for (ImmunizationWrapper iEntry : immunizationList) {
                Set<Obs> obs = createImmunizationObs(iEntry, enc);
                enc.setObs(obs);
            }

            encounterService.saveEncounter(enc);

        }
        patientService.savePatient(patient);

    }

    private Set<Obs> createImmunizationObs(ImmunizationWrapper entry, Encounter encounter) {

        Concept groupingConcept = conceptService.getConcept(1421);
        Concept vaccineConcept = conceptService.getConcept(984);
        Concept sequenceNumber = conceptService.getConcept(1418);
        Set<Obs> immunizationObs = new HashSet<Obs>();

        Obs obsGroup = new Obs();
        obsGroup.setConcept(groupingConcept);
        obsGroup.setObsDatetime(entry.getVaccineDate());
        obsGroup.setPerson(patient);
        obsGroup.setEncounter(encounter);

        Obs immunization = new Obs();
        immunization.setConcept(vaccineConcept);
        immunization.setValueCoded(entry.getVaccine());
        immunization.setObsDatetime(entry.getVaccineDate());
        immunization.setPerson(patient);
        immunization.setObsGroup(obsGroup);
        immunization.setEncounter(encounter);

        immunizationObs.addAll(Arrays.asList(obsGroup, immunization));

        if (entry.getSequenceNumber() != null) {
            Obs immunizationSequenceNumber = new Obs();
            immunizationSequenceNumber.setConcept(sequenceNumber);
            immunizationSequenceNumber.setValueNumeric(Double.valueOf(entry.getSequenceNumber()));
            immunizationSequenceNumber.setPerson(patient);
            immunizationSequenceNumber.setObsGroup(obsGroup);
            immunizationSequenceNumber.setObsDatetime(entry.getVaccineDate());
            immunizationSequenceNumber.setEncounter(encounter);
            immunizationObs.add(immunizationSequenceNumber);

        }


        return immunizationObs;
    }

    private List<ImmunizationWrapper> getAllImmunizationDataFromDb() {

        Concept groupingConcept = conceptService.getConcept(1421);
        Concept vaccineConcept = conceptService.getConcept(984);
        Concept sequenceNumber = conceptService.getConcept(1418);
        Concept dateGiven = conceptService.getConcept(1282);
        Concept dateGivenConcept2 = conceptService.getConcept(1410);
        Form pSmartImmunizationForm = Context.getFormService().getFormByUuid(SmartCardMetadata._Form.PSMART_IMMUNIZATION);


        // get immunizations from immunization form
        List<Encounter> immunizationEncounters = encounterService.getEncounters(
                patient,
                null,
                null,
                null,
                Arrays.asList(Context.getFormService().getFormByUuid(IMMUNIZATION_FORM_UUID), pSmartImmunizationForm),
                null,
                null,
                null,
                null,
                false
        );

        List<ImmunizationWrapper> immunizationList = new ArrayList<ImmunizationWrapper>();
        // extract blocks of vaccines organized by grouping concept
        for (Encounter encounter : immunizationEncounters) {
            List<Obs> obs = obsService.getObservations(
                    Arrays.asList(Context.getPersonService().getPerson(patient.getPersonId())),
                    Arrays.asList(encounter),
                    Arrays.asList(groupingConcept),
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
            // Iterate through groups
            for (Obs group : obs) {
                ImmunizationWrapper groupWrapper;
                Concept vaccine = null;
                Integer sequence = null;
                Date vaccineDate = null;
                Set<Obs> members = group.getGroupMembers();
                // iterate through obs for a particular group
                for (Obs memberObs : members) {
                    if (memberObs.getConcept().equals(vaccineConcept)) {
                        vaccine = memberObs.getValueCoded();
                    } else if (memberObs.getConcept().equals(sequenceNumber)) {
                        sequence = memberObs.getValueNumeric() != null ? memberObs.getValueNumeric().intValue() : sequence;
                    } else if (memberObs.getConcept().equals(dateGiven) || memberObs.getConcept().equals(dateGivenConcept2)) {
                        vaccineDate = memberObs.getValueDate();
                    }
                }
                immunizationList.add(new ImmunizationWrapper(vaccine, sequence, vaccineDate));


            }
        }


        return immunizationList;
    }

    private List<ImmunizationWrapper> processImmunizationDataFromSHR() {

        Concept BCG = conceptService.getConcept(886);
        Concept OPV = conceptService.getConcept(783);
        Concept IPV = conceptService.getConcept(1422);
        Concept DPT = conceptService.getConcept(781);
        Concept PCV = conceptService.getConcept(162342);
        Concept ROTA = conceptService.getConcept(83531);
        Concept MEASLESorRUBELLA = conceptService.getConcept(162586);
        Concept MEASLES = conceptService.getConcept(36);
        Concept YELLOW_FEVER = conceptService.getConcept(5864);

        List<ImmunizationWrapper> shrData = new ArrayList<ImmunizationWrapper>();
        if (ArrayUtils.isNotEmpty(SHRUtils.getSHR(this.incomingSHR).iMMUNIZATION) && SHRUtils.getSHR(this.incomingSHR).iMMUNIZATION != null) {
            for (int i = 0; i < SHRUtils.getSHR(this.incomingSHR).iMMUNIZATION.length; i++) {

                String name = SHRUtils.getSHR(this.incomingSHR).iMMUNIZATION[i].nAME;
                String dateAministered = SHRUtils.getSHR(this.incomingSHR).iMMUNIZATION[i].dATE_ADMINISTERED;
                Date date = null;
                try {
                    date = new SimpleDateFormat("yyyyMMdd").parse(dateAministered);
                } catch (ParseException ex) {

                    ex.printStackTrace();
                }
                ImmunizationWrapper entry = new ImmunizationWrapper();

                if (name == null || "".equals("")) {
                    continue;
                }

                if (name.trim().equals("BCG")) {
                    entry.setVaccine(BCG);
                    entry.setSequenceNumber(null);
                } else if (name.trim().equals("OPV_AT_BIRTH")) {
                    entry.setVaccine(OPV);
                    entry.setSequenceNumber(0);
                } else if (name.trim().equals("OPV1")) {
                    entry.setVaccine(OPV);
                    entry.setSequenceNumber(1);
                } else if (name.trim().equals("OPV2")) {
                    entry.setVaccine(OPV);
                    entry.setSequenceNumber(2);
                } else if (name.trim().equals("OPV3")) {
                    entry.setVaccine(OPV);
                    entry.setSequenceNumber(3);
                } else if (name.trim().equals("PCV10-1")) {
                    entry.setVaccine(PCV);
                    entry.setSequenceNumber(1);
                } else if (name.trim().equals("PCV10-2")) {
                    entry.setVaccine(PCV);
                    entry.setSequenceNumber(2);
                } else if (name.trim().equals("PCV10-3")) {
                    entry.setVaccine(PCV);
                    entry.setSequenceNumber(3);
                } else if (name.trim().equals("ROTA1")) {
                    entry.setVaccine(ROTA);
                    entry.setSequenceNumber(1);
                } else if (name.trim().equals("ROTA2")) {
                    entry.setVaccine(ROTA);
                    entry.setSequenceNumber(2);
                } else if (name.trim().equals("MEASLES6")) {
                    entry.setVaccine(MEASLES);
                    entry.setSequenceNumber(1);
                } else if (name.trim().equals("MEASLES9")) {
                    entry.setVaccine(MEASLESorRUBELLA);
                    entry.setSequenceNumber(1);
                } else if (name.trim().equals("MEASLES18")) {
                    entry.setVaccine(MEASLESorRUBELLA);
                    entry.setSequenceNumber(2);
                }
                entry.setVaccineDate(date);
                if (entry.getVaccine() != null && entry.getVaccineDate() != null) {
                    shrData.add(entry);
                }
            }
        }
        return shrData;
    }

    *//**
     * saves the first next of kin details. The system does not support multiple
     *//*
    private void addNextOfKinDetails() {

        String NEXT_OF_KIN_ADDRESS = "7cf22bec-d90a-46ad-9f48-035952261294";
        String NEXT_OF_KIN_CONTACT = "342a1d39-c541-4b29-8818-930916f4c2dc";
        String NEXT_OF_KIN_NAME = "830bef6d-b01f-449d-9f8d-ac0fede8dbd3";
        String NEXT_OF_KIN_RELATIONSHIP = "d0aa9fd1-2ac5-45d8-9c5e-4317c622c8f5";
        Set<PersonAttribute> attributes = new TreeSet<PersonAttribute>();
        Set<PersonAttribute> patientAttributes = new HashSet<PersonAttribute>();
        if (SHRUtils.getSHR(this.incomingSHR).nEXT_OF_KIN != null && SHRUtils.getSHR(this.incomingSHR).nEXT_OF_KIN.length > 0) {
            PersonAttributeType nextOfKinNameAttrType = personService.getPersonAttributeTypeByUuid(NEXT_OF_KIN_NAME);
            PersonAttributeType nextOfKinAddressAttrType = personService.getPersonAttributeTypeByUuid(NEXT_OF_KIN_ADDRESS);
            PersonAttributeType nextOfKinPhoneContactAttrType = personService.getPersonAttributeTypeByUuid(NEXT_OF_KIN_CONTACT);
            PersonAttributeType nextOfKinRelationshipAttrType = personService.getPersonAttributeTypeByUuid(NEXT_OF_KIN_RELATIONSHIP);

            String nextOfKinName = SHRUtils.getSHR(this.incomingSHR).nEXT_OF_KIN[0].nOK_NAME.fIRST_NAME.concat(" ").concat(
                    SHRUtils.getSHR(this.incomingSHR).nEXT_OF_KIN[0].nOK_NAME.mIDDLE_NAME != "" ? SHRUtils.getSHR(this.incomingSHR).nEXT_OF_KIN[0].nOK_NAME.mIDDLE_NAME : ""
            ).concat(" ").concat(
                    SHRUtils.getSHR(this.incomingSHR).nEXT_OF_KIN[0].nOK_NAME.lAST_NAME != "" ? SHRUtils.getSHR(this.incomingSHR).nEXT_OF_KIN[0].nOK_NAME.lAST_NAME : ""
            );



            String nextOfKinAddress = SHRUtils.getSHR(this.incomingSHR).nEXT_OF_KIN[0].aDDRESS;
            String nextOfKinPhoneContact = SHRUtils.getSHR(this.incomingSHR).nEXT_OF_KIN[0].pHONE_NUMBER;
            String nextOfKinRelationship = SHRUtils.getSHR(this.incomingSHR).nEXT_OF_KIN[0].rELATIONSHIP;

            List<PatientIdentifier> idList = new ArrayList<PatientIdentifier>();

            if (nextOfKinName != null) {
                PersonAttribute kinName = new PersonAttribute();
                kinName.setAttributeType(nextOfKinNameAttrType);
                kinName.setValue(nextOfKinName.trim());
                patientAttributes.add(kinName);
            }

            if (nextOfKinAddress != null) {
                PersonAttribute kinAddress = new PersonAttribute();
                kinAddress.setAttributeType(nextOfKinAddressAttrType);
                kinAddress.setValue(nextOfKinAddress.trim());
                patientAttributes.add(kinAddress);
            }

            if (nextOfKinPhoneContact != null) {
                PersonAttribute kinPhoneContact = new PersonAttribute();
                kinPhoneContact.setAttributeType(nextOfKinPhoneContactAttrType);
                kinPhoneContact.setValue(nextOfKinPhoneContact.trim());
                patientAttributes.add(kinPhoneContact);

            }

            if (nextOfKinRelationship != null) {
                PersonAttribute kinRelationship = new PersonAttribute();
                kinRelationship.setAttributeType(nextOfKinRelationshipAttrType);
                kinRelationship.setValue(nextOfKinRelationship.trim());
                patientAttributes.add(kinRelationship);
            }
        }

        for(PersonAttribute thisAttribute : patientAttributes) {

            PersonAttribute attribute = new PersonAttribute(thisAttribute.getAttributeType(), thisAttribute.getValue());

            try {
                Object hydratedObject = attribute.getHydratedObject();
                if (hydratedObject == null || "".equals(hydratedObject.toString())) {
                    // if null is returned, the value should be blanked out
                    attribute.setValue("");
                } else if (hydratedObject instanceof Attributable) {
                    attribute.setValue(((Attributable) hydratedObject).serialize());
                } else if (!hydratedObject.getClass().getName().equals(thisAttribute.getAttributeType().getFormat())) {
                    // if the classes doesn't match the format, the hydration failed somehow
                    // TODO change the PersonAttribute.getHydratedObject() to not swallow all errors?
                    throw new APIException();
                }
            } catch (APIException e) {
                //.warn("Got an invalid value: " + value + " while setting personAttributeType id #" + paramName, e);
                // setting the value to empty so that the user can reset the value to something else
                attribute.setValue("");
            }
            patient.addAttribute(attribute);
        }

    }

    private void saveObsData() {

        String cIVIL_STATUS = SHRUtils.getSHR(this.incomingSHR).pATIENT_IDENTIFICATION.mARITAL_STATUS;
        if (cIVIL_STATUS != null) {

        }
    }

    *//**
     * Can't save patients unless they have required OpenMRS IDs
     *//*
    private PatientIdentifier generateOpenMRSID(boolean patientExists) {
        PatientIdentifierType openmrsIDType = Context.getPatientService().getPatientIdentifierTypeByUuid("dfacd928-0370-4315-99d7-6ec1c9f7ae76");
        String generated = Context.getService(IdentifierSourceService.class).generateIdentifier(openmrsIDType, "Registration");
        if(patientExists) {
            List<PatientIdentifier> existingIdentifier = Utils.getOpenMRSIdentifiers(patient);
            if (existingIdentifier != null && existingIdentifier.size() > 0) {
                return existingIdentifier.get(0);
            }
        }
        PatientIdentifier identifier = new PatientIdentifier(generated, openmrsIDType, Utils.getDefaultLocation());
        return identifier;
    }

    private List<SmartCardHivTest> getHivTests() {

        // test concepts
        Concept finalHivTestResultConcept = conceptService.getConcept(159427);
        Concept testTypeConcept = conceptService.getConcept(162084);
        Concept testStrategyConcept = conceptService.getConcept(164956);
        Concept testFacilityCodeConcept = conceptService.getConcept(162724);
        Concept healthProviderConcept = conceptService.getConcept(1473);
        Concept healthProviderIdentifierConcept = conceptService.getConcept(163161);


        Form HTS_INITIAL_FORM = Context.getFormService().getFormByUuid(HTS_INITIAL_TEST_FORM_UUID);
        Form HTS_CONFIRMATORY_FORM = Context.getFormService().getFormByUuid(HTS_CONFIRMATORY_TEST_FORM_UUID);

        EncounterType smartCardHTSEntry = Context.getEncounterService().getEncounterTypeByUuid(SmartCardMetadata._EncounterType.EXTERNAL_PSMART_DATA);
        Form SMART_CARD_HTS_FORM = Context.getFormService().getFormByUuid(SmartCardMetadata._Form.PSMART_HIV_TEST);


        List<Encounter> htsEncounters = Utils.getEncounters(patient, Arrays.asList(HTS_CONFIRMATORY_FORM, HTS_INITIAL_FORM));
        List<Encounter> processedIncomingTests = Utils.getEncounters(patient, Arrays.asList(SMART_CARD_HTS_FORM));

        List<SmartCardHivTest> testList = new ArrayList<SmartCardHivTest>();
        // loop through encounters and extract hiv test information
        for (Encounter encounter : htsEncounters) {
            List<Obs> obs = Utils.getEncounterObservationsForQuestions(patient, encounter, Arrays.asList(finalHivTestResultConcept, testTypeConcept, testStrategyConcept));
            testList.add(extractHivTestInformation(obs));
        }

        // append processed tests from card
        for (Encounter encounter : processedIncomingTests) {
            List<Obs> obs = Utils.getEncounterObservationsForQuestions(patient, encounter, Arrays.asList(finalHivTestResultConcept, testTypeConcept, testStrategyConcept, testFacilityCodeConcept, healthProviderConcept, healthProviderIdentifierConcept));
            testList.add(extractHivTestInformation(obs));
        }

        return testList;
    }

    private SmartCardHivTest extractHivTestInformation(List<Obs> obsList) {

        Integer finalHivTestResultConcept = 159427;
        Integer testTypeConcept = 162084;
        Integer testStrategyConcept = 164956;
        Integer testFacilityCodeConcept = 162724;
        Integer healthProviderConcept = 1473;
        Integer healthProviderIdentifierConcept = 163161;

        Date testDate = obsList.get(0).getObsDatetime();
        User provider = obsList.get(0).getCreator();
        Concept testResult = null;
        String testType = null;
        String testFacility = null;
        Concept testStrategy = null;
        String providerName = null;
        String providerId = null;

        for (Obs obs : obsList) {

            if (obs.getEncounter().getForm().getUuid().equals(HTS_CONFIRMATORY_TEST_FORM_UUID)) {
                testType = "CONFIRMATORY";
            } else if (obs.getEncounter().getForm().getUuid().equals(HTS_INITIAL_TEST_FORM_UUID)) {
                testType = "SCREENING";
            }

            if (obs.getConcept().getConceptId().equals(testTypeConcept)) {
                testType = testTypeToStringConverter(obs.getValueCoded());
            }

            if (obs.getConcept().getConceptId().equals(finalHivTestResultConcept)) {
                testResult = obs.getValueCoded();
            } else if (obs.getConcept().getConceptId().equals(testStrategyConcept)) {
                testStrategy = obs.getValueCoded();
            } else if (obs.getConcept().getConceptId().equals(testFacilityCodeConcept)) {
                testFacility = obs.getValueText();
            } else if (obs.getConcept().getConceptId().equals(healthProviderConcept)) {
                providerName = obs.getValueText();
            } else if (obs.getConcept().getConceptId().equals(healthProviderIdentifierConcept)) {
                providerId = obs.getValueText();
            }
        }
        return new SmartCardHivTest(testResult, testFacility, testStrategy, testDate, testType, providerName, providerId);

    }*/

}
