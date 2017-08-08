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
package com.blackducksoftware.integration.hub.sonar.component;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.sonar.MockLogger;

public class ComponentComparerTest {

    @Test
    public void getSharedComponentsMatchTest() throws IntegrationException {
        final MockLogger logger = new MockLogger();
        final List<String> first = Arrays.asList("one", "two", "three");
        final List<String> second = Arrays.asList("one", "two", "three");

        final ComponentComparer comparer = new ComponentComparer(logger, first, second);

        assertEquals(comparer.getSharedComponents(), first);
    }

    @Test
    public void getSharedComponentsPartiallyMatchTest() throws IntegrationException {
        final MockLogger logger = new MockLogger();
        final List<String> first = Arrays.asList("one", "two", "three");
        final List<String> second = Arrays.asList("uno", "dos", "three");

        final ComponentComparer comparer = new ComponentComparer(logger, first, second);

        assertEquals(comparer.getSharedComponents(), Arrays.asList("three"));
    }

    @Test
    public void getSharedComponentsDoNotMatchTest() throws IntegrationException {
        final MockLogger logger = new MockLogger();
        final List<String> first = Arrays.asList("one", "two", "three");
        final List<String> second = Arrays.asList("one1", "two2", "three3");

        final ComponentComparer comparer = new ComponentComparer(logger, first, second);

        assertNotEquals(comparer.getSharedComponents(), first);
        assertEquals(comparer.getSharedComponentCount(), 0);
    }

    @Test
    public void getSharedComponentsPartialMatchTest() throws IntegrationException {
        final MockLogger logger = new MockLogger();
        final List<String> absolutePathList = Arrays.asList("/dir/one", "/dir/two", "/dir/three");
        final List<String> relativePathList = Arrays.asList("one", "two", "three");

        final ComponentComparer comparer = new ComponentComparer(logger, absolutePathList, relativePathList);

        assertEquals(comparer.getSharedComponents(), absolutePathList);
    }

}
