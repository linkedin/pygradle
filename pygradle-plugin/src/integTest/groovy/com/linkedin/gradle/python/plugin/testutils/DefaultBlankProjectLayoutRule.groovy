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
package com.linkedin.gradle.python.plugin.testutils

import java.nio.file.Paths

class DefaultBlankProjectLayoutRule extends DefaultProjectLayoutRule {

    @Override
    void setupProject() {
        // set up the default project name
        def settingsGradle = newFile('settings.gradle')
        settingsGradle << PyGradleTestBuilder.createSettingGradle()
        settingsGradle << "\ninclude ':foo'\n"
    }


    void copyBuildDocsInfo() {
        String docsFldr = "docs"

        newFolder(PROJECT_NAME_DIR, docsFldr)
        def spinxConfig = newFile(Paths.get(PROJECT_NAME_DIR, docsFldr, "conf.py").toFile().path)
        spinxConfig << sphinxConfigData()

        def indexRs = newFile(Paths.get(PROJECT_NAME_DIR, docsFldr, "index.rst").toFile().path)

        indexRs << """
            This Is Cool
            ============
            """.stripIndent()
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    String sphinxConfigData() {
        """
        | import os
        |
        | extensions = [
        |     'sphinx.ext.autodoc',
        |     'sphinx.ext.doctest',
        |     'sphinx.ext.intersphinx',
        |     'sphinx.ext.todo',
        |     'sphinx.ext.coverage',
        |     'sphinx.ext.mathjax',
        |     'sphinx.ext.ifconfig',
        |     'sphinx.ext.viewcode',
        | ]
        |
        | templates_path = ['_templates']
        | source_suffix = '.rst'
        | master_doc = 'index'
        | project =os.getenv('PYGRADLE_PROJECT_NAME')
        | copyright = u'2017, PygradleTests'
        | version = os.getenv('PYGRADLE_PROJECT_VERSION')
        | release = version
        | language = None
        | exclude_patterns = ['_build']
        | pygments_style = 'sphinx'
        | keep_warnings = True
        | todo_include_todos = True
        | html_theme = 'bizstyle'
        | html_theme_options = {}
        | html_static_path = ['_static']
        | htmlhelp_basename = '@htmlhelp_basename@'
        | latex_elements = {
        | }
        | latex_documents = [
        |   (master_doc, '@projectName@.tex', u'@projectName@ Documentation',
        |    u'@author@', 'manual'),
        | ]
        | epub_title = project
        | epub_copyright = copyright
        | epub_exclude_files = ['search.html']""".stripMargin().stripIndent()
    }
}
