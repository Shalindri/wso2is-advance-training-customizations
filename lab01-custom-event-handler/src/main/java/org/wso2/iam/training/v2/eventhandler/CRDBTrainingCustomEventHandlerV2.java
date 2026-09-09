package org.wso2.iam.training.v2.eventhandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.event.IdentityEventConstants;
import org.wso2.carbon.identity.event.IdentityEventException;
import org.wso2.carbon.identity.event.event.Event;
import org.wso2.carbon.identity.event.handler.AbstractEventHandler;
import org.wso2.carbon.user.api.UserStoreManager;

/*
 * ============================================================================
 * LAB 01 (v2) - CUSTOM EVENT HANDLER
 * ============================================================================
 *
 * WHAT IT DOES
 *   Every time a new user is created, this class:
 *     1. prints one line to the server log, and
 *     2. gives that user a default "country" attribute.
 *
 * WHY YOU WOULD DO THIS
 *   Filling in a default attribute at sign-up is one of the most common small
 *   customisations there is. Same idea for a default language, a default role,
 *   or sending a welcome email.
 *
 * HOW IT GETS CALLED
 *   WSO2 IS announces things that happen ("events"). You subscribe to the ones
 *   you care about in deployment.toml, and IS calls handleEvent() below.
 *   You never call it yourself.
 *
 * THE ONLY METHOD THAT MATTERS
 *   handleEvent()  <- your code goes here.
 * ======================================================================git add -A======
 */
public class CRDBTrainingCustomEventHandlerV2 extends AbstractEventHandler {

    private static final Log log = LogFactory.getLog(CRDBTrainingCustomEventHandlerV2.class);

    /* This name links the class to the deployment.toml entry. They MUST match. */
    public static final String HANDLER_NAME = "crdbTrainingCustomEventHandlerV2";

    /* The attribute we are going to set, and the value to put in it. */
    private static final String COUNTRY_CLAIM = "http://wso2.org/claims/country";
    private static final String DEFAULT_COUNTRY = "Tanzania, United Republic of";

    @Override
    public String getName() {

        return HANDLER_NAME;
    }

    @Override
    public void handleEvent(Event event) throws IdentityEventException {

        /* We only subscribed to POST_ADD_USER, but check anyway - it costs nothing. */
        if (!IdentityEventConstants.Event.POST_ADD_USER.equals(event.getEventName())) {
            return;
        }

        /* Read the username out of the event. */
        String username = (String) event.getEventProperties()
                .get(IdentityEventConstants.EventProperty.USER_NAME);

        /* ---------------- STEP 1: print a log line ---------------- */
        log.info("=== [Lab01-v2] A new user was created: " + username);

        /*
         * ---------------- STEP 2: set a default attribute ----------------
         * The event hands us the user store the user was just written to, so we
         * can write straight back to it - no extra setup needed.
         */
        UserStoreManager userStoreManager = (UserStoreManager) event.getEventProperties()
                .get(IdentityEventConstants.EventProperty.USER_STORE_MANAGER);

        if (userStoreManager == null) {
            log.warn("=== [Lab01-v2] No user store in the event, skipping the attribute.");
            return;
        }

        try {
            userStoreManager.setUserClaimValue(username, COUNTRY_CLAIM, DEFAULT_COUNTRY, null);
            log.info("=== [Lab01-v2] Set country='" + DEFAULT_COUNTRY + "' on " + username);
        } catch (Exception e) {
            /*
             * Log and swallow. If we let this exception out, IS would abandon the
             * whole "create user" operation - and failing a sign-up because a
             * default attribute could not be set is not a good trade.
             */
            log.error("=== [Lab01-v2] Could not set country on " + username, e);
        }
    }

    /*
     * ============================================================================
     * TRY THIS NEXT
     *   1. Change DEFAULT_COUNTRY and rebuild.
     *   2. Add "PRE_ADD_USER" to subscriptions in deployment.toml, then log
     *      event.getEventName() to see both events arrive.
     *   3. Set a second attribute, e.g. http://wso2.org/claims/local = "en_US".
     * ============================================================================
     */
}
