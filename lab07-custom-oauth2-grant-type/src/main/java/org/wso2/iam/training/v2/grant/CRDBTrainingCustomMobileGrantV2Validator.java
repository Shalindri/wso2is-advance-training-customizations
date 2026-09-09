package org.wso2.iam.training.v2.grant;

import org.wso2.carbon.identity.oauth2.token.handlers.grant.AbstractValidator;

/*
 * ============================================================================
 * THE VALIDATOR - checks the request has the fields we need.
 * ============================================================================
 *
 * If a field is missing, IS refuses the request before our grant class runs and
 * replies with a helpful message like:
 *     {"error":"invalid_request","error_description":"Missing parameters: mobileNumber"}
 *
 * WATCH OUT
 *   Older WSO2 examples do this in the constructor:
 *       requiredParams.add("mobileNumber");
 *   That does not compile on IS 7.3.0. The correct way is to override
 *   configureParams() and call addRequiredParam(), as below.
 * ============================================================================
 */
public class CRDBTrainingCustomMobileGrantV2Validator extends AbstractValidator {

    @Override
    protected void configureParams() {

        addRequiredParam(CRDBTrainingCustomMobileGrantV2.MOBILE_PARAM);
        addRequiredParam(CRDBTrainingCustomMobileGrantV2.PASSWORD_PARAM);
    }
}
