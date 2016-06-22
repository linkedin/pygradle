package com.linkedin.python.importer.ivy

import com.linkedin.python.importer.deps.DependencySubstitution
import com.linkedin.python.importer.pypi.VersionEntry
import groovy.transform.TupleConstructor
import groovy.xml.MarkupBuilder
import org.apache.commons.io.FilenameUtils

@TupleConstructor
class IvyFileWriter {

    final String name
    final String version
    final List<VersionEntry> archives
    final Map<String, List<String>> dependencies
    final DependencySubstitution dependencySubstitution

    def writeIvyFile(File destDir) {

        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)
        xml.setDoubleQuotes(true)

        def pub = archives.collect { artifact ->
            def ext = artifact.filename.contains(".tar.") ? artifact.filename.find('tar\\..*') : FilenameUtils.getExtension(artifact.filename)
            String filename = artifact.filename - ("." + ext);

            def source = 'sdist'.equals(artifact.packageType)

            def map = [name: name, ext: ext, conf: source ? 'source':'default', type: ext == 'whl' ? 'zip' : ext]

            if(filename.indexOf(version) + version.length() + 1 < filename.length()) {
                def classifier = filename.substring(filename.indexOf(version) + version.length() + 1)
                map['m:classifier'] = classifier
            }

            return map
        }

        if(!(pub.any { it.conf == 'default'})) {
            pub.first().conf = 'default'
        }

        xml.'ivy-module'(version: "2.0", 'xmlns:e': "http://ant.apache.org/ivy/extra", 'xmlns:m': "http://ant.apache.org/ivy/maven") {
            info(organisation:"pypi", module: name, revision: version)
            configurations {
                def configurations = new HashSet<>(dependencies.keySet())
                configurations.add("source")
                configurations.each {
                    def map = [name: it, description: 'auto generated configuration for ' + it]
                    if(!'default'.equals(it)) {
                        map['extends'] = 'default'
                    }
                    conf(map)
                }
            }
            publications {
                pub.each { archive ->
                    artifact(archive)
                }
            }
            dependencies(defaultconfmapping: "*->default") {
                dependencies.each { config, deps ->
                    deps.each { dep ->
                        def (name, version) = dep.split(':')
                        dependency(org: 'pypi', name: name, rev: version, conf: config)
                    }
                }
            }
        }

        def ivyText = writer.toString()

        new File(destDir, "${name}-${version}.ivy").text = ivyText
    }
}
