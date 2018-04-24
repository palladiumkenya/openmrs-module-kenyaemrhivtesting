package org.openmrs.module.hivtestingservices.converter;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.hivtestingservices.api.ContactTrace;
import org.openmrs.module.hivtestingservices.api.HTSService;
import org.openmrs.module.hivtestingservices.api.PatientContact;
import org.openmrs.ui.framework.converter.util.ConversionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToContactTraceConverter implements Converter<String, ContactTrace> {

    /**
     * @see Converter#convert(Object)
     */
    @Override
    public ContactTrace convert(String id) {
        HTSService service = Context.getService(HTSService.class);
        if (org.apache.commons.lang.StringUtils.isBlank(id)) {
            return null;
        } else if (ConversionUtil.onlyDigits(id)) {
            return service.getPatientContactTraceById(Integer.valueOf(id));
        }
        return null;
    }
}
