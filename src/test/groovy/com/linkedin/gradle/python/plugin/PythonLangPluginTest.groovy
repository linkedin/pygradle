package com.linkedin.gradle.python.plugin

import com.linkedin.gradle.python.internal.DefaultPythonSourceSet
import com.linkedin.gradle.python.plugin.internal.AbstractBaseRuleSourcePluginTest
import com.linkedin.gradle.python.spec.binary.SourceDistBinarySpec
import com.linkedin.gradle.python.spec.binary.WheelBinarySpec
import com.linkedin.gradle.python.spec.component.internal.PythonComponentSpecInternal
import org.gradle.api.internal.file.FileResolver
import org.gradle.internal.reflect.DirectInstantiator
import org.gradle.language.base.sources.BaseLanguageSourceSet
import spock.lang.Specification

class PythonLangPluginTest extends AbstractBaseRuleSourcePluginTest {

    def "has useful string representation"() {
        setup:
        def parent = "main"
        def fileResolver = Mock(FileResolver)
        def sourceSet = BaseLanguageSourceSet.create(DefaultPythonSourceSet, "python", parent, fileResolver, DirectInstantiator.INSTANCE)

        expect:
        sourceSet.displayName == "Python source 'main:python'"
        sourceSet.toString() == "Python source 'main:python'"
    }
}
