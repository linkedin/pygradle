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
package com.linkedin.gradle.python.util.internal;

import java.util.HashMap;
import java.util.Map;

public class TaskTimer {

    private Map<String, Long> timingResults = new HashMap<>();

    public TickingClock start(String name) {
        return new TickingClock(name, System.currentTimeMillis());
    }

    public String buildReport() {
        StringBuilder sb = new StringBuilder();
        timingResults.forEach((key, value) -> sb.append(key).append(":\t").append(value).append("\n"));

        return sb.toString();
    }

    public class TickingClock {
        private final String name;
        private final long startedAt;

        TickingClock(String name, long startedAt) {
            this.name = name;
            this.startedAt = startedAt;
        }

        public void stop() {
            timingResults.put(name, System.currentTimeMillis() - startedAt);
        }
    }
}
