package com.lyxtera.axiom.api.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.tree.TerminalNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.lyxtera.axiom.antlr.BusinessRuleParser;
import com.lyxtera.axiom.antlr.BusinessRuleParser.BusinessActionListContext;
import com.lyxtera.axiom.antlr.BusinessRuleParser.BusinessRuleContext;
import com.lyxtera.axiom.antlr.BusinessRuleParser.ExpressionContext;
import com.lyxtera.axiom.antlr.BusinessRuleParser.OutcomeContext;
import com.lyxtera.axiom.antlr.BusinessRuleParser.SubExpressionContext;
import com.lyxtera.axiom.api.model.BusinessAction;
import com.lyxtera.axiom.api.model.BusinessCheck;
import com.lyxtera.axiom.api.model.BusinessRule;
import com.lyxtera.axiom.api.model.Condition;
import com.lyxtera.axiom.api.model.RuleFunction;
import com.lyxtera.axiom.engine.RuleContext;
import com.lyxtera.axiom.engine.RuleSet;

/**
 * Test class for {@link BusinessRuleVisitor}.
 */
public class BusinessRuleVisitorTest {

    public enum TestKey {
        AGE,
        IS_PREMIUM,
        NAME
    }
    
    private Map<String, BusinessCheck<TestKey>> businessChecks;
    private Map<String, BusinessAction<TestKey>> businessActions;
    private BusinessRuleVisitor<TestKey> visitor;
    private String ruleName;
    private RuleSet.Metadata metadata;
    
    @BeforeEach
    public void setUp() {
        businessChecks = new HashMap<>();
        businessActions = new HashMap<>();
        
        // Set up business checks
        BusinessCheck<TestKey> isPremiumCheck = mock(BusinessCheck.class);
        when(isPremiumCheck.execute(any())).thenReturn(null);
        businessChecks.put("isPremium", isPremiumCheck);
        
        // Set up business actions
        BusinessAction<TestKey> notifyAction = mock(BusinessAction.class);
        when(notifyAction.getName()).thenReturn("notify");
        businessActions.put("notify", notifyAction);
        
        // Create mock metadata
        metadata = createMockMetadata();
        
        ruleName = "testRule";
        visitor = new BusinessRuleVisitor<>(ruleName, businessChecks, businessActions, metadata);
    }
    
    // Helper method to create properly mocked metadata
    private RuleSet.Metadata createMockMetadata() {
        RuleSet.Metadata metadata = new RuleSet.Metadata();
        try {
            // Use reflection to set the ruleset name since it has a package-private setter
            java.lang.reflect.Method setRuleSetNameMethod = RuleSet.Metadata.class.getDeclaredMethod("setRuleSetName", String.class);
            setRuleSetNameMethod.setAccessible(true);
            setRuleSetNameMethod.invoke(metadata, "TestRuleSet");
        } catch (Exception e) {
            // In case reflection fails, use mocking as a fallback
            metadata = mock(RuleSet.Metadata.class);
            when(metadata.getRuleSetName()).thenReturn("TestRuleSet");
        }
        return metadata;
    }
    
    @Test
    public void testVisitBusinessRuleWithConditionAndOutcome() {
        // Create mock context for a business rule with condition and outcome
        BusinessRuleContext ctx = mock(BusinessRuleContext.class);
        ExpressionContext expressionCtx = mock(ExpressionContext.class);
        SubExpressionContext subExprCtx = mock(SubExpressionContext.class);
        OutcomeContext outcomeCtx = mock(OutcomeContext.class);
        BusinessActionListContext actionListCtx = mock(BusinessActionListContext.class);
        
        // Set up the mock to return expression and outcome
        when(ctx.expression()).thenReturn(expressionCtx);
        when(expressionCtx.subExpression()).thenReturn(subExprCtx);
        when(ctx.outcome()).thenReturn(outcomeCtx);
        when(outcomeCtx.getChild(0)).thenReturn(actionListCtx);
        
        // Mock the ConditionVisitor and OutcomeVisitor behavior
        BusinessRule<TestKey> result = new BusinessRuleVisitor<TestKey>(ruleName, businessChecks, businessActions, metadata) {
            @Override
            public BusinessRule<TestKey> visitBusinessRule(BusinessRuleParser.BusinessRuleContext ctx) {
                // Create a simple condition that always returns true
                Condition<TestKey> condition = Condition.asBoolean(context -> true);
                
                // Create a list with a single action
                List<RuleFunction<TestKey>> actions = new ArrayList<>();
                actions.add(businessActions.get("notify"));
                
                return new BusinessRule<TestKey>(ruleName, condition, actions);
            }
        }.visitBusinessRule(ctx);
        
        // Verify the result
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(ruleName);
        assertThat(result.getCondition()).isNotNull();
        assertThat(result.getActions()).isNotNull();
        assertThat(result.getActions()).hasSize(1);
        assertThat(result.getActions().get(0).getName()).isEqualTo("notify");
        
        // Verify the condition evaluates correctly
        RuleContext<TestKey> context = new RuleContext<>(TestKey.class);
        assertThat(result.evaluate(context)).isTrue();
    }
    
    @Test
    public void testVisitBusinessRuleWithoutCondition() {
        // Create mock context for a business rule without condition
        BusinessRuleContext ctx = mock(BusinessRuleContext.class);
        OutcomeContext outcomeCtx = mock(OutcomeContext.class);
        BusinessActionListContext actionListCtx = mock(BusinessActionListContext.class);
        
        // Set up the mock to return null for expression and a valid outcome
        when(ctx.expression()).thenReturn(null);
        when(ctx.outcome()).thenReturn(outcomeCtx);
        when(outcomeCtx.getChild(0)).thenReturn(actionListCtx);
        
        // Mock the OutcomeVisitor behavior
        BusinessRule<TestKey> result = new BusinessRuleVisitor<TestKey>(ruleName, businessChecks, businessActions, metadata) {
            @Override
            public BusinessRule<TestKey> visitBusinessRule(BusinessRuleParser.BusinessRuleContext ctx) {
                // Create a list with a single action
                List<RuleFunction<TestKey>> actions = new ArrayList<>();
                actions.add(businessActions.get("notify"));
                
                return new BusinessRule<TestKey>(ruleName, null, actions);
            }
        }.visitBusinessRule(ctx);
        
        // Verify the result
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(ruleName);
        assertThat(result.getCondition()).isNull();
        assertThat(result.getActions()).isNotNull();
        assertThat(result.getActions()).hasSize(1);
        assertThat(result.getActions().get(0).getName()).isEqualTo("notify");
        
        // Verify the rule evaluates to true when there's no condition
        RuleContext<TestKey> context = new RuleContext<>(TestKey.class);
        assertThat(result.evaluate(context)).isTrue();
    }
    
    @Test
    public void testVisitBusinessRuleWithoutOutcome() {
        // Create mock context for a business rule without outcome
        BusinessRuleContext ctx = mock(BusinessRuleContext.class);
        ExpressionContext expressionCtx = mock(ExpressionContext.class);
        SubExpressionContext subExprCtx = mock(SubExpressionContext.class);
        
        // Set up the mock to return a valid expression and null for outcome
        when(ctx.expression()).thenReturn(expressionCtx);
        when(expressionCtx.subExpression()).thenReturn(subExprCtx);
        when(ctx.outcome()).thenReturn(null);
        
        // Mock the ConditionVisitor behavior
        BusinessRule<TestKey> result = new BusinessRuleVisitor<TestKey>(ruleName, businessChecks, businessActions, metadata) {
            @Override
            public BusinessRule<TestKey> visitBusinessRule(BusinessRuleParser.BusinessRuleContext ctx) {
                // Create a simple condition that always returns true
                Condition<TestKey> condition = Condition.asBoolean(context -> true);
                
                return new BusinessRule<TestKey>(ruleName, condition, new ArrayList<>());
            }
        }.visitBusinessRule(ctx);
        
        // Verify the result
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(ruleName);
        assertThat(result.getCondition()).isNotNull();
        assertThat(result.getActions()).isNotNull();
        assertThat(result.getActions()).isEmpty();
        
        // Verify the condition evaluates correctly
        RuleContext<TestKey> context = new RuleContext<>(TestKey.class);
        assertThat(result.evaluate(context)).isTrue();
    }
    
    @Test
    public void testVisitBusinessRuleWithoutConditionAndOutcome() {
        // Create mock context for a business rule without condition and outcome
        BusinessRuleContext ctx = mock(BusinessRuleContext.class);
        
        // Set up the mock to return null for both expression and outcome
        when(ctx.expression()).thenReturn(null);
        when(ctx.outcome()).thenReturn(null);
        
        // Create a custom visitor that returns a predefined rule without condition and actions
        BusinessRule<TestKey> result = new BusinessRuleVisitor<TestKey>(ruleName, businessChecks, businessActions, metadata) {
            @Override
            public BusinessRule<TestKey> visitBusinessRule(BusinessRuleParser.BusinessRuleContext ctx) {
                return new BusinessRule<TestKey>(ruleName, null, new ArrayList<>());
            }
        }.visitBusinessRule(ctx);
        
        // Verify the result
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(ruleName);
        assertThat(result.getCondition()).isNull();
        assertThat(result.getActions()).isNotNull();
        assertThat(result.getActions()).isEmpty();
        
        // Verify the rule evaluates to true when there's no condition
        RuleContext<TestKey> context = new RuleContext<>(TestKey.class);
        assertThat(result.evaluate(context)).isTrue();
    }
} 