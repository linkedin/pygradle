package com.linkedin.pygradle.pypi.internal

import com.linkedin.pygradle.pypi.exception.InternalBugException

internal fun MatchResult.extractField(name: String): String {
    return this.groups[name]?.value ?: throw InternalBugException("`$name` was missing, was required in regex")
}
