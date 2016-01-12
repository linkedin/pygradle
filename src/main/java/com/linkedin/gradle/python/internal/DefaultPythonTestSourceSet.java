package com.linkedin.gradle.python.internal;

import com.linkedin.gradle.python.PythonTestSourceSet;
import org.gradle.language.base.sources.BaseLanguageSourceSet;


public class DefaultPythonTestSourceSet extends BaseLanguageSourceSet implements PythonTestSourceSet {
    @Override
    protected String getTypeName() {
        return "Python Test source";
    }
}
