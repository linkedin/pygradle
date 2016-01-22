package com.linkedin.gradle.python.tasks.internal;

import com.linkedin.gradle.python.tasks.BasePythonTask;
import com.linkedin.gradle.python.tasks.PublishingTask;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.internal.artifacts.publish.DefaultPublishArtifact;
import org.gradle.api.internal.tasks.DefaultTaskDependency;
import org.gradle.api.internal.tasks.options.Option;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.OutputFiles;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.process.ExecResult;
import org.gradle.process.internal.ExecAction;
import org.gradle.util.GFileUtils;


abstract public class AbstractDistTask extends BasePythonTask implements PublishingTask {

    private final String setupPyCommand;
    protected final List<String> args = new ArrayList<String>();

    public File distributablePath = new File(getProject().getBuildDir(), "distributable");

    @InputFiles
    public List<File> sourceSet = new ArrayList<File>();


    protected AbstractDistTask(String setupPyCommand, String... args) {
        this.setupPyCommand = setupPyCommand;
        if (args != null) {
            this.args.addAll(Arrays.asList(args));
        }
    }

    public List<String> extraArgs() {
      return Collections.emptyList();
    }

    @TaskAction
    public void buildSourceDist() {
        args.addAll(extraArgs());

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ExecResult sdist = execute(new Action<ExecAction>() {
            @Override
            public void execute(ExecAction execAction) {
                execAction.setIgnoreExitValue(true);
                execAction.setStandardOutput(outputStream);
                execAction.setErrorOutput(outputStream);
                execAction.args("setup.py", setupPyCommand, "--dist-dir", getTemporaryDir().getAbsolutePath());
                execAction.args(args);
            }
        });

        if (sdist.getExitValue() != 0) {
            getLogger().lifecycle("Execution Failed\n{}", outputStream.toString());
        } else if (getLogger().isInfoEnabled()) {
            getLogger().lifecycle("Execution Output:\n{}", outputStream.toString());
        }
        sdist.assertNormalExitValue();

        File archiveFile = getProject().fileTree(getTemporaryDir()).getSingleFile();
        File outputFile = getPythonArtifact();
        if (outputFile.exists()) {
            GFileUtils.deleteQuietly(outputFile);
        }
        GFileUtils.moveFile(archiveFile, outputFile);
    }

    abstract public String getExtension();

    @OutputFile
    protected abstract File getPythonArtifact();

    public PublishArtifact getFileToPublish() {
        File pythonArtifact = getPythonArtifact();
        String name = FilenameUtils.getBaseName(pythonArtifact.getName());
        return new DefaultPublishArtifact(name, getExtension(), getExtension(), null, new Date(), pythonArtifact, this);
    }

    public void sourceSet(SourceDirectorySet source) {
        sourceSet.addAll(source.getSrcDirs());
    }
}
