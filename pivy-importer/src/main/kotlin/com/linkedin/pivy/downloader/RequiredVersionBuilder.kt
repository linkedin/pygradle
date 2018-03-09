package com.linkedin.pivy.downloader

import com.linkedin.pivy.ImporterOptions

object RequiredVersionBuilder {

    fun buildRequiredVersion(args: ImporterOptions): RequiredVersionContainer {

        val requiredVersionContainer = RequiredVersionContainer()

        args.versionRequirements.forEach {
            requiredVersionContainer.register(it.key, it.value)
        }

        return requiredVersionContainer
    }
}
