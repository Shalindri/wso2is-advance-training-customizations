package org.wso2.iam.training.v2.eventhandler.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.identity.event.handler.AbstractEventHandler;
import org.wso2.iam.training.v2.eventhandler.CRDBTrainingCustomEventHandlerV2;

/*
 * ============================================================================
 * BOILERPLATE - you can copy this file between labs and change two words.
 * ============================================================================
 *
 * WHAT IT DOES
 *   Tells WSO2 IS "here is my handler, please use it". Without this file your
 *   class is just a class sitting in a jar and nothing ever calls it.
 *
 * WHEN IT RUNS
 *   Once, while the server starts.
 * ============================================================================
 */
@Component(
        name = "org.wso2.iam.training.v2.eventhandler.internal.CRDBTrainingCustomEventHandlerV2ServiceComponent",
        immediate = true
)
public class CRDBTrainingCustomEventHandlerV2ServiceComponent {

    private static final Log log =
            LogFactory.getLog(CRDBTrainingCustomEventHandlerV2ServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        try {
            /* "Register" = hand our object to IS under the type it looks for. */
            context.getBundleContext().registerService(
                    AbstractEventHandler.class.getName(),
                    new CRDBTrainingCustomEventHandlerV2(),
                    null);
            log.info("=== [Lab01-v2] Event handler registered.");
        } catch (Throwable e) {
            log.error("=== [Lab01-v2] Failed to register the event handler.", e);
        }
    }
}
