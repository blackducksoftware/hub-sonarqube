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
package com.blackducksoftware.integration.hub.sonar.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.InputFile;

public class MockFilePredicate implements FilePredicate {
    private final List<String> inclusionPatterns;
    private final List<String> exclusionPatterns;

    public MockFilePredicate(final String[] inclusionPatterns, final String[] exclusionPatterns) {
        this.inclusionPatterns = new ArrayList<>();
        if (inclusionPatterns != null) {
            this.inclusionPatterns.addAll(Arrays.asList(inclusionPatterns));
        }
        this.exclusionPatterns = new ArrayList<>();
        if (exclusionPatterns != null) {
            this.exclusionPatterns.addAll(Arrays.asList(exclusionPatterns));
        }
    }

    @Override
    public boolean apply(final InputFile inputFile) {
        boolean shouldApply = false;
        if (inputFile != null) {
            final String[] pathArray = inputFile.absolutePath().split("/");
            final String fileName = pathArray[pathArray.length - 1];
            shouldApply = !shouldExclude(fileName) && shouldInclude(fileName);
        }
        return shouldApply;
    }

    private boolean shouldExclude(final String fileName) {
        if (exclusionPatterns != null) {
            for (final String exclude : exclusionPatterns) {
                if (fileName.endsWith(exclude)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean shouldInclude(final String fileName) {
        if (inclusionPatterns != null) {
            for (final String include : inclusionPatterns) {
                if (fileName.endsWith(include)) {
                    return true;
                }
            }
        }
        return false;
    }

    public String[] getInclusionPatterns() {
        return inclusionPatterns.toArray(new String[0]);
    }

    public void addInclusionPatterns(final String[] inclusionPatterns) {
        if (inclusionPatterns != null) {
            this.inclusionPatterns.addAll(Arrays.asList(inclusionPatterns));
        }
    }

    public String[] getExclusionPatterns() {
        return exclusionPatterns.toArray(new String[0]);
    }

    public void addExclusionPatterns(final String[] exclusuionPatterns) {
        if (exclusionPatterns != null) {
            this.exclusionPatterns.addAll(Arrays.asList(exclusuionPatterns));
        }
    }

}
