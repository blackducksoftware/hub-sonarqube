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
package com.blackducksoftware.integration.hub.sonar;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.sonar.api.internal.apachecommons.io.IOUtils;

public class SonarTestUtils {
    public static final String MY_PROJECT_KEY = "myProjectKey";
    public static final String TEST_DIRECTORY = "./src/test/resources/baseDir";
    public static final String JSON_DIRECTORY = TEST_DIRECTORY + "/json";
    public static final String PATH_DELIM = "/";

    private static final String[] JSON_COMPONENT_FILE_NAMES = { "component0.txt", "component1.txt" };

    private SonarTestUtils() {
        // This class should not be instantiated.
    }

    public static boolean stringArrayEquals(String[] firstArray, String[] secondArray) {
        List<String> first = Arrays.asList(firstArray);
        List<String> second = Arrays.asList(secondArray);
        return first.equals(second);
    }

    @SuppressWarnings("resource")
    public static String getJsonFromFile(String fileName) throws IOException {
        File file = new File(JSON_DIRECTORY + PATH_DELIM + fileName);
        FileInputStream input = new FileInputStream(file);
        String json = IOUtils.toString(input);
        input.close();
        return json;
    }

    public static String[] getJsonComponentFileNames() {
        return JSON_COMPONENT_FILE_NAMES;
    }
}
