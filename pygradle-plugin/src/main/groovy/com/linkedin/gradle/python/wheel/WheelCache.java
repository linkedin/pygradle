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
package com.linkedin.gradle.python.wheel;

import com.linkedin.gradle.python.extension.PythonDetails;

import java.io.File;
import java.io.Serializable;
import java.util.Optional;
import java.util.function.Function;

public interface WheelCache extends Serializable {
    /**
     * A filter will prevent a Cache Hit when the {@link Function} returns true.
     *
     * This is useful if you want to exclude pre-release/chaning builds from using the cache.
     *
     * @param filter returning true, will prevent version from matching in the cache.
     */
    void addVersionFilter(Function<String, Boolean> filter);

    /**
     * Given that we have versions that can be filtered, we should also prevent wheels from being built for them.
     *
     * @param version version to check for.
     * @return true when the cacheable wheel should be built.
     */
    boolean isWheelForVersionCacheable(String version);

    /**
     * A filter will prevent a Cache Hit when the {@link Function} returns true.
     *
     * This is useful if you want to exclude pre-release/chaning builds from using the cache.
     *
     * @param filter returning true, will prevent version from matching in the cache.
     */
    void addDependencyFilter(Function<String, Boolean> filter);

    /**
     * If a dependency shouldn't have a wheel built, then we don't want to build wheels for it either.
     *
     * @param dependencyName Dependency Name
     * @return true, when the cacheable wheel should be built.
     */
    boolean isWheelForDependencyCacheable(String dependencyName);

    Optional<File> findWheel(String library, String version, PythonDetails pythonDetails);
}
