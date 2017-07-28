/**
 * Black Duck Hub Plugin for SonarQube
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.blackducksoftware.integration.hub.sonar;

import org.apache.commons.lang3.StringUtils;
import org.sonar.api.config.Settings;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.log.IntLogger;

public class HubSonarUtils {

    public static final String SONAR_PROJECT_NAME_KEY = "sonar.projectName";
    public static final String SONAR_PROJECT_VERSION_KEY = "sonar.projectVersion";

    public static RestConnection getRestConnection(final IntLogger logger, final HubServerConfig hubServerConfig) throws EncryptionException {
        return hubServerConfig.createCredentialsRestConnection(logger);
    }

    public static HubServerConfig getHubServerConfig(final Settings settings) {
        final HubServerConfigBuilder configBuilder = new HubServerConfigBuilder();
        if (settings != null) {
            configBuilder.setHubUrl(getAndTrimProp(settings, HubPropertyConstants.HUB_URL));
            configBuilder.setUsername(getAndTrimProp(settings, HubPropertyConstants.HUB_USERNAME));
            configBuilder.setPassword(getAndTrimProp(settings, HubPropertyConstants.HUB_PASSWORD));
            configBuilder.setTimeout(getAndTrimProp(settings, HubPropertyConstants.HUB_TIMEOUT));
            configBuilder.setAutoImportHttpsCertificates(Boolean.parseBoolean(getAndTrimProp(settings, HubPropertyConstants.HUB_IMPORT_SSL_CERT)));

            configBuilder.setProxyHost(getAndTrimProp(settings, HubPropertyConstants.HUB_PROXY_HOST));
            configBuilder.setProxyPort(getAndTrimProp(settings, HubPropertyConstants.HUB_PROXY_PORT));
            configBuilder.setProxyUsername(getAndTrimProp(settings, HubPropertyConstants.HUB_PROXY_USERNAME));
            configBuilder.setProxyPassword(getAndTrimProp(settings, HubPropertyConstants.HUB_PROXY_PASSWORD));
        }
        return configBuilder.build();
    }

    public static String getAndTrimProp(final Settings settings, final String key) {
        final String value = settings.getString(key);
        return StringUtils.isEmpty(value) ? null : value.trim();
    }

}
