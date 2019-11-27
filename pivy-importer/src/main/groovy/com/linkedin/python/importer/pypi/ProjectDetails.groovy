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

    ProjectDetails(Map<String, Object> details) {
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

        throw new RuntimeException("Unable to find ${name}@${version}")
    }

    String maybeFixVersion(String version) {
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

    boolean hasVersion(String version) {
        return releases.containsKey(version)
    }

    String getLatestVersion() {
        return latest
    }

    private List<String> getVersionInRange(VersionRange range, List<String> excluded, boolean allowPreReleases) {
        String start = range.startVersion ?: '0'
        String end = range.endVersion ?: '999999'
        def sortedReleases = releases.keySet().sort { a, b -> VersionRange.compareVersions(a, b) }
        def matchingRange = []

        for (String release : sortedReleases) {
            if (excluded.contains(release)) {
                continue
            }
            if (!allowPreReleases && release ==~ /^.*\d(a|alpha|b|beta|rc)\d*$/) {
                // skip alpha/beta/release candidate versions
                continue
            }
            if (VersionRange.compareVersions(release, start) > 0 ||
                    ((release == start || VersionRange.compareVersions(release, start) == 0) && range.includeStart)) {
                matchingRange.add(release)
            }
            if (VersionRange.compareVersions(release, end) > 0 ||
                    ((release == end || VersionRange.compareVersions(release, end) == 0) && !range.includeEnd)) {
                break
            }
        }

        if (matchingRange.isEmpty()) {
            throw new RuntimeException("Unable to find ${name} in range ${range.startVersion}..${range.endVersion}")
        }

        return matchingRange
    }

    String getHighestVersionInRange(VersionRange range, List<String> excluded, boolean allowPreReleases) {
        return getVersionInRange(range, excluded, allowPreReleases).last()
    }

    String getLowestVersionInRange(VersionRange range, List<String> excluded, boolean allowPreReleases) {
        return getVersionInRange(range, excluded, allowPreReleases).first()
    }
}
