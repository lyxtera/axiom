package com.lyxtera.axiom.engine;

import static com.lyxtera.axiom.api.exception.AxiomEngineException.MSG_RULE_EVALUATION_FAILED;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.lyxtera.axiom.api.exception.AxiomEngineException;
import com.lyxtera.axiom.api.exception.DynamicRuleValidationException;
import com.lyxtera.axiom.api.model.BusinessRule;
import com.lyxtera.axiom.api.model.DynamicRuleRequest;
import com.lyxtera.axiom.api.model.RuleSetDescriptor;
import com.lyxtera.axiom.api.parser.Parser;
import com.lyxtera.axiom.api.validation.EntityPermissionValidator;
import com.lyxtera.axiom.api.validation.PermissionValidationResult;

/**
 * Orchestrates rule execution for a specific rule set.
 * <p>
 * This class provides methods to execute rules from a specific rule set
 * against a given context, and also provides rule parsing functionality.
 *
 * @param <K> The enum type to be used as context keys
 */
public class RuleOrchestrator<K extends Enum<K>> {
    
    private final RuleSet<K> ruleSet;
    private final Parser<K> parser;
    private final EntityPermissionValidator permissionValidator;
    
    /**
     * Creates a new rule orchestrator.
     *
     * @param ruleSet The rule set to orchestrate
     */
    public RuleOrchestrator(RuleSet<K> ruleSet) {
        this.ruleSet = ruleSet;
        this.parser = null;
        this.permissionValidator = new EntityPermissionValidator();
        ruleSet.validate();
    }
    
    /**
     * Creates a new rule orchestrator with parser support for dynamic rules.
     *
     * @param ruleSet The rule set to orchestrate
     * @param parser The parser for dynamic rule expressions
     */
    public RuleOrchestrator(RuleSet<K> ruleSet, Parser<K> parser) {
        this.ruleSet = ruleSet;
        this.parser = parser;
        this.permissionValidator = new EntityPermissionValidator();
        ruleSet.validate();
    }

    /**
     * Gets the first matching rule in the rule set against the given context.
     *
     * @param context The context to execute the rule against
     * @return The first matching rule, or empty if no rule matches
     */
    public Optional<BusinessRule<K>> getFirstMatchingRule(RuleContext<K> context) {
        return ruleSet.getRulesInPriorityOrder().stream()
            .filter(rule -> evaluateCondition(rule, context))
            .findFirst();
    }
    
    /**
     * Executes all rules in the rule set against the given context.
     * <p>
     * This method evaluates all rules in the rule set and executes their
     * actions if their conditions are met.
     *
     * @param context The context to execute the rules against
     * @return A result object containing details about the rule execution
     */
    public RuleExecutionResult<K> executeAllMatchingRules(RuleContext<K> context) {
        try {
            List<BusinessRule<K>> matchedRules = new ArrayList<>();
            Map<BusinessRule<K>, Boolean> executedRules = new LinkedHashMap<>();
            BusinessRule<K> firstMatchedRule = null;
            Boolean firstRuleResult = null;
            
            for (BusinessRule<K> rule : ruleSet.getRulesInPriorityOrder()) {
                if (evaluateCondition(rule, context)) {
                    matchedRules.add(rule);
                    
                    // Execute the rule and store the result
                    boolean result = rule.evaluate(context);
                    executedRules.put(rule, result);
                    
                    // Keep track of the first matched rule and its result
                    if (firstMatchedRule == null) {
                        firstMatchedRule = rule;
                        firstRuleResult = result;
                    }
                }
            }
            
            if (matchedRules.isEmpty()) {
                return RuleExecutionResult.empty();
            }
            
            return RuleExecutionResult.multiple(
                matchedRules,
                executedRules,
                firstMatchedRule,
                firstRuleResult
            );
        } catch (Exception e) {
            return RuleExecutionResult.failure(String.format("Execution failed. %s", e.getMessage()));
        }
    }

    /**
     * Executes the first matching rule in the rule set against the given context.
     * <p>
     * This method finds the first rule in the rule set whose condition is met,
     * executes its actions, and returns the execution result.
     *
     * @param context The context to execute the rule against
     * @return A result object containing details about the rule execution
     */
    public RuleExecutionResult<K> executeFirstMatchingRule(RuleContext<K> context) {
        try {
            Optional<BusinessRule<K>> ruleOpt = getFirstMatchingRule(context);
            
            if (ruleOpt.isEmpty()) {
                return RuleExecutionResult.empty();
            }
            
            BusinessRule<K> rule = ruleOpt.get();
            boolean result = rule.evaluate(context);
            
            return RuleExecutionResult.single(rule, result);
        } catch (Exception e) {
            return RuleExecutionResult.failure("Error executing rule: " + e.getMessage());
        }
    }
    
    /**
     * Gets the underlying rule set.
     *
     * @return The rule set
     */
    public RuleSet<K> getRuleSet() {
        return ruleSet;
    }
    
    /**
     * Executes externally supplied dynamic rules against the given context.
     * <p>
     * This method validates entity permissions and ruleset configuration before execution.
     * Dynamic rules are parsed and executed using the same engine as static rules.
     *
     * @param context The context to execute rules against
     * @param ruleExpressions List of rule expressions to execute
     * @param entityName The name of the requesting entity
     * @return Result of dynamic rule execution with permission validation
     * @throws DynamicRuleValidationException if validation fails
     */
    public RuleExecutionResult<K> executeDynamicRules(
            RuleContext<K> context, 
            List<String> ruleExpressions, 
            String entityName) {
        
        if (parser == null) {
            throw new DynamicRuleValidationException(
                "Parser not available - RuleOrchestrator must be created with a Parser to support dynamic rules");
        }
        
        // Create a basic dynamic rule request
        DynamicRuleRequest<K> request = new DynamicRuleRequest<>(entityName, 
            ruleSet.getMetadata().getRuleSetName(), ruleExpressions);
        
        return executeDynamicRuleSet(context, request);
    }
    
    /**
     * Executes a complete dynamic rule request with full validation.
     * <p>
     * This method performs comprehensive validation including:
     * - Ruleset allows dynamic execution
     * - Entity has required permissions
     * - Rule expressions are valid
     * - Functions exist in the ruleset
     *
     * @param context The context to execute rules against  
     * @param request The dynamic rule request
     * @return Result of dynamic rule execution
     * @throws DynamicRuleValidationException if validation fails
     */
    public RuleExecutionResult<K> executeDynamicRuleSet(
            RuleContext<K> context, 
            DynamicRuleRequest<K> request) {
        
        if (parser == null) {
            throw new DynamicRuleValidationException(
                "Parser not available - RuleOrchestrator must be created with a Parser to support dynamic rules");
        }
        
        // Validate the request
        validateDynamicRuleRequest(request);
        
        // Check if dynamic execution is allowed
        if (!ruleSet.getMetadata().isAllowDynamicExecution()) {
            throw DynamicRuleValidationException.dynamicExecutionNotAllowed(
                ruleSet.getMetadata().getRuleSetName());
        }
        
        // Create temporary RuleSetDescriptor for validation
        RuleSetDescriptor tempDescriptor = createTempDescriptor();
        
        // Validate entity permissions
        PermissionValidationResult validationResult = permissionValidator.validateEntityPermissions(
            request.getEntityName(), request.getRuleExpressions(), tempDescriptor);
        
        if (!validationResult.isValid()) {
            throw new DynamicRuleValidationException(validationResult);
        }
        
        try {
            // Convert rule expressions to BusinessRule objects
            List<BusinessRule<K>> dynamicRules = parseDynamicRules(request);
            
            // Execute the dynamic rules
            return executeDynamicBusinessRules(context, dynamicRules, request);
            
        } catch (Exception e) {
            if (e instanceof DynamicRuleValidationException) {
                throw e;
            }
            throw new DynamicRuleValidationException(
                "Failed to execute dynamic rules for entity '" + request.getEntityName() + "'", e);
        }
    }
    
    /**
     * Validates a dynamic rule request for basic completeness and consistency.
     *
     * @param request The request to validate
     * @throws DynamicRuleValidationException if validation fails
     */
    private void validateDynamicRuleRequest(DynamicRuleRequest<K> request) {
        if (request == null) {
            throw new DynamicRuleValidationException("Dynamic rule request cannot be null");
        }
        
        if (!request.isValid()) {
            throw new DynamicRuleValidationException(
                "Invalid dynamic rule request: " + request.toString());
        }
        
        if (!request.getRulesetName().equals(ruleSet.getMetadata().getRuleSetName())) {
            throw new DynamicRuleValidationException(
                "Request ruleset '" + request.getRulesetName() + "' does not match orchestrator ruleset '" 
                + ruleSet.getMetadata().getRuleSetName() + "'");
        }
    }
    
    /**
     * Creates a temporary RuleSetDescriptor from the current ruleset metadata.
     * This is used for permission validation.
     *
     * @return A temporary RuleSetDescriptor
     */
    private RuleSetDescriptor createTempDescriptor() {
        RuleSetDescriptor descriptor = new RuleSetDescriptor();
        RuleSet.Metadata metadata = ruleSet.getMetadata();
        
        descriptor.setRulesetName(metadata.getRuleSetName());
        descriptor.setRulesetDescription(metadata.getRuleSetDescription());
        descriptor.setAllowDynamicExecution(metadata.isAllowDynamicExecution());
        
        // Convert metadata maps to lists
        List<RuleSetDescriptor.BusinessCheckDescriptor> checks = new ArrayList<>();
        metadata.getBusinessCheckDescriptors().values().forEach(checks::add);
        descriptor.setBusinessChecks(checks);
        
        List<RuleSetDescriptor.BusinessActionDescriptor> actions = new ArrayList<>();
        metadata.getBusinessActionDescriptors().values().forEach(actions::add);
        descriptor.setBusinessActions(actions);
        
        List<com.lyxtera.axiom.api.model.EntityPermissionDescriptor> permissions = new ArrayList<>();
        metadata.getEntityPermissions().values().forEach(permissions::add);
        descriptor.setEntityPermissions(permissions);
        
        return descriptor;
    }
    
    /**
     * Parses dynamic rule expressions into BusinessRule objects.
     *
     * @param request The dynamic rule request
     * @return List of parsed business rules
     * @throws DynamicRuleValidationException if parsing fails
     */
    private List<BusinessRule<K>> parseDynamicRules(DynamicRuleRequest<K> request) {
        List<BusinessRule<K>> rules = new ArrayList<>();
        RuleSet.Metadata metadata = ruleSet.getMetadata();
        
        for (int i = 0; i < request.getRuleExpressions().size(); i++) {
            String expression = request.getRuleExpressions().get(i);
            
            try {
                // Generate a name for the dynamic rule
                String ruleName = "DynamicRule_" + request.getEntityName() + "_" + (i + 1);
                
                // Parse the rule expression
                BusinessRule<K> rule = parser.parseRule(metadata, ruleName, expression);
                rules.add(rule);
                
            } catch (Exception e) {
                throw DynamicRuleValidationException.ruleParsingFailed(expression, e);
            }
        }
        
        return rules;
    }
    
    /**
     * Executes a list of dynamic business rules against the context.
     *
     * @param context The execution context
     * @param dynamicRules The dynamic rules to execute
     * @param request The original request (for metadata)
     * @return The execution result
     */
    private RuleExecutionResult<K> executeDynamicBusinessRules(
            RuleContext<K> context, 
            List<BusinessRule<K>> dynamicRules, 
            DynamicRuleRequest<K> request) {
        
        try {
            List<BusinessRule<K>> matchedRules = new ArrayList<>();
            Map<BusinessRule<K>, Boolean> executedRules = new LinkedHashMap<>();
            BusinessRule<K> firstMatchedRule = null;
            Boolean firstRuleResult = null;
            
            // Execute dynamic rules in order
            for (BusinessRule<K> rule : dynamicRules) {
                if (evaluateCondition(rule, context)) {
                    matchedRules.add(rule);
                    
                    // Execute the rule and store the result
                    boolean result = rule.evaluate(context);
                    executedRules.put(rule, result);
                    
                    // Keep track of the first matched rule and its result
                    if (firstMatchedRule == null) {
                        firstMatchedRule = rule;
                        firstRuleResult = result;
                    }
                }
            }
            
            if (matchedRules.isEmpty()) {
                return RuleExecutionResult.empty();
            }
            
            return RuleExecutionResult.multiple(
                matchedRules,
                executedRules,
                firstMatchedRule,
                firstRuleResult
            );
            
        } catch (Exception e) {
            return RuleExecutionResult.failure(
                String.format("Dynamic rule execution failed for entity '%s': %s", 
                    request.getEntityName(), e.getMessage()));
        }
    }

    private boolean evaluateCondition(BusinessRule<K> rule, RuleContext<K> context) {
        try {
            return rule.getCondition().evaluate(context);
        } catch (AxiomEngineException e) {
            throw AxiomEngineException.of(MSG_RULE_EVALUATION_FAILED, rule.getName(), e.getMessage());
        }
    }
} 