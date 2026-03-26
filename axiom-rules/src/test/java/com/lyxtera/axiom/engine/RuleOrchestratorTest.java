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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.lyxtera.axiom.api.exception.AxiomEngineException;
import com.lyxtera.axiom.api.exception.DynamicRuleValidationException;
import com.lyxtera.axiom.api.model.BusinessRule;
import com.lyxtera.axiom.api.model.Condition;
import com.lyxtera.axiom.api.model.DynamicRuleRequest;
import com.lyxtera.axiom.api.model.Expression;
import com.lyxtera.axiom.api.model.RuleFunction;
import com.lyxtera.axiom.api.model.RuleSetDescriptor.BusinessActionDescriptor;
import com.lyxtera.axiom.api.parser.Parser;

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

    @Test
    void testExecuteFirstMatchingRule_WithMatchingGateReturnsChildActionRule() {
        RuleSet<TestKey> parentRuleSet = new RuleSet<>();
        RuleSet<TestKey> childRuleSet = new RuleSet<>();

        BusinessRule<TestKey> gateRule = createGateRule("GateRule", ctx -> true, childRuleSet, "/child.yaml");
        BusinessRule<TestKey> childActionRule = createConcreteRule("ChildActionRule", ctx -> true);

        childRuleSet.addRule(childActionRule, 10, ZonedDateTime.now());
        parentRuleSet.addRule(gateRule, 10, ZonedDateTime.now());

        RuleOrchestrator<TestKey> orchestrator = new RuleOrchestrator<>(parentRuleSet);
        RuleExecutionResult<TestKey> result = orchestrator.executeFirstMatchingRule(context);

        assertThat(result.hasMatches()).isTrue();
        assertThat(result.getFirstMatchedRule()).hasValue(childActionRule);
    }

    @Test
    void testExecuteFirstMatchingRule_WithGateNoChildMatchContinuesToSiblingRule() {
        RuleSet<TestKey> parentRuleSet = new RuleSet<>();
        RuleSet<TestKey> childRuleSet = new RuleSet<>();

        BusinessRule<TestKey> gateRule = createGateRule("GateRule", ctx -> true, childRuleSet, "/child.yaml");
        BusinessRule<TestKey> childMissRule = createConcreteRule("ChildMissRule", ctx -> false);
        BusinessRule<TestKey> siblingRule = createConcreteRule("SiblingRule", ctx -> true);

        childRuleSet.addRule(childMissRule, 10, ZonedDateTime.now());
        parentRuleSet.addRule(gateRule, 10, ZonedDateTime.now());
        parentRuleSet.addRule(siblingRule, 20, ZonedDateTime.now());

        RuleOrchestrator<TestKey> orchestrator = new RuleOrchestrator<>(parentRuleSet);
        RuleExecutionResult<TestKey> result = orchestrator.executeFirstMatchingRule(context);

        assertThat(result.hasMatches()).isTrue();
        assertThat(result.getFirstMatchedRule()).hasValue(siblingRule);
    }

    @Test
    void testExecuteAllMatchingRules_WithMatchingGateMergesChildActions() {
        RuleSet<TestKey> parentRuleSet = new RuleSet<>();
        RuleSet<TestKey> childRuleSet = new RuleSet<>();

        BusinessRule<TestKey> parentActionRule = createConcreteRule("ParentActionRule", ctx -> true);
        BusinessRule<TestKey> gateRule = createGateRule("GateRule", ctx -> true, childRuleSet, "/child.yaml");
        BusinessRule<TestKey> childActionRule = createConcreteRule("ChildActionRule", ctx -> true);

        childRuleSet.addRule(childActionRule, 10, ZonedDateTime.now());
        parentRuleSet.addRule(parentActionRule, 10, ZonedDateTime.now());
        parentRuleSet.addRule(gateRule, 20, ZonedDateTime.now());

        RuleOrchestrator<TestKey> orchestrator = new RuleOrchestrator<>(parentRuleSet);
        RuleExecutionResult<TestKey> result = orchestrator.executeAllMatchingRules(context);

        assertThat(result.hasMatches()).isTrue();
        assertThat(result.getMatchedRules()).containsExactly(parentActionRule, childActionRule);
        assertThat(result.getExecutedRules()).containsKeys(parentActionRule, childActionRule);
    }
    
    // ===== Dynamic Rule Tests =====

    @Test
    void testExecuteDynamicRules_ThrowsWhenNoParser() {
        // Create an orchestrator without a parser
        RuleOrchestrator<TestKey> orchestrator = new RuleOrchestrator<>(ruleSet);
        
        assertThatThrownBy(() -> orchestrator.executeDynamicRules(
                context, List.of("expr"), "testEntity"))
            .isInstanceOf(DynamicRuleValidationException.class)
            .hasMessageContaining("Parser not available");
    }
    
    @Test
    void testExecuteDynamicRuleSet_ThrowsWhenNoParser() {
        // Create an orchestrator without a parser
        RuleOrchestrator<TestKey> orchestrator = new RuleOrchestrator<>(ruleSet);
        DynamicRuleRequest<TestKey> request = new DynamicRuleRequest<>(
            "testEntity", "TestRuleSet", List.of("expr"));
        
        assertThatThrownBy(() -> orchestrator.executeDynamicRuleSet(context, request))
            .isInstanceOf(DynamicRuleValidationException.class)
            .hasMessageContaining("Parser not available");
    }
    
    @Test
    void testExecuteDynamicRuleSet_ThrowsForNullRequest() {
        @SuppressWarnings("unchecked")
        Parser<TestKey> mockParser = mock(Parser.class);
        RuleOrchestrator<TestKey> orchestrator = new RuleOrchestrator<>(ruleSet, mockParser);
        
        assertThatThrownBy(() -> orchestrator.executeDynamicRuleSet(context, null))
            .isInstanceOf(DynamicRuleValidationException.class)
            .hasMessageContaining("cannot be null");
    }
    
    @Test
    void testExecuteDynamicRuleSet_ThrowsForInvalidRequest() {
        @SuppressWarnings("unchecked")
        Parser<TestKey> mockParser = mock(Parser.class);
        RuleOrchestrator<TestKey> orchestrator = new RuleOrchestrator<>(ruleSet, mockParser);
        
        // Empty entity name makes the request invalid
        DynamicRuleRequest<TestKey> request = new DynamicRuleRequest<>(
            "", "TestRuleSet", List.of("expr"));
        
        assertThatThrownBy(() -> orchestrator.executeDynamicRuleSet(context, request))
            .isInstanceOf(DynamicRuleValidationException.class)
            .hasMessageContaining("Invalid dynamic rule request");
    }
    
    @Test
    void testExecuteDynamicRuleSet_ThrowsForRulesetNameMismatch() {
        @SuppressWarnings("unchecked")
        Parser<TestKey> mockParser = mock(Parser.class);
        
        // Use a fresh ruleset instead of the one from setUp to avoid action name validation on mock rules
        RuleSet<TestKey> freshRuleSet = new RuleSet<>();
        RuleSet.Metadata metadata = new RuleSet.Metadata();
        metadata.setRuleSetName("CorrectRulesetName");
        
        // Add a descriptor for the action in currentRule to pass validation
        BusinessActionDescriptor actionDescriptor = new BusinessActionDescriptor();
        actionDescriptor.setName("action1");
        metadata.setBusinessActionDescriptors(List.of(actionDescriptor));
        
        freshRuleSet.setMetadata(metadata);
        
        // Add a rule to pass "Rule set is empty" validation
        BusinessRule<TestKey> currentRule = createMockRule("Rule1", (RuleContext<TestKey> ctx) -> true);
        RuleFunction<TestKey> action = currentRule.getActions().get(0);
        when(action.getName()).thenReturn("action1");
        freshRuleSet.addRule(currentRule, 1, java.time.ZonedDateTime.now());
        
        RuleOrchestrator<TestKey> orchestrator = new RuleOrchestrator<>(freshRuleSet, mockParser);
        
        // Request targets a different ruleset than the orchestrator's
        DynamicRuleRequest<TestKey> request = new DynamicRuleRequest<>(
            "testEntity", "WrongRulesetName", List.of("expr"));
        
        assertThatThrownBy(() -> orchestrator.executeDynamicRuleSet(context, request))
            .isInstanceOf(DynamicRuleValidationException.class)
            .hasMessageContaining("does not match");
    }
    
    @Test
    void testExecuteDynamicRuleSet_ThrowsWhenDynamicExecutionNotAllowed() {
        @SuppressWarnings("unchecked")
        Parser<TestKey> mockParser = mock(Parser.class);
        
        // Create a ruleset with dynamic execution disabled (default)
        RuleSet<TestKey> dynamicRuleSet = new RuleSet<>();
        RuleSet.Metadata metadata = new RuleSet.Metadata();
        metadata.setRuleSetName("TestRuleSet");
        metadata.setAllowDynamicExecution(false);
        
        // Add a descriptor for the action in rule1 to pass validation
        BusinessActionDescriptor actionDescriptor = new BusinessActionDescriptor();
        actionDescriptor.setName("action1");
        metadata.setBusinessActionDescriptors(List.of(actionDescriptor));
        
        dynamicRuleSet.setMetadata(metadata);
        
        // Make rule1's action have a matching name
        BusinessRule<TestKey> dynamicRule = createMockRule("Rule1", (RuleContext<TestKey> ctx) -> true);
        RuleFunction<TestKey> action = dynamicRule.getActions().get(0);
        when(action.getName()).thenReturn("action1");
        
        dynamicRuleSet.addRule(dynamicRule, 10, java.time.ZonedDateTime.now());
        
        RuleOrchestrator<TestKey> orchestrator = new RuleOrchestrator<>(dynamicRuleSet, mockParser);
        
        DynamicRuleRequest<TestKey> request = new DynamicRuleRequest<>(
            "testEntity", "TestRuleSet", List.of("expr"));
        
        assertThatThrownBy(() -> orchestrator.executeDynamicRuleSet(context, request))
            .isInstanceOf(DynamicRuleValidationException.class)
            .hasMessageContaining("does not allow dynamic rule execution");
    }
    
    @Test
    void testConstructor_WithParser() {
        @SuppressWarnings("unchecked")
        Parser<TestKey> mockParser = mock(Parser.class);
        RuleOrchestrator<TestKey> orchestrator = new RuleOrchestrator<>(ruleSet, mockParser);
        
        assertThat(orchestrator.getRuleSet()).isEqualTo(ruleSet);
    }

    // Helper method to create a mock rule
    private BusinessRule<TestKey> createMockRule(String name, Expression<TestKey> condition) {

        @SuppressWarnings("unchecked")
        BusinessRule<TestKey> rule = mock(BusinessRule.class);
        when(rule.getName()).thenReturn(name);
        when(rule.getCondition()).thenReturn(Condition.asBoolean(condition));
        when(rule.isGateRule()).thenReturn(false);
        when(rule.isActionRule()).thenReturn(true);
        
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

    private BusinessRule<TestKey> createConcreteRule(String name, Expression<TestKey> condition) {
        @SuppressWarnings("unchecked")
        RuleFunction<TestKey> action = mock(RuleFunction.class);
        List<RuleFunction<TestKey>> actions = new ArrayList<>();
        actions.add(action);
        return new BusinessRule<>(name, Condition.asBoolean(condition), actions);
    }

    private BusinessRule<TestKey> createGateRule(
            String name,
            Expression<TestKey> condition,
            RuleSet<TestKey> childRuleSet,
            String forwardRef) {
        BusinessRule<TestKey> gateRule = new BusinessRule<>(name, Condition.asBoolean(condition), Collections.emptyList(), forwardRef);
        gateRule.setChildRuleSet(childRuleSet);
        return gateRule;
    }
}
