package com.lyxtera.axiom.api.model;

import static org.assertj.core.api.Assertions.assertThat;
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
class ConditionTest {

    public enum TestKey {
        TEST_KEY
    }

    @Mock
    private RuleContext<TestKey> context;

    @Mock
    private BusinessCheck<TestKey> businessCheck;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAsLogicalExpressionAnd() {
        // Create two expressions that return true and false
        Expression<TestKey> trueExpression = ctx -> true;
        Expression<TestKey> falseExpression = ctx -> false;

        // Test AND operator
        Condition<TestKey> andCondition = Condition.asLogicalExpression(trueExpression, Operator.AND, trueExpression);
        assertThat(andCondition.evaluate(context)).isTrue();

        andCondition = Condition.asLogicalExpression(trueExpression, Operator.AND, falseExpression);
        assertThat(andCondition.evaluate(context)).isFalse();

        andCondition = Condition.asLogicalExpression(falseExpression, Operator.AND, trueExpression);
        assertThat(andCondition.evaluate(context)).isFalse();

        andCondition = Condition.asLogicalExpression(falseExpression, Operator.AND, falseExpression);
        assertThat(andCondition.evaluate(context)).isFalse();

        // Verify getters
        assertThat(Condition.asLogicalExpression(trueExpression, Operator.AND, falseExpression).getLeft()).isEqualTo(trueExpression);
        assertThat(Condition.asLogicalExpression(trueExpression, Operator.AND, falseExpression).getOperator()).isEqualTo(Operator.AND);
        assertThat(Condition.asLogicalExpression(trueExpression, Operator.AND, falseExpression).getRight()).isEqualTo(falseExpression);
    }

    @Test
    void testAsLogicalExpressionOr() {
        // Create two expressions that return true and false
        Expression<TestKey> trueExpression = ctx -> true;
        Expression<TestKey> falseExpression = ctx -> false;

        // Test OR operator
        Condition<TestKey> orCondition = Condition.asLogicalExpression(trueExpression, Operator.OR, trueExpression);
        assertThat(orCondition.evaluate(context)).isTrue();

        orCondition = Condition.asLogicalExpression(trueExpression, Operator.OR, falseExpression);
        assertThat(orCondition.evaluate(context)).isTrue();

        orCondition = Condition.asLogicalExpression(falseExpression, Operator.OR, trueExpression);
        assertThat(orCondition.evaluate(context)).isTrue();

        orCondition = Condition.asLogicalExpression(falseExpression, Operator.OR, falseExpression);
        assertThat(orCondition.evaluate(context)).isFalse();
    }

    @Test
    void testAsNegation() {
        // Create two expressions that return true and false
        Expression<TestKey> trueExpression = ctx -> true;
        Expression<TestKey> falseExpression = ctx -> false;

        // Test negation
        Condition<TestKey> notCondition = Condition.asNegation(trueExpression);
        assertThat(notCondition.evaluate(context)).isFalse();

        notCondition = Condition.asNegation(falseExpression);
        assertThat(notCondition.evaluate(context)).isTrue();
    }

    @Test
    void testAsBoolean() {
        // Create two expressions that return true and false
        Expression<TestKey> trueExpression = ctx -> true;
        Expression<TestKey> falseExpression = ctx -> false;

        // Test boolean condition
        Condition<TestKey> booleanCondition = Condition.asBoolean(trueExpression);
        assertThat(booleanCondition.evaluate(context)).isTrue();

        booleanCondition = Condition.asBoolean(falseExpression);
        assertThat(booleanCondition.evaluate(context)).isFalse();
    }

    @Test
    void testAsComparison() {
        // Mock a business check that returns a value
        when(businessCheck.execute(context)).thenReturn(new Value("42", Value.Type.NUMBER));

        // Test comparison with equal value
        Condition<TestKey> equalsCondition = Condition.asComparison(businessCheck, Operator.EQUALS, new Value("42", Value.Type.NUMBER));
        assertThat(equalsCondition.evaluate(context)).isTrue();

        // Test comparison with different value
        equalsCondition = Condition.asComparison(businessCheck, Operator.EQUALS, new Value("43", Value.Type.NUMBER));
        assertThat(equalsCondition.evaluate(context)).isFalse();
    }

    @Test
    void testExpressionNegate() {
        // Create an expression that returns true
        Expression<TestKey> trueExpression = ctx -> true;

        // Test negation
        Expression<TestKey> negatedExpression = trueExpression.negate();
        assertThat(negatedExpression.evaluate(context)).isFalse();

        // Double negation should return the original value
        Expression<TestKey> doubleNegatedExpression = negatedExpression.negate();
        assertThat(doubleNegatedExpression.evaluate(context)).isTrue();
    }
} 