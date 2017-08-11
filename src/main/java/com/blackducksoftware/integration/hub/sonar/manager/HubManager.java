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
package com.blackducksoftware.integration.hub.sonar.manager;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.api.vulnerablebomcomponent.VulnerableBomComponentRequestService;
import com.blackducksoftware.integration.hub.dataservice.project.ProjectDataService;
import com.blackducksoftware.integration.hub.dataservice.project.ProjectVersionWrapper;
import com.blackducksoftware.integration.hub.request.HubRequestFactory;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubResponseService;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.hub.sonar.HubSonarLogger;
import com.blackducksoftware.integration.hub.sonar.service.MatchedFilesRequestService;

public class HubManager {
    private final HubSonarLogger logger;
    private final RestConnection restConnection;

    private HubRequestFactory requestFactory = null;
    private HubServicesFactory servicesFactory = null;
    private HubResponseService responseService = null;
    private ProjectDataService projectDataService = null;
    private MatchedFilesRequestService matchedFilesRequestService = null;
    private VulnerableBomComponentRequestService vulnerableBomComponentRequestService = null;
    private MetaService metaService = null;

    public HubManager(final HubSonarLogger logger, final RestConnection restConnection) {
        this.restConnection = restConnection;
        this.logger = logger;
    }

    public RestConnection getRestConnection() {
        return restConnection;
    }

    public HubResponseService getResponseService() {
        if (responseService == null) {
            responseService = getServicesFactory().createHubResponseService();
        }
        return responseService;
    }

    public HubRequestFactory getRequestFactory() {
        if (requestFactory == null) {
            requestFactory = new HubRequestFactory(restConnection);
        }
        return requestFactory;
    }

    public MetaService getMetaService() {
        if (metaService == null) {
            metaService = getServicesFactory().createMetaService(logger);
        }
        return metaService;
    }

    public ProjectVersionWrapper getProjectVersionWrapper(final String hubProjectName, final String hubProjectVersionName) {
        ProjectVersionWrapper wrapper = null;
        try {
            wrapper = getProjectDataService().getProjectVersion(hubProjectName, hubProjectVersionName);
        } catch (final IntegrationException e) {
            logger.error(e);
        }
        return wrapper;
    }

    public VulnerableBomComponentRequestService getVulnerableBomComponentRequestService() {
        if (vulnerableBomComponentRequestService == null) {
            vulnerableBomComponentRequestService = getServicesFactory().createVulnerableBomComponentRequestService();
        }
        return vulnerableBomComponentRequestService;
    }

    public MatchedFilesRequestService getMatchedFilesRequestService() {
        if (matchedFilesRequestService == null) {
            matchedFilesRequestService = new MatchedFilesRequestService(restConnection);
        }
        return matchedFilesRequestService;
    }

    private ProjectDataService getProjectDataService() {
        if (projectDataService == null) {
            projectDataService = getServicesFactory().createProjectDataService(logger);
        }
        return projectDataService;
    }

    private HubServicesFactory getServicesFactory() {
        if (servicesFactory == null) {
            servicesFactory = new HubServicesFactory(restConnection);
        }
        return servicesFactory;
    }
}
