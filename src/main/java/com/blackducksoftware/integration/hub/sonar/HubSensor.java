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

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.utils.log.Loggers;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.sonar.component.ComponentComparer;
import com.blackducksoftware.integration.hub.sonar.component.HubVulnerableComponentGatherer;
import com.blackducksoftware.integration.hub.sonar.component.LocalComponentGatherer;

public class HubSensor implements Sensor {

    @Override
    public void describe(final SensorDescriptor descriptor) {
        descriptor.name(HubPlugin.PLUGIN_NAME);
        descriptor.onlyOnFileType(InputFile.Type.MAIN);
    }

    @Override
    public void execute(final SensorContext context) {
        final HubSonarLogger logger = new HubSonarLogger(Loggers.get(context.getClass()));
        HubSonarUtils.setSettings(context.settings());
        logger.info("=============================");
        logger.info("|| Black Duck Hub Analysis ||");
        logger.info("=============================");

        logger.info("Gathering local component files...");
        final LocalComponentGatherer localComponentGatherer = new LocalComponentGatherer(logger, context);
        final List<String> localComponents = localComponentGatherer.gatherComponents();

        logger.info("Gathering Hub component files...");
        final HubVulnerableComponentGatherer hubComponentGatherer = new HubVulnerableComponentGatherer(logger, context.settings());
        final List<String> hubComponents = hubComponentGatherer.gatherComponents();

        logger.info(String.format("--> Number of local component files matched: %d", localComponents.size()));
        logger.info(String.format("--> Number of vulnerable Hub component files matched: %d", hubComponents.size()));

        ComponentComparer componentComparer = null;
        List<String> sharedComponents = null;
        if (localComponents.isEmpty() || hubComponents.isEmpty()) {
            logger.info("No comparison will be performed because at least one of the lists of components had zero entries.");
        } else {
            componentComparer = new ComponentComparer(logger, localComponents, hubComponents);
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
    }

}
