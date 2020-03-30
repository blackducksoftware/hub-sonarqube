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

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.junit.Test;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.internal.google.common.collect.Sets;

import com.blackducksoftware.integration.hub.sonar.HubPropertyConstants;
import com.blackducksoftware.integration.hub.sonar.SonarTestUtils;
import com.blackducksoftware.integration.hub.sonar.manager.SonarManager;
import com.blackducksoftware.integration.hub.sonar.model.MockFilePredicates;
import com.blackducksoftware.integration.hub.sonar.model.MockFileSystem;
import com.blackducksoftware.integration.log.LogLevel;
import com.blackducksoftware.integration.log.PrintStreamIntLogger;

public class LocalComponentGathererTest {
    @Test
    public void gatherComponentsTest() throws IOException {
        File baseDir = new File(SonarTestUtils.TEST_DIRECTORY);
        String canonicalPath = baseDir.getCanonicalPath() + "/";
        LocalComponentGatherer gatherer = createGatherer(baseDir);
        Set<String> expectedList = Sets.newHashSet(canonicalPath + "test.jar", canonicalPath + "test.tar");

        assertEquals(expectedList, gatherer.gatherComponents());
    }

    @SuppressWarnings("deprecation")
    protected static LocalComponentGatherer createGatherer(File baseDir) {
        PrintStreamIntLogger logger = new PrintStreamIntLogger(System.out, LogLevel.INFO);
        MapSettings settings = new MapSettings();
        settings.setProperty(HubPropertyConstants.HUB_BINARY_INCLUSION_PATTERN_OVERRIDE, ".jar,.tar");
        settings.setProperty(HubPropertyConstants.HUB_BINARY_EXCLUSION_PATTERN_OVERRIDE, ".png");
        SonarManager manager = new SonarManager(settings.asConfig());

        DefaultFileSystem fileSystem = new MockFileSystem(baseDir);
        FilePredicates predicates = new MockFilePredicates();
        FilePredicate predicate = predicates.and(predicates.matchesPathPatterns(manager.getGlobalInclusionPatterns()), predicates.doesNotMatchPathPatterns(manager.getGlobalExclusionPatterns()));

        return new LocalComponentGatherer(logger, manager, fileSystem, predicate);
    }
}
