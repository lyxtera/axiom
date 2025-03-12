package com.lyxtera.axiom.api.model;

import static java.lang.Boolean.TRUE;

import java.util.List;

import com.lyxtera.axiom.engine.RuleContext;

/**
 * Represents a business rule with a condition and a list of actions.
 * <p>
 * A business rule consists of a name, a condition that can be evaluated,
 * and a list of actions that are executed when the condition is met.
 *
 * @param <K> The enum type to be used as context keys
 */
public class BusinessRule<K extends Enum<K>> {
    private final String name;
    private final Condition<K> condition;
    private final List<RuleFunction<K>> actions;
    
    /**
     * Creates a new BusinessRule with the specified name, condition, and actions.
     *
     * @param name The name of the rule
     * @param condition The condition to evaluate
     * @param actions The actions to execute when the condition is met
     */
    public BusinessRule(String name, Condition<K> condition, List<RuleFunction<K>> actions) {
        this.name = name;
        this.condition = condition;
        this.actions = actions;
    }
    
    /**
     * Gets the name of the rule.
     *
     * @return The name of the rule
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the condition of the rule.
     *
     * @return The condition of the rule
     */
    public Condition<K> getCondition() {
        return condition;
    }
    
    /**
     * Gets the actions of the rule.
     *
     * @return The actions of the rule
     */
    public List<RuleFunction<K>> getActions() {
        return actions;
    }
    
    /**
     * Evaluates the rule's condition and executes the actions if the condition is met.
     * <p>
     * This method evaluates the rule's condition against the context and executes
     * the rule's actions if the condition is met.
     *
     * @param context The context containing evaluation data
     * @return true if the rule's condition was met and actions were executed, false otherwise
     */
    public boolean evaluate(RuleContext<K> context) {
        if (condition == null || TRUE.equals(condition.evaluate(context))) {
            for (RuleFunction<K> action : actions) {
                action.execute(context);
            }
            return true;
        }
        return false;
    }
} 