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

package com.linkedin.gradle.python.plugin;

import com.linkedin.gradle.python.PythonSourceSet;
import com.linkedin.gradle.python.PythonTestSourceSet;
import com.linkedin.gradle.python.plugin.extension.PythonPluginConfigurations;
import com.linkedin.gradle.python.plugin.internal.base.PythonBaseRulePlugin;
import com.linkedin.gradle.python.plugin.internal.sources.SourceDistRulePlugin;
import com.linkedin.gradle.python.plugin.internal.wheel.WheelRulePlugin;
import com.linkedin.gradle.python.spec.binary.SourceDistBinarySpec;
import com.linkedin.gradle.python.spec.binary.WheelBinarySpec;
import com.linkedin.gradle.python.spec.component.PythonComponentSpec;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.publish.ivy.plugins.IvyPublishPlugin;


/**
 * Base plugin for Py-Gradle.
 * <p>
 * This plugin is responsible for applying all of the required plugins to make python build.
 * <p>
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
 * <p>
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
 * <p>
 * As you can see in the example, you must define a target platform, but you don't have to use the one defined as a
 * <pre>targetPlatform</pre>. It's also worth noting that if needed, you can specify the full path to a python executable.
 *
 * When the targetPlatform is evaluated all the <pre>targetPlatform</pre>'s will be evaluated each python will be executed
 * to determine the version.
 *
 * When defining binaries, you can specify the target in full version number (2.7.11), major.minor (2.7), or executable name (python2.7).
 */
public class PythonBaseLangPlugin implements Plugin<Project> {

    public static final String PYTHON_CONFIGURATIONS = "pythonConfigurations";

    public void apply(final Project project) {
        project.getPluginManager().apply(BasePlugin.class);
        project.getPluginManager().apply(IvyPublishPlugin.class);
        project.getPluginManager().apply(PythonBaseRulePlugin.class);
        project.getPluginManager().apply(SourceDistRulePlugin.class);
        project.getPluginManager().apply(WheelRulePlugin.class);

        project.getExtensions().getExtraProperties().set(PythonComponentSpec.class.getSimpleName(), PythonComponentSpec.class);
        project.getExtensions().getExtraProperties().set(PythonTestSourceSet.class.getSimpleName(), PythonTestSourceSet.class);
        project.getExtensions().getExtraProperties().set(PythonSourceSet.class.getSimpleName(), PythonSourceSet.class);
        project.getExtensions().getExtraProperties().set(WheelBinarySpec.class.getSimpleName(), WheelBinarySpec.class);
        project.getExtensions().getExtraProperties().set(SourceDistBinarySpec.class.getSimpleName(), SourceDistBinarySpec.class);

        if (project.getExtensions().findByName(PYTHON_CONFIGURATIONS) == null) {
            project.getExtensions().create(PYTHON_CONFIGURATIONS, PythonPluginConfigurations.class, project.getConfigurations(), project.getDependencies());
        }
    }
}
