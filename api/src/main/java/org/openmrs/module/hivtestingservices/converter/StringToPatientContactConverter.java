package org.openmrs.module.hivtestingservices.converter;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Relationship;
import org.openmrs.api.context.Context;
import org.openmrs.module.hivtestingservices.api.HTSService;
import org.openmrs.module.hivtestingservices.api.PatientContact;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToPatientContactConverter implements Converter<String, PatientContact> {

    /**
     * @see org.springframework.core.convert.converter.Converter#convert(Object)
     */
    @Override
    public PatientContact convert(String source) {
        if (StringUtils.isEmpty(source)) {
            return null;
        }

        return Context.getService(HTSService.class).getPatientContactByID(Integer.valueOf(source));
    }
}
