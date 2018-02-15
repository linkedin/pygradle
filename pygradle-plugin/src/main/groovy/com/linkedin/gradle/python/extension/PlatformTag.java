package com.linkedin.gradle.python.extension;

import org.gradle.api.Project;

import java.io.*;

public class PlatformTag implements Serializable {

    private final String platform;

    public PlatformTag(String platform) {
        this.platform = platform;
    }

    public String getPlatform() {
        return platform;
    }

    public static PlatformTag makePlatformTag(Project project, File pythonInterpreter) {
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {

            project.exec(exec -> {
                exec.commandLine(pythonInterpreter.getAbsolutePath(),
                    "-c",
                    "import distutils; print(distutils.util.get_platform())");
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
