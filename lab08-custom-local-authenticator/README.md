# Lab 08 (v2) — Custom Local Authenticator

Original tutorial: [`docs/`](docs/)

## What you are building

Your own login step. It shows the standard login page, then checks the username and password
itself and logs a line.

## Why you would do this

This is the slot where you put **your own way of deciding who somebody is**: call a legacy
authentication API, check a smart card, require a specific group. Here it just asks the user
store, so you can see the shape of the thing without another system in the way. One line in
`processAuthenticationResponse()` is marked as the spot where your own check would go.

## The important idea: your code is called twice

This is what confuses people about authenticators.

| | What arrives | `canHandle()` | IS calls |
|---|---|---|---|
| **Visit 1** | browser, no credentials yet | `false` | `initiateAuthenticationRequest()` → you send the browser to the login page and stop |
| **Visit 2** | the login page posts username + password | `true` | `processAuthenticationResponse()` → you check them |

The two visits are tied together by the `sessionDataKey`, which is what
`getContextIdentifier()` returns.

**The classic bug** is getting `canHandle()` wrong. Say `true` too early and IS skips the
login page, then fails because there is nothing to check.

## Build and install

```bash
mvn -f lab08-custom-local-authenticator/pom.xml clean install

cp lab08-custom-local-authenticator/target/lab08-custom-local-authenticator-v2-2.0.0.jar \
   $IS_HOME/repository/components/dropins/
```

Add to `deployment.toml`:

```toml
[authentication.authenticator.crdb_training_custom_authenticator_v2]
name = "CRDBTrainingCustomAuthenticatorV2"
enable = true
```

Restart.

> `name` must be **exactly** what `getName()` returns in the Java class.

Check IS picked it up:

```bash
curl -sk -u admin:<admin-password> https://localhost:9443/api/server/v1/authenticators \
  | grep -o '"name":"CRDBTrainingCustomAuthenticatorV2"'
```

## Configure the application

Console → your application → **Login Flow**:

1. **Remove** the existing *Username & Password* option.
2. Add **CRDB Training Login (v2)** as the only option in step 1.
3. **Update**.

> Leaving *Username & Password* in the same step is the mistake the original tutorial warns
> about — your authenticator then does not appear on the login page.

## Try it

Open the application and log in with a normal username and password.

```
=== [Lab08-v2] Sending the user to the login page.
=== [Lab08-v2] Login OK for v2user
```

A wrong password gives *"Invalid username or password."* and the login page comes back.

**A check you can do without logging in at all** — this proves visit 1 works:

```bash
curl -sk -D - -o /dev/null \
  "https://localhost:9443/oauth2/authorize?response_type=code&client_id=<client-id>&redirect_uri=<callback>&scope=openid" \
  | grep -i ^location
```

The redirect must end with `&authenticators=CRDBTrainingCustomAuthenticatorV2` — that part is
built by your `initiateAuthenticationRequest()`.

## Things worth understanding

**Register under `ApplicationAuthenticator`**, the general type — not
`LocalApplicationAuthenticator`. IS collects them all under the general type and then works
out which are local.

**`SESSION_DATA_KEY` lives on `FrameworkConstants`**, not on the `FrameworkConstants.RequestParams`
inner class where you would expect it.

**`retryAuthenticationEnabled()` returns `true`** so a wrong password shows the login page
again instead of blowing up the whole login.

**The failure message is deliberately vague** — the same text for an unknown user and a wrong
password, so the login page cannot be used to find out which usernames exist.

## Try this next

1. After a successful check, also require the user to have a `country` attribute (the one lab
   01 sets) and fail the login if it is missing.
2. Temporarily replace the `authenticate(...)` call with `boolean ok = true;` to prove to
   yourself that this really is the decision point — then put it back.

## Verified

Built and run on WSO2 IS 7.3.0 (update level 7), JDK 21. The authenticator is listed by the
API as `type: LOCAL, isEnabled: true`, was accepted into a real application's login flow, and
`initiateAuthenticationRequest()` was confirmed running — the authorize endpoint redirected
with `authenticators=CRDBTrainingCustomAuthenticatorV2` and the log line appeared. The final
password check in the browser was not exercised — that is the "Try it" step above.
