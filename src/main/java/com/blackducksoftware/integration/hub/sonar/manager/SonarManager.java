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
package com.blackducksoftware.integration.hub.sonar.manager;

import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.config.Settings;

import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.sonar.HubPropertyConstants;

public class SonarManager {
    private final SensorContext context;
    private final Settings settings;

    public SonarManager(final Settings settings) {
        this.context = null;
        this.settings = settings;
    }

    public SonarManager(final SensorContext context) {
        this.context = context;
        this.settings = context.settings();
    }

    public SensorContext getSensorContext() {
        return context;
    }

    public HubServerConfig getHubServerConfigFromSettings() {
        final HubServerConfigBuilder configBuilder = new HubServerConfigBuilder();
        if (settings != null) {
            configBuilder.setHubUrl(getValue(HubPropertyConstants.HUB_URL));
            configBuilder.setUsername(getValue(HubPropertyConstants.HUB_USERNAME));
            configBuilder.setPassword(getValue(HubPropertyConstants.HUB_PASSWORD));
            configBuilder.setTimeout(getValue(HubPropertyConstants.HUB_TIMEOUT));
            configBuilder.setAlwaysTrustServerCertificate(Boolean.parseBoolean(getValue(HubPropertyConstants.HUB_TRUST_SSL_CERT)));

            configBuilder.setProxyHost(getValue(HubPropertyConstants.HUB_PROXY_HOST));
            configBuilder.setProxyPort(getValue(HubPropertyConstants.HUB_PROXY_PORT));
            configBuilder.setProxyUsername(getValue(HubPropertyConstants.HUB_PROXY_USERNAME));
            configBuilder.setProxyPassword(getValue(HubPropertyConstants.HUB_PROXY_PASSWORD));
        }
        return configBuilder.build();
    }

    public String[] getGlobalInclusionPatterns() {
        return getValues(HubPropertyConstants.HUB_BINARY_INCLUSION_PATTERN_OVERRIDE);
    }

    public String[] getGlobalExclusionPatterns() {
        return getValues(HubPropertyConstants.HUB_BINARY_EXCLUSION_PATTERN_OVERRIDE);
    }

    public String getValue(final String key) {
        final String value = settings.getString(key);
        return StringUtils.isEmpty(value) ? "" : value.trim();
    }

    public String[] getValues(final String key) {
        return settings.getStringArray(key);
    }

    public String getHubPluginVersion() {
        final Properties properties = new Properties();
        String version = null;
        try {
            properties.load(this.getClass().getResourceAsStream("/plugin.properties"));
            version = properties.getProperty("version");
            if (version != null) {
                return version;
            }
        } catch (final Exception e) {
            // Do nothing
        }
        return "<unknown>";
    }
}
