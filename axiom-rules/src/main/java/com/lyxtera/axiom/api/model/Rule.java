package com.lyxtera.axiom.api.model;

/**
 * Public interface representing a business rule.
 * This is what external consumers will work with.
 */
public interface Rule {
    /**
     * Gets the name of the rule
     */
    String getName();
    
    /**
     * Gets the original expression used to create this rule
     */
    String getExpression();
} 