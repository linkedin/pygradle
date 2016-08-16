/*
 * Copyright 2016 LinkedIn Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.linkedin.gradle.build.version

import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.Person
import org.ajoberstar.grgit.operation.OpenOp
import org.gradle.api.DefaultTask
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.*

class VersionBumpTask extends DefaultTask {

    public static final Logger logger = Logging.getLogger(VerificationTask)

    @InputFile
    File versionFile

    @Input
    Version currentVersion

    VersionBumpTask() {
        group = 'Version'
        description = 'Bump the version to the next version, message can contain [no bump], [ci skip], [bump minor], [bump major]'
    }

    @TaskAction
    public void bumpVersion() {
        def repo = Grgit.open(project.getProjectDir())
        def message = repo.head().fullMessage

        if (message.contains('[no bump]') || message.contains('[ci skip]') || currentVersion.isSnapshot()) {
            logger.lifecycle("Skipping bump...")
            throw new StopActionException('No bump should happen');
        }

        logger.info("Using message: {}", message)

        Version nextVersion;
        if (message.contains('[bump minor]')) {
            nextVersion = currentVersion.withNextMinor()
        } else if (message.contains('[bump major]')) {
            nextVersion = currentVersion.withNextMajor()
        } else {
            nextVersion = currentVersion.withNextPatch()
        }

        VersionFile.writeVersionToFile(versionFile, nextVersion)
        repo.add(patterns: ['version.properties'])
        repo.commit(
            message: "Bumping version to ${nextVersion.toString()}\n\n[ci skip]",
            author: new Person('circleci', 'ci@pygradle.linkedin.com'))

        repo.tag.add(name: "v${nextVersion.toString()}")

        repo.push()
        repo.push(tags: true)
        getLogger().lifecycle("Next version is: {}", nextVersion.toString())
    }

}
