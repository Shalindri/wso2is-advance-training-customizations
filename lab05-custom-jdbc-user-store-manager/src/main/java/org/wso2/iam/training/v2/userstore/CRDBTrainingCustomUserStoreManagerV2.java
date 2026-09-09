package org.wso2.iam.training.v2.userstore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.claim.ClaimManager;
import org.wso2.carbon.user.core.common.AuthenticationResult;
import org.wso2.carbon.user.core.jdbc.UniqueIDJDBCUserStoreManager;
import org.wso2.carbon.user.core.profile.ProfileConfigurationManager;

import java.util.Map;

/*
 * ============================================================================
 * LAB 05 (v2) - CUSTOM JDBC USER STORE MANAGER
 * ============================================================================
 *
 * WHAT IT DOES
 *   Behaves exactly like the built-in database user store, except it prints a
 *   log line every time it checks somebody's password.
 *
 * WHY YOU WOULD DO THIS
 *   When a customer's user table is not shaped the way WSO2 expects, or their
 *   passwords are scrambled in their own way, you take the product's user store
 *   and replace just the one method that differs. You do not write a user store
 *   from scratch - you inherit one and override a little.
 *
 * HOW IT WORKS
 *   "extends UniqueIDJDBCUserStoreManager" means: be the normal database user
 *   store. Every method we do not write ourselves keeps working as before.
 *   We override one method, log, and then call super... which runs the original.
 *
 * WHY "UniqueID"
 *   IS 7.x identifies users by a hidden id rather than by username. Always
 *   extend the UniqueID... class; the older JDBCUserStoreManager looks like it
 *   works and then fails in places you will not think to test.
 *
 * WHAT ELSE THIS LAB NEEDS
 *   A database, and a user store created in the Console that points at it.
 *   See README.md - the database part is one command.
 * ============================================================================
 */
public class CRDBTrainingCustomUserStoreManagerV2 extends UniqueIDJDBCUserStoreManager {

    private static final Log log = LogFactory.getLog(CRDBTrainingCustomUserStoreManagerV2.class);

    /*
     * Two constructors, and you need BOTH. Do not delete either.
     *
     *   - the empty one lets IS discover the class and list it in the Console
     *   - the long one is what IS actually calls to build a working store
     *
     * Leave the long one out and creating the user store fails with a
     * "NoSuchMethodException" naming this exact list of arguments.
     */
    public CRDBTrainingCustomUserStoreManagerV2() {

        super();
    }

    public CRDBTrainingCustomUserStoreManagerV2(RealmConfiguration realmConfig,
                                                Map<String, Object> properties,
                                                ClaimManager claimManager,
                                                ProfileConfigurationManager profileManager,
                                                UserRealm realm,
                                                Integer tenantId) throws UserStoreException {

        super(realmConfig, properties, claimManager, profileManager, realm, tenantId);
        log.info("=== [Lab05-v2] Custom user store started for domain '"
                + realmConfig.getUserStoreProperty("DomainName") + "'");
    }

    /* Called every time this user store is asked to check a username + password. */
    @Override
    protected AuthenticationResult doAuthenticateWithUserName(String userName, Object credential)
            throws UserStoreException {

        /* ---------------- your code goes here ---------------- */
        log.info("=== [Lab05-v2] Checking the password for " + userName);

        /* Let the product do the actual password check. */
        AuthenticationResult result = super.doAuthenticateWithUserName(userName, credential);

        log.info("=== [Lab05-v2] Result for " + userName + ": " + result.getAuthenticationStatus());
        return result;
    }

    /*
     * ============================================================================
     * TRY THIS NEXT - your own password scrambling
     *
     * preparePassword() is used BOTH when a password is saved and when one is
     * checked, so overriding it swaps the scrambling for the whole store in one
     * place and the two halves can never disagree.
     *
     * Uncomment, rebuild, restart, then create a NEW user in this store. Old
     * users will no longer be able to log in, because their stored password was
     * scrambled the old way - which is exactly the trap to understand before
     * doing this to a real customer.
     *
     * (Do not ship this particular scrambling. It is here to show the hook.)
     * ============================================================================
     */
    // @Override
    // protected String preparePassword(Object password, String saltValue) throws UserStoreException {
    //
    //     org.wso2.carbon.utils.Secret secret;
    //     try {
    //         secret = org.wso2.carbon.utils.Secret.getSecret(password);
    //     } catch (org.wso2.carbon.utils.UnsupportedSecretTypeException e) {
    //         throw new UserStoreException("Unsupported credential type.", e);
    //     }
    //     try {
    //         java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
    //         if (saltValue != null) {
    //             digest.update(saltValue.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    //         }
    //         String hash = "v2$" + java.util.Base64.getEncoder().encodeToString(digest.digest(secret.getBytes()));
    //         log.info("=== [Lab05-v2] Scrambled the password my own way.");
    //         return hash;
    //     } catch (java.security.NoSuchAlgorithmException e) {
    //         throw new UserStoreException("SHA-256 missing.", e);
    //     } finally {
    //         secret.clear();   // wipe the real password from memory - always do this
    //     }
    // }
}
