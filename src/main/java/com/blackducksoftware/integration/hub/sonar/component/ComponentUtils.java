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

public class ComponentUtils {

    public static final String[] INCLUSION_PATTERNS = { "**/*.jar", "**/*.war", "**/*.zip", "**/*.tar*", "**/*.hpi" };
    public static final String[] EXCLUSION_PATTERNS = { "**/WEB-INF/**/*.jar", "**/test-workspace/**/*.jar" };

    public static String getFilePath(final String composite) {
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

    public static boolean componentMatchesInclusionPatterns(final String str) {
        for (final String include : ComponentUtils.INCLUSION_PATTERNS) {
            if (componentMatchesInclusionPattern(str, include)) {
                return true;
            }
        }
        return false;
    }

    public static boolean componentMatchesInclusionPattern(final String str, final String pattern) {
        final String suffix = trimToSuffix(pattern);
        if (str.endsWith(suffix)) {
            return true;
        }
        return false;
    }

    private static String trimToSuffix(String pattern) {
        if (pattern.contains("*")) {
            final int lastIndex = pattern.lastIndexOf("*");
            pattern = pattern.substring(lastIndex + 1, pattern.length());
        }
        return pattern;
    }

}