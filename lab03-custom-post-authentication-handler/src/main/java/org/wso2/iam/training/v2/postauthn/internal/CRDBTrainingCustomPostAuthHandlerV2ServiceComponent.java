package org.wso2.iam.training.v2.postauthn.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.PostAuthenticationHandler;
import org.wso2.iam.training.v2.postauthn.CRDBTrainingCustomPostAuthHandlerV2;

/* BOILERPLATE - same shape as labs 01 and 02. */
@Component(
        name = "org.wso2.iam.training.v2.postauthn.internal."
                + "CRDBTrainingCustomPostAuthHandlerV2ServiceComponent",
        immediate = true
)
public class CRDBTrainingCustomPostAuthHandlerV2ServiceComponent {

    private static final Log log =
            LogFactory.getLog(CRDBTrainingCustomPostAuthHandlerV2ServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        try {
            context.getBundleContext().registerService(
                    PostAuthenticationHandler.class.getName(),
                    new CRDBTrainingCustomPostAuthHandlerV2(),
                    null);
            log.info("=== [Lab03-v2] Post authentication handler registered.");
        } catch (Throwable e) {
            log.error("=== [Lab03-v2] Failed to register the handler.", e);
        }
    }
}
