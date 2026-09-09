# Lab 04 (v2) — Pre-Update Password Action

Original tutorial: [`docs/`](docs/)

## This lab is different from all the others

Labs 1, 2, 3, 5, 6, 7 and 8 are jars you copy **inside** the Identity Server.
This one is **not**. It is an ordinary little web service that runs on its own, and IS calls
it over HTTP whenever somebody changes a password.

That means:

* no OSGi, no `dropins`, nothing to restart when you change the rule
* it does not even have to be written in Java

## What you are building

A service that refuses any new password containing the word `password`.

## How IS talks to it

IS sends a `POST` with JSON. You reply with JSON. Three possible replies:

| Reply | HTTP | Meaning |
|---|---|---|
| `{"actionStatus":"SUCCESS"}` | 200 | allow the password |
| `{"actionStatus":"FAILED", "failureReason":..., "failureDescription":...}` | **200** | reject it — the user sees your `failureDescription` |
| `{"actionStatus":"ERROR", "errorMessage":...}` | 500 | your service is broken |

**The thing people get wrong:** a rejection is still an HTTP **200**. Reply with a `400` and
IS decides your service is broken and shows the user a generic server error instead of your
message. Returning the map from the controller gives you a 200 automatically, so just do
that.

## Build and run

```bash
mvn -f lab04-pre-update-password-action/pom.xml clean install
java -jar lab04-pre-update-password-action/target/lab04-pre-update-password-action-v2-2.0.0.jar
```

It listens on **port 8091**. Leave it running in its own terminal.

### Check your rule without any of the rest

This is the fastest loop while you are learning — no server, no restart:

```bash
mvn -f lab04-pre-update-password-action/pom.xml test
```

```
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
```

## Register it in IS

Console → **Extensions → Actions → Pre Update Password**, and fill in:

| Field | Value |
|---|---|
| Action Name | `CRDBTrainingCustomPasswordCheckV2` |
| Endpoint URL | `http://localhost:8091/actions/validate-password` |
| Authentication | None (fine for a lab) |
| **Password Sharing Type** | **Plain Text** |

Then click **Save** and switch the action **on**.

> **Password Sharing Type must be Plain Text.** The other option sends a scrambled copy of
> the password, and you cannot look inside a scrambled password to see if it contains a word.
> The service notices this case, logs a warning and allows the change rather than blocking
> every password.

Same thing over the API, if you prefer:

```bash
curl -sk -u admin:<admin-password> -X POST \
  https://localhost:9443/api/server/v1/actions/preUpdatePassword \
  -H "Content-Type: application/json" -d '{
   "name":"CRDBTrainingCustomPasswordCheckV2",
   "endpoint":{"uri":"http://localhost:8091/actions/validate-password",
               "authentication":{"type":"NONE","properties":{}}},
   "passwordSharing":{"format":"PLAIN_TEXT"}}'
```

It is created switched **off**; activate it with the id you get back:

```bash
curl -sk -u admin:<admin-password> -X POST \
  https://localhost:9443/api/server/v1/actions/preUpdatePassword/<id>/activate
```

> **Switch the action off when you finish this lab.** While it is on, *every* password change
> in the server calls your service — and if the service is not running, password changes fail.
> `.../actions/preUpdatePassword/<id>/deactivate`.

## Try it

In the Console, edit a user and set their password to `MyPassword2026!`:

```
Your password must not contain the word 'password'.
```

Set it to `Str0ng!Passphrase#42` and it saves normally.

Your service's terminal shows:

```
Password rejected: it contains 'password'.
Password accepted.
```

## Try this next

1. Change `BANNED_WORD` to your company name. No restart of IS needed — just restart the
   little service.
2. Add a minimum length check.
3. Log who is changing the password. The username arrives at
   `event → user → claims` as a list of `{"uri":..., "value":...}` — but only if you tick the
   username attribute when registering the action.

## Verified

Built and run on WSO2 IS 7.3.0 (update level 7), JDK 21. 4/4 unit tests pass. Registered
against the live server (IS recorded it as contract `v2`), activated, then a password
containing "password" was blocked with `HTTP 400` carrying the message above, and a clean
password was accepted with `HTTP 200`.
