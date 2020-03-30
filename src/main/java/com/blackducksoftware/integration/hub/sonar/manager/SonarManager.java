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

import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.config.Configuration;
import org.sonar.api.utils.log.Loggers;

import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.sonar.HubPropertyConstants;
import com.blackducksoftware.integration.hub.sonar.HubSonarLogger;
import com.blackducksoftware.integration.validator.AbstractValidator;
import com.blackducksoftware.integration.validator.FieldEnum;
import com.blackducksoftware.integration.validator.ValidationResult;
import com.blackducksoftware.integration.validator.ValidationResults;

public class SonarManager {
    private final HubSonarLogger logger;
    private final SensorContext context;
    private final Configuration configuration;

    @Deprecated
    public SonarManager(final Configuration settings) {
        this.context = null;
        this.configuration = settings;
        logger = new HubSonarLogger(Loggers.get(getClass()));
    }

    public SonarManager(final SensorContext context) {
        this.context = context;
        this.configuration = context.config();
        logger = new HubSonarLogger(Loggers.get(context.getClass()));
    }

    public SensorContext getSensorContext() {
        return context;
    }

    public HubServerConfig getHubServerConfigFromSettings() {
        final HubServerConfigBuilder configBuilder = new HubServerConfigBuilder();
        if (configuration != null) {
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
        if (isConfigValid(configBuilder)) {
            return configBuilder.build();
        }
        return new HubServerConfig(null, 300, null, null, false);
    }

    public boolean isConfigValid(final HubServerConfigBuilder configBuilder) {
        final AbstractValidator validator = configBuilder.createValidator();
        final ValidationResults validationResults = validator.assertValid();
        if (validationResults.hasErrors()) {
            final Map<FieldEnum, Set<ValidationResult>> resultsMap = validationResults.getResultMap();
            for (final FieldEnum field : resultsMap.keySet()) {
                logger.error(String.format("%s: %s", field, resultsMap.get(field)));
            }
            return false;
        }
        logger.debug("Hub config validation results: SUCCESS!");
        return true;
    }

    public String[] getGlobalInclusionPatterns() {
        return getValues(HubPropertyConstants.HUB_BINARY_INCLUSION_PATTERN_OVERRIDE);
    }

    public String[] getGlobalExclusionPatterns() {
        return getValues(HubPropertyConstants.HUB_BINARY_EXCLUSION_PATTERN_OVERRIDE);
    }

    public String getValue(final String key) {
        final Optional<String> value = configuration.get(key);
        return value.isPresent() ? value.get().trim() : "";
    }

    public String[] getValues(final String key) {
        return configuration.getStringArray(key);
    }

    public String getHubPluginVersionFromFile(final String fileName) {
        final Properties properties = new Properties();
        String version = null;
        try {
            properties.load(this.getClass().getResourceAsStream(fileName));
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
