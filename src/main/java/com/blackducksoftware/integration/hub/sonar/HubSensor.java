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

import java.util.Map;
import java.util.Set;

import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.utils.log.Loggers;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.dataservice.versionbomcomponent.model.VersionBomComponentModel;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.hub.sonar.component.ComponentComparer;
import com.blackducksoftware.integration.hub.sonar.component.ComponentHelper;
import com.blackducksoftware.integration.hub.sonar.component.HubVulnerableComponentGatherer;
import com.blackducksoftware.integration.hub.sonar.component.LocalComponentGatherer;
import com.blackducksoftware.integration.hub.sonar.manager.SonarManager;
import com.blackducksoftware.integration.hub.sonar.metric.MetricsHelper;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.phonehome.enums.ThirdPartyName;

public class HubSensor implements Sensor {
    @Override
    public void describe(final SensorDescriptor descriptor) {
        descriptor.name(HubPlugin.PLUGIN_NAME);
        descriptor.onlyOnFileType(InputFile.Type.MAIN);
    }

    @Override
    public void execute(final SensorContext context) {
        final HubSonarLogger logger = new HubSonarLogger(Loggers.get(context.getClass()));
        final SonarManager sonarManager = new SonarManager(context);
        final ComponentHelper componentHelper = new ComponentHelper(sonarManager);
        final RestConnection restConnection = createRestConnection(logger, sonarManager);
        if (restConnection == null) {
            logger.warn("No connection to the Hub server could be established, skipping Black Duck Hub Sensor.");
            return;
        }
        final HubServicesFactory hubServicesFactory = new HubServicesFactory(restConnection);
        hubServicesFactory.createPhoneHomeDataService().phoneHome(ThirdPartyName.SONARQUBE, context.getSonarQubeVersion().toString(), sonarManager.getHubPluginVersionFromFile("/plugin.properties"));

        final FileSystem fileSystem = context.fileSystem();
        final FilePredicates filePredicates = fileSystem.predicates();
        final FilePredicate filePredicate = generateFilePredicateFromInclusionAndExclusionPatterns(sonarManager, filePredicates);

        logger.info("Gathering local component files...");
        final LocalComponentGatherer localComponentGatherer = new LocalComponentGatherer(logger, sonarManager, fileSystem, filePredicate);
        final Set<String> localComponents = localComponentGatherer.gatherComponents();

        logger.info("Gathering Hub component files...");
        final HubVulnerableComponentGatherer hubComponentGatherer = new HubVulnerableComponentGatherer(logger, componentHelper, sonarManager, hubServicesFactory.createVersionBomComponentDataservice());
        final Set<String> hubComponents = hubComponentGatherer.gatherComponents();

        logger.info(String.format("--> Number of local files matching inclusion/exclusion patterns: %d", localComponents.size()));
        logger.info(String.format("--> Number of vulnerable Hub component files matched: %d", hubComponents.size()));

        ComponentComparer componentComparer = null;
        Set<String> sharedComponents = null;
        if (localComponents.isEmpty() || hubComponents.isEmpty()) {
            logger.info("No comparison will be performed because at least one of the lists of components had zero entries.");
        } else {
            componentComparer = new ComponentComparer(componentHelper, localComponents, hubComponents);

            logger.info("Comparing local components to Hub components...");
            sharedComponents = componentComparer.getSharedComponents();
            logger.info(String.format("--> Number of shared components: %d", componentComparer.getSharedComponentCount()));

            if (logger.isDebugEnabled()) {
                logger.debug("Shared Components:");
                for (final String sharedComponent : sharedComponents) {
                    logger.debug(sharedComponent);
                }
            }
            final MetricsHelper metricsHelper = new MetricsHelper(logger, context);
            final Map<String, Set<VersionBomComponentModel>> vulnerableComponentsMap = hubComponentGatherer.getVulnerableComponentMap();
            if (vulnerableComponentsMap != null && !vulnerableComponentsMap.isEmpty()) {
                metricsHelper.createMeasuresForInputFiles(vulnerableComponentsMap, componentHelper.getInputFilesFromStrings(sharedComponents));
            }
        }
    }

    private FilePredicate generateFilePredicateFromInclusionAndExclusionPatterns(final SonarManager sonarManager, final FilePredicates filePredicates) {
        final String[] globalInclusionPatterns = sonarManager.getGlobalInclusionPatterns();
        final String[] globalExclusionPatterns = sonarManager.getGlobalExclusionPatterns();
        if (!isStringArrayEmpty(globalInclusionPatterns) && !isStringArrayEmpty(globalExclusionPatterns)) {
            return filePredicates.and(filePredicates.matchesPathPatterns(globalInclusionPatterns), filePredicates.doesNotMatchPathPatterns(globalExclusionPatterns));
        }
        return filePredicates.all();
    }

    private boolean isStringArrayEmpty(final String[] array) {
        return array != null && array.length > 0 && !"".equals(array[0]);
    }

    private RestConnection createRestConnection(final IntLogger logger, final SonarManager sonarManager) {
        RestConnection restConnection = null;
        try {
            final HubServerConfig config = sonarManager.getHubServerConfigFromSettings();
            restConnection = config.createCredentialsRestConnection(logger);
            restConnection.connect();
            logger.info(String.format("Successfully connected to %s", config.getHubUrl()));
        } catch (final IllegalStateException | NullPointerException | IntegrationException e) {
            logger.error(String.format("Error establishing a Hub connection: %s", e.getMessage()));
            return null;
        }
        return restConnection;
    }
}
