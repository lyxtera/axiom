package com.lyxtera.axiom.engine;

import com.lyxtera.axiom.api.exception.ContextException;

/**
 * Exception thrown when there is an error serializing or deserializing a Context.
 * <p>
 * This exception is used to wrap underlying exceptions that occur during
 * JSON serialization or deserialization of Context objects, providing a
 * consistent exception type for error handling.
 * 
 * @deprecated Use {@link ContextException#serializationError(Throwable)} instead
 */
@Deprecated
public class ContextSerializationException extends RuntimeException {
    
    /**
     * Creates a new ContextSerializationException with the specified cause.
     *
     * @param cause The underlying exception that caused this exception
     */
    public ContextSerializationException(Throwable cause) {
        super("Failed to serialize/deserialize context", cause);
    }
} 