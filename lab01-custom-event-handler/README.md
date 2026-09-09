# Lab 01 (v2) — Custom Event Handler

Original tutorial: [`docs/`](docs/)

## What you are building

When someone creates a new user, your code:

1. prints a line in the server log, and
2. sets that user's **country** attribute to `Sri Lanka`.

Setting a default attribute at sign-up is one of the most common small customizations there
is. Same pattern for a default language, a default group, or sending a welcome email.

## How it works

WSO2 IS announces things that happen — it calls them **events**. You say which events you
care about in `deployment.toml`, and IS calls your `handleEvent()` method. You never call it
yourself.

One file has the logic, one file is boilerplate:

| File | Why it exists |
|---|---|
| `CRDBTrainingCustomEventHandlerV2.java` | your code |
| `internal/...ServiceComponent.java` | tells IS the class exists |

## Build and install

```bash
mvn -f lab01-custom-event-handler/pom.xml clean install

cp lab01-custom-event-handler/target/lab01-custom-event-handler-v2-2.0.0.jar \
   $IS_HOME/repository/components/dropins/
```

Add to `$IS_HOME/repository/conf/deployment.toml`:

```toml
[[event_handler]]
name = "crdbTrainingCustomEventHandlerV2"
subscriptions = ["POST_ADD_USER"]
```

Restart the server.

> `name` must be **exactly** what `getName()` returns in the Java class. If it does not
> match, you get no error and no log line — your code is just never called.

## Try it

Console → **User Management → Users → Add User**. Or from a terminal:

```bash
curl -sk -u admin:<admin-password> -X POST https://localhost:9443/scim2/Users \
  -H "Content-Type: application/scim+json" \
  -d '{"schemas":["urn:ietf:params:scim:schemas:core:2.0:User"],
       "userName":"lab01user","password":"Str0ng!Passphrase#42"}'
```

**1. The log line** (`$IS_HOME/repository/logs/wso2carbon.log`):

```
=== [Lab01-v2] A new user was created: lab01user
=== [Lab01-v2] Set country='Sri Lanka' on lab01user
```

**2. The attribute.** Open the user in the Console — the **Country** field says `Sri Lanka`.
Or check over the API:

```bash
curl -sk -u admin:<admin-password> \
  "https://localhost:9443/scim2/Users?filter=userName+eq+lab01user" | grep -o '"country":"[^"]*"'
```

```
"country":"Sri Lanka"
```

## Things worth understanding

**Where the user store comes from.** To set an attribute you need the user store the user was
just written to. You do not have to go looking for it — the event hands it to you:

```java
UserStoreManager userStoreManager = (UserStoreManager) event.getEventProperties()
        .get(IdentityEventConstants.EventProperty.USER_STORE_MANAGER);
```

**Why the `try / catch` swallows the error.** If `handleEvent()` throws, IS abandons the whole
"create user" operation. Failing somebody's sign-up because a default attribute could not be
set is a bad trade, so we log the problem and carry on.

**Keep it quick.** This runs while the caller waits. A slow handler makes every user creation
slow.

## Try this next

1. Change `DEFAULT_COUNTRY`, rebuild, reinstall, restart.
2. Add `"PRE_ADD_USER"` to `subscriptions` and log `event.getEventName()` — you will see two
   events arrive for one user, one before the user is saved and one after.
3. Set a second attribute, for example `http://wso2.org/claims/local` = `en_US`.

## Verified

Built and run on WSO2 IS 7.3.0 (update level 7), JDK 21. Bundle `Active`; both log lines
appear and the `country` attribute is confirmed set on the created user.
