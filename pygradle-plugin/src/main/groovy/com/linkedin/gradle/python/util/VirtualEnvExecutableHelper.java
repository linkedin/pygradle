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

import com.linkedin.gradle.python.PythonExtension;
import com.linkedin.gradle.python.extension.PythonDetails;

import java.io.File;

public class VirtualEnvExecutableHelper {

    private VirtualEnvExecutableHelper() {
        //private constructor for util class
    }

    @Deprecated
    public static File getPythonInterpreter(PythonExtension pythonExtension) {
        return getPythonInterpreter(pythonExtension.getDetails());
    }

    public static File getPythonInterpreter(PythonDetails pythonDetails) {
        return pythonDetails.getVirtualEnvInterpreter();
    }

    @Deprecated
    public static File getPip(PythonExtension pythonExtension) {
        return getPip(pythonExtension.getDetails());
    }

    public static File getPip(PythonDetails pythonDetails) {
        return getExecutable(pythonDetails, "bin/pip");
    }

    @Deprecated
    public static File getPex(PythonExtension pythonExtension) {
        return getPex(pythonExtension.getDetails());
    }

    public static File getPex(PythonDetails pythonDetails) {
        return getExecutable(pythonDetails, "bin/pex");
    }

    public static File getExecutable(File file) {
        if (!file.exists()) {
            throw new RuntimeException("Could not find " + file.getAbsolutePath() + " to execute");
        }

        return file;
    }

    @Deprecated
    public static File getExecutable(PythonExtension pythonExtension, String path) {
        return getExecutable(pythonExtension.getDetails(), path);
    }

    public static File getExecutable(PythonDetails pythonDetails, String path) {
        return getExecutable(new File(pythonDetails.getVirtualEnv(), path));
    }

    @Deprecated
    public static File findExecutable(PythonExtension pythonExtension, String path) {
        return findExecutable(pythonExtension.getDetails(), path);
    }

    public static File findExecutable(PythonDetails pythonDetails, String path) {
        return new File(pythonDetails.getVirtualEnv(), path);
    }
}
