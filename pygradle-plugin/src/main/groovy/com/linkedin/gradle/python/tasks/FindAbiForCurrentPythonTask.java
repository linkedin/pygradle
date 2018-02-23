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

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.linkedin.gradle.python.extension.PythonDetails;
import com.linkedin.gradle.python.wheel.AbiDetails;
import com.linkedin.gradle.python.wheel.SupportedWheelFormats;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class FindAbiForCurrentPythonTask extends DefaultTask {

    private SupportedWheelFormats supportedWheelFormat;

    private List<Supplier<PythonDetails>> supportedPythonVersions = new ArrayList<>();

    public FindAbiForCurrentPythonTask() {
        //We need the exec to happen, so this should always run.
        getOutputs().upToDateWhen(it -> false);
    }

    public void addPythonDetails(PythonDetails pythonDetails) {
        supportedPythonVersions.add(() -> pythonDetails);
    }

    public void addPythonDetails(Supplier<PythonDetails> pythonDetails) {
        supportedPythonVersions.add(pythonDetails);
    }

    @InputFiles
    public List<File> getPythonInterpreters() {
        return getSupportedPythons().stream().map(PythonDetails::getVirtualEnvInterpreter).collect(Collectors.toList());
    }

    public List<PythonDetails> getSupportedPythons() {
        return supportedPythonVersions.stream()
            .map(Supplier::get)
            .collect(Collectors.toList());
    }

    @OutputFile
    public File getPythonFileForSupportedWheels() {
        return new File(getProject().getBuildDir(), "wheel-api.py");
    }

    private File getSupportedAbiFormatsFile(PythonDetails pythonDetails) {
        return new File(getProject().getBuildDir(),
            "wheel-abi-result" + pythonDetails.getPythonVersion().getPythonMajorMinor() + ".json");
    }

    @TaskAction
    public void writeWheelAbiFile() throws IOException {
        InputStream wheelApiResource = FindAbiForCurrentPythonTask.class.getClassLoader()
            .getResourceAsStream("templates/wheel-api.py");

        byte[] buffer = new byte[wheelApiResource.available()];
        wheelApiResource.read(buffer);

        OutputStream outStream = new FileOutputStream(getPythonFileForSupportedWheels());
        outStream.write(buffer);

        for (PythonDetails supportedPythonVersion : getSupportedPythons()) {
            File supportedAbiFormatsFile = getSupportedAbiFormatsFile(supportedPythonVersion);
            getProject().exec(execSpec -> {
                execSpec.commandLine(supportedPythonVersion.getVirtualEnvInterpreter());
                execSpec.args(getPythonFileForSupportedWheels());
                execSpec.args(supportedAbiFormatsFile.getAbsolutePath());
            });

            JsonArray array = Json.parse(new FileReader(supportedAbiFormatsFile)).asArray();
            for (JsonValue jsonValue : array) {
                JsonObject entry = jsonValue.asObject();
                String pythonTag = entry.get("pythonTag").asString();
                String abiTag = entry.get("abiTag").asString();
                String platformTag = entry.get("platformTag").asString();

                AbiDetails triple = new AbiDetails(supportedPythonVersion.getVirtualEnvInterpreter(),
                    pythonTag, abiTag, platformTag);
                supportedWheelFormat.addSupportedAbi(triple);
            }
        }

        getLogger().info("Supported Platforms: {}", supportedWheelFormat);
    }

    public void setSupportedWheelFormat(SupportedWheelFormats supportedWheelFormat) {
        this.supportedWheelFormat = supportedWheelFormat;
    }
}
