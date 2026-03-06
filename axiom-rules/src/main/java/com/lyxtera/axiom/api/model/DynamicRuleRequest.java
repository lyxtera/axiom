package com.lyxtera.axiom.api.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a request to execute dynamic rules against a specific ruleset.
 * <p>
 * This class encapsulates all information needed to execute externally supplied
 * rules, including entity identification, rule expressions, and additional context.
 *
 * @param <K> The enum type used as context keys
 */
public class DynamicRuleRequest<K extends Enum<K>> {
    
    /**
     * The name of the entity making the request (e.g., "checkoutService", "orderService").
     * Used for permission validation.
     */
    private String entityName;
    
    /**
     * The name of the ruleset to execute the dynamic rules against.
     * The ruleset must exist and allow dynamic execution.
     */
    private String rulesetName;
    
    /**
     * List of dynamic rule expressions to execute.
     * Each expression follows the same syntax as static rules.
     */
    private List<String> ruleExpressions = new ArrayList<>();
    
    /**
     * Additional context data that may be needed for rule execution.
     * This supplements the main RuleContext provided during execution.
     */
    private Map<String, Object> additionalContext = new HashMap<>();
    
    /**
     * Optional priority for dynamic rules. If not specified, dynamic rules
     * will be executed after static rules in the order they appear.
     */
    private Integer defaultPriority;
    
    /**
     * Flag indicating whether dynamic rules should be executed before or after
     * static rules in the ruleset. Default is false (after static rules).
     */
    private boolean executeBeforeStaticRules = false;
    
    /**
     * Default constructor.
     */
    public DynamicRuleRequest() {
    }
    
    /**
     * Creates a new DynamicRuleRequest with basic parameters.
     *
     * @param entityName The name of the requesting entity
     * @param rulesetName The name of the target ruleset
     * @param ruleExpressions List of rule expressions to execute
     */
    public DynamicRuleRequest(String entityName, String rulesetName, List<String> ruleExpressions) {
        this.entityName = entityName;
        this.rulesetName = rulesetName;
        this.ruleExpressions = ruleExpressions != null ? new ArrayList<>(ruleExpressions) : new ArrayList<>();
    }
    
    /**
     * Gets the entity name.
     *
     * @return The entity name
     */
    public String getEntityName() {
        return entityName;
    }
    
    /**
     * Sets the entity name.
     *
     * @param entityName The entity name
     */
    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }
    
    /**
     * Gets the ruleset name.
     *
     * @return The ruleset name
     */
    public String getRulesetName() {
        return rulesetName;
    }
    
    /**
     * Sets the ruleset name.
     *
     * @param rulesetName The ruleset name
     */
    public void setRulesetName(String rulesetName) {
        this.rulesetName = rulesetName;
    }
    
    /**
     * Gets the list of rule expressions.
     *
     * @return The rule expressions
     */
    public List<String> getRuleExpressions() {
        return ruleExpressions;
    }
    
    /**
     * Sets the list of rule expressions.
     *
     * @param ruleExpressions The rule expressions
     */
    public void setRuleExpressions(List<String> ruleExpressions) {
        this.ruleExpressions = ruleExpressions != null ? new ArrayList<>(ruleExpressions) : new ArrayList<>();
    }
    
    /**
     * Adds a rule expression to the request.
     *
     * @param ruleExpression The rule expression to add
     */
    public void addRuleExpression(String ruleExpression) {
        if (ruleExpression != null && !ruleExpression.trim().isEmpty()) {
            this.ruleExpressions.add(ruleExpression);
        }
    }
    
    /**
     * Gets the additional context map.
     *
     * @return The additional context
     */
    public Map<String, Object> getAdditionalContext() {
        return additionalContext;
    }
    
    /**
     * Sets the additional context map.
     *
     * @param additionalContext The additional context
     */
    public void setAdditionalContext(Map<String, Object> additionalContext) {
        this.additionalContext = additionalContext != null ? new HashMap<>(additionalContext) : new HashMap<>();
    }
    
    /**
     * Adds an entry to the additional context.
     *
     * @param key The context key
     * @param value The context value
     */
    public void addContextEntry(String key, Object value) {
        if (key != null) {
            this.additionalContext.put(key, value);
        }
    }
    
    /**
     * Gets the default priority for dynamic rules.
     *
     * @return The default priority, or null if not specified
     */
    public Integer getDefaultPriority() {
        return defaultPriority;
    }
    
    /**
     * Sets the default priority for dynamic rules.
     *
     * @param defaultPriority The default priority
     */
    public void setDefaultPriority(Integer defaultPriority) {
        this.defaultPriority = defaultPriority;
    }
    
    /**
     * Gets whether dynamic rules should execute before static rules.
     *
     * @return true if dynamic rules should execute first, false otherwise
     */
    public boolean isExecuteBeforeStaticRules() {
        return executeBeforeStaticRules;
    }
    
    /**
     * Sets whether dynamic rules should execute before static rules.
     *
     * @param executeBeforeStaticRules true to execute before static rules, false otherwise
     */
    public void setExecuteBeforeStaticRules(boolean executeBeforeStaticRules) {
        this.executeBeforeStaticRules = executeBeforeStaticRules;
    }
    
    /**
     * Validates the request for basic completeness.
     *
     * @return true if the request is valid, false otherwise
     */
    public boolean isValid() {
        return entityName != null && !entityName.trim().isEmpty() &&
               rulesetName != null && !rulesetName.trim().isEmpty() &&
               ruleExpressions != null && !ruleExpressions.isEmpty() &&
               ruleExpressions.stream().allMatch(expr -> expr != null && !expr.trim().isEmpty());
    }
    
    /**
     * Gets the number of rule expressions in this request.
     *
     * @return The number of rule expressions
     */
    public int getRuleCount() {
        return ruleExpressions.size();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DynamicRuleRequest<?> that = (DynamicRuleRequest<?>) o;
        return executeBeforeStaticRules == that.executeBeforeStaticRules &&
               Objects.equals(entityName, that.entityName) &&
               Objects.equals(rulesetName, that.rulesetName) &&
               Objects.equals(ruleExpressions, that.ruleExpressions) &&
               Objects.equals(additionalContext, that.additionalContext) &&
               Objects.equals(defaultPriority, that.defaultPriority);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(entityName, rulesetName, ruleExpressions, additionalContext, defaultPriority, executeBeforeStaticRules);
    }
    
    @Override
    public String toString() {
        return "DynamicRuleRequest{" +
               "entityName='" + entityName + '\'' +
               ", rulesetName='" + rulesetName + '\'' +
               ", ruleExpressions=" + ruleExpressions +
               ", additionalContext=" + additionalContext +
               ", defaultPriority=" + defaultPriority +
               ", executeBeforeStaticRules=" + executeBeforeStaticRules +
               '}';
    }
}

