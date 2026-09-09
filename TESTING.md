# Testing record — v2

Environment: **WSO2 IS 7.3.0**, update level 7, running on **JDK 21.0.11**.
Built with Maven 3.6.3 / JDK 21. All eight labs were built, installed into the pack and run.

## Bundle state

From the OSGi console (`lb | grep lab0`):

```
82|Active     |    4|lab01-custom-event-handler-v2 (2.0.0)|2.0.0
83|Active     |    4|lab02-custom-user-store-listener-v2 (2.0.0)|2.0.0
84|Active     |    4|lab03-custom-post-authentication-handler-v2 (2.0.0)|2.0.0
85|Active     |    4|lab05-custom-jdbc-user-store-manager-v2 (2.0.0)|2.0.0
86|Active     |    4|lab06-custom-outbound-provisioning-connector-v2 (2.0.0)|2.0.0
87|Active     |    4|lab07-custom-oauth2-grant-type-v2 (2.0.0)|2.0.0
88|Active     |    4|lab08-custom-local-authenticator-v2 (2.0.0)|2.0.0
```

No lab-related errors or warnings at startup.

## Results

| Lab | Check | Result |
|---|---|---|
| 01 | log line on user creation | `A new user was created: v2user` |
| 01 | **the attribute was really set** | `"country":"Sri Lanka"` on the created user |
| 02 | log line on successful login | `admin logged in successfully.` |
| 03 | handler in the generated `identity.xml` | `<EventListener ... orderId="899" enable="true">` |
| 04 | unit tests | 4/4 pass |
| 04 | registered + activated in IS | contract `v2`, status `ACTIVE` |
| 04 | password containing "password" | blocked, `HTTP 400`, *"Your password must not contain the word 'password'."* |
| 04 | clean password | allowed, `HTTP 200` |
| 05 | type offered by the Console API | `CRDBTrainingCustomUserStoreManagerV2` present |
| 05 | right password | `Result for v2storeuser: SUCCESS` |
| 05 | wrong password | `Result for v2storeuser: FAIL` |
| 06 | connector registered | `crdbTrainingCustomConnectorV2` present |
| 06 | user created | `CREATE v2user in acme-crm (their id there: ...)` |
| 07 | right number + right password | token issued |
| 07 | right number + wrong password | `400 invalid_grant`, `Wrong password for v2user` |
| 07 | unknown number | `400 invalid_grant`, `Nobody has that mobile number.` |
| 07 | `password` field missing | `400` *"Missing parameters: password"* (from the Validator) |
| 07 | token is real | `/oauth2/introspect` → `active: true`, `username: v2user@carbon.super` |
| 08 | authenticator listed | `type: LOCAL`, `isEnabled: true`, display "CRDB Training Login (v2)" |
| 08 | accepted into an app's login flow | yes |
| 08 | first half of the login runs | redirect carried `authenticators=CRDBTrainingCustomAuthenticatorV2`, log line `Sending the user to the login page.` |

## Not verified

The final browser step of **lab 03** (seeing the line appear after a Console login) and
**lab 08** (typing a username and password on the login page). Both need credentials entered
into the login form. Everything up to that point is verified, and each README marks the step.

## State left in the IS pack

* v1's jars were **removed** from `dropins` and replaced with the seven v2 jars.
* `deployment.toml` now carries the v2 blocks; the v1 blocks were removed. The original file
  is still backed up as `deployment.toml.bak-<timestamp>`.
* To go back to v1: run `../iam-training-7.3.0/deploy.sh $IS_HOME`, delete the `*-v2-*.jar`
  files from `dropins`, and swap the config blocks for
  `../iam-training-7.3.0/deployment.toml.sample`.

Test data left behind, so the labs can be re-run:

| Thing | Note |
|---|---|
| users `v2user`, `v2storeuser` | `v2user` is in PRIMARY and has a mobile number, used by lab 07 |
| user store `LAB05` | repointed at the **v2** class, using `repository/database/LAB05_CUSTOM_DB` |
| connection `Lab06V2IdP` | attached as a resident outbound provisioner, so user creation logs `[Lab06-v2]` lines |
| application `Lab07MobileGrantApp` | grants `mobile-v2` + `client_credentials` + `authorization_code`; login flow restored to the default |
| action `CRDBTrainingCustomPasswordCheckV2` | left **INACTIVE** on purpose |

Two of those are worth knowing about:

* **`Lab06V2IdP` is attached**, which is why `[Lab06-v2]` lines appear whenever any user is
  created. Harmless — it only logs. Detach it under
  **Login & Registration → Outbound Provisioning Configuration**.
* **The lab 04 action is INACTIVE on purpose.** While active, every password change calls
  `http://localhost:8091`, and password changes fail if that service is not running.

> The lab 05 README tells you to create a store called `LAB05V2` with its own database. What
> is actually deployed right now is the existing `LAB05` store, repointed at the v2 class and
> reusing the database from the v1 run. Either is fine; the README describes the clean path.
