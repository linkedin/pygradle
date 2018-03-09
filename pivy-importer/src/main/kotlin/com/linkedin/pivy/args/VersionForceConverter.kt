package com.linkedin.pivy.args

import com.beust.jcommander.IStringConverter

class VersionForceConverter : IStringConverter<Map<String, String>> {
    override fun convert(value: String?): Map<String, String> {
        value ?: return emptyMap()

        return value.split(",").map {
            if (!it.contains("=")) {
                throw RuntimeException("$it does not follow the format <name>=<version>")
            }
            val split = it.split("=")
            split[0] to split.last()
        }.toMap()
    }
}
