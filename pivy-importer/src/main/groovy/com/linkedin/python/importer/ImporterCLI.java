package com.linkedin.python.importer;

import com.linkedin.python.importer.deps.DependencyDownloader;
import com.linkedin.python.importer.deps.DependencySubstitution;
import groovy.lang.Closure;
import org.apache.commons.cli.*;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.StringGroovyMethods;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class ImporterCLI {
    public static void main(String[] args) throws Exception {

        Options options = createOptions();

        CommandLineParser parser = new DefaultParser();
        CommandLine line = parser.parse(options, args);

        final File repoPath = new File(line.getOptionValue("repo"));
        final DependencySubstitution replacements = new DependencySubstitution(buildSubstitutionMap(line));

        repoPath.mkdirs();

        if (!repoPath.exists() || !repoPath.isDirectory()) {
            throw new RuntimeException("Unable to continue, " + repoPath.getAbsolutePath() + " does not exist, or is not a directory");
        }


        for (String dependency : line.getArgList()) {
            new DependencyDownloader(dependency, repoPath, replacements).download();
        }
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

        Options options = new Options();
        options.addOption(replacement);
        options.addOption(repo);
        return options;
    }

    public static Map<String, String> buildSubstitutionMap(CommandLine line) {
        Map<String, String> sub = new LinkedHashMap<>();
        if (line.hasOption("replace")) {
            Arrays.asList(line.getOptionValues("replace")).forEach(it -> {
                System.out.println(it);
                String[] split = it.split("=");
                sub.put(split[0], split[1]);
            });
        }
        return sub;

    }

}
