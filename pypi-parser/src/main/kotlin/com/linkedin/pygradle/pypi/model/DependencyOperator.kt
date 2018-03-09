package com.linkedin.pygradle.pypi.model

enum class DependencyOperator(val code: String, val description: String) {
    EQUAL("==", "eq"),
    LESS_THAN_EQUAL("<=", "le"),
    LESS_THAN("<", "lt"),
    GREATER_THAN_EQUAL(">=", "ge"),
    GREATER_THAN(">", "gt"),
    NOT_EQUAL("!=", "ne")
}
