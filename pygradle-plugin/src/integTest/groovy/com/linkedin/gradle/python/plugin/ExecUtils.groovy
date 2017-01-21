package com.linkedin.gradle.python.plugin

import com.linkedin.gradle.python.util.OperatingSystem
import org.apache.commons.exec.CommandLine
import org.apache.commons.exec.DefaultExecutor
import org.apache.commons.exec.PumpStreamHandler

import java.nio.file.Path

class ExecUtils {

    static String run(Path path) {

        ByteArrayOutputStream os = new ByteArrayOutputStream()
        def executor = new DefaultExecutor()
        executor.streamHandler = new PumpStreamHandler(os)

        def cmd = OperatingSystem.current().isWindows() ? "python ${path.toString()}" : path.toString()

        def commandLine = CommandLine.parse(cmd)
        executor.execute(commandLine)

        return os.toString()
    }
}
