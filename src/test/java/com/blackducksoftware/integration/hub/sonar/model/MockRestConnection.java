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
package com.blackducksoftware.integration.hub.sonar.model;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.log.IntLogger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

import okhttp3.OkHttpClient;

public class MockRestConnection extends RestConnection {
    public static final String JSON_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSX";
    public final Gson gson = new GsonBuilder().setDateFormat(JSON_DATE_FORMAT).create();
    public final JsonParser jsonParser = new JsonParser();
    public final OkHttpClient.Builder builder = new OkHttpClient.Builder();
    public final Map<String, String> commonRequestHeaders = new HashMap<>();
    public final URL hubBaseUrl;

    public int timeout = 120;
    public String proxyHost;
    public int proxyPort;
    public String proxyNoHosts;
    public String proxyUsername;
    public String proxyPassword;
    public boolean alwaysTrustServerCertificate;
    public IntLogger logger;

    public MockRestConnection(final IntLogger logger) {
        super(logger, null, 120);
        this.hubBaseUrl = null;
    }

    @Override
    public void addBuilderAuthentication() throws IntegrationException {
        throw new UnsupportedOperationException("Unimplemented method in Mock class.");
    }

    @Override
    public void clientAuthenticate() throws IntegrationException {
        throw new UnsupportedOperationException("Unimplemented method in Mock class.");
    }
}
