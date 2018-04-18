package com.linkedin.pygradle.pypi.exception

import com.linkedin.pygradle.pypi.internal.http.PackageRelease
import com.linkedin.pygradle.pypi.model.PackageType.Companion.matchPackageType

class UnsupportedDistributionTypeException internal constructor(s: PackageRelease) :
    RuntimeException("Unsupported Type: ${s.matchPackageType()}")
