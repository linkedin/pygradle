package com.linkedin.python.importer.deps

import com.linkedin.python.importer.distribution.SourceDistPackage
import com.linkedin.python.importer.ivy.IvyFileWriter
import com.linkedin.python.importer.pypi.ProjectDetails
import com.linkedin.python.importer.pypi.PypiApiCache
import groovy.util.logging.Slf4j
import org.apache.http.client.fluent.Request

import java.nio.file.Paths

@Slf4j
class DependencyDownloader {


    Queue<String> dependencies = [] as Queue
    HashSet<String> processedDependencies = [] as Set
    PypiApiCache cache = new PypiApiCache()
    File ivyRepoRoot
    DependencySubstitution dependencySubstitution

    DependencyDownloader(String project, File ivyRepoRoot, DependencySubstitution dependencySubstitution) {
        this.dependencySubstitution = dependencySubstitution
        this.ivyRepoRoot = ivyRepoRoot
        dependencies.add(project)
    }

    def download() {
        while (!dependencies.isEmpty()) {
            def dep = dependencies.poll()
            if(dep in processedDependencies) {
                continue
            }
            downloadDependency(dep)
            processedDependencies.add(dep)
        }
    }

    def downloadDependency(String dep) {
        log.info("Pulling in $dep")
        def (name, version) = dep.split(":")

        def projectDetails = cache.getDetails(name)
        version = projectDetails.maybeFixVersion(version)
        def sdistDetails = projectDetails.findVersion(version).find { it.packageType == 'sdist'}

        def destDir = new File(ivyRepoRoot, "pypi/${name}/${version}")

        destDir.mkdirs()

        def artifact = downloadArtifact(destDir, sdistDetails.url)
        def packageDependencies = new SourceDistPackage(artifact, cache, dependencySubstitution).dependencies

        new IvyFileWriter(name, version, [sdistDetails], packageDependencies).writeIvyFile(destDir)

        packageDependencies.each { key, value ->
            dependencies.addAll(value)
        }
    }

    static File downloadArtifact(File destDir, String url) {

        def filename = Paths.get(url).getFileName().toString()
        def contents = new File(destDir, filename)

        if(!contents.exists()) {
            Request.Get(url)
                    .connectTimeout(5000)
                    .socketTimeout(5000)
                    .execute().saveContent(contents)
        }

        return contents
    }
}
