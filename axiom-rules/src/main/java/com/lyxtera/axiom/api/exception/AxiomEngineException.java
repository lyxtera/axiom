package com.lyxtera.axiom.api.exception;

/**
 * Root exception for all Axiom engine-related exceptions.
 * This serves as the base class for all custom exceptions in the Axiom module.
 */
public class AxiomEngineException extends RuntimeException {
    
    // Message templates
    // Rule related messages
    public static final String MSG_INVALID_PRIORITY = "Priority must be at least 1";
    public static final String MSG_UNKNOWN_OPERATOR = "Unknown operator: %s";
    public static final String MSG_OPERATOR_INVALID_TYPE = "Operator %s can only be applied to %s";
    public static final String MSG_UNSUPPORTED_OPERATION = "Operation not supported: %s";
    public static final String MSG_UNKNOWN_OPERATOR_TYPE = "Unknown operator";
    public static final String TYPE_NUMBERS = "numbers";
    public static final String TYPE_BOOLEANS = "booleans";
    
    // Parser related messages
    public static final String MSG_UNKNOWN_EXPRESSION_TYPE = "Unknown expression type: %s";
    public static final String MSG_UNKNOWN_COMPARISON_TYPE = "Unknown comparison type: %s";
    public static final String MSG_UNKNOWN_BUSINESS_CHECK = "Unknown business check: %s";
    public static final String MSG_UNKNOWN_BUSINESS_ACTION = "Unknown business action: %s";
    public static final String MSG_FUNCTION_NOT_IMPLEMENTED = "This method must be implemented by the specific rule function";
    public static final String MSG_MISSING_METADATA = "RuleMetadata annotation is missing from %s";
    
    // Context related messages
    public static final String MSG_CONTEXT_SERIALIZATION = "Failed to serialize/deserialize context";
    public static final String MSG_VALUE_MUST_BE_STRING = "Value must be a non-null string";
    public static final String MSG_NO_VALUE_FOR_KEY = "No value present for key %s";
    public static final String MSG_INVALID_ENUM_CONVERSION = "Cannot convert '%s' to a valid enum constant of type %s";
    
    // Rule function related messages
    public static final String MSG_FUNCTION_ARGUMENT_INVALID = "Invalid function argument: %s";
    public static final String MSG_FUNCTION_EXECUTION_ERROR = "Error executing function: %s";
    public static final String MSG_CTXGET_REQUIRES_STRING = "[ctxGet] requires exactly one string argument";
    public static final String MSG_METHOD_HANDLE_ERROR = "%s: Error creating method handle for %s";
    
    // Rule loading related messages
    public static final String MSG_RULESET_LOAD_ERROR = "Failed to load ruleset from %s: %s";
    public static final String MSG_RULESET_PARSE_ERROR = "Failed to parse ruleset: %s";
    public static final String MSG_VALIDATION_ERRORS = "Validation errors found:\n%s";
    public static final String MSG_RULE_PARSE_ERROR = "Failed to parse rule '%s' with expression: '%s'";
    
    /**
     * Constructs a new AxiomEngineException with the specified message.
     *
     * @param message The detail message
     */
    public AxiomEngineException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new AxiomEngineException with the specified message and cause.
     *
     * @param message The detail message
     * @param cause The cause of the exception
     */
    public AxiomEngineException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Creates an AxiomEngineException with a formatted message.
     *
     * @param messageTemplate The message template to format
     * @param args Arguments for the message template
     * @return A new AxiomEngineException
     */
    public static AxiomEngineException of(String messageTemplate, Object... args) {
        return new AxiomEngineException(String.format(messageTemplate, args));
    }
    
    /**
     * Creates an AxiomEngineException with a formatted message and cause.
     *
     * @param messageTemplate The message template to format
     * @param cause The cause of the exception
     * @param args Arguments for the message template
     * @return A new AxiomEngineException
     */
    public static AxiomEngineException of(String messageTemplate, Throwable cause, Object... args) {
        return new AxiomEngineException(String.format(messageTemplate, args), cause);
    }
} 