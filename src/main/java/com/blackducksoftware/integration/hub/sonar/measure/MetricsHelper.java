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
package com.blackducksoftware.integration.hub.sonar.measure;

import java.io.File;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.sonar.api.batch.fs.InputComponent;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.ce.measure.Component;
import org.sonar.api.ce.measure.Measure;
import org.sonar.api.ce.measure.MeasureComputer.MeasureComputerContext;
import org.sonar.api.measures.Metric;

import com.blackducksoftware.integration.hub.model.view.VulnerableComponentView;
import com.blackducksoftware.integration.hub.sonar.HubSonarLogger;

public class MetricsHelper {
    private final SensorContext context;
    private final HubSonarLogger logger;

    public MetricsHelper(final HubSonarLogger logger, final SensorContext context) {
        this.logger = logger;
        this.context = context;
    }

    public void createMeasuresForVulnerableComponents(final Map<String, Set<VulnerableComponentView>> vulnerableComponentsMap, final Iterable<InputFile> inputFiles) {
        for (final InputFile inputFile : inputFiles) {
            final File actualFile = inputFile.file();
            if (actualFile != null) {
                final String[] fileTokens = actualFile.getName().split("/");
                final String fileName = fileTokens[fileTokens.length - 1];
                if (vulnerableComponentsMap.containsKey(fileName)) {
                    int numComponents = 0;
                    int high = 0;
                    int med = 0;
                    int low = 0;
                    for (final VulnerableComponentView vulnerableComponent : vulnerableComponentsMap.get(fileName)) {
                        // TODO replace mock data
                        high = 55;
                        med = 55;
                        low = 55;
                        numComponents++;
                    }
                    createMeasure(HubSonarMetrics.NUM_VULN_HIGH, inputFile, high);
                    createMeasure(HubSonarMetrics.NUM_VULN_MED, inputFile, med);
                    createMeasure(HubSonarMetrics.NUM_VULN_LOW, inputFile, low);
                    createMeasure(HubSonarMetrics.COMPONENT_NAMES, inputFile, "Test"); // TODO make sure this works
                    createMeasure(HubSonarMetrics.NUM_COMPONENTS, inputFile, numComponents);
                    // final List<String> list = new ArrayList<>();
                    // for (final VulnerableComponentView comp : vulnerableComponentsMap.get(fileName)) {
                    // list.add(comp.componentName);
                    // }
                    // createMeasure(HubSonarMetrics.NUM_COMPONENTS, inputFile, list.toArray().toString());
                }
            }
        }
    }

    public void createMeasure(@SuppressWarnings("rawtypes") final Metric metric, final InputComponent inputComponent, final Serializable value) {
        logger.debug(String.format("Creating measure: Metric='%s', Component='%s', Value='%s'", metric.getName(), inputComponent, value));
        context.newMeasure().forMetric(metric).on(inputComponent).withValue(value).save();
    }

    public static void computeSecurityVulnerabilityMeasure(final MeasureComputerContext context, final Metric<Integer> metric) {
        if (context.getComponent().getType() != Component.Type.FILE) {
            final String metricKey = metric.getKey();
            int sum = 0;
            int count = 0;
            for (final Measure child : context.getChildrenMeasures(metricKey)) {
                sum += child.getIntValue();
                count++;
            }
            final int average = count == 0 ? 0 : sum / count;
            context.addMeasure(metricKey, average);
        }
    }
}
