package com.linkedin.gradle.python.plugin;

import com.linkedin.gradle.python.tasks.SourceDistTask;
import com.linkedin.gradle.python.util.StandardTextValues;
import org.gradle.api.Project;

import java.util.LinkedHashMap;

public class PythonSourceDistributionPlugin extends PythonBasePlugin {

    public static final String TASK_PACKAGE_SDIST = "packageSdist";

    @Override
    public void applyTo(final Project project) {

        /**
         * Create a Python source distribution.
         */
        SourceDistTask sdistPackageTask = project.getTasks().create(TASK_PACKAGE_SDIST, SourceDistTask.class,
            task -> task.dependsOn(project.getTasks().getByName(StandardTextValues.TASK_INSTALL_PROJECT.getValue())));

        LinkedHashMap<String, Object> sdistArtifactInfo = new LinkedHashMap<>(5);
        sdistArtifactInfo.put("name", project.getName());
        sdistArtifactInfo.put("type", "tgz");
        sdistArtifactInfo.put("extension", "tar.gz");
        sdistArtifactInfo.put("file", sdistPackageTask.getSdistOutput());
        sdistArtifactInfo.put("builtBy", project.getTasks().getByName(TASK_PACKAGE_SDIST));

        project.getArtifacts().add(StandardTextValues.CONFIGURATION_DEFAULT.getValue(), sdistArtifactInfo);
    }
}
