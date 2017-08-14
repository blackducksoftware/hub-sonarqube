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
package com.blackducksoftware.integration.hub.sonar;

import java.util.Arrays;
import java.util.List;

public class SonarTestUtils {
    public static String TEST_DIRECTORY = "src/test/resources/baseDir";
    public static String[] TEST_FILE_NAMES = { "test.jar", "test.tar", "test.png" };

    public static boolean stringArrayEquals(final String[] firstArray, final String secondArray[]) {
        final List<String> first = Arrays.asList(firstArray);
        final List<String> second = Arrays.asList(secondArray);
        return first.equals(second);
    }
}
