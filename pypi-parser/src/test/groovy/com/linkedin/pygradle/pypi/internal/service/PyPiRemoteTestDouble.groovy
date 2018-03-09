package com.linkedin.pygradle.pypi.internal.service

import com.linkedin.pygradle.pypi.service.PyPiPackageDetails
import com.linkedin.pygradle.pypi.service.PyPiRemote
import org.jetbrains.annotations.NotNull

class PyPiRemoteTestDouble implements PyPiRemote {

    final Map<String, Map<String, String>> specificVersionMap

    PyPiRemoteTestDouble(Map<String, Map<String, String>> specificVersionMap) {
        this.specificVersionMap = specificVersionMap
    }

    @Override
    PyPiPackageDetails resolvePackage(@NotNull String name) {
        if (specificVersionMap.containsKey(name)) {
            def entry = specificVersionMap.get(name)
            return ModelUtil.createPackageDetails(entry["name"], entry["version"])
        }
        return ModelUtil.createPackageDetails(name, "0.0.1")
    }
}
