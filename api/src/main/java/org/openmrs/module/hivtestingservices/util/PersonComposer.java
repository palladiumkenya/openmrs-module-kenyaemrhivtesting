package org.openmrs.module.hivtestingservices.util;

import org.openmrs.*;
import org.openmrs.module.hivtestingservices.api.PatientContact;
import org.openmrs.module.kenyaemr.metadata.CommonMetadata;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;

	@Component
	public class PersonComposer {

		public Person composePerson(PatientContact pc) {
			Person person = new Person();
			PersonName name = new PersonName(
					defaultIfEmpty(pc.getLastName(), "Unknown"),
					defaultIfEmpty(pc.getFirstName(), "Unknown"),
					defaultIfEmpty(pc.getMiddleName(), "Unknown")
			);

			PersonAddress address = new PersonAddress();
			address.setAddress1(defaultIfEmpty(pc.getPhysicalAddress(), "Unknown"));
			address.setAddress2("Unknown");
			address.setCityVillage("Unknown");
			address.setCountry("Unknown");

			Set<PersonAttribute> personAttributes = new HashSet<>(Arrays.asList(
					createPersonAttribute(CommonMetadata._PersonAttributeType.TELEPHONE_CONTACT, pc.getPhoneContact()),
					createPersonAttribute(CommonMetadata._PersonAttributeType.PNS_APPROACH, pc.getPnsApproach().toString()),
					createPersonAttribute(CommonMetadata._PersonAttributeType.PNS_PATIENT_CONTACT_BASELINE_HIV_STATUS, pc.getBaselineHivStatus()),
					createPersonAttribute(CommonMetadata._PersonAttributeType.PNS_PATIENT_CONTACT_LIVING_WITH_PATIENT, pc.getLivingWithPatient().toString()),
					createPersonAttribute(CommonMetadata._PersonAttributeType.PNS_PATIENT_CONTACT_IPV_OUTCOME, pc.getIpvOutcome()),
					createPersonAttribute(CommonMetadata._PersonAttributeType.PNS_PATIENT_CONTACT_REGISTRATION_SOURCE, "1065"),
					createPersonAttribute(CommonMetadata._PersonAttributeType.NEAREST_HEALTH_CENTER, "Unknown")
			));
			// Format birthdate if it's not null
			String formattedBirthdate = null;
			if (pc.getBirthDate() != null) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				formattedBirthdate = sdf.format(pc.getBirthDate());
			}
			person.setBirthdate(formattedBirthdate != null ? new Date(formattedBirthdate) : null);
			person.setGender(pc.getSex());
			person.addName(name);
			/*person.addAddress(address);
			person.setAttributes(personAttributes);*/
			System.out.println("---------------------Person: "+ person.getFamilyName());
			return person;
		}
		private PersonAttribute createPersonAttribute(String uuid, String value) {
			PersonAttributeType attributeType = MetadataUtils.existing(PersonAttributeType.class, uuid);
			return new PersonAttribute(attributeType, value);
		}
		private String defaultIfEmpty(String value, String defaultValue) {
			return value == null || value.trim().isEmpty() ? defaultValue : value;
		}
}