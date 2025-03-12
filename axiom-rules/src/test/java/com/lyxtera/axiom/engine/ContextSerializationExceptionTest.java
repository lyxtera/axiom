package com.lyxtera.axiom.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ContextSerializationException}.
 */
public class ContextSerializationExceptionTest {

    @Test
    public void testConstructorWithCause() {
        Throwable cause = new RuntimeException("Original cause");
        ContextSerializationException exception = new ContextSerializationException(cause);
        
        assertEquals("Failed to serialize/deserialize context", exception.getMessage());
        assertSame(cause, exception.getCause());
    }
} 