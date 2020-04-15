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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.sonar.HubPropertyConstants;
import com.blackducksoftware.integration.hub.sonar.manager.SonarManager;
import com.synopsys.integration.blackduck.api.generated.component.ComponentVersionRiskProfileRiskDataCountsView;
import com.synopsys.integration.blackduck.api.generated.enumeration.ComponentVersionRiskProfileRiskDataCountsCountTypeType;
import com.synopsys.integration.blackduck.api.generated.view.ComponentMatchedFilesView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.RiskProfileView;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.ProjectService;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.blackduck.service.model.RequestFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.request.Request;

public class HubVulnerableComponentGatherer implements ComponentGatherer {
    private static final String FILTER = "filter";
    private static final String SECURITY_RISK = "securityRisk";
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
            Optional<ProjectVersionWrapper> projectVersionWrapper = Optional.empty();
            try {
                projectVersionWrapper = projectService.getProjectVersion(hubProjectName, hubProjectVersionName);
            } catch (IntegrationException e) {
                logger.error(String.format("Couldn't find the Black Duck project '%s' and version '%s'. Error: %s", hubProjectName, hubProjectVersionName, e.getMessage()), e);
            }
            if (projectVersionWrapper.isPresent()) {
                ProjectVersionView projectVersionView = projectVersionWrapper.get().getProjectVersionView();
                components = getProjectVersionComponents(projectVersionView);
            }
            logger.debug(String.format("Found %d components for Black Duck project '%s' version '%s'.", components.size(), hubProjectName, hubProjectVersionName));
            mapMatchedFilesToComponents(vulnerableComponentMap, components);
        }
        return vulnerableComponentMap;
    }

    private void mapMatchedFilesToComponents(Map<String, Set<ProjectVersionComponentView>> vulnerableComponentMap, List<ProjectVersionComponentView> components) {
        if (components != null && !components.isEmpty()) {
            String prevName = "";
            for (ProjectVersionComponentView component : components) {
                if (hasSecurityRisk(component)) {
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
        logger.debug(String.format("Default Black Duck Project to look for: %s", hubProjectName));
        hubProjectVersionName = sonarManager.getValue(HubPropertyConstants.SONAR_PROJECT_VERSION_KEY);
        logger.debug(String.format("Default Black Duck Project-Version to look for: %s", hubProjectVersionName));

        // Only override if the user provides a project AND project version
        String hubProjectNameOverride = sonarManager.getValue(HubPropertyConstants.HUB_PROJECT_OVERRIDE);
        String hubProjectVersionNameOverride = sonarManager.getValue(HubPropertyConstants.HUB_PROJECT_VERSION_OVERRIED);
        if (StringUtils.isNotEmpty(hubProjectNameOverride) && StringUtils.isNotEmpty(hubProjectVersionNameOverride)) {
            hubProjectName = hubProjectNameOverride;
            logger.debug(String.format("Overridden Black Duck Project to look for: %s", hubProjectName));
            hubProjectVersionName = hubProjectVersionNameOverride;
            logger.debug(String.format("Overridden Black Duck Project-Version to look for: %s", hubProjectVersionName));
        }
    }

    private List<ProjectVersionComponentView> getProjectVersionComponents(ProjectVersionView projectVersionView) {
        if (null == projectVersionView) {
            logger.error(String.format("The version '%s' is missing for Black Duck project '%s'.", hubProjectVersionName, hubProjectName));
            return Collections.emptyList();
        }
        try {
            Optional<String> optionalComponentsLink = projectVersionView.getFirstLink(ProjectVersionView.COMPONENTS_LINK);
            if (optionalComponentsLink.isPresent()) {
                String componentsLink = optionalComponentsLink.get();
                Request.Builder requestBuilder = RequestFactory.createCommonGetRequestBuilder();
                requestBuilder.uri(componentsLink);
                requestBuilder.addQueryParameter(FILTER, SECURITY_RISK + ":" + ComponentVersionRiskProfileRiskDataCountsCountTypeType.CRITICAL.toString().toLowerCase());
                requestBuilder.addQueryParameter(FILTER, SECURITY_RISK + ":" + ComponentVersionRiskProfileRiskDataCountsCountTypeType.HIGH.toString().toLowerCase());
                requestBuilder.addQueryParameter(FILTER, SECURITY_RISK + ":" + ComponentVersionRiskProfileRiskDataCountsCountTypeType.MEDIUM.toString().toLowerCase());
                requestBuilder.addQueryParameter(FILTER, SECURITY_RISK + ":" + ComponentVersionRiskProfileRiskDataCountsCountTypeType.LOW.toString().toLowerCase());
                return blackDuckService.getAllResponses(projectVersionView, ProjectVersionView.COMPONENTS_LINK_RESPONSE, requestBuilder);
            } else {
                // if the components link is missing it is likely a permission issue in Black Duck
                logger.error(String.format("The components link is missing for Black Duck project '%s' and version '%s'. Make sure the configured Black Duck user has permission to view the components in this project.", hubProjectName,
                    hubProjectVersionName));
            }
        } catch (IntegrationException e) {
            logger.error(String.format("Problem getting components for Black Duck project '%s' and version '%s'. Error: %s", hubProjectName, hubProjectVersionName, e.getMessage()), e);
        }
        return Collections.emptyList();
    }

    private boolean hasSecurityRisk(ProjectVersionComponentView componentView) {
        RiskProfileView securityRiskProfile = componentView.getSecurityRiskProfile();
        if (null != securityRiskProfile && null != securityRiskProfile.getCounts() && !securityRiskProfile.getCounts().isEmpty()) {
            for (ComponentVersionRiskProfileRiskDataCountsView countView : securityRiskProfile.getCounts()) {
                switch (countView.getCountType()) {
                    case CRITICAL:
                    case HIGH:
                    case MEDIUM:
                    case LOW:
                        if (countView.getCount().intValue() > 0) {
                            return true;
                        }
                        break;
                    default:
                        break;
                }
            }
        }
        return false;
    }
}
