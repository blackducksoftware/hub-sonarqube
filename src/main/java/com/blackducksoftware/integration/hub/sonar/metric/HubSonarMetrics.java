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
package com.blackducksoftware.integration.hub.sonar.metric;

import java.util.Arrays;
import java.util.List;

import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metric.ValueType;
import org.sonar.api.measures.Metrics;

public class HubSonarMetrics implements Metrics {
    public static final String DOMAIN_HUB = "Black Duck Hub Security";

    public static final Metric<String> COMPONENT_NAMES = new Metric.Builder("hub_component_names", "Component Names", ValueType.STRING).setQualitative(false).setDomain(DOMAIN_HUB).setDirection(Metric.DIRECTION_NONE).setHidden(true)
            .setDeleteHistoricalData(true).create();
    public static final Metric<Integer> NUM_VULN_HIGH = new Metric.Builder("num_vuln_high", "High Security Vulnerabilities", ValueType.INT).setQualitative(false).setDomain(DOMAIN_HUB).setDirection(Metric.DIRECTION_WORST).setHidden(true)
            .create();
    public static final Metric<Integer> NUM_VULN_MED = new Metric.Builder("num_vuln_med", "Medium Security Vulnerabilities", ValueType.INT).setQualitative(false).setDomain(DOMAIN_HUB).setDirection(Metric.DIRECTION_WORST).setHidden(true)
            .create();
    public static final Metric<Integer> NUM_VULN_LOW = new Metric.Builder("num_vuln_low", "Low Security Vulnerabilities", ValueType.INT).setQualitative(false).setDomain(DOMAIN_HUB).setDirection(Metric.DIRECTION_WORST).setHidden(true)
            .create();

    public static final Metric<Integer> COMPONENT_RATING = new Metric.Builder("num_components_rating", "Component Ratings", ValueType.RATING).setQualitative(true).setDomain(DOMAIN_HUB).setHidden(true).create();
    public static final Metric<Integer> NUM_COMPONENTS = new Metric.Builder("num_components", "Components Mapped To Files Total", ValueType.INT).setQualitative(false).setDomain(DOMAIN_HUB).setDirection(Metric.DIRECTION_NONE).setHidden(true)
            .setDeleteHistoricalData(true).create();

    @SuppressWarnings("rawtypes")
    @Override
    public List<Metric> getMetrics() {
        return Arrays.asList(COMPONENT_NAMES, COMPONENT_RATING, NUM_VULN_HIGH, NUM_VULN_MED, NUM_VULN_LOW, NUM_COMPONENTS);
    }
}
