# Lab 07 (v2) — Custom OAuth 2.0 Grant Type

Original tutorial: [`docs/`](docs/)

## What you are building

A way for an app to get an access token using a **mobile number and a password**, instead of
a username and a password:

```
POST /oauth2/token
grant_type=mobile-v2&mobileNumber=+94771234567&password=....
```

Useful for a phone app where people know their phone number but not their username. The same
pattern covers "log in with employee number" or "log in with NIC".

## What a grant type is

A grant type is a recipe for *"here is some proof of who I am, give me a token"*. OAuth 2.0
ships a few standard recipes (`password`, `client_credentials`, `authorization_code`); this is
a custom one.

## Two classes do the work

| File | Job |
|---|---|
| `CRDBTrainingCustomMobileGrantV2.java` | checks the proof, says who the user is |
| `CRDBTrainingCustomMobileGrantV2Validator.java` | checks the request even has the fields we need |

Plus two boilerplate files under `internal/`.

## You do not write the token

Your job is only to say *"yes, this is user bob"* by calling `setAuthorizedUser()` and
returning `true`. The product then makes a real token for you.

Older examples on the internet override `issue()` and build a token string with
`UUID.randomUUID()`. Do not copy that. It produces something that looks like a token but was
never saved, so it cannot be checked, cannot be revoked, and stops working the moment anything
inspects it.

## Build and install

```bash
mvn -f lab07-custom-oauth2-grant-type/pom.xml clean install

cp lab07-custom-oauth2-grant-type/target/lab07-custom-oauth2-grant-type-v2-2.0.0.jar \
   $IS_HOME/repository/components/dropins/
```

Add to `deployment.toml`:

```toml
[[oauth.custom_grant_type]]
name = "mobile-v2"
grant_handler = "org.wso2.iam.training.v2.grant.CRDBTrainingCustomMobileGrantV2"
grant_validator = "org.wso2.iam.training.v2.grant.CRDBTrainingCustomMobileGrantV2Validator"

[oauth.custom_grant_type.properties]
IdTokenAllowed = true
PublicClientAllowed = true
```

Restart, and watch the startup log for this:

```
Grant type : mobile-v2, is not added as a supported grant type.
```

If you see that, the jar did not load — check it says `Active` (see the main README).

## Configure

1. Console → your application → **Protocol** → tick **mobile-v2** under *Allowed Grant Types*.
2. Console → **User Management → Users →** pick a user → put a phone number in **Mobile**.

## Try it

```bash
curl -sk --user <client-id>:<client-secret> -X POST https://localhost:9443/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=mobile-v2&mobileNumber=%2B94771112222&password=Str0ng%21Passphrase%2342"
```

| What you send | What you get |
|---|---|
| right number + right password | `200` and an `access_token` |
| right number + wrong password | `400 invalid_grant` |
| a number nobody has | `400 invalid_grant` |
| no `password` field | `400 invalid_request` — **`Missing parameters: password`** |

That last one comes from the **Validator**, not your grant class — which is how you know the
validator is wired up.

Log lines:

```
=== [Lab07-v2] Mobile login OK. Issuing a token for v2user
=== [Lab07-v2] Wrong password for v2user
=== [Lab07-v2] Nobody has that mobile number.
```

Check the token is real:

```bash
curl -sk -u admin:<admin-password> -X POST https://localhost:9443/oauth2/introspect \
  -d "token=<access-token>"
```

```json
{"active": true, "username": "v2user@carbon.super", "token_type": "Bearer"}
```

## Things worth understanding

**Why two people sharing a number is refused.** The mobile attribute is not unique by
default. If two users have the same number, picking the first would let one person get a token
for somebody else — so the code refuses instead.

**Why the cast is there.** `getUserList(attribute, value, profile)` only exists on
`org.wso2.carbon.user.core.UserStoreManager`, not on the `...user.api` one the method
officially returns, so the result has to be cast.

**Why the `DataHolder` class exists.** IS creates the grant class itself, from the class name
in `deployment.toml`. Because IS does the creating, it cannot hand you the user management
service the normal way — so the service component parks it in the DataHolder and the grant
class reads it from there.

**Why we ask for a password.** Older versions of this sample took only a phone number, which
proves nothing — anyone who knows your number could get a token as you. This version asks for
the password too.

## Try this next

1. Send a wrong password and watch the log line appear.
2. Only allow the grant if the user has a `country` attribute — the one lab 01 fills in.

## Verified

Built and run on WSO2 IS 7.3.0 (update level 7), JDK 21. All four rows of the table above
reproduced against the live server, and the issued token confirmed `active: true` with the
right username via `/oauth2/introspect`.
