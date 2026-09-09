package org.wso2.iam.training.v2.passwordaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.Map;

/*
 * ============================================================================
 * LAB 04 (v2) - PRE-UPDATE PASSWORD ACTION
 * ============================================================================
 *
 * WHAT IT DOES
 *   Refuses any new password that contains the word "password".
 *
 * WHY THIS LAB IS DIFFERENT FROM THE OTHERS
 *   Labs 1,2,3,5,6,7,8 are jars you drop inside the Identity Server.
 *   This one is NOT. It is an ordinary web service that runs on its own, and
 *   IS calls it over HTTP whenever somebody changes a password. That means:
 *     - no OSGi, no dropins, no server restart to change the rule
 *     - it does not even have to be written in Java
 *
 * HOW IS TALKS TO US
 *   IS sends a POST with JSON. We reply with JSON. Three possible replies:
 *
 *     {"actionStatus":"SUCCESS"}                      -> allow the password
 *     {"actionStatus":"FAILED", "failureReason":...}   -> reject it
 *     {"actionStatus":"ERROR",  "errorMessage":...}    -> our service broke
 *
 * THE ONE THING PEOPLE GET WRONG
 *   A rejection ("FAILED") is still an HTTP 200 response. Returning HTTP 400
 *   makes IS think the service is broken, and the user then sees a generic
 *   server error instead of your message. Spring returns 200 by default, so
 *   just returning the map below is correct.
 * ============================================================================
 */
@RestController
@RequestMapping("/actions")
public class CRDBTrainingCustomPasswordActionV2Controller {

    private static final Logger log =
            LoggerFactory.getLogger(CRDBTrainingCustomPasswordActionV2Controller.class);

    /* The word we do not allow inside a password. */
    private static final String BANNED_WORD = "password";

    @PostMapping("/validate-password")
    public Map<String, String> validatePassword(@RequestBody Map<String, Object> body) {

        /*
         * The JSON arrives as nested maps. The password lives at:
         *   event -> user -> updatingCredential -> value
         * "get()" below walks down that path one step at a time and returns
         * null if any step is missing, so a surprise payload cannot crash us.
         */
        Map<String, Object> credential = descend(body, "event", "user", "updatingCredential");

        if (credential == null || credential.get("value") == null) {
            log.warn("No password found in the request from IS.");
            return Map.of("actionStatus", "ERROR",
                          "errorMessage", "No password in the request.");
        }

        /*
         * "format" tells us whether IS sent the real password or a scrambled
         * (hashed) copy. We can only look inside a real one. Pick "Plain Text"
         * for the Password Sharing Type when you register this action.
         */
        String format = String.valueOf(credential.get("format"));
        if (!"PLAIN_TEXT".equals(format)) {
            log.warn("IS sent the password as {} - cannot look inside it. Allowing.", format);
            return Map.of("actionStatus", "SUCCESS");
        }

        String password = String.valueOf(credential.get("value"));

        /* ---------------- your rule goes here ---------------- */
        if (password.toLowerCase(Locale.ROOT).contains(BANNED_WORD)) {
            log.info("Password rejected: it contains '{}'.", BANNED_WORD);
            return Map.of("actionStatus", "FAILED",
                          "failureReason", "Weak password",
                          "failureDescription",
                          "Your password must not contain the word '" + BANNED_WORD + "'.");
        }

        log.info("Password accepted.");
        return Map.of("actionStatus", "SUCCESS");
    }

    /* Walks down nested JSON maps. Returns null instead of throwing. */
    @SuppressWarnings("unchecked")
    private Map<String, Object> descend(Map<String, Object> from, String... keys) {

        Map<String, Object> current = from;
        for (String key : keys) {
            if (current == null) {
                return null;
            }
            Object next = current.get(key);
            current = (next instanceof Map) ? (Map<String, Object>) next : null;
        }
        return current;
    }

    /*
     * ============================================================================
     * TRY THIS NEXT
     *   1. Change BANNED_WORD to your company name.
     *   2. Add a minimum length check.
     *   3. Log who is changing the password. The username arrives at
     *      event -> user -> claims, as a list of {"uri":..., "value":...} - and
     *      only if you tick the username attribute when registering the action.
     * ============================================================================
     */
}
