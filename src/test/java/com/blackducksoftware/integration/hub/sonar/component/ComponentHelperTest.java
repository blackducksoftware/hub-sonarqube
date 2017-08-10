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
import org.sonar.api.config.Settings;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.sonar.HubPropertyConstants;
import com.blackducksoftware.integration.hub.sonar.manager.SonarManager;

public class ComponentHelperTest {
    @Test
    public void preProcessComponentListDataTest() throws IntegrationException {
        final Settings settings = new Settings();
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
        final String compositePath = "/windows-service/jenkins.exe.config#bin/target/jenkins-for-test/WEB-INF/lib/jenkins-core-1.580.3.jar!/";
        final String expectedResult = "bin/target/jenkins-for-test/WEB-INF/lib/jenkins-core-1.580.3.jar";
        assertEquals(helper.getFilePath(compositePath), expectedResult);
    }

    @Test
    public void getFilePathForPartialCompositePathTest() {
        final ComponentHelper helper = new ComponentHelper(null);
        final String compositePath = "libs/provided/commons-beanutils-1.7.0.jar#";
        final String expectedResult = "libs/provided/commons-beanutils-1.7.0.jar";
        assertEquals(helper.getFilePath(compositePath), expectedResult);
    }

    @Test
    public void testComponentMatchesNonSuffixInclusionPattern() {
        final ComponentHelper helper = new ComponentHelper(null);
        final String component = "something.jar";
        final String pattern = "**/*.jar";
        assertTrue(helper.componentMatchesInclusionPattern(component, pattern));
    }

    @Test
    public void testComponentMatchesSuffixInclusionPattern() {
        final ComponentHelper helper = new ComponentHelper(null);
        final String component = "something.jar";
        final String pattern = ".jar";
        assertTrue(helper.componentMatchesInclusionPattern(component, pattern));
    }

    @Test
    public void testComponentDoesNotMatchInclusionPattern() {
        final ComponentHelper helper = new ComponentHelper(null);
        final String component = "something.tar";
        final String pattern = "**/*.jar";
        assertFalse(helper.componentMatchesInclusionPattern(component, pattern));
    }
}
