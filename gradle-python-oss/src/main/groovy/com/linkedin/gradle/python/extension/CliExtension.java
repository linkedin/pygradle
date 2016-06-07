package com.linkedin.gradle.python.extension;

public class CliExtension {
    private boolean generateCompletions = false;

    public boolean isGenerateCompletions() {
        return generateCompletions;
    }

    public void setGenerateCompletions(boolean generateCompletions) {
        this.generateCompletions = generateCompletions;
    }
}
