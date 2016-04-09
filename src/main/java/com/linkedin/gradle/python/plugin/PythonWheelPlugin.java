package com.linkedin.gradle.python.plugin;

import com.linkedin.gradle.python.plugin.internal.wheel.WheelAction;
import com.linkedin.gradle.python.spec.binary.PythonBinarySpec;
import com.linkedin.gradle.python.spec.binary.WheelBinarySpec;
import com.linkedin.gradle.python.spec.component.internal.PythonComponentSpecInternal;
import com.linkedin.gradle.python.spec.component.internal.PythonEnvironment;
import com.linkedin.gradle.python.spec.component.internal.PythonEnvironmentContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.model.ModelMap;
import org.gradle.model.RuleSource;
import org.gradle.platform.base.ComponentBinaries;


public class PythonWheelPlugin implements Plugin<Project> {
    public void apply(final Project project) {
        project.getPluginManager().apply(PythonLangPlugin.class);
        project.getPluginManager().apply(Rules.class);
    }

    public static class Rules extends RuleSource {
        @ComponentBinaries
        public void createWheelBinaries(ModelMap<PythonBinarySpec> binarySpecs, final PythonComponentSpecInternal spec) {
            final PythonEnvironmentContainer container = spec.getPythonEnvironments();

            for (PythonEnvironment pythonEnvironment : container.getPythonEnvironments().values()) {
                String name = spec.getName() + "Wheel" + pythonEnvironment.getVersion().getVersionString();
                if (!binarySpecs.containsKey(name)) {
                    binarySpecs.create(name, WheelBinarySpec.class, new WheelAction(pythonEnvironment));
                }
            }
        }
    }
}
