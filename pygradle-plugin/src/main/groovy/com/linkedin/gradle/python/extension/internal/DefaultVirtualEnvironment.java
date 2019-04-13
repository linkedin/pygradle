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
package com.linkedin.gradle.python.extension.internal;

import com.linkedin.gradle.python.extension.PythonDetails;
import com.linkedin.gradle.python.extension.VirtualEnvironment;
import com.linkedin.gradle.python.util.OperatingSystem;

import java.io.File;
import java.nio.file.Path;

import static com.linkedin.gradle.python.extension.PythonDetailsFactory.getPythonApplicationDirectory;

public class DefaultVirtualEnvironment implements VirtualEnvironment {

    private final PythonDetails details;

    DefaultVirtualEnvironment(PythonDetails details) {
        this.details = details;
    }

    @Override public File getPip() {
        return getExecutable("pip");
    }

    @Override public File getPex() {
        return getExecutable("pex");
    }

    private File validateFileExists(Path path) {
        File file = path.toFile();
        if (!file.exists()) {
            throw new RuntimeException("Could not find " + path.toString() + " to execute");
        }

        return file;
    }

    @Override
    public File getExecutable(String name) {
        return validateFileExists(findExecutable(name).toPath());
    }

    @Override
    public File getScript(String name) {
        return validateFileExists(prefixBuilder().resolve(OperatingSystem.current().getScriptName(name)));
    }

    @Override
    public File findExecutable(String name) {
        return findExecutable(prefixBuilder(), name);
    }

    public static File findExecutable(Path path, String name) {
        return path.resolve(OperatingSystem.current().getExecutableName(name)).toFile();
    }

    private Path prefixBuilder() {
        return details.getVirtualEnv().toPath().resolve(getPythonApplicationDirectory());
    }
}
