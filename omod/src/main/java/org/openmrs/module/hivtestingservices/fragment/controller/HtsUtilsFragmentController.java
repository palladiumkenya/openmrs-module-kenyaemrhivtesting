package org.openmrs.module.hivtestingservices.fragment.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.hivtestingservices.api.HTSService;
import org.openmrs.module.hivtestingservices.api.PatientContact;
import org.openmrs.ui.framework.fragment.action.SuccessResult;
import org.springframework.web.bind.annotation.RequestParam;

public class HtsUtilsFragmentController {
    protected static final Log log = LogFactory.getLog(HtsUtilsFragmentController.class);
    /**
     * Voids the given patient contact
     *
     * @param contact the contact
     * @return the simplified contact
     */
    public SuccessResult voidContact(@RequestParam("id") PatientContact contact) {
        HTSService service = Context.getService(HTSService.class);
        contact.setVoided(true);
        PatientContact c = service.savePatientContact(contact);

        if (c != null) {
            return new SuccessResult("Patient contact voided");

        }
        return null;
    }

}



