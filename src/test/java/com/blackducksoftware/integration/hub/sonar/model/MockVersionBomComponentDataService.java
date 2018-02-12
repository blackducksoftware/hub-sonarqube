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
package com.blackducksoftware.integration.hub.sonar.model;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.dataservice.versionbomcomponent.VersionBomComponentDataService;
import com.blackducksoftware.integration.hub.dataservice.versionbomcomponent.model.VersionBomComponentModel;
import com.blackducksoftware.integration.hub.model.view.MatchedFilesView;
import com.blackducksoftware.integration.hub.model.view.VersionBomComponentView;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubResponseService;
import com.blackducksoftware.integration.hub.sonar.SonarTestUtils;

public class MockVersionBomComponentDataService extends VersionBomComponentDataService {
    private final RestConnection restConnection;

    private List<MatchedFilesView> matchedFiles0;
    private List<MatchedFilesView> matchedFiles1;
    private boolean makeEmpty;

    public MockVersionBomComponentDataService(final RestConnection restConnection) {
        super(null, null, null, null, null);
        this.restConnection = restConnection;

        matchedFiles0 = Collections.emptyList();
        matchedFiles1 = Collections.emptyList();
        makeEmpty = false;
    }

    @Override
    public List<VersionBomComponentModel> getComponentsForProjectVersion(final String projectName, final String projectVersionName) throws IntegrationException {
        if (makeEmpty) {
            return Collections.emptyList();
        }
        final HubResponseService hubResponseService = new HubResponseService(restConnection);
        VersionBomComponentView component0;
        VersionBomComponentView component1;
        try {
            component0 = hubResponseService.getItemAs(SonarTestUtils.getJsonFromFile(SonarTestUtils.getJsonComponentFileNames()[0]), VersionBomComponentView.class);
            component1 = hubResponseService.getItemAs(SonarTestUtils.getJsonFromFile(SonarTestUtils.getJsonComponentFileNames()[1]), VersionBomComponentView.class);
        } catch (final IOException e) {
            throw new MockException(e);
        }
        return Arrays.asList(new VersionBomComponentModel(component0, matchedFiles0), new VersionBomComponentModel(component1, matchedFiles1));
    }

    public void setMatchedFiles(final List<MatchedFilesView> matchedFiles0, final List<MatchedFilesView> matchedFiles1) {
        this.matchedFiles0 = matchedFiles0;
        this.matchedFiles1 = matchedFiles1;
    }

    public void setEmpty(final boolean makeEmpty) {
        this.makeEmpty = makeEmpty;
    }
}
