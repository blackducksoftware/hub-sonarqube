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
package com.blackducksoftware.integration.hub.sonar.component;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;

import com.blackducksoftware.integration.hub.sonar.manager.SonarManager;
import com.synopsys.integration.log.IntLogger;

public class LocalComponentGatherer implements ComponentGatherer {
    private final IntLogger logger;
    private final SonarManager sonarManager;
    private final FileSystem fileSystem;
    private final FilePredicate includeExcludePredicate;

    public LocalComponentGatherer(IntLogger logger, SonarManager sonarManager, FileSystem fileSystem, FilePredicate includeExcludePredicate) {
        this.logger = logger;
        this.sonarManager = sonarManager;
        this.fileSystem = fileSystem;
        this.includeExcludePredicate = includeExcludePredicate;
    }

    @Override
    public Set<String> gatherComponents() {
        logger.debug(String.format("Inclusion Patterns: %s", Arrays.toString(sonarManager.getGlobalInclusionPatterns())));
        logger.debug(String.format("Exclusion Patterns: %s", Arrays.toString(sonarManager.getGlobalExclusionPatterns())));
        logger.debug(String.format("Base Directory: %s", fileSystem.baseDir().toString()));

        Set<String> localComponents = new HashSet<>();
        for (InputFile inputFile : fileSystem.inputFiles(includeExcludePredicate)) {
            File fromUri = new File(inputFile.uri());
            try {
                localComponents.add(fromUri.getCanonicalPath());
            } catch (IOException e) {
                logger.error("Problem getting canonical path.", e);
                localComponents.add(fromUri.getAbsolutePath());
            }
        }
        return localComponents;
    }
}
