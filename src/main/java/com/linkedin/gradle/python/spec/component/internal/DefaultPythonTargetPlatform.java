package com.linkedin.gradle.python.spec.component.internal;

import com.linkedin.gradle.python.internal.platform.PythonVersion;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.groovy.runtime.ProcessGroovyMethods;
import org.gradle.api.GradleException;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.util.VersionNumber;

import java.io.File;
import java.io.IOException;


public class DefaultPythonTargetPlatform implements PythonTargetPlatform {

    private static final Logger logger = Logging.getLogger(DefaultPythonTargetPlatform.class);

    private final PythonVersion version;
    private final File pythonExecutable;

    public DefaultPythonTargetPlatform(OperatingSystem operatingSystem, String python) {
        if (new File(python).exists()) {
            pythonExecutable = new File(python);
        } else if (python.startsWith("python")) {
            pythonExecutable = operatingSystem.findInPath(python);
        } else {
            pythonExecutable = operatingSystem.findInPath("python" + python);
        }

        if (pythonExecutable == null) {
            throw new GradleException("Could not find " + python + " in PATH");
        } else if (!pythonExecutable.canExecute()) {
            throw new GradleException("Unable to execute " + pythonExecutable.getAbsolutePath());
        }

        String versionString;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(pythonExecutable.getAbsolutePath(), "--version");
            processBuilder.redirectErrorStream(true);
            Process exec = processBuilder.start();
            ProcessGroovyMethods.waitForOrKill(exec, 5000);
            versionString = IOUtils.toString(exec.getInputStream());
            logger.debug("Python version for {} is {}", pythonExecutable.getAbsolutePath(), versionString);
        } catch (IOException e) {
            logger.error("Unable to execute {}", pythonExecutable.getAbsolutePath(), e);
            throw new GradleException("Unable to execute " + pythonExecutable.getAbsolutePath());
        }

        String trimmedVersionString = StringUtils.trimToEmpty(versionString.split(" ")[1]);
        VersionNumber versionNumber = VersionNumber.parse(trimmedVersionString);
        String majorMinorString = String.format("%d.%d", versionNumber.getMajor(), versionNumber.getMinor());
        logger.debug("Python MAJOR.MINOR {}", majorMinorString);
        version = PythonVersion.parse(majorMinorString);
    }

    public File getPythonExecutable() {
        return pythonExecutable;
    }

    @Override
    public PythonVersion getVersion() {
        return version;
    }

    @Override
    public String getVersionAsString() {
        return getVersion().getVersionString();
    }

    @Override
    public String getDisplayName() {
        return String.format("Python %s", getVersion().getVersionString());
    }

    @Override
    public String getName() {
        return getDisplayName();
    }

    @Override
    public String toString() {
        return String.format("DefaultPythonTargetPlatform{version=%s, pythonExecutable=%s}", version, pythonExecutable);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DefaultPythonTargetPlatform that = (DefaultPythonTargetPlatform) o;

        if (version != that.version) {
            return false;
        }
        return pythonExecutable != null ? pythonExecutable.equals(that.pythonExecutable) : that.pythonExecutable == null;
    }

    @Override
    public int hashCode() {
        int result = version != null ? version.hashCode() : 0;
        result = 31 * result + (pythonExecutable != null ? pythonExecutable.hashCode() : 0);
        return result;
    }
}
