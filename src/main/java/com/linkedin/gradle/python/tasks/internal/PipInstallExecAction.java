package com.linkedin.gradle.python.tasks.internal;

import com.linkedin.gradle.python.tasks.internal.utilities.PipOutputStreamProcessor;
import com.linkedin.gradle.python.tasks.internal.utilities.TaskUtils;
import org.gradle.api.Action;
import org.gradle.process.internal.ExecAction;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PipInstallExecAction implements Action<ExecAction> {

    final File pipExecutable;
    final File venvDir;
    final PipOutputStreamProcessor outputStream;
    final List<String> arguments;

    public PipInstallExecAction(File venvDir, PipOutputStreamProcessor outputStream, List<String> arguments) {
        this.pipExecutable = new File(venvDir, "bin/pip");
        this.venvDir = venvDir;
        this.outputStream = outputStream;
        this.arguments = arguments;
    }

    @Override
    public void execute(ExecAction execAction) {
        execAction.setIgnoreExitValue(true);
        execAction.setStandardOutput(outputStream);
        execAction.setErrorOutput(outputStream);
        ArrayList<String> argumentList = new ArrayList<String>();
        argumentList.addAll(Arrays.asList(pipExecutable.getAbsolutePath(), "install"));
        argumentList.addAll(arguments);
        execAction.args(argumentList);

        outputStream.addCommand(TaskUtils.join(execAction.getArgs(), " "));
    }

}
