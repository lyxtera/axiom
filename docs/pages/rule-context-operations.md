# Rule Context Operations

The `RuleContext` class provides a rich set of operations for storing, retrieving, and manipulating data during rule evaluation. This guide explores the advanced operations available in `RuleContext` and provides practical examples of their usage.

## Core Operations

### Adding Values

The primary way to add values to a context is using the `add` method:

```java
RuleContext<OrderContextKey> context = new RuleContext<>(OrderContextKey.class);

// Add simple values
context.add(OrderContextKey.CUSTOMER_ID, "CUST-12345");
context.add(OrderContextKey.ORDER_AMOUNT, 199.99);
context.add(OrderContextKey.IS_PRIME_MEMBER, true);

// Add complex objects
List<String> productIds = Arrays.asList("PROD-001", "PROD-002", "PROD-003");
context.add(OrderContextKey.PRODUCT_IDS, productIds);

Map<String, Integer> productQuantities = new HashMap<>();
productQuantities.put("PROD-001", 2);
productQuantities.put("PROD-002", 1);
productQuantities.put("PROD-003", 3);
context.add(OrderContextKey.PRODUCT_QUANTITIES, productQuantities);

// Add dates and times
context.add(OrderContextKey.ORDER_DATE, ZonedDateTime.now());
```

### Retrieving Values

There are two primary methods for retrieving values:

1. **`get`**: Returns an `Optional` containing the value if present
2. **`getRequired`**: Returns the value directly, throwing an exception if not present

```java
// Using get (returns Optional)
Optional<String> customerId = context.get(OrderContextKey.CUSTOMER_ID, String.class);
if (customerId.isPresent()) {
    System.out.println("Customer ID: " + customerId.get());
}

// Using getRequired (throws exception if not present)
try {
    Double orderAmount = context.getRequired(OrderContextKey.ORDER_AMOUNT, Double.class);
    System.out.println("Order amount: $" + orderAmount);
} catch (ContextException e) {
    System.err.println("Required value not found: " + e.getMessage());
}

// Getting complex objects
Optional<List<String>> optionalProductIds = context.get(OrderContextKey.PRODUCT_IDS, List.class);
if (optionalProductIds.isPresent()) {
    List<String> products = optionalProductIds.get();
    System.out.println("Number of products: " + products.size());
}

// Type casting for complex types
Optional<Map<String, Integer>> optionalQuantities = 
    context.get(OrderContextKey.PRODUCT_QUANTITIES, Map.class);
if (optionalQuantities.isPresent()) {
    Map<String, Integer> quantities = optionalQuantities.get();
    // Note: You may need to handle type casting for generic types carefully
}
```

### Removing Values

Values can be removed from the context using the `remove` method:

```java
// Remove a value
context.remove(OrderContextKey.TEMPORARY_DATA, Object.class);

// Check if removal was successful
boolean stillExists = context.get(OrderContextKey.TEMPORARY_DATA, Object.class).isPresent();
```

### Checking for Empty Context

You can check if a context has any values using the `isEmpty` method:

```java
if (context.isEmpty()) {
    System.out.println("Context is empty");
} else {
    System.out.println("Context contains values");
}
```

## Advanced Operations

### JSON Serialization and Deserialization

The `RuleContext` supports conversion to and from JSON:

```java
// Convert context to JSON
String json = context.toJson();
System.out.println("Context as JSON: " + json);

// Create a new context from JSON
RuleContext<OrderContextKey> deserializedContext = 
    RuleContext.fromJson(OrderContextKey.class, json);

// Verify values were preserved
Optional<String> deserializedCustomerId = 
    deserializedContext.get(OrderContextKey.CUSTOMER_ID, String.class);
```

This is particularly useful for:
- Persisting contexts to databases
- Transmitting contexts between services
- Debugging rule execution by logging contexts
- Creating snapshots of context state for testing

### Type-safe Access with CtxGet

For more type-safe access to context values, you can use the `CtxGet` utility:

```java
// Create a type-safe accessor for a specific context key and type
CtxGet<OrderContextKey, String> customerId = 
    CtxGet.of(OrderContextKey.CUSTOMER_ID, String.class);

// Use the accessor to get values from a context
Optional<String> id = customerId.from(context);

// Use with getRequired
String requiredId = customerId.getRequired(context);
```

This approach provides better type safety when accessing context values in multiple places.

### Context Merging

You can merge multiple contexts together:

```java
// Create two contexts
RuleContext<OrderContextKey> orderContext = new RuleContext<>(OrderContextKey.class);
orderContext.add(OrderContextKey.ORDER_AMOUNT, 199.99);

RuleContext<OrderContextKey> customerContext = new RuleContext<>(OrderContextKey.class);
customerContext.add(OrderContextKey.CUSTOMER_ID, "CUST-12345");

// Merge contexts (creates a new context with values from both)
RuleContext<OrderContextKey> mergedContext = RuleContext.merge(orderContext, customerContext);

// Values from both contexts are accessible
Optional<String> customerId = mergedContext.get(OrderContextKey.CUSTOMER_ID, String.class);
Optional<Double> orderAmount = mergedContext.get(OrderContextKey.ORDER_AMOUNT, Double.class);
```

If both contexts contain the same key, the value from the later context in the merge parameters will be used.

### Bulk Operations

For performing operations on multiple values:

```java
// Add multiple values at once
Map<OrderContextKey, Object> values = new HashMap<>();
values.put(OrderContextKey.CUSTOMER_ID, "CUST-12345");
values.put(OrderContextKey.ORDER_AMOUNT, 199.99);
values.put(OrderContextKey.IS_PRIME_MEMBER, true);

RuleContext<OrderContextKey> context = new RuleContext<>(OrderContextKey.class);
context.addAll(values);

// Get all keys
Set<OrderContextKey> keys = context.getKeys();
System.out.println("Context contains keys: " + keys);

// Clear all values
context.clear();
```

## Common Patterns

### Context Builder Pattern

For creating contexts with many values, a builder pattern can be helpful:

```java
public class OrderContextBuilder {
    private RuleContext<OrderContextKey> context;
    
    public OrderContextBuilder() {
        context = new RuleContext<>(OrderContextKey.class);
    }
    
    public OrderContextBuilder withCustomerId(String customerId) {
        context.add(OrderContextKey.CUSTOMER_ID, customerId);
        return this;
    }
    
    public OrderContextBuilder withOrderAmount(Double amount) {
        context.add(OrderContextKey.ORDER_AMOUNT, amount);
        return this;
    }
    
    public OrderContextBuilder withProducts(List<String> productIds) {
        context.add(OrderContextKey.PRODUCT_IDS, productIds);
        return this;
    }
    
    public OrderContextBuilder withPrimeMembership(boolean isPrimeMember) {
        context.add(OrderContextKey.IS_PRIME_MEMBER, isPrimeMember);
        return this;
    }
    
    public RuleContext<OrderContextKey> build() {
        return context;
    }
}

// Usage
RuleContext<OrderContextKey> context = new OrderContextBuilder()
    .withCustomerId("CUST-12345")
    .withOrderAmount(199.99)
    .withProducts(Arrays.asList("PROD-001", "PROD-002"))
    .withPrimeMembership(true)
    .build();
```

### Context Factory Pattern

For creating pre-configured contexts for different scenarios:

```java
public class OrderContextFactory {
    public static RuleContext<OrderContextKey> createPrimeCustomerContext(String customerId) {
        RuleContext<OrderContextKey> context = new RuleContext<>(OrderContextKey.class);
        context.add(OrderContextKey.CUSTOMER_ID, customerId);
        context.add(OrderContextKey.IS_PRIME_MEMBER, true);
        context.add(OrderContextKey.CUSTOMER_TIER, "PRIME");
        return context;
    }
    
    public static RuleContext<OrderContextKey> createNewCustomerContext(String customerId) {
        RuleContext<OrderContextKey> context = new RuleContext<>(OrderContextKey.class);
        context.add(OrderContextKey.CUSTOMER_ID, customerId);
        context.add(OrderContextKey.IS_NEW_CUSTOMER, true);
        context.add(OrderContextKey.CUSTOMER_CREATION_DATE, ZonedDateTime.now());
        return context;
    }
    
    public static RuleContext<OrderContextKey> createHighValueOrderContext(
            String customerId, double amount) {
        RuleContext<OrderContextKey> context = new RuleContext<>(OrderContextKey.class);
        context.add(OrderContextKey.CUSTOMER_ID, customerId);
        context.add(OrderContextKey.ORDER_AMOUNT, amount);
        context.add(OrderContextKey.IS_HIGH_VALUE, true);
        return context;
    }
}

// Usage
RuleContext<OrderContextKey> primeContext = 
    OrderContextFactory.createPrimeCustomerContext("CUST-12345");
RuleContext<OrderContextKey> highValueContext = 
    OrderContextFactory.createHighValueOrderContext("CUST-67890", 999.99);
```

### Context Decoration Pattern

For incrementally enhancing a context with additional information:

```java
public class OrderContextDecorator {
    public static RuleContext<OrderContextKey> addPriceInformation(
            RuleContext<OrderContextKey> context) {
        // Get product IDs
        List<String> productIds = context.getRequired(OrderContextKey.PRODUCT_IDS, List.class);
        
        // Simulate looking up prices from a product service
        Map<String, Double> productPrices = new HashMap<>();
        for (String productId : productIds) {
            double price = lookupProductPrice(productId);
            productPrices.put(productId, price);
        }
        
        // Calculate total
        double totalPrice = productPrices.values().stream().mapToDouble(Double::valueOf).sum();
        
        // Add to context
        context.add(OrderContextKey.PRODUCT_PRICES, productPrices);
        context.add(OrderContextKey.ORDER_AMOUNT, totalPrice);
        
        return context;
    }
    
    public static RuleContext<OrderContextKey> addCustomerInformation(
            RuleContext<OrderContextKey> context) {
        // Get customer ID
        String customerId = context.getRequired(OrderContextKey.CUSTOMER_ID, String.class);
        
        // Simulate looking up customer information
        CustomerInfo info = lookupCustomerInfo(customerId);
        
        // Add to context
        context.add(OrderContextKey.CUSTOMER_TIER, info.getTier());
        context.add(OrderContextKey.IS_PRIME_MEMBER, info.isPrimeMember());
        context.add(OrderContextKey.CUSTOMER_REGION, info.getRegion());
        
        return context;
    }
    
    // Simulation methods
    private static double lookupProductPrice(String productId) {
        // In a real implementation, this would call a product service
        return 19.99;
    }
    
    private static CustomerInfo lookupCustomerInfo(String customerId) {
        // In a real implementation, this would call a customer service
        return new CustomerInfo("SILVER", true, "US-EAST");
    }
    
    private static class CustomerInfo {
        private final String tier;
        private final boolean primeMember;
        private final String region;
        
        public CustomerInfo(String tier, boolean primeMember, String region) {
            this.tier = tier;
            this.primeMember = primeMember;
            this.region = region;
        }
        
        public String getTier() { return tier; }
        public boolean isPrimeMember() { return primeMember; }
        public String getRegion() { return region; }
    }
}

// Usage
RuleContext<OrderContextKey> context = new RuleContext<>(OrderContextKey.class);
context.add(OrderContextKey.CUSTOMER_ID, "CUST-12345");
context.add(OrderContextKey.PRODUCT_IDS, Arrays.asList("PROD-001", "PROD-002"));

// Decorate with additional information
context = OrderContextDecorator.addPriceInformation(context);
context = OrderContextDecorator.addCustomerInformation(context);
```

## Best Practices

### 1. Use Strong Typing

Always specify the expected type when retrieving values:

```java
// Good - with explicit type
Optional<String> customerId = context.get(OrderContextKey.CUSTOMER_ID, String.class);

// Bad - returns Optional<Object>
Optional<?> genericValue = context.get(OrderContextKey.CUSTOMER_ID);
```

### 2. Handle Missing Values Gracefully

Use `get` when a value might not be present and handle the Optional properly:

```java
// Good - safe handling
Optional<Double> discountPercentage = context.get(OrderContextKey.DISCOUNT_PERCENTAGE, Double.class);
double finalDiscount = discountPercentage.orElse(0.0);

// Good - with more complex fallback
double finalDiscount = context.get(OrderContextKey.DISCOUNT_PERCENTAGE, Double.class)
    .orElseGet(() -> calculateDefaultDiscount(context));

// Bad - potential NullPointerException
Double discount = context.get(OrderContextKey.DISCOUNT_PERCENTAGE, Double.class).get();
```

### 3. Use getRequired Judiciously

Use `getRequired` only when you're certain a value should be present:

```java
// Good - when the value must be present
String customerId = context.getRequired(OrderContextKey.CUSTOMER_ID, String.class);

// Better - with meaningful exception
try {
    String customerId = context.getRequired(OrderContextKey.CUSTOMER_ID, String.class);
} catch (ContextException e) {
    throw new BusinessException("Customer ID is required for this operation", e);
}
```

### 4. Keep Contexts Focused

Include only the data relevant to your rules to avoid cluttering the context:

```java
// Good - relevant data only
context.add(OrderContextKey.ORDER_AMOUNT, 199.99);
context.add(OrderContextKey.CUSTOMER_TIER, "GOLD");

// Bad - irrelevant data
context.add(OrderContextKey.WEBPAGE_COLOR_THEME, "DARK");
context.add(OrderContextKey.LAST_DB_QUERY_TIME_MS, 23);
```

### 5. Document Context Keys

Document your enum keys to make it clear what each key represents:

```java
/**
 * Keys for the order processing rule context.
 */
public enum OrderContextKey {
    /**
     * The unique identifier of the customer. Type: String
     */
    CUSTOMER_ID,
    
    /**
     * The total amount of the order in USD. Type: Double
     */
    ORDER_AMOUNT,
    
    /**
     * Whether the customer is a prime member. Type: Boolean
     */
    IS_PRIME_MEMBER,
    
    /**
     * List of product IDs in the order. Type: List<String>
     */
    PRODUCT_IDS
}
```

### 6. Clean Up Temporary Values

Remove temporary or intermediate values that are no longer needed:

```java
// Add temporary calculation values
context.add(OrderContextKey.TEMP_SUBTOTAL, 190.0);
context.add(OrderContextKey.TEMP_TAX_RATE, 0.05);

// Perform calculations
double subtotal = context.getRequired(OrderContextKey.TEMP_SUBTOTAL, Double.class);
double taxRate = context.getRequired(OrderContextKey.TEMP_TAX_RATE, Double.class);
double total = subtotal * (1 + taxRate);

// Add the final result
context.add(OrderContextKey.ORDER_TOTAL, total);

// Clean up temporary values
context.remove(OrderContextKey.TEMP_SUBTOTAL, Double.class);
context.remove(OrderContextKey.TEMP_TAX_RATE, Double.class);
```

### 7. Use Immutable Objects When Possible

Store immutable objects in the context when possible to prevent accidental modifications:

```java
// Good - immutable list
List<String> productIds = List.of("PROD-001", "PROD-002");
context.add(OrderContextKey.PRODUCT_IDS, productIds);

// Good - defensive copy of mutable object
List<String> mutableList = new ArrayList<>();
mutableList.add("PROD-001");
mutableList.add("PROD-002");
context.add(OrderContextKey.PRODUCT_IDS, Collections.unmodifiableList(new ArrayList<>(mutableList)));

// Bad - mutable object that could be changed elsewhere
context.add(OrderContextKey.PRODUCT_IDS, mutableList);
```

## Related Sections

- [Rule Context Overview](rule-context-overview.md) - General information about rule contexts
- [Business Actions & Checks Overview](business-components-overview.md) - How context is used in actions and checks
- [Rule Testing](rule-testing.md) - How to test code that uses rule contexts 
