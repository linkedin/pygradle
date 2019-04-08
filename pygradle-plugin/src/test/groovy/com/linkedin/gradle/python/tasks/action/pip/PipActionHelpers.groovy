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
package com.linkedin.gradle.python.tasks.action.pip

import com.linkedin.gradle.python.util.DefaultPackageSettings
import com.linkedin.gradle.python.util.PackageInfo
import org.junit.rules.TemporaryFolder

import java.nio.file.Paths

class PipActionHelpers {
    private PipActionHelpers() {
        //NOOP
    }

    static class RequiresRebuildOverridePackageSettings extends DefaultPackageSettings {
        private final List<String> override

        RequiresRebuildOverridePackageSettings(TemporaryFolder temporaryFolder, List<String> override) {
            super(temporaryFolder.root)
            this.override = override
        }

        @Override
        boolean requiresSourceBuild(PackageInfo packageInfo) {
            return override.contains(packageInfo.name)
        }
    }

    static class CustomizedOverridePackageSettings extends DefaultPackageSettings {
        private final List<String> override

        CustomizedOverridePackageSettings(TemporaryFolder temporaryFolder, List<String> override) {
            super(temporaryFolder.root)
            this.override = override
        }

        @Override
        boolean isCustomized(PackageInfo packageInfo) {
            return override.contains(packageInfo.name)
        }
    }

    static class SupportedLanguageVersionOverridePackageSettings extends DefaultPackageSettings {
        private final Map<String, List<String>> override

        SupportedLanguageVersionOverridePackageSettings(TemporaryFolder temporaryFolder, Map<String, List<String>> override) {
            super(temporaryFolder.root)
            this.override = override
        }

        @Override
        List<String> getSupportedLanguageVersions(PackageInfo packageInfo) {
            return override.getOrDefault(packageInfo.name, [])
        }
    }

    static class InstallOptionOverridePackageSettings extends DefaultPackageSettings {
        private final Map<String, List<String>> override

        InstallOptionOverridePackageSettings(TemporaryFolder temporaryFolder, Map<String, List<String>> override) {
            super(temporaryFolder.root)
            this.override = override
        }

        @Override
        List<String> getInstallOptions(PackageInfo packageInfo) {
            return override.getOrDefault(packageInfo.name, [])
        }

        @Override
        boolean isCustomized(PackageInfo packageInfo) {
            return override.containsKey(packageInfo.name)
        }
    }

    static class GlobalOptionOverridePackageSettings extends DefaultPackageSettings {
        private final Map<String, List<String>> override

        GlobalOptionOverridePackageSettings(TemporaryFolder temporaryFolder, Map<String, List<String>> override) {
            super(temporaryFolder.root)
            this.override = override
        }

        @Override
        List<String> getGlobalOptions(PackageInfo packageInfo) {
            return override.getOrDefault(packageInfo.name, [])
        }

        @Override
        boolean isCustomized(PackageInfo packageInfo) {
            return override.containsKey(packageInfo.name)
        }
    }

    static class EnvOverridePackageSettings extends DefaultPackageSettings {
        private final Map<String, Map<String, String>> envOverride

        EnvOverridePackageSettings(TemporaryFolder temporaryFolder, Map<String, Map<String, String>> envOverride) {
            super(temporaryFolder.root)
            this.envOverride = envOverride
        }

        @Override
        Map<String, String> getEnvironment(PackageInfo packageInfo) {
            envOverride.getOrDefault(packageInfo.name, [:])
        }

        @Override
        boolean isCustomized(PackageInfo packageInfo) {
            return envOverride.containsKey(packageInfo.name)
        }

    }

    static class BuildOptionOverridePackageSetting extends DefaultPackageSettings {
        private final Map<String, List<String>> override

        BuildOptionOverridePackageSetting(TemporaryFolder temporaryFolder, Map<String, List<String>> override) {
            super(temporaryFolder.root)
            this.override = override
        }

        @Override
        List<String> getBuildOptions(PackageInfo packageInfo) {
            return override.getOrDefault(packageInfo.name, [])
        }

        @Override
        boolean isCustomized(PackageInfo packageInfo) {
            return override.containsKey(packageInfo.name)
        }
    }

    static PackageInfo packageInGradleCache(String name) {
        return PackageInfo.fromPath(Paths.get("foo", ".gradle", "caches", name))
    }
}
