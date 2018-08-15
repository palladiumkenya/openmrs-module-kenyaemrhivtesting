package org.openmrs.module.hivtestingservices.advice;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.module.hivtestingservices.advice.model.AOPEncounterEntry;
import org.openmrs.module.hivtestingservices.api.HTSService;
import org.springframework.aop.AfterReturningAdvice;

import java.lang.reflect.Method;
import java.util.Date;

public class PatientContactListProcessor implements AfterReturningAdvice {

    private Log log = LogFactory.getLog(this.getClass());
    public static final String CONTACT_LISTING_FORM = "d4493a7c-49fc-11e8-842f-0ed5f89f718b";
    @Override
    public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {

        if (method.getName().equals("saveEncounter")) {
            Encounter encounter = (Encounter) args[0];
            if(encounter != null && encounter.getForm() != null && encounter.getForm().getUuid().equals(CONTACT_LISTING_FORM)) {
                AOPEncounterEntry entry = new AOPEncounterEntry();
                entry.setDateCreated(new Date());
                entry.setEncounterUUID(encounter.getUuid());
                entry.setFormUUID(encounter.getForm().getUuid());
                entry.setTargetModule("HTS");
                entry.setStatus(0);
                org.openmrs.api.context.Context.getService(HTSService.class).saveAopEncounterEntry(entry);
            }
        }
    }
}
