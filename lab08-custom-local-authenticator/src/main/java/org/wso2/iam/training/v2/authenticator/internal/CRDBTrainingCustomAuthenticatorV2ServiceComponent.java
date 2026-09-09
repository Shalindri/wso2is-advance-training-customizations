package org.wso2.iam.training.v2.authenticator.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.iam.training.v2.authenticator.CRDBTrainingCustomAuthenticatorV2;

/*
 * ============================================================================
 * BOILERPLATE.
 *
 * Register under ApplicationAuthenticator - the general type - not under
 * LocalApplicationAuthenticator. IS collects them all under the general type
 * and then works out which ones are local.
 * ============================================================================
 */
@Component(
        name = "org.wso2.iam.training.v2.authenticator.internal."
                + "CRDBTrainingCustomAuthenticatorV2ServiceComponent",
        immediate = true
)
public class CRDBTrainingCustomAuthenticatorV2ServiceComponent {

    private static final Log log =
            LogFactory.getLog(CRDBTrainingCustomAuthenticatorV2ServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        try {
            context.getBundleContext().registerService(
                    ApplicationAuthenticator.class.getName(),
                    new CRDBTrainingCustomAuthenticatorV2(),
                    null);
            log.info("=== [Lab08-v2] Authenticator registered.");
        } catch (Throwable e) {
            log.error("=== [Lab08-v2] Failed to register the authenticator.", e);
        }
    }

    @Reference(
            name = "RealmService",
            service = RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {

        CRDBTrainingCustomAuthenticatorV2DataHolder.setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {

        CRDBTrainingCustomAuthenticatorV2DataHolder.setRealmService(null);
    }
}
