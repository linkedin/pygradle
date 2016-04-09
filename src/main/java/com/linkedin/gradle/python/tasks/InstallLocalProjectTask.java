package com.linkedin.gradle.python.tasks;

import com.linkedin.gradle.python.tasks.internal.utilities.PipLocalInstallAction;
import com.linkedin.gradle.python.tasks.internal.utilities.TaskUtils;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecResult;

import java.io.File;


public class InstallLocalProjectTask extends BasePythonTask {

    @TaskAction
    public void installLocalProject() {
        PipLocalInstallAction pipLocalInstallAction = new PipLocalInstallAction(getVenvDir());
        ExecResult execute = execute(pipLocalInstallAction.install(getProject().getProjectDir()));
        if (execute.getExitValue() != 0) {
            getLogger().lifecycle(pipLocalInstallAction.getWholeText());
        }
        execute.assertNormalExitValue();
    }

    @OutputFile
    public File getEggLink() {
        String fileName = String.format("%s.egg-link", getProject().getName());
        File installLink = new File(TaskUtils.sitePackage(getVenvDir(), getPythonVersion()), fileName);
        getLogger().info("Link: {}", installLink.getAbsolutePath());
        return installLink;
    }

    @InputFile
    public File getSetupPyFile() {
        File file = new File(getProject().getProjectDir(), "setup.py");
        getLogger().info("Setup.py: {}", file.getAbsolutePath());
        return file;
    }

}
