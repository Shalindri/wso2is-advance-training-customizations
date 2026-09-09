package org.wso2.iam.training.v2.provisioning;

import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.provisioning.AbstractOutboundProvisioningConnector;
import org.wso2.carbon.identity.provisioning.AbstractProvisioningConnectorFactory;
import org.wso2.carbon.identity.provisioning.IdentityProvisioningException;

import java.util.ArrayList;
import java.util.List;

/*
 * ============================================================================
 * THE FACTORY - builds connectors, and describes the Console form.
 * ============================================================================
 *
 * Two jobs:
 *   1. buildConnector()            - hand IS a new connector when it needs one
 *   2. getConfigurationProperties()- describe the settings, and the Console
 *                                    draws the form from this list
 *
 * getConnectorType() is the name that appears in the Console's connector list.
 * ============================================================================
 */
public class CRDBTrainingCustomProvisioningConnectorFactoryV2 extends AbstractProvisioningConnectorFactory {

    /* The name shown in the Console. */
    public static final String CONNECTOR_TYPE = "crdbTrainingCustomConnectorV2";

    @Override
    public String getConnectorType() {

        return CONNECTOR_TYPE;
    }

    @Override
    protected AbstractOutboundProvisioningConnector buildConnector(Property[] properties)
            throws IdentityProvisioningException {

        CRDBTrainingCustomProvisioningConnectorV2 connector =
                new CRDBTrainingCustomProvisioningConnectorV2();
        connector.init(properties);
        return connector;
    }

    /* One text box. Add more Property objects here to get more fields. */
    @Override
    public List<Property> getConfigurationProperties() {

        Property targetSystem = new Property();
        targetSystem.setName(CRDBTrainingCustomProvisioningConnectorV2.PROP_TARGET_SYSTEM);
        targetSystem.setDisplayName("Target system name");
        targetSystem.setDescription("A label for the system you are copying users to.");
        targetSystem.setDefaultValue("the other system");
        targetSystem.setRequired(true);
        targetSystem.setDisplayOrder(1);
        /* For a password field: targetSystem.setConfidential(true); */

        List<Property> properties = new ArrayList<>();
        properties.add(targetSystem);
        return properties;
    }
}
