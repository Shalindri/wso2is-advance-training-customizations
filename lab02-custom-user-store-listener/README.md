# Lab 02 (v2) — Custom User Store Listener

Original tutorial: [`docs/`](docs/)

## What you are building

One line in the server log every time somebody logs in.

## How it works

A **listener** sits right next to the user store, so it sees user operations as they happen:
logins, user creation, attribute changes. You override the method for the operation you care
about and IS calls it.

**This is the only lab that needs no configuration at all.** Copy the jar in, restart, done.

## How it differs from Lab 01

| | Lab 01 (event handler) | Lab 02 (listener) |
|---|---|---|
| Listens to | product-level *events* | *user store operations* |
| Level | higher | lower — closer to the database |
| Can it stop the operation? | not really | **yes** — `doPre...` methods can refuse |

## Build and install

```bash
mvn -f lab02-custom-user-store-listener/pom.xml clean install

cp lab02-custom-user-store-listener/target/lab02-custom-user-store-listener-v2-2.0.0.jar \
   $IS_HOME/repository/components/dropins/
```

Restart the server. No `deployment.toml` change.

## Try it

Do anything that needs a login — sign in to the Console, or:

```bash
curl -sk -u admin:<admin-password> "https://localhost:9443/scim2/Users?count=1" -o /dev/null
```

```
=== [Lab02-v2] admin logged in successfully.
```

Try a wrong password and you get the other line:

```
=== [Lab02-v2] admin typed the wrong password.
```

## The one rule to remember

Every listener method returns a `boolean`, and it does **not** mean "did it work". It means
**"should IS keep running the other listeners"**. Return `true` unless you really mean to
stop the chain — returning `false` silently switches off listeners the product itself
depends on.

## Try this next — refusing an operation

At the bottom of `CRDBTrainingCustomUserStoreListenerV2.java` there is a commented-out
`doPreAddUser` method. Uncomment it, rebuild, reinstall, restart, then try to create a user
called `ab` in the Console. It is refused with your message.

Two things that block is teaching:

* A `doPre...` method that **throws** stops the operation from happening at all.
* Throw `UserStoreClientException`, not plain `UserStoreException`. Both stop the operation,
  but the plain one makes IS report a *server* error (`HTTP 500`, "Error in adding the user")
  and your message never reaches the user. `UserStoreClientException` gives a `HTTP 400` with
  your text — which is what you want, because the caller did make a mistake.

## Verified

Built and run on WSO2 IS 7.3.0 (update level 7), JDK 21. Bundle `Active`; success and
failure log lines both observed against the live server.
