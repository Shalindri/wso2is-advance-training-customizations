package org.wso2.iam.training.v2.authenticator.internal;

import org.wso2.carbon.user.core.service.RealmService;

/* A place to keep the RealmService. Same reason as lab 07 - see that file. */
public class CRDBTrainingCustomAuthenticatorV2DataHolder {

    private static RealmService realmService;

    private CRDBTrainingCustomAuthenticatorV2DataHolder() {
    }

    public static RealmService getRealmService() {

        return realmService;
    }

    public static void setRealmService(RealmService service) {

        realmService = service;
    }
}
