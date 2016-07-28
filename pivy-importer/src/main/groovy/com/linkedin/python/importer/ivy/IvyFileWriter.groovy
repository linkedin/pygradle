/*
 * Copyright 2016 LinkedIn Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.linkedin.python.importer.ivy

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

    @SuppressWarnings("SpaceAroundClosureArrow")
    def writeIvyFile(File destDir) {

        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)
        xml.setDoubleQuotes(true)

        def pub = archives.collect { artifact ->
            def ext = artifact.filename.contains(".tar.") ? artifact.filename.find('tar\\..*') : FilenameUtils.getExtension(artifact.filename)
            String filename = artifact.filename - ("." + ext)

            def source = 'sdist' == artifact.packageType

            def map = [name: name, ext: ext, conf: source ? 'source' : 'default', type: ext == 'whl' ? 'zip' : ext]

            if (filename.indexOf(version) + version.length() + 1 < filename.length()) {
                def classifier = filename.substring(filename.indexOf(version) + version.length() + 1)
                map['m:classifier'] = classifier
            }

            return map
        }

        if (!(pub.any { it.conf == 'default' })) {
            pub.first().conf = 'default'
        }

        xml.'ivy-module'(version: "2.0", 'xmlns:e': "http://ant.apache.org/ivy/extra", 'xmlns:m': "http://ant.apache.org/ivy/maven") {
            info(organisation: "pypi", module: name, revision: version)
            configurations {
                def configurations = new HashSet<>(dependencies.keySet())
                configurations.add("source")
                configurations.each {
                    def map = [name: it, description: 'auto generated configuration for ' + it]
                    if ('default' != it) {
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
