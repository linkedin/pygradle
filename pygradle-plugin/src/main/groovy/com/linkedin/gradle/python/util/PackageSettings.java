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
 * Interface for package specific settings.
 *
 * The settings needed for a specific package to build properly
 * or to customize its build can be obtained from the API provided
 * by this interface.
 *
 * @param <T> Type of the object that represents package information.
 */
public interface PackageSettings<T> {
    /**
     * Get the build environment required for this package.
     *
     * @param t package information object
     * @return a mapping of environment variables and their values
     */
    Map<String, String> getEnvironment(T t);

    /**
     * Get the global options for the package installation.
     * <p>
     * An example is "--global-option" for the "pip" command.
     *
     * @param t package information object
     * @return a list of global options
     */
    List<String> getGlobalOptions(T t);

    /**
     * Get the install options for the package.
     * <p>
     * An example is "--install-option" for the "pip install" command.
     *
     * @param t package information object
     * @return a list of install options
     */
    List<String> getInstallOptions(T t);

    /**
     * Get the build options for the binary version of this package.
     * <p>
     * An example is "--build-option" for the "pip wheel" command.
     *
     * @param t package information object
     * @return a list of build options
     */
    List<String> getBuildOptions(T t);

    /**
     * Get the configure options for the package.
     * <p>
     * For example, the options for "./configure" command when building
     * a C program.
     *
     * @param t package information object
     * @return a list of configure options
     */
    List<String> getConfigureOptions(T t);

    /**
     * Get the language versions supported by the package.
     * <p>
     * An example is Python package support for 2.7, 3.5, and 3.6
     * versions of the language. The major version can be also
     * specified, such as 2 or 3.
     *
     * @param t package information object
     * @return a list of install options
     */
    List<String> getSupportedLanguageVersions(T t);

    /**
     * Determines if the package requires a build from source.
     * <p>
     * Even if the binary artifact of the package is available,
     * the package may need a rebuild because of editable current
     * project or snapshots.
     *
     * @param t package information object
     * @return true when the package needs a rebuild from source
     */
    boolean requiresSourceBuild(T t);

    /**
     * Checks if the package is customized.
     *
     * @param t package information object
     * @return true when the package is customized
     */
    boolean isCustomized(T t);
}
