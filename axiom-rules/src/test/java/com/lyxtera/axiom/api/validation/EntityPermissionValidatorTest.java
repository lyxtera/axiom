package com.lyxtera.axiom.api.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.lyxtera.axiom.api.model.EntityPermissionDescriptor;
import com.lyxtera.axiom.api.model.RuleSetDescriptor;
import com.lyxtera.axiom.api.validation.PermissionValidationResult.PermissionViolation.ViolationType;

/**
 * Unit tests for EntityPermissionValidator.
 */
public class EntityPermissionValidatorTest {
    
    private EntityPermissionValidator validator;
    private RuleSetDescriptor ruleSetDescriptor;
    
    @BeforeEach
    public void setUp() {
        validator = new EntityPermissionValidator();
        ruleSetDescriptor = createTestRuleSetDescriptor();
    }
    
    private RuleSetDescriptor createTestRuleSetDescriptor() {
        RuleSetDescriptor descriptor = new RuleSetDescriptor();
        descriptor.setRulesetName("Test Ruleset");
        descriptor.setAllowDynamicExecution(true);
        
        // Add business checks
        RuleSetDescriptor.BusinessCheckDescriptor check1 = new RuleSetDescriptor.BusinessCheckDescriptor();
        check1.setName("checkFunction1");
        check1.setDescription("Test check function 1");
        
        RuleSetDescriptor.BusinessCheckDescriptor check2 = new RuleSetDescriptor.BusinessCheckDescriptor();
        check2.setName("checkFunction2");
        check2.setDescription("Test check function 2");
        
        descriptor.setBusinessChecks(Arrays.asList(check1, check2));
        
        // Add business actions
        RuleSetDescriptor.BusinessActionDescriptor action1 = new RuleSetDescriptor.BusinessActionDescriptor();
        action1.setName("actionFunction1");
        action1.setDescription("Test action function 1");
        
        RuleSetDescriptor.BusinessActionDescriptor action2 = new RuleSetDescriptor.BusinessActionDescriptor();
        action2.setName("actionFunction2");
        action2.setDescription("Test action function 2");
        
        descriptor.setBusinessActions(Arrays.asList(action1, action2));
        
        // Add entity permissions
        EntityPermissionDescriptor permission1 = new EntityPermissionDescriptor("allowedEntity",
            Arrays.asList("checkFunction1", "actionFunction1", "checkFunction2"),
            Arrays.asList("actionFunction2"));
        
        EntityPermissionDescriptor permission2 = new EntityPermissionDescriptor("limitedEntity",
            Arrays.asList("checkFunction1"),
            Collections.emptyList());
        
        descriptor.setEntityPermissions(Arrays.asList(permission1, permission2));
        
        return descriptor;
    }
    
    @Test
    public void testValidEntityPermissions() {
        List<String> ruleExpressions = Arrays.asList(
            "checkFunction1() then actionFunction1()",
            "checkFunction2() then actionFunction1()"
        );
        
        PermissionValidationResult result = validator.validateEntityPermissions(
            "allowedEntity", ruleExpressions, ruleSetDescriptor);
        
        assertThat(result.isValid()).isTrue();
        assertThat(result.getEntityName()).isEqualTo("allowedEntity");
        assertThat(result.getViolations()).isEmpty();
        assertThat(result.getMessage()).contains("permission to use all");
    }
    
    @Test
    public void testEntityWithoutPermissions() {
        List<String> ruleExpressions = Arrays.asList("checkFunction1() then actionFunction1()");
        
        PermissionValidationResult result = validator.validateEntityPermissions(
            "unknownEntity", ruleExpressions, ruleSetDescriptor);
        
        assertThat(result.isValid()).isFalse();
        assertThat(result.getEntityName()).isEqualTo("unknownEntity");
        assertThat(result.hasViolations()).isTrue();
        assertThat(result.getViolations()).hasSize(1);
        assertThat(result.getViolations().get(0).getType()).isEqualTo(ViolationType.NO_PERMISSIONS);
    }
    
    @Test
    public void testExplicitlyDeniedFunction() {
        List<String> ruleExpressions = Arrays.asList("checkFunction1() then actionFunction2()");
        
        PermissionValidationResult result = validator.validateEntityPermissions(
            "allowedEntity", ruleExpressions, ruleSetDescriptor);
        
        assertThat(result.isValid()).isFalse();
        assertThat(result.hasViolations()).isTrue();
        assertThat(result.getViolations()).hasSize(1);
        assertThat(result.getViolations().get(0).getType()).isEqualTo(ViolationType.EXPLICITLY_DENIED);
        assertThat(result.getViolations().get(0).getFunctionName()).isEqualTo("actionFunction2");
    }
    
    @Test
    public void testFunctionNotAllowed() {
        List<String> ruleExpressions = Arrays.asList("checkFunction2() then actionFunction1()");
        
        PermissionValidationResult result = validator.validateEntityPermissions(
            "limitedEntity", ruleExpressions, ruleSetDescriptor);
        
        assertThat(result.isValid()).isFalse();
        assertThat(result.hasViolations()).isTrue();
        assertThat(result.getViolations()).hasSize(2); // checkFunction2 and actionFunction1 not allowed
        
        // Find the specific violations
        boolean foundCheckFunction2 = result.getViolations().stream()
            .anyMatch(v -> "checkFunction2".equals(v.getFunctionName()) && v.getType() == ViolationType.NOT_ALLOWED);
        boolean foundActionFunction1 = result.getViolations().stream()
            .anyMatch(v -> "actionFunction1".equals(v.getFunctionName()) && v.getType() == ViolationType.NOT_ALLOWED);
        
        assertThat(foundCheckFunction2).isTrue();
        assertThat(foundActionFunction1).isTrue();
    }
    
    @Test
    public void testFunctionNotFound() {
        List<String> ruleExpressions = Arrays.asList("nonExistentFunction() then actionFunction1()");
        
        PermissionValidationResult result = validator.validateEntityPermissions(
            "allowedEntity", ruleExpressions, ruleSetDescriptor);
        
        assertThat(result.isValid()).isFalse();
        assertThat(result.hasViolations()).isTrue();
        assertThat(result.getViolations()).hasSize(1);
        assertThat(result.getViolations().get(0).getType()).isEqualTo(ViolationType.FUNCTION_NOT_FOUND);
        assertThat(result.getViolations().get(0).getFunctionName()).isEqualTo("nonExistentFunction");
    }
    
    @Test
    public void testDynamicExecutionNotAllowed() {
        ruleSetDescriptor.setAllowDynamicExecution(false);
        
        List<String> ruleExpressions = Arrays.asList("checkFunction1() then actionFunction1()");
        
        PermissionValidationResult result = validator.validateEntityPermissions(
            "allowedEntity", ruleExpressions, ruleSetDescriptor);
        
        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).contains("does not allow dynamic execution");
    }
    
    @Test
    public void testValidateWithNullInputs() {
        List<String> ruleExpressions = Arrays.asList("checkFunction1() then actionFunction1()");
        
        // Null entity name
        PermissionValidationResult result1 = validator.validateEntityPermissions(
            null, ruleExpressions, ruleSetDescriptor);
        assertThat(result1.isValid()).isFalse();
        assertThat(result1.getMessage()).contains("cannot be null or empty");
        
        // Empty entity name
        PermissionValidationResult result2 = validator.validateEntityPermissions(
            "", ruleExpressions, ruleSetDescriptor);
        assertThat(result2.isValid()).isFalse();
        assertThat(result2.getMessage()).contains("cannot be null or empty");
        
        // Null rule expressions
        PermissionValidationResult result3 = validator.validateEntityPermissions(
            "allowedEntity", null, ruleSetDescriptor);
        assertThat(result3.isValid()).isTrue();
        assertThat(result3.getMessage()).contains("No rule expressions to validate");
        
        // Empty rule expressions
        PermissionValidationResult result4 = validator.validateEntityPermissions(
            "allowedEntity", Collections.emptyList(), ruleSetDescriptor);
        assertThat(result4.isValid()).isTrue();
        
        // Null ruleset descriptor
        PermissionValidationResult result5 = validator.validateEntityPermissions(
            "allowedEntity", ruleExpressions, null);
        assertThat(result5.isValid()).isFalse();
        assertThat(result5.getMessage()).contains("RuleSet descriptor cannot be null");
    }
    
    @Test
    public void testValidateSingleFunction() {
        // Valid function
        PermissionValidationResult result1 = validator.validateSingleFunction(
            "allowedEntity", "checkFunction1", ruleSetDescriptor);
        assertThat(result1.isValid()).isTrue();
        
        // Invalid function
        PermissionValidationResult result2 = validator.validateSingleFunction(
            "allowedEntity", "actionFunction2", ruleSetDescriptor);
        assertThat(result2.isValid()).isFalse();
        
        // Null function name
        PermissionValidationResult result3 = validator.validateSingleFunction(
            "allowedEntity", null, ruleSetDescriptor);
        assertThat(result3.isValid()).isFalse();
        assertThat(result3.getMessage()).contains("Function name cannot be null or empty");
    }
    
    @Test
    public void testGetFunctionUsageSummary() {
        List<String> ruleExpressions = Arrays.asList(
            "checkFunction1() and checkFunction2() then actionFunction1()",
            "actionFunction2() then checkFunction1()"
        );
        
        String summary = validator.getFunctionUsageSummary(ruleExpressions);
        
        assertThat(summary).contains("Functions found:");
        assertThat(summary).contains("checkFunction1");
        assertThat(summary).contains("checkFunction2");
        assertThat(summary).contains("actionFunction1");
        assertThat(summary).contains("actionFunction2");
    }
    
    @Test
    public void testGetFunctionUsageSummaryWithNoFunctions() {
        List<String> ruleExpressions = Arrays.asList("true then false");
        
        String summary = validator.getFunctionUsageSummary(ruleExpressions);
        
        assertThat(summary).isEqualTo("No functions found in rule expressions");
    }
    
    @Test
    public void testHasAnyPermissions() {
        // Entity with permissions
        assertThat(validator.hasAnyPermissions("allowedEntity", ruleSetDescriptor)).isTrue();
        assertThat(validator.hasAnyPermissions("limitedEntity", ruleSetDescriptor)).isTrue();
        
        // Entity without permissions
        assertThat(validator.hasAnyPermissions("unknownEntity", ruleSetDescriptor)).isFalse();
        
        // Null inputs
        assertThat(validator.hasAnyPermissions(null, ruleSetDescriptor)).isFalse();
        assertThat(validator.hasAnyPermissions("allowedEntity", null)).isFalse();
    }
    
    @Test
    public void testComplexRuleExpressions() {
        List<String> complexRules = Arrays.asList(
            "checkFunction1() and not checkFunction2() then actionFunction1()",
            "(checkFunction1() or checkFunction2()) and true then actionFunction1()",
            "checkFunction1() then actionFunction1(), actionFunction2()" // This should fail due to actionFunction2
        );
        
        PermissionValidationResult result = validator.validateEntityPermissions(
            "allowedEntity", complexRules, ruleSetDescriptor);
        
        assertThat(result.isValid()).isFalse();
        assertThat(result.hasViolations()).isTrue();
        
        // Should find actionFunction2 as explicitly denied
        boolean foundDeniedFunction = result.getViolations().stream()
            .anyMatch(v -> "actionFunction2".equals(v.getFunctionName()) && v.getType() == ViolationType.EXPLICITLY_DENIED);
        assertThat(foundDeniedFunction).isTrue();
    }
}

