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

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.internal.google.common.collect.Sets;
import org.sonar.api.utils.log.Loggers;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.dataservice.versionbomcomponent.VersionBomComponentDataService;
import com.blackducksoftware.integration.hub.dataservice.versionbomcomponent.model.VersionBomComponentModel;
import com.blackducksoftware.integration.hub.model.view.MatchedFilesView;
import com.blackducksoftware.integration.hub.model.view.components.FilePathView;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.sonar.HubPropertyConstants;
import com.blackducksoftware.integration.hub.sonar.HubSonarLogger;
import com.blackducksoftware.integration.hub.sonar.manager.SonarManager;
import com.blackducksoftware.integration.hub.sonar.model.MockRestConnection;
import com.blackducksoftware.integration.hub.sonar.model.MockVersionBomComponentDataService;
import com.blackducksoftware.integration.log.IntLogger;

@SuppressWarnings("deprecation")
public class HubVulnerableComponentGathererTest {
    private IntLogger logger;
    private ComponentHelper componentHelper;
    private SonarManager sonarManager;
    private MockVersionBomComponentDataService versionBomComponentDataService;

    @Before
    public void init() {
        sonarManager = new SonarManager(new MapSettings().asConfig());
        componentHelper = new ComponentHelper(sonarManager);
        logger = new HubSonarLogger(Loggers.get(getClass()));

        final RestConnection restConnection = new MockRestConnection(logger);
        versionBomComponentDataService = new MockVersionBomComponentDataService(restConnection);
    }

    @Test
    public void constructorDoesNotInitializeProjectVersionFieldsTest() throws IntegrationException {
        final SonarManager manager = Mockito.mock(SonarManager.class);
        Mockito.when(manager.getValue(HubPropertyConstants.HUB_PROJECT_OVERRIDE)).thenReturn("projectOverride");
        final HubVulnerableComponentGatherer gatherer = new HubVulnerableComponentGatherer(logger, componentHelper, manager, versionBomComponentDataService);

        assertTrue(null != gatherer);
    }

    @Test
    public void constructorInitializesProjectVersionFieldsTest() throws IntegrationException {
        final SonarManager manager = Mockito.mock(SonarManager.class);
        Mockito.when(manager.getValue(HubPropertyConstants.HUB_PROJECT_OVERRIDE)).thenReturn("projectOverride");
        Mockito.when(manager.getValue(HubPropertyConstants.HUB_PROJECT_VERSION_OVERRIED)).thenReturn("projectVersionOverride");

        final HubVulnerableComponentGatherer gatherer = new HubVulnerableComponentGatherer(logger, componentHelper, manager, versionBomComponentDataService);

        assertTrue(null != gatherer);
    }

    @Test
    public void gatherComponentsEmptyTest() throws IntegrationException {
        versionBomComponentDataService.setEmpty(true);
        final HubVulnerableComponentGatherer gatherer = new HubVulnerableComponentGatherer(logger, componentHelper, sonarManager, versionBomComponentDataService);

        assertTrue(gatherer.gatherComponents().isEmpty());
    }

    @Test
    public void gatherComponentsWithMatchesTest() throws IntegrationException {
        final MatchedFilesView matchedFiles0 = new MatchedFilesView();
        final FilePathView filePath0 = new FilePathView();
        final String fileName = "test.jar";
        filePath0.compositePathContext = fileName + "!";
        matchedFiles0.filePath = filePath0;

        final MatchedFilesView matchedFiles1 = new MatchedFilesView();
        final FilePathView filePath1 = new FilePathView();
        filePath1.compositePathContext = "test.tar!";
        matchedFiles1.filePath = filePath1;

        versionBomComponentDataService.setMatchedFiles(Arrays.asList(matchedFiles0), Arrays.asList(matchedFiles1));
        final HubVulnerableComponentGatherer gatherer = new HubVulnerableComponentGatherer(logger, componentHelper, sonarManager, versionBomComponentDataService);

        assertEquals(Sets.newHashSet(fileName), gatherer.gatherComponents());
    }

    @Test
    public void getVulnerableComponentMapThrowsExceptionTest() throws IntegrationException {
        final VersionBomComponentDataService dataService = Mockito.mock(VersionBomComponentDataService.class);
        Mockito.when(dataService.getComponentsForProjectVersion(Mockito.any(), Mockito.any())).thenThrow(new IntegrationException());

        final HubVulnerableComponentGatherer gatherer = new HubVulnerableComponentGatherer(logger, componentHelper, sonarManager, dataService);
        final Map<String, Set<VersionBomComponentModel>> map = gatherer.getVulnerableComponentMap();

        assertTrue(map.isEmpty());
    }

}
