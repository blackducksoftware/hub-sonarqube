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

import java.io.File;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.sonar.api.batch.fs.InputComponent;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.measures.Metric;

import com.blackducksoftware.integration.hub.dataservice.model.RiskProfileCounts;
import com.blackducksoftware.integration.hub.dataservice.versionbomcomponent.model.VersionBomComponentModel;
import com.blackducksoftware.integration.hub.model.enumeration.RiskCountEnum;
import com.blackducksoftware.integration.log.IntLogger;

public class MetricsHelper {
    private static final int MAX_COMPONENT_NAME_LENGTH = 100;

    private final SensorContext context;
    private final IntLogger logger;

    public MetricsHelper(final IntLogger logger, final SensorContext context) {
        this.logger = logger;
        this.context = context;
    }

    public void createMeasuresForInputFiles(final Map<String, Set<VersionBomComponentModel>> vulnerableComponentsMap, final Iterable<InputFile> inputFiles) {
        for (final InputFile inputFile : inputFiles) {
            createMeasuresForInputFile(vulnerableComponentsMap, inputFile);
        }
    }

    public void createMeasuresForInputFile(final Map<String, Set<VersionBomComponentModel>> vulnerableComponentsMap, final InputFile inputFile) {
        final File actualFile = inputFile.file();
        if (actualFile != null) {
            final String[] fileTokens = actualFile.getName().split("/");
            final String fileName = fileTokens[fileTokens.length - 1];
            if (vulnerableComponentsMap.containsKey(fileName)) {
                final StringBuilder compListBuilder = new StringBuilder();
                int high = 0;
                int med = 0;
                int low = 0;
                for (final VersionBomComponentModel component : vulnerableComponentsMap.get(fileName)) {
                    String compName = component.getComponentName();
                    final String compVersion = component.getComponentVersionName();
                    final String compVersionUrl = component.getComponentVersion();
                    if (compName.length() > MAX_COMPONENT_NAME_LENGTH) {
                        compName = compName.substring(0, MAX_COMPONENT_NAME_LENGTH) + "...";
                    }
                    compListBuilder.append(compName + ",");
                    compListBuilder.append(compVersion + ",");
                    compListBuilder.append(compVersionUrl + ",");

                    final RiskProfileCounts riskProfile = component.getSecurityRiskProfile();
                    high += riskProfile.getCount(RiskCountEnum.HIGH);
                    med += riskProfile.getCount(RiskCountEnum.MEDIUM);
                    low += riskProfile.getCount(RiskCountEnum.LOW);
                }
                createMeasure(HubSonarMetrics.NUM_VULN_LOW, inputFile, low);
                createMeasure(HubSonarMetrics.NUM_VULN_MED, inputFile, med);
                createMeasure(HubSonarMetrics.NUM_VULN_HIGH, inputFile, high);
                if ((low + med + high) > 0) {
                    String compList = compListBuilder.toString();
                    compListBuilder.deleteCharAt(compList.lastIndexOf(','));
                    compList = compListBuilder.toString();
                    createMeasure(HubSonarMetrics.COMPONENT_NAMES, inputFile, compList.trim());
                }
            }
        }
    }

    public void createMeasure(@SuppressWarnings("rawtypes") final Metric metric, final InputComponent inputComponent, final Serializable value) {
        logger.debug(String.format("Creating measure: Metric='%s', Component='%s', Value='%s'", metric.getName(), inputComponent, value));
        context.newMeasure().forMetric(metric).on(inputComponent).withValue(value).save();
    }
}
