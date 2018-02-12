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

import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.LoggerLevel;

import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.log.LogLevel;

public class HubSonarLogger extends IntLogger {
    private final Logger logger;

    public HubSonarLogger(final Logger logger) {
        this.logger = logger;
    }

    /**
     * @deprecated (Since 1.0.0, No equivalent operation)
     */
    @Override
    @Deprecated
    public void alwaysLog(final String txt) {
        logger.info(txt);
    }

    @Override
    public void info(final String txt) {
        logger.info(txt);
    }

    @Override
    public void error(final Throwable t) {
        logger.error(t.getMessage());
    }

    @Override
    public void error(final String txt, final Throwable t) {
        logger.error(txt, t);
    }

    @Override
    public void error(final String txt) {
        logger.error(txt);
    }

    @Override
    public void warn(final String txt) {
        logger.warn(txt);
    }

    @Override
    public void trace(final String txt) {
        if (logger.isTraceEnabled()) {
            logger.trace(txt);
        }
    }

    @Override
    public void trace(final String txt, final Throwable t) {
        if (logger.isTraceEnabled()) {
            logger.trace(txt, t);
        }
    }

    @Override
    public void debug(final String txt) {
        if (logger.isDebugEnabled()) {
            logger.debug(txt);
        }
    }

    @Override
    public void debug(final String txt, final Throwable t) {
        if (isDebugEnabled()) {
            logger.debug(txt, t);
        }
    }

    public boolean isDebugEnabled() {
        return getLogLevel() == LogLevel.DEBUG;
    }

    @Override
    public void setLogLevel(final LogLevel logLevel) {
        final LoggerLevel level;
        switch (logLevel) {
        case OFF:
            level = LoggerLevel.ERROR;
            break;
        case TRACE:
            level = LoggerLevel.TRACE;
            break;
        case DEBUG:
            level = LoggerLevel.DEBUG;
            break;
        case INFO:
            level = LoggerLevel.INFO;
            break;
        case WARN:
            level = LoggerLevel.WARN;
            break;
        case ERROR:
            level = LoggerLevel.ERROR;
            break;
        default:
            level = LoggerLevel.INFO;
        }
        logger.setLevel(level);
    }

    @Override
    public LogLevel getLogLevel() {
        final LoggerLevel level = logger.getLevel();
        switch (level) {
        case TRACE:
            return LogLevel.TRACE;
        case DEBUG:
            return LogLevel.DEBUG;
        case INFO:
            return LogLevel.INFO;
        case WARN:
            return LogLevel.WARN;
        case ERROR:
            return LogLevel.ERROR;
        default:
            return LogLevel.INFO;
        }
    }
}
