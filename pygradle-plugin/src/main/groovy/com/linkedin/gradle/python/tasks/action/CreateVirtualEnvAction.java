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
package com.linkedin.gradle.python.tasks.action;

import com.linkedin.gradle.python.extension.PythonDetails;
import com.linkedin.gradle.python.tasks.execution.TeeOutputContainer;
import com.linkedin.gradle.python.util.pip.PipConfFile;
import com.linkedin.gradle.python.wheel.EditablePythonAbiContainer;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.process.ExecResult;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

public class CreateVirtualEnvAction {

    private static Logger log = Logging.getLogger(CreateVirtualEnvAction.class);
    private final Project project;
    private final PythonDetails pythonDetails;
    private final EditablePythonAbiContainer editablePythonAbiContainer;
    private final TeeOutputContainer container = new TeeOutputContainer();

    public CreateVirtualEnvAction(Project project, PythonDetails pythonDetails,
                                  EditablePythonAbiContainer editablePythonAbiContainer) {
        this.project = project;
        this.pythonDetails = pythonDetails;
        this.editablePythonAbiContainer = editablePythonAbiContainer;
    }

    public void buildVenv(@Nullable Consumer<File> customize) {
        PipConfFile pipConfFile = new PipConfFile(project, pythonDetails);

        File packageDir = makeTempDir().toFile();
        getPyGradleBootstrap(project).getFiles().forEach(file -> {
            project.copy(copySpec -> {
                copySpec.from(project.tarTree(file.getPath()));
                copySpec.into(packageDir);
                copySpec.eachFile(it -> {
                    // Remove the virtualenv-<version> from the file.
                    Path pathInsideTar = Paths.get(it.getPath());
                    if (pathInsideTar.getNameCount() > 1) {
                        it.setPath(pathInsideTar.subpath(1, pathInsideTar.getNameCount()).toString());
                    }
                });
            });
        });

        if (null != customize) {
            customize.accept(packageDir);
        }

        // In virtualenv-16.1.0 the install script was relocated and will be in 17+.
        Path installScriptPath = Paths.get(packageDir.toString(), "virtualenv.py");
        if (!Files.exists(installScriptPath)) {
            installScriptPath = Paths.get(packageDir.toString(), "src", "virtualenv.py");
        }

        final File installScript = installScriptPath.toFile();
        OutputStream outputStream = new ByteArrayOutputStream();
        ExecResult execResult = project.exec(execSpec -> {
            container.setOutputs(execSpec);
            execSpec.commandLine(
                pythonDetails.getSystemPythonInterpreter(),
                installScript,
                "--never-download",
                "--python", pythonDetails.getSystemPythonInterpreter(),
                "--prompt", pythonDetails.getVirtualEnvPrompt(),
                pythonDetails.getVirtualEnv()
            );
            execSpec.setErrorOutput(outputStream);
            execSpec.setStandardOutput(outputStream);
            execSpec.setIgnoreExitValue(true);
        });

        if (log.isInfoEnabled()) {
            log.info(outputStream.toString());
        } else if (execResult.getExitValue() != 0) {
            log.lifecycle(outputStream.toString());
        }

        execResult.assertNormalExitValue();

        ProbeVenvInfoAction.probeVenv(project, pythonDetails, editablePythonAbiContainer);

        project.delete(packageDir);
        try {
            pipConfFile.buildPipConfFile();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Configuration getPyGradleBootstrap(Project project) {
        return project.getConfigurations().getByName("pygradleBootstrap");
    }

    private Path makeTempDir() {
        try {
            return Files.createTempDirectory("virtualenv-dir");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
