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
package com.linkedin.gradle.python.util

import com.linkedin.gradle.python.wheel.PythonWheelDetails
import org.apache.commons.io.FilenameUtils
import org.gradle.api.GradleException

import java.nio.file.Path
import java.util.regex.Matcher


class PackageInfo {

    final File packageFile
    final String name
    final String version

    private PackageInfo(File packageFile, String name, String version) {
        this.packageFile = packageFile
        this.name = name
        this.version = version
    }

    @Deprecated
    static PackageInfo fromPath(String packagePath) {
        return fromPath(new File(packagePath))
    }

    /**
     * Derives a Python package's name and version from its path.
     * <p>
     * This method only recognizes packages with the following extensions. A
     * Python package that doesn't have one of the following exceptions will
     * raise an exception.
     * <ul>
     *   <li>.tar.gz</li>
     *   <li>.tar.bz2</li>
     *   <li>.tar</li>
     *   <li>.tgz</li>
     *   <li>.zip</li>
     *   <li>.whl</li>
     * </ul>
     * <p>
     * A path to a expanded Python package can be provided as long as the path
     * to the expanded Python package refers to the directory that contains the
     * Python package.
     * <p>
     * This method only recognizes Python packages that follow the convention
     * <pre>(name)-(version)[-(extra)].(extension)</pre>. The <pre>extra</pre>
     * field may include something like <pre>-SNAPSHOT</pre> or
     * <pre>-linkedin1</pre>. A Python package that doesn't follow this
     * convention will raise an exception.
     * <p>
     * @param packagePath The path to a Python package.
     */
    static PackageInfo fromPath(File packagePath) {
        def extensionRegex = /\.tar\.gz|\.zip|\.tar|\.tar\.bz2|\.tgz|\.whl/
        def nameVersionRegex = /^(?<name>[a-zA-Z0-9._\-]+)-(?<version>([0-9][0-9a-z\.]+(-.*)*))$/

        def filename = FilenameUtils.getName(packagePath.getPath())
        def packageName = filename.split(extensionRegex).first()

        if (packagePath.isDirectory()) {
            return new PackageInfo(packagePath, filename, null)
        }

        if (packagePath.getName() == packageName) {
            throw new GradleException("Cannot calculate Python package extension from ${ packagePath } using regular expression /${ extensionRegex }/.")
        }

        Optional<PythonWheelDetails> pythonWheels = PythonWheelDetails.fromFile(packagePath)
        if (pythonWheels.isPresent()) {
            return new PackageInfo(packagePath, pythonWheels.get().getDist(), pythonWheels.get().getVersion())
        }

        Matcher matcher = packageName =~ nameVersionRegex
        if (matcher.matches()) {
            def name = matcher.group('name')
            def version = matcher.group('version')
            return new PackageInfo(packagePath, name, version)
        } else {
            throw new GradleException("Cannot calculate Python package name and version from ${ packageName } using regular expression /${ nameVersionRegex }/.")
        }
    }

    static PackageInfo fromPath(Path packagePath) {
        return fromPath(packagePath.toFile())
    }

    @Override
    String toString() {
        return name + "-" + version + "@" + packageFile
    }

    String toShortHand() {
        return version ? "${name}-${version}" : name
    }

    /**
     * Makes PackageInfo from other object with new name and version.
     *
     * <p>Used for testing.</p>
     *
     * @param packageInfo other PackageInfo object
     * @param name new name, usually null for testing
     * @param version new version, usually null for testing
     * @return test PackageInfo object
     */
    static PackageInfo fakeFromOther(PackageInfo packageInfo, String name, String version) {
        return new PackageInfo(packageInfo.packageFile, name, version)
    }
}
