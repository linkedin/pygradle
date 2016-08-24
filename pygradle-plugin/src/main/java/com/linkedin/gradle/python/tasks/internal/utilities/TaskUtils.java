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

package com.linkedin.gradle.python.tasks.internal.utilities;

import com.linkedin.gradle.python.internal.platform.PythonVersion;

import java.io.File;
import java.util.List;

public class TaskUtils {

    public static String join(List<String> args, String separator) {
        if (args == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.size(); i++) {
            sb.append(args.get(i));
            if (i != args.size() - 1) {
                sb.append(separator);
            }
        }
        return sb.toString();
    }

    public static File sitePackage(File vendDir, PythonVersion version) {
        String sitePackagePath = String.format("lib/python%s/site-packages", version.getMajorMinorVersion());
        return new File(vendDir, sitePackagePath);
    }
}
