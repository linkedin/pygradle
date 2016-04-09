package com.linkedin.gradle.python.spec.component;

import org.gradle.internal.HasInternalProtocol;
import org.gradle.platform.base.GeneralComponentSpec;


@HasInternalProtocol
public interface PythonComponentSpec extends GeneralComponentSpec {
    /**
     * Specifies a platform that this component should be built be for.
     */
    void targetPlatform(String targetPlatform);
}
