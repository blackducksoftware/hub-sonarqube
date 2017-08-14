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

import org.sonar.api.utils.log.Logger;

import com.blackducksoftware.integration.hub.sonar.HubSonarLogger;
import com.blackducksoftware.integration.log.LogLevel;

public class MockLogger extends HubSonarLogger {

    public MockLogger() {
        super(null);
    }

    public MockLogger(final Logger logger) {
        super(logger);
    }

    @Override
    public void alwaysLog(final String txt) {
        System.out.println(String.format("Always Log: %s", txt));
    }

    @Override
    public void info(final String txt) {
        System.out.println(String.format("Info: %s", txt));
    }

    @Override
    public void error(final Throwable t) {
        System.out.println(String.format("Error: %s", t));
    }

    @Override
    public void error(final String txt, final Throwable t) {
        System.out.println(String.format("Error: %s | %s", txt, t));
    }

    @Override
    public void error(final String txt) {
        System.out.println(String.format("Error: %s", txt));
    }

    @Override
    public void warn(final String txt) {
        System.out.println(String.format("Warn: %s", txt));
    }

    @Override
    public void trace(final String txt) {
        System.out.println(String.format("Trace: %s", txt));
    }

    @Override
    public void trace(final String txt, final Throwable t) {
        System.out.println(String.format("Trace: %s", txt));
    }

    @Override
    public void debug(final String txt) {
        System.out.println(String.format("Debug: %s", txt));
    }

    @Override
    public void debug(final String txt, final Throwable t) {
        System.out.println(String.format("Debug: %s", txt));
    }

    @Override
    public void setLogLevel(final LogLevel logLevel) {
    }

    @Override
    public LogLevel getLogLevel() {
        return LogLevel.INFO;
    }

}
