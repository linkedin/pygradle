package com.linkedin.gradle.python.util.internal;

import java.util.HashMap;
import java.util.Map;

public class TaskTimer {

    Map<String, Long> timingResults = new HashMap<>();

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
