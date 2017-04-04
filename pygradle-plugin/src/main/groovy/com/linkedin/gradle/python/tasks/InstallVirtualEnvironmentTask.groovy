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
package com.linkedin.gradle.python.tasks

import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

import com.linkedin.gradle.python.extension.PythonDetails
import com.linkedin.gradle.python.tasks.execution.FailureReasonProvider
import com.linkedin.gradle.python.tasks.execution.TeeOutputContainer
import com.linkedin.gradle.python.util.pip.PipConfFile
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ResolvedConfiguration
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecSpec
import org.gradle.util.VersionNumber

@CompileStatic
class InstallVirtualEnvironmentTask extends DefaultTask implements FailureReasonProvider {

    private PythonDetails pythonDetails

    @Input
    @Optional
    String distutilsCfg

    @InputFiles
    Configuration getPyGradleBootstrap() {
        project.configurations.getByName('pygradleBootstrap')
    }

    @OutputFile
    File getVirtualEnvDir() {
        return pythonDetails.getVirtualEnvInterpreter()
    }

    final TeeOutputContainer container = new TeeOutputContainer()

    @TaskAction
    @CompileDynamic
    void installVEnv() {

        PipConfFile pipConfFile = new PipConfFile(project, pythonDetails)

        File packageDir = new File(project.buildDir, "virtualenv-dir")
        if (packageDir.exists()) {
            project.delete(packageDir)
        }

        packageDir.mkdirs()

        getPyGradleBootstrap().files.each { file ->
            project.copy {
                from project.tarTree(file.path)
                into packageDir
            }
        }

        def version = findVirtualEnvDependencyVersion()
        customize(packageDir, version)
        project.exec(new Action<ExecSpec>() {
            @Override
            void execute(ExecSpec execSpec) {
                container.setOutputs(execSpec)
                execSpec.commandLine(
                    pythonDetails.getSystemPythonInterpreter(),
                    project.file("${packageDir}/virtualenv-${version}/virtualenv.py"),
                    '--never-download',
                    '--python', pythonDetails.getSystemPythonInterpreter(),
                    '--prompt', pythonDetails.virtualEnvPrompt,
                    pythonDetails.getVirtualEnv()
                )
            }
        })
        project.delete(packageDir)
        pipConfFile.buildPipConfFile()
    }

    private String findVirtualEnvDependencyVersion() {
        ResolvedConfiguration resolvedConfiguration = getPyGradleBootstrap().getResolvedConfiguration()
        Set<ResolvedDependency> virtualEnvDependencies = resolvedConfiguration.getFirstLevelModuleDependencies(new VirtualEnvSpec())
        if (virtualEnvDependencies.isEmpty()) {
            throw new GradleException("Unable to find virtualenv dependency")
        }

        VersionNumber highest = new VersionNumber(0, 0, 0, null)
        for (ResolvedDependency resolvedDependency : virtualEnvDependencies) {
            VersionNumber test = VersionNumber.parse(resolvedDependency.getModuleVersion())
            if (test.compareTo(highest) > 0) {
                highest = test
            }
        }

        return highest.toString()
    }

    @Override
    String getReason() {
        return container.getCommandOutput()
    }

    public void setPythonDetails(PythonDetails pythonDetails) {
        this.pythonDetails = pythonDetails
    }

    public PythonDetails getPythonDetails() {
        return pythonDetails
    }

    private class VirtualEnvSpec implements Spec<Dependency> {

        @Override
        public boolean isSatisfiedBy(Dependency element) {
            return "virtualenv" == element.getName()
        }
    }

    private void customize(File packageDir, String version) {
        if (distutilsCfg != null) {
            String venvDir = Paths.get("${packageDir}", "virtualenv-${version}").toString()
            Files.write(
                Paths.get(venvDir, "virtualenv_embedded", "distutils.cfg"),
                distutilsCfg.getBytes(),
                StandardOpenOption.APPEND
            )
            ByteArrayOutputStream stream = new ByteArrayOutputStream()
            project.exec(new Action<ExecSpec>() {
                @Override
                void execute(ExecSpec execSpec) {
                    execSpec.commandLine(
                        pythonDetails.getSystemPythonInterpreter(),
                        project.file(Paths.get(venvDir, "bin", "rebuild-script.py").toString())
                    )
                    execSpec.standardOutput = stream
                    execSpec.errorOutput = stream
                }
            })
            logger.info("Customized distutils.cfg")
        }
    }
}
