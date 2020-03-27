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
package com.blackducksoftware.integration.hub.sonar.metric;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

import org.sonar.api.batch.fs.InputComponent;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.measures.Metric;

import com.synopsys.integration.blackduck.api.generated.component.ComponentVersionRiskProfileRiskDataCountsView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentView;
import com.synopsys.integration.blackduck.api.generated.view.RiskProfileView;
import com.synopsys.integration.log.IntLogger;

public class MetricsHelper {
    private static final int MAX_COMPONENT_NAME_LENGTH = 100;

    private final SensorContext context;
    private final IntLogger logger;

    public MetricsHelper(IntLogger logger, SensorContext context) {
        this.logger = logger;
        this.context = context;
    }

    public void createMeasuresForInputFiles(Map<String, Set<ProjectVersionComponentView>> vulnerableComponentsMap, Iterable<InputFile> inputFiles) {
        for (InputFile inputFile : inputFiles) {
            createMeasuresForInputFile(vulnerableComponentsMap, inputFile);
        }
    }

    public void createMeasuresForInputFile(Map<String, Set<ProjectVersionComponentView>> vulnerableComponentsMap, InputFile inputFile) {
        String fileName = inputFile.filename();
        if (vulnerableComponentsMap.containsKey(fileName)) {
            StringBuilder compListBuilder = new StringBuilder();
            BigDecimal critical = new BigDecimal(0);
            BigDecimal high = new BigDecimal(0);
            BigDecimal med = new BigDecimal(0);
            BigDecimal low = new BigDecimal(0);
            for (ProjectVersionComponentView component : vulnerableComponentsMap.get(fileName)) {
                String compName = component.getComponentName();
                String compVersion = component.getComponentVersionName();
                String compVersionUrl = component.getComponentVersion();
                if (compName.length() > MAX_COMPONENT_NAME_LENGTH) {
                    compName = compName.substring(0, MAX_COMPONENT_NAME_LENGTH) + "...";
                }
                compListBuilder.append(compName + ",");
                compListBuilder.append(compVersion + ",");
                compListBuilder.append(compVersionUrl + ",");

                RiskProfileView riskProfile = component.getSecurityRiskProfile();
                for (ComponentVersionRiskProfileRiskDataCountsView countView : riskProfile.getCounts()) {
                    switch (countView.getCountType()) {
                        case CRITICAL:
                            critical.add(countView.getCount());
                            break;
                        case HIGH:
                            high.add(countView.getCount());
                            break;
                        case MEDIUM:
                            med.add(countView.getCount());
                            break;
                        case LOW:
                            low.add(countView.getCount());
                            break;
                        default:
                            break;
                    }

                }

            }
            createMeasure(HubSonarMetrics.NUM_VULN_LOW, inputFile, low);
            createMeasure(HubSonarMetrics.NUM_VULN_MED, inputFile, med);
            createMeasure(HubSonarMetrics.NUM_VULN_HIGH, inputFile, high);
            createMeasure(HubSonarMetrics.NUM_VULN_CRITICAL, inputFile, critical);

            if (low.compareTo(BigDecimal.ZERO) > 0 || med.compareTo(BigDecimal.ZERO) > 0 || high.compareTo(BigDecimal.ZERO) > 0) {
                String compList = compListBuilder.toString();
                compListBuilder.deleteCharAt(compList.lastIndexOf(','));
                compList = compListBuilder.toString();
                createMeasure(HubSonarMetrics.COMPONENT_NAMES, inputFile, compList.trim());
            }
        }
    }

    public void createMeasure(@SuppressWarnings("rawtypes") Metric metric, InputComponent inputComponent, Serializable value) {
        logger.debug(String.format("Creating measure: Metric='%s', Component='%s', Value='%s'", metric.getName(), inputComponent, value));
        context.newMeasure().forMetric(metric).on(inputComponent).withValue(value).save();
    }
}
