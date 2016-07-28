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
package com.linkedin.python.importer.pypi


class ProjectDetails {

    final String name
    final Map<String, List<VersionEntry>> releases = [:]
    final String latest

    public ProjectDetails(Map<String, Object> details) {
        name = details.info.name
        latest = details.info.version

        details.releases.each { version, entry ->
            releases[version] = entry.collect { it -> new VersionEntry(it.url, it.packagetype, it.filename) }
        }
    }

    List<VersionEntry> findVersion(String version) {
        if (releases.containsKey(version)) {
            return releases[version]
        }

        throw new RuntimeException("Unable to find $name@$version")
    }

    public String maybeFixVersion(String version) {
        if (hasVersion(version)) {
            return version
        }

        if (hasVersion(version + '.0')) {
            return version + '.0'
        }

        if (hasVersion(version + '.0.0')) {
            return version + '.0.0'
        }

        throw new RuntimeException("Unable to find version $version for $name")
    }

    public boolean hasVersion(String version) {
        return releases.containsKey(version)
    }

    public String getLatestVersion() {
        return latest
    }
}
