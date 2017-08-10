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
import com.blackducksoftware.integration.hub.sonar.HubPropertyConstants;
import com.blackducksoftware.integration.hub.sonar.manager.SonarManager;

public class ComponentHelper {

    private final SonarManager sonarManager;

    public ComponentHelper(final SonarManager sonarManager) {
        this.sonarManager = sonarManager;
    }

    public static final String DEFAULT_INCLUSION_PATTERNS = "**/*.jar, **/*.war, **/*.zip, **/*.tar*, ";
    public static final String DEFAULT_EXCLUSION_PATTERNS = "";

    public String getFilePath(final String composite) {
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
        for (final String include : sonarManager.getValues(HubPropertyConstants.HUB_BINARY_INCLUSION_PATTERN_OVERRIDE)) {
            if (componentMatchesInclusionPattern(str, include)) {
                return true;
            }
        }
        return false;
    }

    public boolean componentMatchesInclusionPattern(final String str, final String pattern) {
        final String suffix = trimToSuffix(pattern);
        if (str.endsWith(suffix)) {
            return true;
        }
        return false;
    }

    private String trimToSuffix(String pattern) {
        if (pattern.contains("*")) {
            final int lastIndex = pattern.lastIndexOf("*");
            pattern = pattern.substring(lastIndex + 1, pattern.length()).trim();
        }
        return pattern;
    }

}
