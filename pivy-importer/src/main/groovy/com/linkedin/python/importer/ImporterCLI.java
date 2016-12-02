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
package com.linkedin.python.importer;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.linkedin.python.importer.deps.DependencyDownloader;
import com.linkedin.python.importer.deps.DependencySubstitution;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class ImporterCLI {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ImporterCLI.class);

    private ImporterCLI() {
        //private constructor for "util" class
    }

    public static void main(String[] args) throws Exception {

        Options options = createOptions();

        CommandLineParser parser = new DefaultParser();
        CommandLine line = parser.parse(options, args);

        if (line.hasOption("quite")) {
            Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
            root.setLevel(Level.WARN);
        }

        if (!line.hasOption("repo")) {
            throw new RuntimeException("Unable to continue, no repository location given on the command line (use the --repo switch)");
        }
        final File repoPath = new File(line.getOptionValue("repo"));
        final DependencySubstitution replacements = new DependencySubstitution(buildSubstitutionMap(line));

        repoPath.mkdirs();

        if (!repoPath.exists() || !repoPath.isDirectory()) {
            throw new RuntimeException("Unable to continue, " + repoPath.getAbsolutePath() + " does not exist, or is not a directory");
        }


        for (String dependency : line.getArgList()) {
            new DependencyDownloader(dependency, repoPath, replacements).download();
        }

        logger.info("Execution Finished!");
    }

    private static Options createOptions() {
        Option repo = Option.builder()
            .longOpt("repo")
            .numberOfArgs(1)
            .argName("file")
            .desc("location of the ivy repo")
            .build();

        Option replacement = Option.builder()
            .longOpt("replace")
            .numberOfArgs(Option.UNLIMITED_VALUES)
            .argName("replacement")
            .desc("Noted like orig:version=replace:version")
            .valueSeparator(',')
            .build();

        Option quite = Option.builder()
            .longOpt("quite")
            .numberOfArgs(0)
            .desc("Sets logging level to WARN")
            .build();

        Options options = new Options();
        options.addOption(replacement);
        options.addOption(repo);
        options.addOption(quite);
        return options;
    }

    public static Map<String, String> buildSubstitutionMap(CommandLine line) {
        Map<String, String> sub = new LinkedHashMap<>();
        if (line.hasOption("replace")) {
            for (String it : Arrays.asList(line.getOptionValues("replace"))) {
                System.out.println(it);
                String[] split = it.split("=");
                sub.put(split[0], split[1]);
            }
        }
        return sub;

    }

}
