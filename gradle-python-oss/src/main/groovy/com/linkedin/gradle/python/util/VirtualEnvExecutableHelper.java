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

import java.io.File;

public class VirtualEnvExecutableHelper {

    private VirtualEnvExecutableHelper() {

    }

    public static File getPythonInterpreter(PythonExtension pythonExtension) {
        return pythonExtension.getDetails().getVirtualEnvInterpreter();
    }

    public static File getPip(PythonExtension pythonExtension) {
        return getExecutable(pythonExtension, "bin/pip");
    }

    public static File getPex(PythonExtension pythonExtension) {
        return getExecutable(pythonExtension, "bin/pex");
    }

    public static File getExecutable(File file) {
        if(!file.exists()) {
            throw new RuntimeException("Could not find " + file.getAbsolutePath() + " to execute");
        }

        return file;
    }

    public static File getExecutable(PythonExtension pythonExtension, String path) {
        return getExecutable(new File(pythonExtension.getDetails().getVirtualEnv(), path));
    }

    public static File findExecutable(PythonExtension pythonExtension, String path) {
        return new File(pythonExtension.getDetails().getVirtualEnv(), path);
    }
}
