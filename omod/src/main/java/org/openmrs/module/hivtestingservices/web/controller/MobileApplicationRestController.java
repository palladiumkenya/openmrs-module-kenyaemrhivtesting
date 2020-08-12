package org.openmrs.module.hivtestingservices.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.hivtestingservices.util.MedicDataExchange;
import org.openmrs.module.hivtestingservices.util.Utils;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * The main controller.
 */
@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/edata")
public class MobileApplicationRestController extends BaseRestController {
    protected final Log log = LogFactory.getLog(getClass());

    @RequestMapping(method = RequestMethod.POST, value = "/medicregistration")
    @ResponseBody
    public Object receiveSHR(HttpServletRequest request) {

        String requestBody = null;
        try {
            requestBody = Utils.fetchRequestBody(request.getReader());
        } catch (IOException e) {
            return new SimpleObject().add("ServerResponse", "Error extracting request body");
        }

        if (requestBody != null) {
            MedicDataExchange shr = new MedicDataExchange();
            return shr.processIncomingRegistration(requestBody);

        }
        return new SimpleObject().add("Report", "The request could not be interpreted properly");
    }

    /**
     * processes incoming medic queue data
     * @param request
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/medicformsdata") // end point for medic queue data
    @ResponseBody
    public Object processMedicQueueData(HttpServletRequest request) {
        String requestBody = null;
        try {
            requestBody = Utils.fetchRequestBody(request.getReader());
        } catch (IOException e) {
            return new SimpleObject().add("ServerResponse", "Error extracting request body");
        }

        if (requestBody != null) {
            MedicDataExchange shr = new MedicDataExchange();
            return shr.processIncomingFormData(requestBody);

        }
        return new SimpleObject().add("Report", "The request could not be interpreted properly");
    }

    /**
     * processes incoming medic contacts data
     * @param request
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/medicContactsdata") // end point for medic contacts data
    @ResponseBody
    public Object processMedicContactsData(HttpServletRequest request) {
        String requestBody = null;
        try {
            requestBody = Utils.fetchRequestBody(request.getReader());
        } catch (IOException e) {
            return new SimpleObject().add("ServerResponse", "Error extracting request body");
        }

        if (requestBody != null) {
            MedicDataExchange shr = new MedicDataExchange();
            return shr.addContactListToDataqueue(requestBody);

        }
        return new SimpleObject().add("Report", "The request could not be interpreted properly");
    }
}
