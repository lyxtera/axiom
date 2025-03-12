package com.lyxtera.axiom.api.exception;

/**
 * Exception thrown for errors related to rule operations.
 * This includes rule configuration, validation, and execution issues.
 */
public class RuleException extends AxiomEngineException {

    /**
     * Constructs a new RuleException with the specified message.
     *
     * @param message The detail message
     */
    public RuleException(String message) {
        super(message);
    }

    /**
     * Constructs a new RuleException with the specified message and cause.
     *
     * @param message The detail message
     * @param cause The cause of the exception
     */
    public RuleException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a RuleException with a formatted message.
     *
     * @param messageTemplate The message template to format
     * @param args Arguments for the message template
     * @return A new RuleException
     */
    public static RuleException of(String messageTemplate, Object... args) {
        return new RuleException(String.format(messageTemplate, args));
    }

    /**
     * Creates a RuleException with a formatted message and cause.
     *
     * @param messageTemplate The message template to format
     * @param cause The cause of the exception
     * @param args Arguments for the message template
     * @return A new RuleException
     */
    public static RuleException of(String messageTemplate, Throwable cause, Object... args) {
        return new RuleException(String.format(messageTemplate, args), cause);
    }
    
    /**
     * Creates a RuleException for an invalid priority value.
     *
     * @return A new RuleException
     */
    public static RuleException invalidPriority() {
        return new RuleException(MSG_INVALID_PRIORITY);
    }
} 