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
package com.linkedin.gradle.python.util.zipapp;

import java.io.IOException;
import java.io.Serializable;

/**
 * Used to get a template to use for the entry points
 */
public interface EntryPointTemplateProvider extends Serializable {

    /**
     * Returns a template to be used for an entry point. The template must be renderable by
     * {@link groovy.text.SimpleTemplateEngine}.
     *
     * @param options to use to pick the template
     * @return A template to be used when building entry points.
     */
    String retrieveTemplate(TemplateProviderOptions options, boolean isPythonWrapper) throws IOException;
}
