package com.linkedin.gradle.python.extension;

import org.gradle.api.Project;
import org.gradle.process.ExecResult;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PythonTag implements Serializable {
    public static final PythonTag Clean = new PythonTag("pypy", "clean");
    public static final PythonTag CPython = new PythonTag("cp", "cpython");
    public static final PythonTag IronPython = new PythonTag("ip", "ironpython");
    public static final PythonTag PyPy = new PythonTag("pp", "pypy");
    public static final PythonTag Jython = new PythonTag("jy", "python");

    private static final PythonTag[] KNOWN_TAGS = new PythonTag[]{CPython, IronPython, PyPy, Jython};

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

        if(result.getExitValue() != 0) { // pick something sane
            return CPython;
        }

        String pythonImplementation = stream.toString().trim();
        for (PythonTag knownTag : KNOWN_TAGS) {
            if (knownTag.implementation.equalsIgnoreCase(pythonImplementation)) {
                return knownTag;
            }
        }

        return new PythonTag(pythonImplementation, pythonImplementation);
    }

    @Override
    public String toString() {
        return "PythonTag{" +
            "prefix='" + prefix + '\'' +
            ", implementation='" + implementation + '\'' +
            '}';
    }
}
