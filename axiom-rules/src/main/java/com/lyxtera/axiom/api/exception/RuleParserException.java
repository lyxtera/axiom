package com.lyxtera.axiom.api.exception;

/**
 * Exception thrown for errors encountered during rule parsing.
 * This includes syntax errors, unknown expressions, and invalid operators.
 */
public class RuleParserException extends AxiomEngineException {

    /**
     * Constructs a new RuleParserException with the specified message.
     *
     * @param message The detail message
     */
    public RuleParserException(String message) {
        super(message);
    }

    /**
     * Constructs a new RuleParserException with the specified message and cause.
     *
     * @param message The detail message
     * @param cause The cause of the exception
     */
    public RuleParserException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a RuleParserException with a formatted message.
     *
     * @param messageTemplate The message template to format
     * @param args Arguments for the message template
     * @return A new RuleParserException
     */
    public static RuleParserException of(String messageTemplate, Object... args) {
        return new RuleParserException(String.format(messageTemplate, args));
    }

    /**
     * Creates a RuleParserException with a formatted message and cause.
     *
     * @param messageTemplate The message template to format
     * @param cause The cause of the exception
     * @param args Arguments for the message template
     * @return A new RuleParserException
     */
    public static RuleParserException of(String messageTemplate, Throwable cause, Object... args) {
        return new RuleParserException(String.format(messageTemplate, args), cause);
    }
    
    /**
     * Creates a RuleParserException for an unknown expression type.
     *
     * @param expressionType The name of the unknown expression type
     * @return A new RuleParserException
     */
    public static RuleParserException unknownExpressionType(String expressionType) {
        return of(MSG_UNKNOWN_EXPRESSION_TYPE, expressionType);
    }
    
    /**
     * Creates a RuleParserException for an unknown comparison type.
     *
     * @param comparisonType The name of the unknown comparison type
     * @return A new RuleParserException
     */
    public static RuleParserException unknownComparisonType(String comparisonType) {
        return of(MSG_UNKNOWN_COMPARISON_TYPE, comparisonType);
    }
    
    /**
     * Creates a RuleParserException for an unknown operator.
     *
     * @param operator The unknown operator
     * @return A new RuleParserException
     */
    public static RuleParserException unknownOperator(String operator) {
        return of(MSG_UNKNOWN_OPERATOR, operator);
    }
} 