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

import java.util.ArrayList;
import java.util.List;

import org.sonar.api.Plugin;

public class HubPlugin implements Plugin {
    @Override
    public void define(final Context context) {
        context.addExtensions(getExtensions());
    }

    public List<Object> getExtensions() {
        final List<Object> extensions = new ArrayList<>();
        extensions.add(HubPropertyConstants.Definitions.HUB_URL);
        extensions.add(HubPropertyConstants.Definitions.HUB_USERNAME);
        extensions.add(HubPropertyConstants.Definitions.HUB_PASSWORD);
        extensions.add(HubPropertyConstants.Definitions.HUB_TIMEOUT);
        extensions.add(HubPropertyConstants.Definitions.HUB_IMPORT_SSL_CERT);
        extensions.add(HubPropertyConstants.Definitions.HUB_PROXY_HOST);
        extensions.add(HubPropertyConstants.Definitions.HUB_PROXY_PORT);
        extensions.add(HubPropertyConstants.Definitions.HUB_NO_PROXY_HOSTS);
        extensions.add(HubPropertyConstants.Definitions.HUB_PROXY_USERNAME);
        extensions.add(HubPropertyConstants.Definitions.HUB_PROXY_PASSWORD);
        return extensions;
    }
}
