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
package com.blackducksoftware.integration.hub.sonar.component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.aggregate.bom.AggregateBomRequestService;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.api.matchedfiles.MatchedFilesRequestService;
import com.blackducksoftware.integration.hub.dataservice.project.ProjectVersionWrapper;
import com.blackducksoftware.integration.hub.model.view.MatchedFilesView;
import com.blackducksoftware.integration.hub.model.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.model.view.VersionBomComponentView;
import com.blackducksoftware.integration.hub.sonar.HubPropertyConstants;
import com.blackducksoftware.integration.hub.sonar.HubSonarLogger;
import com.blackducksoftware.integration.hub.sonar.manager.HubManager;
import com.blackducksoftware.integration.hub.sonar.manager.SonarManager;

public class HubVulnerableComponentGatherer implements ComponentGatherer {
    private final HubSonarLogger logger;
    private final ComponentHelper componentHelper;
    private final SonarManager sonarManager;
    private final HubManager hubManager;

    private final Map<String, Set<VersionBomComponentView>> vulnerableComponentMap;
    private String hubProjectName;
    private String hubProjectVersionName;

    public HubVulnerableComponentGatherer(final HubSonarLogger logger, final ComponentHelper componentHelper, final SonarManager sonarManager, final HubManager hubManager) {
        this.logger = logger;
        this.componentHelper = componentHelper;
        this.sonarManager = sonarManager;
        this.hubManager = hubManager;

        this.vulnerableComponentMap = new HashMap<>();
        setProjectAndVersion();
    }

    @Override
    public Set<String> gatherComponents() {
        return getVulnerableComponentMap().keySet();
    }

    public Map<String, Set<VersionBomComponentView>> getVulnerableComponentMap() {
        if (vulnerableComponentMap.isEmpty()) {
            if (hubManager.getRestConnection() != null) {
                final ProjectVersionWrapper projectVersionWrapper = hubManager.getProjectVersionWrapper(hubProjectName, hubProjectVersionName);
                if (projectVersionWrapper != null) {
                    final List<VersionBomComponentView> components = getBomComponents(projectVersionWrapper.getProjectVersionView(), hubManager.getAggregateBomRequestService());
                    if (components != null) {
                        String prevName = "";
                        for (final VersionBomComponentView component : components) {
                            if (!prevName.equals(component.componentName)) {
                                logger.info(String.format("Getting matched files for %s...", component.componentName));
                                prevName = component.componentName;
                            }
                            mapMatchedFilesToComponent(component, hubManager.getMatchedFilesRequestService(), hubManager.getMetaService());
                        }
                    } else {
                        logger.warn("List of vulnerable Hub components was null. No files will be matched.");
                    }
                } else {
                    logger.warn(String.format("Hub project (%s) and version (%s) not found.", hubProjectName, hubProjectVersionName));
                }
            }
        }
        return vulnerableComponentMap;
    }

    private List<VersionBomComponentView> getBomComponents(final ProjectVersionView version, final AggregateBomRequestService aggregateBomRequestService) {
        List<VersionBomComponentView> components = null;
        try {
            logger.info(String.format("Attempting to get vulnerable components from '%s > %s'...", hubProjectName, hubProjectVersionName));
            components = aggregateBomRequestService.getBomEntries(version);
            logger.info(String.format("Success! Found %d vulnerable components.", components.size()));
        } catch (final IntegrationException e) {
            logger.error(e);
        }

        return components;
    }

    private void mapMatchedFilesToComponent(final VersionBomComponentView component, final MatchedFilesRequestService matchedFilesRequestService, final MetaService metaService) {
        final String matchedFilesLink = metaService.getFirstLinkSafely(component, MetaService.MATCHED_FILES_LINK);
        List<MatchedFilesView> allMatchedFiles = null;
        try {
            allMatchedFiles = matchedFilesRequestService.getMatchedFiles(matchedFilesLink);
        } catch (final IntegrationException e) {
            logger.error(e);
        }

        if (allMatchedFiles != null) {
            for (final MatchedFilesView matchedFile : allMatchedFiles) {
                final String fileName = componentHelper.getFileNameFromComposite(matchedFile.filePath.compositePathContext);
                if (!vulnerableComponentMap.containsKey(fileName)) {
                    vulnerableComponentMap.put(fileName, new HashSet<VersionBomComponentView>());
                }
                vulnerableComponentMap.get(fileName).add(component);
            }
        }
    }

    private void setProjectAndVersion() {
        hubProjectName = sonarManager.getValue(HubPropertyConstants.SONAR_PROJECT_NAME_KEY);
        logger.debug(String.format("Default Hub Project to look for: %s", hubProjectName));
        hubProjectVersionName = sonarManager.getValue(HubPropertyConstants.SONAR_PROJECT_VERSION_KEY);
        logger.debug(String.format("Default Hub Project-Version to look for: %s", hubProjectVersionName));

        // Only override if the user provides a project AND project version
        final String hubProjectNameOverride = sonarManager.getValue(HubPropertyConstants.HUB_PROJECT_OVERRIDE);
        final String hubProjectVersionNameOverride = sonarManager.getValue(HubPropertyConstants.HUB_PROJECT_VERSION_OVERRIED);
        if (StringUtils.isNotEmpty(hubProjectNameOverride) && StringUtils.isNotEmpty(hubProjectVersionNameOverride)) {
            hubProjectName = hubProjectNameOverride;
            logger.debug(String.format("Overriden Hub Project to look for: %s", hubProjectName));
            hubProjectVersionName = hubProjectVersionNameOverride;
            logger.debug(String.format("Overriden Hub Project-Version to look for: %s", hubProjectVersionName));
        }
    }
}
