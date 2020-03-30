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
package com.blackducksoftware.integration.hub.sonar.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.sonar.HubPropertyConstants;
import com.blackducksoftware.integration.hub.sonar.manager.SonarManager;
import com.synopsys.integration.blackduck.api.generated.view.ComponentMatchedFilesView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.RiskProfileView;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.ProjectService;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;

public class HubVulnerableComponentGatherer implements ComponentGatherer {
    private final IntLogger logger;
    private final ComponentHelper componentHelper;
    private final SonarManager sonarManager;
    private final ProjectService projectService;
    private final BlackDuckService blackDuckService;

    private final Map<String, Set<ProjectVersionComponentView>> vulnerableComponentMap;
    private String hubProjectName;
    private String hubProjectVersionName;

    public HubVulnerableComponentGatherer(IntLogger logger, ComponentHelper componentHelper, SonarManager sonarManager, ProjectService projectService, BlackDuckService blackDuckService) {
        this.logger = logger;
        this.componentHelper = componentHelper;
        this.sonarManager = sonarManager;
        this.projectService = projectService;
        this.blackDuckService = blackDuckService;

        this.vulnerableComponentMap = new HashMap<>();
        setProjectAndVersion();
    }

    @Override
    public Set<String> gatherComponents() {
        return getVulnerableComponentMap().keySet();
    }

    // Returns a mapping of unqualified file names to sets of vulnerable bom components from that file.
    // Note: The sets of bom components for each file may (and likely do) intersect with other sets of bom components from different files. If this is the case, the sets likely have a parent-child relationship.
    public Map<String, Set<ProjectVersionComponentView>> getVulnerableComponentMap() {
        if (vulnerableComponentMap.isEmpty()) {
            List<ProjectVersionComponentView> components = new ArrayList<>();
            Optional<ProjectVersionWrapper> projectVersion = Optional.empty();
            try {
                projectVersion = projectService.getProjectVersion(hubProjectName, hubProjectVersionName);
            } catch (IntegrationException e) {
                logger.error(String.format("Couldn't find the BlackDuck project '%s' and version '%s'. Error: %s", hubProjectName, hubProjectVersionName, e.getMessage()), e);
            }
            if (projectVersion.isPresent()) {
                ProjectVersionView versionView = projectVersion.get().getProjectVersionView();
                try {
                    components = blackDuckService.getAllResponses(versionView, ProjectVersionView.COMPONENTS_LINK_RESPONSE);
                } catch (IntegrationException e) {
                    logger.error(String.format("Problem getting BOM components. Error: %s", e.getMessage()), e);
                }
            }
            mapMatchedFilesToComponents(vulnerableComponentMap, components);
        }
        return vulnerableComponentMap;
    }

    private void mapMatchedFilesToComponents(Map<String, Set<ProjectVersionComponentView>> vulnerableComponentMap, List<ProjectVersionComponentView> components) {
        if (components != null && !components.isEmpty()) {
            String prevName = "";
            for (ProjectVersionComponentView component : components) {
                RiskProfileView securityRiskProfile = component.getSecurityRiskProfile();
                if (null != securityRiskProfile && null != securityRiskProfile.getCounts() && securityRiskProfile.getCounts().size() > 0) {
                    prevName = logComponentName(prevName, component.getComponentName());
                    mapMatchedFilesToComponent(vulnerableComponentMap, component);
                }
            }
        }

    }

    private void mapMatchedFilesToComponent(Map<String, Set<ProjectVersionComponentView>> vulnerableComponentMap, ProjectVersionComponentView component) {
        List<ComponentMatchedFilesView> matchedFiles = new ArrayList<>();
        try {
            matchedFiles = blackDuckService.getAllResponses(component, ProjectVersionComponentView.MATCHED_FILES_LINK_RESPONSE);
        } catch (IntegrationException e) {
            logger.error(String.format("Problem getting the matched files for component '%s' version '%s'. Error: %s", component.getComponentName(), component.getComponentVersionName(), e.getMessage()), e);
        }
        logger.debug(String.format("Found %d files for component '%s' version '%s'.", matchedFiles.size(), component.getComponentName(), component.getComponentVersionName()));
        for (ComponentMatchedFilesView matchedFile : matchedFiles) {
            if (null != matchedFile.getFilePath() && null != matchedFile.getFilePath().getCompositePathContext()) {
                String fileName = componentHelper.getFileNameFromComposite(matchedFile.getFilePath().getCompositePathContext());
                if (!vulnerableComponentMap.containsKey(fileName)) {
                    vulnerableComponentMap.put(fileName, new HashSet<ProjectVersionComponentView>());
                }
                vulnerableComponentMap.get(fileName).add(component);
            }
        }
    }

    private String logComponentName(String prevName, String curName) {
        if (!prevName.equals(curName)) {
            logger.info(String.format("Getting matched files for %s...", curName));
            return curName;
        }
        return prevName;
    }

    private void setProjectAndVersion() {
        hubProjectName = sonarManager.getValue(HubPropertyConstants.SONAR_PROJECT_NAME_KEY);
        logger.debug(String.format("Default Hub Project to look for: %s", hubProjectName));
        hubProjectVersionName = sonarManager.getValue(HubPropertyConstants.SONAR_PROJECT_VERSION_KEY);
        logger.debug(String.format("Default Hub Project-Version to look for: %s", hubProjectVersionName));

        // Only override if the user provides a project AND project version
        String hubProjectNameOverride = sonarManager.getValue(HubPropertyConstants.HUB_PROJECT_OVERRIDE);
        String hubProjectVersionNameOverride = sonarManager.getValue(HubPropertyConstants.HUB_PROJECT_VERSION_OVERRIED);
        if (StringUtils.isNotEmpty(hubProjectNameOverride) && StringUtils.isNotEmpty(hubProjectVersionNameOverride)) {
            hubProjectName = hubProjectNameOverride;
            logger.debug(String.format("Overriden Hub Project to look for: %s", hubProjectName));
            hubProjectVersionName = hubProjectVersionNameOverride;
            logger.debug(String.format("Overriden Hub Project-Version to look for: %s", hubProjectVersionName));
        }
    }
}
