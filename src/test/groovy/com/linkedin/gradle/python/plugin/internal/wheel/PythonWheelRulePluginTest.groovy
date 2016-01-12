package com.linkedin.gradle.python.plugin.internal.wheel
import com.linkedin.gradle.python.PythonSourceSet
import com.linkedin.gradle.python.plugin.internal.BasePythonRulePlugin
import com.linkedin.gradle.python.plugin.internal.AbstractBaseRuleSourcePluginTest

import com.linkedin.gradle.python.spec.component.internal.DefaultPythonTargetPlatform
import org.gradle.internal.os.OperatingSystem
import org.gradle.model.ModelMap
import spock.lang.Ignore

@Ignore
class PythonWheelRulePluginTest extends AbstractBaseRuleSourcePluginTest {

    def "creates python source set with conventional locations for components"() {
        when:
        dsl {
            pluginManager.apply BasePythonRulePlugin
            pluginManager.apply DefaultPythonTaskRule
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
        wheel.targetPlatforms == [new DefaultPythonTargetPlatform(OperatingSystem.current(), "python2.7")]
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
            pluginManager.apply DefaultPythonTaskRule
            pluginManager.apply PythonWheelRulePlugin
            model {
                components {
                    wheel(com.linkedin.gradle.python.spec.component.WheelComponentSpec) {
                        targetPlatform 'python2.7'
                        targetPlatform 'python2.6'
                    }
                }
            }
        }

        then:
        def components = realizeComponents()
        def binaries = realizeBinaries()
        def wheel = components.wheel
        wheel.targetPlatforms.size() == 2
        ['2.7', '2.6'].each { version ->
            assert wheel.targetPlatforms.contains(new DefaultPythonTargetPlatform(OperatingSystem.current(), "python$version"))
            def binary = binaries.get("wheel$version")
            assert binary != null
            assert (binary.tasks.toList().collect { it.name } as Set).containsAll(defaultTasks(''))
        }
        binaries.size() == 2
    }
}
