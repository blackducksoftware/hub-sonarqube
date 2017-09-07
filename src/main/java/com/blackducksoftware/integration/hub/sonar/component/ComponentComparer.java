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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.blackducksoftware.integration.exception.IntegrationException;

public class ComponentComparer {
    private final Collection<String> localComponentList;
    private final Collection<String> remoteComponentList;
    private final ComponentHelper componentHelper;
    private int sharedComponentCount;
    private final boolean needsValidation;

    public ComponentComparer(final ComponentHelper componentHelper, final ComponentGatherer localComponentGatherer, final ComponentGatherer remoteComponentGatherer) {
        this.componentHelper = componentHelper;
        this.localComponentList = localComponentGatherer.gatherComponents();
        this.remoteComponentList = remoteComponentGatherer.gatherComponents();
        this.sharedComponentCount = -1;
        this.needsValidation = false;
    }

    public ComponentComparer(final ComponentHelper componentHelper, final Collection<String> localComponentList, final Collection<String> remoteComponentList) {
        this.componentHelper = componentHelper;
        this.localComponentList = localComponentList;
        this.remoteComponentList = remoteComponentList;
        this.sharedComponentCount = -1;
        this.needsValidation = true;
    }

    public Set<String> getSharedComponents() throws IntegrationException {
        if (needsValidation) {
            componentHelper.preProcessComponentListData(localComponentList);
            componentHelper.preProcessComponentListData(remoteComponentList);
        }
        final Set<String> sharedComponents = new HashSet<>();
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
}
