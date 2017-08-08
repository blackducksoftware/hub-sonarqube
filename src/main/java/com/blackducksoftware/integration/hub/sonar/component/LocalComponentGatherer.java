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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.sensor.SensorContext;

import com.blackducksoftware.integration.hub.sonar.HubSonarLogger;

public class LocalComponentGatherer implements ComponentGatherer {

    private final HubSonarLogger logger;
    private final SensorContext context;

    public LocalComponentGatherer(final HubSonarLogger logger, final SensorContext context) {
        this.logger = logger;
        this.context = context;
    }

    @Override
    public List<String> gatherComponents() {
        final FileSystem fileSystem = context.fileSystem();
        final FilePredicates filePredicates = fileSystem.predicates();
        final FilePredicate includeExcludePredicate = filePredicates.and(filePredicates.matchesPathPatterns(ComponentUtils.INCLUSION_PATTERNS), filePredicates.doesNotMatchPathPatterns(ComponentUtils.EXCLUSION_PATTERNS));

        logger.debug(String.format("Inclusion Patterns: %s", Arrays.toString(ComponentUtils.INCLUSION_PATTERNS)));
        logger.debug(String.format("Exclusion Patterns: %s", Arrays.toString(ComponentUtils.EXCLUSION_PATTERNS)));
        logger.debug(String.format("Base Directory: %s", fileSystem.baseDir().toString()));

        final Iterator<File> fileIterator = fileSystem.files(includeExcludePredicate).iterator();

        final List<String> localBinaries = new ArrayList<>();
        while (fileIterator.hasNext()) {
            final File file = fileIterator.next();
            try {
                localBinaries.add(file.getCanonicalPath());
            } catch (final IOException e) {
                logger.warn(String.format("Problem getting canonical path for: %s", file));
                localBinaries.add(file.getAbsolutePath());
            }
        }

        return localBinaries;
    }

}
