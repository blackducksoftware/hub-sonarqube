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

import java.util.Arrays;

import org.sonar.api.PropertyType;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;

import com.blackducksoftware.integration.hub.sonar.component.ComponentHelper;

public class HubPropertyConstants {
    public static final String SONAR_PROJECT_NAME_KEY = "sonar.projectName";
    public static final String SONAR_PROJECT_VERSION_KEY = "sonar.projectVersion";

    public static final String HUB_SONAR_PREFIX = "sonar.hub.";
    public static final String HUB_SONAR_SUFFIX = ".override";
    public static final String HUB_URL = HUB_SONAR_PREFIX + "url";
    public static final String HUB_USERNAME = HUB_SONAR_PREFIX + "username";
    public static final String HUB_PASSWORD = HUB_SONAR_PREFIX + "password";
    public static final String HUB_TIMEOUT = HUB_SONAR_PREFIX + "timeout";
    public static final String HUB_TRUST_SSL_CERT = HUB_SONAR_PREFIX + "trust.ssl.cert";

    public static final String HUB_SONAR_PROXY_PREFIX = HUB_SONAR_PREFIX + "proxy.";
    public static final String HUB_PROXY_HOST = HUB_SONAR_PROXY_PREFIX + "host";
    public static final String HUB_PROXY_PORT = HUB_SONAR_PROXY_PREFIX + "port";
    public static final String HUB_NO_PROXY_HOSTS = HUB_SONAR_PROXY_PREFIX + "non.hosts";
    public static final String HUB_PROXY_USERNAME = HUB_SONAR_PROXY_PREFIX + "username";
    public static final String HUB_PROXY_PASSWORD = HUB_SONAR_PROXY_PREFIX + "password";

    public static final String HUB_PROJECT_OVERRIDE = HUB_SONAR_PREFIX + "project" + HUB_SONAR_SUFFIX;
    public static final String HUB_PROJECT_VERSION_OVERRIED = HUB_SONAR_PREFIX + "project.version" + HUB_SONAR_SUFFIX;
    public static final String HUB_BINARY_INCLUSION_PATTERN_OVERRIDE = HUB_SONAR_PREFIX + "binary.inclusion.pattern" + HUB_SONAR_SUFFIX;
    public static final String HUB_BINARY_EXCLUSION_PATTERN_OVERRIDE = HUB_SONAR_PREFIX + "binary.exclusion.pattern" + HUB_SONAR_SUFFIX;

    private HubPropertyConstants() {
        // This class should not be instantiated. Added to meet SonarQube standard.
    }

    public static class Definitions {
        private static final String CATEGORY_GENERAL = "Hub Settings";
        private static final String CATEGORY_PROXY = "Proxy Settings";
        private static final String CATEGORY_PROJECT_OVERRIDE = "Hub Project Override Properties";
        private static final String CATEGORY_PATTERN_OVERRIDE = "Inclusion/Excludion Pattern Override Properties";

        public static final PropertyDefinition HUB_URL = buildGlobalDef(HubPropertyConstants.HUB_URL, PropertyType.STRING, "Hub URL: ", "Specify the URL of your Hub installation, for example: http://hub.blackducksoftware.com", "",
                CATEGORY_GENERAL, 0);
        public static final PropertyDefinition HUB_USERNAME = buildGlobalDef(HubPropertyConstants.HUB_USERNAME, PropertyType.STRING, "Username: ", "The Hub username.", "", CATEGORY_GENERAL, 1);
        public static final PropertyDefinition HUB_PASSWORD = buildGlobalDef(HubPropertyConstants.HUB_PASSWORD, PropertyType.PASSWORD, "Password: ", "The password for the specified Hub user.", "", CATEGORY_GENERAL, 2);
        public static final PropertyDefinition HUB_TIMEOUT = buildGlobalDef(HubPropertyConstants.HUB_TIMEOUT, PropertyType.INTEGER, "Timeout (secs): ", "Hub connection timeout.", "120", CATEGORY_GENERAL, 3);
        public static final PropertyDefinition HUB_TRUST_SSL_CERT = buildGlobalDef(HubPropertyConstants.HUB_TRUST_SSL_CERT, PropertyType.BOOLEAN, "Trust Hub SSL Certificate: ",
                "This will trust the SSL certificate of the specified HTTPS Hub server.", "false", CATEGORY_GENERAL, 4);
        public static final PropertyDefinition HUB_PROXY_HOST = buildGlobalDef(HubPropertyConstants.HUB_PROXY_HOST, PropertyType.STRING, "Proxy Host: ",
                "If the server is behind a firewall and does not have direct access to the internet, you may want to specify a proxy server. This will send any requests from the Hub Plugin to this server first.", "", CATEGORY_PROXY, 5);
        public static final PropertyDefinition HUB_PROXY_PORT = buildGlobalDef(HubPropertyConstants.HUB_PROXY_PORT, PropertyType.INTEGER, "Proxy Port: ", "The port to be used to connect to the Proxy Server.", "", CATEGORY_PROXY, 6);
        public static final PropertyDefinition HUB_NO_PROXY_HOSTS = buildGlobalDef(HubPropertyConstants.HUB_NO_PROXY_HOSTS, PropertyType.STRING, "No Proxy Host Names: ",
                "Specify host name regular expression patterns that shouldn't go through the proxy, in a comma separated list. Ex. .*blackducksoftware.com.*", "", CATEGORY_PROXY, 7);
        public static final PropertyDefinition HUB_PROXY_USERNAME = buildGlobalDef(HubPropertyConstants.HUB_PROXY_USERNAME, PropertyType.STRING, "Proxy Username: ",
                "The username to use in the Proxy authentication. We currently only support proxies with Basic authenticaiton or no authentication.", "", CATEGORY_PROXY, 8);
        public static final PropertyDefinition HUB_PROXY_PASSWORD = buildGlobalDef(HubPropertyConstants.HUB_PROXY_PASSWORD, PropertyType.PASSWORD, "Proxy Password: ",
                "The password to use in the Proxy authentication. We currently only support proxies with Basic authenticaiton or no authentication.", "", CATEGORY_PROXY, 9);

        public static final PropertyDefinition HUB_PROJECT_OVERRIDE = buildProjectDef(HubPropertyConstants.HUB_PROJECT_OVERRIDE, PropertyType.STRING, "Hub Project Name: ",
                "Hub project to map this SonarQube project to. Note: If this is not provided, the name of the SonarQube project and version will be used.", "", CATEGORY_PROJECT_OVERRIDE, 0);
        public static final PropertyDefinition HUB_PROJECT_VERSION_OVERRIED = buildProjectDef(HubPropertyConstants.HUB_PROJECT_VERSION_OVERRIED, PropertyType.STRING, "Hub Project Version Name: ",
                "Version of Hub project to map this SonarQube project to. Note: If this is not provided, but the Hub project name is, both of these properties will be ignored.", "", CATEGORY_PROJECT_OVERRIDE, 1);
        public static final PropertyDefinition HUB_BINARY_INCLUSION_PATTERN_OVERRIDE = createPropertyDefinitionBuilder(HubPropertyConstants.HUB_BINARY_INCLUSION_PATTERN_OVERRIDE, PropertyType.STRING, "Binary Inclusion Patterns: ",
                "File patterns used for gathering local components.", ComponentHelper.DEFAULT_INCLUSION_PATTERNS, CATEGORY_PATTERN_OVERRIDE, 2).onlyOnQualifiers(Arrays.asList(Qualifiers.PROJECT)).multiValues(true).build();
        public static final PropertyDefinition HUB_BINARY_EXCLUSION_PATTERN_OVERRIDE = createPropertyDefinitionBuilder(HubPropertyConstants.HUB_BINARY_EXCLUSION_PATTERN_OVERRIDE, PropertyType.STRING, "Binary Exclusion Patterns: ",
                "File patterns used for gathering local components.", ComponentHelper.DEFAULT_EXCLUSION_PATTERNS, CATEGORY_PATTERN_OVERRIDE, 3).onlyOnQualifiers(Arrays.asList(Qualifiers.PROJECT)).multiValues(true).build();

        private Definitions() {
            // This class should not be instantiated. Added to meet SonarQube standard.
        }

        private static PropertyDefinition buildGlobalDef(final String prop, final PropertyType type, final String name, final String desc, final String defaultVal, final String subCategory, final int index) {
            return createPropertyDefinitionBuilder(prop, type, name, desc, defaultVal, subCategory, index).build();
        }

        private static PropertyDefinition buildProjectDef(final String prop, final PropertyType type, final String name, final String desc, final String defaultVal, final String subCategory, final int index) {
            return createPropertyDefinitionBuilder(prop, type, name, desc, defaultVal, subCategory, index).onlyOnQualifiers(Arrays.asList(Qualifiers.PROJECT)).build();
        }

        private static PropertyDefinition.Builder createPropertyDefinitionBuilder(final String prop, final PropertyType type, final String name, final String desc, final String defaultVal, final String subCategory, final int index) {
            return PropertyDefinition.builder(prop).type(type).name(name).description(desc).defaultValue(defaultVal).subCategory(subCategory).index(index);
        }
    }
}
