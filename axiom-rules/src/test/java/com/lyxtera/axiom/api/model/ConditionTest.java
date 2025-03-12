package com.lyxtera.axiom.api.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.lyxtera.axiom.engine.RuleContext;

/**
 * Test class for {@link Condition} and {@link Expression}.
 */
public class ConditionTest {

    public enum TestKey {
        TEST_KEY
    }

    @Mock
    private RuleContext<TestKey> context;

    @Mock
    private BusinessCheck<TestKey> businessCheck;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testAsLogicalExpressionAnd() {
        // Create two expressions that return true and false
        Expression<TestKey> trueExpression = ctx -> true;
        Expression<TestKey> falseExpression = ctx -> false;

        // Test AND operator
        Condition<TestKey> andCondition = Condition.asLogicalExpression(trueExpression, Operator.AND, trueExpression);
        assertTrue(andCondition.evaluate(context));

        andCondition = Condition.asLogicalExpression(trueExpression, Operator.AND, falseExpression);
        assertFalse(andCondition.evaluate(context));

        andCondition = Condition.asLogicalExpression(falseExpression, Operator.AND, trueExpression);
        assertFalse(andCondition.evaluate(context));

        andCondition = Condition.asLogicalExpression(falseExpression, Operator.AND, falseExpression);
        assertFalse(andCondition.evaluate(context));

        // Verify getters
        assertEquals(trueExpression, Condition.asLogicalExpression(trueExpression, Operator.AND, falseExpression).getLeft());
        assertEquals(Operator.AND, Condition.asLogicalExpression(trueExpression, Operator.AND, falseExpression).getOperator());
        assertEquals(falseExpression, Condition.asLogicalExpression(trueExpression, Operator.AND, falseExpression).getRight());
    }

    @Test
    public void testAsLogicalExpressionOr() {
        // Create two expressions that return true and false
        Expression<TestKey> trueExpression = ctx -> true;
        Expression<TestKey> falseExpression = ctx -> false;

        // Test OR operator
        Condition<TestKey> orCondition = Condition.asLogicalExpression(trueExpression, Operator.OR, trueExpression);
        assertTrue(orCondition.evaluate(context));

        orCondition = Condition.asLogicalExpression(trueExpression, Operator.OR, falseExpression);
        assertTrue(orCondition.evaluate(context));

        orCondition = Condition.asLogicalExpression(falseExpression, Operator.OR, trueExpression);
        assertTrue(orCondition.evaluate(context));

        orCondition = Condition.asLogicalExpression(falseExpression, Operator.OR, falseExpression);
        assertFalse(orCondition.evaluate(context));
    }

    @Test
    public void testAsNegation() {
        // Create two expressions that return true and false
        Expression<TestKey> trueExpression = ctx -> true;
        Expression<TestKey> falseExpression = ctx -> false;

        // Test negation
        Condition<TestKey> notCondition = Condition.asNegation(trueExpression);
        assertFalse(notCondition.evaluate(context));

        notCondition = Condition.asNegation(falseExpression);
        assertTrue(notCondition.evaluate(context));
    }

    @Test
    public void testAsBoolean() {
        // Create two expressions that return true and false
        Expression<TestKey> trueExpression = ctx -> true;
        Expression<TestKey> falseExpression = ctx -> false;

        // Test boolean condition
        Condition<TestKey> booleanCondition = Condition.asBoolean(trueExpression);
        assertTrue(booleanCondition.evaluate(context));

        booleanCondition = Condition.asBoolean(falseExpression);
        assertFalse(booleanCondition.evaluate(context));
    }

    @Test
    public void testAsComparison() {
        // Mock a business check that returns a value
        when(businessCheck.execute(context)).thenReturn(new Value(42, Value.Type.INTEGER));

        // Test comparison with equal value
        Condition<TestKey> equalsCondition = Condition.asComparison(businessCheck, Operator.EQUALS, new Value(42, Value.Type.INTEGER));
        assertTrue(equalsCondition.evaluate(context));

        // Test comparison with different value
        equalsCondition = Condition.asComparison(businessCheck, Operator.EQUALS, new Value(43, Value.Type.INTEGER));
        assertFalse(equalsCondition.evaluate(context));
    }

    @Test
    public void testExpressionNegate() {
        // Create an expression that returns true
        Expression<TestKey> trueExpression = ctx -> true;

        // Test negation
        Expression<TestKey> negatedExpression = trueExpression.negate();
        assertFalse(negatedExpression.evaluate(context));

        // Double negation should return the original value
        Expression<TestKey> doubleNegatedExpression = negatedExpression.negate();
        assertTrue(doubleNegatedExpression.evaluate(context));
    }
} 