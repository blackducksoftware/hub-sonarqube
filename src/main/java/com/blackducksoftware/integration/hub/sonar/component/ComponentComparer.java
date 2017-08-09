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
package com.blackducksoftware.integration.hub.sonar.component;

import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.sonar.HubSonarLogger;
import com.blackducksoftware.integration.hub.sonar.HubSonarUtils;

public class ComponentComparer {

    private final List<String> localComponentList;
    private final List<String> remoteComponentList;

    private int sharedComponentCount;
    private final boolean needsValidation;
    private final HubSonarLogger logger;

    public ComponentComparer(final HubSonarLogger logger, final ComponentGatherer localComponentGatherer, final ComponentGatherer remoteComponentGatherer) {
        this.logger = logger;
        this.localComponentList = localComponentGatherer.gatherComponents();
        this.remoteComponentList = remoteComponentGatherer.gatherComponents();
        this.sharedComponentCount = -1;
        this.needsValidation = false;
    }

    public ComponentComparer(final HubSonarLogger logger, final List<String> localComponentList, final List<String> remoteComponentList) {
        this.logger = logger;
        this.localComponentList = localComponentList;
        this.remoteComponentList = remoteComponentList;
        this.sharedComponentCount = -1;
        this.needsValidation = true;
    }

    public List<String> getSharedComponents() throws IntegrationException {
        if (needsValidation) {
            preProcessListData(localComponentList);
            preProcessListData(remoteComponentList);
        }
        final List<String> sharedComponents = new ArrayList<>();

        // TODO find a better way to do this
        for (final String local : localComponentList) {
            for (final String remote : remoteComponentList) {
                if (local.contains(remote)) {
                    sharedComponents.add(local);
                    break;
                }
            }
        }
        sharedComponentCount = sharedComponents.size();

        return sharedComponents;
    }

    public int getSharedComponentCount() throws IntegrationException {
        sharedComponentCount = sharedComponentCount < 0 ? getSharedComponents().size() : sharedComponentCount;
        return sharedComponentCount;
    }

    private void preProcessListData(final List<String> list) throws IntegrationException {
        if (HubSonarUtils.getSettings() != null) {
            final List<String> removalCandidates = new ArrayList<>();
            for (final String str : list) {
                if (!ComponentUtils.componentMatchesInclusionPatterns(HubSonarUtils.getSettings(), str)) {
                    logger.debug(String.format("Removing '%s', as it does not match any inclusion patterns.", str));
                    removalCandidates.add(str);
                }
            }
            list.removeAll(removalCandidates);
        }
    }

}
