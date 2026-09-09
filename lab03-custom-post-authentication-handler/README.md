# Lab 03 (v2) — Custom Post Authentication Handler

Original tutorial: [`docs/`](docs/)

> The tutorial in `docs/` builds a consent page, which needs a JSP and a suspend/resume
> dance. That is a lot of moving parts for a first look. This version does the simple half —
> run code after a successful login — and the full consent version is in the v1 project
> (`../../iam-training-7.3.0/lab03-custom-post-authentication-handler`).

## What you are building

Right after somebody's login succeeds, print one line saying who logged in and which
application they logged in to.

## How it works

This is the "the login worked, now do something" slot. The login is not finished until every
post-authentication handler has said it is happy — which is what makes this the place to add
an extra check that can still refuse entry.

You return one of:

| Return | Meaning |
|---|---|
| `SUCCESS_COMPLETED` | I am done, carry on with the login |
| `INCOMPLETE` | I sent the browser somewhere, ask me again later *(this is how a consent screen works)* |
| *throw* `PostAuthenticationFailedException` | fail the login |

This lab only ever returns `SUCCESS_COMPLETED`, so it cannot break anything.

## ⚠️ This runs for every application

Including the **Console** and **My Account**. That is normal for this extension point, but it
means a bug here can lock everybody out. Keep the off switch in mind before you start
changing it:

```toml
enable = false     # then restart
```

## Build and install

```bash
mvn -f lab03-custom-post-authentication-handler/pom.xml clean install

cp lab03-custom-post-authentication-handler/target/lab03-custom-post-authentication-handler-v2-2.0.0.jar \
   $IS_HOME/repository/components/dropins/
```

Add to `deployment.toml`:

```toml
[[event_listener]]
id = "crdb_training_custom_post_auth_v2"
type = "org.wso2.carbon.identity.core.handler.AbstractIdentityHandler"
name = "org.wso2.iam.training.v2.postauthn.CRDBTrainingCustomPostAuthHandlerV2"
order = 899
enable = true
```

Restart.

> Note `name` here is the **full class name** — unlike lab 01, where it was a short label.
> Copying the jar in is not enough: without this block the handler is never given a place in
> the queue and never runs.

## Try it

Log in to the Console at `https://localhost:9443/console`.

```
=== [Lab03-v2] admin signed in to Console
```

Log in to a different application and you will see that application's name instead.

> **Why an API call does not trigger it.** `curl -u admin:...` uses HTTP Basic
> authentication, which does not go through the login flow, so no post-authentication
> handler runs. You need an actual browser login.

## Things worth understanding

Both helper methods in the class check for `null` carefully. That is deliberate: this handler
runs for every kind of login, and some have no user or no application attached. A
`NullPointerException` here would fail that login — and for a handler that runs everywhere,
that means nobody can sign in.

## Try this next

1. Also log `context.getTenantDomain()`.
2. Refuse the login for one user by throwing
   `new PostAuthenticationFailedException("Blocked", "Not allowed here")`. **Test with a
   throwaway user, never with admin** — you would lock yourself out of the Console until you
   removed the handler.

## Verified

Built and run on WSO2 IS 7.3.0 (update level 7), JDK 21. Bundle `Active`, and the
`<EventListener ... orderId="899">` entry confirmed in the generated
`repository/conf/identity/identity.xml`. The browser login itself was not exercised — that is
the "Try it" step above.
