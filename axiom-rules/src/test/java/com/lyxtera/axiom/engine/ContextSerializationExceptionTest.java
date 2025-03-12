package com.lyxtera.axiom.engine;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ContextSerializationException}.
 */
public class ContextSerializationExceptionTest {

    @Test
    public void testConstructorWithCause() {
        Throwable cause = new RuntimeException("Original cause");
        ContextSerializationException exception = new ContextSerializationException(cause);
        
        assertThat(exception)
            .hasMessage("Failed to serialize/deserialize context")
            .hasCause(cause);
    }
} 