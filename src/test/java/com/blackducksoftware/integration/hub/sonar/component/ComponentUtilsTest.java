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

import org.junit.Test;

public class ComponentUtilsTest {

    /*
     * Test Cases:
     *
     * libs/provided/commons-beanutils-1.7.0.jar#
     *
     */

    @Test
    public void getFilePathForTrueCompositePathTest() {
        final String compositePath = "/windows-service/jenkins.exe.config#bin/target/jenkins-for-test/WEB-INF/lib/jenkins-core-1.580.3.jar!/";
        final String expectedResult = "bin/target/jenkins-for-test/WEB-INF/lib/jenkins-core-1.580.3.jar";
        assertEquals(ComponentUtils.getFilePath(compositePath), expectedResult);
    }

    @Test
    public void getFilePathForPartialCompositePathTest() {
        final String compositePath = "libs/provided/commons-beanutils-1.7.0.jar#";
        final String expectedResult = "libs/provided/commons-beanutils-1.7.0.jar";
        assertEquals(ComponentUtils.getFilePath(compositePath), expectedResult);
    }

    @Test
    public void testComponentMatchesNonSuffixInclusionPattern() {
        final String component = "something.jar";
        final String pattern = "**/*.jar";
        assertTrue(ComponentUtils.componentMatchesInclusionPattern(component, pattern));
    }

    @Test
    public void testComponentMatchesSuffixInclusionPattern() {
        final String component = "something.jar";
        final String pattern = ".jar";
        assertTrue(ComponentUtils.componentMatchesInclusionPattern(component, pattern));
    }

    @Test
    public void testComponentDoesNotMatchInclusionPattern() {
        final String component = "something.tar";
        final String pattern = "**/*.jar";
        assertFalse(ComponentUtils.componentMatchesInclusionPattern(component, pattern));
    }
}
