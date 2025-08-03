# Business Components Validation

Validation is a critical aspect of business component development in Axiom. This document covers validation patterns, error handling, and best practices to ensure your business checks and actions operate reliably.

## Importance of Validation

Proper validation in business components ensures:

1. **Robustness**: Components can handle unexpected inputs without crashing
2. **Correctness**: Business logic operates on valid data only
3. **Clear Feedback**: Users receive meaningful error messages when something goes wrong
4. **Security**: Input validation helps prevent security vulnerabilities
5. **Maintainability**: Validation acts as documentation of expected inputs

## Input Validation Patterns

### Parameter Validation

Always validate parameters passed to your business checks and actions:

```java
@Override
public Value execute(RuleContext<OrderContextKey> context, @Arg("discount") Value discountParam) {
    // Validate parameter is present and is a number
    if (discountParam == null) {
        throw new IllegalArgumentException("Discount parameter is required");
    }
    
    if (!discountParam.isNumber()) {
        throw new IllegalArgumentException("Discount must be a number");
    }
    
    Double discount = discountParam.asNumber().doubleValue();
    
    // Validate discount is within acceptable range
    if (discount < 0 || discount > 100) {
        throw new IllegalArgumentException("Discount must be between 0 and 100");
    }
    
    // Continue with execution...
}
```

### Context Data Validation

Always validate data retrieved from the context:

```java
@Override
public Value execute(RuleContext<CustomerContextKey> context) {
    // Check if required key exists
    if (!context.hasValue(CustomerContextKey.CUSTOMER_ID)) {
        throw new MissingContextDataException("Customer ID is required");
    }
    
    // Get and validate customer ID
    String customerId = context.getRequired(CustomerContextKey.CUSTOMER_ID, String.class);
    if (customerId.isEmpty()) {
        throw new InvalidContextDataException("Customer ID cannot be empty");
    }
    
    // Get optional data with validation
    Optional<Integer> age = context.getOptional(CustomerContextKey.AGE, Integer.class);
    if (age.isPresent() && (age.get() < 0 || age.get() > 120)) {
        throw new InvalidContextDataException("Age must be between 0 and 120");
    }
    
    // Continue with execution...
}
```

### Type Validation

When working with complex types, validate their structure:

```java
@Override
public Value execute(RuleContext<OrderContextKey> context) {
    // Get the order from context
    Order order = context.getRequired(OrderContextKey.ORDER, Order.class);
    
    // Validate order structure
    if (order.getItems() == null || order.getItems().isEmpty()) {
        throw new InvalidContextDataException("Order must contain at least one item");
    }
    
    if (order.getCustomerId() == null || order.getCustomerId().isEmpty()) {
        throw new InvalidContextDataException("Order must have a customer ID");
    }
    
    // Validate individual items
    for (OrderItem item : order.getItems()) {
        if (item.getQuantity() <= 0) {
            throw new InvalidContextDataException("Order item quantity must be greater than zero");
        }
        if (item.getPrice() < 0) {
            throw new InvalidContextDataException("Order item price cannot be negative");
        }
    }
    
    // Continue with execution...
}
```

## Custom Validation Exceptions

Define custom exceptions for different validation scenarios:

```java
/**
 * Exception thrown when required data is missing from the context.
 */
public class MissingContextDataException extends RuntimeException {
    public MissingContextDataException(String message) {
        super(message);
    }
}

/**
 * Exception thrown when data in the context is invalid.
 */
public class InvalidContextDataException extends RuntimeException {
    public InvalidContextDataException(String message) {
        super(message);
    }
}

/**
 * Exception thrown when a parameter is invalid.
 */
public class InvalidParameterException extends RuntimeException {
    private final String parameterName;
    
    public InvalidParameterException(String parameterName, String message) {
        super(message);
        this.parameterName = parameterName;
    }
    
    public String getParameterName() {
        return parameterName;
    }
}
```

## Validation Utilities

Create reusable validation utilities to simplify common validation tasks:

```java
/**
 * Utility class for validating rule context data.
 */
public class ContextValidator {
    
    /**
     * Validates that required keys are present in the context.
     *
     * @param context The rule context to validate
     * @param keys The keys that must be present
     * @throws MissingContextDataException if any key is missing
     */
    public static <T extends Enum<T>> void validateRequiredKeys(RuleContext<T> context, T... keys) {
        for (T key : keys) {
            if (!context.hasValue(key)) {
                throw new MissingContextDataException("Required key missing: " + key);
            }
        }
    }
    
    /**
     * Validates a numeric value is within a given range.
     *
     * @param value The value to validate
     * @param min The minimum allowed value (inclusive)
     * @param max The maximum allowed value (inclusive)
     * @param errorMessage The error message if validation fails
     * @throws InvalidContextDataException if validation fails
     */
    public static void validateRange(Double value, Double min, Double max, String errorMessage) {
        if (value < min || value > max) {
            throw new InvalidContextDataException(errorMessage);
        }
    }
    
    /**
     * Validates a string is not null or empty.
     *
     * @param value The string to validate
     * @param errorMessage The error message if validation fails
     * @throws InvalidContextDataException if validation fails
     */
    public static void validateNotEmpty(String value, String errorMessage) {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidContextDataException(errorMessage);
        }
    }
    
    // More validation methods...
}
```

Using these utilities in your components:

```java
@Override
public Value execute(RuleContext<OrderContextKey> context, @Arg("threshold") Value threshold) {
    // Validate required context keys
    ContextValidator.validateRequiredKeys(context, 
        OrderContextKey.ORDER_ID, 
        OrderContextKey.CUSTOMER_ID,
        OrderContextKey.ORDER_AMOUNT);
    
    // Validate order amount
    Double amount = context.getRequired(OrderContextKey.ORDER_AMOUNT, Double.class);
    ContextValidator.validateRange(amount, 0.0, Double.MAX_VALUE, 
        "Order amount must be positive");
    
    // Validate customer ID
    String customerId = context.getRequired(OrderContextKey.CUSTOMER_ID, String.class);
    ContextValidator.validateNotEmpty(customerId, "Customer ID cannot be empty");
    
    // Continue with execution...
}
```

## Defensive Validation Approaches

### Graceful Fallbacks

For non-critical validations, consider using fallbacks instead of throwing exceptions:

```java
@Override
public Value execute(RuleContext<OrderContextKey> context) {
    // Get discount with fallback to zero
    Optional<Double> discountOpt = context.getOptional(OrderContextKey.DISCOUNT_PERCENTAGE, Double.class);
    Double discount = discountOpt.orElse(0.0);
    
    // Clamp discount to valid range
    discount = Math.max(0.0, Math.min(discount, 100.0));
    
    // Get order amount with validation
    Double amount = context.getRequired(OrderContextKey.ORDER_AMOUNT, Double.class);
    if (amount < 0) {
        logger.warn("Negative order amount detected: {}. Using absolute value.", amount);
        amount = Math.abs(amount);
    }
    
    // Apply discount
    Double discountedAmount = amount * (1 - (discount / 100.0));
    context.add(OrderContextKey.DISCOUNTED_AMOUNT, discountedAmount);
    
    return Value.of(true);
}
```

### Validation Levels

Consider implementing different validation levels based on your application needs:

```java
public enum ValidationLevel {
    STRICT,    // Throw exceptions for any validation issue
    NORMAL,    // Throw exceptions for critical issues, log warnings for others
    LENIENT    // Log warnings but try to proceed for most issues
}

@Override
public Value execute(RuleContext<OrderContextKey> context, @Arg("amount") Value amountParam) {
    // Get validation level from context or use default
    ValidationLevel level = context
        .getOptional(OrderContextKey.VALIDATION_LEVEL, ValidationLevel.class)
        .orElse(ValidationLevel.NORMAL);
    
    // Validate amount based on level
    Double amount = amountParam.asNumber().doubleValue();
    if (amount < 0) {
        switch (level) {
            case STRICT:
                throw new InvalidParameterException("amount", "Amount cannot be negative");
            case NORMAL:
                logger.warn("Negative amount detected: {}. Using absolute value.", amount);
                amount = Math.abs(amount);
                break;
            case LENIENT:
                logger.info("Using negative amount as provided: {}", amount);
                break;
        }
    }
    
    // Continue with execution...
}
```

## Validation Testing

### Unit Testing Validations

Always include tests specifically for validation scenarios:

```java
@Test
public void testExecute_withNegativeDiscountParam_throwsException() {
    // Setup
    RuleContext<OrderContextKey> context = new RuleContext<>(OrderContextKey.class);
    Value negativeDiscount = Value.of(-10);
    
    // Execute and verify exception
    assertThrows(IllegalArgumentException.class, () -> {
        discountAction.execute(context, negativeDiscount);
    });
}

@Test
public void testExecute_withMissingCustomerId_throwsMissingContextDataException() {
    // Setup
    RuleContext<OrderContextKey> context = new RuleContext<>(OrderContextKey.class);
    context.add(OrderContextKey.ORDER_AMOUNT, 100.0);
    // Note: Not adding CUSTOMER_ID
    
    // Execute and verify exception
    assertThrows(MissingContextDataException.class, () -> {
        customerDiscountCheck.execute(context, Value.of(10));
    });
}
```

### Testing Error Messages

Test that error messages are clear and informative:

```java
@Test
public void testExecute_withInvalidParam_providesHelpfulErrorMessage() {
    // Setup
    RuleContext<OrderContextKey> context = new RuleContext<>(OrderContextKey.class);
    Value invalidDiscount = Value.of(150); // Over 100%
    
    // Execute and verify exception message
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
        discountAction.execute(context, invalidDiscount);
    });
    
    assertTrue(exception.getMessage().contains("between 0 and 100"),
        "Error message should explain the valid range");
}
```

## Best Practices

### 1. Validate Early

Perform validation as early as possible in your business component:

```java
@Override
public Value execute(RuleContext<T> context, @Arg("param") Value param) {
    // Validate inputs first
    validateInputs(context, param);
    
    // Then proceed with business logic
    // ...
}

private void validateInputs(RuleContext<T> context, Value param) {
    // Validation logic here
}
```

### 2. Be Specific About Requirements

In your error messages, be specific about what is expected:

```java
// Poor: "Invalid input"
// Better: "Age must be between 18 and 120"

if (age < 18 || age > 120) {
    throw new InvalidContextDataException("Age must be between 18 and 120, got: " + age);
}
```

### 3. Log Validation Failures

Always log validation failures with appropriate levels:

```java
try {
    // Validate user input
    if (userId.isEmpty()) {
        logger.warn("Empty user ID provided");
        throw new InvalidContextDataException("User ID cannot be empty");
    }
    
    // Continue with execution...
} catch (Exception e) {
    logger.error("Validation failed: {}", e.getMessage(), e);
    throw e;
}
```

### 4. Document Validation Requirements

Document all validation rules in your component's Javadoc:

```java
/**
 * Applies a percentage discount to the order amount.
 * 
 * <p>Validation requirements:</p>
 * <ul>
 *   <li>Context must contain ORDER_AMOUNT (Double, positive)</li>
 *   <li>Context must contain CUSTOMER_ID (String, non-empty)</li>
 *   <li>Discount parameter must be a number between 0 and 100</li>
 * </ul>
 *
 * @throws MissingContextDataException if required context keys are missing
 * @throws InvalidContextDataException if context data is invalid
 * @throws IllegalArgumentException if discount parameter is invalid
 */
@Override
public Value execute(RuleContext<OrderContextKey> context, @Arg("discount") Value discountParam) {
    // Implementation...
}
```

### 5. Use Default Values Carefully

When using default values, make sure they are safe and appropriate:

```java
// Get order date with fallback to current date
LocalDate orderDate = context
    .getOptional(OrderContextKey.ORDER_DATE, LocalDate.class)
    .orElse(LocalDate.now());
```

### 6. Fail Fast for Critical Validations

For critical validation issues, fail fast:

```java
// Security validation - fail fast
if (!isAuthorized(userId, requestedAction)) {
    logger.error("Unauthorized action attempt: User {} tried to perform {}", userId, requestedAction);
    throw new SecurityValidationException("User is not authorized to perform this action");
}

// Continue with other validations...
```

### 7. Separate Validation Logic

For complex components, separate validation logic for better maintainability:

```java
public class CustomerDiscountCheck implements BusinessCheck<OrderContextKey> {
    
    @Override
    public Value execute(RuleContext<OrderContextKey> context, @Arg("threshold") Value threshold) {
        // Validate inputs
        ValidationResult result = validateInputs(context, threshold);
        if (!result.isValid()) {
            throw result.getException();
        }
        
        // Business logic
        // ...
    }
    
    private ValidationResult validateInputs(RuleContext<OrderContextKey> context, Value threshold) {
        ValidationResult result = new ValidationResult();
        
        // Check required context keys
        if (!context.hasValue(OrderContextKey.CUSTOMER_ID)) {
            return result.invalid(new MissingContextDataException("Customer ID is required"));
        }
        
        if (!context.hasValue(OrderContextKey.ORDER_AMOUNT)) {
            return result.invalid(new MissingContextDataException("Order amount is required"));
        }
        
        // Validate threshold parameter
        if (!threshold.isNumber()) {
            return result.invalid(
                new IllegalArgumentException("Threshold must be a number"));
        }
        
        Double thresholdValue = threshold.asNumber().doubleValue();
        if (thresholdValue < 0) {
            return result.invalid(
                new IllegalArgumentException("Threshold cannot be negative"));
        }
        
        return result.valid();
    }
    
    private static class ValidationResult {
        private boolean valid = true;
        private RuntimeException exception;
        
        public ValidationResult valid() {
            this.valid = true;
            return this;
        }
        
        public ValidationResult invalid(RuntimeException exception) {
            this.valid = false;
            this.exception = exception;
            return this;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public RuntimeException getException() {
            return exception;
        }
    }
}
```

## Integration with Axiom Validation Framework

Axiom provides a validation framework that works at the rule set level. Business component validations are complementary to this framework:

- **Rule Set Validation**: Ensures rule sets are structurally valid before loading
- **Business Component Validation**: Ensures runtime behavior is correct during execution

You can leverage the rule set validation framework to register custom validators for your business components:

```java
public class BusinessComponentValidator<T extends Enum<T>> implements RuleSetValidator<T> {
    
    @Override
    public List<ValidationError> validate(RuleSet<T> ruleSet, 
                                          Map<String, BusinessCheck<T>> checks,
                                          Map<String, BusinessAction<T>> actions) {
        List<ValidationError> errors = new ArrayList<>();
        
        // Validate that referenced checks exist
        for (Rule<T> rule : ruleSet.getRules()) {
            String expression = rule.getExpression();
            
            // Extract check names from expression and validate they exist
            List<String> checkNames = extractCheckNames(expression);
            for (String checkName : checkNames) {
                if (!checks.containsKey(checkName)) {
                    errors.add(new ValidationError(
                        "MissingCheckError",
                        "Check '" + checkName + "' referenced in rule '" + rule.getName() + 
                        "' is not defined",
                        null
                    ));
                }
            }
            
            // Similar validation for actions
            // ...
        }
        
        return errors;
    }
    
    private List<String> extractCheckNames(String expression) {
        // Logic to extract check names from expression
        // ...
    }
}
```

Register your custom validator with the rule set loader:

```java
RuleSetLoader<MyContextKey> loader = new YamlRuleSetLoader<>("ruleset.yaml")
    .addValidator(new BusinessComponentValidator<>());
```

[← Back to Previous Section](ruleset-structure.md) 
