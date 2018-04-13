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
package com.linkedin.gradle.python.tasks;

import com.linkedin.gradle.python.extension.PythonDetails;
import com.linkedin.gradle.python.tasks.action.CreateVirtualEnvAction;
import com.linkedin.gradle.python.tasks.execution.FailureReasonProvider;
import com.linkedin.gradle.python.tasks.execution.TeeOutputContainer;
import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class InstallVirtualEnvironmentTask extends DefaultTask implements FailureReasonProvider {
    private PythonDetails pythonDetails;
    @Input
    @Optional
    private String distutilsCfg;

    private final TeeOutputContainer container = new TeeOutputContainer();

    @InputFiles
    public Configuration getPyGradleBootstrap() {
        return getProject().getConfigurations().getByName("pygradleBootstrap");
    }

    @OutputFile
    public File getVirtualEnvDir() {
        return pythonDetails.getVirtualEnvInterpreter();
    }

    @TaskAction
    public void installVEnv() {
        CreateVirtualEnvAction action = new CreateVirtualEnvAction(getProject(), pythonDetails);
        action.buildVenv(file -> {
            try {
                customize(file);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    @Override
    public String getReason() {
        return container.getCommandOutput();
    }

    public void setPythonDetails(PythonDetails pythonDetails) {
        this.pythonDetails = pythonDetails;
    }

    public PythonDetails getPythonDetails() {
        return pythonDetails;
    }

    private void customize(File preInstallVenv) throws IOException {
        if (distutilsCfg != null) {
            final Path path = preInstallVenv.toPath();
            Files.write(path.resolve(Paths.get("virtualenv_embedded", "distutils.cfg")), distutilsCfg.getBytes(), StandardOpenOption.APPEND);
            final ByteArrayOutputStream stream = new ByteArrayOutputStream();
            getProject().exec(execSpec -> {
                execSpec.commandLine(getPythonDetails().getSystemPythonInterpreter(), path.resolve(Paths.get("bin", "rebuild-script.py")).toFile());
                execSpec.setStandardOutput(stream);
                execSpec.setErrorOutput(stream);
            });
            getLogger().info("Customized distutils.cfg");
        }
    }

    public String getDistutilsCfg() {
        return distutilsCfg;
    }

    public void setDistutilsCfg(String distutilsCfg) {
        this.distutilsCfg = distutilsCfg;
    }

}
