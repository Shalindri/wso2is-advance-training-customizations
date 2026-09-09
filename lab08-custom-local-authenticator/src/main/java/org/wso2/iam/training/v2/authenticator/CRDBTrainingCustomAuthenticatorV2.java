package org.wso2.iam.training.v2.authenticator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.AbstractApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.LocalApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.iam.training.v2.authenticator.internal.CRDBTrainingCustomAuthenticatorV2DataHolder;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
 * ============================================================================
 * LAB 08 (v2) - CUSTOM LOCAL AUTHENTICATOR
 * ============================================================================
 *
 * WHAT IT DOES
 *   Replaces the normal login step with our own. It shows the standard login
 *   page, then checks the username and password itself and logs a line.
 *
 * WHY YOU WOULD DO THIS
 *   This is the slot where you put YOUR OWN way of deciding who somebody is:
 *   call a legacy authentication API, check a smart card, require a specific
 *   group. Here it just asks the user store, so you can see the shape of the
 *   thing without another system in the way. The marked spot is where your own
 *   check would go.
 *
 * HOW IT GETS CALLED - TWO VISITS, NOT ONE
 *   This is the part that confuses people. Your authenticator is called twice:
 *
 *     Visit 1: the browser arrives with no username or password yet.
 *              canHandle() says false, so IS calls
 *              initiateAuthenticationRequest() -> we send the browser to the
 *              login page and stop.
 *
 *     Visit 2: the login page posts the username and password back.
 *              canHandle() now says true, so IS calls
 *              processAuthenticationResponse() -> we check them.
 *
 *   Getting canHandle() wrong is the classic bug: say true too early and IS
 *   skips the login page and then fails because there is nothing to check.
 * ============================================================================
 */
public class CRDBTrainingCustomAuthenticatorV2 extends AbstractApplicationAuthenticator
        implements LocalApplicationAuthenticator {

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(CRDBTrainingCustomAuthenticatorV2.class);

    /* This name must match the deployment.toml entry. */
    public static final String AUTHENTICATOR_NAME = "CRDBTrainingCustomAuthenticatorV2";

    /* What an administrator sees in the Console's Login Flow editor. */
    private static final String FRIENDLY_NAME = "CRDB Training Login (v2)";

    private static final String USERNAME_FIELD = "username";
    private static final String PASSWORD_FIELD = "password";

    @Override
    public String getName() {

        return AUTHENTICATOR_NAME;
    }

    @Override
    public String getFriendlyName() {

        return FRIENDLY_NAME;
    }

    /* "Do I have something to check yet?" See the two-visits note above. */
    @Override
    public boolean canHandle(HttpServletRequest request) {

        return request.getParameter(USERNAME_FIELD) != null
                && request.getParameter(PASSWORD_FIELD) != null;
    }

    /* Links visit 1 and visit 2 together. Always the framework's session key. */
    @Override
    public String getContextIdentifier(HttpServletRequest request) {

        return request.getParameter(FrameworkConstants.SESSION_DATA_KEY);
    }

    /* VISIT 1 - send the browser to the login page. */
    @Override
    protected void initiateAuthenticationRequest(HttpServletRequest request,
                                                 HttpServletResponse response,
                                                 AuthenticationContext context)
            throws AuthenticationFailedException {

        try {
            String loginPage = ConfigurationFacade.getInstance().getAuthenticationEndpointURL();
            String queryParams = FrameworkUtils.getQueryStringWithFrameworkContextId(
                    context.getQueryParams(),
                    context.getCallerSessionKey(),
                    context.getContextIdentifier());

            /* "&authenticators=<our name>" tells the login page it is for us. */
            String url = loginPage + "?" + queryParams + "&authenticators=" + getName();

            log.info("=== [Lab08-v2] Sending the user to the login page.");
            response.sendRedirect(url);

        } catch (IOException e) {
            throw new AuthenticationFailedException("Could not open the login page.", e);
        }
    }

    /* VISIT 2 - the username and password have arrived. Check them. */
    @Override
    protected void processAuthenticationResponse(HttpServletRequest request,
                                                 HttpServletResponse response,
                                                 AuthenticationContext context)
            throws AuthenticationFailedException {

        String username = request.getParameter(USERNAME_FIELD);
        String password = request.getParameter(PASSWORD_FIELD);

        String tenantDomain = context.getTenantDomain() != null
                ? context.getTenantDomain()
                : MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;

        try {
            /* ---------------- YOUR CHECK GOES HERE ---------------- */
            /* Right now we ask the user store. Replace this line with a call to
             * your own API, a database query, whatever the customer needs. */
            boolean ok = userStoreFor(tenantDomain).authenticate(username, password);

            if (!ok) {
                log.info("=== [Lab08-v2] Login failed for " + username);
                /* Deliberately vague: do not tell an attacker which half was wrong. */
                throw new AuthenticationFailedException("Invalid username or password.");
            }

            log.info("=== [Lab08-v2] Login OK for " + username);

            /* Tell IS who just logged in. Without this the login does not finish. */
            AuthenticatedUser user =
                    AuthenticatedUser.createLocalAuthenticatedUserFromSubjectIdentifier(username);
            user.setTenantDomain(tenantDomain);
            context.setSubject(user);

        } catch (UserStoreException e) {
            throw new AuthenticationFailedException("Could not check the password.", e);
        }
    }

    /*
     * Let the user try again after a wrong password, instead of the whole login
     * blowing up. Needed whenever you let the parent class drive the flow, which
     * is what we are doing here.
     */
    @Override
    protected boolean retryAuthenticationEnabled() {

        return true;
    }

    private UserStoreManager userStoreFor(String tenantDomain)
            throws UserStoreException, AuthenticationFailedException {

        RealmService realmService = CRDBTrainingCustomAuthenticatorV2DataHolder.getRealmService();
        if (realmService == null) {
            throw new AuthenticationFailedException("User management is not ready.");
        }
        int tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
        return realmService.getTenantUserRealm(tenantId).getUserStoreManager();
    }

    /*
     * ============================================================================
     * TRY THIS NEXT
     *   1. After a successful check, also require the user to have a country
     *      attribute (the one lab 01 sets), and fail the login if it is missing.
     *   2. Replace the authenticate() call with "return true" temporarily, to
     *      prove to yourself that this method really is the decision point.
     *      Then put it back.
     * ============================================================================
     */
}
