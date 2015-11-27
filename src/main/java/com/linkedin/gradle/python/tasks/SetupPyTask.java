package com.linkedin.gradle.python.tasks;

import com.linkedin.gradle.python.PythonSourceSet;
import com.linkedin.gradle.python.internal.platform.PythonPlatform;
import com.linkedin.gradle.python.internal.platform.PythonVersion;
import com.linkedin.gradle.python.spec.PythonComponentSpec;
import com.linkedin.gradle.python.spec.PythonEntryPoint;
import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.util.GFileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SetupPyTask extends DefaultTask {

    private List<PythonPlatform> pythonPlatforms;
    private PythonComponentSpec componentSpec;
    private PythonSourceSet sourceDir;

    public SetupPyTask() {
        onlyIf(new Spec<Task>() {
            @Override
            public boolean isSatisfiedBy(Task element) {
                return componentSpec.autoGenerateSetupPy();
            }
        });
    }

    @TaskAction
    public void doWork() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("# AUTO GENERATED FILE\n");
        stringBuilder.append("# Always prefer setuptools over distutils\n");
        stringBuilder.append("from setuptools import setup, find_packages\n");
        stringBuilder.append("\n");

        stringBuilder.append("setup(\n");
        stringBuilder.append(String.format("\tname='%s',\n", getProject().getName()));
        stringBuilder.append(String.format("\tversion='%s',\n", getProject().getVersion().toString()));
        stringBuilder.append(String.format("\tkeywords='%s',\n", getKeywords()));
        stringBuilder.append("\tclassifiers=[\n");
        for (String s : getPythonVersionSupported()) {
            stringBuilder.append(s);
        }
        stringBuilder.append("\t],\n");

        stringBuilder.append(String.format("\tpackages=find_packages('%s'),\n", getSourceDir()));
        stringBuilder.append(String.format("\tpackage_dir={'':'%s'},\n", getSourceDir()));
        stringBuilder.append("\tentry_points={\n");
        stringBuilder.append("\t\t'console_scripts': [\n");

        for (PythonEntryPoint entryPoint : componentSpec.getConsoleScripts()) {
            stringBuilder.append(String.format("\t\t\t'%s = %s',\n", entryPoint.getScriptName(), entryPoint.getPythonReference()));
        }
        stringBuilder.append("\t\t],\n");
        stringBuilder.append("\t},\n");

        stringBuilder.append(")\n");

        GFileUtils.writeFile(stringBuilder.toString(), getSetupPyFile());
    }

    private String getKeywords() {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < componentSpec.getKeywords().size(); i++) {
            sb.append(componentSpec.getKeywords().get(i));
            if(i != componentSpec.getKeywords().size() - 1) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }


    @OutputFile
    public File getSetupPyFile() {
        return new File(getProject().getProjectDir(), "setup.py");
    }

    @Input
    private Set<String> getPythonVersionSupported() {
        Set<String> pythonMajorVersions = new HashSet<String>();
        for (PythonPlatform platformRequirement : pythonPlatforms) {
            PythonVersion version = platformRequirement.getVersion();
            pythonMajorVersions.add(String.format("\t\t'Programming Language :: Python :: %s',\n", version.getVersionString()));
            pythonMajorVersions.add(String.format("\t\t'Programming Language :: Python :: %s',\n", version.getMajorVersion()));
        }
        return pythonMajorVersions;
    }

    @Input
    public String getSourceDir() {
        String path = new ArrayList<File>(sourceDir.getSource().getSrcDirs()).get(0).getPath();
        return path.replace(getProject().getProjectDir() + "/", "");
    }

    public void setSourceDir(PythonSourceSet sourceDir) {
        this.sourceDir = sourceDir;
    }

    public void setPythonPlatforms(List<PythonPlatform> pythonPlatforms) {
        this.pythonPlatforms = pythonPlatforms;
    }

    public void setComponentSpec(PythonComponentSpec componentSpec) {
        this.componentSpec = componentSpec;
    }
}
