package org.wso2.iam.training.v2.grant.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.user.core.service.RealmService;

/*
 * ============================================================================
 * BOILERPLATE - but notice there is NO registerService call here.
 *
 * A custom grant type is wired up through deployment.toml, not through the
 * service registry. This component exists only to catch the RealmService and
 * park it in the DataHolder so the grant class can use it.
 * ============================================================================
 */
@Component(
        name = "org.wso2.iam.training.v2.grant.internal."
                + "CRDBTrainingCustomMobileGrantV2ServiceComponent",
        immediate = true
)
public class CRDBTrainingCustomMobileGrantV2ServiceComponent {

    private static final Log log =
            LogFactory.getLog(CRDBTrainingCustomMobileGrantV2ServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        log.info("=== [Lab07-v2] Mobile grant is ready.");
    }

    @Reference(
            name = "RealmService",
            service = RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {

        CRDBTrainingCustomMobileGrantV2DataHolder.setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {

        CRDBTrainingCustomMobileGrantV2DataHolder.setRealmService(null);
    }
}
