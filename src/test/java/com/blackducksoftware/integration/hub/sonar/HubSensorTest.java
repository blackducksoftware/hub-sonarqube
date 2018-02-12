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
package com.blackducksoftware.integration.hub.sonar;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.utils.log.Loggers;

public class HubSensorTest {
    private HubSensor sensor;

    @Before
    public void init() {
        sensor = new HubSensor();
    }

    @Test
    public void executeAndAvoidException() {
        final SensorContextTester context = SensorContextTester.create(new File(SonarTestUtils.TEST_DIRECTORY));

        Exception exception = null;
        try {
            sensor.execute(context);
        } catch (final Exception e) {
            exception = e;
        }

        assertEquals(null, exception);
    }

    @Test
    public void executeAndAvoidExceptionWithNullSettings() {
        final SensorContextTester context = SensorContextTester.create(new File(SonarTestUtils.TEST_DIRECTORY));
        context.setSettings(null);

        Exception exception = null;
        try {
            sensor.execute(context);
        } catch (final Exception e) {
            exception = e;
        }

        assertEquals(null, exception);
    }

    @Test
    public void describeTest() {
        Exception exception = null;
        try {
            sensor.describe(new TestSensorDescriptor());
        } catch (final Exception e) {
            exception = e;
        }
        assertEquals(null, exception);
    }

    // Only implement necessary methods for testing.
    protected class TestSensorDescriptor implements SensorDescriptor {
        private String name;
        private Type onlyOnType;

        @Override
        public SensorDescriptor name(final String sensorName) {
            this.name = sensorName;
            return this;
        }

        @Override
        public SensorDescriptor onlyOnLanguage(final String languageKey) {
            return this;
        }

        @Override
        public SensorDescriptor onlyOnLanguages(final String... languageKeys) {
            return this;
        }

        @Override
        public SensorDescriptor onlyOnFileType(final Type type) {
            this.onlyOnType = type;
            return this;
        }

        @Override
        public SensorDescriptor createIssuesForRuleRepository(final String... repositoryKey) {
            return this;
        }

        @Override
        public SensorDescriptor createIssuesForRuleRepositories(final String... repositoryKeys) {
            return this;
        }

        @Override
        public SensorDescriptor requireProperty(final String... propertyKey) {
            return this;
        }

        @Override
        public SensorDescriptor requireProperties(final String... propertyKeys) {
            return this;
        }

        @Override
        public SensorDescriptor global() {
            Loggers.get(getClass()).info(String.format("Name: %s, Type: %s", name, onlyOnType));
            return this;
        }
    }
}
