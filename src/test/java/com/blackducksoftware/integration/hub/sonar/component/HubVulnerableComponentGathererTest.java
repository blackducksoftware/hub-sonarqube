/**
 * Black Duck Hub Plugin for SonarQube
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.internal.google.common.collect.Sets;
import org.sonar.api.utils.log.Loggers;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.generated.component.CompositePathWithArchiveContext;
import com.blackducksoftware.integration.hub.api.generated.view.MatchedFileView;
import com.blackducksoftware.integration.hub.api.generated.view.VersionBomComponentView;
import com.blackducksoftware.integration.hub.service.ProjectService;
import com.blackducksoftware.integration.hub.service.model.VersionBomComponentModel;
import com.blackducksoftware.integration.hub.sonar.HubPropertyConstants;
import com.blackducksoftware.integration.hub.sonar.HubSonarLogger;
import com.blackducksoftware.integration.hub.sonar.SonarTestUtils;
import com.blackducksoftware.integration.hub.sonar.manager.SonarManager;
import com.blackducksoftware.integration.log.IntLogger;
import com.google.gson.JsonSyntaxException;

@SuppressWarnings("deprecation")
public class HubVulnerableComponentGathererTest {
    private final IntLogger logger = new HubSonarLogger(Loggers.get(getClass()));

    private ComponentHelper componentHelper;
    private SonarManager sonarManager;
    private ProjectService mockProjectService;

    @Before
    public void init() {
        sonarManager = new SonarManager(new MapSettings().asConfig());
        componentHelper = new ComponentHelper(sonarManager);

        mockProjectService = Mockito.mock(ProjectService.class);
    }

    @Test
    public void constructorDoesNotInitializeProjectVersionFieldsTest() throws IntegrationException {
        final SonarManager manager = Mockito.mock(SonarManager.class);
        Mockito.when(manager.getValue(HubPropertyConstants.HUB_PROJECT_OVERRIDE)).thenReturn("projectOverride");
        final HubVulnerableComponentGatherer gatherer = new HubVulnerableComponentGatherer(logger, componentHelper, manager, mockProjectService);

        assertTrue(null != gatherer);
    }

    @Test
    public void constructorInitializesProjectVersionFieldsTest() throws IntegrationException {
        final SonarManager manager = Mockito.mock(SonarManager.class);
        Mockito.when(manager.getValue(HubPropertyConstants.HUB_PROJECT_OVERRIDE)).thenReturn("projectOverride");
        Mockito.when(manager.getValue(HubPropertyConstants.HUB_PROJECT_VERSION_OVERRIED)).thenReturn("projectVersionOverride");

        final HubVulnerableComponentGatherer gatherer = new HubVulnerableComponentGatherer(logger, componentHelper, manager, mockProjectService);

        assertTrue(null != gatherer);
    }

    @Test
    public void gatherComponentsEmptyTest() throws IntegrationException {
        final HubVulnerableComponentGatherer gatherer = new HubVulnerableComponentGatherer(logger, componentHelper, sonarManager, mockProjectService);

        assertTrue(gatherer.gatherComponents().isEmpty());
    }

    @Test
    public void gatherComponentsWithMatchesTest() throws IntegrationException, JsonSyntaxException, IOException {
        final MatchedFileView matchedFiles0 = new MatchedFileView();
        final CompositePathWithArchiveContext filePath0 = new CompositePathWithArchiveContext();
        final String fileName = "test.jar";
        filePath0.compositePathContext = fileName + "!";
        matchedFiles0.filePath = filePath0;
        matchedFiles0.usages = Collections.emptyList();

        final MatchedFileView matchedFiles1 = new MatchedFileView();
        final CompositePathWithArchiveContext filePath1 = new CompositePathWithArchiveContext();
        filePath1.compositePathContext = "test.tar!";
        matchedFiles1.filePath = filePath1;
        matchedFiles1.usages = Collections.emptyList();

        final HubVulnerableComponentGatherer gatherer = new HubVulnerableComponentGatherer(logger, componentHelper, sonarManager, mockProjectService);

        final VersionBomComponentView component0 = SonarTestUtils.getObjectFromJsonFile(SonarTestUtils.JSON_COMPONENT_FILE_NAMES[0], VersionBomComponentView.class);
        final VersionBomComponentView component1 = SonarTestUtils.getObjectFromJsonFile(SonarTestUtils.JSON_COMPONENT_FILE_NAMES[1], VersionBomComponentView.class);
        final List<VersionBomComponentModel> components = Arrays.asList(new VersionBomComponentModel(component0, Arrays.asList(matchedFiles0)), new VersionBomComponentModel(component1, Arrays.asList(matchedFiles1)));
        Mockito.when(mockProjectService.getComponentsWithMatchedFilesForProjectVersion(Mockito.anyString(), Mockito.anyString())).thenReturn(components);

        assertEquals(Sets.newHashSet(fileName), gatherer.gatherComponents());
    }

    @Test
    public void getVulnerableComponentMapThrowsExceptionTest() throws IntegrationException {
        final ProjectService projectServiceMock = Mockito.mock(ProjectService.class);
        Mockito.when(projectServiceMock.getComponentsForProjectVersion(Mockito.any(), Mockito.any())).thenThrow(new IntegrationException());

        final HubVulnerableComponentGatherer gatherer = new HubVulnerableComponentGatherer(logger, componentHelper, sonarManager, projectServiceMock);
        final Map<String, Set<VersionBomComponentModel>> map = gatherer.getVulnerableComponentMap();

        assertTrue(map.isEmpty());
    }

}
