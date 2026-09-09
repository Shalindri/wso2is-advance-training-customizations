package org.wso2.iam.training.v2.userstore.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.iam.training.v2.userstore.CRDBTrainingCustomUserStoreManagerV2;

/*
 * ============================================================================
 * BOILERPLATE - with one detail that is easy to get wrong.
 * ============================================================================
 *
 * REGISTER UNDER org.wso2.carbon.user.api.UserStoreManager
 *
 * There are two interfaces with the same short name, UserStoreManager - one in
 * ...user.api and one in ...user.core. IS only looks for the "api" one when it
 * builds the list of user store types for the Console.
 *
 * Use the wrong one and everything LOOKS fine - the jar loads, this log line
 * prints - but your user store never appears in the Console. Both names are
 * registered below so it cannot go wrong.
 * ============================================================================
 */
@Component(
        name = "org.wso2.iam.training.v2.userstore.internal."
                + "CRDBTrainingCustomUserStoreManagerV2ServiceComponent",
        immediate = true
)
public class CRDBTrainingCustomUserStoreManagerV2ServiceComponent {

    private static final Log log =
            LogFactory.getLog(CRDBTrainingCustomUserStoreManagerV2ServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        try {
            String[] bothNames = {
                    org.wso2.carbon.user.api.UserStoreManager.class.getName(),
                    org.wso2.carbon.user.core.UserStoreManager.class.getName()
            };
            context.getBundleContext().registerService(
                    bothNames,
                    new CRDBTrainingCustomUserStoreManagerV2(),
                    null);
            log.info("=== [Lab05-v2] User store type registered.");
        } catch (Throwable e) {
            log.error("=== [Lab05-v2] Failed to register the user store type.", e);
        }
    }

    /*
     * This makes the server wait until the user management layer is ready before
     * running activate() above. Without it we can register too early and be missed.
     */
    @Reference(
            name = "RealmService",
            service = RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {
        /* nothing to store - we only want the timing */
    }

    protected void unsetRealmService(RealmService realmService) {
        /* nothing to clean up */
    }
}
