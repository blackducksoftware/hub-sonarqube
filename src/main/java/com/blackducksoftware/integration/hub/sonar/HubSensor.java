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
package com.blackducksoftware.integration.hub.sonar;

import java.util.List;

import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.utils.log.Loggers;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.sonar.component.ComponentComparer;
import com.blackducksoftware.integration.hub.sonar.component.ComponentHelper;
import com.blackducksoftware.integration.hub.sonar.component.HubVulnerableComponentGatherer;
import com.blackducksoftware.integration.hub.sonar.component.LocalComponentGatherer;
import com.blackducksoftware.integration.hub.sonar.manager.HubManager;
import com.blackducksoftware.integration.hub.sonar.manager.SonarManager;
import com.blackducksoftware.integration.hub.sonar.metrics.HubSonarMetrics;
import com.blackducksoftware.integration.hub.sonar.metrics.MetricsHelper;

public class HubSensor implements Sensor {
    @Override
    public void describe(final SensorDescriptor descriptor) {
        descriptor.name(HubPlugin.PLUGIN_NAME);
        descriptor.onlyOnFileType(InputFile.Type.MAIN);
    }

    @Override
    public void execute(final SensorContext context) {
        final HubSonarLogger logger = new HubSonarLogger(Loggers.get(context.getClass()));
        final SonarManager sonarManager = new SonarManager(context.settings());
        final ComponentHelper componentHelper = new ComponentHelper(sonarManager);
        final RestConnection restConnection = createRestConnection(logger, sonarManager.getHubServerConfigFromSettings());
        final HubManager hubManager = new HubManager(logger, restConnection);
        final FileSystem fileSystem = context.fileSystem();
        final FilePredicates filePredicates = fileSystem.predicates();
        final FilePredicate filePredicate = filePredicates.and(filePredicates.matchesPathPatterns(sonarManager.getGlobalInclusionPatterns()), filePredicates.doesNotMatchPathPatterns(sonarManager.getGlobalExclusionPatterns()));

        logger.info("=============================");
        logger.info("|| Black Duck Hub Analysis ||");
        logger.info("=============================");

        logger.info("Gathering local component files...");
        final LocalComponentGatherer localComponentGatherer = new LocalComponentGatherer(logger, sonarManager, fileSystem, filePredicate);
        final List<String> localComponents = localComponentGatherer.gatherComponents();

        logger.info("Gathering Hub component files...");
        final HubVulnerableComponentGatherer hubComponentGatherer = new HubVulnerableComponentGatherer(logger, componentHelper, sonarManager, hubManager);
        final List<String> hubComponents = hubComponentGatherer.gatherComponents();

        logger.info(String.format("--> Number of local component files matched: %d", localComponents.size()));
        logger.info(String.format("--> Number of vulnerable Hub component files matched: %d", hubComponents.size()));

        ComponentComparer componentComparer = null;
        List<String> sharedComponents = null;
        if (localComponents.isEmpty() || hubComponents.isEmpty()) {
            logger.info("No comparison will be performed because at least one of the lists of components had zero entries.");
        } else {
            componentComparer = new ComponentComparer(componentHelper, localComponents, hubComponents);
            try {
                logger.info("Comparing local components to Hub components...");
                sharedComponents = componentComparer.getSharedComponents();
                logger.info(String.format("--> Number of shared components: %d", componentComparer.getSharedComponentCount()));

                logger.debug("Shared Components:");
                for (final String sharedComponent : sharedComponents) {
                    logger.debug(sharedComponent);
                }

                // TODO store shared component data

            } catch (final IntegrationException e) {
                logger.error("Could not get shared components.", e);
            }
        }

        // TODO move it to the else statement (above) when complete
        final MetricsHelper metricsHelper = new MetricsHelper(context);
        for (final InputFile file : fileSystem.inputFiles(filePredicate)) {
            final int numComponents = 5; // TODO get this number from the hub
            metricsHelper.createMeasure(HubSonarMetrics.NUM_COMPONENTS, file, numComponents);
        }
    }

    private RestConnection createRestConnection(final HubSonarLogger logger, final HubServerConfig hubServerConfig) {
        RestConnection restConnection = null;
        try {
            restConnection = hubServerConfig.createCredentialsRestConnection(logger);
            restConnection.connect();
            logger.info(String.format("Successfully connected to %s", hubServerConfig.getHubUrl()));
        } catch (final IntegrationException e) {
            logger.error(String.format("Error connecting to %s: ", hubServerConfig.getHubUrl(), e));
        }
        return restConnection;
    }
}
