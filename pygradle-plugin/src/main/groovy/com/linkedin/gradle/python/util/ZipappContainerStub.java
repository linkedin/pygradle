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

import org.gradle.api.Project;


/**
 * This class provides a stub in PythonExtension so that build.gradle files
 * can use the following construct:
 *
 * python {
 *     zipapp.isFat = true
 * }
 *
 * This is the replacement for `python.pex.fatPex`, `python.pex.isFat`, and
 * `python.shiv.isFat`.  The reason we need this is that we don't know which
 * container format the user wants until *after* build.gradle is evaluated,
 * but of course they want to set the isFat flag *in* their build.gradle.
 *
 * Once build.gradle is evaluated, the setting of this flag is copied to the
 * actual zip application container instance.
 */
public class ZipappContainerStub implements ZipappContainer {
    private boolean isFat = OperatingSystem.current().isWindows();

    public boolean isFat() {
        return isFat;
    }

    public void setIsFat(boolean isFat) {
        this.isFat = isFat;
    }

    public void addExtensions(Project project) {
        // Should we throw an exception?
    }

    public void addDependencies(Project project) {
        // Should we throw an exception?
    }

    public void makeTasks(Project project) {
        // Should we throw an exception?
    }
}
