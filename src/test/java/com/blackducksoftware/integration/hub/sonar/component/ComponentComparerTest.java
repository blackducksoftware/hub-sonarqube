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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.sonar.api.config.Settings;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.sonar.HubPropertyConstants;
import com.blackducksoftware.integration.hub.sonar.HubSonarUtils;
import com.blackducksoftware.integration.hub.sonar.MockLogger;

public class ComponentComparerTest {

    @After
    public void unsetSettings() {
        HubSonarUtils.setSettings(null);
    }

    @Test
    public void getSharedComponentsMatchTest() throws IntegrationException {
        final MockLogger logger = new MockLogger();
        final List<String> first = Arrays.asList("one", "two", "three");
        final List<String> second = Arrays.asList("one", "two", "three");

        final ComponentComparer comparer = new ComponentComparer(logger, first, second);

        assertEquals(comparer.getSharedComponents(), first);
    }

    @Test
    public void getSharedComponentsPartiallyMatchTest() throws IntegrationException {
        final MockLogger logger = new MockLogger();
        final List<String> first = Arrays.asList("one", "two", "three");
        final List<String> second = Arrays.asList("uno", "dos", "three");

        final ComponentComparer comparer = new ComponentComparer(logger, first, second);

        assertEquals(comparer.getSharedComponents(), Arrays.asList("three"));
    }

    @Test
    public void getSharedComponentsDoNotMatchTest() throws IntegrationException {
        final MockLogger logger = new MockLogger();
        final List<String> first = Arrays.asList("one", "two", "three");
        final List<String> second = Arrays.asList("one1", "two2", "three3");

        final ComponentComparer comparer = new ComponentComparer(logger, first, second);

        assertNotEquals(comparer.getSharedComponents(), first);
        assertEquals(comparer.getSharedComponentCount(), 0);
    }

    @Test
    public void getSharedComponentsPartialMatchTest() throws IntegrationException {
        final MockLogger logger = new MockLogger();
        final List<String> absolutePathList = Arrays.asList("/dir/one", "/dir/two", "/dir/three");
        final List<String> relativePathList = Arrays.asList("one", "two", "three");

        final ComponentComparer comparer = new ComponentComparer(logger, absolutePathList, relativePathList);

        assertEquals(comparer.getSharedComponents(), absolutePathList);
    }

    @Test
    public void getSharedComponentsPreprocessingTest() throws IntegrationException {
        final MockLogger logger = new MockLogger();
        final Settings settings = new Settings();

        settings.setProperty(HubPropertyConstants.HUB_BINARY_INCLUSION_PATTERN_OVERRIDE, "e, o");
        HubSonarUtils.setSettings(settings);

        final List<String> first = new ArrayList<>(Arrays.asList("one", "two", "three"));
        final List<String> second = new ArrayList<>(Arrays.asList("one", "two", "three", "three and a half"));

        final ComponentComparer comparer = new ComponentComparer(logger, first, second);

        assertEquals(comparer.getSharedComponents(), first);
    }

}
