package org.openmrs.module.hivtestingservices.converter;

import org.openmrs.api.context.Context;
import org.openmrs.module.hivtestingservices.api.ContactTrace;
import org.openmrs.module.hivtestingservices.api.HTSService;
import org.openmrs.module.hivtestingservices.api.PatientContact;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class IntegerToPatientContactConverter implements Converter<Integer, PatientContact> {

    /**
     * @see Converter#convert(Object)
     */
    @Override
    public PatientContact convert(Integer id) {
        HTSService service = Context.getService(HTSService.class);
        if (id == null) {
            return null;
        } else {
            return service.getPatientContactByID(id);
        }

    }
}
