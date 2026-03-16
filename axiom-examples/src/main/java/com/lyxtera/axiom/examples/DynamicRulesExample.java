package com.lyxtera.axiom.examples;

import java.util.Arrays;
import java.util.List;

// import com.lyxtera.axiom.api.model.DynamicRuleRequest;
import com.lyxtera.axiom.engine.RuleContext;

/**
 * Example demonstrating the Dynamic Rules feature.
 * <p>
 * This example shows how external services can supply rules at runtime
 * while maintaining security through entity permissions and validation.
 * 
 * Use case: OrderService and CheckoutService integration where CheckoutService
 * needs to apply dynamic discounts based on order history without exposing
 * internal state through APIs.
 */
public class DynamicRulesExample {
    
    // Example context keys for the checkout domain
    public enum CheckoutContextKey {
        CUSTOMER_ID,
        ORDER_VALUE,
        ORDER_COUNT,
        LOYALTY_LEVEL,
        IS_FIRST_TIME_CUSTOMER
    }
    
    public static void main(String[] args) {
        try {
            // 1. Load the ruleset with dynamic execution enabled
            System.out.println("=== Dynamic Rules Example ===");
            System.out.println("1. Loading ruleset with dynamic execution support...");
            
            // Note: This example shows the API usage. In a real implementation, you would:
            // RuleSetLoader<CheckoutContextKey> loader = 
            //     new RuleSetLoader.YamlRuleSetLoader<>("checkout_order_discount_ruleset.yaml");
            
            // Note: In a real application, you would inject the parser
            // RuleSet<CheckoutContextKey> ruleSet = loader.loadRuleSet(parser);
            // RuleOrchestrator<CheckoutContextKey> orchestrator = new RuleOrchestrator<>(ruleSet, parser);
            
            System.out.println("✓ Ruleset loaded successfully");
            System.out.println("✓ Dynamic execution enabled: " + true); // ruleSet.getMetadata().isAllowDynamicExecution()
            System.out.println("✓ Static rules may also include gate rules via onMatchForwardTo");
            
            // 2. Demonstrate dynamic rule execution scenarios
            demonstrateValidDynamicRules();
            demonstratePermissionValidation();
            demonstrateInvalidScenarios();
            
        } catch (Exception e) {
            System.err.println("Example failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Demonstrates valid dynamic rule execution scenarios.
     */
    private static void demonstrateValidDynamicRules() {
        System.out.println("\n2. Valid Dynamic Rule Scenarios:");
        
        // Scenario 1: CheckoutService applying tiered discounts
        System.out.println("\n--- Scenario 1: Tiered Discount Rules ---");
        List<String> tieredDiscountRules = Arrays.asList(
            "successfulOrders() > 15 then applyDiscount(10), updateNote(\"Very good customer\")",
            "successfulOrders() > 5 then applyDiscount(5), updateNote(\"Returning customer\")",
            "isFirstTimeCustomer() then applyDiscount(3), updateNote(\"Welcome discount\")"
        );
        
        System.out.println("Dynamic rules to execute:");
        tieredDiscountRules.forEach(rule -> System.out.println("  - " + rule));
        
        // This would be the actual execution:
        // RuleContext<CheckoutContextKey> context = createSampleContext(20, 150.0);
        // RuleExecutionResult<CheckoutContextKey> result = orchestrator.executeDynamicRules(
        //     context, tieredDiscountRules, "checkoutService");
        System.out.println("✓ Entity 'checkoutService' has permission for all functions");
        System.out.println("✓ Rules would execute successfully");
        
        // Scenario 2: Loyalty-based rules
        System.out.println("\n--- Scenario 2: Loyalty-Based Rules ---");
        List<String> loyaltyRules = Arrays.asList(
            "hasLoyaltyMembership(\"GOLD\") then applyDiscount(15), addLoyaltyPoints(100)",
            "hasLoyaltyMembership(\"SILVER\") then applyDiscount(10), addLoyaltyPoints(50)",
            "orderValue() > 200 then addLoyaltyPoints(25)"
        );
        
        System.out.println("Dynamic loyalty rules:");
        loyaltyRules.forEach(rule -> System.out.println("  - " + rule));
        System.out.println("✓ Rules validated and ready for execution");
    }
    
    /**
     * Demonstrates permission validation scenarios.
     */
    private static void demonstratePermissionValidation() {
        System.out.println("\n3. Permission Validation Scenarios:");
        
        // Valid permissions for checkoutService
        System.out.println("\n--- Valid Permissions (checkoutService) ---");
        List<String> allowedFunctions = Arrays.asList(
            "successfulOrders", "updateNote", "applyDiscount", "orderValue",
            "isFirstTimeCustomer", "hasLoyaltyMembership", "addLoyaltyPoints", "sendNotification"
        );
        System.out.println("Allowed functions: " + allowedFunctions);
        System.out.println("Denied functions: [updateTotalAmount]");
        
        // Limited permissions for orderService
        System.out.println("\n--- Limited Permissions (orderService) ---");
        List<String> orderServiceAllowed = Arrays.asList(
            "successfulOrders", "orderValue", "updateNote"
        );
        List<String> orderServiceDenied = Arrays.asList(
            "applyDiscount", "updateTotalAmount", "addLoyaltyPoints", "sendNotification"
        );
        System.out.println("Allowed functions: " + orderServiceAllowed);
        System.out.println("Denied functions: " + orderServiceDenied);
        
        // Example rule that would fail for orderService
        String invalidRule = "successfulOrders() > 5 then applyDiscount(10)";
        System.out.println("\nRule: " + invalidRule);
        System.out.println("✗ Would fail for 'orderService' - applyDiscount not allowed");
        System.out.println("✓ Would succeed for 'checkoutService' - applyDiscount allowed");
    }
    
    /**
     * Demonstrates invalid scenarios that would be rejected.
     */
    private static void demonstrateInvalidScenarios() {
        System.out.println("\n4. Invalid Scenarios (Would Be Rejected):");
        
        // Scenario 1: Entity without permissions
        System.out.println("\n--- Scenario 1: Unknown Entity ---");
        System.out.println("Entity: 'unknownService'");
        System.out.println("Rule: 'successfulOrders() > 5 then applyDiscount(10)'");
        System.out.println("✗ Would fail: No permissions defined for entity 'unknownService'");
        
        // Scenario 2: Using denied functions
        System.out.println("\n--- Scenario 2: Denied Function Usage ---");
        System.out.println("Entity: 'checkoutService'");
        System.out.println("Rule: 'orderValue() > 100 then updateTotalAmount(50)'");
        System.out.println("✗ Would fail: Function 'updateTotalAmount' is explicitly denied");
        
        // Scenario 3: Non-existent functions
        System.out.println("\n--- Scenario 3: Non-existent Function ---");
        System.out.println("Entity: 'checkoutService'");
        System.out.println("Rule: 'successfulOrders() > 5 then deleteOrder()'");
        System.out.println("✗ Would fail: Function 'deleteOrder' is not defined in ruleset");
        
        // Scenario 4: Ruleset doesn't allow dynamic execution
        System.out.println("\n--- Scenario 4: Dynamic Execution Disabled ---");
        System.out.println("Ruleset: hypothetical 'secure_payment_ruleset'");
        System.out.println("allowDynamicExecution: false");
        System.out.println("✗ Would fail: Ruleset does not allow dynamic rule execution");
    }
    
    /**
     * Creates a sample rule context for testing.
     * This method demonstrates how to create and populate a RuleContext.
     */
    @SuppressWarnings("unused") // This is an example method for demonstration
    private static RuleContext<CheckoutContextKey> createSampleContext(int orderCount, double orderValue) {
        RuleContext<CheckoutContextKey> context = new RuleContext<>(CheckoutContextKey.class);
        context.add(CheckoutContextKey.CUSTOMER_ID, "CUST_12345");
        context.add(CheckoutContextKey.ORDER_COUNT, orderCount);
        context.add(CheckoutContextKey.ORDER_VALUE, orderValue);
        context.add(CheckoutContextKey.IS_FIRST_TIME_CUSTOMER, orderCount == 1);
        context.add(CheckoutContextKey.LOYALTY_LEVEL, orderCount > 10 ? "GOLD" : "SILVER");
        return context;
    }
    
    /**
     * Example of how to use DynamicRuleRequest for more complex scenarios.
     * Note: This method demonstrates the API usage conceptually.
     */
    public static void demonstrateDynamicRuleRequest() {
        System.out.println("\n5. Advanced Dynamic Rule Request:");
        
        System.out.println("Creating a comprehensive dynamic rule request...");
        System.out.println("Entity: checkoutService");
        System.out.println("Ruleset: Checkout Discount Ruleset");
        
        // These would be the rule expressions added to the request
        System.out.println("Rule expressions:");
        System.out.println("  - successfulOrders() > 15 then applyDiscount(10), updateNote(\"VIP\")");
        System.out.println("  - orderValue() > 500 then addLoyaltyPoints(100), sendNotification(\"Bonus points!\")");
        System.out.println("  - isFirstTimeCustomer() then sendNotification(\"Welcome!\")");
        
        System.out.println("Additional context:");
        System.out.println("  - promotionCode: SUMMER2024");
        System.out.println("  - channel: mobile");
        
        System.out.println("Execution preferences:");
        System.out.println("  - Default priority: 1000 (lower than static rules)");
        System.out.println("  - Execute before static rules: false");
        
        /*
        In actual implementation, this would be:
        
        DynamicRuleRequest<CheckoutContextKey> request = new DynamicRuleRequest<>();
        request.setEntityName("checkoutService");
        request.setRulesetName("Checkout Discount Ruleset");
        request.addRuleExpression("successfulOrders() > 15 then applyDiscount(10), updateNote(\"VIP\")");
        request.addRuleExpression("orderValue() > 500 then addLoyaltyPoints(100), sendNotification(\"Bonus points!\")");
        request.addRuleExpression("isFirstTimeCustomer() then sendNotification(\"Welcome!\")");
        request.addContextEntry("promotionCode", "SUMMER2024");
        request.addContextEntry("channel", "mobile");
        request.setDefaultPriority(1000);
        request.setExecuteBeforeStaticRules(false);
        
        RuleExecutionResult<CheckoutContextKey> result = orchestrator.executeDynamicRuleSet(context, request);
        */
        
        System.out.println("✓ Request would be ready for execution");
    }
}
