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
package com.blackducksoftware.integration.hub.sonar.model;

import java.io.Serializable;

import org.sonar.api.SonarRuntime;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputModule;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.coverage.NewCoverage;
import org.sonar.api.batch.sensor.cpd.NewCpdTokens;
import org.sonar.api.batch.sensor.error.NewAnalysisError;
import org.sonar.api.batch.sensor.highlighting.NewHighlighting;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.measure.NewMeasure;
import org.sonar.api.batch.sensor.symbol.NewSymbolTable;
import org.sonar.api.config.Configuration;
import org.sonar.api.config.Settings;
import org.sonar.api.utils.Version;

public class MockSensorContext implements SensorContext {
    private Configuration configuration;
    private FileSystem fileSystem;

    public MockSensorContext(Configuration configuration, FileSystem fileSystem) {
        this.configuration = configuration;
        this.fileSystem = fileSystem;
    }

    @Override
    public Settings settings() {
        return null;
    }

    @Override
    public Configuration config() {
        return configuration;
    }

    @Override
    public FileSystem fileSystem() {
        return fileSystem;
    }

    @Override
    public ActiveRules activeRules() {
        return null;
    }

    @Override
    public InputModule module() {
        return null;
    }

    @Override
    public Version getSonarQubeVersion() {
        return null;
    }

    @Override
    public SonarRuntime runtime() {
        return null;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public <G extends Serializable> NewMeasure<G> newMeasure() {
        return null;
    }

    @Override
    public NewIssue newIssue() {
        return null;
    }

    @Override
    public NewHighlighting newHighlighting() {
        return null;
    }

    @Override
    public NewSymbolTable newSymbolTable() {
        return null;
    }

    @Override
    public NewCoverage newCoverage() {
        return null;
    }

    @Override
    public NewCpdTokens newCpdTokens() {
        return null;
    }

    @Override
    public NewAnalysisError newAnalysisError() {
        return null;
    }

    @Override
    public void addContextProperty(String s, String s1) {

    }

    @Override
    public void markForPublishing(InputFile inputFile) {

    }
}
