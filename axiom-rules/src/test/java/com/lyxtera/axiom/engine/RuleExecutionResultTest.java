package com.lyxtera.axiom.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.lyxtera.axiom.api.model.BusinessRule;

class RuleExecutionResultTest {

    public enum TestKey {
        TEST_KEY
    }

    @Test
    void testEmpty() {
        // Create an empty result
        RuleExecutionResult<TestKey> result = RuleExecutionResult.empty();
        
        // Verify the properties
        assertThat(result.getMatchedRules()).isEmpty();
        assertThat(result.getExecutedRules()).isEmpty();
        assertThat(result.getFirstMatchedRule()).isEmpty();
        assertThat(result.getFirstRuleResult()).isEmpty();
        assertThat(result.getFailureReason()).isPresent();
        assertThat(result.getFailureReason().get()).isEqualTo("No rules matched the context");
        
        // Verify status methods
        assertThat(result.hasMatches()).isFalse();
        assertThat(result.hasFailed()).isTrue();
    }
    
    @Test
    void testSingle() {
        // Create a mock rule
        @SuppressWarnings("unchecked")
        BusinessRule<TestKey> rule = mock(BusinessRule.class);
        when(rule.getName()).thenReturn("TestRule");
        
        // Create a single result
        RuleExecutionResult<TestKey> result = RuleExecutionResult.single(rule, true);
        
        // Verify the properties
        assertThat(result.getMatchedRules()).hasSize(1);
        assertThat(result.getMatchedRules().get(0)).isEqualTo(rule);
        assertThat(result.getExecutedRules()).hasSize(1);
        assertThat(result.getExecutedRules().get(rule)).isTrue();
        assertThat(result.getFirstMatchedRule()).isPresent();
        assertThat(result.getFirstMatchedRule().get()).isEqualTo(rule);
        assertThat(result.getFirstRuleResult()).isPresent();
        assertThat(result.getFirstRuleResult().get()).isTrue();
        assertThat(result.getFailureReason()).isEmpty();
        
        // Verify status methods
        assertThat(result.hasMatches()).isTrue();
        assertThat(result.hasFailed()).isFalse();
    }
    
    @Test
    void testMultiple() {
        // Create mock rules
        @SuppressWarnings("unchecked")
        BusinessRule<TestKey> rule1 = mock(BusinessRule.class);
        when(rule1.getName()).thenReturn("Rule1");
        
        @SuppressWarnings("unchecked")
        BusinessRule<TestKey> rule2 = mock(BusinessRule.class);
        when(rule2.getName()).thenReturn("Rule2");
        
        // Create matched rules list
        List<BusinessRule<TestKey>> matchedRules = Arrays.asList(rule1, rule2);
        
        // Create executed rules map
        Map<BusinessRule<TestKey>, Boolean> executedRules = new HashMap<>();
        executedRules.put(rule1, true);
        executedRules.put(rule2, false);
        
        // Create a multiple result
        RuleExecutionResult<TestKey> result = RuleExecutionResult.multiple(
            matchedRules, executedRules, rule1, true);
        
        // Verify the properties
        assertThat(result.getMatchedRules()).hasSize(2);
        assertThat(result.getMatchedRules()).containsExactly(rule1, rule2);
        assertThat(result.getExecutedRules()).hasSize(2);
        assertThat(result.getExecutedRules().get(rule1)).isTrue();
        assertThat(result.getExecutedRules().get(rule2)).isFalse();
        assertThat(result.getFirstMatchedRule()).isPresent();
        assertThat(result.getFirstMatchedRule().get()).isEqualTo(rule1);
        assertThat(result.getFirstRuleResult()).isPresent();
        assertThat(result.getFirstRuleResult().get()).isTrue();
        assertThat(result.getFailureReason()).isEmpty();
        
        // Verify status methods
        assertThat(result.hasMatches()).isTrue();
        assertThat(result.hasFailed()).isFalse();
    }
    
    @Test
    void testFailure() {
        // Create a failure result
        RuleExecutionResult<TestKey> result = RuleExecutionResult.failure("Test failure");
        
        // Verify the properties
        assertThat(result.getMatchedRules()).isEmpty();
        assertThat(result.getExecutedRules()).isEmpty();
        assertThat(result.getFirstMatchedRule()).isEmpty();
        assertThat(result.getFirstRuleResult()).isEmpty();
        assertThat(result.getFailureReason()).isPresent();
        assertThat(result.getFailureReason().get()).isEqualTo("Test failure");
        
        // Verify status methods
        assertThat(result.hasMatches()).isFalse();
        assertThat(result.hasFailed()).isTrue();
    }
    
    @Test
    void testToString() {
        // Create mock rule
        @SuppressWarnings("unchecked")
        BusinessRule<TestKey> rule = mock(BusinessRule.class);
        when(rule.getName()).thenReturn("TestRule");
        
        // Test toString for different result types
        RuleExecutionResult<TestKey> emptyResult = RuleExecutionResult.empty();
        RuleExecutionResult<TestKey> singleResult = RuleExecutionResult.single(rule, true);
        RuleExecutionResult<TestKey> failureResult = RuleExecutionResult.failure("Test failure");
        
        // Verify toString output
        assertThat(emptyResult.toString()).isEqualTo("RuleExecutionResult{failed: No rules matched the context}");
        assertThat(singleResult.toString()).contains("TestRule");
        assertThat(singleResult.toString()).contains("true");
        assertThat(failureResult.toString()).contains("failed");
        assertThat(failureResult.toString()).contains("Test failure");
    }
    
    @Test
    void testGetDetailedDescription() {
        // Create mock rules
        @SuppressWarnings("unchecked")
        BusinessRule<TestKey> rule1 = mock(BusinessRule.class);
        when(rule1.getName()).thenReturn("Rule1");
        
        @SuppressWarnings("unchecked")
        BusinessRule<TestKey> rule2 = mock(BusinessRule.class);
        when(rule2.getName()).thenReturn("Rule2");
        
        // Create different result types
        RuleExecutionResult<TestKey> emptyResult = RuleExecutionResult.empty();
        RuleExecutionResult<TestKey> singleResult = RuleExecutionResult.single(rule1, true);
        RuleExecutionResult<TestKey> failureResult = RuleExecutionResult.failure("Test failure");
        
        // Create a multiple result
        List<BusinessRule<TestKey>> matchedRules = Arrays.asList(rule1, rule2);
        Map<BusinessRule<TestKey>, Boolean> executedRules = new HashMap<>();
        executedRules.put(rule1, true);
        executedRules.put(rule2, false);
        RuleExecutionResult<TestKey> multipleResult = RuleExecutionResult.multiple(
            matchedRules, executedRules, rule1, true);
        
        // Verify detailed descriptions
        assertThat(emptyResult.getDetailedDescription()).contains("No rules matched");
        assertThat(singleResult.getDetailedDescription()).contains("Rule1");
        assertThat(singleResult.getDetailedDescription()).contains("true");
        assertThat(failureResult.getDetailedDescription()).contains("Execution failed");
        assertThat(failureResult.getDetailedDescription()).contains("Test failure");
        assertThat(multipleResult.getDetailedDescription()).contains("Matched rules (2)");
        assertThat(multipleResult.getDetailedDescription()).contains("Rule1");
        assertThat(multipleResult.getDetailedDescription()).contains("Rule2");
    }
} 