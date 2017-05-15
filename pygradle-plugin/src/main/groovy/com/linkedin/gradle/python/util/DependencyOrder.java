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

package com.linkedin.gradle.python.util;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.gradle.api.GradleException;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.artifacts.result.DependencyResult;
import org.gradle.api.artifacts.result.ResolvedComponentResult;
import org.gradle.api.artifacts.result.ResolvedDependencyResult;


/**
 * A container of static utilities for dependency order.
 */
public class DependencyOrder {
    private DependencyOrder() {
        // prevent instantiation of the utility class
    }

    /**
     * Traverses the dependency tree post-order and collects dependencies.
     * <p>
     * The recursive post-order traversal returns the root of the (sub)tree.
     * This allows the post-order adding into the set of dependencies as we
     * return from the recursive calls. The set of seen dependencies ensures
     * ensures that the cycles in Ivy metadata do not cause cycles in our
     * recursive calls.
     *
     * @param root the root of the dependency (sub)tree
     * @param seen the input set of already seen dependencies
     * @param dependencies the output set of dependencies in post-order
     * @return the root itself
     */
    public static ResolvedComponentResult postOrderDependencies(
            ResolvedComponentResult root,
            Set<ComponentIdentifier> seen,
            Set<ResolvedComponentResult> dependencies) {
        for (DependencyResult d : root.getDependencies()) {
            if (!(d instanceof ResolvedDependencyResult)) {
                throw new GradleException("Unresolved dependency found for " + d.toString());
            }

            ResolvedComponentResult component = ((ResolvedDependencyResult) d).getSelected();
            ComponentIdentifier id = component.getId();

            if (!seen.contains(id)) {
                seen.add(id);
                dependencies.add(postOrderDependencies(component, seen, dependencies));
            }
        }

        return root;
    }

    /**
     * Collects configuration dependencies in post-order.
     *
     * @param configuration Gradle configuration to work with
     * @return set of resolved dependencies in post-order
     */
    public static Set<ResolvedComponentResult> configurationPostOrderDependencies(Configuration configuration) {
        ResolvedComponentResult root = configuration.getIncoming().getResolutionResult().getRoot();
        Set<ResolvedComponentResult> dependencies = new LinkedHashSet<>();
        Set<ComponentIdentifier> seen = new HashSet<>();

        postOrderDependencies(root, seen, dependencies);

        return dependencies;
    }

    /**
     * Collects configuration files in post-order.
     *
     * @param configuration Gradle configuration to work with
     * @return set of files corresponding to post-order of dependency tree
     */
    public static Collection<File> configurationPostOrderFiles(Configuration configuration) {
        Map<ModuleVersionIdentifier, File> idToFileMap = new HashMap<>();
        Set<File> files = new LinkedHashSet<>();

        // Create an id:file mapping.
        Set<ResolvedArtifact> artifacts = configuration.getResolvedConfiguration().getResolvedArtifacts();
        for (ResolvedArtifact artifact : artifacts) {
            ModuleVersionIdentifier id = artifact.getModuleVersion().getId();
            File file = artifact.getFile();
            idToFileMap.put(id, file);
        }

        // Prepare an ordered set of files in post-order.
        for (ResolvedComponentResult d : configurationPostOrderDependencies(configuration)) {
            files.add(idToFileMap.get(d.getModuleVersion()));
        }

        // Check that the new set is the same size as the configuration file collection.
        if (files.size() != configuration.getFiles().size()) {
            throw new GradleException("Could not find matching dependencies for all configuration files");
        }

        return files;
    }
}
