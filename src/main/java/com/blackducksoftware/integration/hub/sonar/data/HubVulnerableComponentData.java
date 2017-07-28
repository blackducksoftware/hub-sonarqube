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
package com.blackducksoftware.integration.hub.sonar.data;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.sonar.api.config.Settings;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.model.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.model.view.ProjectView;
import com.blackducksoftware.integration.hub.model.view.VulnerableComponentView;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.hub.sonar.HubPropertyConstants;
import com.blackducksoftware.integration.hub.sonar.HubSonarLogger;
import com.blackducksoftware.integration.hub.sonar.HubSonarUtils;

public class HubVulnerableComponentData {

    private final Settings settings;
    private final HubSonarLogger logger;
    private String hubProjectName;
    private String hubProjectVersionName;

    public HubVulnerableComponentData(final HubSonarLogger logger, final Settings settings) {
        this.logger = logger;
        this.settings = settings;
        setProjectAndVersion(settings);
    }

    public List<VulnerableComponentView> gatherVulnerableComponents() throws IntegrationException {
        final HubServerConfig hubServerConfig = HubSonarUtils.getHubServerConfig(settings);
        final RestConnection restConnection = HubSonarUtils.getRestConnection(logger, hubServerConfig);
        restConnection.connect();

        final HubServicesFactory services = new HubServicesFactory(restConnection);
        final MetaService metaService = services.createMetaService(logger);
        ProjectView project = null;
        ProjectVersionView version = null;
        try {
            project = services.createProjectRequestService(logger).getProjectByName(hubProjectName);
            version = services.createProjectVersionRequestService(logger).getProjectVersion(project, hubProjectVersionName);
        } catch (final IntegrationException e) {
            // TODO handle
        }

        final String vulnerableBomComponentsUrl = metaService.getFirstLinkSafely(version, MetaService.VULNERABLE_COMPONENTS_LINK);
        final List<VulnerableComponentView> components = services.createVulnerableBomComponentRequestService().getVulnerableComponentsMatchingComponentName(vulnerableBomComponentsUrl);

        return components;
    }

    public List<String> gatherMatchedFiles(final List<VulnerableComponentView> components, final MetaService metaService) throws HubIntegrationException {
        final List<String> matchedFiles = new ArrayList<>();
        // TODO add "matched-files" to MetaService links
        for (final VulnerableComponentView component : components) {
            matchedFiles.add(metaService.getFirstLink(component, "matched-files"));
        }
        return matchedFiles;
    }

    private void setProjectAndVersion(final Settings settings) {
        hubProjectName = HubSonarUtils.getAndTrimProp(settings, HubSonarUtils.SONAR_PROJECT_NAME_KEY);
        hubProjectVersionName = HubSonarUtils.getAndTrimProp(settings, HubSonarUtils.SONAR_PROJECT_VERSION_KEY);

        // Only override if the user provides a project AND project version
        final String hubProjectNameOverride = HubSonarUtils.getAndTrimProp(settings, HubPropertyConstants.HUB_PROJECT_OVERRIDE);
        final String hubProjectVersionNameOverride = HubSonarUtils.getAndTrimProp(settings, HubPropertyConstants.HUB_PROJECT_VERSION_OVERRIED);
        if (StringUtils.isNotEmpty(hubProjectNameOverride) && StringUtils.isNotEmpty(hubProjectVersionNameOverride)) {
            hubProjectName = hubProjectNameOverride;
            hubProjectVersionName = hubProjectVersionNameOverride;
        }
    }

}
