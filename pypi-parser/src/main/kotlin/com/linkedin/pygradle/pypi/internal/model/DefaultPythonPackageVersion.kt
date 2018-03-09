package com.linkedin.pygradle.pypi.internal.model

import com.linkedin.pygradle.pypi.model.PythonPackageVersion
import org.apache.commons.lang3.builder.ToStringBuilder
import org.apache.commons.lang3.builder.ToStringStyle

class DefaultPythonPackageVersion(private val version: String) : PythonPackageVersion {

    internal val epoch: String?
    internal val release: String
    internal val pre: String?
    internal val post: String?
    internal val dev: String?
    internal val local: String?
    internal val wildcard: Boolean

    init {
        val tempVersion = if (version.endsWith(".*")) {
            wildcard = true
            version.substring(0, version.length - 2)
        } else {
            wildcard = false
            version
        }

        if (!isSupportedVersion(tempVersion)) throw RuntimeException("Version $version($tempVersion) doesn't match PEP-440")
        val result = regex.find(tempVersion)!!
        epoch = result.groups["epoch"]?.value
        release = result.groups["release"]?.value!!
        pre = normalizeToTrailingZero(result.groups["pre"]?.value)
        post = result.groups["post"]?.value
        dev = result.groups["dev"]?.value
        local = result.groups["local"]?.value
    }

    companion object {
        private const val pep440Pattern = "((?<epoch>\\d+)!)?" +
            "(?<release>(\\d+)([.r]\\d+)*(\\.\\+)?)" +
            "(?<pre>(a|b|rc|alpha|beta|c|pre|preview)\\d*)?" +
            "(\\.post(?<post>\\d+))?" +
            "(\\.dev(?<dev>\\d+))?" +
            "(\\+(?<local>[a-zA-Z0-9]([.a-zA-Z0-9])*[a-zA-Z0-9]?))?"
        val regex = Regex(pep440Pattern)

        fun isSupportedVersion(string: String): Boolean = regex.matches(string)

        private fun normalizeToTrailingZero(value: String?): String? {
            val input = value ?: return null
            return if (!input.last().isDigit()) {
                return input + "0"
            } else {
                input
            }
        }
    }

    override fun toVersionString(): String = version

    override fun getEpoch(): String? = epoch

    override fun getRelease(): String = release

    override fun getPost(): String? = post

    override fun getDev(): String? = dev

    override fun getLocal(): String? = local

    override fun isWildcardVersion(): Boolean = wildcard

    override fun getGroups(): List<String?> = listOf(epoch, release, pre, post, dev, local)

    override fun toString(): String {
        return ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("epoch", epoch)
            .append("release", release)
            .append("pre", pre)
            .append("post", post)
            .append("dev", dev)
            .append("local", local)
            .toString()
    }
}
