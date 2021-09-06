/**
 * Black Duck Hub Plugin for SonarQube
 *
 * Copyright (C) 2020 Black Duck Software, Inc.
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

import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.config.Configuration;
import org.sonar.api.utils.log.Loggers;

import com.blackducksoftware.integration.hub.sonar.HubPropertyConstants;
import com.blackducksoftware.integration.hub.sonar.HubSonarLogger;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfigBuilder;
import com.synopsys.integration.builder.BuilderStatus;

public class SonarManager {
    private final HubSonarLogger logger;
    private final SensorContext context;
    private final Configuration configuration;

    public SonarManager(SensorContext context) {
        this.context = context;
        this.configuration = context.config();
        logger = new HubSonarLogger(Loggers.get(context.getClass()));
    }

    public SensorContext getSensorContext() {
        return context;
    }

    public Optional<BlackDuckServerConfig> getBlackDuckServerConfigFromSettings() {
        BlackDuckServerConfigBuilder configBuilder = new BlackDuckServerConfigBuilder();
        if (configuration != null) {
            configBuilder.setUrl(getValue(HubPropertyConstants.HUB_URL));
            configBuilder.setUsername(getValue(HubPropertyConstants.HUB_USERNAME));
            configBuilder.setPassword(getValue(HubPropertyConstants.HUB_PASSWORD));
            configBuilder.setApiToken(getValue(HubPropertyConstants.HUB_API_TOKEN));
            configBuilder.setTimeoutInSeconds(getValue(HubPropertyConstants.HUB_TIMEOUT));
            configBuilder.setTrustCert(Boolean.parseBoolean(getValue(HubPropertyConstants.HUB_TRUST_SSL_CERT)));

            configBuilder.setProxyHost(getValue(HubPropertyConstants.HUB_PROXY_HOST));
            configBuilder.setProxyPort(getValue(HubPropertyConstants.HUB_PROXY_PORT));
            configBuilder.setProxyUsername(getValue(HubPropertyConstants.HUB_PROXY_USERNAME));
            configBuilder.setProxyPassword(getValue(HubPropertyConstants.HUB_PROXY_PASSWORD));
        }
        if (isConfigValid(configBuilder)) {
            return Optional.of(configBuilder.build());
        }
        return Optional.empty();
    }

    public boolean isConfigValid(BlackDuckServerConfigBuilder configBuilder) {
        BuilderStatus builderStatus = configBuilder.validateAndGetBuilderStatus();
        if (builderStatus.isValid()) {
            logger.debug("Black Duck config validation results: SUCCESS!");
            return true;
        }
        List<String> errorMessages = builderStatus.getErrorMessages();
        for (String error : errorMessages) {
            logger.error(error);
        }
        return false;
    }

    public String[] getGlobalInclusionPatterns() {
        return getValues(HubPropertyConstants.HUB_BINARY_INCLUSION_PATTERN_OVERRIDE);
    }

    public String[] getGlobalExclusionPatterns() {
        return getValues(HubPropertyConstants.HUB_BINARY_EXCLUSION_PATTERN_OVERRIDE);
    }

    public String getValue(String key) {
        Optional<String> value = configuration.get(key);
        return value.isPresent() ? value.get().trim() : "";
    }

    public String[] getValues(String key) {
        return configuration.getStringArray(key);
    }

    public String getHubPluginVersionFromFile(String fileName) {
        Properties properties = new Properties();
        String version = null;
        try {
            properties.load(this.getClass().getResourceAsStream(fileName));
            version = properties.getProperty("version");
            if (version != null) {
                return version;
            }
        } catch (Exception e) {
            // Do nothing
        }
        return "<unknown>";
    }
}
