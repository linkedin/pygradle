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

import org.gradle.api.GradleException;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.artifacts.result.DependencyResult;
import org.gradle.api.artifacts.result.ResolvedComponentResult;
import org.gradle.api.artifacts.result.ResolvedDependencyResult;
import org.gradle.api.file.FileCollection;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * A container of static utilities for dependency order.
 */
public class DependencyOrder {

    private static final Logger logger = Logging.getLogger(DependencyOrder.class);

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
     * @param root         the root of the dependency (sub)tree
     * @param seen         the input set of already seen dependencies
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
        Set<File> configurationFiles = configuration.getFiles();

        // Create an id:file mapping.
        Set<ResolvedArtifact> artifacts = configuration.getResolvedConfiguration().getResolvedArtifacts();
        for (ResolvedArtifact artifact : artifacts) {
            ModuleVersionIdentifier id = artifact.getModuleVersion().getId();
            File file = artifact.getFile();
            idToFileMap.put(id, file);
        }

        // Prepare an ordered set of files in post-order.
        for (ResolvedComponentResult d : configurationPostOrderDependencies(configuration)) {
            if (!idToFileMap.containsKey(d.getModuleVersion())) {
                logger.warn("***** WARNING Missing resolved artifact for " + d);
            } else {
                files.add(idToFileMap.get(d.getModuleVersion()));
            }
        }

        // Make sure the files set is a subset of configuration files.
        if (!configurationFiles.containsAll(files)) {
            throw new GradleException("Could not find matching dependencies for all configuration files");
        }

        /*
         * Our rest.li generated packages will extend configuration and
         * will not appear in the resolved results. We are going to add
         * them at the end in the same order they were added to
         * configuration files.
         */
        if (files.size() != configurationFiles.size()) {
            files.addAll(difference(configurationFiles, files));
        }

        return files;
    }

    /**
     * Returns a set of configuration files in the tree post-order,
     * or sorted, or in the original insert order.
     *
     * Attempt to collect dependency tree post-order and fall back to
     * sorted or insert order.
     * If sorted is false, it will always return the original insert order.
     */
    public static Collection<File> getConfigurationFiles(FileCollection files, boolean sorted) {
        if (sorted && (files instanceof Configuration)) {
            try {
                return DependencyOrder.configurationPostOrderFiles((Configuration) files);
            } catch (Throwable e) {
                // Log and fall back to old style installation order as before.
                logger.lifecycle("***** WARNING: ${ e.message } *****");
            }
        }
        return sorted
            ? files.getFiles().stream().sorted().collect(Collectors.toSet())
            : files.getFiles();
    }

    /**
     * Returns a set of configuration files in the tree post-order or sorted.
     *
     * Attempt to collect dependency tree post-order and fall back to
     * sorted order.
     */
    public static Collection<File> getConfigurationFiles(FileCollection files) {
        return getConfigurationFiles(files, true);
    }

    /*
     * Return a set difference between the larger and smaller sets.
     */
    private static Set<File> difference(Set<File> larger, Set<File> smaller) {
        Set<File> diff = new LinkedHashSet<>();

        for (File file : larger) {
            if (!smaller.contains(file)) {
                diff.add(file);
            }
        }

        return diff;
    }
}
