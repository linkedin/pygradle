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

import java.util.List;
import java.util.Map;


/**
 * Interface for environment merging utility.
 */
public interface EnvironmentMerger {
    /**
     * Merge source environment into the target environment.
     *
     * @param target the target environment to merge into
     * @param source the source environment to merge
     */
    void mergeIntoEnvironment(Map<String, String> target, Map<String, String> source);

    /**
     * Merge source environments together.
     *
     * @param sources the source environments to merge
     * @return the environment with sources merged in
     */
    Map<String, String> mergeEnvironments(List<Map<String, String>> sources);
}
