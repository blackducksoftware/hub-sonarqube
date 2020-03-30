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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;

import com.blackducksoftware.integration.hub.sonar.manager.SonarManager;

public class ComponentHelper {
    public static final String ANY_STRING_PATTERN = "*";
    private final SonarManager sonarManager;
    private Map<String, InputFile> inputFiles;

    public ComponentHelper(SonarManager sonarManager) {
        this.sonarManager = sonarManager;
    }

    public static final String DEFAULT_INCLUSION_PATTERNS = "**/*.jar, **/*.war, **/*.zip, **/*.tar*, ";
    public static final String DEFAULT_EXCLUSION_PATTERNS = "";

    public String getFileNameFromComposite(String composite) {
        String filePath = getFilePathFromComposite(composite);
        return filePath.substring(filePath.lastIndexOf('/') + 1);
    }

    public String getFilePathFromComposite(String composite) {
        int lastIndex = composite.length();
        int archiveMarkIndex = composite.indexOf('!');
        int otherMarkIndex = composite.indexOf('#');
        boolean otherMarkIsLastCharacter = otherMarkIndex == lastIndex - 1;

        int startIndex;
        if (otherMarkIndex >= 0 && !otherMarkIsLastCharacter) {
            startIndex = otherMarkIndex + 1;
        } else {
            startIndex = 0;
        }

        int endIndex;
        if (archiveMarkIndex > startIndex) {
            endIndex = archiveMarkIndex;
        } else if (otherMarkIsLastCharacter) {
            endIndex = otherMarkIndex;
        } else {
            endIndex = lastIndex;
        }

        return composite.substring(startIndex, endIndex).trim();
    }

    public Collection<InputFile> getInputFilesFromStrings(Collection<String> sharedComponentNames) {
        Collection<InputFile> inputFilesFromStrings = new HashSet<>();
        for (String filePath : sharedComponentNames) {
            InputFile inputFileFromString = getInputFileFromString(filePath);
            if (inputFileFromString != null) {
                inputFilesFromStrings.add(inputFileFromString);
            }
        }
        return inputFilesFromStrings;
    }

    public InputFile getInputFileFromString(String filePath) {
        SensorContext context = sonarManager.getSensorContext();
        if (context != null) {
            return getInputFiles(context.fileSystem()).get(filePath);
        }
        return null;
    }

    public void preProcessComponentListData(Collection<String> collection) {
        if (sonarManager != null) {
            Collection<String> removalCandidates = new HashSet<>();
            for (String str : collection) {
                if (!componentMatchesInclusionPatterns(str)) {
                    removalCandidates.add(str);
                }
            }
            collection.removeAll(removalCandidates);
        }
    }

    public boolean componentMatchesInclusionPatterns(String str) {
        for (String include : sonarManager.getGlobalInclusionPatterns()) {
            if (componentMatchesInclusionPattern(str, include)) {
                return true;
            }
        }
        return false;
    }

    public Set<String> flattenCollectionOfCollections(Collection<Collection<String>> collection) {
        return collection.stream().flatMap(Collection::stream).collect(Collectors.toSet());
    }

    // returns true if str loosely matches (case insensitive) the provided pattern or if str strictly matches (case sensitive) the unqualifiedPattern
    public boolean componentMatchesInclusionPattern(String str, String pattern) {
        String unqualifiedPattern = pattern.substring(pattern.lastIndexOf('/') + 1);
        return FilenameUtils.wildcardMatch(str, pattern, IOCase.INSENSITIVE) || FilenameUtils.wildcardMatch(str, unqualifiedPattern, IOCase.SENSITIVE);
    }

    private Map<String, InputFile> getInputFiles(FileSystem fileSystem) {
        if (inputFiles == null) {
            inputFiles = new HashMap<>();
            for (InputFile inputFile : fileSystem.inputFiles(fileSystem.predicates().all())) {
                File fromUri = new File(inputFile.uri());
                String filePath;
                try {
                    filePath = fromUri.getCanonicalPath();
                } catch (IOException e) {
                    filePath = fromUri.getAbsolutePath();
                }
                inputFiles.put(filePath, inputFile);
            }
        }
        return inputFiles;
    }
}
