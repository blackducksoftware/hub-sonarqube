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

import org.sonar.api.PropertyType;
import org.sonar.api.config.PropertyDefinition;

public class HubPropertyConstants {
    public static final String HUB_SONAR_PREFIX = "sonar.hub.";
    public static final String HUB_SONAR_POSTFIX = ".override";
    public static final String HUB_URL = HUB_SONAR_PREFIX + "url";
    public static final String HUB_USERNAME = HUB_SONAR_PREFIX + "username";
    public static final String HUB_PASSWORD = HUB_SONAR_PREFIX + "password";
    public static final String HUB_TIMEOUT = HUB_SONAR_PREFIX + "timeout";
    public static final String HUB_IMPORT_SSL_CERT = HUB_SONAR_PREFIX + "auto.import.https.certs";

    public static final String HUB_SONAR_PROXY_PREFIX = HUB_SONAR_PREFIX + "proxy.";
    public static final String HUB_PROXY_HOST = HUB_SONAR_PROXY_PREFIX + "host";
    public static final String HUB_PROXY_PORT = HUB_SONAR_PROXY_PREFIX + "port";
    public static final String HUB_NO_PROXY_HOSTS = HUB_SONAR_PROXY_PREFIX + "non.hosts";
    public static final String HUB_PROXY_USERNAME = HUB_SONAR_PROXY_PREFIX + "username";
    public static final String HUB_PROXY_PASSWORD = HUB_SONAR_PROXY_PREFIX + "password";

    public static final String HUB_PROJECT_OVERRIDE = HUB_SONAR_PREFIX + "project" + HUB_SONAR_POSTFIX;
    public static final String HUB_PROJECT_VERSION_OVERRIED = HUB_SONAR_PREFIX + "project.version" + HUB_SONAR_POSTFIX;

    public static class Definitions {
        public static final PropertyDefinition HUB_URL = buildDef(HubPropertyConstants.HUB_URL, PropertyType.STRING, "Hub URL: ", "Specify the URL of your Hub installation, for example: http://hub.blackducksoftware", "");
        public static final PropertyDefinition HUB_USERNAME = buildDef(HubPropertyConstants.HUB_USERNAME, PropertyType.STRING, "Username: ", "The Hub username.", "");
        public static final PropertyDefinition HUB_PASSWORD = buildDef(HubPropertyConstants.HUB_PASSWORD, PropertyType.PASSWORD, "Password: ", "The password for the specified Hub user.", "");
        public static final PropertyDefinition HUB_TIMEOUT = buildDef(HubPropertyConstants.HUB_TIMEOUT, PropertyType.INTEGER, "Timeout (secs): ", "Hub connection timeout.", "120");
        public static final PropertyDefinition HUB_IMPORT_SSL_CERT = buildDef(HubPropertyConstants.HUB_IMPORT_SSL_CERT, PropertyType.BOOLEAN, "Import SSL Certificate: ",
                "This will import the SSL certificate of the specified HTTPS Hub server. Note: For this to work, the Java keystore must be writable by the proper user.", "false");
        public static final PropertyDefinition HUB_PROXY_HOST = buildDef(HubPropertyConstants.HUB_PROXY_HOST, PropertyType.STRING, "Proxy Host: ",
                "If the server is behind a firewall and does not have direct access to the internet, you may want to specify a proxy server. This will send any requests from the Hub Plugin to this server first.", "");
        public static final PropertyDefinition HUB_PROXY_PORT = buildDef(HubPropertyConstants.HUB_PROXY_PORT, PropertyType.INTEGER, "Proxy Port: ", "The port to be used to connect to the Proxy Server.", "");
        public static final PropertyDefinition HUB_NO_PROXY_HOSTS = buildDef(HubPropertyConstants.HUB_NO_PROXY_HOSTS, PropertyType.STRING, "No Proxy Host Names: ",
                "Specify host name regular expression patterns that shouldn't go through the proxy, in a comma separated list. Ex. .*blackducksoftware.com.*", "");
        public static final PropertyDefinition HUB_PROXY_USERNAME = buildDef(HubPropertyConstants.HUB_PROXY_USERNAME, PropertyType.STRING, "Proxy Username: ",
                "The username to use in the Proxy authentication. We currently only support proxies with Basic authenticaiton or no authentication.", "");
        public static final PropertyDefinition HUB_PROXY_PASSWORD = buildDef(HubPropertyConstants.HUB_PROXY_PASSWORD, PropertyType.PASSWORD, "Proxy Password: ",
                "The password to use in the Proxy authentication. We currently only support proxies with Basic authenticaiton or no authentication.", "");

        private static PropertyDefinition buildDef(final String prop, final PropertyType type, final String name, final String desc, final String defaultVal) {
            return PropertyDefinition.builder(prop).type(type).name(name).description(desc).defaultValue(defaultVal).build();
        }
    }
}
