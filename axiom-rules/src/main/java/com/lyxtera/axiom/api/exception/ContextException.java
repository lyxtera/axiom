package com.lyxtera.axiom.api.exception;

/**
 * Exception thrown for errors related to rule context operations.
 * This includes context manipulation, validation, and serialization issues.
 */
public class ContextException extends AxiomEngineException {

    /**
     * Constructs a new ContextException with the specified message.
     *
     * @param message The detail message
     */
    public ContextException(String message) {
        super(message);
    }

    /**
     * Constructs a new ContextException with the specified message and cause.
     *
     * @param message The detail message
     * @param cause The cause of the exception
     */
    public ContextException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a ContextException with a formatted message.
     *
     * @param messageTemplate The message template to format
     * @param args Arguments for the message template
     * @return A new ContextException
     */
    public static ContextException of(String messageTemplate, Object... args) {
        return new ContextException(String.format(messageTemplate, args));
    }

    /**
     * Creates a ContextException with a formatted message and cause.
     *
     * @param messageTemplate The message template to format
     * @param cause The cause of the exception
     * @param args Arguments for the message template
     * @return A new ContextException
     */
    public static ContextException of(String messageTemplate, Throwable cause, Object... args) {
        return new ContextException(String.format(messageTemplate, args), cause);
    }
    
    /**
     * Creates a ContextException for context serialization or deserialization errors.
     *
     * @param cause The cause of the exception
     * @return A new ContextException
     */
    public static ContextException serializationError(Throwable cause) {
        return new ContextException(MSG_CONTEXT_SERIALIZATION, cause);
    }
    
    /**
     * Creates a ContextException for non-string value errors.
     *
     * @return A new ContextException
     */
    public static ContextException valueNotString() {
        return new ContextException(MSG_VALUE_MUST_BE_STRING);
    }
} 