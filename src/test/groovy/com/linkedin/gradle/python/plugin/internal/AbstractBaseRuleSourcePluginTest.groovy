package com.linkedin.gradle.python.plugin.internal


import com.linkedin.gradle.python.plugin.PythonPluginConfigurations
import org.gradle.api.Project
import org.gradle.language.base.ProjectSourceSet
import org.gradle.model.ModelMap
import org.gradle.model.internal.core.ModelPath
import org.gradle.model.internal.type.ModelType
import org.gradle.model.internal.type.ModelTypes
import org.gradle.platform.base.BinarySpec
import org.gradle.platform.base.ComponentSpec
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

abstract class AbstractBaseRuleSourcePluginTest extends Specification {
    def project

    ModelMap<ComponentSpec> realizeComponents() {
        return (ModelMap<ComponentSpec>) project.modelRegistry.realize(ModelPath.path("components"), ModelTypes.modelMap(ComponentSpec))
    }

    ProjectSourceSet realizeSourceSets() {
        return (ProjectSourceSet) project.modelRegistry.find(ModelPath.path("sources"), ModelType.of(ProjectSourceSet))
    }

    ModelMap<BinarySpec> realizeBinaries() {
        return (ModelMap<BinarySpec>) project.modelRegistry.find(ModelPath.path("binaries"), ModelTypes.modelMap(BinarySpec))
    }

    def setup() {
        project = ProjectBuilder.builder().build()
        project.getExtensions().create("pythonConfigurations", PythonPluginConfigurations.class, project.getConfigurations(), project.getDependencies());
    }

    static Set<String> defaultTasks(String postFix) {
        return [
                "createVirtualEnv$postFix" as String,
                "installRequiredDependencies$postFix" as String,
                "installRuntimeDependencies$postFix" as String,
                "installEditable$postFix" as String,
        ] as Set
    }

    def dsl(@DelegatesTo(Project) Closure closure) {
        closure.delegate = project
        closure()
        project.tasks.realize()
        project.bindAllModelRules()
    }
}
