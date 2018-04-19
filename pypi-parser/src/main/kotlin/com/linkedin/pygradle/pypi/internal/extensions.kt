package com.linkedin.pygradle.pypi.internal

import com.linkedin.pygradle.pypi.exception.PyPiParserBugException

internal fun MatchResult.extractField(name: String): String {
    return this.groups[name]?.value ?: throw PyPiParserBugException("`$name` was missing, was required in regex")
}
