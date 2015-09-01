package com.linkedin.gradle.python.internal;

import com.linkedin.gradle.python.PythonSourceSet;
import org.gradle.language.base.sources.BaseLanguageSourceSet;

public class DefaultPythonSourceSet extends BaseLanguageSourceSet implements PythonSourceSet {
    @Override
    protected String getTypeName() {
        return "Python source";
    }
}
