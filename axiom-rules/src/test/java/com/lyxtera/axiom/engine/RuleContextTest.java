package com.lyxtera.axiom.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.lyxtera.axiom.api.exception.ContextException;

/**
 * Test class for {@link RuleContext}.
 */
public class RuleContextTest {

    public enum TestKey {
        STRING_KEY,
        INTEGER_KEY,
        BOOLEAN_KEY,
        OBJECT_KEY
    }

    private RuleContext<TestKey> context;

    @BeforeEach
    public void setUp() {
        context = new RuleContext<>(TestKey.class);
    }

    @Test
    public void testConstructor() {
        assertTrue(context.isEmpty());
    }

    @Test
    public void testAddAndGet() {
        // Add and get a string value
        context.add(TestKey.STRING_KEY, "test");
        assertEquals("test", context.get(TestKey.STRING_KEY, String.class).get());
        
        // Add and get an integer value
        context.add(TestKey.INTEGER_KEY, 123);
        assertEquals(123, context.get(TestKey.INTEGER_KEY, Integer.class).get());
        
        // Add and get a boolean value
        context.add(TestKey.BOOLEAN_KEY, true);
        assertEquals(true, context.get(TestKey.BOOLEAN_KEY, Boolean.class).get());
        
        // Add and get a complex object
        Object obj = new Object();
        context.add(TestKey.OBJECT_KEY, obj);
        assertEquals(obj, context.get(TestKey.OBJECT_KEY, Object.class).get());
    }

    @Test
    public void testGetRequired() {
        // Add a value
        context.add(TestKey.STRING_KEY, "test");
        
        // Get required value when it exists
        assertEquals("test", context.getRequired(TestKey.STRING_KEY, String.class));
        
        // Get required value when it doesn't exist
        assertThrows(ContextException.class, () -> context.getRequired(TestKey.INTEGER_KEY, Integer.class));
    }

    @Test
    public void testGetWithNonExistentKey() {
        Optional<String> result = context.get(TestKey.STRING_KEY, String.class);
        assertFalse(result.isPresent());
    }

    @Test
    public void testRemove() {
        // Add a value
        context.add(TestKey.STRING_KEY, "test");
        assertTrue(context.get(TestKey.STRING_KEY, String.class).isPresent());
        
        // Remove the value
        context.remove(TestKey.STRING_KEY, String.class);
        assertFalse(context.get(TestKey.STRING_KEY, String.class).isPresent());
    }

    @Test
    public void testIsEmpty() {
        // Initially empty
        assertTrue(context.isEmpty());
        
        // Not empty after adding a value
        context.add(TestKey.STRING_KEY, "test");
        assertFalse(context.isEmpty());
        
        // Empty after removing the value
        context.remove(TestKey.STRING_KEY, String.class);
        assertTrue(context.isEmpty());
    }

    @Test
    public void testToJson() {
        context.add(TestKey.STRING_KEY, "test");
        context.add(TestKey.INTEGER_KEY, 123);
        
        String json = context.toJson();
        
        // Verify that the JSON contains the key-value pairs
        assertTrue(json.contains("STRING_KEY"));
        assertTrue(json.contains("test"));
        assertTrue(json.contains("INTEGER_KEY"));
        assertTrue(json.contains("123"));
    }

    @Test
    public void testFromJson() {
        // Create a context with some values
        context.add(TestKey.STRING_KEY, "test");
        context.add(TestKey.INTEGER_KEY, 123);
        
        // Convert to JSON
        String json = context.toJson();
        
        // Create a new context from the JSON
        RuleContext<TestKey> newContext = RuleContext.fromJson(TestKey.class, json);
        
        // Verify that the values were correctly deserialized
        assertEquals("test", newContext.get(TestKey.STRING_KEY, String.class).get());
        assertEquals(123, newContext.get(TestKey.INTEGER_KEY, Integer.class).get());
    }

    @Test
    public void testToString() {
        context.add(TestKey.STRING_KEY, "test");
        
        String toString = context.toString();
        
        // Verify that the toString method includes the key-value pairs
        assertTrue(toString.contains("STRING_KEY"));
        assertTrue(toString.contains("test"));
    }

    @Test
    public void testAddNullValue() {
        // Add a null value
        Optional<String> result = context.add(TestKey.STRING_KEY, null);
        
        // Verify that the result is an empty Optional
        assertFalse(result.isPresent());
    }
} 