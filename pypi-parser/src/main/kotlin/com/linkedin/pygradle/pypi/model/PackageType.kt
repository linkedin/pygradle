package com.linkedin.pygradle.pypi.model

import com.linkedin.pygradle.pypi.internal.http.PackageRelease

enum class PackageType(val text: String) {
    BDIST_WHEEL("bdist_wheel"),
    BDIST_WININST("bdist_wininst"),
    BDIST_EGG("bdist_egg"),
    S_DIST("sdist"),
    ;


    companion object {
        fun parse(text: String): PackageType {
            return when(text) {
                BDIST_WHEEL.text -> BDIST_WHEEL
                S_DIST.text -> S_DIST
                BDIST_WININST.text -> BDIST_WININST
                BDIST_EGG.text -> BDIST_EGG
                else -> throw RuntimeException("Unable to find match for `$text`")
            }
        }

        fun PackageRelease.matchPackageType(): PackageType {
            return parse(this.packageType)
        }
    }
}
