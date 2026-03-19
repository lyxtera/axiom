package com.lyxtera.axiom.api.model;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a single dynamic rule that is supplied externally at runtime.
 * <p>
 * This class is similar to RuleDescriptor but designed for rules that are
 * created and supplied dynamically rather than defined in YAML files.
 * It contains the minimal information needed to create and execute a rule.
 */
public class DynamicRuleDescriptor {
    
    /**
     * The name of the dynamic rule.
     * If not provided, a name will be generated automatically.
     */
    private String name;
    
    /**
     * The rule expression following the same syntax as static rules.
     * Example: "successfulOrders() > 5 then applyDiscount(10)"
     */
    private String expression;
    
    /**
     * The priority of the rule. Lower values indicate higher priority.
     * If not specified, uses the default priority from the request.
     */
    private Integer priority;
    
    /**
     * Optional description of what the rule does.
     */
    private String description;
    
    /**
     * The time from which this rule is effective.
     * If not specified, the rule is effective immediately.
     */
    private ZonedDateTime effectiveFrom;
    
    /**
     * The time until which this rule is effective.
     * If not specified, the rule is effective indefinitely.
     */
    private ZonedDateTime effectiveTo;
    
    /**
     * Additional metadata for the rule.
     */
    private Map<String, Object> metadata = new HashMap<>();
    
    /**
     * Default constructor.
     */
    public DynamicRuleDescriptor() {
    }
    
    /**
     * Creates a new DynamicRuleDescriptor with basic parameters.
     *
     * @param name The name of the rule
     * @param expression The rule expression
     */
    public DynamicRuleDescriptor(String name, String expression) {
        this.name = name;
        this.expression = expression;
    }
    
    /**
     * Creates a new DynamicRuleDescriptor with name, expression, and priority.
     *
     * @param name The name of the rule
     * @param expression The rule expression
     * @param priority The rule priority
     */
    public DynamicRuleDescriptor(String name, String expression, Integer priority) {
        this.name = name;
        this.expression = expression;
        this.priority = priority;
    }
    
    /**
     * Gets the name of the rule.
     *
     * @return The rule name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the name of the rule.
     *
     * @param name The rule name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Gets the rule expression.
     *
     * @return The rule expression
     */
    public String getExpression() {
        return expression;
    }
    
    /**
     * Sets the rule expression.
     *
     * @param expression The rule expression
     */
    public void setExpression(String expression) {
        this.expression = expression;
    }
    
    /**
     * Gets the rule priority.
     *
     * @return The rule priority, or null if not specified
     */
    public Integer getPriority() {
        return priority;
    }
    
    /**
     * Sets the rule priority.
     *
     * @param priority The rule priority
     */
    public void setPriority(Integer priority) {
        this.priority = priority;
    }
    
    /**
     * Gets the rule description.
     *
     * @return The rule description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Sets the rule description.
     *
     * @param description The rule description
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Gets the effective from date.
     *
     * @return The effective from date, or null if not specified
     */
    public ZonedDateTime getEffectiveFrom() {
        return effectiveFrom;
    }
    
    /**
     * Sets the effective from date.
     *
     * @param effectiveFrom The effective from date
     */
    public void setEffectiveFrom(ZonedDateTime effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }
    
    /**
     * Gets the effective to date.
     *
     * @return The effective to date, or null if not specified
     */
    public ZonedDateTime getEffectiveTo() {
        return effectiveTo;
    }
    
    /**
     * Sets the effective to date.
     *
     * @param effectiveTo The effective to date
     */
    public void setEffectiveTo(ZonedDateTime effectiveTo) {
        this.effectiveTo = effectiveTo;
    }
    
    /**
     * Gets the metadata map.
     *
     * @return The metadata map
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    /**
     * Sets the metadata map.
     *
     * @param metadata The metadata map
     */
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
    }
    
    /**
     * Adds a metadata entry.
     *
     * @param key The metadata key
     * @param value The metadata value
     */
    public void addMetadata(String key, Object value) {
        if (key != null) {
            this.metadata.put(key, value);
        }
    }
    
    /**
     * Validates the rule descriptor for basic completeness.
     *
     * @return true if the descriptor is valid, false otherwise
     */
    public boolean isValid() {
        return expression != null && !expression.trim().isEmpty();
    }
    
    /**
     * Generates a name for the rule if none is provided.
     *
     * @param index The index of this rule in a collection (for uniqueness)
     * @return A generated rule name
     */
    public String generateName(int index) {
        if (name != null && !name.trim().isEmpty()) {
            return name;
        }
        return "DynamicRule_" + index;
    }
    
    /**
     * Checks if this rule is currently effective based on the given time.
     *
     * @param currentTime The current time to check against
     * @return true if the rule is effective at the given time, false otherwise
     */
    public boolean isEffectiveAt(ZonedDateTime currentTime) {
        if (currentTime == null) {
            return true; // If no time specified, assume effective
        }
        
        if (effectiveFrom != null && currentTime.isBefore(effectiveFrom)) {
            return false;
        }
        
        if (effectiveTo != null && currentTime.isAfter(effectiveTo)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Converts this DynamicRuleDescriptor to a static RuleDescriptor.
     * Useful for integration with existing rule processing logic.
     *
     * @return A RuleDescriptor with the same content
     */
    public RuleSetDescriptor.RuleDescriptor toRuleDescriptor() {
        RuleSetDescriptor.RuleDescriptor ruleDescriptor = new RuleSetDescriptor.RuleDescriptor();
        ruleDescriptor.setName(name);
        ruleDescriptor.setExpression(expression);
        ruleDescriptor.setDescription(description);
        ruleDescriptor.setEffectiveFrom(effectiveFrom);
        ruleDescriptor.setEffectiveTo(effectiveTo);
        ruleDescriptor.setMetadata(new HashMap<>(metadata));
        
        // Set priority, using a default if not specified
        if (priority != null) {
            ruleDescriptor.setPriority(priority);
        } else {
            ruleDescriptor.setPriority(1000); // Default priority for dynamic rules
        }
        
        return ruleDescriptor;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DynamicRuleDescriptor that = (DynamicRuleDescriptor) o;
        return Objects.equals(name, that.name) &&
               Objects.equals(expression, that.expression) &&
               Objects.equals(priority, that.priority) &&
               Objects.equals(description, that.description) &&
               Objects.equals(effectiveFrom, that.effectiveFrom) &&
               Objects.equals(effectiveTo, that.effectiveTo) &&
               Objects.equals(metadata, that.metadata);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, expression, priority, description, effectiveFrom, effectiveTo, metadata);
    }
    
    @Override
    public String toString() {
        return "DynamicRuleDescriptor{" +
               "name='" + name + '\'' +
               ", expression='" + expression + '\'' +
               ", priority=" + priority +
               ", description='" + description + '\'' +
               ", effectiveFrom=" + effectiveFrom +
               ", effectiveTo=" + effectiveTo +
               ", metadata=" + metadata +
               '}';
    }
}

