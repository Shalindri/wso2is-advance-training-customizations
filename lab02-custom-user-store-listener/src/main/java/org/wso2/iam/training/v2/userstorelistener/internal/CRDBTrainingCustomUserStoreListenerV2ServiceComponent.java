package org.wso2.iam.training.v2.userstorelistener.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.user.core.listener.UserOperationEventListener;
import org.wso2.iam.training.v2.userstorelistener.CRDBTrainingCustomUserStoreListenerV2;

/*
 * ============================================================================
 * BOILERPLATE - same shape as lab 01, only the type on registerService differs.
 * ============================================================================
 */
@Component(
        name = "org.wso2.iam.training.v2.userstorelistener.internal."
                + "CRDBTrainingCustomUserStoreListenerV2ServiceComponent",
        immediate = true
)
public class CRDBTrainingCustomUserStoreListenerV2ServiceComponent {

    private static final Log log =
            LogFactory.getLog(CRDBTrainingCustomUserStoreListenerV2ServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        try {
            context.getBundleContext().registerService(
                    UserOperationEventListener.class.getName(),
                    new CRDBTrainingCustomUserStoreListenerV2(),
                    null);
            log.info("=== [Lab02-v2] Listener registered.");
        } catch (Throwable e) {
            log.error("=== [Lab02-v2] Failed to register the listener.", e);
        }
    }
}
