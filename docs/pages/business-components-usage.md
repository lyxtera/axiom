# Business Components Usage

This document covers how to use business checks and actions effectively in your rule expressions. You'll learn how to define rule expressions, pass parameters, and follow best practices for component usage.

## Rule Expression Basics

Rule expressions in Axiom follow a simple pattern:

```
check_condition then action
```

Where:
- `check_condition` is an expression that evaluates to true or false
- `then` is a keyword that separates the condition from the action
- `action` is the action to perform when the condition is true

For example:

```
isHighValueCustomer(5000) then applyPremiumDiscount(15)
```

This rule checks if a customer is a high-value customer with a spending threshold of $5,000, and if true, applies a 15% premium discount.

## Working with Business Checks in Expressions

### Basic Check Usage

The simplest form of a check in a rule expression is:

```
checkName(param1, param2, ...)
```

For example:

```
isAdult(18)
```

### Logical Operators

You can combine multiple checks using logical operators:

```
checkA(param) AND checkB(param)   // Both must be true
checkA(param) OR checkB(param)    // Either can be true
NOT checkA(param)                 // Inverts the result
```

Examples:

```
isAdult(18) AND hasValidId()
isPreferredCustomer() OR hasActivePromotion()
NOT isRestrictedRegion("US,CA")
```

### Complex Expressions

You can create more complex expressions using parentheses:

```
(checkA() OR checkB()) AND NOT checkC()
```

For example:

```
(isPremiumMember() OR hasLoyaltyStatus("GOLD")) AND NOT hasOutstandingInvoices()
```

## Working with Business Actions in Expressions

Actions are simpler than checks because they're always placed after the `then` keyword and can't be combined:

```
then actionName(param1, param2, ...)
```

For example:

```
then applyDiscount(10)
then addLoyaltyPoints(100, "PROMOTION")
```

## Parameter Types

Axiom supports different parameter types in rule expressions:

### String Parameters

String parameters must be enclosed in quotes:

```
isInRegion("North America")
```

### Numeric Parameters

Numbers don't need quotes:

```
isAboveThreshold(1000)
isWithinRange(0.5, 1.5)
```

### Boolean Parameters

Boolean values don't need quotes:

```
setFlag(true)
enableFeature(false)
```

### List Parameters

You can pass comma-separated values that will be converted to lists:

```
isInCountries("US,CA,MX")
```

In your implementation, you would parse this:

```java
String countries = countryParam.asString();
List<String> countryList = Arrays.asList(countries.split(","));
```

### Date Parameters

Dates should be passed as ISO-8601 formatted strings:

```
isAfterDate("2023-01-01T00:00:00Z")
```

In your implementation, you would parse this:

```java
String dateStr = dateParam.asString();
Instant date = Instant.parse(dateStr);
```

## Real-World Usage Examples

### E-commerce Discount Rules

```yaml
rules:
  - name: "Premium Member Discount"
    description: "Apply 15% discount for premium members"
    expression: isPremiumMember() then applyDiscount(15, "PREMIUM_MEMBER")
    priority: 10
    
  - name: "First-Time Customer Discount"
    description: "Apply 10% discount for first-time customers"
    expression: isFirstTimeCustomer() then applyDiscount(10, "FIRST_TIME")
    priority: 20
    
  - name: "Large Order Discount"
    description: "Apply 5% discount for orders over $1000"
    expression: isOrderValueAbove(1000) then applyDiscount(5, "LARGE_ORDER")
    priority: 30
    
  - name: "Weekend Flash Sale"
    description: "Apply 20% discount during weekend flash sale hours"
    expression: isWeekend() AND isTimeBetween("10:00", "14:00") then applyDiscount(20, "FLASH_SALE")
    priority: 5
```

### Fraud Detection Rules

```yaml
rules:
  - name: "High-Risk Country Order"
    description: "Flag orders from high-risk countries for review"
    expression: isFromHighRiskCountry() then flagForReview("COUNTRY_RISK", "HIGH")
    priority: 10
    
  - name: "Multiple Failed Payments"
    description: "Block accounts with multiple failed payment attempts"
    expression: hasFailedPaymentAttempts(3) then blockAccount("PAYMENT_FAILURES")
    priority: 5
    
  - name: "Unusual Purchase Pattern"
    description: "Flag unusual purchase patterns for review"
    expression: isUnusualPurchasePattern(0.85) then flagForReview("UNUSUAL_PATTERN", "MEDIUM")
    priority: 15
```

### Content Moderation Rules

```yaml
rules:
  - name: "Explicit Content Detection"
    description: "Automatically reject content flagged as explicit"
    expression: containsExplicitContent(0.9) then rejectContent("EXPLICIT")
    priority: 5
    
  - name: "Potential Copyright Infringement"
    description: "Flag content with potential copyright issues for review"
    expression: copyrightMatchScore(0.7) then flagForReview("COPYRIGHT", "HIGH")
    priority: 10
```

## Best Practices for Component Usage

### 1. Consistent Naming Conventions

Use consistent naming patterns for your checks and actions:

- Checks: Use "is", "has", or "can" prefixes for boolean checks
- Actions: Use verb-based names that describe what they do

Good examples:
- `isEligibleForDiscount()`
- `hasCompletedProfile()`
- `applyDiscount()`
- `sendNotification()`

### 2. Parameter Organization

- Order parameters from most important to least important
- Use consistent parameter ordering across similar components
- Consider using default values for optional parameters

### 3. Error Handling

- Validate parameters in your component implementations
- Handle missing context data gracefully
- Provide clear error messages

### 4. Documentation

- Document the purpose of each check and action clearly
- Describe parameter requirements and expected values
- Include examples of usage in rule expressions

### 5. Granularity

- Keep checks and actions focused on a single responsibility
- Prefer multiple specific checks over complex checks with many parameters
- Balance specificity with reusability

For example, instead of:
```
isEligibleForDiscount(type, amount, region, ...)
```

Consider:
```
isPreferredCustomerType(type) AND isInTargetRegion(region) then applyDiscount(amount)
```

### 6. Common Patterns

#### Feature Flagging

```
isFeatureEnabled("feature_name") then enableFeature()
```

#### Progressive Discounting

```
isOrderValueAbove(1000) then applyDiscount(5)
isOrderValueAbove(2000) then applyDiscount(10)
isOrderValueAbove(5000) then applyDiscount(15)
```

#### Combined Conditions

```
isPremiumMember() AND hasItemInCart("PREMIUM_ONLY") then allowPurchase()
```

#### Exclusion Rules

```
isRestrictedUser() then blockOperation("USER_RESTRICTED")
isRestrictedRegion(region) then blockOperation("REGION_RESTRICTED")
```

## Testing Component Usage

When testing rules that use your components:

1. Create unit tests for individual checks and actions
2. Create integration tests for complete rule executions
3. Test edge cases and boundary conditions
4. Verify that rules combine components as expected

Example test:

```java
@Test
public void testPremiumMemberDiscount() {
    // Setup test context
    RuleContext<OrderContextKey> context = new RuleContext<>(OrderContextKey.class);
    context.add(OrderContextKey.USER_TYPE, "PREMIUM");
    context.add(OrderContextKey.ORDER_AMOUNT, 100.0);
    
    // Execute rule set
    RuleExecutionResult<OrderContextKey> result = discountOrchestrator.executeAllMatches(context);
    
    // Verify results
    assertTrue(result.hasMatches());
    assertEquals(1, result.getMatchedRules().size());
    assertEquals("Premium Member Discount", result.getMatchedRules().get(0).getName());
    assertEquals(85.0, context.getRequired(OrderContextKey.ORDER_AMOUNT, Double.class), 0.01);
}
```

## Performance Considerations

1. **Keep Checks Lightweight**: Checks are evaluated first and potentially more often than actions.

2. **Cache Expensive Operations**: For checks that need expensive operations:

   ```java
   public Value execute(RuleContext<T> context, ...) {
       // Check if we've already calculated this result
       Optional<Boolean> cachedResult = context.getOptional(
           MyContextKey.CACHED_EXPENSIVE_CHECK_RESULT, 
           Boolean.class
       );
       
       if (cachedResult.isPresent()) {
           return Value.of(cachedResult.get());
       }
       
       // Perform expensive calculation
       boolean result = performExpensiveOperation();
       
       // Cache the result in the context
       context.add(MyContextKey.CACHED_EXPENSIVE_CHECK_RESULT, result);
       
       return Value.of(result);
   }
   ```

3. **Optimize Order of Checks**: In complex expressions, put faster/more likely to fail checks first:

   ```
   isFastCheck() AND isExpensiveCheck() then doAction()
   ```

4. **Reuse Context Objects**: When executing multiple rule sets, reuse the same context object.

[← Back to Previous Section](ruleset-structure.md) 
