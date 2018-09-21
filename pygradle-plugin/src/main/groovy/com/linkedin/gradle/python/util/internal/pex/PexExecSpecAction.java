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
package com.linkedin.gradle.python.util.internal.pex;

import com.linkedin.gradle.python.PythonExtension;
import com.linkedin.gradle.python.extension.DeployableExtension;
import com.linkedin.gradle.python.extension.PexExtension;
import com.linkedin.gradle.python.extension.WheelExtension;
import com.linkedin.gradle.python.util.ExtensionUtils;
import com.linkedin.gradle.python.util.PexFileUtil;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.process.ExecSpec;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


class PexExecSpecAction implements Action<ExecSpec> {

    private final PythonExtension pythonExtension;
    private final File pexCache;
    private final File outputFile;
    private final File wheelCache;
    private final File pexShebang;
    private final String entryPoint;
    private final List<String> pexOptions;
    private final Map<String, String> dependencies;
    private final ByteArrayOutputStream outputStream;

    /**
     * Build a pex file.
     *
     * @param pexCache     The directory to use for pex's build cache.
     * @param outputFile   The name to use for the output pex file.
     * @param wheelCache   The repository (usually a wheel-cache) to use to build the pex file.
     * @param pexShebang   The explicit shebang line to be prepended to the resulting pex file.
     * @param entryPoint   The entry point to burn into the pex file, or <code>null</code> if no entry point should be used.
     * @param dependencies The dependencies that are needed for this pex
     */
    private PexExecSpecAction(PythonExtension pythonExtension, File pexCache, File outputFile, File wheelCache,
                              File pexShebang, String entryPoint, List<String> pexOptions, Map<String, String> dependencies) {
        this.pythonExtension = pythonExtension;
        this.pexCache = pexCache;
        this.outputFile = outputFile;
        this.wheelCache = wheelCache;
        this.pexShebang = pexShebang;
        this.entryPoint = entryPoint;
        this.pexOptions = pexOptions;
        this.dependencies = dependencies;
        this.outputStream = new ByteArrayOutputStream();
    }

    @Override
    public void execute(ExecSpec execSpec) {
        execSpec.commandLine(pythonExtension.getDetails().getVirtualEnvInterpreter());
        execSpec.args(pythonExtension.getDetails().getVirtualEnvironment().getPex());

        System.out.println(outputFile.getAbsolutePath());

        execSpec.args(Arrays.asList("--no-pypi",
            "--cache-dir", pexCache.getAbsolutePath(),
            "--output-file", outputFile.getAbsolutePath(),
            "--repo", wheelCache.getAbsolutePath(),
            "--python-shebang", pexShebang.getAbsolutePath()));

        if (entryPoint != null) {
            execSpec.args(Arrays.asList("--entry-point", entryPoint));
        }

        execSpec.args(pexOptions);
        execSpec.args(pexRequirements(dependencies));

        execSpec.setStandardOutput(outputStream);
        execSpec.setErrorOutput(outputStream);
        execSpec.setIgnoreExitValue(true);
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    /**
     * Generate a Pex with an entry point
     *
     * @param project      a reference to a project.
     * @param pexName      the name of the pex you want to make
     * @param entryPoint   the entry point you want to make
     * @param pexOptions   Options to be passed to pex
     * @param dependencies The dependencies that are needed for this pex name => version
     * @return an instance of PexExecSpecAction to build a pex
     */
    public static PexExecSpecAction withEntryPoint(
        Project project, String pexName, String entryPoint, List<String> pexOptions, Map<String, String> dependencies) {
        PythonExtension pythonExtension = ExtensionUtils.getPythonExtension(project);
        PexExtension pexExtension = ExtensionUtils.getPythonComponentExtension(project, PexExtension.class);
        WheelExtension wheelExtension = ExtensionUtils.getPythonComponentExtension(project, WheelExtension.class);
        DeployableExtension deployableExtension = ExtensionUtils.getPythonComponentExtension(project, DeployableExtension.class);

        return new PexExecSpecAction(pythonExtension,
            pexExtension.getPexCache(),
            new File(deployableExtension.getDeployableBinDir(), pexName),
            wheelExtension.getWheelCache(),
            pythonExtension.getDetails().getSystemPythonInterpreter(),
            entryPoint,
            pexOptions,
            dependencies);
    }

    /**
     * Generate a Pex without an entry point
     *
     * @param project      a reference to a project.
     * @param pexName      the name of the pex you want to make
     * @param pexOptions   Options to be passed to pex
     * @param dependencies The dependencies that are needed for this pex name => version
     * @return an instance of PexExecSpecAction to build a pex
     */
    public static PexExecSpecAction withOutEntryPoint(
        Project project, String pexName, List<String> pexOptions, Map<String, String> dependencies) {
        PythonExtension pythonExtension = ExtensionUtils.getPythonExtension(project);
        PexExtension pexExtension = ExtensionUtils.getPythonComponentExtension(project, PexExtension.class);
        WheelExtension wheelExtension = ExtensionUtils.getPythonComponentExtension(project, WheelExtension.class);
        DeployableExtension deployableExtension = ExtensionUtils.getPythonComponentExtension(project, DeployableExtension.class);

        return new PexExecSpecAction(pythonExtension,
            pexExtension.getPexCache(),
            new File(deployableExtension.getDeployableBinDir(), PexFileUtil.createThinPexFilename(pexName)),
            wheelExtension.getWheelCache(),
            pythonExtension.getDetails().getSystemPythonInterpreter(),
            null,
            pexOptions,
            dependencies);
    }

    private List<String> pexRequirements(Map<String, String> dependencies) {
        List<String> requirements = new ArrayList<>();
        for (Map.Entry<String, String> entry : dependencies.entrySet()) {
            /*
             * NOTE: There used to be a workaround for the bug in pex here.
             * It did not follow PEP 427 completely: https://www.python.org/dev/peps/pep-0427/
             * It turned hyphens into underscores for wheel file name in package (distribution) name only,
             * but it did not do the same for package version.
             * The PEP is quite clear about this:
             *     "Each component of the filename is escaped by replacing runs of non-alphanumeric characters
             *     with an underscore _"
             * On the other hand, pip handles this correctly, so there was a discrepancy.
             * Until pex fixed this bug, we had to tell it that version in the file name has underscore.
             *
             * Leaving this comment here in case there's a regression again.
             * It seems that pex has fixed this between 1.2.13 and 1.4.5.
             * We used to need `.replace("-", "_"));` instead of `);` at the end below.
             */
            requirements.add(entry.getKey() + "==" + entry.getValue());
        }
        return requirements;
    }
}
