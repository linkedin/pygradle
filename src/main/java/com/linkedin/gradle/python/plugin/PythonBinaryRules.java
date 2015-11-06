package com.linkedin.gradle.python.plugin;

import com.linkedin.gradle.python.internal.platform.PythonPlatform;
import com.linkedin.gradle.python.internal.platform.PythonToolChainRegistry;
import com.linkedin.gradle.python.spec.PythonComponentSpec;
import com.linkedin.gradle.python.spec.WheelBinarySpec;
import java.io.File;
import org.gradle.api.Action;
import org.gradle.language.base.internal.BuildDirHolder;
import org.gradle.model.Defaults;
import org.gradle.model.RuleSource;


/**
 * {@link org.gradle.jvm.internal.JarBinaryRules}
 */
@SuppressWarnings("UnusedDeclaration")
public class PythonBinaryRules extends RuleSource {

  @Defaults
  void configureJarBinaries(final PythonComponentSpec pythonLibrary, final BuildDirHolder buildDirHolder, final PythonToolChainRegistry toolChains) {

    pythonLibrary.getBinaries().withType(WheelBinarySpec.class).beforeEach(new Action<WheelBinarySpec>() {
      @Override
      public void execute(WheelBinarySpec wheelBinarySpec) {
//        PythonToolChain platform = toolChains.getForPlatform(wheelBinarySpec.getTargetPlatform());
//        PythonPlatform platform = wheelBinarySpec.getTargetPlatform();
//        final File pythonBuildDir = new File(buildDirHolder.getDir(), "python-" + platform.getVersion().getVersionString());
//        final File virtualEnvDir = new File(pythonBuildDir, "venv");
//        wheelBinarySpec.setVirtualEnvDir(virtualEnvDir);
//        wheelBinarySpec.setPythonBuildDir(pythonBuildDir);
      }
    });
  }
}
