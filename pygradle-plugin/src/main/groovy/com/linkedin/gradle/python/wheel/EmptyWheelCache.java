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
import java.util.Optional;

public class EmptyWheelCache implements WheelCache {
    @Override
    public Optional<File> findWheel(String name, String version, PythonDetails pythonDetails) {
        return Optional.empty();
    }

    @Override
    public Optional<File> findWheel(String name, String version, PythonDetails pythonDetails, WheelCacheLayer wheelCacheLayer) {
        return Optional.empty();
    }

    @Override
    public void storeWheel(File wheel) { }

    @Override
    public void storeWheel(File wheel, WheelCacheLayer wheelCacheLayer) { }

    @Override
    public Optional<File> getTargetDirectory() {
        return Optional.empty();
    }

    @Override
    public boolean isWheelsReady() {
        return false;
    }

    @Override
    public void setWheelsReady(boolean wheelsReady) { }
}
