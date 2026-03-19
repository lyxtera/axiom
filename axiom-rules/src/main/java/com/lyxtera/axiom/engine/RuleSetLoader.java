package com.lyxtera.axiom.engine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lyxtera.axiom.api.exception.RuleLoadException;
import com.lyxtera.axiom.api.model.BusinessRule;
import com.lyxtera.axiom.api.model.RuleSetDescriptor;
import com.lyxtera.axiom.api.parser.Parser;

/**
 * Minimal service for loading rule sets from YAML files.
 *
 * @param <K> the type of the context key
 */
public abstract class RuleSetLoader<K extends Enum<K>> {

    private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory())
        .registerModule(new JavaTimeModule())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /**
     * Load the rule set descriptor.
     * Implementations should provide logic to load from their specific source.
     */
    public abstract RuleSetDescriptor loadDescriptor();
    
    /**
     * Creates a rule set from a YAML rule set.
     *
     * @param descriptor the YAML rule set
     * @return the rule set
     */
    public RuleSet<K> loadRuleSet(Parser<K> parser) {
        RuleSetDescriptor ruleSetDescriptor = loadDescriptor();
        RuleSet<K> ruleSet = new RuleSet<>();
        
        // Populate metadata for the rule set
        RuleSet.Metadata metadata = new RuleSet.Metadata();
        metadata.setRuleSetName(ruleSetDescriptor.getRulesetName());
        metadata.setRuleSetDescription(ruleSetDescriptor.getRulesetDescription());
        metadata.setBusinessCheckDescriptors(ruleSetDescriptor.getBusinessChecks());
        metadata.setBusinessActionDescriptors(ruleSetDescriptor.getBusinessActions());
        metadata.setAllowDynamicExecution(ruleSetDescriptor.isAllowDynamicExecution());
        metadata.setEntityPermissions(ruleSetDescriptor.getEntityPermissions());

        ruleSet.setMetadata(metadata);
        
        // Add rules
        for (var descriptor : ruleSetDescriptor.getRules()) {
            try {
                // Parse the rule expression using the AxiomEngine
                BusinessRule<K> rule = parser.parseRule(metadata, descriptor.getName(), descriptor.getExpression());
                ruleSet.addRule(rule, descriptor.getPriority(), descriptor.getEffectiveFrom(), descriptor.getEffectiveTo());
            } catch (Exception e) {
                e.printStackTrace();

                throw RuleLoadException.ruleParseError(
                    descriptor.getName(), descriptor.getExpression(), e);
            }
        }
        
        return ruleSet;
    }

    public static class YamlRuleSetLoader<K extends Enum<K>> extends RuleSetLoader<K> {

        private final String resourcePath;
        
        public YamlRuleSetLoader(String resourcePath) {
            this.resourcePath = resourcePath;
        }

        /**
         * Loads a YAML rule set descriptor from a classpath resource or file path.
         * First tries to load as a file, then falls back to classpath resource.
         *
         * @return the rule set descriptor
         */
        @Override
        public RuleSetDescriptor loadDescriptor() {
            return resolveSource(resourcePath, null).readDescriptor();
        }

        @Override
        public RuleSet<K> loadRuleSet(Parser<K> parser) {
            return loadRuleSet(parser, resolveSource(resourcePath, null), new LinkedHashSet<>());
        }

        private RuleSet<K> loadRuleSet(Parser<K> parser, SourceHandle source, LinkedHashSet<String> loadingStack) {
            if (!loadingStack.add(source.id())) {
                throw RuleLoadException.validationErrors(
                    "Cyclic ruleset forwarding detected: " + String.join(" -> ", loadingStack) + " -> " + source.id());
            }

            try {
                RuleSetDescriptor ruleSetDescriptor = source.readDescriptor();
                RuleSet<K> ruleSet = new RuleSet<>();

                RuleSet.Metadata metadata = new RuleSet.Metadata();
                metadata.setRuleSetName(ruleSetDescriptor.getRulesetName());
                metadata.setRuleSetDescription(ruleSetDescriptor.getRulesetDescription());
                metadata.setBusinessCheckDescriptors(ruleSetDescriptor.getBusinessChecks());
                metadata.setBusinessActionDescriptors(ruleSetDescriptor.getBusinessActions());
                metadata.setAllowDynamicExecution(ruleSetDescriptor.isAllowDynamicExecution());
                metadata.setEntityPermissions(ruleSetDescriptor.getEntityPermissions());
                ruleSet.setMetadata(metadata);

                for (var descriptor : ruleSetDescriptor.getRules()) {
                    try {
                        BusinessRule<K> parsedRule = parser.parseRule(metadata, descriptor.getName(), descriptor.getExpression());
                        BusinessRule<K> rule = validateAndCreateRule(descriptor, parsedRule);

                        if (rule.isGateRule()) {
                            String forwardRef = rule.getOnMatchForwardTo().orElseThrow();
                            SourceHandle childSource = resolveSource(forwardRef, source);
                            rule.setChildRuleSet(loadRuleSet(parser, childSource, loadingStack));
                        }

                        ruleSet.addRule(rule, descriptor.getPriority(), descriptor.getEffectiveFrom(), descriptor.getEffectiveTo());
                    } catch (RuleLoadException e) {
                        throw e;
                    } catch (Exception e) {
                        throw RuleLoadException.ruleParseError(
                            descriptor.getName(), descriptor.getExpression(), e);
                    }
                }

                ruleSet.validate();
                return ruleSet;
            } finally {
                loadingStack.remove(source.id());
            }
        }

        private BusinessRule<K> validateAndCreateRule(
                RuleSetDescriptor.RuleDescriptor descriptor,
                BusinessRule<K> parsedRule) {
            boolean hasActions = !parsedRule.getActions().isEmpty();
            String forwardRef = descriptor.getOnMatchForwardTo();
            boolean hasForwardField = forwardRef != null;
            boolean hasForwardRef = hasForwardField && !forwardRef.trim().isEmpty();

            if (hasActions) {
                if (hasForwardField) {
                    throw RuleLoadException.validationErrors(
                        String.format("Rule '%s' cannot define both actions and onMatchForwardTo", descriptor.getName()));
                }
                return parsedRule;
            }

            if (!hasForwardField || !hasForwardRef) {
                throw RuleLoadException.validationErrors(
                    String.format("Gate rule '%s' must define a non-empty onMatchForwardTo reference", descriptor.getName()));
            }

            return new BusinessRule<>(
                parsedRule.getName(),
                parsedRule.getCondition(),
                parsedRule.getActions(),
                forwardRef.trim());
        }

        private SourceHandle resolveSource(String sourceRef, SourceHandle parent) {
            String normalizedRef = sourceRef == null ? null : sourceRef.trim();
            if (normalizedRef == null || normalizedRef.isEmpty()) {
                throw RuleLoadException.loadError(String.valueOf(sourceRef), "Ruleset reference cannot be blank");
            }

            if (parent != null) {
                if (parent.classpathResource() != null) {
                    String classpathCandidate = normalizedRef.startsWith("/")
                        ? normalizedRef.substring(1)
                        : joinClasspathPath(parent.classpathParent(), normalizedRef);
                    SourceHandle handle = tryClasspath(classpathCandidate);
                    if (handle != null) {
                        return handle;
                    }
                }

                if (parent.filePath() != null) {
                    Path parentDirectory = parent.filePath().getParent();
                    Path fileCandidate = normalizedRef.startsWith("/")
                        ? Paths.get(normalizedRef)
                        : (parentDirectory == null ? Paths.get(normalizedRef) : parentDirectory.resolve(normalizedRef)).normalize();
                    SourceHandle handle = tryFile(fileCandidate);
                    if (handle != null) {
                        return handle;
                    }
                }
            }

            SourceHandle classpathHandle = tryClasspath(normalizedRef.startsWith("/") ? normalizedRef.substring(1) : normalizedRef);
            if (classpathHandle != null) {
                return classpathHandle;
            }

            SourceHandle fileHandle = tryFile(Paths.get(normalizedRef));
            if (fileHandle != null) {
                return fileHandle;
            }

            throw RuleLoadException.loadError(normalizedRef, "Resource not found as file or in classpath");
        }

        private SourceHandle tryClasspath(String classpathResource) {
            if (classpathResource == null || classpathResource.isBlank()) {
                return null;
            }
            if (getClass().getClassLoader().getResource(classpathResource) == null) {
                return null;
            }
            return new SourceHandle("classpath:" + classpathResource, classpathResource, null);
        }

        private SourceHandle tryFile(Path filePath) {
            if (filePath == null) {
                return null;
            }

            File file = filePath.toFile();
            if (!file.exists() || !file.isFile()) {
                return null;
            }

            return new SourceHandle("file:" + file.toPath().toAbsolutePath().normalize(), null, file.toPath().toAbsolutePath().normalize());
        }

        private String joinClasspathPath(String parentPath, String childPath) {
            if (parentPath == null || parentPath.isBlank()) {
                return childPath;
            }
            return parentPath + "/" + childPath;
        }

        private static final class SourceHandle {
            private final String id;
            private final String classpathResource;
            private final Path filePath;

            private SourceHandle(String id, String classpathResource, Path filePath) {
                this.id = id;
                this.classpathResource = classpathResource;
                this.filePath = filePath;
            }

            RuleSetDescriptor readDescriptor() {
                try {
                    if (classpathResource != null) {
                        try (var stream = YamlRuleSetLoader.class.getClassLoader().getResourceAsStream(classpathResource)) {
                            if (stream == null) {
                                throw RuleLoadException.loadError(classpathResource, "Classpath resource disappeared during load");
                            }
                            return MAPPER.readValue(stream, RuleSetDescriptor.class);
                        }
                    }

                    return MAPPER.readValue(filePath.toFile(), RuleSetDescriptor.class);
                } catch (IOException e) {
                    throw RuleLoadException.parseError(id, e);
                }
            }

            String id() {
                return id;
            }

            String classpathResource() {
                return classpathResource;
            }

            Path filePath() {
                return filePath;
            }

            String classpathParent() {
                if (classpathResource == null || !classpathResource.contains("/")) {
                    return "";
                }
                return classpathResource.substring(0, classpathResource.lastIndexOf('/'));
            }
        }
    }
}
