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
package com.blackducksoftware.integration.hub.sonar;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.utils.log.Loggers;

import com.blackducksoftware.integration.hub.sonar.component.ComponentComparer;
import com.blackducksoftware.integration.hub.sonar.component.ComponentHelper;
import com.blackducksoftware.integration.hub.sonar.component.HubVulnerableComponentGatherer;
import com.blackducksoftware.integration.hub.sonar.component.LocalComponentGatherer;
import com.blackducksoftware.integration.hub.sonar.manager.SonarManager;
import com.blackducksoftware.integration.hub.sonar.metric.MetricsHelper;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentView;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.blackduck.phonehome.BlackDuckPhoneHomeHelper;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.phonehome.PhoneHomeResponse;

public class HubSensor implements Sensor {
    public static final String ARTIFACT_ID = "blackduck-sonarqube";

    @Override
    public void describe(SensorDescriptor descriptor) {
        descriptor.name(HubPlugin.PLUGIN_NAME);
        descriptor.onlyOnFileType(InputFile.Type.MAIN);
    }

    @Override
    public void execute(SensorContext context) {
        HubSonarLogger logger = new HubSonarLogger(Loggers.get(context.getClass()));
        SonarManager sonarManager = new SonarManager(context);
        ComponentHelper componentHelper = new ComponentHelper(sonarManager);
        Optional<BlackDuckServicesFactory> servicesFactoryOptional = createServiceFactory(logger, sonarManager);
        if (!servicesFactoryOptional.isPresent()) {
            logger.warn("No connection to the Black Duck server could be established, skipping the Black Duck Hub Sensor.");
            return;
        }
        BlackDuckServicesFactory blackDuckServicesFactory = servicesFactoryOptional.get();
        Optional<PhoneHomeResponse> phoneHomeResponseOptional = phoneHome(logger, blackDuckServicesFactory, sonarManager);
        try {
            FileSystem fileSystem = context.fileSystem();
            FilePredicates filePredicates = fileSystem.predicates();
            FilePredicate filePredicate = generateFilePredicateFromInclusionAndExclusionPatterns(sonarManager, filePredicates);

            logger.info("Gathering local component files...");
            LocalComponentGatherer localComponentGatherer = new LocalComponentGatherer(logger, sonarManager, fileSystem, filePredicate);
            Set<String> localComponents = localComponentGatherer.gatherComponents();

            logger.info("Gathering Black Duck component files...");
            HubVulnerableComponentGatherer hubComponentGatherer = new HubVulnerableComponentGatherer(logger, componentHelper, sonarManager, blackDuckServicesFactory.createProjectService(), blackDuckServicesFactory.createBlackDuckService());
            Set<String> blackDuckComponents = hubComponentGatherer.gatherComponents();

            logger.info(String.format("--> Number of local files matching inclusion/exclusion patterns: %d", localComponents.size()));
            logger.info(String.format("--> Number of vulnerable Black Duck component files matched: %d", blackDuckComponents.size()));

            if (localComponents.isEmpty() || blackDuckComponents.isEmpty()) {
                logger.info("No comparison will be performed because at least one of the lists of components had zero entries.");
                return;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Local Components:");
                for (String localComponent : localComponents) {
                    logger.debug(localComponent);
                }
                logger.debug("Black Duck Components:");
                for (String blackDuckComponent : blackDuckComponents) {
                    logger.debug(blackDuckComponent);
                }
            }
            ComponentComparer componentComparer = new ComponentComparer(componentHelper, localComponents, blackDuckComponents);

            logger.info("Comparing local components to Black Duck components...");
            Set<String> sharedComponents = componentComparer.getSharedComponents();
            logger.info(String.format("--> Number of shared components: %d", componentComparer.getSharedComponentCount()));

            if (logger.isDebugEnabled()) {
                logger.debug("Shared Components:");
                for (String sharedComponent : sharedComponents) {
                    logger.debug(sharedComponent);
                }
            }
            MetricsHelper metricsHelper = new MetricsHelper(logger, context);
            Map<String, Set<ProjectVersionComponentView>> vulnerableComponentsMap = hubComponentGatherer.getVulnerableComponentMap();
            if (vulnerableComponentsMap != null && !vulnerableComponentsMap.isEmpty()) {
                metricsHelper.createMeasuresForInputFiles(vulnerableComponentsMap, componentHelper.getInputFilesFromStrings(sharedComponents));
            }

        } finally {
            phoneHomeResponseOptional.ifPresent(phoneHomeResponse -> phoneHomeResponse.awaitResult(10));
        }
    }

    private FilePredicate generateFilePredicateFromInclusionAndExclusionPatterns(SonarManager sonarManager, FilePredicates filePredicates) {
        String[] globalInclusionPatterns = sonarManager.getGlobalInclusionPatterns();
        String[] globalExclusionPatterns = sonarManager.getGlobalExclusionPatterns();
        if (!isStringArrayEmpty(globalInclusionPatterns) && !isStringArrayEmpty(globalExclusionPatterns)) {
            return filePredicates.and(filePredicates.matchesPathPatterns(globalInclusionPatterns), filePredicates.doesNotMatchPathPatterns(globalExclusionPatterns));
        }
        return filePredicates.all();
    }

    private boolean isStringArrayEmpty(String[] array) {
        if (array != null) {
            return array.length == 0 || (array.length > 0 && "".equals(array[0]));
        }
        return true;
    }

    private Optional<BlackDuckServicesFactory> createServiceFactory(IntLogger logger, SonarManager sonarManager) {
        try {
            Optional<BlackDuckServerConfig> config = sonarManager.getBlackDuckServerConfigFromSettings();
            if (config.isPresent()) {
                BlackDuckServerConfig blackDuckServerConfig = config.get();
                blackDuckServerConfig.attemptConnection(logger);
                logger.info(String.format("Successfully connected to %s", blackDuckServerConfig.getBlackDuckUrl()));
                BlackDuckServicesFactory blackDuckServicesFactory = blackDuckServerConfig.createBlackDuckServicesFactory(logger);
                return Optional.of(blackDuckServicesFactory);
            }
        } catch (Exception e) {
            logger.error(String.format("Error establishing a Black Duck connection: %s", e.getMessage()), e);
        }
        return Optional.empty();
    }

    private Optional<PhoneHomeResponse> phoneHome(IntLogger logger, BlackDuckServicesFactory blackDuckServicesFactory, SonarManager sonarManager) {
        try {
            ExecutorService phoneHomeExecutor = Executors.newSingleThreadExecutor();
            BlackDuckPhoneHomeHelper blackDuckPhoneHomeHelper = BlackDuckPhoneHomeHelper.createAsynchronousPhoneHomeHelper(blackDuckServicesFactory, phoneHomeExecutor);
            return Optional.of(blackDuckPhoneHomeHelper.handlePhoneHome(ARTIFACT_ID, sonarManager.getHubPluginVersionFromFile("/plugin.properties")));
        } catch (Exception e) {
            logger.debug(String.format("Could not send the phone home data. Error: %s", e.getMessage()));
            logger.trace(e.getMessage(), e);
        }
        return Optional.empty();
    }
}
