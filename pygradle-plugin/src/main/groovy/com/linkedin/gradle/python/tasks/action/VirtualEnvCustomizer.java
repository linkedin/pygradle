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
import com.linkedin.gradle.python.tasks.exec.ExternalExec;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.process.ExecResult;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.function.Consumer;

public class VirtualEnvCustomizer implements Consumer<File> {
    private static final int NO_CHANGE = 0;
    private static final int CHANGED = 1;
    private static final Logger log = Logging.getLogger(VirtualEnvCustomizer.class);

    private final String distutilsCfg;
    private final ExternalExec exec;
    private final PythonDetails pythonDetails;

    public VirtualEnvCustomizer(String distutilsCfg, ExternalExec exec, PythonDetails pythonDetails) {
        this.distutilsCfg = distutilsCfg;
        this.exec = exec;
        this.pythonDetails = pythonDetails;
    }

    @Override
    public void accept(File file) {
        if (distutilsCfg != null) {
            final Path path = file.toPath();
            try {
                Files.write(path.resolve(
                    Paths.get("virtualenv_embedded", "distutils.cfg")),
                    distutilsCfg.getBytes(),
                    StandardOpenOption.APPEND);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

            // Since virtualenv-16.2.0 the rebuild script was renamed and relocated.
            Path rebuildScriptPath = path.resolve(Paths.get("bin", "rebuild-script.py"));
            if (!Files.exists(rebuildScriptPath)) {
                rebuildScriptPath = path.resolve(Paths.get("tasks", "update_embedded.py"));
            }

            final ByteArrayOutputStream stream = new ByteArrayOutputStream();
            final File rebuildScript = rebuildScriptPath.toFile();
            ExecResult execResult = exec.exec(execSpec -> {
                execSpec.commandLine(pythonDetails.getSystemPythonInterpreter(), rebuildScript);
                execSpec.setStandardOutput(stream);
                execSpec.setErrorOutput(stream);
                execSpec.setIgnoreExitValue(true);
            });

            /*
             * Starting with virtualenv-16.1.0 the rebuild script returns 1 when there's a change.
             * Since the change is exactly what we want, we cannot allow the failure for exit code 1.
             */
            if (execResult.getExitValue() != NO_CHANGE && execResult.getExitValue() != CHANGED) {
                log.lifecycle(stream.toString());
                execResult.assertNormalExitValue();
            }

            log.info("Customized distutils.cfg");
        }
    }
}
