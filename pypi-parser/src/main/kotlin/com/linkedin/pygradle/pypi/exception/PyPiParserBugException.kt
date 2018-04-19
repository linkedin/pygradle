package com.linkedin.pygradle.pypi.exception

import com.linkedin.pygradle.pypi.internal.http.PackageRelease
import com.linkedin.pygradle.pypi.model.PackageType.Companion.matchPackageType
import java.nio.file.Path

/**
 * A wrapper for internal errors that users shouldn't deal with
 */
open class PyPiParserBugException(message: String) : RuntimeException("Pivy Internal Error: $message") {
    /**
     * Error parsing wheel
     */
    class WheelParseException(packageName: String) : PyPiParserBugException("Unable to parse wheel for $packageName")
    /**
     * The artifact downloaded was not valid
     */
    class DownloadedArtifactWasNotValidException(f: Path, s: String)
        : PyPiParserBugException("$f doesn't match expected md5 ($s)")

    /**
     * Network request for a package failed
     */
    class RequestWasNotSuccessfulException(packageName: String) : PyPiParserBugException("Unable to request details for $packageName")

    /**
     * A package was downloaded, but we don't know how to support this package type
     */
    class UnsupportedDistributionTypeException internal constructor(s: PackageRelease) :
        RuntimeException("Unsupported Type: ${s.matchPackageType()}")

}
