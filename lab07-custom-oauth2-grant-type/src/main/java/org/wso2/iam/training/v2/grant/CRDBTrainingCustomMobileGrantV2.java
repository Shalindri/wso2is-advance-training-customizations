package org.wso2.iam.training.v2.grant;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.model.RequestParameter;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.identity.oauth2.token.handlers.grant.AbstractAuthorizationGrantHandler;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.iam.training.v2.grant.internal.CRDBTrainingCustomMobileGrantV2DataHolder;

/*
 * ============================================================================
 * LAB 07 (v2) - CUSTOM OAUTH 2.0 GRANT TYPE
 * ============================================================================
 *
 * WHAT IT DOES
 *   Lets an app get an access token using a MOBILE NUMBER and a password,
 *   instead of a username and a password.
 *
 *     POST /oauth2/token
 *     grant_type=mobile&mobileNumber=+94771234567&password=....
 *
 * WHY YOU WOULD DO THIS
 *   A phone app where people know their phone number but not their username.
 *   The same pattern covers "log in with employee number", "log in with NIC".
 *
 * WHAT A GRANT TYPE IS
 *   A grant type is a recipe for "here is some proof of who I am, give me a
 *   token". OAuth 2.0 ships a few standard recipes; this is a custom one.
 *
 * TWO CLASSES ARE NEEDED
 *   THIS one     - checks the proof and says who the user is
 *   the Validator - checks the request even has the fields we need
 *
 * WE DO NOT WRITE THE TOKEN
 *   We only say "yes, this is user bob" by calling setAuthorizedUser(), then
 *   return true. The product then makes a real, revocable token for us. Do not
 *   be tempted to build a token string yourself - see README.
 * ============================================================================
 */
public class CRDBTrainingCustomMobileGrantV2 extends AbstractAuthorizationGrantHandler {

    private static final Log log = LogFactory.getLog(CRDBTrainingCustomMobileGrantV2.class);

    /* The two extra fields we expect in the token request. */
    public static final String MOBILE_PARAM = "mobileNumber";
    public static final String PASSWORD_PARAM = "password";

    /* The attribute we search users by. */
    private static final String MOBILE_CLAIM = "http://wso2.org/claims/mobile";

    /*
     * Called by IS when someone asks for a token with grant_type=mobile.
     * Return true to allow, false to refuse.
     */
    @Override
    public boolean validateGrant(OAuthTokenReqMessageContext context) throws IdentityOAuth2Exception {

        String mobileNumber = readParameter(context, MOBILE_PARAM);
        String password = readParameter(context, PASSWORD_PARAM);

        if (mobileNumber == null || password == null) {
            log.warn("=== [Lab07-v2] Request is missing the mobile number or the password.");
            return false;
        }

        String tenantDomain = context.getOauth2AccessTokenReqDTO().getTenantDomain();
        if (tenantDomain == null) {
            tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }

        try {
            UserStoreManager userStore = userStoreFor(tenantDomain);

            /* ---------- STEP 1: find who owns that phone number ---------- */
            String[] matches = userStore.getUserList(MOBILE_CLAIM, mobileNumber, null);

            if (matches == null || matches.length == 0) {
                log.warn("=== [Lab07-v2] Nobody has that mobile number.");
                return false;
            }
            if (matches.length > 1) {
                /*
                 * Two people share the number. Refuse - picking the first one
                 * would let one person get a token for somebody else.
                 */
                log.warn("=== [Lab07-v2] " + matches.length + " users share that number. Refusing.");
                return false;
            }
            String username = matches[0];

            /* ---------- STEP 2: check their password ---------- */
            if (!userStore.authenticate(username, password)) {
                log.warn("=== [Lab07-v2] Wrong password for " + username);
                return false;
            }

            /* ---------- STEP 3: tell IS who this is ---------- */
            AuthenticatedUser user =
                    AuthenticatedUser.createLocalAuthenticatedUserFromSubjectIdentifier(username);
            user.setTenantDomain(tenantDomain);
            context.setAuthorizedUser(user);
            context.setScope(context.getOauth2AccessTokenReqDTO().getScope());

            log.info("=== [Lab07-v2] Mobile login OK. Issuing a token for " + username);
            return true;

        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new IdentityOAuth2Exception("Could not look up the user.", e);
        }
    }

    /* Pull one field out of the token request. */
    private String readParameter(OAuthTokenReqMessageContext context, String name) {

        RequestParameter[] parameters = context.getOauth2AccessTokenReqDTO().getRequestParameters();
        if (parameters == null) {
            return null;
        }
        for (RequestParameter parameter : parameters) {
            if (name.equals(parameter.getKey())
                    && parameter.getValue() != null && parameter.getValue().length > 0) {
                return parameter.getValue()[0];
            }
        }
        return null;
    }

    /*
     * Get the user store for this tenant.
     *
     * The cast to ...user.core.UserStoreManager is needed because
     * getUserList(attribute, value, profile) only exists on that interface,
     * not on the ...user.api one this method officially returns.
     */
    private UserStoreManager userStoreFor(String tenantDomain)
            throws org.wso2.carbon.user.api.UserStoreException, IdentityOAuth2Exception {

        RealmService realmService = CRDBTrainingCustomMobileGrantV2DataHolder.getRealmService();
        if (realmService == null) {
            throw new IdentityOAuth2Exception("User management is not ready.");
        }
        int tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
        return (UserStoreManager) realmService.getTenantUserRealm(tenantId).getUserStoreManager();
    }

    /* The user is a person, not the application itself. */
    @Override
    public boolean isOfTypeApplicationUser() throws IdentityOAuth2Exception {

        return true;
    }

    /* No separate consent step: the person asking IS the owner of the account. */
    @Override
    public boolean authorizeAccessDelegation(OAuthTokenReqMessageContext context)
            throws IdentityOAuth2Exception {

        return true;
    }

    /*
     * ============================================================================
     * TRY THIS NEXT
     *   1. Send a wrong password and watch the log line appear.
     *   2. Add a rule: only allow the grant if the user's country attribute is
     *      set (the attribute lab 01 fills in).
     * ============================================================================
     */
}
