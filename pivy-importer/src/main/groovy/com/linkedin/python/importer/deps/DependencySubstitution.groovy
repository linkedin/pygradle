package com.linkedin.python.importer.deps

class DependencySubstitution {

    final Map<String, String> replacementMap

    DependencySubstitution(Map<String, String> replacementMap) {
        this.replacementMap = replacementMap
        println replacementMap
    }

    String maybeReplace(String dependency) {
        if(replacementMap.containsKey(dependency)) {
            return replacementMap.get(dependency)
        } else {
            return dependency
        }
    }
}
