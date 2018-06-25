package com.linkedin.python.importer.distribution

import com.linkedin.python.importer.deps.DependencySubstitution
import com.linkedin.python.importer.pypi.ProjectDetails
import com.linkedin.python.importer.pypi.PypiApiCache
import com.linkedin.python.importer.pypi.VersionRange
import groovy.util.logging.Slf4j
import org.apache.http.client.HttpResponseException

import java.util.zip.ZipFile

@Slf4j
abstract class PythonPackage {
    protected final File packageFile
    protected final PypiApiCache pypiApiCache
    protected final DependencySubstitution dependencySubstitution
    protected final boolean latestVersions
    protected final boolean allowPreReleases
    protected final boolean lenient

    PythonPackage(
        File packageFile,
        PypiApiCache pypiApiCache,
        DependencySubstitution dependencySubstitution,
        boolean latestVersions,
        boolean allowPreReleases,
        boolean lenient) {
            this.dependencySubstitution = dependencySubstitution
            this.pypiApiCache = pypiApiCache
            this.packageFile = packageFile
            this.latestVersions = latestVersions
            this.allowPreReleases = allowPreReleases
            this.lenient = lenient
    }

    abstract Map<String, List<String>> getDependencies()

    protected String explodeZipForTargetEntry(String entryName) {
        def file = new ZipFile(packageFile)
        def entry = file.getEntry(entryName)

        if (entry) {
            return file.getInputStream(entry).text
        }
        return ''
    }

    protected String getModuleName() {
        return packageFile.name.split('-')[0]
    }

    protected String getVersion() {
        return packageFile.name.split('-')[1]
    }

    protected String parseDependencyFromRequire(String require) {
        if (require.contains('[')) {
            require = require.replaceAll(/\[.*?]/, '')
        }
        if (require.contains(' ')) {
            require = require.replaceAll(' ', '')
        }

        /*
         * Newer versions of setuptools allow use of markers in
         * install_requires. Previously they were allowed only
         * in extras_require and appeared only in separate config
         * section in the metadata. We need to parse for markers
         * in the default section now too.
         */
        List<String> lineWithMarker = require.split(';')
        log.debug("Split line into lineWithMarker ({}) {}", require, lineWithMarker)

        List<String> conditions = lineWithMarker[0].split(',')
        log.debug("Split lineWithMarker[0] into conditions ({}) {}", lineWithMarker[0], conditions)

        String moduleName = conditions[0].split(/~=|!=|==|[><]=?/)[0]
        VersionRange range = new VersionRange('', false, '', false)
        List<String> excluded = []

        for (String condition : conditions) {
            List<String> parts = condition.split(/~=|!=|==|[><]=?/)
            log.debug("Split condition into parts ({}) {}", condition, parts)

            if (parts.size() > 1) {
                String v = parts[1]
                String operator = condition[parts[0].size()..-(parts[1].size() + 1)]

                switch (operator) {
                    case '>=':
                        range.includeStart = true
                /* FALL THROUGH */
                    case '>':
                        range.startVersion = v
                        break
                    case '<=':
                        range.includeEnd = true
                /* FALL THROUGH */
                    case '<':
                        range.endVersion = v
                        break
                    case '!=':
                        excluded.add(v)
                        break
                    case '==':
                        range.includeStart = true
                        range.startVersion = v
                        range.includeEnd = true
                        range.endVersion = v
                        break
                    // TODO: This fix is from chrisbrake:master https://github.com/linkedin/pygradle/pull/232/files
                    case '~=':
                        range.includeStart = true
                        range.startVersion = v
                        range.includeEnd = true
                        List<String> endVersion = v.tokenize('.').init()
                        endVersion.push('99999')
                        range.endVersion = endVersion.join('.')
                        log.debug("Expecting versions of {} between {} and {} to be compatible with {}",
                            parts[0], range.startVersion, range.endVersion, v)
                        break
                    default:
                        throw new RuntimeException("Unrecognizable package version condition ${condition}")
                }
            }
        }

        def projectDetails = pypiApiCache.getDetails(moduleName, lenient)

        // project name is illegal, which means we can't find any information about this project on PyPI
        if (projectDetails == null) {
            return null
        }

        String name = projectDetails.getName()
        String version
        if (range.startVersion == '' && range.endVersion == '') {
            // No specific version requested. Get the latest stable.
            if (allowPreReleases) {
                version = projectDetails.getLatestVersion()
            } else {
                range.endVersion = projectDetails.getLatestVersion()
                version = projectDetails.getHighestVersionInRange(range, excluded, allowPreReleases)
            }
        } else if (latestVersions) {
            version = projectDetails.getHighestVersionInRange(range, excluded, allowPreReleases)
        } else {
            version = projectDetails.getLowestVersionInRange(range, excluded, allowPreReleases)
        }

        (name, version) = dependencySubstitution.maybeReplace(name + ':' + version).split(':')
        version = projectDetails.maybeFixVersion(version)

        return name + ':' + version
    }
}
