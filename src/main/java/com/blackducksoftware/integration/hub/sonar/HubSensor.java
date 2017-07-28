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

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
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
import com.blackducksoftware.integration.hub.model.view.VulnerableComponentView;
import com.blackducksoftware.integration.hub.sonar.data.HubVulnerableComponentData;

public class HubSensor implements Sensor {

    final String[] inclusionPatterns = { "**/*.jar", "**/*.war", "**/*.zip", "**/*.tar*" };
    final String[] exclusionPatterns = { "**/WEB-INF/**/*.jar", "**/test-workspace/**/*.jar" };

    @Override
    public void describe(final SensorDescriptor descriptor) {
        descriptor.name(HubPlugin.PLUGIN_NAME);
        descriptor.onlyOnFileType(InputFile.Type.MAIN);
    }

    @Override
    public void execute(final SensorContext context) {
        final HubSonarLogger logger = new HubSonarLogger(Loggers.get(context.getClass()));
        final FileSystem fileSystem = context.fileSystem();
        final FilePredicates filePredicates = fileSystem.predicates();
        final FilePredicate includeExcludePredicate = filePredicates.and(filePredicates.matchesPathPatterns(inclusionPatterns), filePredicates.doesNotMatchPathPatterns(exclusionPatterns));

        logger.info("=============================");
        logger.info("|| Black Duck Hub Analysis ||");
        logger.info("=============================");
        // logger.info(String.format("PROP: %s", HubSonarUtils.getAndTrimProp(context.settings(), HubPropertyConstants.PROP)));

        // TODO Locate jar files
        int localComponentCount = 0;

        logger.info(String.format("Found Base Directory: %s", fileSystem.baseDir().toString()));

        final Iterator<File> fileIterator = fileSystem.files(includeExcludePredicate).iterator();
        while (fileIterator.hasNext()) {
            localComponentCount++;
            final File file = fileIterator.next();
            try {
                logger.info(String.format("Found File: %s", file.getCanonicalPath()));
            } catch (final IOException e) {
                logger.warn(String.format("Problem getting canonical path for: %s", file.getName()));
            }
        }

        logger.info(String.format("--> Number of Local Components: %d", localComponentCount));

        // TODO Store metadata

        // TODO Get Hub Project/Version Components and Matched Files
        final HubVulnerableComponentData hubData = new HubVulnerableComponentData(logger, context.settings());
        List<VulnerableComponentView> components = null;
        try {
            components = hubData.gatherVulnerableComponents();
        } catch (final IntegrationException e) {
            // TODO handle
        }

        if (components != null) {
            logger.info(String.format("--> Number of Vulnerable Hub Components: %d", components.size()));
        } else {
            logger.info("--> No Vulnerable Hub components found.");
        }

        // TODO Compare with Hub Project/Version Components
    }

}
