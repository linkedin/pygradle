package com.linkedin.gradle.python.extension;

import org.gradle.api.Project;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;

public class PlatformTag implements Serializable {

    private final String platform;

    public PlatformTag(String platform) {
        this.platform = platform;
    }

    public String getPlatform() {
        return platform;
    }

    public static PlatformTag makePlatformTag(Project project, PythonDetails pythonDetails) {
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {

            List<String> args = Arrays.asList(pythonDetails.getVirtualEnvInterpreter().getAbsolutePath(),
                "-c",
                "import distutils; print(distutils.util.get_platform())");


            project.exec(exec -> {
                exec.commandLine(args);
                exec.setStandardOutput(stream);
            });

            String platform = stream.toString().trim().replace('-', '_').replace('.', '_');
            return new PlatformTag(platform);

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String toString() {
        return "PlatformTag{" +
            "platform='" + platform + '\'' +
            '}';
    }
}
