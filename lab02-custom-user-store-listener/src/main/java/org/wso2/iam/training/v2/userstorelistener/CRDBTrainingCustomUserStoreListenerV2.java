package org.wso2.iam.training.v2.userstorelistener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.common.AbstractUserOperationEventListener;

/*
 * ============================================================================
 * LAB 02 (v2) - CUSTOM USER STORE LISTENER
 * ============================================================================
 *
 * WHAT IT DOES
 *   Prints one line to the server log every time somebody logs in.
 *
 * WHY YOU WOULD DO THIS
 *   A listener sits right next to the user store, so it is the place to put
 *   "whenever a user does X, also do Y" - login auditing, updating a
 *   last-login attribute, blocking an operation.
 *
 * HOW IT IS DIFFERENT FROM LAB 01
 *   Lab 01's event handler listens to product-level EVENTS.
 *   This listens to USER STORE OPERATIONS - one level lower, so you also get
 *   "before" hooks that can stop the operation happening at all.
 *
 * HOW IT GETS CALLED
 *   You override a doXxx method and IS calls it. There is no configuration
 *   for this lab at all - deploying the jar is enough.
 *
 * ONE RULE TO REMEMBER
 *   Every method returns a boolean, and it does NOT mean "did it work".
 *   It means "should IS keep running the other listeners". Always return true
 *   unless you really know you want to stop the chain.
 * ============================================================================
 */
public class CRDBTrainingCustomUserStoreListenerV2 extends AbstractUserOperationEventListener {

    private static final Log log = LogFactory.getLog(CRDBTrainingCustomUserStoreListenerV2.class);

    /*
     * Where we sit in the queue of listeners. Anything above about 9000 runs
     * comfortably last. The exact number rarely matters for a lab.
     */
    @Override
    public int getExecutionOrderId() {

        return 9884;
    }

    /* Called by IS just after it has checked somebody's password. */
    @Override
    public boolean doPostAuthenticate(String userName, boolean authenticated,
                                      UserStoreManager userStoreManager) throws UserStoreException {

        /* ---------------- your code goes here ---------------- */
        if (authenticated) {
            log.info("=== [Lab02-v2] " + userName + " logged in successfully.");
        } else {
            log.info("=== [Lab02-v2] " + userName + " typed the wrong password.");
        }

        return true;   /* keep the other listeners running */
    }

    /*
     * ============================================================================
     * TRY THIS NEXT - blocking an operation
     *
     * Uncomment the method below, rebuild, redeploy, restart. Then try to create
     * a user called "ab" in the Console: it will be refused with your message.
     *
     * The lesson: a doPre... method that throws stops the operation. Use
     * UserStoreClientException (not plain UserStoreException) so the caller gets
     * a "you made a mistake" 400 instead of a "the server broke" 500.
     * ============================================================================
     */
    // @Override
    // public boolean doPreAddUser(String userName, Object credential, String[] roleList,
    //                             java.util.Map<String, String> claims, String profile,
    //                             UserStoreManager userStoreManager) throws UserStoreException {
    //
    //     if (userName != null && userName.length() < 4) {
    //         log.warn("=== [Lab02-v2] Refusing to create '" + userName + "' - name is too short.");
    //         throw new org.wso2.carbon.user.core.UserStoreClientException(
    //                 "Username must be at least 4 characters long.");
    //     }
    //     return true;
    // }
}
