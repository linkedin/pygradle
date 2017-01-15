package com.linkedin.gradle.python.extension;

import com.linkedin.gradle.python.util.OperatingSystem;

import java.io.File;
import java.nio.file.Path;

public class VirtualEnvironment {

    private final PythonDetails details;

    public VirtualEnvironment(PythonDetails details) {
        this.details = details;
    }

    public File getPip() {
        return getExecutable("pip");
    }

    public File getPex() {
        return getExecutable("pex");
    }


    public File validateFileExists(Path path) {
        File file = path.toFile();
        if (!file.exists()) {
            throw new RuntimeException("Could not find " + path.toString() + " to execute");
        }

        return file;
    }

    public static String getPythonApplicationDirectory() {
        return OperatingSystem.current() == OperatingSystem.WINDOWS ? "Scripts" : "bin";
    }

    public File getExecutable(String path) {
        return validateFileExists(prefixBuilder().resolve(OperatingSystem.current().getExecutableName(path)));
    }

    public File getScript(String path) {
        return validateFileExists(prefixBuilder().resolve(OperatingSystem.current().getScriptName(path)));
    }

    public File findExecutable(String path) {
        return prefixBuilder().resolve(OperatingSystem.current().getExecutableName(path)).toFile();
    }

    private Path prefixBuilder() {
        return details.getVirtualEnv().toPath().resolve(getPythonApplicationDirectory());
    }
}
