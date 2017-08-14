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
import java.io.IOException;
import java.nio.file.LinkOption;
import java.util.ArrayList;
import java.util.List;

import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;

public class MockFileSystem extends DefaultFileSystem {

    private final File baseDir;

    public MockFileSystem(final File baseDir) {
        super(baseDir);
        this.baseDir = baseDir;
    }

    @Override
    public Iterable<File> files(final FilePredicate predicate) {
        final List<File> fileList = new ArrayList<>();
        for (final File file : baseDir.listFiles()) {
            final DefaultInputFile inputFile = new DefaultInputFile(file.getName(), file.getName());
            try {
                inputFile.setModuleBaseDir(baseDir.toPath().toRealPath(LinkOption.NOFOLLOW_LINKS));
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
            if (predicate.apply(inputFile)) {
                fileList.add(file);
            }
        }
        return fileList;
    }

}
