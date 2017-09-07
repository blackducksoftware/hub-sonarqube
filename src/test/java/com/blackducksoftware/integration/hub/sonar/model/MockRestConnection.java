/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.hub.sonar.model;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.log.IntLogger;

public class MockRestConnection extends RestConnection {
    public MockRestConnection(final IntLogger logger) {
        super(logger, null, 120);
    }

    @Override
    public void addBuilderAuthentication() throws IntegrationException {
    }

    @Override
    public void clientAuthenticate() throws IntegrationException {
    }
}
