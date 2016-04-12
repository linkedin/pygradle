/*
 * Copyright 2016 LinkedIn Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.linkedin.gradle.python.plugin.internal


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
