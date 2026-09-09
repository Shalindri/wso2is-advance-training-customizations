# Introduction

OAuth 2.0 authorization servers provide support for four main grant
types according to the OAuth 2.0 specification. It also has the
flexibility to support any custom grant types. To implement a custom
grant type, you need to write a handler and a validator for your grant
type.

#### Grant Type Handler

Specifies how the validation must be done and how the token should be
issued. This can be done in two ways. Either you can implement the
“AuthorizationGrantHandler” interface or you can extend the
“AbstractAuthorizationGrantHandler” class.

#### Grant Type Validator

Verifies and validates the token request and checks whether all the
required parameters are sent with the request. This can be implemented
by extending the “AbstarctValidator” class.

# Scenario

In this exercise, we are implementing a simple custom grant type called
“Mobile Grant”. You will pass a mobile number to the OAuth2 “token”
endpoint in exchange for an access token.

# Write the Custom Grant Handler

# Follow the steps given below.

1.  Create a maven project and in the source folder (a folder named
    *Java*), create a package (org.wso2.carbon.identity.custom.grant)
    for the new grant type. Then create two java classes for the
    validator and the handler.\
    \
    <img
    src="images/image3.png"
    style="width:4.06771in;height:1.7731in" />

2.  In the handler class extend the AbstractAuthorizationGrantHandler
    class.\
    \
    Then override the validateGrant method and implement the custom
    logic as follows:

<table style="width:92%;">
<colgroup>
<col style="width: 92%" />
</colgroup>
<thead>
<tr>
<th><p>public class MobileGrant extends
AbstractAuthorizationGrantHandler {</p>
<p>private static Log log = LogFactory.getLog(MobileGrant.class);</p>
<p>public static final String MOBILE_GRANT_PARAM = "mobileNumber";</p>
<p>@Override</p>
<p>public boolean validateGrant(OAuthTokenReqMessageContext</p>
<p>oAuthTokenReqMessageContext) throws IdentityOAuth2Exception {</p>
<p>log.info("Mobile Grant handler is hit");</p>
<p>boolean authStatus = false;</p>
<p>// extract request parameters</p>
<p>RequestParameter[] parameters = oAuthTokenReqMessageContext</p>
<p>.getOauth2AccessTokenReqDTO().getRequestParameters();</p>
<p>String mobileNumber = null;</p>
<p>// find out mobile number</p>
<p>for (RequestParameter parameter : parameters) {</p>
<p>if (MOBILE_GRANT_PARAM.equals(parameter.getKey())) {</p>
<p>if (parameter.getValue() != null &amp;&amp;
parameter.getValue().length &gt; 0) {</p>
<p>mobileNumber = parameter.getValue()[0];</p>
<p>}</p>
<p>}</p>
<p>}</p>
<p>if (mobileNumber != null) {</p>
<p>//validate mobile number</p>
<p>authStatus = isValidMobileNumber(mobileNumber);</p>
<p>if (authStatus) {</p>
<p>// if valid set authorized mobile number as grant user</p>
<p>String tenantAwareUsername = MultitenantUtils</p>
<p>.getTenantAwareUsername(mobileNumber);</p>
<p>/*</p>
<p>Please use AuthenticatedUser</p>
<p>.createFederateAuthenticatedUserFromSubjectIdentifier()</p>
<p>if a federated user is involved with this custom grant.</p>
<p>*/</p>
<p>AuthenticatedUser mobileUser = AuthenticatedUser</p>
<p>.createLocalAuthenticatedUserFromSubjectIdentifier(</p>
<p>tenantAwareUsername);</p>
<p>// Set the federated IdP name if a federated user is involved</p>
<p>// with this custom grant.</p>
<p>//
mobileUser.setFederatedIdPName(FrameworkConstants.LOCAL_IDP_NAME);</p>
<p>oAuthTokenReqMessageContext.setAuthorizedUser(mobileUser);</p>
<p>oAuthTokenReqMessageContext.setScope(</p>
<p>oAuthTokenReqMessageContext.getOauth2AccessTokenReqDTO()</p>
<p>.getScope());</p>
<p>} else {</p>
<p>ResponseHeader responseHeader = new ResponseHeader();</p>
<p>responseHeader.setKey("SampleHeader-999");</p>
<p>responseHeader.setValue("Provided Mobile Number is Invalid.");</p>
<p>oAuthTokenReqMessageContext.addProperty("RESPONSE_HEADERS",</p>
<p>new ResponseHeader[]{responseHeader});</p>
<p>}</p>
<p>}</p>
<p>return authStatus;</p>
<p>}</p>
<p>@Override</p>
<p>public OAuth2AccessTokenRespDTO issue(OAuthTokenReqMessageContext
tokReqMsgCtx) throws IdentityOAuth2Exception {</p>
<p>OAuth2AccessTokenRespDTO tokenRespDTO = new
OAuth2AccessTokenRespDTO();</p>
<p>tokenRespDTO.setExpiresIn(tokReqMsgCtx.getAccessTokenIssuedTime() +
10000);</p>
<p>tokenRespDTO.setAccessToken(UUID.randomUUID().toString());</p>
<p>tokenRespDTO.setRefreshToken(UUID.randomUUID().toString());</p>
<p>tokenRespDTO.setTokenType("mobile");</p>
<p>return tokenRespDTO;</p>
<p>}</p>
<p>public boolean authorizeAccessDelegation(OAuthTokenReqMessageContext
tokReqMsgCtx)</p>
<p>throws IdentityOAuth2Exception {</p>
<p>// if we need to just ignore the end user's extended verification</p>
<p>return true;</p>
<p>}</p>
<p>public boolean validateScope(OAuthTokenReqMessageContext
tokReqMsgCtx)</p>
<p>throws IdentityOAuth2Exception {</p>
<p>// if we need to just ignore the scope verification</p>
<p>return true;</p>
<p>}</p>
<p>/**</p>
<p>* You need to implement how to validate the mobile number</p>
<p>*</p>
<p>* @param mobileNumber Mobile number of the user.</p>
<p>* @return true if the mobile number is valid, otherwise false.</p>
<p>*/</p>
<p>private boolean isValidMobileNumber(String mobileNumber) {</p>
<p>// Regular expression to match 10 digits, with optional country
code</p>
<p>String pattern = "^(\\+\\d{1,3})?\\d{10}$";</p>
<p>// Create a Pattern object</p>
<p>Pattern r = Pattern.compile(pattern);</p>
<p>// Create Matcher object</p>
<p>Matcher m = r.matcher(mobileNumber);</p>
<p>// Check if the pattern matches</p>
<p>return m.matches();</p>
<p>}</p>
<p>@Override</p>
<p>public boolean isOfTypeApplicationUser() throws
IdentityOAuth2Exception {</p>
<p>return true;</p>
<p>}</p>
<p>}</p></th>
</tr>
</thead>
<tbody>
</tbody>
</table>

3.  In the validator class extend the AbstarctValidator class.\
    In the constructor, mention the mandatory parameters that should be
    in the request body.

<table style="width:92%;">
<colgroup>
<col style="width: 92%" />
</colgroup>
<thead>
<tr>
<th><p>public class CRDBTrainingCustomMobileGrantValidator extends AbstractValidator {</p>
<p>public CRDBTrainingCustomMobileGrantValidator() {</p>
<p>requiredParams.add(CRDBTrainingCustomMobileGrantHandler.MOBILE_NUMBER);</p>
<p>}</p>
<p>}</p></th>
</tr>
</thead>
<tbody>
</tbody>
</table>

# Prerequisites

1.  A [<u>WSO2 Identity Server</u>](https://wso2.com/identity-server/)
    setup.

- Please verify that all the [<u>system
  requirements</u>](https://is.docs.wso2.com/en/latest/deploy/environment-compatibility/)
  are satisfied.

2.  An Application registered in Identity Server.

3.  An application deployed in Tomcat server.

> Ex:
> [<u>pickup-dispatch</u>](https://github.com/wso2/samples-is/releases/download/v4.6.3/pickup-dispatch.war)
> application

# Set up

You can use the sample custom grant handler implementation from
[<u>here</u>](https://github.com/wso2/samples-is/tree/v4.6.0/oauth2/custom-grant).

1.  Navigate to the \<SAMPLE_IS\>/oauth2/custom-grant directory and run
    the following command:

| mvn clean install |
|-------------------|

2.  Copy the generated jar file in the target folder into
    \<IS_HOME\>/repository/components/lib/ folder.

3.  To register the custom grant type, configure the
    \<IS_HOME\>/repository/conf/deployment.toml file by adding a new
    entry, as shown below.

<table style="width:92%;">
<colgroup>
<col style="width: 92%" />
</colgroup>
<thead>
<tr>
<th style="text-align: left;"><p>[[oauth.custom_grant_type]]</p>
<p>name="mobile"</p>
<p>grant_handler="org.wso2.sample.identity.oauth2.grant.mobile.MobileGrant"</p>
<p>grant_validator="org.wso2.sample.identity.oauth2.grant.mobile.CRDBTrainingCustomMobileGrantValidator"</p>
<p>[oauth.custom_grant_type.properties]</p>
<p>IdTokenAllowed=true</p>
<p>PublicClientAllowed=true</p></th>
</tr>
</thead>
<tbody>
</tbody>
</table>

4.  Restart the server.

5.  View your created application tab and goto the Protocol tab.

6.  Select the custom grant type **mobile** from the **Allowed Grant
    Types** list and click **Update**.

> <img
> src="images/image4.png"
> style="width:6.5in;height:3.69444in" />

7.  Create a user and add a valid value for the **Mobile** field.

\
=

# Try it out

Follow the steps given below.

1.  Use the following request for mobile grant token generation.

<table style="width:92%;">
<colgroup>
<col style="width: 92%" />
</colgroup>
<thead>
<tr>
<th><blockquote>
<p>curl --user &lt;CLIENT_KEY&gt;:&lt;CLIENT_SECRET&gt; -k -d
"grant_type=mobile&amp;mobileNumber=&lt;MOBILE_NUMBER&gt;" -H
"Content-Type: application/x-www-form-urlencoded"
https://localhost:9443/oauth2/token</p>
</blockquote></th>
</tr>
</thead>
<tbody>
</tbody>
</table>

2.  You should get a response as below.

> <img
> src="images/image5.png"
> style="width:4.0625in;height:1.01563in" />
