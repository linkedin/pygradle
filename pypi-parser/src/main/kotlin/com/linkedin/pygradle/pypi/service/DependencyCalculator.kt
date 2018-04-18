package com.linkedin.pygradle.pypi.service

import com.linkedin.pygradle.pypi.model.ParseReport
import com.linkedin.pygradle.pypi.model.PythonPackageVersion

/**
 * Instances will generate a report for a package
 */
interface DependencyCalculator {
    /**
     * Resolve the direct graph for the package
     */
    fun calculateDependencies(packageDetails: PyPiPackageDetails, version: PythonPackageVersion): ParseReport
}
