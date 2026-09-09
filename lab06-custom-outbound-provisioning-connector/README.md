# Lab 06 (v2) — Custom Outbound Provisioning Connector

Original tutorial: [`docs/`](docs/)

## What you are building

Every time a user is created, changed or deleted in WSO2 IS, print a line saying what
happened and to whom.

## Why you would do this

"Outbound provisioning" means copying users **out** to another system — a CRM, a payroll app,
an internal database. A real connector would make an HTTP call at the marked spot in the
code. Printing a log line instead keeps this lab on one machine with nothing else to install,
and the shape of the code is identical to a real connector.

## Three classes

| File | Job |
|---|---|
| `...ProvisioningConnectorV2.java` | does the work — **your code goes here** |
| `...ProvisioningConnectorFactoryV2.java` | builds connectors, and describes the Console form |
| `internal/...ServiceComponent.java` | boilerplate |

**There is no HTML anywhere in this lab.** The settings form you fill in on the Console is
generated from the list of `Property` objects in the Factory. You never write a UI.

## Build and install

```bash
mvn -f lab06-custom-outbound-provisioning-connector/pom.xml clean install

cp lab06-custom-outbound-provisioning-connector/target/lab06-custom-outbound-provisioning-connector-v2-2.0.0.jar \
   $IS_HOME/repository/components/dropins/
```

Restart. No `deployment.toml` change.

Check it registered:

```bash
curl -sk -u admin:<admin-password> \
  https://localhost:9443/api/server/v1/identity-providers/meta/outbound-provisioning-connectors
```

`crdbTrainingCustomConnectorV2` should be in the list.

## Configure it — two steps

**1. Create a connection that uses your connector.**
Console → **Connections → New Connection → Custom Connector**, name it `Lab06V2IdP`, then on
the **Outbound Provisioning** tab enable `crdbTrainingCustomConnectorV2` and set
**Target system name** to `acme-crm`.

**2. Tell IS to use it for ordinary user creation.**
Console → **Login & Registration → Outbound Provisioning Configuration → New Provisioner** →
pick `Lab06V2IdP` and your connector.

Without step 2 nothing happens — step 1 only defines the connector, step 2 switches it on.

## Try it

Create a user in the Console. In the log:

```
=== [Lab06-v2] Connector ready. Target system = acme-crm
=== [Lab06-v2] CREATE lab06user in acme-crm (their id there: 4e6a6c5a-7e64-...)
```

Edit the user's profile and you get `UPDATE`. Delete them and you get `DELETE`.

## Things worth understanding

**Only a CREATE returns an id.** IS remembers the id you return so a later update or delete
knows which record in the other system to touch. Returning a new id on an update would throw
that link away, so `UPDATE` and `DELETE` return `null`.

**`getClaimDialectUri()` is not optional.** It tells IS which naming scheme to translate the
user's attributes into before handing them to you. The default is "translate nothing", so if
you leave this method out your connector receives a user with **no attributes at all** — which
is a confusing thing to debug.

## Try this next

1. Log the user's email as well:
   ```java
   Map<String, String> attrs = getSingleValuedClaims(entity.getAttributes());
   log.info(attrs.get("http://wso2.org/claims/emailaddress"));
   ```
   Never log anything with `password` in the name.
2. Add a second setting in the Factory and read it in `init()`.
3. Replace the log line with a real HTTP call using `java.net.http.HttpClient`.

## Verified

Built and run on WSO2 IS 7.3.0 (update level 7), JDK 21. The connector appears in the
metadata API with its form field rendered from `getConfigurationProperties()`; a connection
was created and attached, and creating a user produced the `CREATE` line with the configured
target name.
