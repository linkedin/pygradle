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

import groovy.json.JsonSlurper
import groovy.transform.InheritConstructors
import groovy.util.logging.Slf4j

@Slf4j @InheritConstructors
class WheelsPackage extends PythonPackage {
    /**
     * Get all the dependencies from package metadata. Json metadata is preferred since Version 2.0 of metadata
     * is migrated to JSON representation. See details at https://legacy.python.org/dev/peps/pep-0426/#abstract.
     * @return a Map whose key is configuration and value is dependencies List
     */
    @Override
    Map<String, List<String>> getDependencies(boolean latestVersions,
                                              boolean allowPreReleases,
                                              boolean fetchExtras,
                                              boolean lenient) {
        Map<String, List<String>> dependenciesMap

        try {
            dependenciesMap = parseRuntimeRequiresFromMetadataJson(
                runtimeRequiresFromMetadataJson, latestVersions, allowPreReleases, fetchExtras, lenient)
        } catch(Exception e) {
            log.debug("Failed to parse Json Metadata for package ${packageFile.name}: ${e.message} " +
                "Parsing METADATA text file instead.")
            dependenciesMap = parseDistRequiresFromMetadataText(
                metadataText, latestVersions, allowPreReleases, fetchExtras, lenient)
        }

        return dependenciesMap
    }

    private Map<String, List<String>> parseRuntimeRequiresFromMetadataJson(
        Map<String, List<String>> requires,
        boolean latestVersions,
        boolean allowPreReleases,
        boolean fetchExtras,
        boolean lenient) {

        def dependenciesMap = [:]

        requires.each { key, value ->
            String config = key
            List<String> requiresList = value

            if ('default' != config && !fetchExtras) {
                return
            }

            if (!dependenciesMap.containsKey(config)) {
                dependenciesMap[config] = []
            }

            log.debug("The requires of configuration $key: ${requiresList.toString()}")
            for (String require : requiresList) {
                require = require.replaceAll(/[()]/, "")
                String dependency = parseDependencyFromRequire(require, latestVersions, allowPreReleases, lenient)
                if (dependency != null) {
                    dependenciesMap[config] << dependency
                }
            }
        }

        log.debug("The dependencies of package ${packageFile.name}: ${dependenciesMap.toString()}")
        return dependenciesMap
    }

    /**
     * Get a Map of runtime requires which includes run_requires and meta_requires dependencies from Json metadata.
     * The kep is configuration (extra), and value is runtime requires List.
     * @param jsonMetadata
     * @return
     */
    protected Map<String, List<String>> getRuntimeRequiresFromMetadataJson() {
        def jsonMetadata = getJsonMetadata()

        if (jsonMetadata == null) {
            throw new RuntimeException("There is no metadata Json file in package ${packageFile.name}.")
        }

        Map<String, List<String>> runtimeRequiresMap = [:]
        runtimeRequiresMap['default'] = []

        def runRequires = jsonMetadata["run_requires"]
        def metaRequires = jsonMetadata["meta_requires"]

        addRuntimeRequiresFromRequiresMap(runtimeRequiresMap, runRequires)
        addRuntimeRequiresFromRequiresMap(runtimeRequiresMap, metaRequires)

        log.debug("The runtime requires of package ${packageFile.name}: ${runtimeRequiresMap.toString()}")
        return runtimeRequiresMap
    }

    /**
     * Get package metadata in Json format from the package.
     * @return Json package metadata, otherwise empty String if not found Json metadata
     */
    private getJsonMetadata() {
        String jsonMetadataEntry = moduleName + '-' + version + ".dist-info/metadata.json"
        String metadata = explodeZipForTargetEntry(jsonMetadataEntry)
        if (metadata == "") {
            return null
        }

        def jsonSlurper = new JsonSlurper()
        def jsonMetadata = jsonSlurper.parseText(metadata)
        log.debug("Json metadata of package ${packageFile.name}: $jsonMetadata")

        return jsonMetadata
    }

    /**
     * Add all the runtime requires dependencies from requires List.
     * @param runtimeRequires
     * @param requires
     */
    private static void addRuntimeRequiresFromRequiresMap(Map<String, List<String>> runtimeRequiresMap,
                                                          def requires) {
        for (def require_map : requires) {
            String config = require_map["extra"] ?: "default"

            if (runtimeRequiresMap[config] == null) {
                runtimeRequiresMap[config] = []
            }
            runtimeRequiresMap[config].addAll((List) require_map["requires"])
        }
    }

    private Map<String, List<String>> parseDistRequiresFromMetadataText(String requires,
                                                                        boolean latestVersions,
                                                                        boolean allowPreReleases,
                                                                        boolean fetchExtras,
                                                                        boolean lenient) {
        def dependenciesMap = [:]
        log.debug("Runtime requires of package ${packageFile.name} from Metadata text: {}", requires)

        def config = 'default'
        dependenciesMap[config] = []
        requires.eachLine { line ->
            if (line.startsWith("Requires-Dist:")) {
                String newLine = line.replaceAll(/[()]/, "").substring("Requires-Dist:".length())

                // there is package named extras, see https://pypi.org/project/extras/
                int indexOfLastExtra = newLine.lastIndexOf("extra")
                if (indexOfLastExtra != -1) {
                    if (!fetchExtras) {
                        return
                    }
                    config = newLine.substring(indexOfLastExtra + "extra".length())
                        .replaceAll(/['=\s]/, "")
                }

                if (!dependenciesMap.containsKey(config)) {
                    dependenciesMap[config] = []
                }

                String dependency = parseDependencyFromRequire(newLine, latestVersions, allowPreReleases, lenient)
                if (dependency != null) {
                    dependenciesMap[config] << dependency
                }
            }
            // make IDE happy by having all execution paths in the closure return
            return
        }

        return dependenciesMap
    }

    protected String getMetadataText() {
        String metadataTextEntry = moduleName + '-' + version + ".dist-info/METADATA"
        return explodeZipForTargetEntry(metadataTextEntry)
    }
}
