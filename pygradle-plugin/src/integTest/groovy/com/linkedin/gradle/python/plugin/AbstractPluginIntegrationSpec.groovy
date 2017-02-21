package com.linkedin.gradle.python.plugin

import com.linkedin.gradle.python.plugin.testutils.DefaultProjectLayoutRule
import com.linkedin.gradle.python.util.StandardTextValuesTasks
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import spock.lang.Specification

class AbstractPluginIntegrationSpec extends Specification {

    enum RunType { PASS, FAIL }

    List<File> pluginClasspath = []

    @Rule
    final DefaultProjectLayoutRule temporaryFolder = new DefaultProjectLayoutRule()

    File buildFile

    static pygradleSupportedGradleVersions = ['3.2']

    def setup() {
        buildFile = temporaryFolder.newFile('build.gradle')

        temporaryFolder.newFolder("src", "main", "java")
        temporaryFolder.newFolder("src", "main", "groovy")
    }


    File getIntegResourcesFile(String sPath) {
        File file = new File("src/integTest/resources" + sPath)
        new File(file.getAbsolutePath())
    }

    BuildResult run(StandardTextValuesTasks task, List<String> cmdOption = []) {
        runJob(RunType.PASS, null, task, cmdOption)
    }

    BuildResult run(String gradleVersion, StandardTextValuesTasks task, List<String> cmdOption = []) {
        runJob(RunType.PASS, gradleVersion, task, cmdOption)
    }

    BuildResult fail(StandardTextValuesTasks task, List<String> cmdOption = []) {
        runJob(RunType.FAIL, null, task, cmdOption)
    }

    BuildResult fail(String gradleVersion, StandardTextValuesTasks task, List<String> cmdOption = []) {
        runJob(RunType.FAIL, gradleVersion, task, cmdOption)
    }

    BuildResult runJob(RunType r, String gradleVersion, StandardTextValuesTasks task, List<String> cmdOption = []) {
        def writer = new StringWriter()
        GradleRunner runner = newRunner(task, writer, gradleVersion, cmdOption)

        def result

        switch (r) {
            case RunType.PASS:
                result = runner.build()
                break
            case RunType.FAIL:
                result = runner.buildAndFail()
                break
        }

        println writer
        return result
    }

    private GradleRunner newRunner(StandardTextValuesTasks task, StringWriter writer, String gradleVersion,
                                   List<String> cmdOption = []) {
        def gradleUserHome = temporaryFolder.newFolder('some-custom-user-home')

         cmdOption.add(0, task.value)
         cmdOption.addAll(["-g", gradleUserHome.absolutePath])

        def runner = GradleRunner.create()
                .withProjectDir(temporaryFolder.root)
                .withArguments( cmdOption)
                //.withPluginClasspath(pluginClasspath)
                .forwardStdOutput(writer)
                .withPluginClasspath()

        if (gradleVersion) {
            runner.withGradleVersion(gradleVersion)
        }

        runner
    }
}
