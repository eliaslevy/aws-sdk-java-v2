/*
 * Copyright 2010-2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package software.amazon.awssdk.auth;

import static software.amazon.awssdk.utils.StringUtils.trim;

import java.util.Optional;
import software.amazon.awssdk.AwsSystemSetting;
import software.amazon.awssdk.SdkClientException;
import software.amazon.awssdk.annotation.SdkInternalApi;
import software.amazon.awssdk.utils.StringUtils;
import software.amazon.awssdk.utils.SystemSetting;

/**
 * Loads credentials providers from the {@link AwsSystemSetting#AWS_ACCESS_KEY_ID},
 * {@link AwsSystemSetting#AWS_SECRET_ACCESS_KEY}, and {@link AwsSystemSetting#AWS_SESSION_TOKEN} system settings.
 *
 * This does not load the credentials directly. Instead, the actual mapping of setting to credentials is done by child classes.
 * This allows us to separately load the credentials from system properties and environment variables so that customers can
 * remove one or the other from their credential chain, or build a different chain with these pieces of functionality separated.
 *
 * @see EnvironmentVariableCredentialsProvider
 * @see SystemPropertyCredentialsProvider
 */
@SdkInternalApi
abstract class SystemSettingsCredentialsProvider implements AwsCredentialsProvider {
    @Override
    public AwsCredentials getCredentials() {
        String accessKey = trim(loadSetting(AwsSystemSetting.AWS_ACCESS_KEY_ID).orElse(null));
        String secretKey = trim(loadSetting(AwsSystemSetting.AWS_SECRET_ACCESS_KEY).orElse(null));
        String sessionToken = trim(loadSetting(AwsSystemSetting.AWS_SESSION_TOKEN).orElse(null));

        if (StringUtils.isEmpty(accessKey)) {
            throw new SdkClientException(
                    String.format("Unable to load credentials from system settings. Access key must be specified either via "
                                  + "environment variable (%s) or system property (%s).",
                                  AwsSystemSetting.AWS_ACCESS_KEY_ID.environmentVariable(),
                                  AwsSystemSetting.AWS_ACCESS_KEY_ID.property()));
        }

        if (StringUtils.isEmpty(secretKey)) {
            throw new SdkClientException(
                    String.format("Unable to load credentials from system settings. Secret key must be specified either via "
                                  + "environment variable (%s) or system property (%s).",
                                  AwsSystemSetting.AWS_SECRET_ACCESS_KEY.environmentVariable(),
                                  AwsSystemSetting.AWS_SECRET_ACCESS_KEY.property()));
        }

        return sessionToken == null ? new AwsCredentials(accessKey, secretKey)
                                    : new AwsSessionCredentials(accessKey, secretKey, sessionToken);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    /**
     * Implemented by child classes to load the requested setting.
     */
    protected abstract Optional<String> loadSetting(SystemSetting setting);
}
