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
import com.linkedin.gradle.python.spec.component.PythonComponentSpec;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.language.base.LanguageSourceSet;
import org.gradle.model.ModelMap;
import org.gradle.model.Mutate;
import org.gradle.model.RuleSource;


/**
 * This plugin provides a default location for all sources. The default location for python source files is src/main/python.
 * The default location for tests is src/test/python.
 * <p>
 * These values have no baring on the setup.py's dist, but allow gradle to cache inputs and outputs more effectively.
 * <p>
 * For more examples on how to configure this plugin, please check out {@link PythonBaseLangPlugin}.
 */
public class PythonLangPlugin implements Plugin<Project> {

    public void apply(final Project project) {
        project.getPluginManager().apply(PythonBaseLangPlugin.class);
        project.getPluginManager().apply(Rules.class);
    }

    public static class Rules extends RuleSource {
        @Mutate
        void createPythonSourceSets(ModelMap<PythonComponentSpec> binaries) {
            binaries.all(new Action<PythonComponentSpec>() {
                @Override
                public void execute(PythonComponentSpec wheelComponentSpec) {
                    ModelMap<LanguageSourceSet> sources = wheelComponentSpec.getSources();
                    if (!sources.containsKey("python")) {
                        sources.create("python", PythonSourceSet.class, new Action<PythonSourceSet>() {
                            @Override
                            public void execute(PythonSourceSet defaultPythonSourceSet) {
                                defaultPythonSourceSet.getSource().srcDir("src/main/python");
                                defaultPythonSourceSet.getSource().include("**/*.py");
                            }
                        });
                    }
                    if (!sources.containsKey("pythonTest")) {
                        sources.create("pythonTest", PythonTestSourceSet.class, new Action<PythonTestSourceSet>() {
                            @Override
                            public void execute(PythonTestSourceSet pythonTestSourceSet) {
                                pythonTestSourceSet.getSource().srcDir("src/test/python");
                                pythonTestSourceSet.getSource().include("**/*.py");
                            }
                        });
                    }
                }
            });
        }
    }
}
