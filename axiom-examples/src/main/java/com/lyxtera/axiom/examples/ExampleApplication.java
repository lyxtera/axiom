package com.lyxtera.axiom.examples;

import static com.lyxtera.axiom.examples.rules.CustomerContextKey.DISCOUNT_PERCENTAGE;
import static com.lyxtera.axiom.examples.rules.CustomerContextKey.IS_VIP;
import static com.lyxtera.axiom.examples.rules.CustomerContextKey.LOYALTY_LEVEL;
import static com.lyxtera.axiom.examples.rules.CustomerContextKey.REGISTRATION_DATE;
import static com.lyxtera.axiom.examples.rules.CustomerContextKey.SEND_WELCOME_GIFT;
import static com.lyxtera.axiom.examples.rules.CustomerContextKey.SPENDING_AMOUNT;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import com.lyxtera.axiom.api.model.BusinessRule;
import com.lyxtera.axiom.engine.RuleContext;
import com.lyxtera.axiom.engine.RuleExecutionResult;
import com.lyxtera.axiom.engine.RuleOrchestrator;
import com.lyxtera.axiom.examples.config.ApplicationMainModule;
import com.lyxtera.axiom.examples.rules.CustomerContextKey;

/**
 * Example application demonstrating how to use RuleOrchestrator with dependency injection.
 */
public class ExampleApplication {

    private final RuleOrchestrator<CustomerContextKey> ruleOrchestrator;

    @Inject
    public ExampleApplication(@Named("customer_discount") RuleOrchestrator<CustomerContextKey> ruleOrchestrator) {
        this.ruleOrchestrator = ruleOrchestrator;
    }

    public void processCustomerDiscount(BigDecimal spendingAmount, int loyaltyLevel, LocalDateTime registrationDate) {
        // Create a new context with customer data
        RuleContext<CustomerContextKey> ctx = new RuleContext<>(CustomerContextKey.class);
        ctx.add(SPENDING_AMOUNT, spendingAmount);
        ctx.add(LOYALTY_LEVEL, loyaltyLevel);
        ctx.add(REGISTRATION_DATE, registrationDate);
        ctx.add(DISCOUNT_PERCENTAGE, BigDecimal.ZERO);
        ctx.add(IS_VIP, false);
        ctx.add(SEND_WELCOME_GIFT, false);

        // Execute all matching rules
        RuleExecutionResult<CustomerContextKey> result = ruleOrchestrator.executeAllMatchingRules(ctx);

        // Process the results
        if (result.hasFailed() && result.getFailureReason().isPresent()) {
            System.out.println("Error executing rules: " + result.getFailureReason().get());
        } else if (result.hasMatches()) {
            System.out.println("Rules executed successfully!");
            System.out.println("Number of matched rules: " + result.getMatchedRules().size());
            System.out.println("Number of executed rules: " + result.getExecutedRules().size());

            for (BusinessRule<CustomerContextKey> rule : result.getExecutedRules().keySet()) {
                System.out.println("Rule name: " + rule.getName());
            }
            System.out.println("First rule result: " + result.getFirstRuleResult());
            result.getFirstRuleResult().ifPresent(ruleResult -> {
                System.out.println("Rule result: " + ruleResult);
                System.out.println("Applied discount: " + ctx.getRequired(DISCOUNT_PERCENTAGE, BigDecimal.class) + "%");
                System.out.println("VIP status: " + ctx.getRequired(IS_VIP, Boolean.class));
                System.out.println("Send welcome gift: " + ctx.getRequired(SEND_WELCOME_GIFT, Boolean.class));
            });
        } else {
            System.out.println("No matching rules found for the given context");
        }
    }

    public static void main(String[] args) {
        // Create the Guice injector
        Injector injector = Guice.createInjector(new ApplicationMainModule());

        // Get an instance of our application
        ExampleApplication app = injector.getInstance(ExampleApplication.class);

        app.processCustomerDiscount(
            new BigDecimal("2500.00"),  // High spending amount
            5,                          // Loyalty level
            LocalDateTime.now().minusDays(35)  // Registration date (35 days ago)
        );
    }
} 