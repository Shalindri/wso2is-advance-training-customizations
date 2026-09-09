# Introduction

In the custom user store, you can have any type of schema according to
your wish. Suppose you have a company that already has a user database
and needs to authenticate the user through our WSO2 Identity Server. In
that case, you don’t have to convert your whole database to the WSO2
Identity Server standard schema.

# Sample scenario

Consider a sample scenario where you want to use a custom hashing method
using a 3<sup>rd</sup> party library such as
[<u>Jasypt</u>](http://www.jasypt.org/) and to do this, you need to
override the doAuthentication and preparePassword methods.

# Write a custom userstore

Follow the steps given below.

1.  Create a new Apache Maven project with the help of the IDE that you
    are using.

2.  Create a new class by extending the existing
    UniqueIDJDBCUserStoreManager implementation.

<table style="width:92%;">
<colgroup>
<col style="width: 92%" />
</colgroup>
<thead>
<tr>
<th><p>package org.wso2.custom.user.store.internal;</p>
<p>public class CustomUserStoreManager extends
UniqueIDJDBCUserStoreManager {</p></th>
</tr>
</thead>
<tbody>
</tbody>
</table>

3.  Override the doAuthenticateWithUserName method to write custom
    authentication logic and preparePassword method to hash the
    passwords using Jasypt.

<table style="width:92%;">
<colgroup>
<col style="width: 92%" />
</colgroup>
<thead>
<tr>
<th><p>package org.wso2.custom.user.store;</p>
<p>public class CustomUserStoreManager extends
UniqueIDJDBCUserStoreManager {</p>
<p>private static final Log log =
LogFactory.getLog(CustomUserStoreManager.class);</p>
<p>private static final StrongPasswordEncryptor passwordEncryptor =
new</p>
<p>StrongPasswordEncryptor();</p>
<p>public CustomUserStoreManager() {}</p>
<p>public CustomUserStoreManager(RealmConfiguration realmConfig,</p>
<p>Map&lt;String, Object&gt; properties, ClaimManager claimManager,</p>
<p>ProfileConfigurationManager profileManager, UserRealm realm,</p>
<p>Integer tenantId) throws UserStoreException {</p>
<p>super(realmConfig, properties, claimManager, profileManager, realm,
tenantId);</p>
<p>log.info("CustomUserStoreManager initialized...");</p>
<p>}</p>
<p>@Override</p>
<p>public AuthenticationResult
<strong>doAuthenticateWithUserName</strong>(String userName,</p>
<p>Object credential) throws UserStoreException {</p>
<p><strong>// custom authentication logic.</strong></p>
<p>}</p>
<p>@Override</p>
<p>protected String <strong>preparePassword</strong>(Object password,
String saltValue)</p>
<p>throws UserStoreException {</p>
<p>if (password != null) {</p>
<p>String candidatePassword = String.copyValueOf(((Secret)password)</p>
<p>.getChars());</p>
<p>// ignore saltValue for the time being</p>
<p>log.info("Generating hash value using jasypt...");</p>
<p>return passwordEncryptor.encryptPassword(candidatePassword);</p>
<p>} else {</p>
<p>log.error("Password cannot be null");</p>
<p>throw new UserStoreException("Authentication Failure");</p>
<p>}</p>
<p>}</p>
<p>private AuthenticationResult getAuthenticationResult(String reason)
{</p>
<p>AuthenticationResult authenticationResult = new
AuthenticationResult(</p>
<p>AuthenticationResult.AuthenticationStatus.FAIL);</p>
<p>authenticationResult.setFailureReason(new FailureReason(reason));</p>
<p>return authenticationResult;</p>
<p>}</p>
<p>}</p></th>
</tr>
</thead>
<tbody>
</tbody>
</table>

4.  Register as an OSGi service.

<table style="width:92%;">
<colgroup>
<col style="width: 92%" />
</colgroup>
<thead>
<tr>
<th><p>package org.wso2.custom.user.store.internal;</p>
<p>@Component(name = "custom.jdbc.user.store.mgt.component",</p>
<p>immediate = true)</p>
<p>public class CustomJDBCUserStoreMgtComponent {</p>
<p>private static Log log =
LogFactory.getLog(CustomJDBCUserStoreMgtComponent.class);</p>
<p>private static RealmService realmService;</p>
<p>@Activate</p>
<p>protected void activate(ComponentContext ctxt) {</p>
<p>UserStoreManager customUserStoreManager = new
CustomUserStoreManager();</p>
<p>ctxt.getBundleContext().registerService(UserStoreManager.class.getName(),</p>
<p>customUserStoreManager, null);</p>
<p>log.info("CustomUserStoreManager bundle activated
successfully..");</p>
<p>}</p>
<p>@Deactivate</p>
<p>protected void deactivate(ComponentContext ctxt) {</p>
<p>if (log.isDebugEnabled()) {</p>
<p>log.debug("Custom User Store Manager is deactivated ");</p>
<p>}</p>
<p>}</p>
<p>@Reference(</p>
<p>name = "RealmService",</p>
<p>service = org.wso2.carbon.user.core.service.RealmService.class,</p>
<p>cardinality = ReferenceCardinality.MANDATORY,</p>
<p>policy = ReferencePolicy.DYNAMIC,</p>
<p>unbind = "unsetRealmService")</p>
<p>protected void setRealmService(RealmService rlmService) {</p>
<p>realmService = rlmService;</p>
<p>}</p>
<p>protected void unsetRealmService(RealmService realmService) {</p>
<p>realmService = null;</p>
<p>}</p>
<p>}</p></th>
</tr>
</thead>
<tbody>
</tbody>
</table>

# Prerequisites

1.  A [<u>Wso2 Identity Server</u>](https://wso2.com/identity-server/)
    setup.

- Please verify that all the [<u>system
  requirements</u>](https://is.docs.wso2.com/en/latest/deploy/environment-compatibility/)
  are satisfied.

2.  MySQL driver added to the \<IS_HOME\>/repository/components/lib/
    folder

3.  An Application registered in Identity Server.

4.  An application deployed in Tomcat server.

> Ex:
> [<u>pickup-dispatch</u>](https://github.com/wso2/samples-is/releases/download/v4.6.3/pickup-dispatch.war)
> application

# Set up

We have implemented a sample custom event handler for you. You can
access that from
[<u>here</u>](https://github.com/wso2/samples-is/tree/master/user-mgt/custom-jdbc-user-store-manager).
Follow the below instructions after cloning the
[<u>sample-is</u>](https://github.com/wso2/samples-is) repo into your
local machine.

1.  Navigate to the
    \<SAMPLE_IS\>/user-mgt/custom-jdbc-user-store-manager directory and
    run the following command.

| mvn clean install |
|-------------------|

2.  Copy the generated JAR file in the target folder into the
    \<IS-HOME\>/repository/components/dropins/ directory.

3.  Add the following configuration to the
    \<IS_HOME\>/repository/conf/deployment.toml file to use our custom
    implementation for user store management.

<table style="width:92%;">
<colgroup>
<col style="width: 92%" />
</colgroup>
<thead>
<tr>
<th style="text-align: left;"><p>[user_store_mgt]</p>
<p>custom_user_stores=["<strong>org.wso2.custom.user.store.CustomUserStoreManager</strong>"]</p></th>
</tr>
</thead>
<tbody>
</tbody>
</table>

4.  Navigate to \<IS_HOME\>/bin and start the server by issuing one of
    the following commands based on your operating system:

5.  Create a database called custom_db and run the
    \<IS-HOME\>/dbscripts/mysql.sql file against the database created.

6.  Create a user store with the custom user store manager you created.

7.  Click **User Attributes & Stores** \> **User stores** \> **New User
    Store**.

8.  Select the custom user store manager you just created.

> <img
> src="images/image4.png"
> style="width:6.5in;height:3.69444in" />

9.  Add the data related to custom_db connection details.

10. Click **Finish**.

# Try it out

Follow the steps given below.

1.  Create a user under the CUSTOM userstore created.

2.  You should see the below server logs in your Identity Server
    Console.

> <img
> src="images/image3.png"
> style="width:6.5in;height:0.48611in" />
