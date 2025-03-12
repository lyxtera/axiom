package com.lyxtera.axiom.api.model;

import com.lyxtera.axiom.engine.RuleContext;

/**
 * Interface representing an expression in a business rule.
 * Expressions can be conditions, functions, or values.
 */
@FunctionalInterface
public interface Expression<K extends Enum<K>> {
    /**
     * Evaluates the expression in the given context.
     *
     * @param context The context in which to evaluate the expression
     * @return The result of evaluating the expression
     */
    boolean evaluate(RuleContext<K> context);

    /**
     * Returns an expression that represents the logical negation of this
     * expression.
     *
     * @return an expression that represents the logical negation of this
     * expression
     */
    default Expression<K> negate() {
        return (t) -> !evaluate(t);
    }
} 