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
package com.blackducksoftware.integration.hub.sonar.model;

import java.io.File;
import java.util.Collection;

import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.InputFile.Status;
import org.sonar.api.batch.fs.InputFile.Type;

public class MockFilePredicates implements FilePredicates {
    private final MockFilePredicate predicate;

    public MockFilePredicates() {
        predicate = new MockFilePredicate(null, null);
    }

    @Override
    public FilePredicate all() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FilePredicate none() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FilePredicate hasAbsolutePath(final String s) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FilePredicate hasRelativePath(final String s) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FilePredicate matchesPathPattern(final String inclusionPattern) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FilePredicate matchesPathPatterns(final String[] inclusionPatterns) {
        predicate.addInclusionPatterns(inclusionPatterns);
        return predicate;
    }

    @Override
    public FilePredicate doesNotMatchPathPattern(final String exclusionPattern) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FilePredicate doesNotMatchPathPatterns(final String[] exclusionPatterns) {
        predicate.addExclusionPatterns(exclusionPatterns);
        return predicate;
    }

    @Override
    public FilePredicate hasPath(final String s) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FilePredicate is(final File ioFile) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FilePredicate hasLanguage(final String language) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FilePredicate hasLanguages(final Collection<String> languages) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FilePredicate hasLanguages(final String... languages) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FilePredicate hasStatus(final Status status) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FilePredicate hasType(final Type type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FilePredicate not(final FilePredicate p) {
        return predicate;
    }

    @Override
    public FilePredicate or(final Collection<FilePredicate> or) {
        return predicate;
    }

    @Override
    public FilePredicate or(final FilePredicate... or) {
        return predicate;
    }

    @Override
    public FilePredicate or(final FilePredicate first, final FilePredicate second) {
        return predicate;
    }

    @Override
    public FilePredicate and(final Collection<FilePredicate> and) {
        return predicate;
    }

    @Override
    public FilePredicate and(final FilePredicate... and) {
        return predicate;
    }

    @Override
    public FilePredicate and(final FilePredicate first, final FilePredicate second) {
        return predicate;
    }
}
