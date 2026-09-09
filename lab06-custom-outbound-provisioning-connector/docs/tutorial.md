# Introduction

A provisioning connector enables you to provision users from WSO2
Identity Server to external systems. For instance, you can set up a
Salesforce connector as a provisioning connector for your identity
provider to provision users to your Salesforce application from WSO2
Identity Server.

Outbound provisioning refers to the process of provisioning users,groups
and user-group assignments to external systems. To set this up, you need
to configure one or more outbound provisioning connectors with a
particular identity provider. This can be triggered by any of the
following events:

- a user is provisioned in WSO2 Identity Server over SCIM2 API.

- an administrator onboards a user from the WSO2 Identity Server
  Console(SCIM2 APIs).

- a user self signs up from a WSO2 Identity Server login page.

- a user is JIT provisioned in WSO2 Identity Server.

Please refer to the documentation [<u>Custom Outbound Connector - WSO2
Identity
Server</u>](https://is.docs.wso2.com/en/latest/guides/users/outbound-provisioning/outbound-connectors/custom-outbound-connectors/)
to write a custom outbound provisioning connector for Identity Server.

To demonstrate this we will implement a custom provisioning connector

1.  The connector will be developed as an OSGi Service.

2.  Once the connector is developed and deployed in the WSO2 Identity
    Server, it will listen to the user operations and provision the
    users to the configured external system.\[2\]

3.  If the connector is successfully deployed, it will be listed under
    the Outbound Provisioning Connectors of the Identity Provider
    configuration.

4.  The connector can be configured from the UI that is generated.

# Write the connector

1.  create a maven project with the following file structure. (The
    package and class naming can be changed according to your needs.

<table style="width:92%;">
<colgroup>
<col style="width: 92%" />
</colgroup>
<thead>
<tr>
<th><p>├── pom.xml</p>
<p>├── src</p>
<p>│ ├── main</p>
<p>│ │ ├── java</p>
<p>│ │ │ └── org.wso2.carbon.identity.provisioning.connector</p>
<p>│ │ │ │ └──sample</p>
<p>│ │ │ │ │ ├── internal</p>
<p>│ │ │ │ │ │ └── SampleConnectorServiceComponent.java</p>
<p>│ │ │ │ │ ├── SampleConnectorConstants.java</p>
<p>│ │ │ │ │ ├── SampleProvisioningConnectorConfig.java</p>
<p>│ │ │ │ │ ├── SampleProvisioningConnectorFactory.java</p>
<p>│ │ │ │ │ └── SampleProvisioningConnector.java</p></th>
</tr>
</thead>
<tbody>
</tbody>
</table>

2.  SampleConnectorServiceComponent.java

> This class is responsible for registering the connector as an OSGi
> service. It is located inside the internal package, which is not
> exported when the component is bundled.
>
> To identify the annotated class as a Service Component, use the
> @Component annotation at the beginning of the class definition. Inside
> the class, implement the activate method of the Service Component by
> adding the @Activate annotation.

<table style="width:92%;">
<colgroup>
<col style="width: 92%" />
</colgroup>
<thead>
<tr>
<th><p>@activate</p>
<p>protected void activate(ComponentContext context) {</p>
<p>try {</p>
<p>SampleProvisioningConnectorFactory provisioningConnectorFactory = new
SampleProvisioningConnectorFactory();</p>
<p>context.getBundleContext().registerService(AbstractProvisioningConnectorFactory.class.getName(),provisioningConnectorFactory,
null);</p>
<p>} catch (Throwable e) {</p>
<p>log.error("Error while activating Sample Identity Provisioning
Connector ", e);</p>
<p>}</p>
<p>}</p></th>
</tr>
</thead>
<tbody>
</tbody>
</table>

3.  SampleConnectorConstants.java

This class holds all the constants required for our provisioning
connector.

4.  SampleProvisioningConnectorFactory.java

> The connector factory is responsible for building the connector and is
> created by extending the AbstractProvisioningConnectorFactory abstract
> class. It defines the connector type and overrides the buildConnector,
> getConnectorType, and getConfigurationProperties methods of the
> superclass.
>
> To communicate with the external system, the connector requires
> configuration information such as usernames, passwords, and client
> IDs, which should be taken as user inputs. To facilitate this, a user
> interface (UI) is necessary. The provisioning framework is designed to
> be flexible such that defining the configuration properties will
> generate the connector’s UI.

<table style="width:93%;">
<colgroup>
<col style="width: 92%" />
</colgroup>
<thead>
<tr>
<th><p>@Override</p>
<p>public List&lt;Property&gt; getConfigurationProperties() {</p>
<p>Property clientId = new Property();</p>
<p>clientId.setName(SampleConnectorConstants.SAMPLE_CLIENT_ID);</p>
<p>clientId.setDisplayName("Client ID");</p>
<p>clientId.setDisplayOrder(1);</p>
<p>clientId.setRequired(true);</p>
<p>Property clientSecret = new Property();</p>
<p>clientSecret.setName(SampleConnectorConstants</p>
<p>.SAMPLE_CLIENT_SECRET);</p>
<p>clientSecret.setDisplayName("Client Secret");</p>
<p>clientSecret.setConfidential(true);</p>
<p>clientSecret.setDisplayOrder(2);</p>
<p>clientSecret.setRequired(true);</p>
<p>Property username = new Property();</p>
<p>username.setName(SampleConnectorConstants.SAMPLE_USERNAME);</p>
<p>username.setDisplayName("Username");</p>
<p>username.setDescription("Username for the external system");</p>
<p>username.setDisplayOrder(3);</p>
<p>username.setRequired(true);</p>
<p>Property password = new Property();</p>
<p>password.setName(SampleConnectorConstants.SAMPLE_PASSWORD);</p>
<p>password.setDisplayName("Password");</p>
<p>password.setDisplayOrder(4);</p>
<p>password.setRequired(true);</p>
<p>List&lt;Property&gt; properties = new ArrayList&lt;&gt;();</p>
<p>properties.add(clientId);</p>
<p>properties.add(clientSecret);</p>
<p>properties.add(username);</p>
<p>properties.add(password);</p>
<p>return properties;</p>
<p>}</p></th>
</tr>
</thead>
<tbody>
</tbody>
</table>

# 

5.  SampleProvisioningConnectorConfig.java

> Once the properties are configured through the UI, the values are
> stored in an instance of the SampleProvisioningConnectorConfig class.
> This instance is created during the initialization of the connector
> and can be accessed by the connector implementation to retrieve the
> configuration values for communication with the external system.

6.  SampleProvisioningConnector.java

> The SampleProvisioningConnector class inherits the behavior of the
> AbstractOutboundProvisioningConnector class. To customize the behavior
> of the connector, override the init method, which is called when the
> connector is initialized, and the provision method, which is called
> when a user is provisioned.
>
> The provision method is triggered for create, delete and update
> operations of the user.
>
> In the provided code segment, the logic for user creation, deletion,
> and update in the external system is implemented as separate methods,
> and the appropriate method is called upon receiving a provisioning
> operation. Upon successfully creating a user in the external system,
> an ID is returned for the provisioned user, which is then used to
> refer to the user for future operations like updating and deleting.

<table style="width:92%;">
<colgroup>
<col style="width: 92%" />
</colgroup>
<thead>
<tr>
<th><p>@Override</p>
<p>public void init(Property[] provisioningProperties) throws
IdentityProvisioningException {</p>
<p>Properties configs = new Properties();</p>
<p>if (provisioningProperties != null &amp;&amp;
provisioningProperties.length &gt; 0) {</p>
<p>for (Property property : provisioningProperties) {</p>
<p>configs.put(property.getName(), property.getValue());</p>
<p>if
(IdentityProvisioningConstants.JIT_PROVISIONING_ENABLED.equals(property.getName()))
{</p>
<p>if
(SampleConnectorConstants.PROPERTY_VALUE_TRUE.equals(property.getValue()))
{</p>
<p>jitProvisioningEnabled = true;</p>
<p>}</p>
<p>}</p>
<p>}</p>
<p>}</p>
<p>configHolder = new SampleProvisioningConnectorConfig(configs);</p>
<p>}</p>
<p>@Override</p>
<p>public ProvisionedIdentifier provision(ProvisioningEntity
provisioningEntity)</p>
<p>throws IdentityProvisioningException {</p>
<p>String provisionedId = null;</p>
<p>if (provisioningEntity != null) {</p>
<p>if (provisioningEntity.isJitProvisioning() &amp;&amp;
!isJitProvisioningEnabled()) {</p>
<p>log.debug("JIT provisioning disabled for Office365 connector");</p>
<p>return null;</p>
<p>}</p>
<p>if (ProvisioningEntityType.USER ==
provisioningEntity.getEntityType()) {</p>
<p>if (ProvisioningOperation.DELETE ==
provisioningEntity.getOperation()) {</p>
<p>deleteUser(provisioningEntity);</p>
<p>} else if (ProvisioningOperation.POST ==
provisioningEntity.getOperation()) {</p>
<p>provisionedId = createUser(provisioningEntity);</p>
<p>} else if (ProvisioningOperation.PUT ==
provisioningEntity.getOperation()) {</p>
<p>updateUser(provisioningEntity);</p>
<p>} else {</p>
<p>log.warn("Unsupported provisioning operation " +
provisioningEntity.getOperation() +</p>
<p>" for entity type " + provisioningEntity.getEntityType());</p>
<p>}</p>
<p>} else {</p>
<p>log.warn("Unsupported provisioning entity type " +
provisioningEntity.getEntityType());</p>
<p>}</p>
<p>}</p>
<p>// Creates a provisioned identifier for the provisioned user.</p>
<p>ProvisionedIdentifier identifier = new ProvisionedIdentifier();</p>
<p>identifier.setIdentifier(provisionedId);</p>
<p>return identifier;</p>
<p>}</p></th>
</tr>
</thead>
<tbody>
</tbody>
</table>

> The connector can be also written to listen to role-related events. If
> so, you need to have the validation,

| ProvisioningEntityType.GROUP == provisioningEntity.getEntityType() |
|--------------------------------------------------------------------|

# Prerequisites

1.  A [<u>Wso2 Identity Server</u>](https://wso2.com/identity-server/)
    setup.

- Please verify that all the [<u>system
  requirements</u>](https://is.docs.wso2.com/en/latest/deploy/environment-compatibility/)
  are satisfied.

# Set up

## Patch the jar file

You can find the complete implementation for the sample outbound
provisioning connector from
[<u>here</u>](https://github.com/wso2/samples-is/tree/master/sample-outbound-connector).

1.  Navigate to the identity-outbound-provisioning-sample directory and
    run the following command.

2.  Navigate to the sample-outbound-connector/ directory.

| cd sample-outbound-connector |
|------------------------------|

3.  Run the following command:

| mvn clean install |
|-------------------|

4.  Copy the generated jar file in the target folder into
    \<IS_HOME\>/repository/components/dropins/ folder.

5.  Restart the server.

## Configure the custom connector via the console

### **Step 1: Set up the Target WSO2 Identity Server**

First, you need a second new WSO2 Identity Server instance(unizip the
downloaded product zip file for the second instance) to provision users
to.

1.  **Start the second WSO2 Identity Server with a port offset:** This
    ensures it runs on a different port than your primary IS instance,
    preventing conflicts.

sh wso2server.sh -DportOffset=1

> This will typically make the console accessible at
> https://localhost:9444/console and the SCIM2 endpoint at
> https://localhost:9444/scim2/Users.

### **Step 2: Configure the Custom Connector in the Primary WSO2 Identity Server**

1.  Add a new Connection.

> Ex:- provisioningIdP

2.  Go to the Outbound Provisioning tab and create a new SCIM2
    Provisioning connector. Provide the following details of your
    **second WSO2 Identity Server instance**:

- **Username:** admin

- **Password:** admin (or the actual admin credentials of your second
  IS)

- **User Endpoint:** https://localhost:9444/scim2/Users

3.  Enable Password Provisioning.

4.  After adding make sure to click the **Enable** on the listed
    Provisioning connector.

5.  Add the provisioning connector to the resident identity provider.

    1.  Add a new Provisioner using **Login & Registration \>
        Provisioning Settings \> Outbound Provisioning Configuration \>
        New Provisioner.**

    2.  Select the created IDP and SCIM2 Provisioning connector.

# Try It:

Now, let's test if user provisioning works as expected.

- **Create a user on the primary Identity Server:**

  - Log in to the console of your primary Identity Server
    (https://localhost:9443/console).

  - Go to **User Management** \> **Users**.

  - Click **Add User** and create a new user account.

- **Verify the provisioned user on the second Identity Server:**

  - Log in to the console of your second Identity Server
    (https://localhost:9444/console).

  - Navigate to **User Management** \> **Users**.

  - You should now see the user you created on the primary Identity
    Server automatically provisioned to this instance.
