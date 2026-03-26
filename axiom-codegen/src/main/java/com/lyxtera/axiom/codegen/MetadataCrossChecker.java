package com.lyxtera.axiom.codegen;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.lyxtera.axiom.api.model.RuleSetDescriptor;
import com.lyxtera.axiom.api.model.RuleSetDescriptor.BusinessActionDescriptor;
import com.lyxtera.axiom.api.model.RuleSetDescriptor.BusinessCheckDescriptor;
import com.lyxtera.axiom.engine.RuleSetLoader.YamlRuleSetLoader;

/**
 * Validates that rule metadata in YAML correctly matches the Java
 * implementations.
 */
public class MetadataCrossChecker {

    private final String basePackage;
    private final String outputDirectory;
    private final List<String> ruleSetPaths;

    private static final Pattern RULE_METADATA_PATTERN = Pattern
            .compile("@RuleMetadata\\s*\\([^)]*name\\s*=\\s*\"([^\"]+)\"");
    private static final Pattern ARG_PATTERN = Pattern.compile("@Arg\\s*\\(\\s*\"([^\"]+)\"\\s*\\)");

    public MetadataCrossChecker(String basePackage, String outputDirectory, List<String> ruleSetPaths) {
        this.basePackage = basePackage;
        this.outputDirectory = outputDirectory;
        this.ruleSetPaths = ruleSetPaths;
    }

    private static class CodeMetadata {
        String name;
        Set<String> args;

        CodeMetadata(String name, Set<String> args) {
            this.name = name;
            this.args = args;
        }
    }

    public void crossCheck() throws IOException, IllegalStateException {
        List<RuleSetDescriptor> descriptors = loadDescriptors();

        Map<String, CodeMetadata> codeChecks = parseJavaFiles("checks");
        Map<String, CodeMetadata> codeActions = parseJavaFiles("actions");

        Set<String> yamlCheckNames = new HashSet<>();
        Set<String> yamlActionNames = new HashSet<>();

        for (RuleSetDescriptor descriptor : descriptors) {
            for (BusinessCheckDescriptor check : descriptor.getBusinessChecks()) {
                yamlCheckNames.add(check.getName());
                verifyMatch(check.getName(), check.getParams(), codeChecks.get(check.getName()), "check");
            }
            for (BusinessActionDescriptor action : descriptor.getBusinessActions()) {
                yamlActionNames.add(action.getName());
                verifyMatch(action.getName(), action.getParams(), codeActions.get(action.getName()), "action");
            }
        }

        // Check for orphans in code
        for (String codeCheckName : codeChecks.keySet()) {
            if (!yamlCheckNames.contains(codeCheckName)) {
                throw new IllegalStateException("Metadata divergence: implementation for check '" + codeCheckName
                        + "' exists in code but is missing in ruleset.");
            }
        }
        for (String codeActionName : codeActions.keySet()) {
            if (!yamlActionNames.contains(codeActionName)) {
                throw new IllegalStateException("Metadata divergence: implementation for action '" + codeActionName
                        + "' exists in code but is missing in ruleset.");
            }
        }
    }

    private void verifyMatch(String name, List<String> yamlParams, CodeMetadata javaMetadata, String type) {
        if (javaMetadata == null) {
            throw new IllegalStateException("Metadata divergence: " + type + " '" + name
                    + "' exists in ruleset but missing implementation in code.");
        }
        Set<String> yamlArgs = new HashSet<>(yamlParams != null ? yamlParams : new ArrayList<>());
        if (!javaMetadata.args.equals(yamlArgs)) {
            throw new IllegalStateException(
                    "Metadata divergence: arguments for " + type + " '" + name + "' do not match. Code has: "
                            + javaMetadata.args + ", but ruleset has: " + yamlArgs);
        }
    }

    private List<RuleSetDescriptor> loadDescriptors() throws IOException {
        List<RuleSetDescriptor> descriptors = new ArrayList<>();
        for (String path : ruleSetPaths) {
            descriptors.add(new YamlRuleSetLoader<>(path).loadDescriptor());
        }
        return descriptors;
    }

    private Map<String, CodeMetadata> parseJavaFiles(String type) throws IOException {
        Map<String, CodeMetadata> result = new HashMap<>();
        Path dir = Paths.get(outputDirectory, basePackage.replace('.', File.separatorChar), type);
        List<Path> javaFiles = findJavaFiles(dir);

        for (Path file : javaFiles) {
            String content = Files.readString(file);
            Matcher matcher = RULE_METADATA_PATTERN.matcher(content);
            if (matcher.find()) {
                String name = matcher.group(1);
                Set<String> args = extractArgs(content);
                result.put(name, new CodeMetadata(name, args));
            }
        }
        return result;
    }

    private Set<String> extractArgs(String content) {
        Set<String> args = new HashSet<>();
        Matcher argMatcher = ARG_PATTERN.matcher(content);
        while (argMatcher.find()) {
            args.add(argMatcher.group(1));
        }
        return args;
    }

    private List<Path> findJavaFiles(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            return new ArrayList<>();
        }
        try (Stream<Path> walk = Files.walk(dir)) {
            return walk.filter(p -> !Files.isDirectory(p) && p.toString().endsWith(".java"))
                    .collect(Collectors.toList());
        }
    }
}
