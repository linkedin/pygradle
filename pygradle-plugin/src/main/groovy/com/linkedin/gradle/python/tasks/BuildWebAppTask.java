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
package com.linkedin.gradle.python.tasks;

import com.linkedin.gradle.python.extension.PexExtension;
import com.linkedin.gradle.python.util.ExtensionUtils;
import com.linkedin.gradle.python.util.PexFileUtil;
import com.linkedin.gradle.python.util.entrypoint.EntryPointWriter;
import com.linkedin.gradle.python.util.internal.pex.FatPexGenerator;
import com.linkedin.gradle.python.util.internal.pex.ThinPexGenerator;
import org.apache.commons.io.IOUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class BuildWebAppTask extends DefaultTask {

    private File executable;
    private String entryPoint;
    private List<String> pexOptions = new ArrayList<>();

    @Input
    @Optional
    public List<String> getPexOptions() {
        return pexOptions;
    }

    public void setPexOptions(List<String> pexOptions) {
        this.pexOptions = pexOptions;
    }

    @OutputFile
    public File getExecutable() {
        return executable;
    }

    @Input
    public String getEntryPoint() {
        return entryPoint;
    }

    @TaskAction
    public void buildWebapp() throws IOException, ClassNotFoundException {
        Project project = getProject();
        PexExtension pexExtension = ExtensionUtils.getPythonComponentExtension(project, PexExtension.class);

        if (pexExtension.isFat()) {
            new FatPexGenerator(project, pexOptions).buildEntryPoint(
                PexFileUtil.createFatPexFilename(executable.getName()), entryPoint, null);
        } else {
            HashMap<String, String> options = new HashMap<>();
            options.put("entryPoint", entryPoint);
            options.put("realPex", PexFileUtil.createThinPexFilename(project.getName()));
            String template = IOUtils.toString(
                ThinPexGenerator.class.getResourceAsStream("/templates/pex_non_cli_entrypoint.sh.template"));
            new EntryPointWriter(project, template).writeEntryPoint(executable, options);
        }
    }

    public void setExecutable(File executable) {
        this.executable = executable;
    }

    public void setEntryPoint(String entryPoint) {
        this.entryPoint = entryPoint;
    }
}
