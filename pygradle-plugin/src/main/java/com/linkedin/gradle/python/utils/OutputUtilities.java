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

package com.linkedin.gradle.python.utils;

import org.apache.commons.lang.StringUtils;


public class OutputUtilities {

  public static final String GOOD_INSTALL_MESSAGE = "[GOOD]";
  public static final String SKIPPED_INSTALL_MESSAGE = "[SKIPPED]";

  private OutputUtilities() {

  }

  public static String writePaddedString(String beginningMessage, String endingMessage) {
    return writePaddedString(beginningMessage, endingMessage, 80);
  }

  public static String writePaddedString(String beginningMessage, String endingMessage, int length) {
    StringBuilder sb = new StringBuilder();
    int bufferSize = Math.max(5, length - beginningMessage.length() - endingMessage.length());
    sb.append(beginningMessage);
    sb.append(" ");
    sb.append(StringUtils.repeat(".", bufferSize));
    sb.append(" ");
    sb.append(endingMessage);
    return sb.toString();
  }
}
