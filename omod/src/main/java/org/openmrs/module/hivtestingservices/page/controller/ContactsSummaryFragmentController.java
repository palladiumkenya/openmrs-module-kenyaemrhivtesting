/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.hivtestingservices.page.controller;

import org.hibernate.jdbc.Work;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Form;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.annotation.FragmentParam;
import org.openmrs.ui.framework.fragment.FragmentModel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Visit summary fragment
 */
public class ContactsSummaryFragmentController {

	ConceptService conceptService = Context.getConceptService();
	SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	public void controller(FragmentModel model, @FragmentParam("patient") Patient patient) {
		DbSessionFactory sf = Context.getRegisteredComponents(DbSessionFactory.class).get(0);
		PatientService patientService = Context.getPatientService();
		Integer patient_Id = patient.getPersonId();
		final String sqlSelectQuery = "SELECT count(id) FROM openmrs.kenyaemr_hiv_testing_patient_contact where patient_related_to = patient_Id;";
		final List<SimpleObject> ret = new ArrayList<SimpleObject>();

		try {
			sf.getCurrentSession().doWork(new Work() {

				@Override
				public void execute(Connection connection) throws SQLException {
					PreparedStatement statement = connection.prepareStatement(sqlSelectQuery);

					try {

						ResultSet resultSet = statement.executeQuery();
						if (resultSet != null) {
							ResultSetMetaData metaData = resultSet.getMetaData();
							while (resultSet.next()) {
								Object[] row = new Object[metaData.getColumnCount()];
								for (int i = 1; i <= metaData.getColumnCount(); i++) {
									row[i - 1] = resultSet.getObject(i);
								}
								ret.add(SimpleObject.create(
										"date_created", row[0] != null ? row[0].toString() : "",
										"message_type", row[1] != null? row[1].toString() : "",
										"source", row[2] != null ? row[2].toString() : "",
										"status", row[3].toString().equals("1") ? "Processed": "Pending",
										"error", row[4] != null ? row[4].toString() : ""
								));
							}
						}
					}
					finally {
						try {
							if (statement != null) {
								statement.close();
							}
						}
						catch (Exception e) {}
					}
				}
			});
		}
		catch (Exception e) {
			throw new IllegalArgumentException("Unable to execute query", e);
		}

		model.put("logs", ret);

	}

}