package com.linkedin.gradle.python.plugin;

import com.linkedin.gradle.python.plugin.internal.sources.SourceDistAction;
import com.linkedin.gradle.python.spec.binary.PythonBinarySpec;
import com.linkedin.gradle.python.spec.binary.SourceDistBinarySpec;
import com.linkedin.gradle.python.spec.component.internal.PythonComponentSpecInternal;
import com.linkedin.gradle.python.spec.component.internal.PythonEnvironmentContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.model.ModelMap;
import org.gradle.model.RuleSource;
import org.gradle.platform.base.ComponentBinaries;


public class PythonSourceDistPlugin implements Plugin<Project> {
    public void apply(final Project project) {
        project.getPluginManager().apply(PythonLangPlugin.class);
        project.getPluginManager().apply(Rules.class);
    }

    static class Rules extends RuleSource {
        @ComponentBinaries
        public void createSourceDistBinaries(ModelMap<PythonBinarySpec> binarySpecs, final PythonComponentSpecInternal spec) {
            final PythonEnvironmentContainer container = spec.getPythonEnvironments();
            String name = spec.getName() + "SourceDist";
            if (!binarySpecs.containsKey(name)) {
                binarySpecs.create(name, SourceDistBinarySpec.class, new SourceDistAction(container.getDefaultPythonEnvironment()));
            }
        }
    }
}
