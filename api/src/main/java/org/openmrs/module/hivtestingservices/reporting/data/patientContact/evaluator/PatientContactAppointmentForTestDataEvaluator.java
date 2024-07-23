package org.openmrs.module.hivtestingservices.reporting.data.patientContact.evaluator;

import org.openmrs.annotation.Handler;
import org.openmrs.module.hivtestingservices.reporting.data.patientContact.EvaluatedPatientContactData;
import org.openmrs.module.hivtestingservices.reporting.data.patientContact.definition.PatientContactAppointmentForTestDataDefinition;
import org.openmrs.module.hivtestingservices.reporting.data.patientContact.definition.PatientContactDataDefinition;
import org.openmrs.module.hivtestingservices.reporting.data.patientContact.definition.PatientContactScreenedForIpvDataDefinition;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * Evaluates a VisitIdDataDefinition to produce a VisitData
 */
@Handler(supports=PatientContactAppointmentForTestDataDefinition.class, order=50)
public class PatientContactAppointmentForTestDataEvaluator implements PatientContactDataEvaluator {

    @Autowired
    private EvaluationService evaluationService;

    public EvaluatedPatientContactData evaluate(PatientContactDataDefinition definition, EvaluationContext context) throws EvaluationException {
        EvaluatedPatientContactData c = new EvaluatedPatientContactData(definition, context);

        String qry = "select c.patient_id, date(c.appointment_date) as appointment_date from kenyaemr_etl.etl_patient_contact c where c.voided=0; ";

        SqlQueryBuilder queryBuilder = new SqlQueryBuilder();
        queryBuilder.append(qry);
        Map<Integer, Object> data = evaluationService.evaluateToMap(queryBuilder, Integer.class, Object.class, context);
        c.setData(data);
        return c;
    }
}
