package com.linkedin.gradle.python.tasks


import com.linkedin.gradle.python.internal.platform.PythonVersion
import com.linkedin.gradle.python.internal.toolchain.PythonExecutable
import com.linkedin.gradle.python.spec.component.internal.PythonEnvironment
import org.gradle.api.Action
import org.gradle.process.ExecResult
import org.gradle.process.internal.ExecAction

public class PythonTestEnvironmentBuilder {

    PythonTestEnvironmentBuilder() {
    }
    PythonExecutable createPythonExecutable(ExecAction execAction, int exitCode) {
        return new PythonExecutable() {
            @Override
            File getPythonPath() {
                return null
            }

            @Override
            ExecResult execute(Action<ExecAction> action) {
                action.execute(execAction)
                return [
                    getExitValue         : { -> exitCode },
                    assertNormalExitValue: { ->
                        if (exitCode != 0) {
                            throw new RuntimeException()
                        }
                    }
                ] as ExecResult
            }
        }
    }

    public PythonEnvironment build(ExecAction execAction, int exitCode) {
        return new PythonEnvironment() {

            @Override
            String getEnvironmentSetupTaskName() {
                return ''
            }

            @Override
            PythonExecutable getVirtualEnvPythonExecutable() {
                return createPythonExecutable(execAction, exitCode)
            }

            @Override
            PythonExecutable getSystemPythonExecutable() {
                return createPythonExecutable(execAction, exitCode)
            }

            @Override
            File getVenvDir() {
                return new File("/build/venv")
            }

            @Override
            PythonVersion getVersion() {
                return PythonVersion.VERSION_2_6
            }

            @Override
            File getVendorDir() {
                return new File("/build/vendor")
            }

            @Override
            File getBuildDir() {
                return new File("/build")
            }

            @Override
            File getPythonBuildDir() {
                return new File("/build")
            }

            @Override
            String getEnvironmentName() {
                return 'foo'
            }
        }
    }
}
