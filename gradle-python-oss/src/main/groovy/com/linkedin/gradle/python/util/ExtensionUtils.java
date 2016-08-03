/**
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

import com.linkedin.gradle.python.PythonExtension;
import com.linkedin.gradle.python.extension.CliExtension;
import com.linkedin.gradle.python.extension.DeployableExtension;
import com.linkedin.gradle.python.extension.PexExtension;
import com.linkedin.gradle.python.extension.WheelExtension;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.ExtensionContainer;

public class ExtensionUtils {

    private ExtensionUtils() {
        //private constructor for util class
    }

    private static <T> T maybeCreate(Project project, String name, Class<T> type, Object... args) {
        PythonExtension component = getPythonExtension(project);
        ExtensionContainer extensionContainer = ((ExtensionAware) component).getExtensions();

        T maybeExtension = extensionContainer.findByType(type);
        if (maybeExtension == null) {
            maybeExtension = extensionContainer.create(name, type, args);
        }
        return maybeExtension;
    }

    public static DeployableExtension maybeCreateDeployableExtension(Project project) {
        return maybeCreate(project, "deployable", DeployableExtension.class, project);
    }

    public static PexExtension maybeCreatePexExtension(Project project) {
        return maybeCreate(project, "pex", PexExtension.class, project);
    }

    public static WheelExtension maybeCreateWheelExtension(Project project) {
        return maybeCreate(project, "wheel", WheelExtension.class, project);
    }

    public static CliExtension maybeCreateCliExtension(Project project) {
        return maybeCreate(project, "cli", CliExtension.class);
    }

    public static <T> T getPythonComponentExtension(Project project, Class<T> type) {
        PythonExtension component = getPythonExtension(project);
        ExtensionContainer extensionContainer = ((ExtensionAware) component).getExtensions();
        return extensionContainer.getByType(type);
    }

    public static <T> T findPythonComponentExtension(Project project, Class<T> type) {
        PythonExtension component = getPythonExtension(project);
        ExtensionContainer extensionContainer = ((ExtensionAware) component).getExtensions();
        return extensionContainer.findByType(type);
    }

    public static PythonExtension getPythonExtension(Project project) {
        return project.getExtensions().getByType(PythonExtension.class);
    }
}
