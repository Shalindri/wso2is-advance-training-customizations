# How to configure

Please refer to the documentation from
[<u>here</u>](https://is.docs.wso2.com/en/latest/references/extend/authentication/write-a-custom-local-authenticator/)
to write a custom local authenticator for Identity Server.

# Troubleshooting the Sample Authenticator

1.  SampleAuthenticator doesn’t show up in the actual login page.

> <u>Solution</u>:
>
> During Configuring the Application to use the SampleAuthenticator in
> the **Login Flow**, make sure to remove all the other authentication
> options and add only the SampleAuthenticator.
>
> <img
> src="images/image4.png"
> style="width:3.32813in;height:3.29712in" />
>
> 1\. Remove the existing BasicAuthenticator
>
> <img
> src="images/image3.png"
> style="width:3.48724in;height:1.81424in" />
>
> 2\. Add the new SampleAuthenticator

2.  When the correct username and password are added, authentication
    fails.

> <u>Solution</u>:
>
> Add the mobile number configured as the username and the usual
> password for the account.
