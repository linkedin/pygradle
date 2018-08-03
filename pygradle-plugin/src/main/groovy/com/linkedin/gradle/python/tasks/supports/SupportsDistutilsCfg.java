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

import org.gradle.api.Task;

/**
 * A way to use tasks.withType for tasks that support Distutils mutations
 */
public interface SupportsDistutilsCfg extends Task {

    /**
     * Set the dist utils text
     * @param cfg string to be added to the end of the distutils file
     */
    void setDistutilsCfg(String cfg);

    /**
     * @return Get the string to be appended to distutils config file
     */
    String getDistutilsCfg();
}
