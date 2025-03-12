package com.lyxtera.axiom.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.lyxtera.axiom.api.model.BusinessRule;

/**
 * Holds the results and statistics of rule execution.
 * <p>
 * This class provides detailed information about what rules were matched,
 * which ones were executed, and their execution results.
 *
 * @param <K> The enum type used as context keys
 */
public class RuleExecutionResult<K extends Enum<K>> {

    private final List<BusinessRule<K>> matchedRules;
    private final Map<BusinessRule<K>, Boolean> executedRules;
    private final BusinessRule<K> firstMatchedRule;
    private final Boolean firstRuleResult;
    private final String failureReason;

    /**
     * Creates a new empty result, indicating no rules matched.
     */
    public static <K extends Enum<K>> RuleExecutionResult<K> empty() {
        return new RuleExecutionResult<>(
            Collections.emptyList(),
            Collections.emptyMap(),
            null,
            null,
            "No rules matched the context"
        );
    }

    /**
     * Creates a new result with a single executed rule.
     *
     * @param matchedRule The rule that matched
     * @param result The result of executing the rule
     */
    public static <K extends Enum<K>> RuleExecutionResult<K> single(BusinessRule<K> matchedRule, boolean result) {
        List<BusinessRule<K>> matched = new ArrayList<>();
        matched.add(matchedRule);
        
        Map<BusinessRule<K>, Boolean> executed = new LinkedHashMap<>();
        executed.put(matchedRule, result);
        
        return new RuleExecutionResult<>(
            matched,
            executed,
            matchedRule,
            result,
            null
        );
    }

    /**
     * Creates a new result with multiple executed rules.
     *
     * @param matchedRules The rules that matched
     * @param executedRules The executed rules and their results
     * @param firstMatchedRule The first rule that matched (highest priority)
     * @param firstRuleResult The result of executing the first rule
     */
    public static <K extends Enum<K>> RuleExecutionResult<K> multiple(
            List<BusinessRule<K>> matchedRules,
            Map<BusinessRule<K>, Boolean> executedRules,
            BusinessRule<K> firstMatchedRule,
            Boolean firstRuleResult) {
        return new RuleExecutionResult<>(
            matchedRules,
            executedRules,
            firstMatchedRule,
            firstRuleResult,
            null
        );
    }

    /**
     * Creates a new result with an execution failure.
     *
     * @param failureReason The reason for the failure
     */
    public static <K extends Enum<K>> RuleExecutionResult<K> failure(String failureReason) {
        return new RuleExecutionResult<>(
            Collections.emptyList(),
            Collections.emptyMap(),
            null,
            null,
            failureReason
        );
    }

    private RuleExecutionResult(
            List<BusinessRule<K>> matchedRules,
            Map<BusinessRule<K>, Boolean> executedRules,
            BusinessRule<K> firstMatchedRule,
            Boolean firstRuleResult,
            String failureReason) {
        this.matchedRules = Collections.unmodifiableList(matchedRules);
        this.executedRules = Collections.unmodifiableMap(executedRules);
        this.firstMatchedRule = firstMatchedRule;
        this.firstRuleResult = firstRuleResult;
        this.failureReason = failureReason;
    }

    /**
     * Gets the list of rules that matched the context.
     *
     * @return The matched rules
     */
    public List<BusinessRule<K>> getMatchedRules() {
        return matchedRules;
    }

    /**
     * Gets the map of rules that were executed and their results.
     *
     * @return The executed rules and their results
     */
    public Map<BusinessRule<K>, Boolean> getExecutedRules() {
        return executedRules;
    }

    /**
     * Gets the first matched rule (highest priority).
     *
     * @return The first matched rule, or empty if no rules matched
     */
    public Optional<BusinessRule<K>> getFirstMatchedRule() {
        return Optional.ofNullable(firstMatchedRule);
    }

    /**
     * Gets the result of executing the first matched rule.
     *
     * @return The result, or empty if no rules were executed
     */
    public Optional<Boolean> getFirstRuleResult() {
        return Optional.ofNullable(firstRuleResult);
    }

    /**
     * Gets the failure reason, if any.
     *
     * @return The failure reason, or empty if there was no failure
     */
    public Optional<String> getFailureReason() {
        return Optional.ofNullable(failureReason);
    }

    /**
     * Checks if any rules matched.
     *
     * @return true if at least one rule matched, false otherwise
     */
    public boolean hasMatches() {
        return !matchedRules.isEmpty();
    }

    /**
     * Checks if the execution failed.
     *
     * @return true if the execution failed, false otherwise
     */
    public boolean hasFailed() {
        return failureReason != null;
    }

    /**
     * Gets a summary of the execution results.
     *
     * @return A string summary of the execution results
     */
    @Override
    public String toString() {
        if (hasFailed()) {
            return "RuleExecutionResult{failed: " + failureReason + "}";
        }
        
        if (!hasMatches()) {
            return "RuleExecutionResult{no matches}";
        }
        
        StringBuilder builder = new StringBuilder("RuleExecutionResult{");
        builder.append("matched: ").append(matchedRules.size()).append(", ");
        
        if (firstMatchedRule != null) {
            builder.append("first: '").append(firstMatchedRule.getName()).append("'");
            if (firstRuleResult != null) {
                builder.append(" (").append(firstRuleResult).append(")");
            }
        }
        
        builder.append("}");
        return builder.toString();
    }

    /**
     * Gets a detailed description of the execution results.
     *
     * @return A detailed description of the execution results
     */
    public String getDetailedDescription() {
        StringBuilder builder = new StringBuilder();
        
        if (hasFailed()) {
            builder.append("Execution failed: ").append(failureReason);
            return builder.toString();
        }
        
        if (!hasMatches()) {
            builder.append("No rules matched the context");
            return builder.toString();
        }
        
        builder.append("Matched rules (").append(matchedRules.size()).append("):\n");
        
        for (BusinessRule<K> rule : matchedRules) {
            builder.append("  - ").append(rule.getName());
            if (executedRules.containsKey(rule)) {
                builder.append(": ").append(executedRules.get(rule));
            }
            builder.append("\n");
        }
        
        return builder.toString();
    }
} 