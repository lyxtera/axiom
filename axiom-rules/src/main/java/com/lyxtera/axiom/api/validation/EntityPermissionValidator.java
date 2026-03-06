package com.lyxtera.axiom.api.validation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.lyxtera.axiom.api.model.EntityPermissionDescriptor;
import com.lyxtera.axiom.api.model.RuleSetDescriptor;
import com.lyxtera.axiom.api.validation.PermissionValidationResult.PermissionViolation;
import com.lyxtera.axiom.api.validation.PermissionValidationResult.PermissionViolation.ViolationType;

/**
 * Validates entity permissions for dynamic rule execution.
 * <p>
 * This class is responsible for checking whether a given entity has permission
 * to use specific business functions (checks and actions) in dynamic rules.
 */
public class EntityPermissionValidator {
    
    // Pattern to extract function names from rule expressions
    private static final Pattern FUNCTION_PATTERN = Pattern.compile("([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(");
    
    /**
     * Validates whether an entity has permission to execute the specified rule expressions
     * against the given ruleset.
     *
     * @param entityName The name of the entity requesting permission
     * @param ruleExpressions The list of rule expressions to validate
     * @param ruleSetDescriptor The ruleset descriptor containing permission definitions
     * @return The validation result
     */
    public PermissionValidationResult validateEntityPermissions(
            String entityName, 
            List<String> ruleExpressions, 
            RuleSetDescriptor ruleSetDescriptor) {
        
        if (entityName == null || entityName.trim().isEmpty()) {
            return PermissionValidationResult.failure(entityName, "Entity name cannot be null or empty");
        }
        
        if (ruleExpressions == null || ruleExpressions.isEmpty()) {
            return PermissionValidationResult.success(entityName, "No rule expressions to validate");
        }
        
        if (ruleSetDescriptor == null) {
            return PermissionValidationResult.failure(entityName, "RuleSet descriptor cannot be null");
        }
        
        // Check if dynamic execution is allowed for this ruleset
        if (!ruleSetDescriptor.isAllowDynamicExecution()) {
            return PermissionValidationResult.failure(entityName, 
                "Ruleset '" + ruleSetDescriptor.getRulesetName() + "' does not allow dynamic execution");
        }
        
        // Find entity permissions
        EntityPermissionDescriptor entityPermission = ruleSetDescriptor.findEntityPermission(entityName);
        if (entityPermission == null) {
            return PermissionValidationResult.failure(entityName, 
                new PermissionViolation(null, ViolationType.NO_PERMISSIONS, 
                "No permissions defined for entity '" + entityName + "'"));
        }
        
        // Extract all function names from rule expressions
        Set<String> usedFunctions = extractFunctionNames(ruleExpressions);
        
        // Get available functions from ruleset
        Set<String> availableFunctions = getAvailableFunctions(ruleSetDescriptor);
        
        // Validate each function
        List<PermissionViolation> violations = new ArrayList<>();
        for (String functionName : usedFunctions) {
            PermissionViolation violation = validateFunction(functionName, entityPermission, availableFunctions);
            if (violation != null) {
                violations.add(violation);
            }
        }
        
        if (violations.isEmpty()) {
            return PermissionValidationResult.success(entityName, 
                "Entity '" + entityName + "' has permission to use all " + usedFunctions.size() + " function(s)");
        } else {
            return PermissionValidationResult.failure(entityName, violations);
        }
    }
    
    /**
     * Validates whether an entity has permission to use a specific function.
     *
     * @param entityName The name of the entity
     * @param functionName The name of the function to check
     * @param ruleSetDescriptor The ruleset descriptor containing permission definitions
     * @return The validation result
     */
    public PermissionValidationResult validateSingleFunction(
            String entityName, 
            String functionName, 
            RuleSetDescriptor ruleSetDescriptor) {
        
        if (functionName == null || functionName.trim().isEmpty()) {
            return PermissionValidationResult.failure(entityName, "Function name cannot be null or empty");
        }
        
        List<String> mockExpression = List.of(functionName + "()");
        return validateEntityPermissions(entityName, mockExpression, ruleSetDescriptor);
    }
    
    /**
     * Extracts function names from rule expressions using regex pattern matching.
     *
     * @param ruleExpressions The list of rule expressions
     * @return Set of unique function names found
     */
    private Set<String> extractFunctionNames(List<String> ruleExpressions) {
        Set<String> functionNames = new HashSet<>();
        
        for (String expression : ruleExpressions) {
            if (expression == null) continue;
            
            Matcher matcher = FUNCTION_PATTERN.matcher(expression);
            while (matcher.find()) {
                String functionName = matcher.group(1);
                // Filter out common keywords that aren't functions
                if (!isKeyword(functionName)) {
                    functionNames.add(functionName);
                }
            }
        }
        
        return functionNames;
    }
    
    /**
     * Gets all available functions (checks and actions) from the ruleset descriptor.
     *
     * @param ruleSetDescriptor The ruleset descriptor
     * @return Set of available function names
     */
    private Set<String> getAvailableFunctions(RuleSetDescriptor ruleSetDescriptor) {
        Set<String> functions = new HashSet<>();
        
        // Add business checks
        if (ruleSetDescriptor.getBusinessChecks() != null) {
            ruleSetDescriptor.getBusinessChecks().forEach(check -> {
                if (check.getName() != null) {
                    functions.add(check.getName());
                }
            });
        }
        
        // Add business actions
        if (ruleSetDescriptor.getBusinessActions() != null) {
            ruleSetDescriptor.getBusinessActions().forEach(action -> {
                if (action.getName() != null) {
                    functions.add(action.getName());
                }
            });
        }
        
        return functions;
    }
    
    /**
     * Validates a single function against entity permissions.
     *
     * @param functionName The function name to validate
     * @param entityPermission The entity permission descriptor
     * @param availableFunctions Set of available functions in the ruleset
     * @return A permission violation if validation fails, null if successful
     */
    private PermissionViolation validateFunction(
            String functionName, 
            EntityPermissionDescriptor entityPermission, 
            Set<String> availableFunctions) {
        
        // Check if function exists in the ruleset
        if (!availableFunctions.contains(functionName)) {
            return new PermissionViolation(functionName, ViolationType.FUNCTION_NOT_FOUND,
                "Function '" + functionName + "' is not defined in the ruleset");
        }
        
        // Check if function is explicitly denied
        if (entityPermission.getDeniedFunctions().contains(functionName)) {
            return new PermissionViolation(functionName, ViolationType.EXPLICITLY_DENIED,
                "Function '" + functionName + "' is explicitly denied for entity '" + entityPermission.getName() + "'");
        }
        
        // Check if function is allowed
        if (!entityPermission.getAllowedFunctions().contains(functionName)) {
            return new PermissionViolation(functionName, ViolationType.NOT_ALLOWED,
                "Function '" + functionName + "' is not in the allowed list for entity '" + entityPermission.getName() + "'");
        }
        
        return null; // No violation
    }
    
    /**
     * Checks if a string is a reserved keyword that shouldn't be treated as a function.
     *
     * @param word The word to check
     * @return true if it's a keyword, false otherwise
     */
    private boolean isKeyword(String word) {
        return word.equals("then") || 
               word.equals("and") || 
               word.equals("or") || 
               word.equals("not") ||
               word.equals("true") ||
               word.equals("false");
    }
    
    /**
     * Gets a summary of functions used in the given rule expressions.
     *
     * @param ruleExpressions The rule expressions to analyze
     * @return A summary string listing the functions found
     */
    public String getFunctionUsageSummary(List<String> ruleExpressions) {
        Set<String> functions = extractFunctionNames(ruleExpressions);
        if (functions.isEmpty()) {
            return "No functions found in rule expressions";
        }
        return "Functions found: " + String.join(", ", functions);
    }
    
    /**
     * Checks if an entity has permission to use any functions at all.
     *
     * @param entityName The entity name
     * @param ruleSetDescriptor The ruleset descriptor
     * @return true if the entity has any permissions, false otherwise
     */
    public boolean hasAnyPermissions(String entityName, RuleSetDescriptor ruleSetDescriptor) {
        if (entityName == null || ruleSetDescriptor == null) {
            return false;
        }
        
        EntityPermissionDescriptor permission = ruleSetDescriptor.findEntityPermission(entityName);
        return permission != null && !permission.getAllowedFunctions().isEmpty();
    }
}

