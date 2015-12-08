package com.linkedin.gradle.python.plugin.internal.wheel
import com.linkedin.gradle.python.PythonSourceSet
import com.linkedin.gradle.python.plugin.PythonPluginConfigurations
import com.linkedin.gradle.python.plugin.internal.BasePythonRulePlugin
import com.linkedin.gradle.python.plugin.internal.AbstractBaseRuleSourcePluginTest
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

class PythonWheelRulePluginTest extends AbstractBaseRuleSourcePluginTest {

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
}
