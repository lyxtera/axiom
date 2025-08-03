# Business Components Implementation

This guide provides detailed instructions for implementing Business Checks and Business Actions in Axiom, covering both basic and advanced implementation patterns.

## Core Implementation Requirements

Both Business Checks and Business Actions implement the `RuleFunction<K>` interface, which defines a single method:

```java
public interface RuleFunction<K extends Enum<K>> {
    Value execute(RuleContext<K> context, Value... args);
}
```

They are distinguished by implementing their respective marker interfaces:

```java
public interface BusinessCheck<K extends Enum<K>> extends RuleFunction<K> {
    // No additional methods - marker interface
}

public interface BusinessAction<K extends Enum<K>> extends RuleFunction<K> {
    // No additional methods - marker interface
}
```

## Basic Implementation Pattern

### Business Check Implementation

Here's a basic implementation of a Business Check:

```java
@RuleMetadata(
    name = "isHighValueOrder",
    description = "Checks if the order value exceeds a specified threshold"
)
public class HighValueOrderCheck implements BusinessCheck<OrderContextKey> {
    
    @Override
    public Value execute(RuleContext<OrderContextKey> context, @Arg("threshold") Value threshold) {
        // Get the order amount from the context
        Double orderAmount = context.getRequired(OrderContextKey.ORDER_AMOUNT, Double.class);
        
        // Get the threshold value from the argument
        Double thresholdValue = threshold.asNumber().doubleValue();
        
        // Return true if the order amount exceeds the threshold
        return Value.of(orderAmount >= thresholdValue);
    }
}
```

### Business Action Implementation

Here's a basic implementation of a Business Action:

```java
@RuleMetadata(
    name = "applyDiscount",
    description = "Applies a percentage discount to the order amount"
)
public class ApplyDiscountAction implements BusinessAction<OrderContextKey> {
    
    @Override
    public Value execute(RuleContext<OrderContextKey> context, @Arg("percentage") Value percentage) {
        // Get the order amount from the context
        Double orderAmount = context.getRequired(OrderContextKey.ORDER_AMOUNT, Double.class);
        
        // Get the discount percentage from the argument
        Double discountPercentage = percentage.asNumber().doubleValue();
        
        // Calculate the discounted amount
        Double discountedAmount = orderAmount * (1 - (discountPercentage / 100));
        
        // Update the context with the discounted amount
        context.add(OrderContextKey.ORDER_AMOUNT, discountedAmount);
        context.add(OrderContextKey.DISCOUNT_APPLIED, true);
        context.add(OrderContextKey.DISCOUNT_PERCENTAGE, discountPercentage);
        
        // Return true to indicate success
        return Value.of(true);
    }
}
```

## The @RuleMetadata Annotation

The `@RuleMetadata` annotation is crucial for registering your business components with the Axiom framework. It provides metadata that is used for:

1. Identifying the component in rule expressions
2. Validating rule expressions
3. Documenting the component's purpose

Required fields:
- `name`: The name used to reference the component in rule expressions
- `description`: A human-readable description of the component's purpose

Example:
```java
@RuleMetadata(
    name = "hasRiskScore", 
    description = "Checks if the risk score exceeds a specified threshold"
)
```

## The @Arg Annotation

The `@Arg` annotation is used to name the parameters in the `execute` method. This is important for:

1. Documentation and code readability
2. Validation of rule expressions
3. Better error messages when arguments are missing or invalid

Example:
```java
public Value execute(RuleContext<OrderContextKey> context, 
                     @Arg("threshold") Value threshold,
                     @Arg("tolerance") Value tolerance) {
    // Implementation
}
```

## Advanced Implementation Patterns

### Service-Dependent Components

For business components that need to interact with external services:

```java
@RuleMetadata(
    name = "hasGoodCreditScore",
    description = "Checks if the customer has a good credit score"
)
public class CreditScoreCheck implements BusinessCheck<CustomerContextKey> {
    
    private final CreditScoreService creditScoreService;
    
    @Inject
    public CreditScoreCheck(CreditScoreService creditScoreService) {
        this.creditScoreService = creditScoreService;
    }
    
    @Override
    public Value execute(RuleContext<CustomerContextKey> context, @Arg("minScore") Value minScore) {
        // Get the customer ID from the context
        String customerId = context.getRequired(CustomerContextKey.CUSTOMER_ID, String.class);
        
        // Get the minimum score from the argument
        int minimumScore = minScore.asNumber().intValue();
        
        // Call the credit score service
        int creditScore = creditScoreService.getCreditScore(customerId);
        
        // Add the credit score to the context for potential use by other components
        context.add(CustomerContextKey.CREDIT_SCORE, creditScore);
        
        // Return true if the credit score is at least the minimum
        return Value.of(creditScore >= minimumScore);
    }
}
```

### Composite Checks

For complex conditions that combine multiple simpler checks:

```java
@RuleMetadata(
    name = "isEligibleForPremiumOffer",
    description = "Checks if a customer is eligible for premium offers"
)
public class PremiumOfferEligibilityCheck implements BusinessCheck<CustomerContextKey> {
    
    private final BusinessCheck<CustomerContextKey> loyaltyCheck;
    private final BusinessCheck<CustomerContextKey> spendingCheck;
    private final BusinessCheck<CustomerContextKey> regionCheck;
    
    @Inject
    public PremiumOfferEligibilityCheck(
            @Named("isLoyalCustomer") BusinessCheck<CustomerContextKey> loyaltyCheck,
            @Named("hasHighSpending") BusinessCheck<CustomerContextKey> spendingCheck,
            @Named("isInTargetRegion") BusinessCheck<CustomerContextKey> regionCheck) {
        this.loyaltyCheck = loyaltyCheck;
        this.spendingCheck = spendingCheck;
        this.regionCheck = regionCheck;
    }
    
    @Override
    public Value execute(RuleContext<CustomerContextKey> context) {
        // Create values for the sub-checks
        Value loyaltyResult = loyaltyCheck.execute(context);
        Value spendingResult = spendingCheck.execute(context, Value.of(1000)); // Min spending $1000
        Value regionResult = regionCheck.execute(context);
        
        // Customer is eligible if they are loyal AND either have high spending OR are in target region
        boolean isEligible = loyaltyResult.asBoolean() && 
                (spendingResult.asBoolean() || regionResult.asBoolean());
        
        return Value.of(isEligible);
    }
}
```

### Stateful Actions

For actions that need to maintain state between executions:

```java
@RuleMetadata(
    name = "limitDiscountUsage",
    description = "Limits the number of times a discount can be used and applies it if allowed"
)
@Singleton // Important to ensure state is maintained
public class LimitedDiscountAction implements BusinessAction<OrderContextKey> {
    
    private final Map<String, Integer> discountUsageByCustomer = new ConcurrentHashMap<>();
    private final int maxUsagePerCustomer;
    
    @Inject
    public LimitedDiscountAction(@Named("maxDiscountUsage") int maxUsagePerCustomer) {
        this.maxUsagePerCustomer = maxUsagePerCustomer;
    }
    
    @Override
    public Value execute(RuleContext<OrderContextKey> context, @Arg("percentage") Value percentage) {
        // Get the customer ID
        String customerId = context.getRequired(OrderContextKey.CUSTOMER_ID, String.class);
        
        // Get current usage
        int currentUsage = discountUsageByCustomer.getOrDefault(customerId, 0);
        
        // Check if the customer has reached the limit
        if (currentUsage >= maxUsagePerCustomer) {
            context.add(OrderContextKey.DISCOUNT_DENIED_REASON, "Usage limit reached");
            return Value.of(false); // Discount not applied
        }
        
        // Increment usage
        discountUsageByCustomer.put(customerId, currentUsage + 1);
        
        // Get the order amount
        Double orderAmount = context.getRequired(OrderContextKey.ORDER_AMOUNT, Double.class);
        
        // Apply the discount
        Double discountPercentage = percentage.asNumber().doubleValue();
        Double discountedAmount = orderAmount * (1 - (discountPercentage / 100));
        
        // Update the context
        context.add(OrderContextKey.ORDER_AMOUNT, discountedAmount);
        context.add(OrderContextKey.DISCOUNT_APPLIED, true);
        context.add(OrderContextKey.DISCOUNT_PERCENTAGE, discountPercentage);
        context.add(OrderContextKey.DISCOUNT_USAGE_COUNT, currentUsage + 1);
        
        return Value.of(true); // Discount applied
    }
}
```

### Configurable Components

For components that need configuration beyond constructor injection:

```java
@RuleMetadata(
    name = "isInPromotionPeriod",
    description = "Checks if the current date is within a configurable promotion period"
)
public class PromotionPeriodCheck implements BusinessCheck<OrderContextKey> {
    
    private final ZonedDateTime startDate;
    private final ZonedDateTime endDate;
    private final String promotionCode;
    
    @Inject
    public PromotionPeriodCheck(
            @Named("promotionConfig") PromotionConfiguration config) {
        this.startDate = config.getStartDate();
        this.endDate = config.getEndDate();
        this.promotionCode = config.getPromotionCode();
    }
    
    @Override
    public Value execute(RuleContext<OrderContextKey> context) {
        // Get the current date
        ZonedDateTime now = ZonedDateTime.now();
        
        // Check if the current date is within the promotion period
        boolean isInPeriod = !now.isBefore(startDate) && !now.isAfter(endDate);
        
        // If in period, add the promotion code to the context
        if (isInPeriod) {
            context.add(OrderContextKey.ACTIVE_PROMOTION_CODE, promotionCode);
        }
        
        return Value.of(isInPeriod);
    }
    
    public static class PromotionConfiguration {
        private final ZonedDateTime startDate;
        private final ZonedDateTime endDate;
        private final String promotionCode;
        
        public PromotionConfiguration(
                ZonedDateTime startDate, 
                ZonedDateTime endDate, 
                String promotionCode) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.promotionCode = promotionCode;
        }
        
        public ZonedDateTime getStartDate() { return startDate; }
        public ZonedDateTime getEndDate() { return endDate; }
        public String getPromotionCode() { return promotionCode; }
    }
}
```

## Handling Multiple Arguments

For business components that need multiple parameters:

```java
@RuleMetadata(
    name = "isWithinRange",
    description = "Checks if a value is within a specified range"
)
public class RangeCheck implements BusinessCheck<OrderContextKey> {
    
    @Override
    public Value execute(RuleContext<OrderContextKey> context, 
                         @Arg("target") Value target,
                         @Arg("min") Value min,
                         @Arg("max") Value max) {
        // Get the values from the arguments
        String targetKey = target.asString();
        Double minValue = min.asNumber().doubleValue();
        Double maxValue = max.asNumber().doubleValue();
        
        // Get the target value from the context
        Double targetValue = context.getRequired(OrderContextKey.valueOf(targetKey), Double.class);
        
        // Check if the value is within range
        boolean isWithinRange = targetValue >= minValue && targetValue <= maxValue;
        
        return Value.of(isWithinRange);
    }
}

// Usage in YAML:
// expression: isWithinRange("ORDER_AMOUNT", 100, 1000) then applyStandardProcessing()
```

## Error Handling

Proper error handling is crucial for robust business components:

```java
@RuleMetadata(
    name = "validateCustomerData",
    description = "Validates customer data and flags issues"
)
public class CustomerDataValidationCheck implements BusinessCheck<CustomerContextKey> {
    
    @Override
    public Value execute(RuleContext<CustomerContextKey> context) {
        try {
            // Get required data
            String customerId = context.getRequired(CustomerContextKey.CUSTOMER_ID, String.class);
            String email = context.getRequired(CustomerContextKey.EMAIL, String.class);
            
            // Validate email format
            if (!isValidEmail(email)) {
                context.add(CustomerContextKey.VALIDATION_ERRORS, List.of("Invalid email format"));
                return Value.of(false);
            }
            
            // More validations...
            
            return Value.of(true); // All validations passed
        } catch (ContextException e) {
            // Handle missing required data
            String missingField = extractMissingFieldName(e.getMessage());
            context.add(CustomerContextKey.VALIDATION_ERRORS, 
                        List.of("Missing required field: " + missingField));
            return Value.of(false);
        } catch (Exception e) {
            // Handle unexpected errors
            context.add(CustomerContextKey.VALIDATION_ERRORS, 
                        List.of("Unexpected error: " + e.getMessage()));
            return Value.of(false);
        }
    }
    
    private boolean isValidEmail(String email) {
        // Email validation logic
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
    
    private String extractMissingFieldName(String message) {
        // Extract field name from error message
        // Simplified example
        return message.contains("No value for key") 
               ? message.substring(message.lastIndexOf('.') + 1) 
               : "unknown";
    }
}
```

## Logging and Monitoring

Adding logging and monitoring to business components:

```java
@RuleMetadata(
    name = "applyRiskBasedFee",
    description = "Applies a fee based on the risk level"
)
public class RiskBasedFeeAction implements BusinessAction<OrderContextKey> {
    
    private final Logger logger = LoggerFactory.getLogger(RiskBasedFeeAction.class);
    private final MetricsRegistry metrics;
    
    @Inject
    public RiskBasedFeeAction(MetricsRegistry metrics) {
        this.metrics = metrics;
    }
    
    @Override
    public Value execute(RuleContext<OrderContextKey> context) {
        String orderId = context.getRequired(OrderContextKey.ORDER_ID, String.class);
        Integer riskScore = context.getRequired(OrderContextKey.RISK_SCORE, Integer.class);
        Double orderAmount = context.getRequired(OrderContextKey.ORDER_AMOUNT, Double.class);
        
        logger.debug("Calculating risk-based fee for order {} with risk score {}", 
                    orderId, riskScore);
        
        // Measure execution time
        long startTime = System.nanoTime();
        
        try {
            // Calculate fee percentage based on risk score
            double feePercentage = calculateFeePercentage(riskScore);
            
            // Track the fee percentage applied
            metrics.recordValue("risk.fee.percentage", feePercentage);
            
            // Calculate fee amount
            double feeAmount = orderAmount * (feePercentage / 100);
            
            // Update context
            context.add(OrderContextKey.FEE_PERCENTAGE, feePercentage);
            context.add(OrderContextKey.FEE_AMOUNT, feeAmount);
            context.add(OrderContextKey.TOTAL_WITH_FEES, orderAmount + feeAmount);
            
            logger.info("Applied {}% risk-based fee (${}) to order {}", 
                       feePercentage, feeAmount, orderId);
            
            return Value.of(true);
        } catch (Exception e) {
            logger.error("Error applying risk-based fee to order {}: {}", 
                        orderId, e.getMessage(), e);
            metrics.incrementCounter("risk.fee.errors");
            return Value.of(false);
        } finally {
            long duration = System.nanoTime() - startTime;
            metrics.recordTiming("risk.fee.calculation.time", duration / 1_000_000); // Convert to ms
        }
    }
    
    private double calculateFeePercentage(int riskScore) {
        // Example logic:
        if (riskScore < 20) return 0.0;      // Low risk - no fee
        if (riskScore < 50) return 1.0;      // Medium risk - 1% fee
        if (riskScore < 80) return 2.5;      // High risk - 2.5% fee
        return 5.0;                          // Very high risk - 5% fee
    }
}
```

## Testing Considerations

When implementing business components, consider their testability:

1. **Dependency Injection**: Use constructor injection for dependencies to enable easy mocking in tests.

2. **Pure Functions**: When possible, make your components pure functions that don't have side effects beyond the context.

3. **Clear Responsibilities**: Keep components focused on a single responsibility for easier testing.

4. **Testable Arguments**: Design your argument structure to be easily testable with various inputs.

For more detailed testing approaches, see the [Rule Testing](rule-testing.md) guide.

## Registration in AxiomModule

After implementing your business components, register them in your `AxiomModule`:

```java
public class OrderProcessingModule extends AxiomModule<OrderContextKey> {
    
    public OrderProcessingModule() {
        super(OrderContextKey.class);
    }
    
    @Override
    protected void configureBusinessRules(
            MapBinder<String, BusinessCheck<OrderContextKey>> checks,
            MapBinder<String, BusinessAction<OrderContextKey>> actions) {
        // Register business checks
        checks.addBinding("isHighValueOrder").to(HighValueOrderCheck.class);
        checks.addBinding("isRepeatCustomer").to(RepeatCustomerCheck.class);
        checks.addBinding("isWithinRange").to(RangeCheck.class);
        checks.addBinding("isInPromotionPeriod").to(PromotionPeriodCheck.class);
        
        // Register business actions
        actions.addBinding("applyDiscount").to(ApplyDiscountAction.class);
        actions.addBinding("limitDiscountUsage").to(LimitedDiscountAction.class);
        actions.addBinding("applyRiskBasedFee").to(RiskBasedFeeAction.class);
    }
    
    // Configure necessary bindings for components
    @Provides
    @Singleton
    @Named("promotionConfig")
    PromotionPeriodCheck.PromotionConfiguration providePromotionConfig() {
        return new PromotionPeriodCheck.PromotionConfiguration(
            ZonedDateTime.parse("2023-11-24T00:00:00Z"), // Black Friday
            ZonedDateTime.parse("2023-12-31T23:59:59Z"), // New Year's Eve
            "HOLIDAY2023"
        );
    }
    
    @Provides
    @Named("maxDiscountUsage")
    int provideMaxDiscountUsage() {
        return 3; // Each customer can use the discount up to 3 times
    }
}
```

## Best Practices

1. **Single Responsibility**: Each business component should focus on a single responsibility.

2. **Clear Naming**: Choose clear, descriptive names for your components that indicate their purpose.

3. **Comprehensive Documentation**: Provide detailed descriptions in `@RuleMetadata` for self-documenting code.

4. **Argument Validation**: Validate all arguments to ensure they are of the expected type and value range.

5. **Context Safety**: Be careful when modifying the context; avoid removing or overwriting values unexpectedly.

6. **Error Handling**: Implement robust error handling to prevent rule execution failures.

7. **Thread Safety**: Ensure your components are thread-safe, especially if they maintain state.

8. **Performance Consciousness**: Be mindful of performance, especially for components that interact with external services.

9. **Consistent Return Values**: Always return `Value` objects consistently; use `Value.of(true)` to indicate success.

10. **Test Coverage**: Write comprehensive tests for your business components to ensure they behave as expected.

## Related Sections

- [Business Components Overview](business-components-overview.md) - Core concepts of business components
- [Business Components Definition](business-components-definition.md) - How to define components in rule sets
- [Business Components Usage](business-components-usage.md) - How to use components in rules
- [Business Components Validation](business-components-validation.md) - How to validate components

[← Back to Previous Section](ruleset-structure.md) 
