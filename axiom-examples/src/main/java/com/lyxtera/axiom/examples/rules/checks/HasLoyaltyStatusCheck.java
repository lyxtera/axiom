package com.lyxtera.axiom.examples.rules.checks;

import com.lyxtera.axiom.config.Arg;
import com.lyxtera.axiom.config.RuleMetadata;
import com.lyxtera.axiom.api.model.BusinessCheck;
import com.lyxtera.axiom.api.model.Value;
import com.lyxtera.axiom.engine.RuleContext;
import com.lyxtera.axiom.examples.rules.CustomerContextKey;

/**
 * Checks if the customer has loyalty status
 */
@RuleMetadata(
    name = "hasLoyaltyStatus",
    description = "Checks if the customer has loyalty status"
)
public class HasLoyaltyStatusCheck implements BusinessCheck<CustomerContextKey> {

    /**
     * Execute the business check.
     * 
     * @param ctx The rule execution context
     * @param loyaltyLevel The loyalty level to check
     * @return true if the customer has the specified loyalty level or higher, false otherwise
     */
    public Value execute(RuleContext<CustomerContextKey> ctx, @Arg("loyaltyLevel") Value loyaltyLevel) {
        int customerLoyaltyLevel = ctx.getRequired(CustomerContextKey.LOYALTY_LEVEL, Integer.class);
        int requiredLevel = loyaltyLevel.asNumber().intValue();
        
        // Check if the customer's loyalty level meets or exceeds the required level
        boolean hasRequiredLoyaltyStatus = customerLoyaltyLevel >= requiredLevel;
        
        return Value.of(hasRequiredLoyaltyStatus);
    }
} 