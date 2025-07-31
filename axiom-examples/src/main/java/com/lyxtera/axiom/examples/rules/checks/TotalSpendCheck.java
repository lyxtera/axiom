package com.lyxtera.axiom.examples.rules.checks;

import java.math.BigDecimal;

import com.lyxtera.axiom.api.model.BusinessCheck;
import com.lyxtera.axiom.api.model.Value;
import com.lyxtera.axiom.config.RuleMetadata;
import com.lyxtera.axiom.engine.RuleContext;
import com.lyxtera.axiom.examples.rules.CustomerContextKey;

/**
 * Checks the total spend of the customer
 */
@RuleMetadata(
    name = "totalSpend",
    description = "Checks the total spend of the customer"
)
public class TotalSpendCheck implements BusinessCheck<CustomerContextKey> {

    /**
     * Execute the business check.
     * 
     * @param ctx The rule execution context
     * @return the customer's total spending amount
     */
    public Value execute(RuleContext<CustomerContextKey> ctx) {
        // Retrieve the total spend from the context
        BigDecimal spendingAmount = ctx.getRequired(CustomerContextKey.SPENDING_AMOUNT, BigDecimal.class);
        return Value.of(spendingAmount);
    }
} 