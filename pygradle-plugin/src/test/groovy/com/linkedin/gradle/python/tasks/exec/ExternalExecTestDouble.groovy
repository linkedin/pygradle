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
package com.linkedin.gradle.python.tasks.exec

import org.gradle.api.Action
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec

class ExternalExecTestDouble implements ExternalExec {

    private final ExecSpec spec

    ExternalExecTestDouble(ExecSpec spec) {
        this.spec = spec
    }

    @Override
    ExecResult exec(Action<? super ExecSpec> action) {
        action.execute(spec)
        return ['getExitValue': { -> 0 }] as ExecResult
    }
}
