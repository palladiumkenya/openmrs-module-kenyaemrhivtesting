package org.openmrs.module.hivtestingservices.api.shr;

import org.openmrs.api.APIException;
import org.openmrs.api.OrderContext;
import org.openmrs.api.OrderNumberGenerator;
import org.openmrs.api.context.Context;
import org.springframework.stereotype.Component;

@Component("chtOrderNumberGenerator")
public class CHTOrderNumberGenerator  implements OrderNumberGenerator {
    private static final String ORDER_NUMBER_PREFIX = "ORD-";
    @Override
    public String getNewOrderNumber(OrderContext orderContext) throws APIException {
        if (orderContext != null && orderContext.getAttribute("orderNumber") != null) {
            return orderContext.getAttribute("orderNumber").toString();
        } else {
            return ORDER_NUMBER_PREFIX + Context.getOrderService().getNextOrderNumberSeedSequenceValue();
        }
    }
}
