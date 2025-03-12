# Business Components Definition

Business components are the building blocks of Axiom rules. This document explains how to define business checks and actions, including their metadata, parameters, and implementation patterns.

## Business Checks vs. Business Actions

Before diving into definitions, it's important to understand the distinction between checks and actions:

- **Business Checks**: Evaluate conditions and return boolean values (`true`/`false`). They represent the "if" part of a rule.
- **Business Actions**: Perform operations and can return any value. They represent the "then" part of a rule.

## Defining Business Checks

A business check is implemented as a Java class that implements the `BusinessCheck` interface:

```java
@RuleMetadata(
    name = "isHighValueCustomer",
    description = "Checks if a customer is considered high value based on criteria"
)
public class HighValueCustomerCheck implements BusinessCheck<CustomerContextKey> {
    
    @Override
    public Value execute(RuleContext<CustomerContextKey> context, @Arg("spendThreshold") Value spendThreshold) {
        // Get required data from context
        Double totalSpend = context.getRequired(CustomerContextKey.LIFETIME_SPEND, Double.class);
        Boolean isPremium = context.getOptional(CustomerContextKey.IS_PREMIUM_MEMBER, Boolean.class)
                                  .orElse(false);
        
        // Apply business logic
        Double threshold = spendThreshold.asDouble();
        boolean isHighValue = totalSpend > threshold || isPremium;
        
        // Return result as a Value
        return Value.of(isHighValue);
    }
}
```

### Key Components:

1. **@RuleMetadata Annotation**: Provides metadata about the check:
   - `name`: The identifier used in rule expressions (must match the name in YAML)
   - `description`: A description of what the check does

2. **Interface Implementation**: The class must implement `BusinessCheck<T>` where `T` is your context key enum.

3. **execute Method**: The core method that implements the check logic:
   - `context`: Contains all data needed for the check
   - `@Arg` parameters: Values provided when the check is called in a rule expression
   - Return value: A `Value` object, typically containing a boolean

## Defining Business Actions

A business action is implemented as a Java class that implements the `BusinessAction` interface:

```java
@RuleMetadata(
    name = "applyDiscount",
    description = "Applies a percentage discount to the order"
)
public class ApplyDiscountAction implements BusinessAction<OrderContextKey> {
    
    @Override
    public Value execute(RuleContext<OrderContextKey> context, 
                          @Arg("percent") Value percent,
                          @Arg("reason") Value reason) {
        // Get required data from context
        Double orderAmount = context.getRequired(OrderContextKey.ORDER_AMOUNT, Double.class);
        
        // Apply business logic
        Double discountPercent = percent.asDouble();
        String discountReason = reason.asString();
        
        Double discountedAmount = orderAmount * (1 - (discountPercent / 100));
        
        // Update context with new values
        context.add(OrderContextKey.ORDER_AMOUNT, discountedAmount);
        context.add(OrderContextKey.DISCOUNT_APPLIED, true);
        context.add(OrderContextKey.DISCOUNT_REASON, discountReason);
        
        // Return success indicator
        return Value.of(true);
    }
}
```

### Key Components:

1. **@RuleMetadata Annotation**: Similar to checks, provides metadata:
   - `name`: The identifier used in rule expressions
   - `description`: A description of what the action does

2. **Interface Implementation**: The class must implement `BusinessAction<T>`.

3. **execute Method**: Implements the action logic:
   - Can have multiple `@Arg` parameters
   - Typically modifies the context
   - Returns a `Value` that can be of any type (often a success indicator)

## Working with Parameters

Parameters allow rules to customize the behavior of checks and actions. Here's how to work with them:

### Parameter Annotations

Use the `@Arg` annotation to define parameters:

```java
public Value execute(RuleContext<T> context, 
                     @Arg("name") Value nameParam,
                     @Arg("threshold") Value thresholdParam) {
    // ...
}
```

### Parameter Types

All parameters are passed as `Value` objects, which can be converted to specific types:

```java
String name = nameParam.asString();
Double threshold = thresholdParam.asDouble();
Boolean flag = thresholdParam.asBoolean();
List<String> items = thresholdParam.asList(String.class);
Map<String, Object> data = thresholdParam.asMap();
```

### Optional Parameters

Sometimes you might want to make parameters optional with defaults:

```java
public Value execute(RuleContext<T> context, @Arg("threshold") Value thresholdParam) {
    // Default to 100 if not provided or not a valid number
    Double threshold = 100.0;
    try {
        threshold = thresholdParam.asDouble();
    } catch (ValueConversionException e) {
        // Log warning and use default
        logger.warn("Invalid threshold value, using default");
    }
    
    // ...
}
```

## Parameter Validation

It's important to validate parameters to ensure they meet your requirements:

```java
public Value execute(RuleContext<T> context, @Arg("age") Value ageParam) {
    // Validate age is a positive number
    if (!ageParam.isNumber()) {
        throw new IllegalArgumentException("Age must be a number");
    }
    
    Double age = ageParam.asDouble();
    if (age < 0) {
        throw new IllegalArgumentException("Age cannot be negative");
    }
    
    // ...
}
```

## Context Interaction

Business components interact with the rule context to get data and store results:

### Reading from Context

```java
// Required value (throws exception if missing or wrong type)
Customer customer = context.getRequired(CustomerContextKey.CUSTOMER, Customer.class);

// Optional value (returns Optional)
Optional<String> promoCode = context.getOptional(CustomerContextKey.PROMO_CODE, String.class);

// With default value
String region = context.getOptional(CustomerContextKey.REGION, String.class)
                      .orElse("DEFAULT");
```

### Writing to Context

```java
// Adding new values
context.add(OrderContextKey.DISCOUNT_APPLIED, true);
context.add(OrderContextKey.DISCOUNT_AMOUNT, 25.50);

// Updating existing values
context.add(OrderContextKey.ORDER_AMOUNT, newAmount); // Overwrites existing value
```

## Documentation Best Practices

Good documentation is crucial for business components:

1. **Descriptive Names**: Use clear, business-oriented names for your components.

2. **Thorough Descriptions**: The `description` field should explain what the component does in business terms.

3. **Javadoc Documentation**: Add detailed Javadoc to your classes explaining:
   - Purpose and behavior
   - Parameter details and valid values
   - Return value meaning
   - Context keys used and modified
   - Examples of usage

Example:

```java
/**
 * Checks if a customer qualifies for VIP status based on spend and loyalty.
 * <p>
 * A customer qualifies for VIP status if:
 * <ul>
 *   <li>Their annual spend exceeds the specified threshold, OR</li>
 *   <li>They have been a member for at least 5 years and have made a purchase in the last 6 months</li>
 * </ul>
 * <p>
 * Required context keys:
 * <ul>
 *   <li>{@link CustomerContextKey#ANNUAL_SPEND} - Double, the customer's annual spend</li>
 *   <li>{@link CustomerContextKey#MEMBERSHIP_YEARS} - Integer, years as a member</li>
 *   <li>{@link CustomerContextKey#LAST_PURCHASE_DATE} - LocalDate, date of last purchase</li>
 * </ul>
 * <p>
 * Parameters:
 * <ul>
 *   <li>spendThreshold (Double) - The annual spend threshold for VIP status</li>
 * </ul>
 *
 * @see VipBenefitsAction
 */
@RuleMetadata(
    name = "isVipCustomer",
    description = "Checks if a customer qualifies for VIP status"
)
public class VipCustomerCheck implements BusinessCheck<CustomerContextKey> {
    // Implementation...
}
```

## Best Practices for Component Definition

1. **Single Responsibility**: Each component should do one thing well.

2. **Immutability**: Make your components immutable for thread safety.

3. **Error Handling**: Validate inputs and handle errors gracefully.

4. **Testability**: Design components to be easily testable in isolation.

5. **Performance**: Be mindful of performance, especially for frequently used checks.

6. **Reusability**: Design components to be reusable across different rule sets.

7. **Consistency**: Follow a consistent naming convention for components.

8. **Dependency Injection**: Use DI for external dependencies rather than static references.

## Advanced Component Patterns

### Composite Checks

Sometimes you might want to create a check that combines multiple other checks:

```java
@RuleMetadata(
    name = "isEligibleForPromotion",
    description = "Checks if a customer is eligible for a promotion"
)
public class PromotionEligibilityCheck implements BusinessCheck<CustomerContextKey> {
    
    private final BusinessCheck<CustomerContextKey> ageCheck;
    private final BusinessCheck<CustomerContextKey> regionCheck;
    
    @Inject
    public PromotionEligibilityCheck(
            @Named("isAdult") BusinessCheck<CustomerContextKey> ageCheck,
            @Named("isInTargetRegion") BusinessCheck<CustomerContextKey> regionCheck) {
        this.ageCheck = ageCheck;
        this.regionCheck = regionCheck;
    }
    
    @Override
    public Value execute(RuleContext<CustomerContextKey> context, @Arg("promoId") Value promoId) {
        // First check age
        Value ageResult = ageCheck.execute(context, Value.of(18));
        if (!ageResult.asBoolean()) {
            return Value.of(false);
        }
        
        // Then check region
        Value regionResult = regionCheck.execute(context, Value.of("NA,EU"));
        if (!regionResult.asBoolean()) {
            return Value.of(false);
        }
        
        // If both pass, check promotion-specific logic
        String promotionId = promoId.asString();
        // ... additional promotion-specific logic
        
        return Value.of(true);
    }
}
```

### Parameterized Actions

For more complex actions that need configuration:

```java
@RuleMetadata(
    name = "sendNotification",
    description = "Sends a notification through the configured channel"
)
public class NotificationAction implements BusinessAction<UserContextKey> {
    
    private final NotificationService notificationService;
    private final TemplateEngine templateEngine;
    
    @Inject
    public NotificationAction(
            NotificationService notificationService,
            TemplateEngine templateEngine) {
        this.notificationService = notificationService;
        this.templateEngine = templateEngine;
    }
    
    @Override
    public Value execute(RuleContext<UserContextKey> context,
                          @Arg("channel") Value channel,
                          @Arg("templateId") Value templateId,
                          @Arg("priority") Value priority) {
        
        // Get user from context
        User user = context.getRequired(UserContextKey.USER, User.class);
        
        // Process parameters
        String channelType = channel.asString();
        String template = templateId.asString();
        String priorityLevel = priority.asString();
        
        // Prepare notification
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("userName", user.getName());
        templateData.put("userId", user.getId());
        // ... add more template data
        
        String content = templateEngine.render(template, templateData);
        
        // Send notification
        NotificationResult result = notificationService.send(
            user.getContactInfo(),
            channelType,
            content,
            NotificationPriority.valueOf(priorityLevel)
        );
        
        // Update context with notification result
        context.add(UserContextKey.NOTIFICATION_SENT, true);
        context.add(UserContextKey.NOTIFICATION_ID, result.getNotificationId());
        
        return Value.of(result.isSuccess());
    }
}
```

[← Back to Previous Section](ruleset-structure.md) 
