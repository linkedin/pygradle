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

package com.linkedin.gradle.python.util;

import com.linkedin.gradle.python.extension.PythonDetails;
import com.linkedin.gradle.python.plugin.PythonHelpers;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.process.ExecResult;


public class WheelUtils {

    private static final Logger logger = Logging.getLogger(WheelUtils.class);

    private WheelUtils() {
        //private constructor for util class
    }

    /**
     * Makes wheel from sdist.
     *
     * @param project the project to run within
     * @param packageInfo the sdist package info
     * @param pythonDetails details on the python to build the wheel
     * @param targetDir target directory to store the built wheel
     */
    public static void makeWheelFromSdist(Project project, PackageInfo packageInfo, PythonDetails pythonDetails, File targetDir) {
        if (packageInfo.getPackageFile().getName().endsWith(".whl")) {
            return;
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ExecResult results = project.exec(exec -> {
            exec.commandLine(pythonDetails.getVirtualEnvInterpreter(),
                pythonDetails.getVirtualEnvironment().getPip(),
                "wheel",
                "--disable-pip-version-check",
                "--wheel-dir", targetDir,
                "--no-deps",
                packageInfo.getPackageFile().getAbsoluteFile().getAbsolutePath());
            exec.setStandardOutput(stream);
            exec.setErrorOutput(stream);
            exec.setIgnoreExitValue(true);
        });

        if (results.getExitValue() != 0) {
            logger.info("Unable to build wheel for {}-{}", packageInfo.getName(), packageInfo.getVersion());
            File resultDir = new File(project.getBuildDir(), "makeWheel-" + packageInfo.getName() + "-" + packageInfo.getVersion() + ".txt");
            try {
                FileUtils.write(resultDir, stream.toString());
            } catch (IOException ignored) {
                // Don't fail if there is are issues writing the wheel report.
            }
        } else {
            if (PythonHelpers.isPlainOrVerbose(project)) {
                logger.lifecycle("Wheel was built for {}-{}", packageInfo.getName(), packageInfo.getVersion());
            }
        }
    }
}
