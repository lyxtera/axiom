package com.lyxtera.axiom.api.parser;

import com.lyxtera.axiom.api.exception.RuleParserException;
import com.lyxtera.axiom.api.model.BusinessRule;
import com.lyxtera.axiom.engine.RuleSet;

/**
 * Interface for parsing rule expressions into business rules.
 *
 * @param <K> The type of keys in the rule context
 */
public interface Parser<K extends Enum<K>> {
    
    /**
     * Parses a rule expression into a business rule.
     *
     * @param metadata The rule set metadata
     * @param ruleName The name of the rule
     * @param expression The rule expression to parse
     * @return A business rule representing the parsed expression
     * @throws RuleParserException if the expression is invalid
     */
    BusinessRule<K> parseRule(RuleSet.Metadata metadata, String ruleName, String expression);
} 