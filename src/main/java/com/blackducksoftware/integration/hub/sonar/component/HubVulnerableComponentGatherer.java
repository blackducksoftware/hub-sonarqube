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
import com.blackducksoftware.integration.hub.dataservice.versionbomcomponent.VersionBomComponentDataService;
import com.blackducksoftware.integration.hub.dataservice.versionbomcomponent.model.MatchedFilesModel;
import com.blackducksoftware.integration.hub.dataservice.versionbomcomponent.model.VersionBomComponentModel;
import com.blackducksoftware.integration.hub.sonar.HubPropertyConstants;
import com.blackducksoftware.integration.hub.sonar.manager.SonarManager;
import com.blackducksoftware.integration.log.IntLogger;

public class HubVulnerableComponentGatherer implements ComponentGatherer {
    private final IntLogger logger;
    private final ComponentHelper componentHelper;
    private final SonarManager sonarManager;
    private final VersionBomComponentDataService versionBomComponentDataService;

    private final Map<String, Set<VersionBomComponentModel>> vulnerableComponentMap;
    private String hubProjectName;
    private String hubProjectVersionName;

    public HubVulnerableComponentGatherer(final IntLogger logger, final ComponentHelper componentHelper, final SonarManager sonarManager, final VersionBomComponentDataService versionBomComponentDataService) {
        this.logger = logger;
        this.componentHelper = componentHelper;
        this.sonarManager = sonarManager;
        this.versionBomComponentDataService = versionBomComponentDataService;

        this.vulnerableComponentMap = new HashMap<>();
        setProjectAndVersion();
    }

    @Override
    public Set<String> gatherComponents() {
        return getVulnerableComponentMap().keySet();
    }

    public Map<String, Set<VersionBomComponentModel>> getVulnerableComponentMap() {
        if (vulnerableComponentMap.isEmpty()) {
            List<VersionBomComponentModel> components = null;
            try {
                components = versionBomComponentDataService.getComponentsForProjectVersion(hubProjectName, hubProjectVersionName);
            } catch (final IntegrationException e) {
                logger.error(String.format("Problem getting BOM components: %s", e));
            }
            if (components != null && !components.isEmpty()) {
                mapMatchedFilesToComponents(components);
            }
        }
        return vulnerableComponentMap;
    }

    private void mapMatchedFilesToComponents(final List<VersionBomComponentModel> components) {
        String prevName = "";
        for (final VersionBomComponentModel component : components) {
            if (component.hasSecurityRisk()) {
                final String curName = component.getComponentName();
                if (!prevName.equals(curName)) {
                    logger.info(String.format("Getting matched files for %s...", curName));
                    prevName = curName;
                }
                final List<MatchedFilesModel> allMatchedFiles = component.getMatchedFiles();
                if (allMatchedFiles != null && !allMatchedFiles.isEmpty()) {
                    for (final MatchedFilesModel matchedFile : allMatchedFiles) {
                        final String fileName = componentHelper.getFileNameFromComposite(matchedFile.getCompositePathContext());
                        if (!vulnerableComponentMap.containsKey(fileName)) {
                            vulnerableComponentMap.put(fileName, new HashSet<VersionBomComponentModel>());
                        }
                        vulnerableComponentMap.get(fileName).add(component);
                    }
                }
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
