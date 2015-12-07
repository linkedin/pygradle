package com.linkedin.gradle.python.plugin.internal.wheel
import com.linkedin.gradle.python.PythonSourceSet
import com.linkedin.gradle.python.plugin.PythonPluginConfigurations
import com.linkedin.gradle.python.plugin.internal.BasePythonRulePlugin
import org.gradle.api.Project
import org.gradle.language.base.ProjectSourceSet
import org.gradle.model.ModelMap
import org.gradle.model.internal.core.ModelPath
import org.gradle.model.internal.type.ModelType
import org.gradle.model.internal.type.ModelTypes
import org.gradle.platform.base.BinarySpec
import org.gradle.platform.base.ComponentSpec
import org.gradle.platform.base.internal.DefaultPlatformRequirement
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class PythonWheelRulePluginTest extends Specification {
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

    def "creates python source set with conventional locations for components"() {
        when:
        dsl {
            pluginManager.apply BasePythonRulePlugin
            pluginManager.apply PythonWheelRulePlugin
            model {
                components {
                    wheel(com.linkedin.gradle.python.spec.component.WheelComponentSpec) {
                        targetPlatform 'python2.7'
                    }
                }
            }
        }


        then:
        def components = realizeComponents()
        def wheel = components.wheel
        wheel.targetPlatforms == [DefaultPlatformRequirement.create("python2.7")]
        wheel.sources instanceof ModelMap
        wheel.sources.python instanceof PythonSourceSet
        wheel.sources.python.source.srcDirs == [project.file("src/main/python")] as Set

        and:
        def sources = realizeSourceSets()
        sources as Set == wheel.sources as Set
    }

    def 'apply multiple platforms'() {
        when:
        dsl {
            pluginManager.apply BasePythonRulePlugin
            pluginManager.apply PythonWheelRulePlugin
            model {
                components {
                    wheel(com.linkedin.gradle.python.spec.component.WheelComponentSpec) {
                        targetPlatform 'python2.7'
                        targetPlatform 'python2.6'
                        targetPlatform 'python3.1'
                    }
                }
            }
        }

        then:
        def components = realizeComponents()
        def binaries = realizeBinaries()
        def wheel = components.wheel
        ['2.7', '2.6', '3.1'].each { version ->
            assert wheel.targetPlatforms.contains(DefaultPlatformRequirement.create("python$version"))
            def binary = binaries.get("wheel$version")
            assert binary != null
            assert (binary.tasks.toList().collect { it.name } as Set).containsAll(defaultTasks(version))
        }
        binaries.size() == 3
    }

    private static Set<String> defaultTasks(String version) {
        return [
                "createVirtualEnv$version" as String,
                "installRequiredDependencies$version" as String,
                "installRuntimeDependencies$version" as String,
                "installTestDependencies$version" as String,
                "installEditable$version" as String,
        ] as Set
    }

    def dsl(@DelegatesTo(Project) Closure closure) {
        closure.delegate = project
        closure()
        project.tasks.realize()
        project.bindAllModelRules()
    }
}
