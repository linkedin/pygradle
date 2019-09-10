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
package com.linkedin.python.importer.util

import groovy.util.logging.Slf4j
import org.apache.http.HttpHost

@Slf4j
class ProxyDetector {

    private final static String HTTP_PROXY_HOST = "http.proxyHost"
    private final static String HTTP_PROXY_PORT = "http.proxyPort"

    static maybeGetHttpProxy() {
        int proxyPort = -1

        def proxyPortString = System.getProperty(HTTP_PROXY_PORT)
        def proxyHost = System.getProperty(HTTP_PROXY_HOST)

        def proxy = null
        if (null != proxyPortString && null != proxyHost) {
            log.debug("Detected {} = '{}' and {} = '{}' in the system properties.", HTTP_PROXY_HOST, proxyHost, HTTP_PROXY_PORT, proxyPortString)
            try {
                proxyPort = Integer.valueOf(proxyPortString)
                proxy = new HttpHost(proxyHost, proxyPort)
            } catch (NumberFormatException e) {
                // number could not be parsed as a number.
                log.warn("Unable to parse {} as a number. No proxy configured!", proxyPortString)
            }
        }
        return proxy
    }
}
