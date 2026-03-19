package com.lyxtera.axiom.engine;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.lyxtera.axiom.api.exception.RuleException;
import com.lyxtera.axiom.api.model.BusinessRule;
import com.lyxtera.axiom.api.model.EntityPermissionDescriptor;
import com.lyxtera.axiom.api.model.RuleSetDescriptor.BusinessActionDescriptor;
import com.lyxtera.axiom.api.model.RuleSetDescriptor.BusinessCheckDescriptor;

/**
 * A collection of rules that can be evaluated in priority order.
 * When multiple rules match conditions, the one with the lowest priority number is selected.
 *
 * @param <K> The enum type to be used as context keys
 */
public class RuleSet<K extends Enum<K>> {
    
    private final List<PrioritizedRule<K>> rules;
    private Metadata metadata;

    /**
     * Creates a new rule set
     */
    public RuleSet() {
        this.rules = new ArrayList<>();
    }

    /**
     * Gets all rules in priority order (lowest number first)
     *
     * @return Unmodifiable list of rules in priority order
     */
    public List<BusinessRule<K>> getRulesInPriorityOrder() {
        return rules.stream()
            .filter(PrioritizedRule::isEffectiveNow)
            .map(PrioritizedRule::getRule)
            .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Gets the metadata associated with this rule set
     * 
     * @return The metadata object containing descriptors
     */
    public Metadata getMetadata() {
        return metadata;
    }

    /**
     * Sets the metadata for this rule set
     * 
     * @param metadata The metadata to set
     */
    void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    /**
     * Adds a rule to the set with the specified priority
     *
     * @param rule The rule to add
     * @param priority Lower values indicate higher priority (priority 1 is higher than priority 2)
     * @param effectiveFrom The date and time from which the rule is effective
     * @throws RuleException if priority is less than 1
     */
    void addRule(BusinessRule<K> rule, int priority, ZonedDateTime effectiveFrom) {
        addRule(rule, priority, effectiveFrom, null);
    }
    
    /**
     * Adds a rule to the set with the specified priority and effective date range
     *
     * @param rule The rule to add
     * @param priority Lower values indicate higher priority (priority 1 is higher than priority 2)
     * @param effectiveFrom The date and time from which the rule is effective
     * @param effectiveTo The date and time until which the rule is effective (null for indefinite)
     * @throws RuleException if priority is less than 1
     */
    void addRule(BusinessRule<K> rule, int priority, ZonedDateTime effectiveFrom, ZonedDateTime effectiveTo) {
        if (priority < 1) {
            throw RuleException.invalidPriority();
        }
        rules.add(new PrioritizedRule<>(rule, priority, effectiveFrom, effectiveTo));
        // Sort in ascending order (lowest priority number first = highest priority)
        Collections.sort(rules);
    }

    /**
     * Validates the rule set for consistency and correctness.
     * This method is called during bootstrap to ensure the rule set is valid before it can be used.
     * 
     * @throws RuleException if any validation fails
     */
    public void validate() {
        // 1. Check if we have any rules at all
        if (rules.isEmpty()) {
            throw RuleException.of("Rule set is empty");
        }

        // 2. Check for duplicate priorities
        Map<Integer, List<PrioritizedRule<K>>> priorityGroups = rules.stream()
            .collect(Collectors.groupingBy(rule -> rule.priority));
            
        priorityGroups.forEach((priority, rulesWithPriority) -> {
            if (rulesWithPriority.size() > 1) {
                String duplicateRules = rulesWithPriority.stream()
                    .map(r -> r.getRule().getName())
                    .collect(Collectors.joining(", "));
                throw RuleException.of("Multiple rules found with priority %d: %s", priority, duplicateRules);
            }
        });

        // 3. Validate metadata if present
        if (metadata != null) {
            validateMetadata();
        }

        // 4. Validate each rule's effective dates
        ZonedDateTime now = ZonedDateTime.now();
        rules.forEach(rule -> {
            // Check if effectiveTo is not before effectiveFrom
            if (rule.effectiveFrom != null && rule.effectiveTo != null 
                && rule.effectiveTo.isBefore(rule.effectiveFrom)) {
                throw RuleException.of(
                    "Rule '%s' has effectiveTo date (%s) before effectiveFrom date (%s)",
                    rule.getRule().getName(),
                    rule.effectiveTo,
                    rule.effectiveFrom
                );
            }

            // Warn if rule is not yet effective or has expired
            if (!rule.isEffectiveNow()) {
                if (rule.effectiveFrom != null && rule.effectiveFrom.isAfter(now)) {
                    System.out.printf("Warning: Rule '%s' is not yet effective (starts from %s)%n",
                        rule.getRule().getName(), rule.effectiveFrom);
                } else if (rule.effectiveTo != null && rule.effectiveTo.isBefore(now)) {
                    System.out.printf("Warning: Rule '%s' has expired (ended at %s)%n",
                        rule.getRule().getName(), rule.effectiveTo);
                }
            }
        });

        // 5. Validate each rule individually
        rules.forEach(prioritizedRule -> validateRule(prioritizedRule.getRule()));
    }

    /**
     * Validates the metadata of the rule set.
     * 
     * @throws RuleException if metadata validation fails
     */
    private void validateMetadata() {
        // Check required metadata fields
        if (metadata.getRuleSetName() == null || metadata.getRuleSetName().trim().isEmpty()) {
            throw RuleException.of("Rule set name is required in metadata");
        }

        // Validate business check descriptors
        metadata.getBusinessCheckDescriptors().forEach((name, descriptor) -> {
            if (descriptor.getName() == null || descriptor.getName().trim().isEmpty()) {
                throw RuleException.of("Business check descriptor name cannot be empty");
            }
            if (descriptor.getParams() == null) {
                throw RuleException.of("Business check '%s' has null parameter list", name);
            }
        });

        // Validate business action descriptors
        metadata.getBusinessActionDescriptors().forEach((name, descriptor) -> {
            if (descriptor.getName() == null || descriptor.getName().trim().isEmpty()) {
                throw RuleException.of("Business action descriptor name cannot be empty");
            }
            if (descriptor.getParams() == null) {
                throw RuleException.of("Business action '%s' has null parameter list", name);
            }
        });

        // Check for descriptor name consistency
        rules.forEach(prioritizedRule -> {
            BusinessRule<K> rule = prioritizedRule.getRule();

            // Validate business actions used in the rule
            rule.getActions().forEach(action -> {
                String actionName = action.getName();
                if (!metadata.getBusinessActionDescriptors().containsKey(actionName)) {
                    throw RuleException.of(
                        "Rule '%s' uses undefined business action '%s'",
                        rule.getName(), actionName
                    );
                }
            });
        });
    }

    /**
     * Validates an individual rule.
     * 
     * @param rule The rule to validate
     * @throws RuleException if rule validation fails
     */
    private void validateRule(BusinessRule<K> rule) {
        // Check required rule properties
        if (rule.getName() == null || rule.getName().trim().isEmpty()) {
            throw RuleException.of("Rule name cannot be empty");
        }

        // Validate rule checks
        if (rule.getCondition() == null) {
            throw RuleException.of("Rule '%s' must have a condition", rule.getName());
        }

        if (rule.isGateRule()) {
            String forwardRef = rule.getOnMatchForwardTo().orElse("").trim();

            if (forwardRef.isEmpty()) {
                throw RuleException.of("Gate rule '%s' must define a non-empty onMatchForwardTo reference", rule.getName());
            }
            if (!rule.getActions().isEmpty()) {
                throw RuleException.of("Rule '%s' cannot define both actions and onMatchForwardTo", rule.getName());
            }
            if (rule.getChildRuleSet().isEmpty()) {
                throw RuleException.of("Gate rule '%s' must have a loaded child ruleset", rule.getName());
            }
            return;
        }

        if (!rule.getActions().isEmpty()) {
            rule.getActions().forEach(action ->
                Objects.requireNonNull(action, () -> "Rule '" + rule.getName() + "' contains a null action"));
            return;
        }

        throw RuleException.of("Rule '%s' must have at least one action", rule.getName());
    }

    /**
     * Contains metadata information about checks, actions, and rules.
     * This is used for better error reporting and documentation.
     */
    public static class Metadata {
        private final Map<String, BusinessCheckDescriptor> checkDescriptors = new HashMap<>();
        private final Map<String, BusinessActionDescriptor> actionDescriptors = new HashMap<>();
        private final Map<String, EntityPermissionDescriptor> entityPermissions = new HashMap<>();
        private String ruleSetName;
        private String ruleSetDescription;
        private boolean allowDynamicExecution = false;
        
        /**
         * Sets the descriptors for business checks
         * 
         * @param descriptors The list of check descriptors
         */
        void setBusinessCheckDescriptors(List<BusinessCheckDescriptor> descriptors) {
            for (BusinessCheckDescriptor descriptor : descriptors) {
                checkDescriptors.put(descriptor.getName(), descriptor);
            }
        }
        
        /**
         * Sets the descriptors for business actions
         * 
         * @param descriptors The list of action descriptors
         */
        void setBusinessActionDescriptors(List<BusinessActionDescriptor> descriptors) {
            for (BusinessActionDescriptor descriptor : descriptors) {
                actionDescriptors.put(descriptor.getName(), descriptor);
            }
        }
        
        /**
         * Sets the rule set name
         * 
         * @param name The name of the rule set
         */
        void setRuleSetName(String name) {
            this.ruleSetName = name;
        }
        
        /**
         * Sets the rule set description
         * 
         * @param description The description of the rule set
         */
        void setRuleSetDescription(String description) {
            this.ruleSetDescription = description;
        }
        
        /**
         * Sets the dynamic execution flag
         * 
         * @param allowDynamicExecution Whether dynamic execution is allowed
         */
        void setAllowDynamicExecution(boolean allowDynamicExecution) {
            this.allowDynamicExecution = allowDynamicExecution;
        }
        
        /**
         * Sets the entity permissions for dynamic rule execution
         * 
         * @param permissions The list of entity permission descriptors
         */
        void setEntityPermissions(List<EntityPermissionDescriptor> permissions) {
            entityPermissions.clear();
            if (permissions != null) {
                for (EntityPermissionDescriptor permission : permissions) {
                    if (permission.getName() != null) {
                        entityPermissions.put(permission.getName(), permission);
                    }
                }
            }
        }
        
        /**
         * Gets the business check descriptor for the given name
         * 
         * @param name The name of the business check
         * @return The descriptor, or null if not found
         */
        public BusinessCheckDescriptor getBusinessCheckDescriptor(String name) {
            return checkDescriptors.get(name);
        }
        
        /**
         * Gets the business action descriptor for the given name
         * 
         * @param name The name of the business action
         * @return The descriptor, or null if not found
         */
        public BusinessActionDescriptor getBusinessActionDescriptor(String name) {
            return actionDescriptors.get(name);
        }
        
        /**
         * Gets a map of all business check descriptors
         * 
         * @return Map of business check descriptors keyed by name
         */
        public Map<String, BusinessCheckDescriptor> getBusinessCheckDescriptors() {
            return Collections.unmodifiableMap(checkDescriptors);
        }
        
        /**
         * Gets a map of all business action descriptors
         * 
         * @return Map of business action descriptors keyed by name
         */
        public Map<String, BusinessActionDescriptor> getBusinessActionDescriptors() {
            return Collections.unmodifiableMap(actionDescriptors);
        }
        
        /**
         * Gets the name of the rule set
         * 
         * @return The rule set name
         */
        public String getRuleSetName() {
            return ruleSetName;
        }
        
        /**
         * Gets the description of the rule set
         * 
         * @return The rule set description
         */
        public String getRuleSetDescription() {
            return ruleSetDescription;
        }
        
        /**
         * Gets whether dynamic execution is allowed
         * 
         * @return true if dynamic execution is allowed, false otherwise
         */
        public boolean isAllowDynamicExecution() {
            return allowDynamicExecution;
        }
        
        /**
         * Gets the entity permission descriptor for the given entity name
         * 
         * @param entityName The name of the entity
         * @return The entity permission descriptor, or null if not found
         */
        public EntityPermissionDescriptor getEntityPermission(String entityName) {
            return entityPermissions.get(entityName);
        }
        
        /**
         * Gets a map of all entity permissions
         * 
         * @return Map of entity permissions keyed by entity name
         */
        public Map<String, EntityPermissionDescriptor> getEntityPermissions() {
            return Collections.unmodifiableMap(entityPermissions);
        }
        
        /**
         * Checks if the specified entity has any permissions defined
         * 
         * @param entityName The name of the entity
         * @return true if the entity has permissions, false otherwise
         */
        public boolean hasEntityPermissions(String entityName) {
            return entityPermissions.containsKey(entityName);
        }
        
        /**
         * Gets the parameter names for a business check
         * 
         * @param checkName The name of the business check
         * @return List of parameter names, or empty list if not found
         */
        public List<String> getBusinessCheckParamNames(String checkName) {
            BusinessCheckDescriptor descriptor = getBusinessCheckDescriptor(checkName);
            return descriptor != null ? descriptor.getParams() : Collections.emptyList();
        }
        
        /**
         * Gets the parameter names for a business action
         * 
         * @param actionName The name of the business action
         * @return List of parameter names, or empty list if not found
         */
        public List<String> getBusinessActionParamNames(String actionName) {
            BusinessActionDescriptor descriptor = getBusinessActionDescriptor(actionName);
            return descriptor != null ? descriptor.getParams() : Collections.emptyList();
        }
    }

    /**
     * Internal class to hold a rule with its priority
     */
    private static class PrioritizedRule<K extends Enum<K>> implements Comparable<PrioritizedRule<K>> {
        private final BusinessRule<K> rule;
        private final int priority;
        private final ZonedDateTime effectiveFrom;
        private final ZonedDateTime effectiveTo;
        
        PrioritizedRule(BusinessRule<K> rule, int priority, ZonedDateTime effectiveFrom, ZonedDateTime effectiveTo) {
            this.rule = rule;
            this.priority = priority;
            this.effectiveFrom = effectiveFrom;
            this.effectiveTo = effectiveTo;
        }

        BusinessRule<K> getRule() {
            return rule;
        }
        
        /**
         * Checks if the rule is effective at the current time.
         * 
         * @return true if the rule is effective, false otherwise
         */
        boolean isEffectiveNow() {
            ZonedDateTime now = ZonedDateTime.now();
            boolean afterStart = effectiveFrom == null || effectiveFrom.isBefore(now);
            boolean beforeEnd = effectiveTo == null || effectiveTo.isAfter(now);
            return afterStart && beforeEnd;
        }

        @Override
        public int compareTo(PrioritizedRule<K> other) {
            return Integer.compare(this.priority, other.priority);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            
            PrioritizedRule<?> other = (PrioritizedRule<?>) obj;
            return priority == other.priority && 
                   (rule == null ? other.rule == null : rule.equals(other.rule));
        }

        @Override
        public int hashCode() {
            int result = rule != null ? rule.hashCode() : 0;
            result = 31 * result + priority;
            return result;
        }

        @Override
        public String toString() {
            return String.format("Rule[%s] with priority %d", rule.getName(), priority);
        }
    }
} 
