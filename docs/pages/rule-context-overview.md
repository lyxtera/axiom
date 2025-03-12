# Rule Context Overview

The `RuleContext` is a central component in the Axiom framework, serving as a thread-safe container for data that is processed by rules. It provides a type-safe way to store and retrieve data, ensuring that rules can access the information they need during evaluation.

## Key Features

- **Type Safety**: Uses Java generics to ensure type safety when storing and retrieving values
- **Thread Safety**: Designed to be thread-safe, allowing concurrent access from multiple threads
- **Enum-Based Keys**: Uses enum values as keys, providing compile-time validation for key names
- **Optional Values**: Uses Java's Optional to handle the absence of values gracefully
- **JSON Serialization**: Supports serialization to and from JSON for persistence or transmission

## Core Concepts

### Context Keys

The `RuleContext` uses enum values as keys to store and retrieve data. This approach provides several benefits:

1. Compile-time validation of key names
2. Clear documentation of available keys through the enum definition
3. Type safety when retrieving values

Example enum definition:

```java
public enum OrderContextKey {
    CUSTOMER_ID,
    ORDER_AMOUNT,
    PRODUCT_IDS,
    IS_REPEAT_CUSTOMER,
    HAS_DISCOUNT_APPLIED,
    SHIPPING_COUNTRY
}
```

### Value Storage and Retrieval

The `RuleContext` provides methods to store, retrieve, and remove values:

```java
// Create a new context
RuleContext<OrderContextKey> context = new RuleContext<>(OrderContextKey.class);

// Add values
context.add(OrderContextKey.CUSTOMER_ID, "CUST-12345");
context.add(OrderContextKey.ORDER_AMOUNT, 199.99);
context.add(OrderContextKey.PRODUCT_IDS, Arrays.asList("PROD-001", "PROD-002"));
context.add(OrderContextKey.IS_REPEAT_CUSTOMER, true);

// Retrieve optional values (returns Optional objects)
Optional<String> customerId = context.get(OrderContextKey.CUSTOMER_ID, String.class);
Optional<Double> orderAmount = context.get(OrderContextKey.ORDER_AMOUNT, Double.class);
Optional<List<String>> productIds = context.get(OrderContextKey.PRODUCT_IDS, List.class);

// Retrieve required values (throws exception if not present)
String requiredCustomerId = context.getRequired(OrderContextKey.CUSTOMER_ID, String.class);
Double requiredOrderAmount = context.getRequired(OrderContextKey.ORDER_AMOUNT, Double.class);

// Remove values
context.remove(OrderContextKey.IS_REPEAT_CUSTOMER, Boolean.class);

// Check if context is empty
boolean isEmpty = context.isEmpty();
```

### JSON Serialization and Deserialization

The `RuleContext` supports serialization to and from JSON, which is useful for:

- Persisting context data to a database
- Transmitting context data over a network
- Debugging rule execution by inspecting the context state

```java
// Serialize to JSON
String json = context.toJson();

// Deserialize from JSON
RuleContext<OrderContextKey> deserializedContext = 
    RuleContext.fromJson(OrderContextKey.class, json);
```

## Using RuleContext in Business Checks and Actions

Business checks and actions use the `RuleContext` to access the data they need for evaluation or execution.

### In Business Checks

Business checks retrieve values from the context to evaluate conditions:

```java
@RuleMetadata(name = "isHighValueOrder", description = "Checks if the order value exceeds a threshold")
public class HighValueOrderCheck implements BusinessCheck<OrderContextKey> {
    @Override
    public Value execute(RuleContext<OrderContextKey> context, @Arg("threshold") Value threshold) {
        Double orderAmount = context.getRequired(OrderContextKey.ORDER_AMOUNT, Double.class);
        Double thresholdValue = threshold.asDouble();
        return Value.of(orderAmount > thresholdValue);
    }
}
```

### In Business Actions

Business actions can both retrieve and modify the context:

```java
@RuleMetadata(name = "applyDiscount", description = "Applies a percentage discount to the order")
public class ApplyDiscountAction implements BusinessAction<OrderContextKey> {
    @Override
    public Value execute(RuleContext<OrderContextKey> context, @Arg("percent") Value percent) {
        Double orderAmount = context.getRequired(OrderContextKey.ORDER_AMOUNT, Double.class);
        Double discountPercent = percent.asDouble();
        
        Double discountedAmount = orderAmount * (1 - (discountPercent / 100));
        context.add(OrderContextKey.ORDER_AMOUNT, discountedAmount);
        context.add(OrderContextKey.HAS_DISCOUNT_APPLIED, true);
        
        return Value.of(true);
    }
}
```

## Best Practices

1. **Define Clear Context Keys**: Use descriptive names for your enum values and include comments to document their purpose and expected types.

2. **Keep Context Focused**: Include only the data relevant to your rules to avoid cluttering the context.

3. **Handle Missing Values Gracefully**: Use the `get` method when a value might not exist, and `getRequired` only when you're certain a value should be present.

4. **Type Safety**: Always specify the expected type when retrieving values to ensure type safety.

5. **Thread Safety**: Remember that `RuleContext` is thread-safe, but any objects you store in it might not be. Ensure that objects stored in the context are either immutable or properly synchronized if they'll be accessed concurrently.

## Related Sections

- [Rule Context Definition & Operations](rule-context-operations.md) - Details on specific operations available in RuleContext
- [Business Actions & Checks Overview](business-components-overview.md) - How business actions and checks interact with RuleContext
- [Rule Orchestrators Overview](rule-orchestrator-overview.md) - How rule orchestrators use RuleContext during rule execution 
