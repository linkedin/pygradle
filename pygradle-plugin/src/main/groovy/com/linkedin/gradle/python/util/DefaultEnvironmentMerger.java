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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of EnvironmentMerger.
 *
 * Just updates maps directly.
 */
public class DefaultEnvironmentMerger implements EnvironmentMerger {
    @Override
    public void mergeIntoEnvironment(Map<String, String> target, Map<String, String> source) {
        if (source != null) {
            target.putAll(source);
        }
    }

    @Override
    public Map<String, String> mergeEnvironments(List<Map<String, String>> sources) {
        Map<String, String> target = new HashMap<>();
        if (sources != null) {
            for (Map<String, String> source : sources) {
                mergeIntoEnvironment(target, source);
            }
        }
        return target;
    }
}
