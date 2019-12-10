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

import com.linkedin.gradle.python.PythonExtension;
import com.linkedin.gradle.python.util.ExtensionUtils;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.process.ExecResult;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Flake8Task extends AbstractPythonMainSourceDefaultTask {

    private static final Logger log = Logging.getLogger(Flake8Task.class);
    // Track whether the current run is excluding the new rules
    private Boolean ignoringNewRules = false;
    private String firstRunOutput = null;

    // Set of flake8 rules to ignore (i.e. warn if these checks fail, rather than failing the task)
    private Set<String> ignoreRules = new HashSet<>();

    private static final String IGNORED_RULES_MSG = "######################### WARNING ##########################\n"
        + "The flake8 version has been recently updated, which added the following new rules:\n"
        + "%s\n"  // This will be replaced with the set of ignored rules
        + "Your project is failing for one or more of these rules. Please address them, as they will be enforced soon.\n"
        + "%s############################################################\n";

    // Provide the ability to set the ignored rules for testing purposes,
    // but mainly so that users can enforce the use of a different version of flake8 in their plugins.
    public void setIgnoreRules(Set<String> ignoreRules) {
        this.ignoreRules = ignoreRules;
    }

    public Set<String> getIgnoreRules() {
        return ignoreRules;
    }

    public void preExecution() {
        ignoreExitValue = true;
        PythonExtension pythonExtension = ExtensionUtils.getPythonExtension(getProject());
        File flake8Exec = pythonExtension.getDetails().getVirtualEnvironment().findExecutable("flake8");

        /*
         Modified to only include folders that exist. if no folders exist, then
         the task isn't actually run.
         */
        List<String> paths = new ArrayList<>();
        if (getProject().file(pythonExtension.srcDir).exists()) {
            log.info("Flake8: adding {}", pythonExtension.srcDir);
            paths.add(pythonExtension.srcDir);
        } else {
            log.info("Flake8: srcDir doesn't exist");
        }

        if (getProject().file(pythonExtension.testDir).exists()) {
            log.info("Flake8: adding {}", pythonExtension.testDir);
            paths.add(pythonExtension.testDir);
        } else {
            log.info("Flake8: testDir doesn't exist");
        }
        args(Arrays.asList(
            flake8Exec.getAbsolutePath(),
            "--config", pythonExtension.setupCfg));

        // creating a flake8 config file if one doesn't exist, this prevents "file not found" issues.
        File cfgCheck = getProject().file(pythonExtension.setupCfg);
        if (!cfgCheck.exists()) {
            log.info("Flake8 config file doesn't exist, creating default");
            try {
                FileUtils.write(cfgCheck, "[flake8]");
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        } else {
            log.info("Flake8 config file exists");
        }

        args(paths);
    }

    @Override
    public void processResults(ExecResult execResult) {
        // If the first run of flake8 fails, trying running it again but ignoring the
        // rules/checks added by the previous version bump.
        if ((execResult.getExitValue() != 0) && !ignoringNewRules && (ignoreRules.size() > 0)) {
            ignoringNewRules = true;
            firstRunOutput = this.output;
            subArgs("--extend-ignore=" + String.join(",", ignoreRules));
            executePythonProcess();
        } else if ((execResult.getExitValue() == 0) && ignoringNewRules) {
            // The previous run failed, but flake8 succeeds when we ignore the most recent rules.
            // Warn the user that they are failing one or more of the new rules.
            log.warn(String.format(IGNORED_RULES_MSG, String.join(", ", ignoreRules), firstRunOutput));
        } else {
            execResult.assertNormalExitValue();
        }
    }
}
