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
package com.blackducksoftware.integration.hub.sonar.component;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.internal.google.common.collect.Sets;

import com.blackducksoftware.integration.hub.sonar.HubPropertyConstants;
import com.blackducksoftware.integration.hub.sonar.SonarTestUtils;
import com.blackducksoftware.integration.hub.sonar.manager.SonarManager;
import com.blackducksoftware.integration.hub.sonar.model.MockFileSystem;
import com.synopsys.integration.exception.IntegrationException;

public class ComponentHelperTest {
    private static final String EXAMPLE_COMPOSITE_PATH_LONG = "/windows-service/jenkins.exe.config#bin/target/jenkins-for-test/WEB-INF/lib/jenkins-core-1.580.3.jar!/";
    private static final String EXAMPLE_COMPOSITE_PATH_LONG_FILE_PATH = "bin/target/jenkins-for-test/WEB-INF/lib/jenkins-core-1.580.3.jar";
    private static final String EXAMPLE_COMPOSITE_PATH_LONG_FILE_NAME = "jenkins-core-1.580.3.jar";
    private static final String EXAMPLE_COMPOSITE_PATH_SHORT = "libs/provided/commons-beanutils-1.7.0.jar#";
    private static final String EXAMPLE_COMPOSITE_PATH_SHORT_FILE_PATH = "libs/provided/commons-beanutils-1.7.0.jar";
    private static final String EXAMPLE_COMPOSITE_PATH_SHORT_FILE_NAME = "commons-beanutils-1.7.0.jar";

    private static final String EXAMPLE_COMPONENT_FILE_NAME = "something.jar";

    private ComponentHelper helper;

    @Before
    public void init() {
        helper = new ComponentHelper(null);
    }

    @Test
    public void preProcessComponentListDataTest() throws IntegrationException {
        MapSettings settings = new MapSettings();
        settings.setProperty(HubPropertyConstants.HUB_BINARY_INCLUSION_PATTERN_OVERRIDE, "o??, *ee");

        @SuppressWarnings("deprecation") ComponentHelper compHelper = new ComponentHelper(new SonarManager(settings.asConfig()));

        List<String> first = new ArrayList<>(Arrays.asList("one", "three"));
        List<String> second = new ArrayList<>(Arrays.asList("one", "two", "three", "three and a half"));

        compHelper.preProcessComponentListData(second);

        assertEquals(first, second);
    }

    @Test
    public void getFilePathForTrueCompositePathTest() {
        assertEquals(EXAMPLE_COMPOSITE_PATH_LONG_FILE_PATH, helper.getFilePathFromComposite(EXAMPLE_COMPOSITE_PATH_LONG));
    }

    @Test
    public void getFilePathForPartialCompositePathTest() {
        assertEquals(EXAMPLE_COMPOSITE_PATH_SHORT_FILE_PATH, helper.getFilePathFromComposite(EXAMPLE_COMPOSITE_PATH_SHORT));
    }

    @Test
    public void getFileNameForTrueCompositePathTest() {
        assertEquals(EXAMPLE_COMPOSITE_PATH_LONG_FILE_NAME, helper.getFileNameFromComposite(EXAMPLE_COMPOSITE_PATH_LONG));
    }

    @Test
    public void getFileNameForPartialCompositePathTest() {
        assertEquals(EXAMPLE_COMPOSITE_PATH_SHORT_FILE_NAME, helper.getFileNameFromComposite(EXAMPLE_COMPOSITE_PATH_SHORT));
    }

    @Test
    public void getInputFilesFromStringsTest() {
        File baseDir = new File(SonarTestUtils.TEST_DIRECTORY);
        Set<String> inputFiles = LocalComponentGathererTest.createGatherer(baseDir).gatherComponents();

        SensorContextTester context = SensorContextTester.create(baseDir);
        context.setFileSystem(new MockFileSystem(baseDir));

        SonarManager manager = new SonarManager(context);
        ComponentHelper compHelper = new ComponentHelper(manager);
        Collection<InputFile> collection = compHelper.getInputFilesFromStrings(inputFiles);

        assertTrue(collection != null && !collection.isEmpty());
        assertEquals(2, collection.size());
    }

    @Test
    public void getInputFilesFromStringsWithNullContextTest() {
        @SuppressWarnings("deprecation") SonarManager manager = new SonarManager(new MapSettings().asConfig());
        ComponentHelper compHelper = new ComponentHelper(manager);
        InputFile inputFile = compHelper.getInputFileFromString("INVALID_FILE");

        assertEquals(null, inputFile);
    }

    @Test
    public void componentMatchesNonSuffixInclusionPatternTest() {
        assertTrue(helper.componentMatchesInclusionPattern(EXAMPLE_COMPONENT_FILE_NAME, "**/*.jar"));
    }

    @Test
    public void componentMatchesSuffixInclusionPatternTest() {
        assertTrue(helper.componentMatchesInclusionPattern(EXAMPLE_COMPONENT_FILE_NAME, "*.jar"));
    }

    @Test
    public void componentMatchesPartialSuffixInclusionPatternTest() {
        assertTrue(helper.componentMatchesInclusionPattern("something.tar.gz", "*.tar*"));
    }

    @Test
    public void componentDoesNotMatchInclusionPatternTest() {
        assertFalse(helper.componentMatchesInclusionPattern(EXAMPLE_COMPONENT_FILE_NAME, "**/*.tar"));
    }

    @Test
    public void flattenCollectionOfCollectionsTest() {
        Collection<Collection<String>> collectionOfCollections = Arrays.asList(Arrays.asList("element 1", "element 2"), Arrays.asList("element 3"));
        Set<String> flattenedSet = helper.flattenCollectionOfCollections(collectionOfCollections);
        assertEquals(Sets.newHashSet("element 1", "element 2", "element 3"), flattenedSet);
    }
}
