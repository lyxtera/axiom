package com.lyxtera.axiom.api.model;

import com.lyxtera.axiom.engine.RuleContext;

/**
 * Represents a condition in a business rule.
 * A condition is a binary expression with a left operand, an operator, and a right operand.
 */
public class Condition<K extends Enum<K>> implements Expression<K> {
    private final Expression<K> left;
    private final Operator operator;
    private final Expression<K> right;
    
    private Condition(Expression<K> left, Operator operator, Expression<K> right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    public static <K extends Enum<K>> Condition<K> asLogicalExpression(Expression<K> left, Operator operator, Expression<K> right) {
        return new Condition<>(left, operator, right);
    }

    public static <K extends Enum<K>> Condition<K> asNegation(Expression<K> left) {
        return new Condition<>(left.negate(), null, null);
    }

    public static <K extends Enum<K>> Condition<K> asBoolean(Expression<K> left) {
        return new Condition<>(left, null, null);
    }

    public static <K extends Enum<K>> Condition<K> asComparison(RuleFunction<K> checkResult, Operator operator, Value value) {
        return asBoolean(k -> checkResult.execute(k).getValue().equals(value.getValue()));
    }
    
    public Expression<K> getLeft() {
        return left;
    }
    
    public Operator getOperator() {
        return operator;
    }
    
    public Expression<K> getRight() {
        return right;
    }
    
    @Override
    public boolean evaluate(RuleContext<K> context) {
        boolean leftValue = left.evaluate(context);

        if (right != null) {
            Boolean rightValue = right.evaluate(context);
            return operator.apply(leftValue, rightValue);
        }

        return leftValue;
    }
} 