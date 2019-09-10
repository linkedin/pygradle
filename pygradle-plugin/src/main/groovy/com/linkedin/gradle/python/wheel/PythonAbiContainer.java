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

import java.io.File;
import java.io.Serializable;

/**
 * Provides a mechanism for tooling to check if a python executable is compatible with a python
 * component.
 */
public interface PythonAbiContainer extends Serializable {
    /**
     * Checks to see if a python executable is compatible with a python version, python abi, and platform.
     *
     * For pythonTag, abiTag, and platformTag you can use a dot (.) between values to make multiple. For example
     * py2.py3 is python2 and python3.
     *
     * @param pythonExecutable to check against
     * @param pythonTag version of python (ex. py2.py3)
     * @param abiTag the ABI for the python exec
     * @param platformTag platform required
     * @return true is the python exec is compatible
     */
    boolean matchesSupportedVersion(File pythonExecutable, String pythonTag, String abiTag, String platformTag);

    /**
     * Create and return a new object with the same containers
     */
    PythonAbiContainer copy();
}
