package com.lyxtera.axiom.examples.rules.actions;

import java.math.BigDecimal;

import com.lyxtera.axiom.config.Arg;
import com.lyxtera.axiom.config.RuleMetadata;
import com.lyxtera.axiom.api.model.BusinessAction;
import com.lyxtera.axiom.api.model.Value;
import com.lyxtera.axiom.engine.RuleContext;
import com.lyxtera.axiom.examples.rules.CustomerContextKey;

/**
 * Applies a discount to the customer's order
 */
@RuleMetadata(name = "applyDiscount", description = "Applies a discount to the customer's order")
public class ApplyDiscountAction implements BusinessAction<CustomerContextKey> {

    /**
     * Execute the business action.
     * 
     * @param ctx        The rule execution context
     * @param percentage The discount percentage to apply (can be negative for surcharges)
     * @return The applied discount percentage
     */
    public Value execute(RuleContext<CustomerContextKey> ctx, @Arg("percentage") Value percentage) {

        // Get the new discount percentage from the argument
        BigDecimal newDiscount = percentage.asNumber();

        ctx.add(CustomerContextKey.DISCOUNT_PERCENTAGE, newDiscount);

        // Return the discount that was applied
        return Value.of(newDiscount);
    }
}