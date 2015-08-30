package com.linkedin.gradle.python.internal;

import com.linkedin.gradle.python.PythonSourceSet;
import org.gradle.language.base.sources.BaseLanguageSourceSet;
import org.gradle.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DefaultPythonSourceSet extends BaseLanguageSourceSet implements PythonSourceSet {
    private final List<Object> libs = new ArrayList<Object>();

    @Override
    protected String getTypeName() {
        return "Python source";
    }

    public Collection<?> getLibs() {
        return libs;
    }

    public void lib(Object library) {
        if (library instanceof Iterable<?>) {
            Iterable<?> iterable = (Iterable) library;
            CollectionUtils.addAll(libs, iterable);
        } else {
            libs.add(library);
        }
    }

    @Override
    public void setPreCompiledHeader(String s) {
        //NOOP
    }

    @Override
    public String getPreCompiledHeader() {
        return null;
    }
}
