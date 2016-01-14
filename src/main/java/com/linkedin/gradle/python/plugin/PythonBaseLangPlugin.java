package com.linkedin.gradle.python.plugin;

import com.linkedin.gradle.python.plugin.internal.PythonPluginConfigurations;
import com.linkedin.gradle.python.plugin.internal.base.PythonBaseRulePlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;


/**
 * Base plugin for Py-Gradle.
 *
 * This plugin is responsible for applying all of the required plugins to make python build.
 *
 * The following example is the most simple python build that can be done. It defines a single targetPlatform (what python
 * you want to use). This also declares the sources that should be used for determining if a task should run.
 * When this runs, no artifacts will be built but tests will be run.
 * <pre>
 *  components {
 *      python(com.linkedin.gradle.python.spec.component.PythonComponentSpec) {
 *          targetPlatform 'python2.7'
 *          sources {
 *              pyMain(com.linkedin.gradle.python.PythonSourceSet) {
 *                  source {
 *                      srcDir 'src/main/python'
 *                      include '**\/*.py'
 *                  }
 *              }
 *              pyTest(com.linkedin.gradle.python.PythonTestSourceSet) {
 *                  source {
 *                      srcDir 'src/test/python'
 *                      include '**\/*.py'
 *                  }
 *              }
 *          }
 *      }
 *  }
 * </pre>
 *
 * A more complicated example would be to build both wheels and source dists.
 * <pre>
 *  components {
 *      python(com.linkedin.gradle.python.spec.component.PythonComponentSpec) {
 *          targetPlatform '/export/apps/python/2.7.11/bin/python2.7'
 *          binaries {
 *              wheel(com.linkedin.gradle.python.spec.binary.WheelBinarySpec) {
 *                  targets '2.7'
 *              }
 *              source(com.linkedin.gradle.python.spec.binary.SourceDistBinarySpec) {
 *                  targets 'python2.7'
 *              }
 *          }
 *          sources {
 *              pyMain(com.linkedin.gradle.python.PythonSourceSet) {
 *                  source {
 *                      srcDir 'src/main/python'
 *                      include '**\/*.py'
 *                  }
 *              }
 *              pyTest(com.linkedin.gradle.python.PythonTestSourceSet) {
 *                  source {
 *                      srcDir 'src/test/python'
 *                      include '**\/*.py'
 *                  }
 *              }
 *          }
 *      }
 *  }
 * </pre>
 *
 * As you can see in the example, you must define a target platform, but you don't have to use the one defined as a
 * <pre>targetPlatform</pre>. It's also worth noting that if needed, you can specify the full path to a python executable.
 *
 * When the targetPlatform is evaluated all the <pre>targetPlatform</pre>'s will be evaluated each python will be executed
 * to determine the version.
 *
 * When defining binaries, you can specify the target in full version number (2.7.11), major.minor (2.7), or executable name (python2.7).
 */
public class PythonBaseLangPlugin implements Plugin<Project>  {

    public static final String PYTHON_CONFIGURATIONS = "pythonConfigurations";

    public void apply(final Project project) {
        project.getPluginManager().apply(PythonBaseRulePlugin.class);

        if (project.getExtensions().findByName(PYTHON_CONFIGURATIONS) == null) {
            project.getExtensions().create(PYTHON_CONFIGURATIONS, PythonPluginConfigurations.class, project.getConfigurations(), project.getDependencies());
        }
    }
}
