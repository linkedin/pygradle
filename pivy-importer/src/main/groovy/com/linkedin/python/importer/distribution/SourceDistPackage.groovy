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
package com.linkedin.python.importer.distribution

import com.linkedin.python.importer.deps.DependencySubstitution
import com.linkedin.python.importer.pypi.PypiApiCache
import groovy.util.logging.Slf4j
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream

import java.util.zip.ZipFile

@Slf4j
class SourceDistPackage {

    private final File packageFile
    private final PypiApiCache pypiApiCache
    private final DependencySubstitution dependencySubstitution

    public SourceDistPackage(File packageFile, PypiApiCache pypiApiCache, DependencySubstitution dependencySubstitution) {
        this.dependencySubstitution = dependencySubstitution
        this.pypiApiCache = pypiApiCache
        this.packageFile = packageFile
    }

    public Map<String, List<String>> getDependencies() {
        return parseRequiresText(getRequiresTextFile())
    }

    @SuppressWarnings("ParameterReassignment")
    private Map<String, List<String>> parseRequiresText(String requires) {
        def dependencies = [:]
        log.debug("requires: {}", requires)
        def configuration = 'default'
        dependencies[configuration] = []
        requires.eachLine { line ->
            if (line.isEmpty()) {
                return
            }
            def configMatcher = line =~ '^\\[(.*?)\\]$'
            if (configMatcher.matches()) {
                configuration = configMatcher.group(1).split(":")[0]
                if (configuration.isEmpty()) {
                    configuration = 'default'
                }
                log.debug("New config {}", configuration)
                if (!dependencies.containsKey(configuration)) {
                    dependencies[configuration] = []
                }
            } else {

                if (line.contains('[')) {
                    line = line.replaceAll('\\[.*?\\]', '')
                }
                if (line.contains(' ')) {
                    line = line.replaceAll(' ', '')
                }

                def split = line.split('[=<>]=?')
                log.debug("Split({}) {}", line, split)

                def projectDetails = pypiApiCache.getDetails(split[0])
                def version = split.length == 1 ? projectDetails.getLatestVersion() : split[1].split(',')[0]
                def name = projectDetails.getName()
                (name, version) = dependencySubstitution.maybeReplace(name + ':' + version).split(':')

                version = projectDetails.maybeFixVersion(version)
                dependencies[configuration] << name + ':' + version
            }
        }

        return dependencies
    }

    private String getRequiresTextFile() {
        if (packageFile.absolutePath.contains('.tar.')) {
            return explodeTarForRequiresText()
        } else {
            return explodeZipForRequiresText()
        }
    }

    private String explodeZipForRequiresText() {
        def file = new ZipFile(packageFile)
        def entry = file.getEntry('.egg-info/requires.txt')
        if (entry) {
            return file.getInputStream(entry).text
        }
        return ''
    }


    private String explodeTarForRequiresText() {
        TarArchiveInputStream tarIn = explodeArtifact()

        tarIn.getNextEntry()
        while (tarIn.getCurrentEntry() != null && !tarIn.getCurrentEntry().name.endsWith('.egg-info/requires.txt')) {
            tarIn.getNextEntry()
        }

        if (tarIn.getCurrentEntry() == null) {
            return ''
        }

        def byteArray = new byte[tarIn.getCurrentEntry().getSize()]
        tarIn.read(byteArray)

        return new String(byteArray)
    }

    private TarArchiveInputStream explodeArtifact() {
        FileInputStream fin = new FileInputStream(packageFile)
        BufferedInputStream inputStream = new BufferedInputStream(fin)
        InputStream compressorInputStream

        if (packageFile.absolutePath.endsWith('.gz')) {
            compressorInputStream = new GzipCompressorInputStream(inputStream)
        } else if (packageFile.absolutePath.endsWith('.bz2')) {
            compressorInputStream = new BZip2CompressorInputStream(inputStream)
        } else {
            compressorInputStream = inputStream
        }
        return new TarArchiveInputStream(compressorInputStream)
    }

}
