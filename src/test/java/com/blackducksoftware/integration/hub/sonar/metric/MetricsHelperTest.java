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
package com.blackducksoftware.integration.hub.sonar.metric;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.measure.Measure;
import org.sonar.api.internal.google.common.collect.Sets;

import com.blackducksoftware.integration.hub.model.view.VersionBomComponentView;
import com.blackducksoftware.integration.hub.service.HubResponseService;
import com.blackducksoftware.integration.hub.sonar.SonarTestUtils;
import com.blackducksoftware.integration.hub.sonar.model.MockLogger;
import com.blackducksoftware.integration.hub.sonar.model.MockRestConnection;

public class MetricsHelperTest {
    private static final File BASE_DIR = new File(SonarTestUtils.TEST_DIRECTORY);
    private static final MockLogger LOG = new MockLogger();

    @Test
    public void createMeasuresForVulnerableComponentsTest() throws IOException {
        final String file1 = "test.jar";
        final String componentKey1 = SonarTestUtils.MY_PROJECT_KEY + ":" + file1;

        final Map<String, Set<VersionBomComponentView>> vulnerableComponentsMap = new HashMap<>();
        final HubResponseService hubResponseService = new HubResponseService(new MockRestConnection(LOG));
        final VersionBomComponentView component0 = hubResponseService.getItemAs(SonarTestUtils.getJsonFromFile(SonarTestUtils.JSON_COMPONENT_FILES[0]), VersionBomComponentView.class);
        final VersionBomComponentView component1 = hubResponseService.getItemAs(SonarTestUtils.getJsonFromFile(SonarTestUtils.JSON_COMPONENT_FILES[1]), VersionBomComponentView.class);
        vulnerableComponentsMap.put(file1, Sets.newHashSet(component0, component1));

        final List<InputFile> inputFiles = Arrays.asList(TestInputFileBuilder.create(SonarTestUtils.MY_PROJECT_KEY, file1).build());

        final SensorContextTester context = SensorContextTester.create(BASE_DIR);
        final MetricsHelper metricsHelper = new MetricsHelper(LOG, context);
        metricsHelper.createMeasuresForInputFiles(vulnerableComponentsMap, inputFiles);

        final Measure<Integer> numVulnLow = context.measure(componentKey1, HubSonarMetrics.NUM_VULN_LOW);
        final Measure<Integer> numVulnMed = context.measure(componentKey1, HubSonarMetrics.NUM_VULN_MED);
        final Measure<Integer> numVulnHigh = context.measure(componentKey1, HubSonarMetrics.NUM_VULN_HIGH);
        final Measure<String> componentNames = context.measure(componentKey1, HubSonarMetrics.COMPONENT_NAMES);
        final Measure<Integer> numComponentsTotal = context.measure(componentKey1, HubSonarMetrics.NUM_COMPONENTS);

        assertEquals(numVulnLow.value().intValue(), 1);
        assertEquals(numVulnMed.value().intValue(), 1);
        assertEquals(numVulnHigh.value().intValue(), 1);
        assertEquals(numComponentsTotal.value().intValue(), 2);

        final String compNames = componentNames.value();
        assertTrue(compNames.contains("Test Component 0") && compNames.contains("Test Component 1"));
    }
}
