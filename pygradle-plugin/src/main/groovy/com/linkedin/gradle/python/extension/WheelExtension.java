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
package com.linkedin.gradle.python.extension;

import com.linkedin.gradle.python.wheel.WheelCacheLayer;
import org.gradle.api.Project;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;


public class WheelExtension {

    private File wheelCache;
    private File hostLayerWheelCache;
    private File projectLayerWheelCache;
    private Map<WheelCacheLayer, File> layeredCacheMap = new LinkedHashMap<>();

    public WheelExtension(Project project) {
        wheelCache = new File(project.getBuildDir(), "wheel-cache");
    }

    public File getWheelCache() {
        return wheelCache;
    }

    public void setWheelCache(File wheelCache) {
        this.wheelCache = wheelCache;
    }

    public Optional<File> getHostLayerWheelCache() {
        return Optional.ofNullable(hostLayerWheelCache);
    }

    public void setHostLayerWheelCache(File hostLayerWheelCache) {
        this.hostLayerWheelCache = hostLayerWheelCache;
        layeredCacheMap.put(WheelCacheLayer.HOST_LAYER, this.hostLayerWheelCache);
    }

    public Optional<File> getProjectLayerWheelCache() {
        return Optional.ofNullable(projectLayerWheelCache);
    }

    public void setProjectLayerWheelCache(File projectLayerWheelCache) {
        this.projectLayerWheelCache = projectLayerWheelCache;
        layeredCacheMap.put(WheelCacheLayer.PROJECT_LAYER, this.projectLayerWheelCache);
    }

    public Map<WheelCacheLayer, File> getLayeredCacheMap() {
        return layeredCacheMap;
    }
}
