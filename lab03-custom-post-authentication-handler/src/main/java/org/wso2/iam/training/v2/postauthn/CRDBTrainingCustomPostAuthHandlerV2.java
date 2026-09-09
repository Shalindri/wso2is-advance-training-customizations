package org.wso2.iam.training.v2.postauthn;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.PostAuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.AbstractPostAuthnHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.PostAuthnHandlerFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
 * ============================================================================
 * LAB 03 (v2) - CUSTOM POST AUTHENTICATION HANDLER
 * ============================================================================
 *
 * WHAT IT DOES
 *   Runs immediately after somebody's login succeeds, and prints one line
 *   saying who logged in and which application they logged in to.
 *
 * WHY YOU WOULD DO THIS
 *   This is the "the login worked, now do something" slot. Real uses: write an
 *   audit record, call another system, or refuse the login after an extra check.
 *
 * WHEN IT RUNS
 *   After the password (or OTP, or whatever) has been accepted, but BEFORE the
 *   user is handed back to the application. The login is not finished until
 *   every post-authentication handler has said it is happy.
 *
 * IMPORTANT - THIS RUNS FOR EVERY APPLICATION
 *   Including the Console and My Account. That is normal for this extension
 *   point, but it means a bug here can lock everybody out. This version only
 *   ever returns SUCCESS_COMPLETED, so it cannot block a login.
 * ============================================================================
 */
public class CRDBTrainingCustomPostAuthHandlerV2 extends AbstractPostAuthnHandler {

    private static final Log log = LogFactory.getLog(CRDBTrainingCustomPostAuthHandlerV2.class);

    @Override
    public String getName() {

        return "CRDBTrainingCustomPostAuthHandlerV2";
    }

    @Override
    public PostAuthnHandlerFlowStatus handle(HttpServletRequest request,
                                             HttpServletResponse response,
                                             AuthenticationContext context)
            throws PostAuthenticationFailedException {

        String username = readUsername(context);
        String application = readApplicationName(context);

        /* ---------------- your code goes here ---------------- */
        log.info("=== [Lab03-v2] " + username + " signed in to " + application);

        /*
         * SUCCESS_COMPLETED = "I am done, carry on with the login."
         * The other two values you could return:
         *   INCOMPLETE  - "I sent the browser somewhere, ask me again later"
         *                 (that is how a consent screen is built - see v1)
         *   or throw PostAuthenticationFailedException to fail the login.
         */
        return PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED;
    }

    /*
     * Both helpers below are careful about nulls on purpose. This handler runs
     * for every kind of login, and some of them have no user or no application
     * attached. A NullPointerException here would fail that login.
     */
    private String readUsername(AuthenticationContext context) {

        if (context == null || context.getSequenceConfig() == null) {
            return "<unknown user>";
        }
        AuthenticatedUser user = context.getSequenceConfig().getAuthenticatedUser();
        return user == null ? "<unknown user>" : user.getUserName();
    }

    private String readApplicationName(AuthenticationContext context) {

        if (context == null || context.getSequenceConfig() == null
                || context.getSequenceConfig().getApplicationConfig() == null) {
            return "<unknown application>";
        }
        String name = context.getSequenceConfig().getApplicationConfig().getApplicationName();
        return name == null ? "<unknown application>" : name;
    }

    /*
     * ============================================================================
     * TRY THIS NEXT
     *   1. Log context.getTenantDomain() as well.
     *   2. Refuse the login for one specific user by throwing
     *      new PostAuthenticationFailedException("Blocked", "Not allowed here").
     *      Test it with a throwaway user, NOT with admin - you would lock
     *      yourself out of the Console until you removed the handler.
     * ============================================================================
     */
}
