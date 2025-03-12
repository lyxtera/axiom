package com.lyxtera.axiom.api.exception;

/**
 * Exception thrown for errors related to operators in rule expressions.
 * This includes type mismatches and unknown operators.
 */
public class OperatorException extends AxiomEngineException {

    /**
     * Constructs a new OperatorException with the specified message.
     *
     * @param message The detail message
     */
    public OperatorException(String message) {
        super(message);
    }

    /**
     * Constructs a new OperatorException with the specified message and cause.
     *
     * @param message The detail message
     * @param cause The cause of the exception
     */
    public OperatorException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates an OperatorException with a formatted message.
     *
     * @param messageTemplate The message template to format
     * @param args Arguments for the message template
     * @return A new OperatorException
     */
    public static OperatorException of(String messageTemplate, Object... args) {
        return new OperatorException(String.format(messageTemplate, args));
    }

    /**
     * Creates an OperatorException for an invalid data type with an operator.
     *
     * @param operator The operator that was used
     * @param expectedType The expected data type
     * @return A new OperatorException
     */
    public static OperatorException invalidType(String operator, String expectedType) {
        return of(MSG_OPERATOR_INVALID_TYPE, operator, expectedType);
    }
    
    /**
     * Creates an OperatorException for an unknown operator.
     *
     * @param operator The unknown operator
     * @return A new OperatorException
     */
    public static OperatorException unknownOperator(String operator) {
        return of(MSG_UNKNOWN_OPERATOR, operator);
    }
    
    /**
     * Creates an OperatorException for an unsupported operation.
     *
     * @param operation Description of the unsupported operation
     * @return A new OperatorException
     */
    public static OperatorException unsupportedOperation(String operation) {
        return of(MSG_UNSUPPORTED_OPERATION, operation);
    }
} 