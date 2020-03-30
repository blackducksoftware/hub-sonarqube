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
package com.blackducksoftware.integration.hub.sonar.web;

import static org.junit.Assert.assertTrue;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;
import org.sonar.api.web.page.Context;

public class HubSonarPageDefinitionTest {
    @Test
    public void addPageDefinitionTest() {
        final HubSonarPageDefinition pageDefinition = new HubSonarPageDefinition();
        final Context pageContext = new Context();
        pageDefinition.define(pageContext);

        final Set<String> pageKeys = pageContext.getPages().stream().map(page -> page.getKey()).collect(Collectors.toSet());

        assertTrue(pageKeys.contains(HubSonarPageDefinition.PLUGIN_PAGE_LOCATION));
    }
}
