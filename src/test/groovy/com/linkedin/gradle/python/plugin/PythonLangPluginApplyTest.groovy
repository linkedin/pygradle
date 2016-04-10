package com.linkedin.gradle.python.plugin;

import nebula.test.PluginProjectSpec;


/**
 * This is used to make sure the plugin can apply
 */
public class PythonLangPluginApplyTest extends PluginProjectSpec {
    @Override
    String getPluginName() {
        return 'python-lang'
    }
}
