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

package com.linkedin.gradle.python.packaging;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.gradle.api.GradleException;


public class PythonPackageNamingUtil {
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
  public static PackageInformation packageInfoFromPath(String packagePath) {
    String extensionRegex = ".tar\\.gz|\\.zip|\\.tar|\\.tar\\.bz2|\\.tgz";
    String nameVersionRegex = "^((.*)/)*(?<name>[a-zA-Z0-9._\\-]+)-(?<version>([0-9][0-9a-z\\.]+(-.*)*))$";

    String[] split = packagePath.split(extensionRegex);
    if (split.length == 0) {
      throw new IllegalArgumentException("packagePath must have one of the approved types in it");
    }
    final String packageName = split[0];

    if (new File(packagePath).isDirectory()) {
      String[] fileSeperatorSplit = packagePath.split(File.separator);
      return new PackageInformation(fileSeperatorSplit[fileSeperatorSplit.length - 1], null);
    }

    if (packagePath.equals(packageName)) {
      throw new GradleException(
          "Cannot calculate Python package extension from " + packagePath + " using regular expression /"
              + extensionRegex + "/.");
    }

    Matcher matcher = Pattern.compile(nameVersionRegex).matcher(packageName);
    if (matcher.matches()) {
      String name = matcher.group("name");
      String version = matcher.group("version");
      return new PackageInformation(name, version);
    } else {
      throw new GradleException(
          "Cannot calculate Python package name and version from " + packageName + " using regular expression /" + nameVersionRegex + "/.");
    }
  }
}
