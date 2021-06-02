package org.openmrs.module.hivtestingservices.reporting.data.converter;

import org.openmrs.module.reporting.data.converter.DataConverter;

/**
 * Created by Steve on 30 May, 2021
 * Converter for KP types
 */
public class KPTypeConverter implements DataConverter {
    @Override
    public Object convert(Object obj) {

        if (obj == null) {
            return "";
        }

        String value = (String) obj;

        if(value == null) {
            return  "";
        }

        else if(value.equals("Men who have sex with men")) {
            return "MSM";
        }
        else if(value.equals("People who inject drugs")){
            return "DU";
        }
        else if(value.equals("Female sex worker")){
            return "FSW";
        }
        return  "";

    }

    @Override
    public Class<?> getInputDataType() {
        return Object.class;
    }

    @Override
    public Class<?> getDataType() {
        return String.class;
    }

}
