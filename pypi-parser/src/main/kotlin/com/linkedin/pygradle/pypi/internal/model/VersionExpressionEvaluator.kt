package com.linkedin.pygradle.pypi.internal.model

import com.linkedin.pygradle.pypi.model.DependencyOperator
import java.util.Objects

internal object VersionExpressionEvaluator {
    private val comparator = VersionComparator()

    fun isExpressionTrue(first: Any, operator: DependencyOperator, second: Any): Boolean {
        if (operator == DependencyOperator.EQUAL) {
            return Objects.equals(first, second)
        }

        if (operator == DependencyOperator.NOT_EQUAL) {
            return !Objects.equals(first, second)
        }

        val compareResult = comparator.compare(first, second)
        return when (operator) {
            DependencyOperator.EQUAL -> compareResult == 0
            DependencyOperator.LESS_THAN_EQUAL -> compareResult <= 0
            DependencyOperator.LESS_THAN -> compareResult < 0
            DependencyOperator.GREATER_THAN_EQUAL -> compareResult >= 0
            DependencyOperator.GREATER_THAN -> compareResult > 0
            DependencyOperator.NOT_EQUAL -> compareResult != 0
        }
    }
}
