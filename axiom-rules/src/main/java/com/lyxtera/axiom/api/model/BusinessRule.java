package com.lyxtera.axiom.api.model;

import static java.lang.Boolean.TRUE;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.lyxtera.axiom.engine.RuleContext;
import com.lyxtera.axiom.engine.RuleSet;

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
    private final String onMatchForwardTo;
    private RuleSet<K> childRuleSet;
    
    /**
     * Creates a new BusinessRule with the specified name, condition, and actions.
     *
     * @param name The name of the rule
     * @param condition The condition to evaluate
     * @param actions The actions to execute when the condition is met
     */
    public BusinessRule(String name, Condition<K> condition, List<RuleFunction<K>> actions) {
        this(name, condition, actions, null);
    }

    /**
     * Creates a new BusinessRule with the specified name, condition, actions, and child forward reference.
     *
     * @param name The name of the rule
     * @param condition The condition to evaluate
     * @param actions The actions to execute when the condition is met
     * @param onMatchForwardTo The child ruleset reference for gate rules
     */
    public BusinessRule(String name, Condition<K> condition, List<RuleFunction<K>> actions, String onMatchForwardTo) {
        this.name = name;
        this.condition = condition;
        this.actions = actions == null ? Collections.emptyList() : List.copyOf(actions);
        this.onMatchForwardTo = onMatchForwardTo;
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
     * Gets the child ruleset reference for gate rules.
     *
     * @return The child ruleset reference, if this is a gate rule
     */
    public Optional<String> getOnMatchForwardTo() {
        return Optional.ofNullable(onMatchForwardTo);
    }

    /**
     * Gets the child ruleset for gate rules.
     *
     * @return The child ruleset, if one has been loaded
     */
    public Optional<RuleSet<K>> getChildRuleSet() {
        return Optional.ofNullable(childRuleSet);
    }

    /**
     * Returns whether this rule is an action rule.
     *
     * @return true when the rule executes actions directly
     */
    public boolean isActionRule() {
        return !isGateRule();
    }

    /**
     * Returns whether this rule is a gate rule.
     *
     * @return true when the rule forwards to a child ruleset
     */
    public boolean isGateRule() {
        return onMatchForwardTo != null;
    }

    /**
     * Attaches a child ruleset to a gate rule after load-time resolution.
     *
     * @param childRuleSet The child ruleset to evaluate when the gate matches
     */
    public void setChildRuleSet(RuleSet<K> childRuleSet) {
        this.childRuleSet = childRuleSet;
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
