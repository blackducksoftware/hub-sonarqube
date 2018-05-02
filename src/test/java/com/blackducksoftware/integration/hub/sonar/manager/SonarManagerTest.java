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
package com.blackducksoftware.integration.hub.sonar.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.mockito.Mockito;
import org.sonar.api.config.Configuration;
import org.sonar.api.config.internal.MapSettings;

import com.blackducksoftware.integration.hub.configuration.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.configuration.HubServerConfigValidator;
import com.blackducksoftware.integration.hub.sonar.HubPropertyConstants;
import com.blackducksoftware.integration.hub.sonar.SonarTestUtils;
import com.blackducksoftware.integration.validator.ValidationResults;

@SuppressWarnings("deprecation")
public class SonarManagerTest {
    private static final String EXAMPLE_INCLUSION_OR_EXCLUSION_PATTERNS = "*.jar, *.gz";
    private static final String DELIMITER = ", ";

    private static final String EXAMPLE_KEY = "key";

    @Test
    public void getHubServerConfigFromSettingsWithMissingFieldsTest() {
        SonarManager manager = new SonarManager((Configuration) null);
        manager = Mockito.spy(manager);
        Mockito.doReturn(Boolean.TRUE).when(manager).isConfigValid(Mockito.any());

        try {
            manager.getHubServerConfigFromSettings();
            fail();
        } catch (final Exception e) {
            // The builder should throw an exception because nothing is configured.
        }
    }

    @Test
    public void isConfigValidTest() {
        final SonarManager manager = new SonarManager(new MapSettings().asConfig());

        final HubServerConfigBuilder configBuilderMock = Mockito.mock(HubServerConfigBuilder.class);
        final HubServerConfigValidator validatorMock = Mockito.mock(HubServerConfigValidator.class);
        final ValidationResults validationResults = Mockito.mock(ValidationResults.class);

        Mockito.when(configBuilderMock.createValidator()).thenReturn(validatorMock);
        Mockito.when(validatorMock.assertValid()).thenReturn(validationResults);
        Mockito.when(validationResults.hasErrors()).thenReturn(Boolean.FALSE);

        assertTrue(manager.isConfigValid(configBuilderMock));
    }

    @Test
    public void getGlobalInclusionPatternsTest() {
        final MapSettings settings = new MapSettings();
        settings.setProperty(HubPropertyConstants.HUB_BINARY_INCLUSION_PATTERN_OVERRIDE, EXAMPLE_INCLUSION_OR_EXCLUSION_PATTERNS);
        final SonarManager manager = new SonarManager(settings.asConfig());

        assertTrue(SonarTestUtils.stringArrayEquals(manager.getGlobalInclusionPatterns(), EXAMPLE_INCLUSION_OR_EXCLUSION_PATTERNS.split(DELIMITER)));
    }

    @Test
    public void getGlobalExclusionPatternsTest() {
        final MapSettings settings = new MapSettings();
        settings.setProperty(HubPropertyConstants.HUB_BINARY_EXCLUSION_PATTERN_OVERRIDE, EXAMPLE_INCLUSION_OR_EXCLUSION_PATTERNS);
        final SonarManager manager = new SonarManager(settings.asConfig());

        assertTrue(SonarTestUtils.stringArrayEquals(manager.getGlobalExclusionPatterns(), EXAMPLE_INCLUSION_OR_EXCLUSION_PATTERNS.split(DELIMITER)));
    }

    @Test
    public void getValueTest() {
        final MapSettings settings = new MapSettings();
        final String value = "value";
        settings.setProperty(EXAMPLE_KEY, value);
        final SonarManager manager = new SonarManager(settings.asConfig());

        assertEquals(manager.getValue(EXAMPLE_KEY), value);
    }

    @Test
    public void getNullValueTest() {
        final MapSettings settings = new MapSettings();
        final String value = null;
        settings.setProperty(EXAMPLE_KEY, value);
        final SonarManager manager = new SonarManager(settings.asConfig());

        assertNotNull(manager.getValue(EXAMPLE_KEY));
        assertEquals("", manager.getValue(EXAMPLE_KEY));
    }

    @Test
    public void getValuesTest() {
        final MapSettings settings = new MapSettings();
        final String value = " one, two,three, four             ";
        final String[] values = { "one", "two", "three", "four" };
        settings.setProperty(EXAMPLE_KEY, value);
        final SonarManager manager = new SonarManager(settings.asConfig());

        assertTrue(SonarTestUtils.stringArrayEquals(manager.getValues(EXAMPLE_KEY), values));
    }

    @Test
    public void getHubPluginVersionTest() {
        final SonarManager manager = new SonarManager(new MapSettings().asConfig());

        assertTrue("<unknown>" != manager.getHubPluginVersion());
    }

    @Test
    public void getHubPluginVersionFromFileTest() {
        final SonarManager manager = new SonarManager(new MapSettings().asConfig());

        assertTrue("<unknown>" != manager.getHubPluginVersionFromFile("/plugin.properties"));
    }

    @Test
    public void getHubPluginVersionFromFileUnknownTest() {
        final SonarManager manager = new SonarManager(new MapSettings().asConfig());

        assertEquals("<unknown>", manager.getHubPluginVersionFromFile("/NULL"));
    }
}
