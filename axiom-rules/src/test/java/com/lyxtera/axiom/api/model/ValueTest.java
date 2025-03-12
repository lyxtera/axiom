package com.lyxtera.axiom.api.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Value}.
 */
public class ValueTest {

    @Test
    public void testStringValue() {
        Value value = new Value("test", Value.Type.STRING);
        assertEquals("test", value.getValue());
        assertEquals(Value.Type.STRING, value.getType());
        assertEquals("\"test\"", value.toString());
    }

    @Test
    public void testIntegerValue() {
        Value value = new Value(42, Value.Type.INTEGER);
        assertEquals(42, value.getValue());
        assertEquals(Value.Type.INTEGER, value.getType());
        assertEquals("42", value.toString());
    }

    @Test
    public void testDecimalValue() {
        Value value = new Value(3.14, Value.Type.DECIMAL);
        assertEquals(3.14, value.getValue());
        assertEquals(Value.Type.DECIMAL, value.getType());
        assertEquals("3.14", value.toString());
    }

    @Test
    public void testBooleanValue() {
        Value value = new Value(true, Value.Type.BOOLEAN);
        assertEquals(true, value.getValue());
        assertEquals(Value.Type.BOOLEAN, value.getType());
        assertEquals("true", value.toString());
    }

    @Test
    public void testNullValue() {
        Value value = new Value(null, Value.Type.STRING);
        assertEquals(null, value.getValue());
        assertEquals(Value.Type.STRING, value.getType());
        assertEquals("\"null\"", value.toString());
    }

    @Test
    public void testValueTypes() {
        assertNotNull(Value.Type.values());
        assertEquals(4, Value.Type.values().length);
        assertEquals(Value.Type.STRING, Value.Type.valueOf("STRING"));
        assertEquals(Value.Type.INTEGER, Value.Type.valueOf("INTEGER"));
        assertEquals(Value.Type.DECIMAL, Value.Type.valueOf("DECIMAL"));
        assertEquals(Value.Type.BOOLEAN, Value.Type.valueOf("BOOLEAN"));
    }
} 