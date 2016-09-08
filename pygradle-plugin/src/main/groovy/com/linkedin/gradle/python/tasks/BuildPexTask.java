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
import com.linkedin.gradle.python.extension.DeployableExtension;
import com.linkedin.gradle.python.extension.PexExtension;
import com.linkedin.gradle.python.extension.WheelExtension;
import com.linkedin.gradle.python.util.ExtensionUtils;
import com.linkedin.gradle.python.util.VirtualEnvExecutableHelper;
import com.linkedin.gradle.python.util.internal.pex.FatPexGenerator;
import com.linkedin.gradle.python.util.internal.pex.ThinPexGenerator;
import com.linkedin.gradle.python.util.pex.DefaultEntryPointTemplateProvider;
import com.linkedin.gradle.python.util.pex.EntryPointTemplateProvider;
import org.apache.commons.io.FileUtils;
import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecSpec;

import java.util.Map;

/**
 * This task builds pex files; both 'thin' and 'fat' based on the settings on {@link PexExtension}
 *
 * If in 'fat' pex mode, each entry point will generate it's own pex.
 *
 * If in 'thin' pex mode, one pex will be created and a script for each entry point will be rendered. This helps save
 * on disk space when one pex contains multiple entry points.
 *
 * The entry points scripts are customizable, so there is a property {@link BuildPexTask#templateProvider} that can be set
 * allowing the task to customize the entry point.
 *
 * The template that is provided will be rendered using a {@link groovy.text.SimpleTemplateEngine}, and will have two
 * properties passed to it automatically. They are named <code>realPex</code>, gives the name of the pex to execute against
 * and <code>entryPoint</code> which is the name of the entry point. If you wish to provide your own template, with more
 * options they can be added to {@link BuildPexTask#additionalProperties} and they will be provided to the template engine.
 */
public class BuildPexTask extends DefaultTask {

    private Map<String, String> additionalProperties;
    private EntryPointTemplateProvider templateProvider = new DefaultEntryPointTemplateProvider();

    @TaskAction
    public void buildPex() throws Exception {
        Project project = getProject();

        final PythonExtension pythonExtension = ExtensionUtils.getPythonExtension(project);
        DeployableExtension deployableExtension = ExtensionUtils.getPythonComponentExtension(project, DeployableExtension.class);
        PexExtension pexExtension = ExtensionUtils.getPythonComponentExtension(project, PexExtension.class);

        // Recreate the pex cache if it exists so that we don't mistakenly use an old build's version of the local project.
        if (pexExtension.getPexCache().exists()) {
            FileUtils.deleteQuietly(pexExtension.getPexCache());
            pexExtension.getPexCache().mkdirs();
        }

        project.exec(new Action<ExecSpec>() {
            @Override
            public void execute(ExecSpec execSpec) {
                configureExecution(pythonExtension, execSpec);
            }
        });

        deployableExtension.getDeployableBuildDir().mkdirs();

        if (pexExtension.isFatPex()) {
            new FatPexGenerator(project).buildEntryPoints();
        } else {
            new ThinPexGenerator(project, templateProvider, additionalProperties).buildEntryPoints();
        }
    }

    private void configureExecution(PythonExtension pythonExtension, ExecSpec spec) {
        WheelExtension wheelExtension = ExtensionUtils.maybeCreateWheelExtension(getProject());

        spec.environment(pythonExtension.pythonEnvironment);
        spec.environment(pythonExtension.pythonEnvironmentDistgradle);
        spec.commandLine(VirtualEnvExecutableHelper.getPythonInterpreter(pythonExtension.getDetails()));
        spec.args(VirtualEnvExecutableHelper.getPip(pythonExtension.getDetails()),
            "wheel",
            "--disable-pip-version-check",
            "--wheel-dir",
            wheelExtension.getWheelCache().getAbsolutePath(),
            "--no-deps",
            ".");
    }

    @Input
    @Optional
    public Map<String, String> getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(Map<String, String> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    @Input
    @Optional
    public EntryPointTemplateProvider getTemplateProvider() {
        return templateProvider;
    }

    public void setTemplateProvider(EntryPointTemplateProvider templateProvider) {
        this.templateProvider = templateProvider;
    }
}
