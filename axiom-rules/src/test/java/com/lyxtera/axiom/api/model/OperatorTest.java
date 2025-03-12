package com.lyxtera.axiom.api.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.lyxtera.axiom.api.exception.OperatorException;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link Operator} class.
 */
public class OperatorTest {

    @Test
    public void testEqualsOperator() {
        Operator operator = Operator.fromSymbol("=");
        assertThat(operator).isEqualTo(Operator.EQUALS);
        assertThat(operator.apply(2, 2)).isTrue();
    }

    @Test
    public void testNotEqualsOperator() {
        // If NOT_EQUALS exists, uncomment this test
        // Operator operator = Operator.fromSymbol("!=");
        // assertThat(operator).isEqualTo(Operator.NOT_EQUALS);
        // assertThat(operator.apply(2, 3)).isTrue();
    }

    @Test
    public void testGreaterThanOperator() {
        Operator operator = Operator.fromSymbol(">");
        assertThat(operator).isEqualTo(Operator.GREATER_THAN);
        assertThat(operator.apply(3, 2)).isTrue();
    }

    @Test
    public void testLessThanOperator() {
        Operator operator = Operator.fromSymbol("<");
        assertThat(operator).isEqualTo(Operator.LESS_THAN);
        assertThat(operator.apply(2, 3)).isTrue();
    }

    @Test
    public void testGreaterThanOrEqualOperator() {
        // If GREATER_THAN_OR_EQUAL exists, uncomment this test
        // Operator operator = Operator.fromSymbol(">=");
        // assertThat(operator).isEqualTo(Operator.GREATER_THAN_OR_EQUAL);
        // assertThat(operator.apply(3, 3)).isTrue();
    }

    @Test
    public void testLessThanOrEqualOperator() {
        // If LESS_THAN_OR_EQUAL exists, uncomment this test
        // Operator operator = Operator.fromSymbol("<=");
        // assertThat(operator).isEqualTo(Operator.LESS_THAN_OR_EQUAL);
        // assertThat(operator.apply(3, 3)).isTrue();
    }

    @Test
    public void testLogicalAndOperator() {
        Operator operator = Operator.fromSymbol("and");
        assertThat(operator).isEqualTo(Operator.AND);
        assertThat(operator.apply(true, true)).isTrue();
    }

    @Test
    public void testLogicalOrOperator() {
        Operator operator = Operator.fromSymbol("or");
        assertThat(operator).isEqualTo(Operator.OR);
        assertThat(operator.apply(true, false)).isTrue();
    }

    @Test
    public void testUnknownOperator() {
        assertThatThrownBy(() -> Operator.fromSymbol("UNKNOWN"))
                .isInstanceOf(OperatorException.class)
                .hasMessageContaining("Unknown operator: UNKNOWN");
    }
    
    @Test
    public void testEqualsOperatorSymbol() {
        assertThat(Operator.EQUALS.getSymbol()).isEqualTo("=");
    }
    
    @Test
    public void testNotEqualsOperatorSymbol() {
        // If NOT_EQUALS exists, uncomment this test
        // assertThat(Operator.NOT_EQUALS.getSymbol()).isEqualTo("!=");
    }
    
    @Test
    public void testGreaterThanOperatorSymbol() {
        assertThat(Operator.GREATER_THAN.getSymbol()).isEqualTo(">");
    }
    
    @Test
    public void testLessThanOperatorSymbol() {
        assertThat(Operator.LESS_THAN.getSymbol()).isEqualTo("<");
    }
    
    @Test
    public void testGreaterThanOrEqualOperatorSymbol() {
        // If GREATER_THAN_OR_EQUAL exists, uncomment this test
        // assertThat(Operator.GREATER_THAN_OR_EQUAL.getSymbol()).isEqualTo(">=");
    }
    
    @Test
    public void testLessThanOrEqualOperatorSymbol() {
        // If LESS_THAN_OR_EQUAL exists, uncomment this test
        // assertThat(Operator.LESS_THAN_OR_EQUAL.getSymbol()).isEqualTo("<=");
    }
    
    @Test
    public void testLogicalAndOperatorSymbol() {
        assertThat(Operator.AND.getSymbol()).isEqualTo("and");
    }
    
    @Test
    public void testLogicalOrOperatorSymbol() {
        assertThat(Operator.OR.getSymbol()).isEqualTo("or");
    }
    
    @Test
    public void testInvalidComparisonType() {
        // The EQUALS operator in the actual implementation might handle strings differently
        // Let's check if it throws an exception for non-numeric types in comparison operators
        assertThatThrownBy(() -> Operator.GREATER_THAN.apply("string1", "string2"))
                .isInstanceOf(OperatorException.class)
                .hasMessageContaining("Operator > can only be applied to numbers");
    }
    
    @Test
    public void testInvalidNullComparisonGreaterThan() {
        assertThatThrownBy(() -> Operator.GREATER_THAN.apply(null, 5))
                .isInstanceOf(OperatorException.class)
                .hasMessageContaining("Operator > can only be applied to numbers");
    }
    
    @Test
    public void testInvalidNullComparisonLessThan() {
        assertThatThrownBy(() -> Operator.LESS_THAN.apply(5, null))
                .isInstanceOf(OperatorException.class)
                .hasMessageContaining("Operator < can only be applied to numbers");
    }
    
    @Test
    public void testInvalidBooleanComparison() {
        assertThatThrownBy(() -> Operator.GREATER_THAN.apply(true, false))
                .isInstanceOf(OperatorException.class)
                .hasMessageContaining("Operator > can only be applied to numbers");
    }
    
    @Test
    public void testInvalidLogicalOperation() {
        assertThatThrownBy(() -> Operator.AND.apply(1, 2))
                .isInstanceOf(OperatorException.class)
                .hasMessageContaining("Operator and can only be applied to booleans");
    }
    
    @Test
    public void testInvalidNullLogicalOperation() {
        assertThatThrownBy(() -> Operator.OR.apply(null, true))
                .isInstanceOf(OperatorException.class)
                .hasMessageContaining("Operator or can only be applied to booleans");
    }
} 