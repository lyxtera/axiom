# Business Actions & Checks Overview

Business Actions and Business Checks are the core components that define the behavior of rules in the Axiom framework. They provide the building blocks for creating expressive and powerful business rules.

## What are Business Checks and Actions?

- **Business Checks** are functions that evaluate conditions against a rule context. They return boolean values indicating whether a condition is met.
- **Business Actions** are functions that perform operations when a rule's condition is met. They can modify the rule context or perform external operations.

Both Business Checks and Business Actions are implemented as Java classes that implement the respective interfaces and are registered with the Axiom framework.

## Business Checks

Business Checks implement the `BusinessCheck<K>` interface, which defines a single method:

```java
public interface BusinessCheck<K extends Enum<K>> extends RuleFunction<K> {
    // Inherited from RuleFunction
    Value execute(RuleContext<K> context, Value... args);
}
```

### Implementing a Business Check

Here's an example of a Business Check implementation:

```java
@RuleMetadata(
    name = "hasRiskScore",
    description = "Checks if the risk score is above a specified threshold"
)
public class HasRiskScoreCheck implements BusinessCheck<MyContextKey> {
    
    @Override
    public Value execute(RuleContext<MyContextKey> context, @Arg("threshold") Value threshold) {
        // Get the risk score from the context
        Integer riskScore = context.getRequired(MyContextKey.RISK_SCORE, Integer.class);
        
        // Get the threshold value from the argument
        Integer thresholdValue = threshold.asNumber().intValue();
        
        // Return true if the risk score is greater than or equal to the threshold
        return Value.of(riskScore >= thresholdValue);
    }
}
```

Key points about this implementation:

1. The `@RuleMetadata` annotation provides metadata about the check, including its name and description.
2. The `execute` method takes a `RuleContext` and optional arguments, and returns a `Value` object.
3. The `@Arg` annotation is used to name the arguments, which helps with validation and documentation.
4. The implementation retrieves values from the context, performs a comparison, and returns a boolean result.

### Using Business Checks in Rules

Business Checks are used in rule expressions to define conditions:

```yaml
rules:
  - name: "High Risk Score Rule"
    description: "Block requests with very high risk scores"
    expression: hasRiskScore(90) then blockRequest()
    priority: 80
    effectiveFrom: "2023-05-15T00:00:00Z"
```

In this example, `hasRiskScore(90)` is a call to the `HasRiskScoreCheck` with an argument of `90`.

## Business Actions

Business Actions implement the `BusinessAction<K>` interface, which also defines a single method:

```java
public interface BusinessAction<K extends Enum<K>> extends RuleFunction<K> {
    // Inherited from RuleFunction
    Value execute(RuleContext<K> context, Value... args);
}
```

### Implementing a Business Action

Here's an example of a Business Action implementation:

```java
@RuleMetadata(
    name = "blockRequest",
    description = "Blocks the request entirely"
)
public class BlockRequestAction implements BusinessAction<MyContextKey> {
    
    @Override
    public Value execute(RuleContext<MyContextKey> context) {
        // Mark the request as blocked in the context
        context.add(MyContextKey.REQUEST_BLOCKED, true);
        
        // Log the block action
        String requestId = context.getRequired(MyContextKey.REQUEST_ID, String.class);
        System.out.println("Blocking request: " + requestId);
        
        // Return true to indicate the action was performed
        return Value.of(true);
    }
}
```

Key points about this implementation:

1. The `@RuleMetadata` annotation provides metadata about the action, including its name and description.
2. The `execute` method takes a `RuleContext` and optional arguments, and returns a `Value` object.
3. The implementation modifies the context by adding a value indicating the request is blocked.
4. The implementation may perform additional operations, such as logging.
5. The action returns a boolean value indicating whether it was successful.

### Using Business Actions in Rules

Business Actions are used in rule expressions to define what happens when a condition is met:

```yaml
rules:
  - name: "Fraud Detection Rule"
    description: "Block requests with fraud signals"
    expression: hasFraudSignals() then blockRequest()
    priority: 100
    effectiveFrom: "2023-01-01T00:00:00Z"
```

In this example, `blockRequest()` is a call to the `BlockRequestAction`.

## Registering Business Checks and Actions

Business Checks and Actions must be registered with the Axiom framework to be used in rules. This is typically done in an `AxiomModule` implementation:

```java
public class MyAxiomModule extends AxiomModule<MyContextKey> {
    
    @Override
    protected void configureBusinessRules(
            MapBinder<String, BusinessCheck<MyContextKey>> checks,
            MapBinder<String, BusinessAction<MyContextKey>> actions) {
        // Register business checks
        checks.addBinding("hasFraudSignals").to(HasFraudSignalsCheck.class);
        checks.addBinding("hasRiskScore").to(HasRiskScoreCheck.class);
        
        // Register business actions
        actions.addBinding("blockRequest").to(BlockRequestAction.class);
        actions.addBinding("flagForReview").to(FlagForReviewAction.class);
    }
    
    // Other methods...
}
```

The names used in the `addBinding` calls must match the names used in rule expressions.

## Working with Arguments

Both Business Checks and Business Actions can accept arguments in rule expressions. These arguments are passed as `Value` objects to the `execute` method.

### Defining Arguments

Arguments can be defined using the `@Arg` annotation:

```java
@Override
public Value execute(RuleContext<MyContextKey> context, 
                     @Arg("threshold") Value threshold,
                     @Arg("tolerance") Value tolerance) {
    // Use the arguments...
}
```

### Accessing Argument Values

The `Value` class provides methods to convert the argument to various types:

```java
// Convert to primitive types
        Integer intValue = value.asNumber().intValue();
Double doubleValue = value.asNumber().doubleValue();
Boolean boolValue = value.asBoolean();
String stringValue = value.asString();

// Check the type
boolean isInteger = value.isInteger();
boolean isDouble = value.isDouble();
boolean isBoolean = value.isBoolean();
boolean isString = value.isString();
```

### Validating Arguments

It's important to validate arguments to ensure they are of the expected type:

```java
if (!threshold.isInteger()) {
    throw new IllegalArgumentException("Threshold must be an integer");
}
```

## Best Practices

1. **Keep Checks and Actions Focused**: Each check or action should have a single, well-defined responsibility.

2. **Use Clear Names**: Choose descriptive names for your checks and actions that clearly indicate their purpose.

3. **Provide Detailed Descriptions**: Use the `description` field in the `@RuleMetadata` annotation to provide clear documentation.

4. **Validate Inputs**: Always validate context values and arguments to ensure they are of the expected type.

5. **Handle Errors Gracefully**: Catch and handle exceptions appropriately to prevent rule execution from failing unexpectedly.

6. **Document Expected Context Keys**: Clearly document which context keys your checks and actions expect to be present.

7. **Return Meaningful Values**: Ensure that your checks and actions return values that accurately reflect their execution status.

## Related Sections

- [Business Components Definition](business-components-definition.md) - How to define business components in rule sets
- [Business Components Usage](business-components-usage.md) - How to use business components in rules
- [Business Components Implementation](business-components-implementation.md) - Detailed implementation guidelines
- [Business Components Validation](business-components-validation.md) - How to validate business components

[← Back to Previous Section](ruleset-structure.md) 
