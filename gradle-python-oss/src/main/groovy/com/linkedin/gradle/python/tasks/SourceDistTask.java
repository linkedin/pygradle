package com.linkedin.gradle.python.tasks;

import java.io.File;

import com.linkedin.gradle.python.PythonComponent;
import com.linkedin.gradle.python.util.VirtualEnvExecutableHelper;
import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecSpec;


public class SourceDistTask extends DefaultTask {

    @TaskAction
    public void packageSdist() {

        final PythonComponent settings = getProject().getExtensions().getByType(PythonComponent.class);

        getProject().exec(new Action<ExecSpec>() {
            @Override
            public void execute(ExecSpec execSpec) {
                execSpec.environment(settings.pythonEnvironmentDistgradle);
                execSpec.commandLine(
                        VirtualEnvExecutableHelper.getPythonInterpreter(settings),
                        "setup.py",
                        "sdist",
                        "--dist-dir",
                        getDistDir().getAbsolutePath());
            }
        });
    }

    @OutputFile
    public File getSdistOutput() {
        Project project = getProject();
        return new File(getDistDir(), String.format("%s-%s.tar.gz", project.getName(), project.getVersion()));
    }

    private File getDistDir() {
        return new File(getProject().getBuildDir(), "distributions");
    }
}
