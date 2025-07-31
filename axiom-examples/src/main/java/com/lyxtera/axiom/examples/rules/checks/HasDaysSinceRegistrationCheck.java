package com.lyxtera.axiom.examples.rules.checks;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import com.lyxtera.axiom.config.Arg;
import com.lyxtera.axiom.config.RuleMetadata;
import com.lyxtera.axiom.api.model.BusinessCheck;
import com.lyxtera.axiom.api.model.Value;
import com.lyxtera.axiom.engine.RuleContext;
import com.lyxtera.axiom.examples.rules.CustomerContextKey;

/**
 * Checks if the customer has been registered for a certain number of days
 */
@RuleMetadata(
    name = "hasDaysSinceRegistration",
    description = "Checks if the customer has been registered for a certain number of days"
)
public class HasDaysSinceRegistrationCheck implements BusinessCheck<CustomerContextKey> {

    /**
     * Execute the business check.
     * 
     * @param ctx The rule execution context
     * @param daysSinceRegistration The minimum number of days since registration
     * @return true if the customer has been registered for at least the specified number of days, false otherwise
     */
    public Value execute(RuleContext<CustomerContextKey> ctx, @Arg("daysSinceRegistration") Value daysSinceRegistration) {
        LocalDateTime registrationDate = ctx.getRequired(CustomerContextKey.REGISTRATION_DATE, LocalDateTime.class);
        long requiredDays = daysSinceRegistration.asNumber().longValue();
        
        // Calculate the number of days since registration
        LocalDateTime now = LocalDateTime.now();
        long actualDaysSinceRegistration = ChronoUnit.DAYS.between(registrationDate, now);
        
        // Check if the customer has been registered for at least the required number of days
        boolean hasSufficientRegistrationPeriod = actualDaysSinceRegistration >= requiredDays;
        
        return Value.of(hasSufficientRegistrationPeriod);
    }
} 