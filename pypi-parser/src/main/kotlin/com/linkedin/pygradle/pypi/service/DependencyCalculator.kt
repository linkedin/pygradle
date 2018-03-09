package com.linkedin.pygradle.pypi.service

import com.linkedin.pygradle.pypi.model.ParseReport
import com.linkedin.pygradle.pypi.model.PythonPackageVersion

interface DependencyCalculator {
    fun calculateDependencies(packageDetails: PyPiPackageDetails, version: PythonPackageVersion): ParseReport
}
