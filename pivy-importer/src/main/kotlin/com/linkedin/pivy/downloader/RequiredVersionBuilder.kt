package com.linkedin.pivy.downloader

import com.linkedin.pivy.ImporterOptions

internal object RequiredVersionBuilder {

    /**
     * Given CLI args, get the required versions for the run.
     */
    fun buildRequiredVersion(args: ImporterOptions): RequiredVersionContainer {

        val requiredVersionContainer = RequiredVersionContainer()

        args.versionRequirements.forEach {
            requiredVersionContainer.register(it.key, it.value)
        }

        return requiredVersionContainer
    }
}
