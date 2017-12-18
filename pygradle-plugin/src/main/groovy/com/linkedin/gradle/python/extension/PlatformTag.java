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
                "import distutils.util; print(distutils.util.get_platform())");


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
        return "PlatformTag{"
            + "platform='" + platform + '\''
            + '}';
    }
}
