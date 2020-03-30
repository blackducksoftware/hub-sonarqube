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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.internal.google.common.collect.Sets;
import org.sonar.api.utils.log.Loggers;

import com.blackducksoftware.integration.hub.sonar.HubPropertyConstants;
import com.blackducksoftware.integration.hub.sonar.HubSonarLogger;
import com.blackducksoftware.integration.hub.sonar.SonarTestUtils;
import com.blackducksoftware.integration.hub.sonar.manager.SonarManager;
import com.blackducksoftware.integration.hub.sonar.model.MockFileSystem;
import com.blackducksoftware.integration.hub.sonar.model.MockSensorContext;
import com.synopsys.integration.blackduck.api.generated.component.ComponentMatchedFilesItemsFilePathView;
import com.synopsys.integration.blackduck.api.generated.component.ComponentVersionRiskProfileRiskDataCountsView;
import com.synopsys.integration.blackduck.api.generated.view.ComponentMatchedFilesView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.RiskProfileView;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.ProjectService;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;

@SuppressWarnings("deprecation")
public class HubVulnerableComponentGathererTest {
    private IntLogger logger;
    private ComponentHelper componentHelper;
    private SonarManager sonarManager;
    private ProjectService projectService;
    private BlackDuckService blackDuckService;

    @Before
    public void init() {
        File baseDir = new File(SonarTestUtils.TEST_DIRECTORY);
        MockSensorContext sensorContext = new MockSensorContext(new MapSettings().asConfig(), new MockFileSystem(baseDir));

        sonarManager = new SonarManager(sensorContext);
        componentHelper = new ComponentHelper(sonarManager);
        logger = new HubSonarLogger(Loggers.get(getClass()));

        projectService = Mockito.mock(ProjectService.class);
        blackDuckService = Mockito.mock(BlackDuckService.class);
    }

    @Test
    public void constructorDoesNotInitializeProjectVersionFieldsTest() throws IntegrationException {
        SonarManager manager = Mockito.mock(SonarManager.class);
        Mockito.when(manager.getValue(HubPropertyConstants.HUB_PROJECT_OVERRIDE)).thenReturn("projectOverride");
        HubVulnerableComponentGatherer gatherer = new HubVulnerableComponentGatherer(logger, componentHelper, manager, projectService, blackDuckService);

        assertTrue(null != gatherer);
    }

    @Test
    public void constructorInitializesProjectVersionFieldsTest() throws IntegrationException {
        SonarManager manager = Mockito.mock(SonarManager.class);
        Mockito.when(manager.getValue(HubPropertyConstants.HUB_PROJECT_OVERRIDE)).thenReturn("projectOverride");
        Mockito.when(manager.getValue(HubPropertyConstants.HUB_PROJECT_VERSION_OVERRIED)).thenReturn("projectVersionOverride");

        HubVulnerableComponentGatherer gatherer = new HubVulnerableComponentGatherer(logger, componentHelper, manager, projectService, blackDuckService);

        assertTrue(null != gatherer);
    }

    @Test
    public void gatherComponentsEmptyTest() throws IntegrationException {
        Mockito.when(projectService.getProjectVersion(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.empty());

        HubVulnerableComponentGatherer gatherer = new HubVulnerableComponentGatherer(logger, componentHelper, sonarManager, projectService, blackDuckService);

        assertTrue(gatherer.gatherComponents().isEmpty());
    }

    @Test
    public void gatherComponentsWithMatchesTest() throws IntegrationException {
        ComponentMatchedFilesView matchedFile0 = new ComponentMatchedFilesView();
        ComponentMatchedFilesItemsFilePathView filePath0 = new ComponentMatchedFilesItemsFilePathView();
        final String fileName0 = "test.jar";
        filePath0.setCompositePathContext(fileName0);
        matchedFile0.setFilePath(filePath0);

        ComponentMatchedFilesView matchedFile1 = new ComponentMatchedFilesView();
        ComponentMatchedFilesItemsFilePathView filePath1 = new ComponentMatchedFilesItemsFilePathView();
        final String fileName1 = "test.jar!";
        filePath1.setCompositePathContext(fileName1);
        matchedFile1.setFilePath(filePath1);

        ProjectVersionWrapper projectVersionWrapper = new ProjectVersionWrapper();
        Mockito.when(projectService.getProjectVersion(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.of(projectVersionWrapper));

        ProjectVersionComponentView projectVersionComponentView = new ProjectVersionComponentView();
        RiskProfileView securityRiskProfile = new RiskProfileView();
        ComponentVersionRiskProfileRiskDataCountsView componentVersionRiskProfileRiskDataCountsView = new ComponentVersionRiskProfileRiskDataCountsView();
        securityRiskProfile.setCounts(Arrays.asList(componentVersionRiskProfileRiskDataCountsView));
        projectVersionComponentView.setSecurityRiskProfile(securityRiskProfile);
        Mockito.when(blackDuckService.getAllResponses(Mockito.any(), Mockito.eq(ProjectVersionView.COMPONENTS_LINK_RESPONSE))).thenReturn(Arrays.asList(projectVersionComponentView));

        Mockito.when(blackDuckService.getAllResponses(Mockito.any(), Mockito.eq(ProjectVersionComponentView.MATCHED_FILES_LINK_RESPONSE))).thenReturn(Arrays.asList(matchedFile0, matchedFile1));

        HubVulnerableComponentGatherer gatherer = new HubVulnerableComponentGatherer(logger, componentHelper, sonarManager, projectService, blackDuckService);
        Set<String> strings = gatherer.gatherComponents();

        assertEquals(Sets.newHashSet(fileName0), strings);
    }

    @Test
    public void getVulnerableComponentMapThrowsProjectExceptionTest() throws IntegrationException {
        Mockito.when(projectService.getProjectVersion(Mockito.anyString(), Mockito.anyString())).thenThrow(new IntegrationException("Expected Exception"));

        HubVulnerableComponentGatherer gatherer = new HubVulnerableComponentGatherer(logger, componentHelper, sonarManager, projectService, blackDuckService);
        Map<String, Set<ProjectVersionComponentView>> map = gatherer.getVulnerableComponentMap();

        assertTrue(map.isEmpty());
    }

    @Test
    public void getVulnerableComponentMapNoProjectTest() throws IntegrationException {
        Mockito.when(projectService.getProjectVersion(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.empty());

        HubVulnerableComponentGatherer gatherer = new HubVulnerableComponentGatherer(logger, componentHelper, sonarManager, projectService, blackDuckService);
        Map<String, Set<ProjectVersionComponentView>> map = gatherer.getVulnerableComponentMap();

        assertTrue(map.isEmpty());
    }

    @Test
    public void getVulnerableComponentMapThrowsComponentsExceptionTest() throws IntegrationException {
        ProjectVersionWrapper projectVersionWrapper = new ProjectVersionWrapper();
        Mockito.when(projectService.getProjectVersion(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.of(projectVersionWrapper));
        Mockito.when(blackDuckService.getAllResponses(Mockito.any(), Mockito.eq(ProjectVersionView.COMPONENTS_LINK_RESPONSE))).thenThrow(new IntegrationException("Expected Exception"));

        HubVulnerableComponentGatherer gatherer = new HubVulnerableComponentGatherer(logger, componentHelper, sonarManager, projectService, blackDuckService);
        Map<String, Set<ProjectVersionComponentView>> map = gatherer.getVulnerableComponentMap();

        assertTrue(map.isEmpty());
    }

}
