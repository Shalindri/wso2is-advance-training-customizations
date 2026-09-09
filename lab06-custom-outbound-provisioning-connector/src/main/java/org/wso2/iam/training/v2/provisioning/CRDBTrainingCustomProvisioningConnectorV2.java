package org.wso2.iam.training.v2.provisioning;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.provisioning.AbstractOutboundProvisioningConnector;
import org.wso2.carbon.identity.provisioning.IdentityProvisioningConstants;
import org.wso2.carbon.identity.provisioning.IdentityProvisioningException;
import org.wso2.carbon.identity.provisioning.ProvisionedIdentifier;
import org.wso2.carbon.identity.provisioning.ProvisioningEntity;
import org.wso2.carbon.identity.provisioning.ProvisioningOperation;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/*
 * ============================================================================
 * LAB 06 (v2) - CUSTOM OUTBOUND PROVISIONING CONNECTOR
 * ============================================================================
 *
 * WHAT IT DOES
 *   Every time a user is created, changed or deleted in WSO2 IS, this prints a
 *   log line saying what happened and to whom.
 *
 * WHY YOU WOULD DO THIS
 *   "Outbound provisioning" means copying users OUT to another system - a CRM,
 *   a payroll app, an internal database. A real connector would make an HTTP
 *   call at the marked spots below. Printing a log line instead keeps the lab
 *   to one machine with nothing else to install, and the shape of the code is
 *   identical to a real one.
 *
 * THE ONE METHOD THAT MATTERS
 *   provision() - it is called for create, update AND delete, and you look at
 *   getOperation() to see which one you got.
 *
 * NOTE - NO HTML ANYWHERE
 *   The settings form you fill in on the Console is generated from the list of
 *   Property objects in the Factory class next door. You never write a UI.
 * ============================================================================
 */
public class CRDBTrainingCustomProvisioningConnectorV2 extends AbstractOutboundProvisioningConnector {

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(CRDBTrainingCustomProvisioningConnectorV2.class);

    /* Matches the property name in the Factory. */
    static final String PROP_TARGET_SYSTEM = "crdb-v2-target-system";

    /* Whatever the administrator typed into the Console form. */
    private String targetSystem = "the other system";

    /* Called once, with the settings from the Console. */
    @Override
    public void init(Property[] properties) throws IdentityProvisioningException {

        if (properties != null) {
            for (Property property : properties) {
                if (PROP_TARGET_SYSTEM.equals(property.getName()) && property.getValue() != null) {
                    targetSystem = property.getValue();
                }
                /* This flag has to be picked up by hand; IS does not set it for you. */
                if (IdentityProvisioningConstants.JIT_PROVISIONING_ENABLED.equals(property.getName())) {
                    jitProvisioningEnabled = "true".equals(property.getValue());
                }
            }
        }
        log.info("=== [Lab06-v2] Connector ready. Target system = " + targetSystem);
    }

    /* Called for create, update and delete. */
    @Override
    public ProvisionedIdentifier provision(ProvisioningEntity entity)
            throws IdentityProvisioningException {

        if (entity == null) {
            return null;
        }

        String username = readUsername(entity);
        ProvisioningOperation operation = entity.getOperation();

        /* ---------------- your code goes here ---------------- */
        if (ProvisioningOperation.POST == operation) {

            /* A real connector would POST the user to the other system here. */
            String newId = UUID.randomUUID().toString();
            log.info("=== [Lab06-v2] CREATE " + username + " in " + targetSystem
                    + " (their id there: " + newId + ")");

            /*
             * Only a CREATE returns an id. IS remembers it so that a later
             * update or delete knows which record to touch. Returning a new id
             * on an update would throw away that link, so those return null.
             */
            ProvisionedIdentifier identifier = new ProvisionedIdentifier();
            identifier.setIdentifier(newId);
            return identifier;

        } else if (ProvisioningOperation.PUT == operation) {

            log.info("=== [Lab06-v2] UPDATE " + username + " in " + targetSystem);

        } else if (ProvisioningOperation.DELETE == operation) {

            log.info("=== [Lab06-v2] DELETE " + username + " from " + targetSystem);

        } else {
            log.warn("=== [Lab06-v2] Do not know how to handle: " + operation);
        }

        return null;
    }

    /*
     * Tell IS which "dialect" (naming scheme) to translate the user's
     * attributes into before handing them to us. Leave this out and we get a
     * user with NO attributes at all - the default is "translate nothing".
     */
    @Override
    public String getClaimDialectUri() throws IdentityProvisioningException {

        return IdentityProvisioningConstants.WSO2_CARBON_DIALECT;
    }

    private String readUsername(ProvisioningEntity entity) {

        List<String> names = getUserNames(entity.getAttributes());
        if (names != null && !names.isEmpty()) {
            return names.get(0);
        }
        return entity.getEntityName();
    }

    /*
     * ============================================================================
     * TRY THIS NEXT
     *   1. Log the user's email as well. Attributes arrive as a map:
     *        Map<String, String> attrs = getSingleValuedClaims(entity.getAttributes());
     *        log.info(attrs.get("http://wso2.org/claims/emailaddress"));
     *      Never log anything with "password" in the name.
     *   2. Add a second setting in the Factory and read it in init().
     * ============================================================================
     */
}
