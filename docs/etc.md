The Python ecosystem is extremely productive in aspects such as local
development, artifact management, library functionality, and more. But, the
Python ecosystem does struggle with things like dependency management,
dependency resolution, conflict resolution, integration with existing metadata
systems, and more. Projects that are built with the PyGradle build system get
the best of both worlds by leveraging each tool, Python and Gradle, only for
what it is good at.

The @linkedin/pygradle-devs team feels the major advantages of using PyGradle,
among many others, are the following.

- You get real dependency resolution as opposed to 'mock' dependency resolution.
- You get conflict resolution which resolves the notorious VersionConflict and DistributionNotFound errors that plague large projects.
- You get the Gradle cache which is an enterprise quality caching solution that many organizations leverage.
- You get the ability to build your Python code alongside your Java, Scala, and C++ code in a first class way.
- You get a pluggable build system that can quickly be customized or adapted to support new technologies like pex.
- You get to integrate completely with the metadata systems that teams have spent millions of engineering dollars working on.

