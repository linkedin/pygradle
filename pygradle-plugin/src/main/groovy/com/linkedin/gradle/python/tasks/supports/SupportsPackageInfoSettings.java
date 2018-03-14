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
package com.linkedin.gradle.python.tasks.supports;

import com.linkedin.gradle.python.util.PackageInfo;
import com.linkedin.gradle.python.util.PackageSettings;
import org.gradle.api.Task;

/**
 * Marker interface for tasks that support PackageSettings
 */
public interface SupportsPackageInfoSettings extends Task {

    /**
     * Set the package settings
     */
    void setPackageSettings(PackageSettings<PackageInfo> settings);

    /**
     * Get the package settings
     *
     * @return package settings
     */
    PackageSettings<PackageInfo> getPackageSettings();
}
