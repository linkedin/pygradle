package com.linkedin.pivy.args

import com.beust.jcommander.IStringConverter
import com.linkedin.pivy.VersionParseException

internal class VersionForceConverter : IStringConverter<Map<String, String>> {
    override fun convert(value: String?): Map<String, String> {
        value ?: return emptyMap()

        return value.split(",").map {
            if (!it.contains("=")) {
                throw VersionParseException("$it does not follow the format <name>=<version>")
            }
            val split = it.split("=")
            split[0] to split.last()
        }.toMap()
    }
}
