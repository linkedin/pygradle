# Getting Started

As covered in our main [README](../README.md), there are a few high level
things you're going to have to decide:

- Decide how you want to host your artifacts.
- Decide how you want to distribute and install a custom setuptools distribution class.
- Author your build.gradle file.

We'll cover each of these topics in more detail below. If you're reading this,
you should be familiar with the topics in the [Gradle User
Guide](https://docs.gradle.org/3.3/userguide/userguide.html).

## Hosting

The Python community maintains [The Python Package
Index](https://pypi.org), or PyPI for short, to host open source
Python artifacts.

Unfortunately, PyGradle cannot leverage PyPI yet for one primary reason: open
source Python artifacts don't publish Ivy metadata. Ivy metadata is a
requirement for Gradle to do proper dependency management. Until the entire
Python community is emitting Ivy metadata, we'll need to find an alternative to
PyPI.

LinkedIn uses [Artifactory](https://www.jfrog.com/artifactory/) to host
artifacts. Artifactory is well suited for a number of artifact hosting purposes
and comes with many plugins that you can install. One of these plugins is a
PyPI-compatible interface. Although not needed for the purposes of PyGradle,
it's a nifty interface for making open source and internal Python projects play
nicely together.

LinkedIn's main use of Artifactory is to host two types of artifacts:

- Artifacts that we've mirrored from open source _and enhanced with Ivy metadata_.
- Artifacts that we've built and published using PyGradle which automatically have Ivy metadata.

The core concept to walk away with from this section is this: you need Ivy
metadata and a way to host them for your artifacts to be usable within a Gradle
ecosystem.

### Artifactory

LinkedIn's [public Artifactory instance](https://linkedin.jfrog.io/linkedin/webapp/#/artifacts/browse/tree/General/pypi-external)
is an example of an artifact hosting server that has examples of both types of
artifacts: those that we've mirrored and enhanced with Ivy metadata, and those
that we've published using PyGradle.

If you would like to use this demonstrative Artifactory instance, you can
configure your project to use it with the following code in your `build.gradle`.

    repositories {
       pyGradlePyPi()
    }

Remember, if you configure your project to use LinkedIn's demonstrative
Artifactory instance, only those artifacts hosted there will be available to
your project.

If you choose to use Artifactory, you can refer to JFrog's [User
Guide](https://www.jfrog.com/confluence/display/RTF/Using+Artifactory) to get
started.

#### Tools

Observant readers will realize that mirroring and enhancing open source
artifacts could be very tedious. It is. So, we've built tooling to make this
easier: [pivy-importer](pivy-importer.md) helps create Ivy metadata for a given
project that is suitable for publication to your artifact hosting server of
choice.

Please note, pivy-importer is a tool provided as-is, and was only used for
building our demonstrative Artifactory instance for the examples. We're happy
to review and pull requests that harden the pivy-importer tool!

### Other

We'd love to hear from someone else using PyGradle without Artifactory. If you
are doing this, please submit a pull request to tell us how you set up your
environment!

## Setuptools

You'll use a custom [setuptools](https://setuptools.readthedocs.io/en/latest/)
distribution class to take care of passing information from Gradle (i.e., your
`build.gradle` file) to Python (i.e., your `setup.py` file).

For example, the following distribution class is a very simple one that takes
the project's name and version from environment variables passed in by Gradle
plugin.

    from setuptools import setup, find_packages

    class GradleDistribution(Distribution, object):
        def __init__(self, attrs):
            attrs['name'] = os.getenv('PYGRADLE_PROJECT_NAME')
            attrs['version'] = os.getenv('PYGRADLE_PROJECT_VERSION')

    setup(
        distclass=GradleDistribution,
        package_dir={'': 'src'},
        packages=find_packages('src'),
        include_package_data=True,
    )

This is a pretty trivial example. In practice, your distribution class will
probably look more like [this](../examples/example-project/setup.py).

If you'd like to use our suggested `setup.py` template, you can make use of the
`generateSetupPy` task that is provided as part of PyGradle. This task uses a
[template](../pygradle-plugin/templates/setup.py.template) to automatically
generate your project's `setup.py` file. Be careful, this task *will* overwrite
any existing setup.py in the project.

## Gradle

Finally, lets write some Gradle code.

    plugins {
      id "com.linkedin.python-sdist" version "0.3.9"
    }

    dependencies {
        python 'pypi:requests:2.9.1'
        test 'pypi:mock:2.0.0'
    }

    repositories {
       pyGradlePyPi()
    }

This `build.gradle` file is nice and simple: it applies the PyGradle source
distribution plugin, it declares two dependencies, and lastly, registers our
demonstrative Artifactory instance as the server from which artifacts will be
pulled.

### Customization

You will very likely want to customize PyGradle to fit the needs of your
particular project or enterprise. Gradle makes this easy.

We suggest that you customize PyGradle by wrapping it within your own plugins.

Wrapping PyGradle within your own plugins gives you a good way to do
things like register your artifact hosting server with all of your projects. It
also gives you the ability to do things like marry PyGradle with your
enterprise's proprietary way of managing project name or version. It could even
be used to leverage entirely new metadata formats.

The sky is the limit!

# Examples

For hands on examples, see the example project in [examples](../examples).
