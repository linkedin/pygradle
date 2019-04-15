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

public interface WheelCache extends Serializable {
    /**
     * Finds a wheel in the cache.
     *
     * <p>The wheel can be stored in any cache layer.</p>
     *
     * @param name package name
     * @param version package version
     * @param pythonDetails the Python interpreter and other details
     * @return the wheel if found, otherwise {@code Optional.empty()}
     */
    Optional<File> findWheel(String name, String version, PythonDetails pythonDetails);

    /**
     * Finds a wheel in the cache layer.
     *
     * @param name package name
     * @param version package version
     * @param pythonDetails the Python interpreter and other details
     * @param wheelCacheLayer the {@link WheelCacheLayer} to fetch the wheel from
     * @return the wheel if found in the specified layer, otherwise {@code Optional.empty()}
     */
    Optional<File> findWheel(String name, String version, PythonDetails pythonDetails, WheelCacheLayer wheelCacheLayer);

    /**
     * Stores the wheel file into all cache layers.
     *
     * @param wheel the wheel file to store
     */
    void storeWheel(File wheel);

    /**
     * Stores the wheel file into the cache layer.
     *
     * @param wheel the wheel file to store
     * @param wheelCacheLayer the {@link WheelCacheLayer} to store the wheel in
     */
    void storeWheel(File wheel, WheelCacheLayer wheelCacheLayer);

    /**
     * Gets the default directory for wheel build target.
     *
     * <p>This should be project layer cache directory</p>
     *
     * @return the directory used for wheel build target
     */
    Optional<File> getTargetDirectory();

    /**
     * Tells if all wheels are ready for packing into a container.
     *
     * <p>If they are, we do not need to run a legacy build wheels task.
     * Otherwise, at least some still need building.</p>
     *
     * @return true when all wheels were built into the project layer cache
     */
    boolean isWheelsReady();

    /**
     * Sets the flag whether the wheels are ready for packing into a container.
     *
     * @param wheelsReady indicator of wheel readiness for packing.
     */
    void setWheelsReady(boolean wheelsReady);
}
