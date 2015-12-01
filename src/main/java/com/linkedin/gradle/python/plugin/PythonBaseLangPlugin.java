package com.linkedin.gradle.python.plugin;

import com.linkedin.gradle.python.spec.component.WheelComponentSpec;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.model.internal.registry.ModelRegistry;
import org.gradle.model.internal.type.ModelType;

import javax.inject.Inject;


public class PythonBaseLangPlugin implements Plugin<Project>  {

    private final ModelRegistry modelRegistry;

    @Inject
    public PythonBaseLangPlugin(ModelRegistry modelRegistry) {
        this.modelRegistry = modelRegistry;
    }

    public void apply(final Project project) {
        project.getPluginManager().apply(PythonRulePlugin.class);

        modelRegistry.getRoot().applyToAllLinksTransitive(ModelType.of(WheelComponentSpec.class), PythonBinaryRules.class);
        project.getExtensions().create("pythonConfigurations", PythonPluginConfigurations.class, project.getConfigurations(), project.getDependencies());
    }
}
