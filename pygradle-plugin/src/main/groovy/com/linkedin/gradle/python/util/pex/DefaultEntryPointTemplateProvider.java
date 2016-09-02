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
package com.linkedin.gradle.python.util.pex;

import com.linkedin.gradle.python.PythonExtension;
import com.linkedin.gradle.python.extension.CliExtension;
import com.linkedin.gradle.python.extension.PexExtension;
import com.linkedin.gradle.python.util.ExtensionUtils;
import org.apache.commons.io.IOUtils;

import java.io.IOException;

public class DefaultEntryPointTemplateProvider implements EntryPointTemplateProvider {

    @Override
    public String retrieveTemplate(TemplateProviderOptions options) throws IOException {
        PythonExtension extension = options.getExtension();
        PexExtension pexExtension = ExtensionUtils.getPythonComponentExtension(extension, PexExtension.class);
        CliExtension cliExtension = ExtensionUtils.findPythonComponentExtension(extension, CliExtension.class);
        if (cliExtension != null && pexExtension.isPythonWrapper()) {
            return IOUtils.toString(DefaultEntryPointTemplateProvider.class.getResourceAsStream("/templates/pex_cli_entrypoint.py.template"));
        } else {
            return IOUtils.toString(DefaultEntryPointTemplateProvider.class.getResourceAsStream("/templates/pex_non_cli_entrypoint.sh.template"));
        }
    }
}
