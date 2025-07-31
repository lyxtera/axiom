package com.lyxtera.axiom.api.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.lyxtera.axiom.api.exception.AxiomEngineException;

/**
 * Test class for {@link Value}.
 */
class ValueTest {

    @Test
    void testStringValue() {
        Value value = Value.of("test");
        assertThat(value.getValue()).isEqualTo("test");
        assertThat(value.getType()).isEqualTo(Value.Type.STRING);
        assertThat(value.toString()).isEqualTo("\"test\"");
        assertThat(value.asString()).isEqualTo("test");
    }

    @Test
    void testIntegerValue() {
        Value value = Value.of(42);
        assertThat(value.getValue()).isEqualTo(new BigDecimal("42"));
        assertThat(value.getType()).isEqualTo(Value.Type.NUMBER);
        assertThat(value.toString()).isEqualTo("42");
        assertThat(value.asNumber()).isEqualTo(new BigDecimal("42"));
    }

    @Test
    void testDecimalValue() {
        Value value = Value.of(3.14);
        assertThat(value.getValue()).isEqualTo(new BigDecimal("3.14"));
        assertThat(value.getType()).isEqualTo(Value.Type.NUMBER);
        assertThat(value.toString()).isEqualTo("3.14");
        assertThat(value.asNumber()).isEqualTo(new BigDecimal("3.14"));
    }

    @Test
    void testBooleanValue() {
        Value value = Value.of(true);
        assertThat(value.getValue()).isEqualTo(true);
        assertThat(value.getType()).isEqualTo(Value.Type.BOOLEAN);
        assertThat(value.toString()).isEqualTo("true");
        assertThat(value.asBoolean()).isTrue();
    }

    @Test
    void testNullValue() {
        Value value = Value.of(null);
        assertThat(value).isEqualTo(Value.EMPTY);
        assertThat(value.getType()).isEqualTo(Value.Type.STRING);
        assertThat(value.asString()).isEqualTo("null");
        assertThat(value.asNumber()).isEqualTo(BigDecimal.ZERO);
        assertThat(value.asBoolean()).isFalse();
    }
    
    @Test
    void testTypeConversion() {
        Value stringValue = Value.of("test");
        Value numberValue = Value.of(123);
        Value booleanValue = Value.of(true);
        
        // String type conversions
        assertThat(stringValue.asString()).isEqualTo("test");
        assertThat(numberValue.asString()).isEqualTo("123");
        assertThat(booleanValue.asString()).isEqualTo("true");
        
        // Number type conversions
        assertThatThrownBy(() -> stringValue.asNumber())
            .isInstanceOf(NumberFormatException.class);
        assertThat(numberValue.asNumber()).isEqualTo(new BigDecimal("123"));
        assertThatThrownBy(() -> booleanValue.asNumber())
            .isInstanceOf(NumberFormatException.class);
        
        // Boolean type conversions
        assertThat(stringValue.asBoolean()).isFalse(); // Non-"true" strings are false
        assertThat(Value.of("true").asBoolean()).isTrue();
        assertThat(numberValue.asBoolean()).isFalse(); // Numbers aren't evaluated as booleans
        assertThat(booleanValue.asBoolean()).isTrue();
    }
    
    @Test
    void testEqualityAndHashCode() {
        Value value1 = Value.of("test");
        Value value2 = new Value("test", Value.Type.STRING);
        Value value3 = Value.of(123);
        
        assertThat(value1).isEqualTo(value1); // Same instance
        assertThat(value1).isEqualTo(value2); // Same value and type
        assertThat(value1).isNotEqualTo(value3); // Different type
        assertThat(value1).isNotEqualTo(null); // Null comparison
        assertThat(value1).isNotEqualTo("test"); // Different class
    }
    
    @Test
    void testUnsupportedValueType() {
        // Try to create a Value from an unsupported type
        assertThatThrownBy(() -> Value.of(new Object()))
            .isInstanceOf(AxiomEngineException.class)
            .hasMessageContaining("Unsupported value type");
    }
} 