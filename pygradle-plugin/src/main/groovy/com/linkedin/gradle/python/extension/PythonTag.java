package com.linkedin.gradle.python.extension;

import com.linkedin.gradle.python.PythonExtension;
import org.gradle.api.Project;

import java.io.ByteArrayOutputStream;
import java.io.File;
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
    private final String implemenatation;

    private PythonTag(String prefix, String implemenatation) {
        this.prefix = prefix;
        this.implemenatation = implemenatation;
    }

    public String getPrefix() {
        return prefix;
    }

    static public PythonTag findTag(Project project, PythonDetails pythonDetails) {
        List<String> args = new ArrayList<>(Arrays.asList(pythonDetails.getVirtualEnvInterpreter().getAbsolutePath(), "-c"));

        if ("2".equals(pythonDetails.getPythonVersion().getPythonMajor())) {
            args.add("import distutils; print(distutils.util.get_platform())");
        } else {
            args.add("import sys; print(sys.implementation.name)");
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        project.exec(exec -> {
            exec.commandLine(args);
            exec.setStandardOutput(stream);
        });

        String pythonImplementation = stream.toString().trim();
        for (PythonTag knownTag : KNOWN_TAGS) {
            if (knownTag.implemenatation.equalsIgnoreCase(pythonImplementation)) {
                return knownTag;
            }
        }

        return new PythonTag(pythonImplementation, pythonImplementation);
    }

    @Override
    public String toString() {
        return "PythonTag{" +
            "prefix='" + prefix + '\'' +
            ", implemenatation='" + implemenatation + '\'' +
            '}';
    }
}
