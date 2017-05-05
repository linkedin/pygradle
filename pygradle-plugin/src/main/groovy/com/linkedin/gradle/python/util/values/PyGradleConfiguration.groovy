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
package com.linkedin.gradle.python.util.values

/**
 * CodeNarc is complaining that some classes exceed 350 lines.  TO fall into compliance with CodeNarc, the standard values
 * are being moved to this enum.  Its an Enum rather than an interface because according to CodeNarc, interfaces of nothing
 * but constants is now taboo.
 */
enum PyGradleConfiguration {
    BOOTSTRAP_REQS('pygradleBootstrap'),
    SETUP_REQS('setupRequires'),
    BUILD_REQS('build'),
    DEFAULT('default'),
    PYDOCS('pydocs'),
    PYTHON('python'),
    TEST('test'),
    VENV('venv'),
    WHEEL('wheel'),

    private final String value

    PyGradleConfiguration(String val) {
        this.value = val
    }

    @Override
    String toString() {
        return value
    }

    String getValue() {
        return value
    }
}
