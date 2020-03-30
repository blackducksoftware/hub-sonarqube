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
package com.blackducksoftware.integration.hub.sonar.metric;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.measure.Measure;
import org.sonar.api.internal.google.common.collect.Sets;

import com.blackducksoftware.integration.hub.dataservice.versionbomcomponent.model.VersionBomComponentModel;
import com.blackducksoftware.integration.hub.model.view.MatchedFilesView;
import com.blackducksoftware.integration.hub.model.view.VersionBomComponentView;
import com.blackducksoftware.integration.hub.service.HubResponseService;
import com.blackducksoftware.integration.hub.sonar.SonarTestUtils;
import com.blackducksoftware.integration.hub.sonar.model.MockRestConnection;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.log.LogLevel;
import com.blackducksoftware.integration.log.PrintStreamIntLogger;

public class MetricsHelperTest {
    private static final File BASE_DIR = new File(SonarTestUtils.TEST_DIRECTORY);
    private static final IntLogger LOG = new PrintStreamIntLogger(System.out, LogLevel.INFO);

    private SensorContextTester context;
    private Map<String, Set<VersionBomComponentModel>> vulnerableComponentsMap;
    private MetricsHelper metricsHelper;

    @Before
    public void init() {
        context = SensorContextTester.create(BASE_DIR);
        metricsHelper = new MetricsHelper(LOG, context);
        vulnerableComponentsMap = new HashMap<>();
    }

    @Test
    public void createMeasuresForInputFileWhenMapDoesNotContainFileTest() {
        final String componentKey1 = SonarTestUtils.MY_PROJECT_KEY + ":INVALID_FILE";
        InputFile inputFile = TestInputFileBuilder.create(SonarTestUtils.MY_PROJECT_KEY, "INVALID_FILE").build();
        metricsHelper.createMeasuresForInputFile(vulnerableComponentsMap, inputFile);

        assertEquals(null, context.measure(componentKey1, HubSonarMetrics.NUM_VULN_LOW));
        assertEquals(null, context.measure(componentKey1, HubSonarMetrics.NUM_VULN_MED));
        assertEquals(null, context.measure(componentKey1, HubSonarMetrics.NUM_VULN_HIGH));
        assertEquals(null, context.measure(componentKey1, HubSonarMetrics.COMPONENT_NAMES));
    }

    @Test
    public void createMeasuresForInputFileWhenFileIsMappedToEmptySet() {
        final String file1 = "test.jar";
        final String componentKey1 = SonarTestUtils.MY_PROJECT_KEY + ":" + file1;

        vulnerableComponentsMap.put(file1, Sets.newHashSet());
        metricsHelper.createMeasuresForInputFile(vulnerableComponentsMap, TestInputFileBuilder.create(SonarTestUtils.MY_PROJECT_KEY, file1).build());

        Measure<Integer> numVulnLow = context.measure(componentKey1, HubSonarMetrics.NUM_VULN_LOW);
        Measure<Integer> numVulnMed = context.measure(componentKey1, HubSonarMetrics.NUM_VULN_MED);
        Measure<Integer> numVulnHigh = context.measure(componentKey1, HubSonarMetrics.NUM_VULN_HIGH);
        Measure<String> componentNames = context.measure(componentKey1, HubSonarMetrics.COMPONENT_NAMES);

        int low = numVulnLow.value().intValue();
        int med = numVulnMed.value().intValue();
        int high = numVulnHigh.value().intValue();

        assertEquals(0, low);
        assertEquals(0, med);
        assertEquals(0, high);
        assertEquals(null, componentNames);
    }

    @Test
    public void createMeasuresForVulnerableComponentsTest() throws IOException {
        final String file1 = "test.jar";
        final String longComponentName = "this_is_a_very_very_very_long_but_not_fully_qualified_file_name_that_is_contained_within_the_first_java_archive_file_so_it_will_have_a_subset_of_components.jar";
        final String componentKey1 = SonarTestUtils.MY_PROJECT_KEY + ":" + file1;

        HubResponseService hubResponseService = new HubResponseService(new MockRestConnection(LOG));
        VersionBomComponentView component0 = hubResponseService.getItemAs(SonarTestUtils.getJsonFromFile(SonarTestUtils.getJsonComponentFileNames()[0]), VersionBomComponentView.class);
        VersionBomComponentView component1 = hubResponseService.getItemAs(SonarTestUtils.getJsonFromFile(SonarTestUtils.getJsonComponentFileNames()[1]), VersionBomComponentView.class);
        VersionBomComponentView component2 = hubResponseService.getItemAs(SonarTestUtils.getJsonFromFile(SonarTestUtils.getJsonComponentFileNames()[1]), VersionBomComponentView.class);
        component2.componentName = longComponentName;

        List<MatchedFilesView> matchedFiles = Collections.emptyList();
        vulnerableComponentsMap.put(file1, Sets.newHashSet(new VersionBomComponentModel(component0, matchedFiles), new VersionBomComponentModel(component1, matchedFiles), new VersionBomComponentModel(component2, matchedFiles)));

        List<InputFile> inputFiles = Arrays.asList(TestInputFileBuilder.create(SonarTestUtils.MY_PROJECT_KEY, file1).build());

        metricsHelper.createMeasuresForInputFiles(vulnerableComponentsMap, inputFiles);

        Measure<Integer> numVulnLow = context.measure(componentKey1, HubSonarMetrics.NUM_VULN_LOW);
        Measure<Integer> numVulnMed = context.measure(componentKey1, HubSonarMetrics.NUM_VULN_MED);
        Measure<Integer> numVulnHigh = context.measure(componentKey1, HubSonarMetrics.NUM_VULN_HIGH);
        Measure<String> componentNames = context.measure(componentKey1, HubSonarMetrics.COMPONENT_NAMES);

        int low = numVulnLow.value().intValue();
        int med = numVulnMed.value().intValue();
        int high = numVulnHigh.value().intValue();

        assertTrue((low + med + high) > 0);
        assertEquals(1, low);
        assertEquals(1, med);
        assertEquals(1, high);

        String compNames = componentNames.value();
        assertTrue(compNames.contains("Test Component 0"));
        assertTrue(compNames.contains("Test Component 1"));
    }
}
