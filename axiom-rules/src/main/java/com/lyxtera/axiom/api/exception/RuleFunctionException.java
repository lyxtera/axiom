package com.lyxtera.axiom.api.exception;

/**
 * Exception thrown for errors related to rule function execution.
 * This includes invalid arguments and execution failures.
 */
public class RuleFunctionException extends AxiomEngineException {

    /**
     * Constructs a new RuleFunctionException with the specified message.
     *
     * @param message The detail message
     */
    public RuleFunctionException(String message) {
        super(message);
    }

    /**
     * Constructs a new RuleFunctionException with the specified message and cause.
     *
     * @param message The detail message
     * @param cause The cause of the exception
     */
    public RuleFunctionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a RuleFunctionException with a formatted message.
     *
     * @param messageTemplate The message template to format
     * @param args Arguments for the message template
     * @return A new RuleFunctionException
     */
    public static RuleFunctionException of(String messageTemplate, Object... args) {
        return new RuleFunctionException(String.format(messageTemplate, args));
    }

    /**
     * Creates a RuleFunctionException with a formatted message and cause.
     *
     * @param messageTemplate The message template to format
     * @param cause The cause of the exception
     * @param args Arguments for the message template
     * @return A new RuleFunctionException
     */
    public static RuleFunctionException of(String messageTemplate, Throwable cause, Object... args) {
        return new RuleFunctionException(String.format(messageTemplate, args), cause);
    }
    
    /**
     * Creates a RuleFunctionException for invalid function arguments.
     *
     * @param message The error message about the invalid argument
     * @return A new RuleFunctionException
     */
    public static RuleFunctionException invalidArgument(String message) {
        return of(MSG_FUNCTION_ARGUMENT_INVALID, message);
    }
    
    /**
     * Creates a RuleFunctionException for a function execution error.
     *
     * @param message The error message
     * @param cause The cause of the exception
     * @return A new RuleFunctionException
     */
    public static RuleFunctionException executionError(String message, Throwable cause) {
        return of(MSG_FUNCTION_EXECUTION_ERROR, cause, message);
    }
} 