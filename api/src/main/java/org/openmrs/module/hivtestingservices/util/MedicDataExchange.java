package org.openmrs.module.hivtestingservices.util;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Provider;
import org.openmrs.api.context.Context;
import org.openmrs.module.hivtestingservices.api.HTSService;
import org.openmrs.module.hivtestingservices.api.service.DataService;
import org.openmrs.module.hivtestingservices.api.service.MedicQueData;
import org.openmrs.module.hivtestingservices.model.DataSource;

import javax.swing.*;
import java.io.IOException;

public class MedicDataExchange {
    HTSService htsService = Context.getService(HTSService.class);
    DataService dataService = Context.getService(DataService.class);

    /**
     * processes results from cht     *
     * @param resultPayload this should be an object
     * @return
     */
    public String processIncomingMedicDataQueue(String resultPayload) {


        Integer statusCode;
        String statusMsg;
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonNode = null;
        try {
            jsonNode = (ObjectNode) mapper.readTree(resultPayload);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (jsonNode != null) {
            String discriminator = jsonNode.path("discriminator").path("discriminator").getTextValue();
            String formName = "";
            String formDataUuid = jsonNode.path("encounter").path("encounter.form_uuid").getTextValue();
            String patientUuid = jsonNode.path("patient").path("patient.uuid").getTextValue();
            Integer locationId = Integer.parseInt(jsonNode.path("encounter").path("encounter.location_id").getTextValue());
            String providerString = jsonNode.path("encounter").path("encounter.provider_id").getTextValue();

            saveMedicDataQueue(resultPayload,locationId,providerString,patientUuid,discriminator,formName,formDataUuid);

        }
        return "Data queue created successfully";
    }


    private void saveMedicDataQueue(String payload, Integer locationId, String providerString, String patientUuid, String discriminator,
                                    String formName, String formUuid) {
        DataSource dataSource = dataService.getDataSource(1);
        Provider provider = Context.getProviderService().getProviderByIdentifier(providerString);
        Location location = Context.getLocationService().getLocation(locationId);
        Form form = Context.getFormService().getFormByUuid(formUuid);

        MedicQueData medicQueData = new MedicQueData();
        if(form !=null && form.getName() !=null) { medicQueData.setFormName(form.getName());
        }else {
            medicQueData.setFormName("Unknown name");
        }
        medicQueData.setPayload(payload);
        medicQueData.setDiscriminator(discriminator);
        medicQueData.setPatientUuid(patientUuid);
        medicQueData.setFormDataUuid(formUuid);
        medicQueData.setProvider(provider);
        medicQueData.setLocation(location);
        medicQueData.setDataSource(dataSource);
        htsService.saveQueData(medicQueData);

    }
}
