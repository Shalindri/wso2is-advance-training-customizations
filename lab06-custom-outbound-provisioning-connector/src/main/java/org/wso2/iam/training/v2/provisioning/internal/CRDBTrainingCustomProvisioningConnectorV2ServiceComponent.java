package org.wso2.iam.training.v2.provisioning.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.identity.provisioning.AbstractProvisioningConnectorFactory;
import org.wso2.iam.training.v2.provisioning.CRDBTrainingCustomProvisioningConnectorFactoryV2;

/*
 * ============================================================================
 * BOILERPLATE - register the FACTORY, not the connector.
 *
 * IS wants one long-lived factory and asks it for a connector whenever it needs
 * one. Registering a connector here instead is the usual reason a custom
 * connector never turns up in the Console.
 * ============================================================================
 */
@Component(
        name = "org.wso2.iam.training.v2.provisioning.internal."
                + "CRDBTrainingCustomProvisioningConnectorV2ServiceComponent",
        immediate = true
)
public class CRDBTrainingCustomProvisioningConnectorV2ServiceComponent {

    private static final Log log =
            LogFactory.getLog(CRDBTrainingCustomProvisioningConnectorV2ServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        try {
            context.getBundleContext().registerService(
                    AbstractProvisioningConnectorFactory.class.getName(),
                    new CRDBTrainingCustomProvisioningConnectorFactoryV2(),
                    null);
            log.info("=== [Lab06-v2] Provisioning connector registered.");
        } catch (Throwable e) {
            log.error("=== [Lab06-v2] Failed to register the connector.", e);
        }
    }
}
