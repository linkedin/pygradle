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
import org.gradle.api.Task;
import org.gradle.api.specs.Spec;

import javax.annotation.Nullable;

/**
 * Interface to mark tasks that support package filtering.
 */
public interface SupportsPackageFiltering extends HasPythonDetails, Task {

    /**
     * @return the filter, may be null.
     */
    @Nullable
    Spec<PackageInfo> getPackageExcludeFilter();

    /**
     * Set the package filter
     */
    void setPackageExcludeFilter(Spec<PackageInfo> filter);
}
