package org.wso2.iam.training.v2.grant.internal;

import org.wso2.carbon.user.core.service.RealmService;

/*
 * ============================================================================
 * A PLACE TO KEEP THE RealmService.
 * ============================================================================
 *
 * WHY THIS EXISTS
 *   IS creates the grant class itself, from the class name in deployment.toml.
 *   Because IS does the creating, it cannot hand us the user management service
 *   the normal way. So the component next door puts it here on startup, and the
 *   grant class reads it from here. That is the whole purpose of this file.
 * ============================================================================
 */
public class CRDBTrainingCustomMobileGrantV2DataHolder {

    private static RealmService realmService;

    private CRDBTrainingCustomMobileGrantV2DataHolder() {
    }

    public static RealmService getRealmService() {

        return realmService;
    }

    public static void setRealmService(RealmService service) {

        realmService = service;
    }
}
