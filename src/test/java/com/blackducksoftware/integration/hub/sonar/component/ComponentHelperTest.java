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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.sonar.api.config.MapSettings;
import org.sonar.api.config.Settings;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.sonar.HubPropertyConstants;
import com.blackducksoftware.integration.hub.sonar.manager.SonarManager;

public class ComponentHelperTest {
    private static final String EXAMPLE_COMPOSITE_PATH_LONG = "/windows-service/jenkins.exe.config#bin/target/jenkins-for-test/WEB-INF/lib/jenkins-core-1.580.3.jar!/";
    private static final String EXAMPLE_COMPOSITE_PATH_LONG_FILE_PATH = "bin/target/jenkins-for-test/WEB-INF/lib/jenkins-core-1.580.3.jar";
    private static final String EXAMPLE_COMPOSITE_PATH_LONG_FILE_NAME = "jenkins-core-1.580.3.jar";
    private static final String EXAMPLE_COMPOSITE_PATH_SHORT = "libs/provided/commons-beanutils-1.7.0.jar#";
    private static final String EXAMPLE_COMPOSITE_PATH_SHORT_FILE_PATH = "libs/provided/commons-beanutils-1.7.0.jar";
    private static final String EXAMPLE_COMPOSITE_PATH_SHORT_FILE_NAME = "commons-beanutils-1.7.0.jar";

    private static final String EXAMPLE_COMPONENT_FILE_NAME = "something.jar";

    @Test
    public void preProcessComponentListDataTest() throws IntegrationException {
        final Settings settings = new MapSettings();
        settings.setProperty(HubPropertyConstants.HUB_BINARY_INCLUSION_PATTERN_OVERRIDE, "e, o");

        final ComponentHelper helper = new ComponentHelper(new SonarManager(settings));

        final List<String> first = new ArrayList<>(Arrays.asList("one", "two", "three"));
        final List<String> second = new ArrayList<>(Arrays.asList("one", "two", "three", "three and a half"));

        helper.preProcessComponentListData(second);

        assertEquals(first, second);
    }

    @Test
    public void getFilePathForTrueCompositePathTest() {
        final ComponentHelper helper = new ComponentHelper(null);
        assertEquals(helper.getFilePathFromComposite(EXAMPLE_COMPOSITE_PATH_LONG), EXAMPLE_COMPOSITE_PATH_LONG_FILE_PATH);
    }

    @Test
    public void getFilePathForPartialCompositePathTest() {
        final ComponentHelper helper = new ComponentHelper(null);
        assertEquals(helper.getFilePathFromComposite(EXAMPLE_COMPOSITE_PATH_SHORT), EXAMPLE_COMPOSITE_PATH_SHORT_FILE_PATH);
    }

    @Test
    public void getFileNameForTrueCompositePathTest() {
        final ComponentHelper helper = new ComponentHelper(null);
        assertEquals(helper.getFileNameFromComposite(EXAMPLE_COMPOSITE_PATH_LONG), EXAMPLE_COMPOSITE_PATH_LONG_FILE_NAME);
    }

    @Test
    public void getFileNameForPartialCompositePathTest() {
        final ComponentHelper helper = new ComponentHelper(null);
        assertEquals(helper.getFileNameFromComposite(EXAMPLE_COMPOSITE_PATH_SHORT), EXAMPLE_COMPOSITE_PATH_SHORT_FILE_NAME);
    }

    @Test
    public void testComponentMatchesNonSuffixInclusionPattern() {
        final ComponentHelper helper = new ComponentHelper(null);
        assertTrue(helper.componentMatchesInclusionPattern(EXAMPLE_COMPONENT_FILE_NAME, "**/*.jar"));
    }

    @Test
    public void testComponentMatchesSuffixInclusionPattern() {
        final ComponentHelper helper = new ComponentHelper(null);
        assertTrue(helper.componentMatchesInclusionPattern(EXAMPLE_COMPONENT_FILE_NAME, ".jar"));
    }

    @Test
    public void testComponentMatchesPartialSuffixInclusionPattern() {
        final ComponentHelper helper = new ComponentHelper(null);
        assertTrue(helper.componentMatchesInclusionPattern("something.tar.gz", "*.tar*"));
    }

    @Test
    public void testComponentDoesNotMatchInclusionPattern() {
        final ComponentHelper helper = new ComponentHelper(null);
        assertFalse(helper.componentMatchesInclusionPattern(EXAMPLE_COMPONENT_FILE_NAME, "**/*.tar"));
    }
}
