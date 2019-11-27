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


class VersionRange {
    String startVersion
    boolean includeStart
    String endVersion
    boolean includeEnd

    VersionRange(String startVersion, boolean includeStart, String endVersion, boolean includeEnd) {
        this.startVersion = startVersion
        this.includeStart = includeStart
        this.endVersion = endVersion
        this.includeEnd = includeEnd
    }

    private static List<Integer> numericVersion(String release) {
        def v = []
        for (String s : release.split(/\./)) {
            if (s.isInteger()) {
                v.add(s as Integer)
            } else {
                def m = s =~ /(\d+)(.+?)(\d*)/
                if (m.matches()) {
                    v.add(m.group(1) as Integer)
                    /*
                     * Put negative marker for rc, alpha, beta, keeping the order for letters.
                     * The tilda (~) is the last ASCII letter.
                     */
                    v.add(m.group(2).toCharArray()[0] - '~'.toCharacter() - 1)
                    if (m.group(3).isInteger()) {
                        v.add(m.group(3) as Integer)
                    }
                }
            }
        }
        return v
    }

    static int compareVersions(String r1, String r2) {
        def v1 = numericVersion(r1)
        def v2 = numericVersion(r2)
        /*
         * A version with the same numbers as another is larger than an alpha, beta,
         * or release candidate.
         */
        def i = 0
        while (i < v1.size() && i < v2.size()) {
            if (v1[i] > v2[i]) {
                return 1
            } else if (v2[i] > v1[i]) {
                return -1
            }
            i++
        }
        if (v1.size() == v2.size()) {
            return 0
        }
        // Make sure 0.3 matches 0.3.0, etc.
        if (i < v1.size() && isOnlyZeroesRemain(v1, i) || i < v2.size() && isOnlyZeroesRemain(v2, i)) {
            return 0
        }
        if (i < v2.size()) {
            return (v2[i] < 0) ? 1 : -1
        }
        return (v1[i] < 0) ? -1 : 1
    }

    static boolean isOnlyZeroesRemain(List<Integer> v, int start) {
        for (int i = start; i < v.size(); i++) {
            if (v[i] != 0) {
                return false
            }
        }
        return true
    }

}
