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
package com.linkedin.gradle.python.util.zipapp;

import com.linkedin.gradle.python.PythonExtension;
import com.linkedin.gradle.python.extension.CliExtension;
import com.linkedin.gradle.python.util.ExtensionUtils;
import org.apache.commons.io.IOUtils;

import java.io.IOException;

public class DefaultPexEntryPointTemplateProvider implements EntryPointTemplateProvider {

    @Override
    public String retrieveTemplate(TemplateProviderOptions options, boolean isPythonWrapper) throws IOException {
        PythonExtension extension = options.getExtension();
        CliExtension cliExtension = ExtensionUtils.findPythonComponentExtension(extension, CliExtension.class);
        String template = (cliExtension != null && isPythonWrapper)
            ? "/templates/pex_cli_entrypoint.py.template"
            : "/templates/pex_non_cli_entrypoint.sh.template";

        return IOUtils.toString(DefaultPexEntryPointTemplateProvider.class.getResourceAsStream(template));
    }
}
