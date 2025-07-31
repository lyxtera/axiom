package com.lyxtera.axiom.examples.rules;

/**
 * Enum defining the context keys for customer rule evaluation.
 */
public enum CustomerContextKey {
    /**
     * The customer's total spending amount.
     */
    SPENDING_AMOUNT,
    
    /**
     * The customer's loyalty level.
     */
    LOYALTY_LEVEL,
    
    /**
     * The date when the customer was registered.
     */
    REGISTRATION_DATE,
    
    /**
     * The discount to be applied.
     */
    DISCOUNT_PERCENTAGE,
    
    /**
     * Flag indicating if the customer is a VIP.
     */
    IS_VIP,
    
    /**
     * Flag indicating if a welcome gift should be sent.
     */
    SEND_WELCOME_GIFT
} 