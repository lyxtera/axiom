package com.lyxtera.axiom.examples.config;

import com.google.inject.AbstractModule;
import com.lyxtera.axiom.config.AxiomModule;
import com.lyxtera.axiom.engine.RuleSetLoader.YamlRuleSetLoader;
import com.lyxtera.axiom.examples.rules.CustomerContextKey;
import com.lyxtera.axiom.examples.rules.actions.ApplyDiscountAction;
import com.lyxtera.axiom.examples.rules.actions.UpgradeToVIPAction;
import com.lyxtera.axiom.examples.rules.checks.HasDaysSinceRegistrationCheck;
import com.lyxtera.axiom.examples.rules.checks.HasLoyaltyStatusCheck;
import com.lyxtera.axiom.examples.rules.checks.IsHighValueCustomerCheck;
import com.lyxtera.axiom.examples.rules.checks.TotalSpendCheck;

/**
 * Main application module that demonstrates how to configure Axiom rules.
 */
public class ApplicationMainModule extends AbstractModule {

    @Override
    protected void configure() {

        // Install the configured Axiom module
        install(AxiomModule.buildForKey(CustomerContextKey.class)
            .withRuleLoaders(loaders -> loaders
                .loader("customer_discount", new YamlRuleSetLoader<>("customer_discount_ruleset.yaml"))
            )
            .withChecks(checks -> checks
                .check("isHighValueCustomer", IsHighValueCustomerCheck.class)
                .check("hasLoyaltyStatus", HasLoyaltyStatusCheck.class)
                .check("hasDaysSinceRegistration", HasDaysSinceRegistrationCheck.class)
                .check("totalSpend", TotalSpendCheck.class))
            .withActions(actions -> actions
                .action("applyDiscount", ApplyDiscountAction.class)
                .action("upgradeToVIP", UpgradeToVIPAction.class))
            .build()
        );

        // Configure other application bindings here
        //...
    }
} 