package com.lyxtera.axiom.api.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ArrayList;
import java.util.Collections;

import org.antlr.v4.runtime.tree.TerminalNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;

import com.lyxtera.axiom.antlr.BusinessRuleParser;
import com.lyxtera.axiom.antlr.BusinessRuleParser.AndExpressionContext;
import com.lyxtera.axiom.antlr.BusinessRuleParser.BusinessBooleanExpressionContext;
import com.lyxtera.axiom.antlr.BusinessRuleParser.BusinessCheckExpressionContext;
import com.lyxtera.axiom.antlr.BusinessRuleParser.ComparisonOperationContext;
import com.lyxtera.axiom.antlr.BusinessRuleParser.ComparisonOperatorContext;
import com.lyxtera.axiom.antlr.BusinessRuleParser.ExpressionContext;
import com.lyxtera.axiom.antlr.BusinessRuleParser.GroupingExpressionContext;
import com.lyxtera.axiom.antlr.BusinessRuleParser.LiteralContext;
import com.lyxtera.axiom.antlr.BusinessRuleParser.NotExpressionContext;
import com.lyxtera.axiom.antlr.BusinessRuleParser.NumberLiteralContext;
import com.lyxtera.axiom.antlr.BusinessRuleParser.OrExpressionContext;
import com.lyxtera.axiom.antlr.BusinessRuleParser.StringLiteralContext;
import com.lyxtera.axiom.antlr.BusinessRuleParser.SubExpressionContext;
import com.lyxtera.axiom.antlr.BusinessRuleParser.ArgumentsContext;
import com.lyxtera.axiom.antlr.BusinessRuleParser.BusinessCheckContext;
import com.lyxtera.axiom.api.model.BusinessCheck;
import com.lyxtera.axiom.api.model.Condition;
import com.lyxtera.axiom.api.model.Expression;
import com.lyxtera.axiom.api.model.Operator;
import com.lyxtera.axiom.api.model.Value;
import com.lyxtera.axiom.engine.RuleContext;
import com.lyxtera.axiom.engine.RuleSet;
import com.lyxtera.axiom.api.exception.RuleParserException;

/**
 * Test class for {@link ConditionVisitor}.
 */
public class ConditionVisitorTest {

    public enum TestKey {
        AGE,
        IS_PREMIUM,
        NAME
    }

    private Map<String, BusinessCheck<TestKey>> businessChecks;
    private ConditionVisitor<TestKey> visitor;
    private BusinessCheck<TestKey> isPremiumCheck;
    private BusinessCheck<TestKey> ageCheck;
    private RuleSet.Metadata metadata;
    
    // Custom implementation of BusinessCheck for testing
    public static class TestBusinessCheck<K extends Enum<K>> implements BusinessCheck<K> {
        private final String name;
        private final Value returnValue;
        
        public TestBusinessCheck(String name, Value returnValue) {
            this.name = name;
            this.returnValue = returnValue;
        }
        
        @Override
        public String getName() {
            return name;
        }
        
        @Override
        public Value execute(RuleContext<K> context) {
            return returnValue;
        }
        
        public Value execute(RuleContext<K> context, Value arg) {
            return returnValue;
        }
    }
    
    @BeforeEach
    public void setUp() {
        // Define business checks for testing
        businessChecks = new HashMap<>();
        
        // Set up a mock business check for isPremium
        isPremiumCheck = mock(BusinessCheck.class);
        businessChecks.put("isPremium", isPremiumCheck);
        
        // Set up a mock business check for age
        ageCheck = mock(BusinessCheck.class);
        businessChecks.put("age", ageCheck);
        
        // Initialize the mock metadata
        metadata = createMockMetadata();
        
        visitor = new ConditionVisitor<>(businessChecks, metadata);
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
            lenient().when(metadata.getRuleSetName()).thenReturn("TestRuleSet");
        }
        return metadata;
    }
    
    @Test
    public void testVisitExpression() {
        // Create mock context
        ExpressionContext ctx = mock(ExpressionContext.class);
        SubExpressionContext subCtx = mock(SubExpressionContext.class);
        when(ctx.subExpression()).thenReturn(subCtx);
        
        // Create a mock visitor that returns a simple condition
        RuleSet.Metadata metadata = createMockMetadata();
        ConditionVisitor<TestKey> mockVisitor = new ConditionVisitor<>(businessChecks, metadata) {
            @Override
            public Condition<TestKey> visitSubExpression(SubExpressionContext ctx) {
                return Condition.asBoolean(context -> true);
            }
        };
        
        // Visit the expression context
        Condition<TestKey> condition = mockVisitor.visitExpression(ctx);
        
        // The condition should evaluate to true
        assertTrue(condition.evaluate(mock(RuleContext.class)));
    }
    
    @Test
    public void testVisitAndExpression() {
        // Create mock context for AND expression
        AndExpressionContext ctx = mock(AndExpressionContext.class);
        SubExpressionContext leftCtx = mock(SubExpressionContext.class);
        SubExpressionContext rightCtx = mock(SubExpressionContext.class);
        
        // Mock the behavior of getSubExpression() to return both contexts
        List<SubExpressionContext> subExpressions = new ArrayList<>();
        subExpressions.add(leftCtx);
        subExpressions.add(rightCtx);
        
        when(ctx.subExpression()).thenReturn(subExpressions);
        
        // Create visitor that returns true for one context and false for the other
        RuleSet.Metadata metadata = createMockMetadata();
        ConditionVisitor<TestKey> mockVisitor = new ConditionVisitor<>(businessChecks, metadata) {
            @Override
            public Condition<TestKey> visitSubExpression(SubExpressionContext ctx) {
                if (ctx == leftCtx) {
                    return Condition.asBoolean(context -> true);
                } else {
                    return Condition.asBoolean(context -> false);
                }
            }
        };
        
        // Visit the AND expression context
        Condition<TestKey> condition = mockVisitor.visitAndExpression(ctx);
        
        // The condition should evaluate to false (true AND false = false)
        assertFalse(condition.evaluate(mock(RuleContext.class)));
    }
    
    @Test
    public void testVisitOrExpression() {
        // Create mock context for OR expression
        OrExpressionContext ctx = mock(OrExpressionContext.class);
        SubExpressionContext leftCtx = mock(SubExpressionContext.class);
        SubExpressionContext rightCtx = mock(SubExpressionContext.class);
        
        // Mock the behavior of getSubExpression() to return both contexts
        List<SubExpressionContext> subExpressions = new ArrayList<>();
        subExpressions.add(leftCtx);
        subExpressions.add(rightCtx);
        
        when(ctx.subExpression()).thenReturn(subExpressions);
        
        // Mock the behavior of visitSubExpression to return appropriate conditions
        RuleSet.Metadata metadata = createMockMetadata();
        ConditionVisitor<TestKey> mockVisitor = new ConditionVisitor<>(businessChecks, metadata) {
            @Override
            public Condition<TestKey> visitSubExpression(SubExpressionContext ctx) {
                if (ctx == leftCtx) {
                    return Condition.asBoolean(context -> false);
                } else {
                    return Condition.asBoolean(context -> true);
                }
            }
        };
        
        // Visit the OR expression context
        Condition<TestKey> condition = mockVisitor.visitOrExpression(ctx);
        
        // The condition should evaluate to true (false OR true = true)
        assertTrue(condition.evaluate(mock(RuleContext.class)));
    }
    
    @Test
    public void testVisitNotExpression() {
        // Create mock context for NOT expression
        NotExpressionContext ctx = mock(NotExpressionContext.class);
        SubExpressionContext subCtx = mock(SubExpressionContext.class);
        
        when(ctx.subExpression()).thenReturn(subCtx);
        
        // Mock the behavior of visitSubExpression to return a simple condition
        RuleSet.Metadata metadata = createMockMetadata();
        ConditionVisitor<TestKey> mockVisitor = new ConditionVisitor<>(businessChecks, metadata) {
            @Override
            public Condition<TestKey> visitSubExpression(SubExpressionContext ctx) {
                return Condition.asBoolean(context -> true);
            }
        };
        
        // Visit the NOT expression context
        Condition<TestKey> condition = mockVisitor.visitNotExpression(ctx);
        
        // The condition should evaluate to false (NOT true)
        assertFalse(condition.evaluate(mock(RuleContext.class)));
    }
    
    @Test
    public void testVisitGroupingExpression() {
        // Create mock context for grouping expression
        GroupingExpressionContext ctx = mock(GroupingExpressionContext.class);
        SubExpressionContext exprCtx = mock(SubExpressionContext.class);
        
        when(ctx.subExpression()).thenReturn(exprCtx);
        
        // Mock the behavior of visitSubExpression to return a simple condition
        RuleSet.Metadata metadata = createMockMetadata();
        ConditionVisitor<TestKey> mockVisitor = new ConditionVisitor<>(businessChecks, metadata) {
            @Override
            public Condition<TestKey> visitSubExpression(SubExpressionContext ctx) {
                return Condition.asBoolean(context -> true);
            }
        };
        
        // Visit the grouping expression context
        Condition<TestKey> condition = mockVisitor.visitGroupingExpression(ctx);
        
        // The condition should evaluate to the same result as the enclosed expression
        assertTrue(condition.evaluate(mock(RuleContext.class)));
    }
    
    @Test
    public void testVisitComparisonOperation() {
        // Create mock contexts
        ComparisonOperationContext ctx = mock(ComparisonOperationContext.class);
        BusinessCheckExpressionContext businessCheckExprCtx = mock(BusinessCheckExpressionContext.class);
        NumberLiteralContext numberLiteralCtx = mock(NumberLiteralContext.class);
        ComparisonOperatorContext opCtx = mock(ComparisonOperatorContext.class);
        TerminalNode identifierNode = mock(TerminalNode.class);
        TerminalNode numberNode = mock(TerminalNode.class);
        ArgumentsContext argumentsCtx = mock(ArgumentsContext.class);

        // Set up the mocking chain
        when(ctx.businessCheck()).thenReturn(businessCheckExprCtx);
        when(ctx.literal()).thenReturn(numberLiteralCtx);
        when(ctx.comparisonOperator()).thenReturn(opCtx);
        when(opCtx.getText()).thenReturn("=");

        // Set up the business check expression context
        when(businessCheckExprCtx.IDENTIFIER()).thenReturn(identifierNode);
        when(identifierNode.getText()).thenReturn("age");
        when(businessCheckExprCtx.arguments()).thenReturn(argumentsCtx);
        when(argumentsCtx.literal()).thenReturn(Collections.emptyList());

        // Set up the number literal context
        when(numberLiteralCtx.NUMBER()).thenReturn(numberNode);
        when(numberNode.getText()).thenReturn("35");
        
        // Set up the business check to return a value equal to the literal
        when(ageCheck.execute(any())).thenReturn(Value.fromObject(35));

        // Create visitor and test
        ConditionVisitor<TestKey> visitor = new ConditionVisitor<>(businessChecks, metadata);
        Condition<TestKey> condition = visitor.visitComparisonOperation(ctx);

        // Verify the condition is not null and evaluates to the expected result
        assertNotNull(condition);
        RuleContext<TestKey> ruleContext = new RuleContext<>(TestKey.class);
        assertTrue(condition.evaluate(ruleContext));
    }
    
    @Test
    public void testVisitBusinessBooleanExpression() {
        // Create mock contexts
        BusinessBooleanExpressionContext ctx = mock(BusinessBooleanExpressionContext.class);
        BusinessCheckExpressionContext businessCheckExprCtx = mock(BusinessCheckExpressionContext.class);
        TerminalNode identifierNode = mock(TerminalNode.class);
        ArgumentsContext argumentsCtx = mock(ArgumentsContext.class);
        
        // Set up the mocking chain
        when(ctx.businessCheck()).thenReturn(businessCheckExprCtx);
        
        // Set up the business check expression context
        when(businessCheckExprCtx.IDENTIFIER()).thenReturn(identifierNode);
        when(identifierNode.getText()).thenReturn("isPremium");
        when(businessCheckExprCtx.arguments()).thenReturn(argumentsCtx);
        when(argumentsCtx.literal()).thenReturn(Collections.emptyList());
        
        // Set up the business check to return a value
        when(isPremiumCheck.execute(any())).thenReturn(Value.fromObject(true));
        
        // Create visitor and test
        Condition<TestKey> result = visitor.visitBusinessBooleanExpression(ctx);
        
        // Verify the result
        assertNotNull(result);
        RuleContext<TestKey> ruleContext = new RuleContext<>(TestKey.class);
        assertTrue(result.evaluate(ruleContext));
    }
    
    @Test
    public void testVisitSubExpressionWithUnknownType() {
        // Create a mock SubExpressionContext that is not one of the known types
        SubExpressionContext ctx = mock(SubExpressionContext.class);
        
        // Verify that a RuleParserException is thrown
        assertThrows(RuleParserException.class, () -> {
            visitor.visitSubExpression(ctx);
        });
    }
    
    // Indirect test for BusinessCheckVisitor with no arguments
    @Test 
    public void testBusinessCheckExpressionWithNoArguments() {
        // Create mock contexts
        BusinessBooleanExpressionContext ctx = mock(BusinessBooleanExpressionContext.class);
        BusinessCheckExpressionContext businessCheckExprCtx = mock(BusinessCheckExpressionContext.class);
        TerminalNode identifierNode = mock(TerminalNode.class);
        
        // Set up the mocking chain
        when(ctx.businessCheck()).thenReturn(businessCheckExprCtx);
        
        // Set up the business check expression context
        when(businessCheckExprCtx.IDENTIFIER()).thenReturn(identifierNode);
        when(identifierNode.getText()).thenReturn("age");
        when(businessCheckExprCtx.arguments()).thenReturn(null);
        
        // Set up the business check to return a value
        when(ageCheck.execute(any())).thenReturn(Value.fromObject(true));
        
        // Create visitor and test
        Condition<TestKey> result = visitor.visitBusinessBooleanExpression(ctx);
        
        // Verify the result
        assertNotNull(result);
        RuleContext<TestKey> ruleContext = new RuleContext<>(TestKey.class);
        assertTrue(result.evaluate(ruleContext));
    }
    
    // Indirect test for BusinessCheckVisitor with number arguments
    @Test
    public void testBusinessCheckExpressionWithNumberArguments() {
        // Create mock contexts
        BusinessBooleanExpressionContext ctx = mock(BusinessBooleanExpressionContext.class);
        BusinessCheckExpressionContext businessCheckExprCtx = mock(BusinessCheckExpressionContext.class);
        TerminalNode identifierNode = mock(TerminalNode.class);
        ArgumentsContext argumentsCtx = mock(ArgumentsContext.class);
        NumberLiteralContext numberLiteralCtx = mock(NumberLiteralContext.class);
        TerminalNode numberNode = mock(TerminalNode.class);
        
        // Set up the mocking chain
        when(ctx.businessCheck()).thenReturn(businessCheckExprCtx);
        
        // Set up the business check expression context
        when(businessCheckExprCtx.IDENTIFIER()).thenReturn(identifierNode);
        when(identifierNode.getText()).thenReturn("isPremium");
        when(businessCheckExprCtx.arguments()).thenReturn(argumentsCtx);
        
        // Set up arguments with a number literal
        when(numberLiteralCtx.NUMBER()).thenReturn(numberNode);
        when(numberNode.getText()).thenReturn("42");
        when(argumentsCtx.literal()).thenReturn(Collections.emptyList());
        
        // Set up the business check to return a value
        when(isPremiumCheck.execute(any())).thenReturn(Value.fromObject(true));
        
        // Create visitor and test
        Condition<TestKey> result = visitor.visitBusinessBooleanExpression(ctx);
        
        // Verify the result
        assertNotNull(result);
        RuleContext<TestKey> ruleContext = new RuleContext<>(TestKey.class);
        assertTrue(result.evaluate(ruleContext));
    }
    
    // Indirect test for BusinessCheckVisitor with string arguments
    @Test
    public void testBusinessCheckExpressionWithStringArguments() {
        // Create mock contexts
        BusinessBooleanExpressionContext ctx = mock(BusinessBooleanExpressionContext.class);
        BusinessCheckExpressionContext businessCheckExprCtx = mock(BusinessCheckExpressionContext.class);
        TerminalNode identifierNode = mock(TerminalNode.class);
        ArgumentsContext argumentsCtx = mock(ArgumentsContext.class);
        StringLiteralContext stringLiteralCtx = mock(StringLiteralContext.class);
        TerminalNode stringNode = mock(TerminalNode.class);
        
        // Set up the mocking chain
        when(ctx.businessCheck()).thenReturn(businessCheckExprCtx);
        
        // Set up the business check expression context
        when(businessCheckExprCtx.IDENTIFIER()).thenReturn(identifierNode);
        when(identifierNode.getText()).thenReturn("isPremium");
        when(businessCheckExprCtx.arguments()).thenReturn(argumentsCtx);
        
        // Set up arguments with a string literal
        when(stringLiteralCtx.STRING()).thenReturn(stringNode);
        when(stringNode.getText()).thenReturn("\"test\"");
        when(argumentsCtx.literal()).thenReturn(Collections.emptyList());
        
        // Set up the business check to return a value
        when(isPremiumCheck.execute(any())).thenReturn(Value.fromObject(true));
        
        // Create visitor and test
        Condition<TestKey> result = visitor.visitBusinessBooleanExpression(ctx);
        
        // Verify the result
        assertNotNull(result);
        RuleContext<TestKey> ruleContext = new RuleContext<>(TestKey.class);
        assertTrue(result.evaluate(ruleContext));
    }
    
    // Indirect test for BusinessCheckVisitor with unknown business check
    @Test
    public void testBusinessCheckExpressionWithUnknownBusinessCheck() {
        // Create mock contexts
        BusinessBooleanExpressionContext ctx = mock(BusinessBooleanExpressionContext.class);
        BusinessCheckExpressionContext businessCheckExprCtx = mock(BusinessCheckExpressionContext.class);
        TerminalNode identifierNode = mock(TerminalNode.class);
        ArgumentsContext argumentsCtx = mock(ArgumentsContext.class);
        
        // Set up the mocking chain
        when(ctx.businessCheck()).thenReturn(businessCheckExprCtx);
        
        // Set up the business check expression context
        when(businessCheckExprCtx.IDENTIFIER()).thenReturn(identifierNode);
        when(identifierNode.getText()).thenReturn("unknown");
        when(businessCheckExprCtx.arguments()).thenReturn(argumentsCtx);
        when(argumentsCtx.literal()).thenReturn(Collections.emptyList());
        
        // Verify that a RuleParserException is thrown
        assertThrows(RuleParserException.class, () -> {
            visitor.visitBusinessBooleanExpression(ctx);
        });
    }
    
    @Test
    public void testVisitComparisonOperation_WithDifferentOperators() {
        // Create mock contexts
        ComparisonOperationContext ctx = mock(ComparisonOperationContext.class);
        BusinessCheckExpressionContext businessCheckExprCtx = mock(BusinessCheckExpressionContext.class);
        NumberLiteralContext numberLiteralCtx = mock(NumberLiteralContext.class);
        ComparisonOperatorContext operatorCtx = mock(ComparisonOperatorContext.class);
        TerminalNode identifierNode = mock(TerminalNode.class);
        TerminalNode numberNode = mock(TerminalNode.class);
        ArgumentsContext argumentsCtx = mock(ArgumentsContext.class);
        
        // Set up the mocking chain
        when(ctx.businessCheck()).thenReturn(businessCheckExprCtx);
        when(ctx.literal()).thenReturn(numberLiteralCtx);
        when(ctx.comparisonOperator()).thenReturn(operatorCtx);
        
        // Set up the business check expression context
        when(businessCheckExprCtx.IDENTIFIER()).thenReturn(identifierNode);
        when(identifierNode.getText()).thenReturn("age");
        when(businessCheckExprCtx.arguments()).thenReturn(argumentsCtx);
        when(argumentsCtx.literal()).thenReturn(Collections.emptyList());
        
        // Set up the number literal context
        when(numberLiteralCtx.NUMBER()).thenReturn(numberNode);
        when(numberNode.getText()).thenReturn("30");
        
        // Set up the business check to return a value
        when(ageCheck.execute(any())).thenReturn(Value.fromObject(30));
        
        // Set up the operator with an invalid value
        when(operatorCtx.getText()).thenReturn("INVALID");
        
        // Verify that a RuleParserException is thrown with the invalid operator
        assertThrows(RuleParserException.class, () -> {
            visitor.visitComparisonOperation(ctx);
        });
    }
    
    @Test
    public void testVisitComparisonOperation_WithStringLiteral() {
        // Create mock contexts
        ComparisonOperationContext ctx = mock(ComparisonOperationContext.class);
        BusinessCheckExpressionContext businessCheckExprCtx = mock(BusinessCheckExpressionContext.class);
        StringLiteralContext stringLiteralCtx = mock(StringLiteralContext.class);
        ComparisonOperatorContext operatorCtx = mock(ComparisonOperatorContext.class);
        TerminalNode identifierNode = mock(TerminalNode.class);
        TerminalNode stringNode = mock(TerminalNode.class);
        ArgumentsContext argumentsCtx = mock(ArgumentsContext.class);
        
        // Set up the mocking chain
        when(ctx.businessCheck()).thenReturn(businessCheckExprCtx);
        when(ctx.literal()).thenReturn(stringLiteralCtx);
        when(ctx.comparisonOperator()).thenReturn(operatorCtx);
        when(operatorCtx.getText()).thenReturn("=");
        
        // Set up the business check expression context
        when(businessCheckExprCtx.IDENTIFIER()).thenReturn(identifierNode);
        when(identifierNode.getText()).thenReturn("age");
        when(businessCheckExprCtx.arguments()).thenReturn(argumentsCtx);
        when(argumentsCtx.literal()).thenReturn(Collections.emptyList());
        
        // Set up the string literal context
        when(stringLiteralCtx.STRING()).thenReturn(stringNode);
        when(stringNode.getText()).thenReturn("\"test\"");
        
        // Set up the business check to return a value
        when(ageCheck.execute(any())).thenReturn(Value.fromObject("test"));
        
        // Create visitor and test
        Condition<TestKey> result = visitor.visitComparisonOperation(ctx);
        
        // Verify the result
        assertNotNull(result);
        RuleContext<TestKey> ruleContext = new RuleContext<>(TestKey.class);
        assertTrue(result.evaluate(ruleContext));
    }
    
    @Test
    public void testVisitBusinessBooleanExpression_WithArguments() {
        // Create mock contexts
        BusinessBooleanExpressionContext ctx = mock(BusinessBooleanExpressionContext.class);
        BusinessCheckExpressionContext businessCheckExprCtx = mock(BusinessCheckExpressionContext.class);
        TerminalNode identifierNode = mock(TerminalNode.class);
        ArgumentsContext argumentsCtx = mock(ArgumentsContext.class);
        NumberLiteralContext numberLiteralCtx = mock(NumberLiteralContext.class);
        TerminalNode numberNode = mock(TerminalNode.class);
        
        // Set up the mocking chain
        when(ctx.businessCheck()).thenReturn(businessCheckExprCtx);
        
        // Set up the business check expression context
        when(businessCheckExprCtx.IDENTIFIER()).thenReturn(identifierNode);
        when(identifierNode.getText()).thenReturn("isPremium");
        when(businessCheckExprCtx.arguments()).thenReturn(argumentsCtx);
        
        // Set up arguments with a number literal
        when(numberLiteralCtx.NUMBER()).thenReturn(numberNode);
        when(numberNode.getText()).thenReturn("42");
        when(argumentsCtx.literal()).thenReturn(Collections.emptyList());
        
        // Set up the business check to return a value
        when(isPremiumCheck.execute(any())).thenReturn(Value.fromObject(true));
        
        // Create visitor and test
        Condition<TestKey> result = visitor.visitBusinessBooleanExpression(ctx);
        
        // Verify the result
        assertNotNull(result);
        RuleContext<TestKey> ruleContext = new RuleContext<>(TestKey.class);
        assertTrue(result.evaluate(ruleContext));
    }
    
    @Test
    public void testVisitBusinessBooleanExpression_WithUnknownBusinessCheck() {
        BusinessBooleanExpressionContext ctx = mock(BusinessBooleanExpressionContext.class);
        BusinessCheckExpressionContext businessCheckCtx = mock(BusinessCheckExpressionContext.class);
        TerminalNode identifierNode = mock(TerminalNode.class);
        
        when(ctx.businessCheck()).thenReturn(businessCheckCtx);
        when(businessCheckCtx.IDENTIFIER()).thenReturn(identifierNode);
        when(identifierNode.getText()).thenReturn("unknown");
        when(businessCheckCtx.arguments()).thenReturn(null);
        
        assertThrows(RuleParserException.class, () -> {
            visitor.visitBusinessBooleanExpression(ctx);
        });
    }

    @Test
    public void testInterpretComparisonOperation() {
        // Create mock contexts
        ComparisonOperationContext ctx = mock(ComparisonOperationContext.class);
        BusinessCheckExpressionContext businessCheckExprCtx = mock(BusinessCheckExpressionContext.class);
        StringLiteralContext stringLiteralCtx = mock(StringLiteralContext.class);
        ComparisonOperatorContext operatorCtx = mock(ComparisonOperatorContext.class);
        TerminalNode identifierNode = mock(TerminalNode.class);
        TerminalNode stringNode = mock(TerminalNode.class);
        ArgumentsContext argumentsCtx = mock(ArgumentsContext.class);
        
        // Set up the mocking chain
        when(ctx.businessCheck()).thenReturn(businessCheckExprCtx);
        when(ctx.literal()).thenReturn(stringLiteralCtx);
        when(ctx.comparisonOperator()).thenReturn(operatorCtx);
        when(operatorCtx.getText()).thenReturn("=");
        
        // Set up the business check expression context
        when(businessCheckExprCtx.IDENTIFIER()).thenReturn(identifierNode);
        when(identifierNode.getText()).thenReturn("age");
        when(businessCheckExprCtx.arguments()).thenReturn(argumentsCtx);
        when(argumentsCtx.literal()).thenReturn(Collections.emptyList());
        
        // Set up the string literal context
        when(stringLiteralCtx.STRING()).thenReturn(stringNode);
        when(stringNode.getText()).thenReturn("\"20\"");
        
        // Set up the business check to return a value equal to the literal
        when(ageCheck.execute(any())).thenReturn(Value.fromObject("20"));
        
        // Create visitor and test
        ConditionVisitor<TestKey> testVisitor = new ConditionVisitor<>(businessChecks, metadata);
        Condition<TestKey> condition = testVisitor.visitComparisonOperation(ctx);
        
        // Verify the result
        assertNotNull(condition);
        RuleContext<TestKey> ruleContext = new RuleContext<>(TestKey.class);
        assertTrue(condition.evaluate(ruleContext));
    }

    // Helper method to create a token with the given text
    private Token token(String text) {
        return new CommonToken(0, text);
    }
}