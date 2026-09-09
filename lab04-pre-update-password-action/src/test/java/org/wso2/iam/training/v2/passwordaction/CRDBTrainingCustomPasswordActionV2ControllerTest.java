package org.wso2.iam.training.v2.passwordaction;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/*
 * Checks the rule without starting anything. Run with:
 *   mvn test
 * This is the fastest way to see whether your rule does what you think.
 */
class CRDBTrainingCustomPasswordActionV2ControllerTest {

    private final CRDBTrainingCustomPasswordActionV2Controller controller =
            new CRDBTrainingCustomPasswordActionV2Controller();

    /* Builds the same JSON shape IS sends, so we can call the method directly. */
    private Map<String, Object> request(String password, String format) {

        return Map.of("event", Map.of("user", Map.of("updatingCredential",
                Map.of("value", password, "format", format))));
    }

    @Test
    void allowsAGoodPassword() {

        assertEquals("SUCCESS",
                controller.validatePassword(request("Str0ng!Passphrase#42", "PLAIN_TEXT"))
                        .get("actionStatus"));
    }

    @Test
    void rejectsThePasswordContainingTheBannedWord() {

        Map<String, String> result = controller.validatePassword(request("MyPassword1!", "PLAIN_TEXT"));
        assertEquals("FAILED", result.get("actionStatus"));
        assertEquals("Weak password", result.get("failureReason"));
    }

    @Test
    void allowsWhenIsSentAHashInstead() {

        assertEquals("SUCCESS",
                controller.validatePassword(request("aGFzaGVk", "HASH")).get("actionStatus"));
    }

    @Test
    void reportsAnErrorWhenThereIsNoPassword() {

        assertEquals("ERROR", controller.validatePassword(Map.of()).get("actionStatus"));
    }
}
