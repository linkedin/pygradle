# pivy-importer

The pivy-importer is a Java application that can be used to pull libraries from PyPI into an Ivy format. It has several
features that you may find useful in dealing with pypi projects.

## Getting the Jar

### Published Jar

We publish the importer into bintray. You can find all the copies at https://bintray.com/linkedin/maven/pivy-importer.

At the time of writing this doc, the current version is 0.9.1, so the jar you would want to download is
https://dl.bintray.com/linkedin/maven/com/linkedin/pygradle/pivy-importer/0.9.1/pivy-importer-0.9.1-all.jar .
This artifact has all dependencies bundled together to make a nice CLI.

### Building it Locally

To build the pivy-importer you'll need to run `../gradlew build` in the pivy-importer package. This will produce several
jars in `../build/pivy-importer/libs/` (relative to the `pivy-importer` directory). We produce a normal jar that could
be used when consuming in other projects and a fat jar, with all the dependencies added into a single executable. This
one is the one you would use on the command line.  It follows the naming convention `pivy-importer-<VERSION>-all.jar`.

## General Usage

`java -jar pivy-importer-VERSION-all.jar --repo /path/to/destination virtualenv:15.0.1 pip:7.1.2 --replace alabaster:0.7=alabaster:0.7.1,pytz:0a=pytz:2016.4,Babel:0.8=Babel:1.0,sphinx_rtd_theme:0.1=sphinx_rtd_theme:0.1.1`

For a complete usage example please review the build.gradle file for the pivy-importer project.

#### Arguments
* --repo - location of the ivy repo : `--repo /path/to/destination`
* --replace - replacement of defined transitive dependency : `--replace alabaster:0.7=alabaster:0.7.1,pytz:0a=pytz:2016.4`
* --quiet - sets logging level to WARN : `--quiet`
* --force - same as replace but with higher priority : `--force alabaster:0.7`
* --latest - gets latest versions of dependencies : `--latest`
* --pre - allows pre-releases (alpha, beta, release candidates) : `--pre`
* --lenient - allows to continue processing in case of missing dependency with logging it : `--lenient`

## Replacement

In some cases it's useful to replace dependencies with another one to deal with how Pip does dependency resolution.
To enable this feature you need to add the option `--replace` followed by a list of arguments in the form
`oldName:oldVersion=newName:newVersion` (`alabaster:0.7=alabaster:0.7.1`). You can provide multiple
replacements by joining them with a comma.

## Usage in Gradle

To be able to consume the artifacts produced by the pivy-importer, you'll have to define a
[repository](https://docs.gradle.org/current/userguide/dependency_management.html#sec:repositories) in Gradle. We use a
specific format for the repository that you'll have to define. Here is an example definition in Gradle. You would add
this to your project's build.gradle.

```
repositories {
  ivy {
    name 'pypi-local'   //optional, but nice
    url '/path/to/repo'
    layout "pattern", {
      ivy "[organisation]/[module]/[revision]/[module]-[revision].ivy"
      artifact "[organisation]/[module]/[revision]/[artifact]-[revision](-[classifier]).[ext]"
      m2compatible = true
    }
  }
}
```


## Example Usage

If you run:

```
wget https://dl.bintray.com/linkedin/maven/com/linkedin/pygradle/pivy-importer/0.9.1/pivy-importer-0.9.1-all.jar
mkdir repo
java -jar pivy-importer-0.9.1-all.jar --repo repo virtualenv:15.0.1 pip:7.1.2 --replace alabaster:0.7=alabaster:0.7.1,pytz:0a=pytz:2016.4,Babel:0.8=Babel:1.0,sphinx_rtd_theme:0.1=sphinx_rtd_theme:0.1.1
```

You should see something like:
```
--2016-09-09 08:44:21--  https://dl.bintray.com/linkedin/maven/com/linkedin/pygradle/pivy-importer/0.9.1/pivy-importer-0.9.1-all.jar
Resolving dl.bintray.com... 75.126.118.188, 108.168.243.150
Connecting to dl.bintray.com|75.126.118.188|:443... connected.
HTTP request sent, awaiting response... 302
Location: https://akamai.bintray.com/97/976b1f4893fddf7404cfd758b3c4f4a8824b0cc73ba3d6c224d434eac92a064b?__gda__=exp=1473436581~hmac=40097ee25e4c89d511edbc85b88876932cae126a046f57b9ca8fe1a86525d649&response-content-disposition=attachment%3Bfilename%3D%22pivy-importer-0.9.1-all.jar%22&response-content-type=application%2Foctet-stream&requestInfo=U2FsdGVkX1_pGSnRSogFWs1rzKkDVqglZy09ajLVG-4bOSodkYsBy9CQcsdzILMmmImVodQXE68y9OX4hg9qdtVHjEc3YfrrpKbPC4Fr-vLhH7woFp_JiRj0HXU_igQQ1p78B8BimxuW01cGd8V6eTvRWL7srKgo3T4LDbSak4U [following]
--2016-09-09 08:44:21--  https://akamai.bintray.com/97/976b1f4893fddf7404cfd758b3c4f4a8824b0cc73ba3d6c224d434eac92a064b?__gda__=exp=1473436581~hmac=40097ee25e4c89d511edbc85b88876932cae126a046f57b9ca8fe1a86525d649&response-content-disposition=attachment%3Bfilename%3D%22pivy-importer-0.9.1-all.jar%22&response-content-type=application%2Foctet-stream&requestInfo=U2FsdGVkX1_pGSnRSogFWs1rzKkDVqglZy09ajLVG-4bOSodkYsBy9CQcsdzILMmmImVodQXE68y9OX4hg9qdtVHjEc3YfrrpKbPC4Fr-vLhH7woFp_JiRj0HXU_igQQ1p78B8BimxuW01cGd8V6eTvRWL7srKgo3T4LDbSak4U
Resolving akamai.bintray.com... 23.38.227.203
Connecting to akamai.bintray.com|23.38.227.203|:443... connected.
HTTP request sent, awaiting response... 200 OK
Length: 9916558 (9.5M) [application/octet-stream]
Saving to: 'pivy-importer-0.9.1-all.jar'

pivy-importer-0.9.1-all.jar                                                               100%[========================================================================================================================================================================================================================================>]   9.46M  14.6MB/s    in 0.6s

2016-09-09 08:44:23 (14.6 MB/s) - 'pivy-importer-0.9.1-all.jar' saved [9916558/9916558]

alabaster:0.7=alabaster:0.7.1
pytz:0a=pytz:2016.4
Babel:0.8=Babel:1.0
sphinx_rtd_theme:0.1=sphinx_rtd_theme:0.1.1
08:44:23.602 INFO  c.l.p.i.deps.DependencyDownloader - Pulling in virtualenv:15.0.1
08:44:24.236 INFO  c.l.p.i.deps.DependencyDownloader - Pulling in pip:7.1.2
08:44:24.375 INFO  c.l.p.i.deps.DependencyDownloader - Pulling in pytest:3.0.2
08:44:24.437 INFO  c.l.p.i.deps.DependencyDownloader - Pulling in virtualenv:1.10
08:44:24.505 INFO  c.l.p.i.deps.DependencyDownloader - Pulling in scripttest:1.3
08:44:24.514 INFO  c.l.p.i.deps.DependencyDownloader - Pulling in mock:2.0.0
08:44:24.803 INFO  c.l.p.i.deps.DependencyDownloader - Pulling in py:1.4.29
08:44:24.821 INFO  c.l.p.i.deps.DependencyDownloader - Pulling in colorama:0.3.7
08:44:24.844 INFO  c.l.p.i.deps.DependencyDownloader - Pulling in pbr:0.11.0
08:44:24.858 INFO  c.l.p.i.deps.DependencyDownloader - Pulling in six:1.9.0
08:44:24.867 INFO  c.l.p.i.deps.DependencyDownloader - Pulling in funcsigs:1.0.0
08:44:24.881 INFO  c.l.p.i.deps.DependencyDownloader - Pulling in Sphinx:1.4.6
08:44:25.446 INFO  c.l.p.i.deps.DependencyDownloader - Pulling in Jinja2:2.7
08:44:25.584 INFO  c.l.p.i.deps.DependencyDownloader - Pulling in Pygments:2.0
08:44:25.709 INFO  c.l.p.i.deps.DependencyDownloader - Pulling in Sphinx:1.3
08:44:25.845 INFO  c.l.p.i.deps.DependencyDownloader - Pulling in unittest2:1.1.0
08:44:25.863 INFO  c.l.p.i.deps.DependencyDownloader - Pulling in pip:8.1.2
08:44:25.930 INFO  c.l.p.i.deps.DependencyDownloader - Pulling in ordereddict:1.1
08:44:25.938 INFO  c.l.p.i.deps.DependencyDownloader - Pulling in six:1.5.0
08:44:25.947 INFO  c.l.p.i.deps.DependencyDownloader - Pulling in Jinja2:2.3
08:44:25.982 INFO  c.l.p.i.deps.DependencyDownloader - Pulling in docutils:0.11
08:44:26.059 INFO  c.l.p.i.deps.DependencyDownloader - Pulling in snowballstemmer:1.1.0
08:44:26.069 INFO  c.l.p.i.deps.DependencyDownloader - Pulling in Babel:1.3
08:44:26.240 INFO  c.l.p.i.deps.DependencyDownloader - Pulling in alabaster:0.7.1
08:44:26.248 INFO  c.l.p.i.deps.DependencyDownloader - Pulling in imagesize:0.7.1
08:44:26.255 INFO  c.l.p.i.deps.DependencyDownloader - Pulling in colorama:0.3.5
08:44:26.263 INFO  c.l.p.i.deps.DependencyDownloader - Pulling in nose:1.3.7
08:44:26.285 INFO  c.l.p.i.deps.DependencyDownloader - Pulling in simplejson:3.8.2
08:44:26.296 INFO  c.l.p.i.deps.DependencyDownloader - Pulling in SQLAlchemy:0.9.0
08:44:26.495 INFO  c.l.p.i.deps.DependencyDownloader - Pulling in Whoosh:2.0.0
08:44:26.539 INFO  c.l.p.i.deps.DependencyDownloader - Pulling in MarkupSafe:0.23
08:44:26.552 INFO  c.l.p.i.deps.DependencyDownloader - Pulling in Babel:1.0
08:44:26.692 INFO  c.l.p.i.deps.DependencyDownloader - Pulling in six:1.4.0
08:44:26.700 INFO  c.l.p.i.deps.DependencyDownloader - Pulling in sphinx_rtd_theme:0.1.1
08:44:26.708 INFO  c.l.p.i.deps.DependencyDownloader - Pulling in traceback2:1.4.0
08:44:26.722 INFO  c.l.p.i.deps.DependencyDownloader - Pulling in pretend:1.0.8
08:44:26.730 INFO  c.l.p.i.deps.DependencyDownloader - Pulling in pytz:2016.4
08:44:26.816 INFO  c.l.p.i.deps.DependencyDownloader - Pulling in pytz:2016.6.1
08:44:26.896 INFO  c.l.p.i.deps.DependencyDownloader - Pulling in Sphinx:1.1
08:44:27.007 INFO  c.l.p.i.deps.DependencyDownloader - Pulling in linecache2:1.0.0
08:44:27.015 INFO  c.l.p.i.deps.DependencyDownloader - Pulling in Pygments:1.2
08:44:27.064 INFO  c.l.p.i.deps.DependencyDownloader - Pulling in docutils:0.7
08:44:27.130 INFO  c.l.python.importer.ImporterCLI - Execution Finished!
```
