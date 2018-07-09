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

import com.linkedin.gradle.python.util.OperatingSystem
import org.apache.commons.exec.CommandLine
import org.apache.commons.exec.DefaultExecutor
import org.apache.commons.exec.ExecuteException
import org.apache.commons.exec.PumpStreamHandler

import java.nio.file.Path

class ExecUtils {

    static String run(Path path) {
        return run(path, new String[0])
    }

    static String run(Path path, String... args) {

        ByteArrayOutputStream os = new ByteArrayOutputStream()
        def executor = new DefaultExecutor()
        executor.streamHandler = new PumpStreamHandler(os)

        def cmd = OperatingSystem.current().isWindows() ? "python ${path.toString()}" : path.toString()

        def commandLine = CommandLine.parse(cmd)
        commandLine.addArguments(args)

        try {
            executor.execute(commandLine)
        } catch (ExecuteException exception) {
            println(os.toString())
            throw exception
        }

        return os.toString()
    }
}
