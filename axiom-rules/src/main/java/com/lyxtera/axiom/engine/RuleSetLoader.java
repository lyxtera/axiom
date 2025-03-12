package com.lyxtera.axiom.engine;

import java.io.IOException;

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
         * Loads a YAML rule set descriptor from a classpath resource.
         *
         * @return the rule set descriptor
         */
        @Override
        public RuleSetDescriptor loadDescriptor() {
            try (var stream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
                if (stream != null) {
                    return MAPPER.readValue(stream, RuleSetDescriptor.class);
                }

                throw RuleLoadException.loadError(resourcePath, "Resource not found in classpath");
            } catch (IOException e) {
                throw RuleLoadException.parseError(resourcePath, e);
            }
        }
    }
} 