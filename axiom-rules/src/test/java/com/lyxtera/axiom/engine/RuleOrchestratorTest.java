package com.lyxtera.axiom.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.lyxtera.axiom.api.exception.AxiomEngineException;
import com.lyxtera.axiom.api.model.BusinessRule;
import com.lyxtera.axiom.api.model.Condition;
import com.lyxtera.axiom.api.model.Expression;
import com.lyxtera.axiom.api.model.RuleFunction;

class RuleOrchestratorTest {

    public enum TestKey {
        CONDITION_RESULT,
        THROW_EXCEPTION
    }

    private RuleSet<TestKey> ruleSet;
    private RuleContext<TestKey> context;
    private BusinessRule<TestKey> rule1;
    private BusinessRule<TestKey> rule2;
    private BusinessRule<TestKey> rule3;

    @BeforeEach
    void setUp() {
        // Create a rule set
        ruleSet = new RuleSet<>();
        
        // Create a context
        context = new RuleContext<>(TestKey.class);
        
        // Create mock rules
        rule1 = createMockRule("Rule1", (RuleContext<TestKey> ctx) -> {
            if (Boolean.TRUE.equals(ctx.get(TestKey.THROW_EXCEPTION, Boolean.class).orElse(false))) {
                throw new AxiomEngineException("Test exception");
            }
            return ctx.get(TestKey.CONDITION_RESULT, Boolean.class).orElse(false);
        });
        
        rule2 = createMockRule("Rule2", (RuleContext<TestKey> ctx) -> true);
        rule3 = createMockRule("Rule3", (RuleContext<TestKey> ctx) -> false);
        
        // Add rules to the rule set
        ruleSet.addRule(rule1, 10, ZonedDateTime.now());
        ruleSet.addRule(rule2, 20, ZonedDateTime.now());
        ruleSet.addRule(rule3, 30, ZonedDateTime.now());
    }

    @Test
    void testConstructor() {
        // Create an orchestrator
        RuleOrchestrator<TestKey> orchestrator = new RuleOrchestrator<>(ruleSet);
        
        // Verify the rule set was set correctly
        assertThat(orchestrator.getRuleSet()).isEqualTo(ruleSet);
    }
    
    @Test
    void testGetFirstMatchingRule_WithMatch() {
        // Set up the context to match rule1
        context.add(TestKey.CONDITION_RESULT, true);
        
        // Create an orchestrator
        RuleOrchestrator<TestKey> orchestrator = new RuleOrchestrator<>(ruleSet);
        
        // Get the first matching rule
        Optional<BusinessRule<TestKey>> result = orchestrator.getFirstMatchingRule(context);
        
        // Verify the result
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(rule1);
    }
    
    @Test
    void testGetFirstMatchingRule_WithNoMatch() {
        // Set up the context to not match any rules
        context.add(TestKey.CONDITION_RESULT, false);
        
        // Create an orchestrator with a rule set that doesn't have rule2 or rule3
        RuleSet<TestKey> emptyRuleSet = new RuleSet<>();
        emptyRuleSet.addRule(rule3, 30, ZonedDateTime.now()); // rule3 always returns false
        RuleOrchestrator<TestKey> orchestrator = new RuleOrchestrator<>(emptyRuleSet);
        
        // Get the first matching rule
        Optional<BusinessRule<TestKey>> result = orchestrator.getFirstMatchingRule(context);
        
        // Verify the result
        assertThat(result).isEmpty();
    }
    
    @Test
    void testGetFirstMatchingRule_WithException() {
        // Set up the context to throw an exception
        context.add(TestKey.THROW_EXCEPTION, true);
        
        // Create an orchestrator
        RuleOrchestrator<TestKey> orchestrator = new RuleOrchestrator<>(ruleSet);
        
        // Verify that getFirstMatchingRule throws an exception
        assertThatThrownBy(() -> orchestrator.getFirstMatchingRule(context))
            .isInstanceOf(AxiomEngineException.class)
            .hasMessageContaining("Test exception");
    }
    
    @Test
    void testExecuteFirstMatchingRule_WithMatch() {
        // Set up the context to match rule1
        context.add(TestKey.CONDITION_RESULT, true);
        
        // Create an orchestrator
        RuleOrchestrator<TestKey> orchestrator = new RuleOrchestrator<>(ruleSet);
        
        // Execute the first matching rule
        RuleExecutionResult<TestKey> result = orchestrator.executeFirstMatchingRule(context);
        
        // Verify the result
        assertThat(result.hasMatches()).isTrue();
        assertThat(result.hasFailed()).isFalse();
        assertThat(result.getFirstMatchedRule()).isPresent();
        assertThat(result.getFirstMatchedRule().get()).isEqualTo(rule1);
        
        // Verify the rule was executed
        verify(rule1, times(1)).evaluate(context);
        verify(rule2, never()).evaluate(context);
        verify(rule3, never()).evaluate(context);
    }
    
    @Test
    void testExecuteFirstMatchingRule_WithNoMatch() {
        // Set up the context to not match any rules
        context.add(TestKey.CONDITION_RESULT, false);
        
        // Create an orchestrator with a rule set that doesn't have rule2
        RuleSet<TestKey> noMatchRuleSet = new RuleSet<>();
        noMatchRuleSet.addRule(rule3, 30, ZonedDateTime.now()); // rule3 always returns false
        RuleOrchestrator<TestKey> orchestrator = new RuleOrchestrator<>(noMatchRuleSet);
        
        // Execute the first matching rule
        RuleExecutionResult<TestKey> result = orchestrator.executeFirstMatchingRule(context);
        
        // Verify the result
        assertThat(result.hasMatches()).isFalse();
        assertThat(result.hasFailed()).isTrue();
        assertThat(result.getFailureReason()).isPresent();
        assertThat(result.getFailureReason().get()).contains("No rules matched");
        
        // Verify rules were checked but not executed
        verify(rule3, never()).evaluate(context);
    }
    
    @Test
    void testExecuteFirstMatchingRule_WithException() {
        // Set up the context to throw an exception
        context.add(TestKey.THROW_EXCEPTION, true);
        
        // Create an orchestrator
        RuleOrchestrator<TestKey> orchestrator = new RuleOrchestrator<>(ruleSet);
        
        // Execute the first matching rule
        RuleExecutionResult<TestKey> result = orchestrator.executeFirstMatchingRule(context);
        
        // Verify the result
        assertThat(result.hasMatches()).isFalse();
        assertThat(result.hasFailed()).isTrue();
        assertThat(result.getFailureReason()).isPresent();
        assertThat(result.getFailureReason().get()).contains("Error executing rule");
    }
    
    @Test
    void testExecuteAllMatchingRules_WithMatches() {
        // Set up the context to match rule1 and rule2
        context.add(TestKey.CONDITION_RESULT, true);
        
        // Create an orchestrator
        RuleOrchestrator<TestKey> orchestrator = new RuleOrchestrator<>(ruleSet);
        
        // Execute all matching rules
        RuleExecutionResult<TestKey> result = orchestrator.executeAllMatchingRules(context);
        
        // Verify the result
        assertThat(result.hasMatches()).isTrue();
        assertThat(result.hasFailed()).isFalse();
        assertThat(result.getMatchedRules()).hasSize(2);
        assertThat(result.getMatchedRules()).contains(rule1, rule2);
        assertThat(result.getFirstMatchedRule()).isPresent();
        assertThat(result.getFirstMatchedRule().get()).isEqualTo(rule1);
        
        // Verify the rules were executed
        verify(rule1, times(1)).evaluate(context);
        verify(rule2, times(1)).evaluate(context);
        verify(rule3, never()).evaluate(context);
    }
    
    @Test
    void testExecuteAllMatchingRules_WithNoMatches() {
        // Set up the context to not match any rules
        context.add(TestKey.CONDITION_RESULT, false);
        
        // Create an orchestrator with a rule set that doesn't have rule2
        RuleSet<TestKey> noMatchRuleSet = new RuleSet<>();
        noMatchRuleSet.addRule(rule3, 30, ZonedDateTime.now()); // rule3 always returns false
        RuleOrchestrator<TestKey> orchestrator = new RuleOrchestrator<>(noMatchRuleSet);
        
        // Execute all matching rules
        RuleExecutionResult<TestKey> result = orchestrator.executeAllMatchingRules(context);
        
        // Verify the result
        assertThat(result.hasMatches()).isFalse();
        assertThat(result.hasFailed()).isTrue();
        assertThat(result.getFailureReason()).isPresent();
        assertThat(result.getMatchedRules()).isEmpty();
        
        // Verify rules were checked but not executed
        verify(rule3, never()).evaluate(context);
    }
    
    @Test
    void testExecuteAllMatchingRules_WithException() {
        // Set up the context to throw an exception
        context.add(TestKey.THROW_EXCEPTION, true);
        
        // Create an orchestrator
        RuleOrchestrator<TestKey> orchestrator = new RuleOrchestrator<>(ruleSet);
        
        // Execute all matching rules
        RuleExecutionResult<TestKey> result = orchestrator.executeAllMatchingRules(context);
        
        // Verify the result
        assertThat(result.hasMatches()).isFalse();
        assertThat(result.hasFailed()).isTrue();
        assertThat(result.getFailureReason()).isPresent();
        assertThat(result.getFailureReason().get()).contains("Execution failed");
    }
    
    // Helper method to create a mock rule
    private BusinessRule<TestKey> createMockRule(String name, Expression<TestKey> condition) {
        @SuppressWarnings("unchecked")
        BusinessRule<TestKey> rule = mock(BusinessRule.class);
        when(rule.getName()).thenReturn(name);
        when(rule.getCondition()).thenReturn(Condition.asBoolean(condition));
        
        // Create a mock action
        @SuppressWarnings("unchecked")
        RuleFunction<TestKey> action = mock(RuleFunction.class);
        when(rule.getActions()).thenReturn(Collections.singletonList(action));
        
        // This is the critical part - make the evaluate method use the actual condition logic
        when(rule.evaluate(any())).thenAnswer(invocation -> {
            RuleContext<TestKey> ctx = invocation.getArgument(0);
            return condition.evaluate(ctx);
        });
        
        return rule;
    }
} 