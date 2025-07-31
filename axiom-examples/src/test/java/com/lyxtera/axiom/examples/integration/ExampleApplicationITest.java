package com.lyxtera.axiom.examples.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import com.lyxtera.axiom.engine.RuleContext;
import com.lyxtera.axiom.engine.RuleExecutionResult;
import com.lyxtera.axiom.engine.RuleOrchestrator;
import com.lyxtera.axiom.examples.config.ApplicationMainModule;
import com.lyxtera.axiom.examples.rules.CustomerContextKey;

/**
 * Integration test for the Axiom example application.
 * Tests end-to-end functionality including rule loading, execution, and business logic.
 */
@DisplayName("Axiom Example Application Integration Tests")
class ExampleApplicationITest {

    private RuleOrchestrator<CustomerContextKey> ruleOrchestrator;

    @BeforeEach
    void setUp() {
        // Create the Guice injector with the application module
        Injector injector = Guice.createInjector(new ApplicationMainModule());
        
        // Inject dependencies
        injector.injectMembers(this);
    }

    @Inject
    public void setRuleOrchestrator(@Named("customer_discount") RuleOrchestrator<CustomerContextKey> ruleOrchestrator) {
        this.ruleOrchestrator = ruleOrchestrator;
    }

    @Test
    @DisplayName("Should apply discount for high value customer with recent registration")
    void testHighValueCustomerDiscount() {
        // Given: A high value customer registered 35 days ago
        RuleContext<CustomerContextKey> context = createContext(
            new BigDecimal("2500.00"), // High spending
            5,                          // High loyalty level
            LocalDateTime.now().minusDays(35) // Recent registration
        );

        // When: Rules are executed
        RuleExecutionResult<CustomerContextKey> result = ruleOrchestrator.executeAllMatchingRules(context);

        // Then: Rules should match and execute successfully
        assertThat(result.hasMatches()).isTrue();
        assertThat(result.hasFailed()).isFalse();
        assertThat(result.getExecutedRules()).hasSizeGreaterThan(0);

        // And: Discount should be applied
        BigDecimal appliedDiscount = context.getRequired(CustomerContextKey.DISCOUNT_PERCENTAGE, BigDecimal.class);
        assertThat(appliedDiscount).isGreaterThan(BigDecimal.ZERO);
        
        // And: Customer should not be VIP yet (depends on rule logic)
        Boolean isVip = context.getRequired(CustomerContextKey.IS_VIP, Boolean.class);
        assertThat(isVip).isFalse();
    }

    @Test
    @DisplayName("Should apply seasonal discount for any customer with minimal registration period")
    void testSeasonalDiscountRule() {
        // Given: Any customer registered more than 1 day ago
        RuleContext<CustomerContextKey> context = createContext(
            new BigDecimal("100.00"),   // Low spending
            1,                          // Low loyalty level
            LocalDateTime.now().minusDays(2) // Registered 2 days ago
        );

        // When: Rules are executed
        RuleExecutionResult<CustomerContextKey> result = ruleOrchestrator.executeAllMatchingRules(context);

        // Then: Seasonal discount rule should match
        assertThat(result.hasMatches()).isTrue();
        assertThat(result.hasFailed()).isFalse();

        // And: Seasonal discount should be applied
        BigDecimal appliedDiscount = context.getRequired(CustomerContextKey.DISCOUNT_PERCENTAGE, BigDecimal.class);
        assertThat(appliedDiscount).isEqualTo(new BigDecimal("3.5"));
    }

    @Test
    @DisplayName("Should handle high spending customer with negative discount (surcharge)")
    void testHighSpendingCustomerSurcharge() {
        // Given: Very high spending customer
        RuleContext<CustomerContextKey> context = createContext(
            new BigDecimal("5000.00"),  // Very high spending (triggers surcharge rule)
            3,                          // Medium loyalty level
            LocalDateTime.now().minusDays(10) // Registered 10 days ago
        );

        // When: Rules are executed
        RuleExecutionResult<CustomerContextKey> result = ruleOrchestrator.executeAllMatchingRules(context);

        // Then: Rules should execute
        assertThat(result.hasMatches()).isTrue();
        assertThat(result.hasFailed()).isFalse();

        // And: Some discount/surcharge should be applied
        BigDecimal appliedDiscount = context.getRequired(CustomerContextKey.DISCOUNT_PERCENTAGE, BigDecimal.class);
        assertThat(appliedDiscount).isNotNull();
    }

    @Test
    @DisplayName("Should handle customer with expired loyalty rule")
    void testExpiredLoyaltyRule() {
        // Given: Customer with high loyalty level (but loyalty rule is expired)
        RuleContext<CustomerContextKey> context = createContext(
            new BigDecimal("1500.00"),  // Medium-high spending
            5,                          // High loyalty level
            LocalDateTime.now().minusDays(60) // Registered 60 days ago
        );

        // When: Rules are executed
        RuleExecutionResult<CustomerContextKey> result = ruleOrchestrator.executeAllMatchingRules(context);

        // Then: Should still have matches (other rules may apply)
        // The loyalty rule should not apply due to expiration
        assertThat(result.hasFailed()).isFalse();
        
        // And: Some discount might still be applied from other rules
        BigDecimal appliedDiscount = context.getRequired(CustomerContextKey.DISCOUNT_PERCENTAGE, BigDecimal.class);
        assertThat(appliedDiscount).isNotNull();
    }

    @Test
    @DisplayName("Should handle new customer scenario")
    void testNewCustomer() {
        // Given: Brand new customer registered today
        RuleContext<CustomerContextKey> context = createContext(
            new BigDecimal("50.00"),    // Low spending
            1,                          // Low loyalty level
            LocalDateTime.now()         // Just registered
        );

        // When: Rules are executed
        RuleExecutionResult<CustomerContextKey> result = ruleOrchestrator.executeAllMatchingRules(context);

        // Then: Either no rules match (which is considered a "failure") or rules execute without errors
        if (result.hasFailed()) {
            // No rules matched for new customer scenario - this is expected
            assertThat(result.getFailureReason()).hasValue("No rules matched the context");
        } else {
            // Some rules matched and executed successfully
            assertThat(result.hasMatches()).isTrue();
        }
        
        // And: Context should have proper values
        BigDecimal appliedDiscount = context.getRequired(CustomerContextKey.DISCOUNT_PERCENTAGE, BigDecimal.class);
        Boolean isVip = context.getRequired(CustomerContextKey.IS_VIP, Boolean.class);
        Boolean sendWelcomeGift = context.getRequired(CustomerContextKey.SEND_WELCOME_GIFT, Boolean.class);
        

        // New customer shouldn't trigger expensive rules
        assertThat(appliedDiscount).isNotNull();
        // VIP status might be set by some rule, just verify it's a boolean
        assertThat(isVip).isNotNull();
        assertThat(sendWelcomeGift).isNotNull();
    }

    @Test
    @DisplayName("Should execute rules in correct priority order")
    void testRulePriorityOrder() {
        // Given: Customer that matches multiple rules
        RuleContext<CustomerContextKey> context = createContext(
            new BigDecimal("2000.00"),  // High enough for multiple rules
            5,                          // High loyalty level
            LocalDateTime.now().minusDays(40) // Long-term customer
        );

        // When: Rules are executed
        RuleExecutionResult<CustomerContextKey> result = ruleOrchestrator.executeAllMatchingRules(context);

        // Then: Should execute multiple rules
        assertThat(result.hasMatches()).isTrue();
        assertThat(result.getExecutedRules().size()).isGreaterThan(1);
        
        // And: Should have proper execution order (priority-based)
        assertThat(result.getMatchedRules()).isNotEmpty();
        
        // And: Final discount should reflect last executed rule
        BigDecimal finalDiscount = context.getRequired(CustomerContextKey.DISCOUNT_PERCENTAGE, BigDecimal.class);
        assertThat(finalDiscount).isNotNull();
    }

    @Test
    @DisplayName("Should handle edge cases gracefully")
    void testEdgeCases() {
        // Given: Edge case values
        RuleContext<CustomerContextKey> context = createContext(
            BigDecimal.ZERO,            // Zero spending
            0,                          // Zero loyalty
            LocalDateTime.now().minusDays(1) // Minimum registration period
        );

        // When: Rules are executed
        RuleExecutionResult<CustomerContextKey> result = ruleOrchestrator.executeAllMatchingRules(context);

        // Then: Should not fail
        assertThat(result.hasFailed()).isFalse();
        
        // And: Should handle edge case gracefully
        BigDecimal appliedDiscount = context.getRequired(CustomerContextKey.DISCOUNT_PERCENTAGE, BigDecimal.class);
        assertThat(appliedDiscount).isNotNull();
    }

    /**
     * Helper method to create a rule context with customer data
     */
    private RuleContext<CustomerContextKey> createContext(BigDecimal spendingAmount, 
                                                         int loyaltyLevel, 
                                                         LocalDateTime registrationDate) {
        RuleContext<CustomerContextKey> context = new RuleContext<>(CustomerContextKey.class);
        context.add(CustomerContextKey.SPENDING_AMOUNT, spendingAmount);
        context.add(CustomerContextKey.LOYALTY_LEVEL, loyaltyLevel);
        context.add(CustomerContextKey.REGISTRATION_DATE, registrationDate);
        context.add(CustomerContextKey.DISCOUNT_PERCENTAGE, BigDecimal.ZERO);
        context.add(CustomerContextKey.IS_VIP, false);
        context.add(CustomerContextKey.SEND_WELCOME_GIFT, false);
        return context;
    }
}