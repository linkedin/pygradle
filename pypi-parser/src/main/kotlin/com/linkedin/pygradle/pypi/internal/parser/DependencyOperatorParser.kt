package com.linkedin.pygradle.pypi.internal.parser

import com.linkedin.pygradle.pypi.model.DependencyOperator

internal object DependencyOperatorParser {
    internal fun parseComparison(operator: String?): DependencyOperator? = when (operator?.trim()) {
        ">=" -> DependencyOperator.GREATER_THAN_EQUAL
        "<=" -> DependencyOperator.LESS_THAN_EQUAL
        ">" -> DependencyOperator.GREATER_THAN
        "<" -> DependencyOperator.LESS_THAN
        "==" -> DependencyOperator.EQUAL
        "!=" -> DependencyOperator.NOT_EQUAL
        null -> null
        else -> throw IllegalArgumentException("`$operator` is not an understood value")
    }
}
