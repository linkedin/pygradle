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
        if(releases.containsKey(version)) {
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
