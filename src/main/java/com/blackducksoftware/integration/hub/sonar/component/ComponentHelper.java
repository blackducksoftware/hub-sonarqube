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

import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.sonar.manager.SonarManager;

public class ComponentHelper {
    public static final String ANY_STRING_PATTERN = "*";
    private final SonarManager sonarManager;

    public ComponentHelper(final SonarManager sonarManager) {
        this.sonarManager = sonarManager;
    }

    public static final String DEFAULT_INCLUSION_PATTERNS = "**/*.jar, **/*.war, **/*.zip, **/*.tar*, ";
    public static final String DEFAULT_EXCLUSION_PATTERNS = "";

    public String getFileNameFromComposite(final String composite) {
        final String filePath = getFilePathFromComposite(composite);
        final String[] pathTokens = filePath.split("/");
        return pathTokens[pathTokens.length - 1];
    }

    public String getFilePathFromComposite(final String composite) {
        final int lastIndex = composite.length() - 1;
        final int archiveMarkIndex = composite.indexOf("!");
        final int otherMarkIndex = composite.indexOf("#");

        final int startIndex;
        if (otherMarkIndex >= 0 && otherMarkIndex < lastIndex) {
            startIndex = otherMarkIndex + 1;
        } else {
            startIndex = 0;
        }

        final int endIndex;
        if (archiveMarkIndex > startIndex) {
            endIndex = archiveMarkIndex;
        } else {
            endIndex = lastIndex;
        }

        final String candidateFilePath = composite.substring(startIndex, endIndex);

        return candidateFilePath;
    }

    public void preProcessComponentListData(final List<String> list) throws IntegrationException {
        if (sonarManager != null) {
            final List<String> removalCandidates = new ArrayList<>();
            for (final String str : list) {
                if (!componentMatchesInclusionPatterns(str)) {
                    removalCandidates.add(str);
                }
            }
            list.removeAll(removalCandidates);
        }
    }

    public boolean componentMatchesInclusionPatterns(final String str) {
        for (final String include : sonarManager.getGlobalInclusionPatterns()) {
            if (componentMatchesInclusionPattern(str, include)) {
                return true;
            }
        }
        return false;
    }

    public boolean componentMatchesInclusionPattern(final String str, final String pattern) {
        final String suffix = trimPatternToSuffix(pattern);
        if (str.endsWith(suffix) || (str.contains(suffix) && pattern.endsWith(ANY_STRING_PATTERN))) {
            return true;
        }
        return false;
    }

    private String trimPatternToSuffix(String pattern) {
        if (pattern.contains(ANY_STRING_PATTERN)) {
            final int lastIndex = pattern.lastIndexOf(ANY_STRING_PATTERN);
            if (lastIndex == pattern.length() - 1) {
                pattern = trimPatternToSuffix(pattern.substring(0, lastIndex));
            } else {
                pattern = pattern.substring(lastIndex + 1, pattern.length()).trim();
            }
        }
        return pattern;
    }

}
