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

public class ComponentComparer {

    private final List<String> firstComponentList;
    private final List<String> secondComponentList;

    private final int sharedComponentCount;
    private final boolean needsValidation;
    private final HubSonarLogger logger;

    public ComponentComparer(final HubSonarLogger logger, final ComponentGatherer firstGatherer, final ComponentGatherer secondGatherer) {
        this.logger = logger;
        this.firstComponentList = firstGatherer.gatherComponents();
        this.secondComponentList = secondGatherer.gatherComponents();
        this.sharedComponentCount = -1;
        this.needsValidation = false;
    }

    public ComponentComparer(final HubSonarLogger logger, final List<String> firstComponentList, final List<String> secondComponentList) {
        this.logger = logger;
        this.firstComponentList = firstComponentList;
        this.secondComponentList = secondComponentList;
        this.sharedComponentCount = -1;
        this.needsValidation = true;
    }

    public List<String> getSharedComponents() throws IntegrationException {
        if (needsValidation) {
            preProcessListData(firstComponentList);
            preProcessListData(secondComponentList);
        }
        final List<String> sharedComponents = new ArrayList<>();

        // TODO find a better way to do this
        for (final String first : firstComponentList) {
            for (final String second : secondComponentList) {
                if (first.contains(second)) {
                    sharedComponents.add(first);
                    break;
                }
            }
        }

        return sharedComponents;
    }

    public int getSharedComponentCount() throws IntegrationException {
        return sharedComponentCount < 0 ? getSharedComponents().size() : sharedComponentCount;
    }

    private void preProcessListData(final List<String> list) throws IntegrationException {
        for (final String str : list) {
            if (!ComponentUtils.componentMatchesInclusionPatterns(str)) {
                logger.debug(String.format("Removing '%s', as it does not match any inclusion patterns.", str));
                list.remove(str);
            }
        }
    }

}
