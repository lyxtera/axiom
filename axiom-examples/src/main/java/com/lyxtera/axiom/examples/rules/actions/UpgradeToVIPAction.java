package com.lyxtera.axiom.examples.rules.actions;

import com.lyxtera.axiom.config.Arg;
import com.lyxtera.axiom.config.RuleMetadata;
import com.lyxtera.axiom.api.model.BusinessAction;
import com.lyxtera.axiom.api.model.Value;
import com.lyxtera.axiom.engine.RuleContext;
import com.lyxtera.axiom.examples.rules.CustomerContextKey;

/**
 * Upgrades the customer to VIP status
 */
@RuleMetadata(
    name = "upgradeToVIP",
    description = "Upgrades the customer to VIP status"
)
public class UpgradeToVIPAction implements BusinessAction<CustomerContextKey> {

    /**
     * Execute the business action.
     * 
     * @param ctx The rule execution context
     * @return true indicating the customer was upgraded to VIP status
     */
    public Value execute(RuleContext<CustomerContextKey> ctx) {
        ctx.add(CustomerContextKey.IS_VIP, true);
        return Value.of(true);
    }
}