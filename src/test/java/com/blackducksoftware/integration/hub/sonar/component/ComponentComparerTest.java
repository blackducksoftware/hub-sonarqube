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
package com.blackducksoftware.integration.hub.sonar.component;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.Set;

import org.junit.Test;
import org.sonar.api.internal.google.common.collect.Sets;

import com.blackducksoftware.integration.exception.IntegrationException;

public class ComponentComparerTest {
    private static final Set<String> BASE_SET = Sets.newHashSet("one", "two", "three");

    @Test
    public void getSharedComponentsMatchTest() throws IntegrationException {
        final ComponentHelper helper = new ComponentHelper(null);
        final ComponentComparer comparer = new ComponentComparer(helper, BASE_SET, BASE_SET);

        assertEquals(comparer.getSharedComponents(), BASE_SET);
        assertEquals(comparer.getSharedComponentCount(), BASE_SET.size());
    }

    @Test
    public void getSharedComponentsPartiallyMatchTest() throws IntegrationException {
        final ComponentHelper helper = new ComponentHelper(null);
        final Set<String> otherSet = Sets.newHashSet("uno", "dos", "three");
        final ComponentComparer comparer = new ComponentComparer(helper, BASE_SET, otherSet);

        assertEquals(comparer.getSharedComponents(), Sets.newHashSet("three"));
        assertEquals(comparer.getSharedComponentCount(), 1);
    }

    @Test
    public void getSharedComponentsDoNotMatchTest() throws IntegrationException {
        final ComponentHelper helper = new ComponentHelper(null);
        final Set<String> otherSet = Sets.newHashSet("one1", "two2", "three3");
        final ComponentComparer comparer = new ComponentComparer(helper, BASE_SET, otherSet);

        assertNotEquals(comparer.getSharedComponents(), BASE_SET);
        assertEquals(comparer.getSharedComponentCount(), 0);
    }

    @Test
    public void getSharedComponentsPartialMatchTest() throws IntegrationException {
        final ComponentHelper helper = new ComponentHelper(null);
        final Set<String> absolutePathSet = Sets.newHashSet("/dir/one", "/dir/two", "/dir/three");

        final ComponentComparer comparer = new ComponentComparer(helper, absolutePathSet, BASE_SET);

        assertEquals(comparer.getSharedComponents(), absolutePathSet);
        assertEquals(comparer.getSharedComponentCount(), absolutePathSet.size());
    }
}
