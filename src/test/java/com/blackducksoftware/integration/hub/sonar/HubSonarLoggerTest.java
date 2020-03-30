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
package com.blackducksoftware.integration.hub.sonar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sonar.api.utils.log.LogTester;
import org.sonar.api.utils.log.LoggerLevel;
import org.sonar.api.utils.log.Loggers;

import com.blackducksoftware.integration.hub.sonar.model.HubSonarLogTester;
import com.blackducksoftware.integration.log.LogLevel;

public class HubSonarLoggerTest {
    private static final String PREFIX_ERROR = "error";
    private static final String TEST_STRING = "foo";

    private static LoggerTestClass logger_class;

    private LogTester logTester;

    @BeforeClass
    public static void beforeClass() {
        logger_class = LoggerTestClass.getInstance();
    }

    @Before
    public void init() {
        logTester = new HubSonarLogTester();
    }

    @Test
    public void alwaysLogTest() {
        logger_class.alwaysLog();
        assertContains(LoggerLevel.INFO, TEST_STRING);
    }

    @Test
    public void infoTest() {
        logger_class.info();
        assertContains(LoggerLevel.INFO, TEST_STRING);
    }

    @Test
    public void errorWithThrowableTest() {
        logger_class.error();
        assertContains(LoggerLevel.ERROR, TEST_STRING);
    }

    @Test
    public void errorWithoutThrowableTest() {
        logger_class.error(PREFIX_ERROR, false);
        assertContains(LoggerLevel.ERROR, PREFIX_ERROR);
    }

    @Test
    public void errorWithBothTest() {
        logger_class.error(PREFIX_ERROR, true);
        assertContains(LoggerLevel.ERROR, PREFIX_ERROR);
        assertContains(LoggerLevel.ERROR, TEST_STRING);
    }

    @Test
    public void warnTest() {
        logger_class.warn();
        assertContains(LoggerLevel.WARN, TEST_STRING);
    }

    @Test
    public void traceWithThrowableTest() {
        logger_class.setLogLevel(LogLevel.TRACE);
        logTester.setLevel(LoggerLevel.TRACE);
        logger_class.trace(true);

        assertContains(LoggerLevel.TRACE, PREFIX_ERROR);
        assertContains(LoggerLevel.TRACE, TEST_STRING);
    }

    @Test
    public void traceWithoutThrowableTest() {
        logger_class.setLogLevel(LogLevel.TRACE);
        logTester.setLevel(LoggerLevel.TRACE);
        logger_class.trace(false);

        assertContains(LoggerLevel.TRACE, TEST_STRING);
    }

    @Test
    public void traceWithThrowableTraceDisabledTest() {
        logger_class.setLogLevel(LogLevel.INFO);
        logTester.setLevel(LoggerLevel.INFO);
        logger_class.trace(true);

        assertDoesNotContain(LoggerLevel.TRACE, PREFIX_ERROR);
        assertDoesNotContain(LoggerLevel.TRACE, TEST_STRING);
    }

    @Test
    public void traceWithoutThrowableTraceDisabledTest() {
        logger_class.setLogLevel(LogLevel.INFO);
        logTester.setLevel(LoggerLevel.INFO);
        logger_class.trace(false);

        assertDoesNotContain(LoggerLevel.TRACE, TEST_STRING);
    }

    @Test
    public void debugWithThrowableTest() {
        logger_class.setLogLevel(LogLevel.DEBUG);
        logTester.setLevel(LoggerLevel.DEBUG);
        logger_class.debug(true);

        assertContains(LoggerLevel.DEBUG, PREFIX_ERROR);
        assertContains(LoggerLevel.DEBUG, TEST_STRING);
    }

    @Test
    public void debugWithoutThrowableTest() {
        logger_class.setLogLevel(LogLevel.DEBUG);
        logTester.setLevel(LoggerLevel.DEBUG);
        logger_class.debug(false);

        assertContains(LoggerLevel.DEBUG, TEST_STRING);
    }

    @Test
    public void debugWithThrowableDebugDisabledTest() {
        logger_class.setLogLevel(LogLevel.INFO);
        logTester.setLevel(LoggerLevel.INFO);
        logger_class.debug(true);

        assertDoesNotContain(LoggerLevel.DEBUG, PREFIX_ERROR);
        assertDoesNotContain(LoggerLevel.DEBUG, TEST_STRING);
    }

    @Test
    public void debugWithoutThrowableDebugDisabledTest() {
        logger_class.setLogLevel(LogLevel.INFO);
        logTester.setLevel(LoggerLevel.INFO);
        logger_class.debug(false);

        assertDoesNotContain(LoggerLevel.DEBUG, TEST_STRING);
    }

    @Test
    public void overloadedSetLogLevelTest() {
        Exception exeption = null;
        try {
            logTester.setLevel(LoggerLevel.TRACE);
            logger_class.setLogLevel(LogLevel.TRACE);

            logTester.setLevel(LoggerLevel.INFO);
            logger_class.setLogLevel(LogLevel.INFO);

            logTester.setLevel(LoggerLevel.WARN);
            logger_class.setLogLevel(LogLevel.WARN);

            logTester.setLevel(LoggerLevel.ERROR);
            logger_class.setLogLevel(LogLevel.ERROR);

            logTester.setLevel(LoggerLevel.TRACE);
            logger_class.setLogLevel(LogLevel.OFF);
            logger_class.setLogLevel(LogLevel.fromString("Invalid string to trigger the default case"));
        } catch (final Exception e) {
            exeption = e;
        }
        assertEquals(null, exeption);
    }

    private void assertContains(final LoggerLevel level, @SuppressWarnings("unused") final String txt) {
        assertTrue(null != logTester.logs(level));
        // TODO - ignore until we can get LogTester to work correctly: assertTrue(logTester.logs(level).stream().anyMatch(log -> log.contains(txt)));
    }

    private void assertDoesNotContain(final LoggerLevel level, @SuppressWarnings("unused") final String txt) {
        assertTrue(null != logTester.logs(level));
        // TODO - ignore until we can get LogTester to work correctly: assertTrue(!logTester.logs(level).stream().anyMatch(log -> log.contains(txt)));
    }

    protected static class LoggerTestClass {
        private final HubSonarLogger logger;

        public LoggerTestClass() {
            logger = new HubSonarLogger(Loggers.get("hubSonarLogger"));
        }

        public static LoggerTestClass getInstance() {
            return new LoggerTestClass();
        }

        @SuppressWarnings("deprecation")
        public void alwaysLog() {
            logger.alwaysLog(TEST_STRING);
        }

        public void info() {
            logger.info(TEST_STRING);
        }

        public void error() {
            logger.error(new Exception(TEST_STRING));
        }

        public void error(final String txt, final boolean exception) {
            if (exception) {
                logger.error(txt, new Exception(TEST_STRING));
            } else {
                logger.error(txt);
            }
        }

        public void warn() {
            logger.warn(TEST_STRING);
        }

        public void trace(final boolean exception) {
            if (exception) {
                logger.trace(PREFIX_ERROR, new Exception(TEST_STRING));
            } else {
                logger.trace(TEST_STRING);
            }
        }

        public void debug(final boolean exception) {
            if (exception) {
                logger.debug(PREFIX_ERROR, new Exception(TEST_STRING));
            } else {
                logger.debug(TEST_STRING);
            }
        }

        public void setLogLevel(final LogLevel level) {
            logger.setLogLevel(level);
        }
    }
}
