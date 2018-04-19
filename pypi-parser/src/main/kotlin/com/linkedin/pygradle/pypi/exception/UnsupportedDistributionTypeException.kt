package com.linkedin.pygradle.pypi.exception

import com.linkedin.pygradle.pypi.internal.http.PackageRelease
import com.linkedin.pygradle.pypi.model.PackageType.Companion.matchPackageType

/**
 * A package was downloaded, but we don't know how to support this package type
 */
class UnsupportedDistributionTypeException internal constructor(s: PackageRelease) :
    RuntimeException("Unsupported Type: ${s.matchPackageType()}")
