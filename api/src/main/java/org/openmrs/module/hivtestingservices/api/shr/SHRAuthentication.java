package org.openmrs.module.hivtestingservices.api.shr;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.openmrs.User;
import org.openmrs.api.context.Context;

public class SHRAuthentication {

    public static ObjectNode authenticateUser(String userName, String pwd) {
        final JsonNodeFactory factory = JsonNodeFactory.instance;
        ObjectNode node = factory.objectNode();
        try {
            Context.authenticate(userName, pwd);
            User authenticatedUser = Context.getAuthenticatedUser();
            if (authenticatedUser != null) {

                node.put("STATUS", "true");
                node.put("DISPLAYNAME", authenticatedUser.getDisplayString());
            }

        } catch (Exception e) {
            node.put("STATUS", "false");
            node.put("DISPLAYNAME", "");
        }
        return node;
    }
}
