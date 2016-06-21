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

import org.gradle.api.GradleException

import java.util.regex.Matcher


class MiscUtils {

    /**
     * Convert a collection of files into a set of names.
     *
     * @param files The set of files to add to the set.
     * @return A set of names.
     */
    protected static Set<String> configurationToSet(Collection<File> files) {
        Set<String> configNames = new HashSet<String>()
        for (File file : files) {
            def (String name, String version) = packageInfoFromPath(file.name)
            configNames.add(name)
        }
        return configNames
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
    public static Collection<String> packageInfoFromPath(String packagePath) {
        def extensionRegex = /\.tar\.gz|\.zip|\.tar|\.tar\.bz2|\.tgz/
        def nameVersionRegex = /^((.*)\/)*(?<name>[a-zA-Z0-9._\-]+)-(?<version>([0-9][0-9a-z\.]+(-.*)*))$/

        def packageName = packagePath.split(extensionRegex).first()

        if (new File(packagePath).isDirectory())
            return [packagePath.split(File.separator)[-1], null]

        if (packagePath == packageName)
            throw new GradleException("Cannot calculate Python package extension from ${packagePath} using regular expression /${extensionRegex}/.")

        Matcher matcher = packageName =~ nameVersionRegex
        if (matcher.matches()) {
            def name = matcher.group('name')
            def version = matcher.group('version')
            return [name, version]
        } else {
            throw new GradleException("Cannot calculate Python package name and version from ${packageName} using regular expression /${nameVersionRegex}/.")
        }
    }

    /**
     * Check if a dependency is in a collection of files.
     *
     * @param dependency The dependency to test.
     * @param files The set of files to test against.
     * @return True if dependency is in set of files.
     */
    protected static boolean inConfiguration(String dependency, Collection<File> files) {
        for (File file : files) {
            def (name, version) = packageInfoFromPath(file.name)
            if (dependency == name)
                return true
        }
        false
    }
}
