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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.sonar.api.config.Settings;

import com.blackducksoftware.integration.hub.sonar.HubPropertyConstants;
import com.blackducksoftware.integration.hub.sonar.SonarTestUtils;

public class SonarManagerTest {
    @Test
    public void getGlobalInclusionPatternsTest() {
        final Settings settings = new Settings();
        final String inclusionPatterns = "*.jar, *.gz";
        settings.setProperty(HubPropertyConstants.HUB_BINARY_INCLUSION_PATTERN_OVERRIDE, inclusionPatterns);
        final SonarManager manager = new SonarManager(settings);

        assertTrue(SonarTestUtils.stringArrayEquals(manager.getGlobalInclusionPatterns(), inclusionPatterns.split(", ")));
    }

    @Test
    public void getGlobalExclusionPatternsTest() {
        final Settings settings = new Settings();
        final String exclusionPatterns = "*.jar, *.gz";
        settings.setProperty(HubPropertyConstants.HUB_BINARY_EXCLUSION_PATTERN_OVERRIDE, exclusionPatterns);
        final SonarManager manager = new SonarManager(settings);

        assertTrue(SonarTestUtils.stringArrayEquals(manager.getGlobalExclusionPatterns(), exclusionPatterns.split(", ")));
    }

    @Test
    public void getValueTest() {
        final Settings settings = new Settings();
        final String key = "key";
        final String value = "value";
        settings.setProperty(key, value);
        final SonarManager manager = new SonarManager(settings);

        assertEquals(manager.getValue(key), value);
    }

    @Test
    public void getNullValueTest() {
        final Settings settings = new Settings();
        final String key = "key";
        final String value = null;
        settings.setProperty(key, value);
        final SonarManager manager = new SonarManager(settings);

        assertNotNull(manager.getValue(key));
        assertEquals(manager.getValue(key), "");
    }

    @Test
    public void getValuesTest() {
        final Settings settings = new Settings();
        final String key = "key";
        final String value = " one, two,three, four             ";
        final String[] values = { "one", "two", "three", "four" };
        settings.setProperty(key, value);
        final SonarManager manager = new SonarManager(settings);

        assertTrue(SonarTestUtils.stringArrayEquals(manager.getValues(key), values));
    }
}
