package com.linkedin.pygradle.pypi.internal.service

import com.linkedin.pygradle.pypi.internal.http.PackageDetails
import com.linkedin.pygradle.pypi.internal.http.PackageInfo
import com.linkedin.pygradle.pypi.internal.http.PackageRelease


class ModelUtil {

    static DefaultPyPiPackageDetails createPackageDetails(String name, String version) {
        new DefaultPyPiPackageDetails(new PackageDetails(new PackageInfo(name),
            [
                (version): [
                    new PackageRelease("2016-07-26T00:44:47", version, "foo.var", "sdist", '')
                ]
            ]
        ))
    }
}
