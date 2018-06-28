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
    Map<String, List<String>> getDependencies() {
        Map<String, List<String>> dependenciesMap

        try {
            dependenciesMap = parseRuntimeRequiresFromMetadataJson(runtimeRequiresFromMetadataJson)
        } catch(Exception e) {
            log.debug("Failed to parse Json Metadata for package ${packageFile.name}: ${e.message} " +
                "Parsing METADATA text file instead.")
            dependenciesMap = parseDistRequiresFromMetadataText(metadataText)
        }

        return dependenciesMap
    }

    private Map<String, List<String>> parseRuntimeRequiresFromMetadataJson(Map<String, List<String>> requires) {
        def dependenciesMap = [:]

        requires.each { key, value ->
            String config = key
            List<String> requiresList = value

            if (!dependenciesMap.containsKey(config)) {
                dependenciesMap[config] = []
            }

            log.debug("The requires of configuration $key: ${requiresList.toString()}")
            for (String require : requiresList) {
                require = require.replaceAll(/[()]/, "")
                String dependency = parseDependencyFromRequire(require)
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
    private Map<String, List<String>> getRuntimeRequiresFromMetadataJson() {
        def jsonMetadata = getJsonMetadata()

        if (jsonMetadata == null) {
            throw new RuntimeException("There is no metadata Json file in package ${packageFile.name}.")
        }

        Map<String, List<String>> runtimeRequiresMap = [:]

        def run_requires = jsonMetadata["run_requires"]
        def meta_requires = jsonMetadata["meta_requires"]

        addRuntimeRequiresFromRequiresMap(runtimeRequiresMap, run_requires)
        addRuntimeRequiresFromRequiresMap(runtimeRequiresMap, meta_requires)

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
    private static void addRuntimeRequiresFromRequiresMap(Map<String, List<String>> runtimeRequiresMap, def requires) {
        for (def require_map : requires) {
            String config = require_map["extra"] ?: "default"

            if (runtimeRequiresMap[config] == null) {
                runtimeRequiresMap[config] = []
            }
            runtimeRequiresMap[config].addAll((List) require_map["requires"])
        }
    }

    private Map<String, List<String>> parseDistRequiresFromMetadataText(String requires) {
        def dependenciesMap = [:]
        log.debug("Runtime requires of package ${packageFile.name} from Metadata text: {}", requires)

        def config = 'default'
        dependenciesMap[config] = []
        requires.eachLine { line ->
            if (line.startsWith("Requires-Dist:")) {
                line = line.replaceAll(/[()]/, "").substring("Requires-Dist:".length())

                // there is package named extras, see https://pypi.org/project/extras/
                config = line.substring(line.lastIndexOf("extra") + "extra".length())
                    .replaceAll(/['=\s]/, "")

                if (!dependenciesMap.containsKey(config)) {
                    dependenciesMap[config] = []
                }

                String dependency = parseDependencyFromRequire(line)
                if (dependency != null) {
                    dependenciesMap[config] << dependency
                }
            }
        }

        return dependenciesMap
    }

    private String getMetadataText() {
        String metadataTextEntry = moduleName + '-' + version + ".dist-info/METADATA"
        return explodeZipForTargetEntry(metadataTextEntry)
    }
}
