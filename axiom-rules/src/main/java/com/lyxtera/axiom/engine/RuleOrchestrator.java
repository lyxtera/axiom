package com.lyxtera.axiom.engine;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.lyxtera.axiom.api.model.BusinessRule;

/**
 * Orchestrates rule execution for a specific rule set.
 * <p>
 * This class provides methods to execute rules from a specific rule set
 * against a given context, and also provides rule parsing functionality.
 *
 * @param <K> The enum type to be used as context keys
 */
public class RuleOrchestrator<K extends Enum<K>> {
    
    private final RuleSet<K> ruleSet;
    
    /**
     * Creates a new rule orchestrator.
     *
     * @param ruleSet The rule set to orchestrate
     */
    public RuleOrchestrator(RuleSet<K> ruleSet) {
        this.ruleSet = ruleSet;
    }

    /**
     * Gets the first matching rule in the rule set against the given context.
     *
     * @param context The context to execute the rule against
     * @return The first matching rule, or empty if no rule matches
     */
    public Optional<BusinessRule<K>> getFirstMatchingRule(RuleContext<K> context) {
        return ruleSet.getRulesInPriorityOrder().stream()
            .filter(rule -> rule.getCondition().evaluate(context))
            .findFirst();
    }
    
    /**
     * Executes all rules in the rule set against the given context.
     * <p>
     * This method evaluates all rules in the rule set and executes their
     * actions if their conditions are met.
     *
     * @param context The context to execute the rules against
     * @return A result object containing details about the rule execution
     */
    public RuleExecutionResult<K> executeAllMatchingRules(RuleContext<K> context) {
        try {
            List<BusinessRule<K>> matchedRules = new ArrayList<>();
            Map<BusinessRule<K>, Boolean> executedRules = new LinkedHashMap<>();
            BusinessRule<K> firstMatchedRule = null;
            Boolean firstRuleResult = null;
            
            for (BusinessRule<K> rule : ruleSet.getRulesInPriorityOrder()) {
                if (rule.getCondition().evaluate(context)) {
                    matchedRules.add(rule);
                    
                    // Execute the rule and store the result
                    boolean result = rule.evaluate(context);
                    executedRules.put(rule, result);
                    
                    // Keep track of the first matched rule and its result
                    if (firstMatchedRule == null) {
                        firstMatchedRule = rule;
                        firstRuleResult = result;
                    }
                }
            }
            
            if (matchedRules.isEmpty()) {
                return RuleExecutionResult.empty();
            }
            
            return RuleExecutionResult.multiple(
                matchedRules,
                executedRules,
                firstMatchedRule,
                firstRuleResult
            );
        } catch (Exception e) {
            return RuleExecutionResult.failure("Error executing rules: " + e.getMessage());
        }
    }

    /**
     * Executes the first matching rule in the rule set against the given context.
     * <p>
     * This method finds the first rule in the rule set whose condition is met,
     * executes its actions, and returns the execution result.
     *
     * @param context The context to execute the rule against
     * @return A result object containing details about the rule execution
     */
    public RuleExecutionResult<K> executeFirstMatchingRule(RuleContext<K> context) {
        try {
            Optional<BusinessRule<K>> ruleOpt = getFirstMatchingRule(context);
            
            if (ruleOpt.isEmpty()) {
                return RuleExecutionResult.empty();
            }
            
            BusinessRule<K> rule = ruleOpt.get();
            boolean result = rule.evaluate(context);
            
            return RuleExecutionResult.single(rule, result);
        } catch (Exception e) {
            return RuleExecutionResult.failure("Error executing rule: " + e.getMessage());
        }
    }
    
    /**
     * Gets the underlying rule set.
     *
     * @return The rule set
     */
    public RuleSet<K> getRuleSet() {
        return ruleSet;
    }
} 