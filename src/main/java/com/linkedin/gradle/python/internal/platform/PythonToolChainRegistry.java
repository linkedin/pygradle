package com.linkedin.gradle.python.internal.platform;

import com.linkedin.gradle.python.internal.toolchain.PythonToolChain;
import com.linkedin.gradle.python.spec.component.internal.PythonTargetPlatform;
import org.gradle.platform.base.ToolChainRegistry;


public interface PythonToolChainRegistry extends ToolChainRegistry<PythonTargetPlatform, PythonToolChain> {
}
