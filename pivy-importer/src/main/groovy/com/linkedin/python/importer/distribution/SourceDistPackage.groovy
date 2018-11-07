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

import groovy.transform.InheritConstructors
import groovy.util.logging.Slf4j
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream

@Slf4j @InheritConstructors
class SourceDistPackage extends PythonPackage {
    @Override
    Map<String, List<String>> getDependencies(boolean latestVersions,
                                              boolean allowPreReleases,
                                              boolean fetchExtras,
                                              boolean lenient) {

        return parseRequiresText(getRequiresTextFile(), latestVersions, allowPreReleases, fetchExtras, lenient)
    }

    @SuppressWarnings("ParameterReassignment")
    private Map<String, List<String>> parseRequiresText(String requires,
                                                        boolean latestVersions,
                                                        boolean allowPreReleases,
                                                        boolean fetchExtras,
                                                        boolean lenient) {
        def dependenciesMap = [:]
        log.debug("requires: {}", requires)
        def config = 'default'
        dependenciesMap[config] = []
        boolean inExtra = false
        requires.eachLine { line ->
            if (line.isEmpty()) {
                return
            }
            def configMatcher = line =~ /^\[(.*?)]$/
            if (configMatcher.matches()) {
                config = configMatcher.group(1).split(":")[0]
                if (config.isEmpty()) {
                    config = 'default'
                    inExtra = false
                } else {
                    inExtra = true
                }
                log.debug("New config {}", config)
                if (inExtra && !fetchExtras) {
                    return
                }
                if (!dependenciesMap.containsKey(config)) {
                    dependenciesMap[config] = []
                }
            } else {
                if (inExtra && !fetchExtras) {
                    return
                }
                String dependency = parseDependencyFromRequire(line, latestVersions, allowPreReleases, lenient)
                if (dependency != null) {
                    dependenciesMap[config] << dependency
                }
            }
            // make IDE happy by having all execution paths in the closure return
            return
        }

        return dependenciesMap
    }

    protected String getRequiresTextFile() {
        if (packageFile.absolutePath.contains('.tar.') || packageFile.absolutePath.endsWith('.tgz')) {
            return explodeTarForRequiresText()
        } else {
            return explodeZipForRequiresText()
        }
    }

    private String explodeZipForRequiresText() {
        String requiresTextEntry = moduleName + '.egg-info/requires.txt'
        return explodeZipForTargetEntry(requiresTextEntry)
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

        if (packageFile.absolutePath.endsWith('.gz') || packageFile.absolutePath.endsWith('.tgz')) {
            compressorInputStream = new GzipCompressorInputStream(inputStream)
        } else if (packageFile.absolutePath.endsWith('.bz2')) {
            compressorInputStream = new BZip2CompressorInputStream(inputStream)
        } else {
            compressorInputStream = inputStream
        }
        return new TarArchiveInputStream(compressorInputStream)
    }
}
