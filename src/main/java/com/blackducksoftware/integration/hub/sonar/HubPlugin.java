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

import java.util.Arrays;
import java.util.List;

import org.sonar.api.Plugin;
import org.sonar.api.config.PropertyDefinition;

import com.blackducksoftware.integration.hub.sonar.measure.HubSonarMetrics;
import com.blackducksoftware.integration.hub.sonar.measure.computer.ComputeHighSecutiryVulnerabilityTotal;
import com.blackducksoftware.integration.hub.sonar.measure.computer.ComputeLowSecurityVulnerabilityTotal;
import com.blackducksoftware.integration.hub.sonar.measure.computer.ComputeMediumSecurityVulnerabilityTotal;
import com.blackducksoftware.integration.hub.sonar.measure.computer.ComputeVulnerabilityAverage;
import com.blackducksoftware.integration.hub.sonar.measure.computer.ComputeVulnerabilityRating;
import com.blackducksoftware.integration.hub.sonar.web.HubVulnerabilityWidget;

public class HubPlugin implements Plugin {
    public static final String PLUGIN_NAME = "Black Duck Hub Plugin for SonarQube";

    @Override
    public void define(final Context context) {
        context.addExtensions(getGlobalPropertyExtensions());
        context.addExtensions(HubSensor.class, HubSonarMetrics.class, HubVulnerabilityWidget.class);
        context.addExtensions(ComputeHighSecutiryVulnerabilityTotal.class, ComputeMediumSecurityVulnerabilityTotal.class, ComputeLowSecurityVulnerabilityTotal.class, ComputeVulnerabilityRating.class, ComputeVulnerabilityAverage.class);
    }

    public List<PropertyDefinition> getGlobalPropertyExtensions() {
        return Arrays.asList(HubPropertyConstants.Definitions.HUB_URL, HubPropertyConstants.Definitions.HUB_USERNAME, HubPropertyConstants.Definitions.HUB_PASSWORD, HubPropertyConstants.Definitions.HUB_TIMEOUT,
                HubPropertyConstants.Definitions.HUB_IMPORT_SSL_CERT, HubPropertyConstants.Definitions.HUB_PROXY_HOST, HubPropertyConstants.Definitions.HUB_PROXY_PORT, HubPropertyConstants.Definitions.HUB_NO_PROXY_HOSTS,
                HubPropertyConstants.Definitions.HUB_PROXY_USERNAME, HubPropertyConstants.Definitions.HUB_PROXY_PASSWORD, HubPropertyConstants.Definitions.HUB_BINARY_INCLUSION_PATTERN_OVERRIDE,
                HubPropertyConstants.Definitions.HUB_BINARY_EXCLUSION_PATTERN_OVERRIDE);
    }
}
