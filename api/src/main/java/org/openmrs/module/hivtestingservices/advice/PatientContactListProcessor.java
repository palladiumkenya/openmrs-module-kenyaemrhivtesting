package org.openmrs.module.hivtestingservices.advice;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.springframework.aop.AfterReturningAdvice;

import java.lang.reflect.Method;

public class PatientContactListProcessor implements AfterReturningAdvice {

    private Log log = LogFactory.getLog(this.getClass());
    public static final String HIV_FAMILY_HISTORY = "7efa0ee0-6617-4cd7-8310-9f95dfee7a82";
    @Override
    public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {

        if (method.getName().equals("saveEncounter")) {
            log.info("KenyaEMR HIV Testing. Method: " + method.getName() +
                    ". After advice called. Go ahead and implement it");
            System.out.println("KenyaEMR HIV Testing. Method: " + method.getName() +
                    ". After advice called. Go ahead and implement it");
            Encounter encounter = (Encounter) args[0];
            if(encounter != null && encounter.getForm().getUuid().equals(HIV_FAMILY_HISTORY)) {
                log.info("Hiv testing Kenyaemr. Got desired form");
                System.out.println("Hiv testing Kenyaemr. Got desired form");
            }
        }
    }
}
