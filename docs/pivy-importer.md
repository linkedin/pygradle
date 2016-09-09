# pivy-importer

The pivy-importer is a Java application that can be used to pull libraries from pypi into an Ivy format. It has
several features that you may find useful in dealing with pypi projects.
 
## General Usage

`java -jar pivy-importer.jar --repo /path/to/destination virtualenv:15.0.1 pip:7.1.2`

For a complete usage example please review the build.gradle file for the pivy-importer project.

## Replacement

In some cases it's useful to replace dependencies with another one to deal with how Pip does dependency resolution. 
To enable this feature you need to add the option `--replace` followed by a list of arguments in the form 
oldName:oldVersion=newName:newVersion (`alabaster:0.7=alabaster:0.7.1`). You can provide multiple 
replacements by joining them with a comma.
 
## Usage in Gradle
To be able to consume the artifacts produced by the pivy-importer, you'll have to define a [repository](https://docs.gradle.org/current/userguide/dependency_management.html#sec:repositories) in Gradle. We use a specific format for the repository that you'll have to define. Here is an example definition in Gradle. You would add this to your project's build.gradle.

```
repositories {
  ivy {
    name 'pypi-local'   //optional, but nice
    url "/path/to/repo'
    layout "pattern", {
      ivy "[organisation]/[module]/[revision]/[module]-[revision].ivy"
      artifact "[organisation]/[module]/[revision]/[artifact]-[revision](-[classifier]).[ext]"
      m2compatible = true
    }
  }
}
```
