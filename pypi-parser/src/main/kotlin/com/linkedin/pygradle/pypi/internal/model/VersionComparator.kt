package com.linkedin.pygradle.pypi.internal.model

import com.linkedin.pygradle.pypi.exception.VersionNotSupportedException
import com.linkedin.pygradle.pypi.model.PythonPackageVersion

//PEP 440 compatible - https://www.python.org/dev/peps/pep-0440/#definitions
internal class VersionComparator : Comparator<Any> {
    override fun compare(o1: Any?, o2: Any?): Int {
        if (o1 == null && o2 == null) return 0
        else if (o1 == null) return 1
        else if (o2 == null) return -1

        val v1 = o1.toPythonVersion()
        val v2 = o2.toPythonVersion()

        val (v1Groups, v2Groups) = zeroPadAsRequired(v1, v2)
        for (i in (0 until v1Groups.size)) {
            val value1 = v1Groups[i].orEmpty()
            val value2 = v2Groups[i].orEmpty()
            val trimmedValue1 = value1.replace(Regex("(?<realVersion>.*?)(.0)+")) { it -> it.groups["realVersion"]!!.value }
            val trimmedValue2 = value2.replace(Regex("(?<realVersion>.*?)(.0)+")) { it -> it.groups["realVersion"]!!.value }

            if (value1 == value2) {
                continue
            }

            if (value1 == "" && !v1.isWildcardVersion()) {
                return 1
            }

            if (value2 == "" && !v2.isWildcardVersion()) {
                return -1
            }

            if (!v1.isWildcardVersion() && !v2.isWildcardVersion()) {
                //drop through
            } else if (v1.isWildcardVersion() && v2.isWildcardVersion()) {
                if (value1.startsWith(trimmedValue2)) {
                    return 0
                } else if (value2.startsWith(trimmedValue1)) {
                    return 0
                }
            } else if (v1.isWildcardVersion()) {
                if (value2.startsWith(trimmedValue1)) {
                    return 0
                }
            } else {
                if (value1.startsWith(trimmedValue2)) {
                    return 0
                }
            }

            return value1.compareTo(value2)

        }
        return 0
    }

    private fun zeroPadAsRequired(v1: PythonPackageVersion, v2: PythonPackageVersion): Pair<List<String?>, List<String?>> {
        val group1 = v1.getGroups().toMutableList()
        val group2 = v2.getGroups().toMutableList()

        for (i in (0 until group1.size)) {
            val part1 = group1[i]
            val part2 = group2[i]

            if (part1 != null && part2 != null) {
                val split1 = part1.split(".")
                val split2 = part2.split(".")

                if (split1.size < split2.size) {
                    group1[i] = part1 + "." + (0 until split2.size - split1.size).map { "0" }.joinToString(".")
                } else if (split1.size > split2.size) {
                    group2[i] = part2 + "." + (0 until split1.size - split2.size).map { "0" }.joinToString(".")
                }
            }
        }

        return Pair(group1, group2)
    }

    private fun Any.toPythonVersion(): PythonPackageVersion {
        return when (this) {
            is PythonPackageVersion -> return this
            is String -> DefaultPythonPackageVersion(this)
            else -> throw VersionNotSupportedException("${this.javaClass.name} is not either String or PythonPackageVersion")
        }
    }
}
