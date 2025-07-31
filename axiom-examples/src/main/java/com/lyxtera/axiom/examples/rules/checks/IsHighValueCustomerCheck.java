package com.lyxtera.axiom.examples.rules.checks;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import com.lyxtera.axiom.config.Arg;
import com.lyxtera.axiom.config.RuleMetadata;
import com.lyxtera.axiom.api.model.BusinessCheck;
import com.lyxtera.axiom.api.model.Value;
import com.lyxtera.axiom.engine.RuleContext;
import com.lyxtera.axiom.examples.rules.CustomerContextKey;

/**
 * Determines if the customer has high spending for the past N days
 */
@RuleMetadata(
    name = "isHighValueCustomer",
    description = "Determines if the customer has high spending for the past N days"
)
public class IsHighValueCustomerCheck implements BusinessCheck<CustomerContextKey> {

    /**
     * Execute the business check.
     * 
     * @param ctx The rule execution context
     * @param spendingThreshold The spending threshold
     * @param days The days
     * @return true if the customer's spending exceeds the threshold within the specified days, false otherwise
     */
    public Value execute(RuleContext<CustomerContextKey> ctx, @Arg("spendingThreshold") Value spendingThreshold, @Arg("days") Value days) {
        BigDecimal customerSpending = ctx.getRequired(CustomerContextKey.SPENDING_AMOUNT, BigDecimal.class);
        double threshold = spendingThreshold.asNumber().doubleValue();
        int daysPeriod = days.asNumber().intValue();
        
        // Determine if the customer has spent more than the threshold
        boolean isHighValue = customerSpending.doubleValue() >= threshold;
        
        // Additionally check if the spending occurred within the specified time period
        if (isHighValue) {
            LocalDateTime registrationDate = ctx.getRequired(CustomerContextKey.REGISTRATION_DATE, LocalDateTime.class);
            LocalDateTime now = LocalDateTime.now();
            long daysSinceRegistration = ChronoUnit.DAYS.between(registrationDate, now);
            
            // Only consider as high value if the spending occurred within the specified days
            isHighValue = daysSinceRegistration <= daysPeriod;
        }
        
        return Value.of(isHighValue);
    }
} 