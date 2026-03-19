package com.lyxtera.axiom.api.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents the result of entity permission validation for dynamic rule execution.
 * <p>
 * This class contains information about whether the validation passed,
 * any violations found, and details about what was validated.
 */
public class PermissionValidationResult {
    
    /**
     * Whether the permission validation passed.
     */
    private final boolean valid;
    
    /**
     * The entity name that was validated.
     */
    private final String entityName;
    
    /**
     * List of permission violations found during validation.
     */
    private final List<PermissionViolation> violations;
    
    /**
     * Additional validation message or context.
     */
    private final String message;
    
    /**
     * Creates a successful validation result.
     *
     * @param entityName The entity name that was validated
     * @return A successful validation result
     */
    public static PermissionValidationResult success(String entityName) {
        return new PermissionValidationResult(true, entityName, Collections.emptyList(), "Validation passed");
    }
    
    /**
     * Creates a successful validation result with a custom message.
     *
     * @param entityName The entity name that was validated
     * @param message The success message
     * @return A successful validation result
     */
    public static PermissionValidationResult success(String entityName, String message) {
        return new PermissionValidationResult(true, entityName, Collections.emptyList(), message);
    }
    
    /**
     * Creates a failed validation result with violations.
     *
     * @param entityName The entity name that was validated
     * @param violations The list of violations found
     * @return A failed validation result
     */
    public static PermissionValidationResult failure(String entityName, List<PermissionViolation> violations) {
        String message = String.format("Validation failed with %d violation(s)", violations.size());
        return new PermissionValidationResult(false, entityName, violations, message);
    }
    
    /**
     * Creates a failed validation result with a single violation.
     *
     * @param entityName The entity name that was validated
     * @param violation The violation found
     * @return A failed validation result
     */
    public static PermissionValidationResult failure(String entityName, PermissionViolation violation) {
        List<PermissionViolation> violations = new ArrayList<>();
        violations.add(violation);
        return failure(entityName, violations);
    }
    
    /**
     * Creates a failed validation result with a custom message.
     *
     * @param entityName The entity name that was validated
     * @param message The failure message
     * @return A failed validation result
     */
    public static PermissionValidationResult failure(String entityName, String message) {
        return new PermissionValidationResult(false, entityName, Collections.emptyList(), message);
    }
    
    private PermissionValidationResult(boolean valid, String entityName, List<PermissionViolation> violations, String message) {
        this.valid = valid;
        this.entityName = entityName;
        this.violations = violations != null ? Collections.unmodifiableList(new ArrayList<>(violations)) : Collections.emptyList();
        this.message = message;
    }
    
    /**
     * Gets whether the validation passed.
     *
     * @return true if validation passed, false otherwise
     */
    public boolean isValid() {
        return valid;
    }
    
    /**
     * Gets the entity name that was validated.
     *
     * @return The entity name
     */
    public String getEntityName() {
        return entityName;
    }
    
    /**
     * Gets the list of permission violations.
     *
     * @return The list of violations (empty if validation passed)
     */
    public List<PermissionViolation> getViolations() {
        return violations;
    }
    
    /**
     * Gets the validation message.
     *
     * @return The validation message
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Gets whether there are any violations.
     *
     * @return true if there are violations, false otherwise
     */
    public boolean hasViolations() {
        return !violations.isEmpty();
    }
    
    /**
     * Gets the number of violations.
     *
     * @return The number of violations
     */
    public int getViolationCount() {
        return violations.size();
    }
    
    /**
     * Gets a detailed error message including all violations.
     *
     * @return A detailed error message
     */
    public String getDetailedMessage() {
        if (valid) {
            return message;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(message);
        
        if (!violations.isEmpty()) {
            sb.append(": ");
            for (int i = 0; i < violations.size(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(violations.get(i).getMessage());
            }
        }
        
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PermissionValidationResult that = (PermissionValidationResult) o;
        return valid == that.valid &&
               Objects.equals(entityName, that.entityName) &&
               Objects.equals(violations, that.violations) &&
               Objects.equals(message, that.message);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(valid, entityName, violations, message);
    }
    
    @Override
    public String toString() {
        return "PermissionValidationResult{" +
               "valid=" + valid +
               ", entityName='" + entityName + '\'' +
               ", violations=" + violations +
               ", message='" + message + '\'' +
               '}';
    }
    
    /**
     * Represents a specific permission violation.
     */
    public static class PermissionViolation {
        
        private final String functionName;
        private final ViolationType type;
        private final String message;
        
        /**
         * Types of permission violations.
         */
        public enum ViolationType {
            /** Function is explicitly denied */
            EXPLICITLY_DENIED,
            /** Function is not in the allowed list */
            NOT_ALLOWED,
            /** Function does not exist in the ruleset */
            FUNCTION_NOT_FOUND,
            /** Entity has no permissions defined */
            NO_PERMISSIONS
        }
        
        public PermissionViolation(String functionName, ViolationType type, String message) {
            this.functionName = functionName;
            this.type = type;
            this.message = message;
        }
        
        /**
         * Gets the function name that caused the violation.
         *
         * @return The function name
         */
        public String getFunctionName() {
            return functionName;
        }
        
        /**
         * Gets the type of violation.
         *
         * @return The violation type
         */
        public ViolationType getType() {
            return type;
        }
        
        /**
         * Gets the violation message.
         *
         * @return The violation message
         */
        public String getMessage() {
            return message;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PermissionViolation that = (PermissionViolation) o;
            return Objects.equals(functionName, that.functionName) &&
                   type == that.type &&
                   Objects.equals(message, that.message);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(functionName, type, message);
        }
        
        @Override
        public String toString() {
            return "PermissionViolation{" +
                   "functionName='" + functionName + '\'' +
                   ", type=" + type +
                   ", message='" + message + '\'' +
                   '}';
        }
    }
}

