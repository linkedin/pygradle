/*
 * Copyright 2016 LinkedIn Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.linkedin.gradle.python.util;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of PackageSettings.
 *
 * Returns empty collections mostly.
 * Handles snapshots and project package rebuilds automatically.
 */
public class DefaultPackageSettings implements PackageSettings<PackageInfo> {
    private static final String PIP_EDITABLE = "--editable";
    private static final String PIP_IGNORE_INSTALLED = "--ignore-installed";

    private final File projectDir;

    public DefaultPackageSettings(File projectDir) {
        this.projectDir = projectDir;
    }

    @Override
    public Map<String, String> getEnvironment(PackageInfo packageInfo) {
        return Collections.emptyMap();
    }

    @Override
    public List<String> getGlobalOptions(PackageInfo packageInfo) {
        return Collections.emptyList();
    }

    @Override
    public List<String> getInstallOptions(PackageInfo packageInfo) {
        String version = packageInfo.getVersion();
        List<String> options = new ArrayList<>();

        // always reinstall snapshots
        if ((version != null && version.contains("-")) || requiresSourceBuild(packageInfo)) {
            options.add(PIP_IGNORE_INSTALLED);
        }

        /*
         * The current project is installed editable.
         * This option **must be last** because it expects the directory name after it.
         */
        if (isProjectDirectory(packageInfo)) {
            options.add(PIP_EDITABLE);
        }

        return options;
    }

    @Override
    public List<String> getBuildOptions(PackageInfo packageInfo) {
        return Collections.emptyList();
    }

    @Override
    public List<String> getConfigureOptions(PackageInfo packageInfo) {
        return Collections.emptyList();
    }

    @Override
    public List<String> getSupportedLanguageVersions(PackageInfo packageInfo) {
        return Collections.emptyList();
    }

    @Override
    public boolean requiresSourceBuild(PackageInfo packageInfo) {
        String version = packageInfo.getVersion();

        // always rebuild the project package itself
        if (isProjectDirectory(packageInfo)) {
            return true;
        }
        // always rebuild snapshots; otherwise no rebuild required, per semver versions with '-' are pre-release
        return (version != null && version.contains("-"));
    }

    @Override
    public boolean isCustomized(PackageInfo packageInfo) {
        // return false by default as no custom build options.
        return false;
    }

    private boolean isProjectDirectory(PackageInfo packageInfo) {
        File packageDir = packageInfo.getPackageFile();
        String version = packageInfo.getVersion();
        return version == null && Files.isDirectory(packageDir.toPath()) && projectDir.equals(packageDir);
    }
}
