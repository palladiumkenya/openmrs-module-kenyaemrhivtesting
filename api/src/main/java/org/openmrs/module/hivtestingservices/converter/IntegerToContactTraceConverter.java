package org.openmrs.module.hivtestingservices.converter;

import org.openmrs.api.context.Context;
import org.openmrs.module.hivtestingservices.api.ContactTrace;
import org.openmrs.module.hivtestingservices.api.HTSService;
import org.openmrs.ui.framework.converter.util.ConversionUtil;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class IntegerToContactTraceConverter implements Converter<Integer, ContactTrace> {

    /**
     * @see Converter#convert(Object)
     */
    @Override
    public ContactTrace convert(Integer id) {
        HTSService service = Context.getService(HTSService.class);
        if (id == null) {
            return null;
        } else {
            return service.getPatientContactTraceById(id);
        }

    }
}
