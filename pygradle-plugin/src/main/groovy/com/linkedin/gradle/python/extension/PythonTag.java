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

import org.gradle.api.Project;
import org.gradle.process.ExecResult;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Implementation of https://www.python.org/dev/peps/pep-0425
public class PythonTag implements Serializable {
    public static final PythonTag C_PYTHON = new PythonTag("cp", "cpython");
    public static final PythonTag IRON_PYTHON = new PythonTag("ip", "ironpython");
    public static final PythonTag PY_PY = new PythonTag("pp", "pypy");
    public static final PythonTag JYTHON = new PythonTag("jy", "jython");

    private static final PythonTag[] KNOWN_TAGS = new PythonTag[]{C_PYTHON, IRON_PYTHON, PY_PY, JYTHON};

    private final String prefix;
    private final String implementation;

    private PythonTag(String prefix, String implementation) {
        this.prefix = prefix;
        this.implementation = implementation;
    }

    public String getPrefix() {
        return prefix;
    }

    static public PythonTag findTag(Project project, PythonDetails pythonDetails) {
        List<String> args = new ArrayList<>(Arrays.asList(pythonDetails.getVirtualEnvInterpreter().getAbsolutePath(), "-c"));

        if ("2".equals(pythonDetails.getPythonVersion().getPythonMajor())) {
            args.add("import platform; print(platform.python_implementation())");
        } else {
            args.add("import sys; print(sys.implementation.name)");
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        ExecResult result = project.exec(exec -> {
            exec.commandLine(args);
            exec.setStandardOutput(stream);
            exec.setIgnoreExitValue(true);
        });

        if (result.getExitValue() != 0) { // pick something sane
            return C_PYTHON;
        }

        String pythonImplementation = stream.toString().trim();
        for (PythonTag knownTag : KNOWN_TAGS) {
            if (knownTag.implementation.equalsIgnoreCase(pythonImplementation)) {
                return knownTag;
            }
        }

        // https://www.python.org/dev/peps/pep-0425/#id11 says
        // "Other Python implementations should use sys.implementation.name"
        return new PythonTag(pythonImplementation, pythonImplementation);
    }

    @Override
    public String toString() {
        return "PythonTag{"
            + "prefix='" + prefix + '\''
            + ", implementation='" + implementation + '\''
            + '}';
    }
}
