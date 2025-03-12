package com.lyxtera.axiom.api.exception;

/**
 * Exception thrown for errors encountered during rule loading and validation.
 * This includes I/O errors, parsing errors, and validation failures.
 */
public class RuleLoadException extends AxiomEngineException {

    /**
     * Constructs a new RuleLoadException with the specified message.
     *
     * @param message The detail message
     */
    public RuleLoadException(String message) {
        super(message);
    }

    /**
     * Constructs a new RuleLoadException with the specified message and cause.
     *
     * @param message The detail message
     * @param cause The cause of the exception
     */
    public RuleLoadException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a RuleLoadException with a formatted message.
     *
     * @param messageTemplate The message template to format
     * @param args Arguments for the message template
     * @return A new RuleLoadException
     */
    public static RuleLoadException of(String messageTemplate, Object... args) {
        return new RuleLoadException(String.format(messageTemplate, args));
    }

    /**
     * Creates a RuleLoadException with a formatted message and cause.
     *
     * @param messageTemplate The message template to format
     * @param cause The cause of the exception
     * @param args Arguments for the message template
     * @return A new RuleLoadException
     */
    public static RuleLoadException of(String messageTemplate, Throwable cause, Object... args) {
        return new RuleLoadException(String.format(messageTemplate, args), cause);
    }
    
    /**
     * Creates a RuleLoadException for an error loading a ruleset.
     *
     * @param source The source of the ruleset
     * @param errorMessage The error message
     * @param cause The cause of the exception
     * @return A new RuleLoadException
     */
    public static RuleLoadException loadError(String source, String errorMessage) {
        return of(MSG_RULESET_LOAD_ERROR, source, errorMessage);
    }
    
    /**
     * Creates a RuleLoadException for an error parsing a ruleset.
     *
     * @param errorMessage The error message
     * @param cause The cause of the exception
     * @return A new RuleLoadException
     */
    public static RuleLoadException parseError(String errorMessage, Throwable cause) {
        return of(MSG_RULESET_PARSE_ERROR, cause, errorMessage);
    }
    
    /**
     * Creates a RuleLoadException for an error parsing a specific rule.
     *
     * @param ruleName The name of the rule that failed to parse
     * @param expression The expression of the rule that failed to parse
     * @param cause The cause of the exception
     * @return A new RuleLoadException
     */
    public static RuleLoadException ruleParseError(String ruleName, String expression, Throwable cause) {
        return of(MSG_RULE_PARSE_ERROR, cause, ruleName, expression);
    }
    
    /**
     * Creates a RuleLoadException for validation errors.
     *
     * @param errors The validation error messages
     * @return A new RuleLoadException
     */
    public static RuleLoadException validationErrors(String errors) {
        return of(MSG_VALIDATION_ERRORS, errors);
    }
} 