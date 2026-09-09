# Lab 05 (v2) — Custom JDBC User Store Manager

Original tutorial: [`docs/`](docs/)

## What you are building

A user store that behaves exactly like the built-in database one, except it prints a line
every time it checks somebody's password.

## Why you would do this

When a customer's user table is not shaped the way WSO2 expects, or their passwords are
scrambled in their own way, you do **not** write a user store from scratch. You take the
product's user store, inherit from it, and replace only the method that differs:

```java
public class CRDBTrainingCustomUserStoreManagerV2 extends UniqueIDJDBCUserStoreManager
```

Everything you do not write yourself keeps working as before.

> Always extend the **`UniqueID...`** class. IS 7.x identifies users by a hidden id rather
> than by username; the older `JDBCUserStoreManager` looks like it works and then fails in
> places you would not think to test.

## Build and install

```bash
mvn -f lab05-custom-jdbc-user-store-manager/pom.xml clean install

cp lab05-custom-jdbc-user-store-manager/target/lab05-custom-jdbc-user-store-manager-v2-2.0.0.jar \
   $IS_HOME/repository/components/dropins/
```

Add to `deployment.toml`:

```toml
[user_store_mgt]
custom_user_stores = ["org.wso2.iam.training.v2.userstore.CRDBTrainingCustomUserStoreManagerV2"]
```

## Make a database

A user store needs somewhere to keep users. The tutorial uses MySQL; **we use H2**, which is
already inside the IS pack and needs no server and no password of its own — one command:

```bash
cd $IS_HOME
java -cp repository/components/plugins/h2-engine_2.2.224.wso2v2.jar org.h2.tools.RunScript \
  -url "jdbc:h2:./repository/database/LAB05_V2_DB;DB_CLOSE_ON_EXIT=FALSE" \
  -user lab05 -password lab05secret \
  -script dbscripts/h2.sql
```

That creates the 60 tables IS expects. **Run it before you start the server** — an H2 file
can only be open in one program at a time.

Now start the server.

## Create the user store

Console → **User Attributes & Stores → User Stores → New User Store**. Pick
**CRDBTrainingCustomUserStoreManagerV2** from the list, name it `LAB05V2`, and fill in:

| Field | Value |
|---|---|
| Driver name | `org.h2.Driver` |
| Connection URL | `jdbc:h2:./repository/database/LAB05_V2_DB;DB_CLOSE_ON_EXIT=FALSE;LOCK_TIMEOUT=60000` |
| Username | `lab05` |
| Password | `lab05secret` |

> **If `CRDBTrainingCustomUserStoreManagerV2` is not in the list**, the cause is almost always
> in the service component — see "Things worth understanding" below.

## Try it

Create a user in the new store — note the `LAB05V2/` prefix:

```bash
curl -sk -u admin:<admin-password> -X POST https://localhost:9443/scim2/Users \
  -H "Content-Type: application/scim+json" \
  -d '{"schemas":["urn:ietf:params:scim:schemas:core:2.0:User"],
       "userName":"LAB05V2/storeuser","password":"Str0ng!Passphrase#42"}'
```

Then log in as that user, once correctly and once with a wrong password:

```bash
curl -sk -u 'LAB05V2/storeuser:Str0ng!Passphrase#42' https://localhost:9443/scim2/Me -o /dev/null
curl -sk -u 'LAB05V2/storeuser:WrongPassword123'     https://localhost:9443/scim2/Me -o /dev/null
```

```
=== [Lab05-v2] Custom user store started for domain 'LAB05V2'
=== [Lab05-v2] Checking the password for storeuser
=== [Lab05-v2] Result for storeuser: SUCCESS
=== [Lab05-v2] Checking the password for storeuser
=== [Lab05-v2] Result for storeuser: FAIL
```

> Read the **log**, not the HTTP status. A normal user gets `401` from `/scim2/Me` because
> they are not allowed to use that endpoint, even when their password was correct. The
> `SUCCESS` line is the bit that matters.

## Things worth understanding

**Two constructors, and you need both.** Do not delete either:

* the empty one lets IS discover the class and list it in the Console;
* the long six-argument one is what IS calls to build a working store. Leave it out and
  creating the user store fails with a `NoSuchMethodException` naming that exact argument list.

**There are two interfaces called `UserStoreManager`** — one in `...user.api` and one in
`...user.core`. When IS builds the list of user store types for the Console it only looks for
the **`api`** one. Register the wrong one and everything *looks* fine — the jar loads, the log
line prints — but your user store never appears in the Console. The service component
registers both names so it cannot go wrong.

**`custom_user_stores` in `deployment.toml` is also required**, but on its own it is not
enough. It only says "the Console is allowed to offer this class"; the class still has to be
registered correctly as above.

## Try this next — your own password scrambling

At the bottom of `CRDBTrainingCustomUserStoreManagerV2.java` there is a commented-out
`preparePassword()` method. It is used **both** when a password is saved and when one is
checked, so overriding it swaps the scrambling for the whole store in one place and the two
halves can never disagree.

Uncomment it, rebuild, restart, and create a **new** user. Existing users can no longer log
in, because their stored password was scrambled the old way — which is exactly the trap to
understand before doing this on a customer's system.

## Verified

Built and run on WSO2 IS 7.3.0 (update level 7), JDK 21. The type appears in
`GET /api/server/v1/userstores/meta/types`; a store was created against an H2 database, a
user added, and authentication produced `SUCCESS` for the right password and `FAIL` for the
wrong one.
