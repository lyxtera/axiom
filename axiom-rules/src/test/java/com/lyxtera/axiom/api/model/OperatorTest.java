package com.lyxtera.axiom.api.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.lyxtera.axiom.api.exception.OperatorException;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link Operator} class.
 */
class OperatorTest {

    @Test
    void testEqualsOperator() {
        Operator operator = Operator.fromSymbol("=");
        assertThat(operator).isEqualTo(Operator.EQUALS);
        assertThat(operator.apply(Value.of(2), Value.of(2))).isTrue();
    }

    @Test
    void testGreaterThanOperator() {
        Operator operator = Operator.fromSymbol(">");
        assertThat(operator).isEqualTo(Operator.GREATER_THAN);
        assertThat(operator.apply(Value.of(3), Value.of(2))).isTrue();
    }

    @Test
    void testLessThanOperator() {
        Operator operator = Operator.fromSymbol("<");
        assertThat(operator).isEqualTo(Operator.LESS_THAN);
        assertThat(operator.apply(Value.of(2), Value.of(3))).isTrue();
    }

    @Test
    void testLogicalAndOperator() {
        Operator operator = Operator.fromSymbol("and");
        assertThat(operator).isEqualTo(Operator.AND);
        assertThat(operator.apply(Value.of(true), Value.of(true))).isTrue();
    }

    @Test
    void testLogicalOrOperator() {
        Operator operator = Operator.fromSymbol("or");
        assertThat(operator).isEqualTo(Operator.OR);
        assertThat(operator.apply(Value.of(true), Value.of(false))).isTrue();
    }

    @Test
    void testUnknownOperator() {
        assertThatThrownBy(() -> Operator.fromSymbol("UNKNOWN"))
                .isInstanceOf(OperatorException.class)
                .hasMessageContaining("Unknown operator: UNKNOWN");
    }
    
    @Test
    void testEqualsOperatorSymbol() {
        assertThat(Operator.EQUALS.getSymbol()).isEqualTo("=");
    }
    
    @Test
    void testGreaterThanOperatorSymbol() {
        assertThat(Operator.GREATER_THAN.getSymbol()).isEqualTo(">");
    }
    
    @Test
    void testLessThanOperatorSymbol() {
        assertThat(Operator.LESS_THAN.getSymbol()).isEqualTo("<");
    }
    
    @Test
    void testLogicalAndOperatorSymbol() {
        assertThat(Operator.AND.getSymbol()).isEqualTo("and");
    }
    
    @Test
    void testLogicalOrOperatorSymbol() {
        assertThat(Operator.OR.getSymbol()).isEqualTo("or");
    }
    
    @Test
    void testInvalidComparisonType() {
        assertThatThrownBy(() -> Operator.GREATER_THAN.apply(Value.of("string1"), Value.of("string2")))
            .isInstanceOf(OperatorException.class)
            .hasMessageContaining("Operator > can only be applied to numbers");
    }
    
    @Test
    void testInvalidNullComparisonGreaterThan() {
        assertThatThrownBy(() -> Operator.GREATER_THAN.apply(Value.of(null), Value.of(5)))
            .isInstanceOf(OperatorException.class)
            .hasMessageContaining("Operator > can only be applied to numbers");
    }
    
    @Test
    void testInvalidNullComparisonLessThan() {
        assertThatThrownBy(() -> Operator.LESS_THAN.apply(Value.of(5), Value.of(null)))
            .isInstanceOf(OperatorException.class)
            .hasMessageContaining("Operator < can only be applied to numbers");
    }
    
    @Test
    void testInvalidBooleanComparison() {
        assertThatThrownBy(() -> Operator.GREATER_THAN.apply(Value.of(true), Value.of(false)))
            .isInstanceOf(OperatorException.class)
            .hasMessageContaining("Operator > can only be applied to numbers");
    }
    
    @Test
    void testInvalidLogicalOperation() {
        assertThatThrownBy(() -> Operator.AND.apply(Value.of(1), Value.of(2)))
            .isInstanceOf(OperatorException.class)
            .hasMessageContaining("Operator and can only be applied to booleans");
    }
    
    @Test
    void testInvalidNullLogicalOperation() {
        assertThatThrownBy(() -> Operator.OR.apply(Value.of(null), Value.of(true)))
            .isInstanceOf(OperatorException.class)
            .hasMessageContaining("Operator or can only be applied to booleans");
    }
    
    @Test
    void testEqualsWithNullValues() {
        assertThat(Operator.EQUALS.apply(null, null)).isTrue();
        assertThat(Operator.EQUALS.apply(Value.of("test"), null)).isFalse();
        assertThat(Operator.EQUALS.apply(null, Value.of("test"))).isFalse();
    }
} 