package com.lyxtera.axiom.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        assertThat(context.isEmpty()).isTrue();
    }

    @Test
    public void testAddAndGet() {
        // Add and get a string value
        context.add(TestKey.STRING_KEY, "test");
        assertThat(context.get(TestKey.STRING_KEY, String.class))
            .isPresent()
            .hasValue("test");
        
        // Add and get an integer value
        context.add(TestKey.INTEGER_KEY, 123);
        assertThat(context.get(TestKey.INTEGER_KEY, Integer.class))
            .isPresent()
            .hasValue(123);
        
        // Add and get a boolean value
        context.add(TestKey.BOOLEAN_KEY, true);
        assertThat(context.get(TestKey.BOOLEAN_KEY, Boolean.class))
            .isPresent()
            .hasValue(true);
        
        // Add and get a complex object
        Object obj = new Object();
        context.add(TestKey.OBJECT_KEY, obj);
        assertThat(context.get(TestKey.OBJECT_KEY, Object.class))
            .isPresent()
            .hasValue(obj);
    }

    @Test
    public void testGetRequired() {
        // Add a value
        context.add(TestKey.STRING_KEY, "test");
        
        // Get required value when it exists
        assertThat(context.getRequired(TestKey.STRING_KEY, String.class))
            .isEqualTo("test");
        
        // Get required value when it doesn't exist
        assertThatThrownBy(() -> context.getRequired(TestKey.INTEGER_KEY, Integer.class))
            .isInstanceOf(ContextException.class);
    }

    @Test
    public void testGetWithNonExistentKey() {
        Optional<String> result = context.get(TestKey.STRING_KEY, String.class);
        assertThat(result).isEmpty();
    }

    @Test
    public void testRemove() {
        // Add a value
        context.add(TestKey.STRING_KEY, "test");
        assertThat(context.get(TestKey.STRING_KEY, String.class)).isPresent();
        
        // Remove the value
        context.remove(TestKey.STRING_KEY, String.class);
        assertThat(context.get(TestKey.STRING_KEY, String.class)).isEmpty();
    }

    @Test
    public void testIsEmpty() {
        // Initially empty
        assertThat(context.isEmpty()).isTrue();
        
        // Not empty after adding a value
        context.add(TestKey.STRING_KEY, "test");
        assertThat(context.isEmpty()).isFalse();
        
        // Empty after removing the value
        context.remove(TestKey.STRING_KEY, String.class);
        assertThat(context.isEmpty()).isTrue();
    }

    @Test
    public void testToJson() {
        context.add(TestKey.STRING_KEY, "test");
        context.add(TestKey.INTEGER_KEY, 123);
        
        String json = context.toJson();
        
        // Verify that the JSON contains the key-value pairs
        assertThat(json)
            .contains("STRING_KEY")
            .contains("test")
            .contains("INTEGER_KEY")
            .contains("123");
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
        assertThat(newContext.get(TestKey.STRING_KEY, String.class))
            .isPresent()
            .hasValue("test");
        assertThat(newContext.get(TestKey.INTEGER_KEY, Integer.class))
            .isPresent()
            .hasValue(123);
    }

    @Test
    public void testToString() {
        context.add(TestKey.STRING_KEY, "test");
        
        String toString = context.toString();
        
        // Verify that the toString method includes the key-value pairs
        assertThat(toString)
            .contains("STRING_KEY")
            .contains("test");
    }

    @Test
    public void testAddNullValue() {
        // Add a null value
        Optional<String> result = context.add(TestKey.STRING_KEY, null);
        
        // Verify that the result is an empty Optional
        assertThat(result).isEmpty();
    }
} 