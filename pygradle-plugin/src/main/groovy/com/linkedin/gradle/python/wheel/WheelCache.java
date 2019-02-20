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
    Optional<File> findWheel(String library, String version, PythonDetails pythonDetails);

    /**
     * Find wheel based on cache layer.
     *
     * @param library         name of the library
     * @param version         version of the library
     * @param pythonDetails   details on the python to find a wheel for
     * @param wheelCacheLayer which {@link WheelCacheLayer} to fetch wheel
     * @return a wheel that could be used in the target layer. If not found, {@code Optional.empty()}
     */
    Optional<File> findWheel(String library, String version, PythonDetails pythonDetails, WheelCacheLayer wheelCacheLayer);

    /**
     * Store given wheel file to target layer.
     *
     * @param wheelFile       the wheel file to store
     * @param wheelCacheLayer which {@link WheelCacheLayer} to store wheel
     */
    void storeWheel(File wheelFile, WheelCacheLayer wheelCacheLayer);
}
