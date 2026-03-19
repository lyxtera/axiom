package com.lyxtera.axiom.api.exception;

import java.util.List;

import com.lyxtera.axiom.api.validation.PermissionValidationResult;
import com.lyxtera.axiom.api.validation.PermissionValidationResult.PermissionViolation;

/**
 * Exception thrown when dynamic rule validation fails.
 * <p>
 * This exception is thrown when:
 * - An entity doesn't have permission to use certain functions
 * - A ruleset doesn't allow dynamic execution
 * - Rule expressions are malformed or invalid
 * - Permission validation fails for other reasons
 */
public class DynamicRuleValidationException extends AxiomEngineException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * The entity name that caused the validation failure.
     */
    private final String entityName;
    
    /**
     * The validation result containing detailed violation information.
     */
    private final PermissionValidationResult validationResult;
    
    /**
     * Creates a new DynamicRuleValidationException with a simple message.
     *
     * @param message The exception message
     */
    public DynamicRuleValidationException(String message) {
        super(message);
        this.entityName = null;
        this.validationResult = null;
    }
    
    /**
     * Creates a new DynamicRuleValidationException with a message and cause.
     *
     * @param message The exception message
     * @param cause The underlying cause
     */
    public DynamicRuleValidationException(String message, Throwable cause) {
        super(message, cause);
        this.entityName = null;
        this.validationResult = null;
    }
    
    /**
     * Creates a new DynamicRuleValidationException from a validation result.
     *
     * @param validationResult The permission validation result that failed
     */
    public DynamicRuleValidationException(PermissionValidationResult validationResult) {
        super(buildMessageFromValidationResult(validationResult));
        this.entityName = validationResult != null ? validationResult.getEntityName() : null;
        this.validationResult = validationResult;
    }
    
    /**
     * Creates a new DynamicRuleValidationException with entity information.
     *
     * @param entityName The entity name
     * @param message The exception message
     */
    public DynamicRuleValidationException(String entityName, String message) {
        super(message);
        this.entityName = entityName;
        this.validationResult = null;
    }
    
    /**
     * Creates a new DynamicRuleValidationException with entity information and cause.
     *
     * @param entityName The entity name
     * @param message The exception message
     * @param cause The underlying cause
     */
    public DynamicRuleValidationException(String entityName, String message, Throwable cause) {
        super(message, cause);
        this.entityName = entityName;
        this.validationResult = null;
    }
    
    /**
     * Gets the entity name that caused the validation failure.
     *
     * @return The entity name, or null if not available
     */
    public String getEntityName() {
        return entityName;
    }
    
    /**
     * Gets the permission validation result.
     *
     * @return The validation result, or null if not available
     */
    public PermissionValidationResult getValidationResult() {
        return validationResult;
    }
    
    /**
     * Gets the list of permission violations.
     *
     * @return The list of violations, or null if not available
     */
    public List<PermissionViolation> getViolations() {
        return validationResult != null ? validationResult.getViolations() : null;
    }
    
    /**
     * Gets whether this exception was caused by permission violations.
     *
     * @return true if there are permission violations, false otherwise
     */
    public boolean hasPermissionViolations() {
        return validationResult != null && validationResult.hasViolations();
    }
    
    /**
     * Gets the number of permission violations.
     *
     * @return The number of violations, or 0 if none
     */
    public int getViolationCount() {
        return validationResult != null ? validationResult.getViolationCount() : 0;
    }
    
    /**
     * Gets a detailed message including all violation details.
     *
     * @return A detailed error message
     */
    public String getDetailedMessage() {
        if (validationResult != null) {
            return validationResult.getDetailedMessage();
        }
        return getMessage();
    }
    
    /**
     * Builds an exception message from a validation result.
     *
     * @param validationResult The validation result
     * @return An appropriate exception message
     */
    private static String buildMessageFromValidationResult(PermissionValidationResult validationResult) {
        if (validationResult == null) {
            return "Dynamic rule validation failed";
        }
        
        if (validationResult.isValid()) {
            return "Validation passed but exception was created - this should not happen";
        }
        
        StringBuilder message = new StringBuilder();
        message.append("Dynamic rule validation failed for entity '")
               .append(validationResult.getEntityName())
               .append("'");
        
        if (validationResult.hasViolations()) {
            message.append(" with ").append(validationResult.getViolationCount()).append(" violation(s)");
        }
        
        if (validationResult.getMessage() != null) {
            message.append(": ").append(validationResult.getMessage());
        }
        
        return message.toString();
    }
    
    // Static factory methods for common scenarios
    
    /**
     * Creates an exception for when dynamic execution is not allowed.
     *
     * @param rulesetName The name of the ruleset
     * @return A new exception instance
     */
    public static DynamicRuleValidationException dynamicExecutionNotAllowed(String rulesetName) {
        return new DynamicRuleValidationException(
            "Ruleset '" + rulesetName + "' does not allow dynamic rule execution");
    }
    
    /**
     * Creates an exception for when an entity has no permissions.
     *
     * @param entityName The entity name
     * @return A new exception instance
     */
    public static DynamicRuleValidationException noPermissionsForEntity(String entityName) {
        return new DynamicRuleValidationException(entityName,
            "No permissions defined for entity '" + entityName + "'");
    }
    
    /**
     * Creates an exception for when a function is not allowed.
     *
     * @param entityName The entity name
     * @param functionName The function name
     * @return A new exception instance
     */
    public static DynamicRuleValidationException functionNotAllowed(String entityName, String functionName) {
        return new DynamicRuleValidationException(entityName,
            "Entity '" + entityName + "' is not allowed to use function '" + functionName + "'");
    }
    
    /**
     * Creates an exception for when a function is explicitly denied.
     *
     * @param entityName The entity name
     * @param functionName The function name
     * @return A new exception instance
     */
    public static DynamicRuleValidationException functionDenied(String entityName, String functionName) {
        return new DynamicRuleValidationException(entityName,
            "Function '" + functionName + "' is explicitly denied for entity '" + entityName + "'");
    }
    
    /**
     * Creates an exception for when a function doesn't exist.
     *
     * @param functionName The function name
     * @param rulesetName The ruleset name
     * @return A new exception instance
     */
    public static DynamicRuleValidationException functionNotFound(String functionName, String rulesetName) {
        return new DynamicRuleValidationException(
            "Function '" + functionName + "' is not defined in ruleset '" + rulesetName + "'");
    }
    
    /**
     * Creates an exception for rule parsing failures.
     *
     * @param ruleExpression The rule expression that failed to parse
     * @param cause The underlying parsing exception
     * @return A new exception instance
     */
    public static DynamicRuleValidationException ruleParsingFailed(String ruleExpression, Throwable cause) {
        return new DynamicRuleValidationException(
            "Failed to parse dynamic rule expression: " + ruleExpression, cause);
    }
}

