# Getting Started with Axiom Business Rules

This guide will help you set up and start using the Axiom Business Rules framework in your Java application. We'll cover the basic steps to integrate Axiom, create your first rule set, and execute rules.

## Prerequisites

Before you begin, ensure you have the following:

- Java 8 or higher
- Maven or Gradle for dependency management
- Basic understanding of Java and dependency injection (Guice is used in this guide)

## Adding Axiom to Your Project

### Maven

Add the Axiom dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.lyxtera</groupId>
    <artifactId>axiom</artifactId>
    <version>1.0.0</version>
</dependency>

<!-- Guice for dependency injection -->
<dependency>
    <groupId>com.google.inject</groupId>
    <artifactId>guice</artifactId>
    <version>5.1.0</version>
</dependency>
```

### Gradle

Add the Axiom dependency to your `build.gradle`:

```groovy
dependencies {
    implementation 'com.lyxtera:axiom:1.0.0'
    implementation 'com.google.inject:guice:5.1.0'
}
```

## Basic Setup Steps

Setting up Axiom involves the following steps:

1. Define your context keys
2. Create business checks and actions
3. Create rule set YAML files
4. Configure the Axiom module
5. Create and use rule orchestrators

Let's go through each step in detail.

## 1. Define Your Context Keys

Create an enum to define the keys for your rule context:

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

## 2. Create Business Checks and Actions

Create implementations of `BusinessCheck` and `BusinessAction` interfaces:

### Business Check Example

```java
@RuleMetadata(
    name = "isHighValueOrder",
    description = "Checks if the order value exceeds a threshold"
)
public class HighValueOrderCheck implements BusinessCheck<OrderContextKey> {
    
    @Override
    public Value execute(RuleContext<OrderContextKey> context, @Arg("threshold") Value threshold) {
        Double orderAmount = context.getRequired(OrderContextKey.ORDER_AMOUNT, Double.class);
        Double thresholdValue = threshold.asDouble();
        return Value.of(orderAmount > thresholdValue);
    }
}
```

### Business Action Example

```java
@RuleMetadata(
    name = "applyDiscount",
    description = "Applies a percentage discount to the order"
)
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

## 3. Create Rule Set YAML Files

Create a YAML file to define your rule set:

```yaml
# src/main/resources/order_discount_rules.yaml
rulesetName: "Order Discount Rules"
rulesetDescription: "Rules for applying discounts to orders"

businessChecks:
  - name: isHighValueOrder
    description: Checks if the order value exceeds a threshold
    params:
      - threshold
  - name: isRepeatCustomer
    description: Checks if the customer has previous orders

businessActions:
  - name: applyDiscount
    description: Applies a percentage discount to the order
    params:
      - percent

rules:
  - name: "High Value Order Discount"
    description: "Apply 10% discount to orders over $1000"
    expression: isHighValueOrder(1000) then applyDiscount(10)
    priority: 10
    effectiveFrom: "2023-01-01T00:00:00Z"
    effectiveTo: "2025-04-01T00:00:00Z"
    
  - name: "Repeat Customer Discount"
    description: "Apply 5% discount to repeat customers"
    expression: isRepeatCustomer() then applyDiscount(5)
    priority: 20
    effectiveFrom: "2023-01-01T00:00:00Z"
```

## 4. Configure the Axiom Module

You can incorporate Axiom directly into your main application Guice module using the builder pattern:

```java
public class YourApplicationGuiceModule extends AbstractModule {
    
    @Override
    protected void configure() {
        // Install Axiom module directly using the builder
        install(AxiomModule.buildForKey(OrderContextKey.class)
            .withRuleLoaders(loaders -> loaders
                .loader("order_discount", new YamlRuleSetLoader<>("order_discount_rules.yaml"))
            )
            .withChecks(checks -> checks
                .check("isHighValueOrder", HighValueOrderCheck.class)
                .check("isRepeatCustomer", RepeatCustomerCheck.class)
            )
            .withActions(actions -> actions
                .action("applyDiscount", ApplyDiscountAction.class)
            )
            .build());
        
        // Your application bindings
        bind(OrderService.class);
        // ... other bindings
    }
}
```

This approach has several advantages:
- Integrates Axiom directly into your application's module structure
- Avoids creating a separate configuration class or method
- Keeps all module configuration in one place
- Allows for more complex dependency relationships between Axiom and your application
- Provides cleaner access to RuleOrchestrators and other Axiom components

## 5. Create and Use Rule Orchestrators

Now you can use the rule orchestrator in your application:

```java
public class OrderService {
    
    private final RuleOrchestrator<OrderContextKey> discountOrchestrator;
    
    @Inject
    public OrderService(@Named("order_discount") RuleOrchestrator<OrderContextKey> discountOrchestrator) {
        this.discountOrchestrator = discountOrchestrator;
    }
    
    public Order processOrder(Order order) {
        // Create a rule context with order data
        RuleContext<OrderContextKey> context = new RuleContext<>(OrderContextKey.class);
        context.add(OrderContextKey.CUSTOMER_ID, order.getCustomerId());
        context.add(OrderContextKey.ORDER_AMOUNT, order.getTotalAmount());
        context.add(OrderContextKey.IS_REPEAT_CUSTOMER, isRepeatCustomer(order.getCustomerId()));
        
        // Execute all matching rules
        RuleExecutionResult<OrderContextKey> result = discountOrchestrator.executeAllMatches(context);
        
        // Update the order with the potentially modified amount
        if (result.hasMatches()) {
            Double discountedAmount = result.getContext().getRequired(OrderContextKey.ORDER_AMOUNT, Double.class);
            order.setTotalAmount(discountedAmount);
            
            // Log which rules were applied
            result.getMatchedRules().forEach(rule -> 
                System.out.println("Applied rule: " + rule.getName()));
        }
        
        return order;
    }
    
    private boolean isRepeatCustomer(String customerId) {
        // Implementation to check if this is a repeat customer
        return true; // Simplified for this example
    }
}
```

## Setting Up the Application

Finally, set up your application with Guice using your main application module:

```java
public class Application {
    
    public static void main(String[] args) {
        // Create the Guice injector with your main application module
        Injector injector = Guice.createInjector(new YourApplicationGuiceModule());
        
        // Get the order service
        OrderService orderService = injector.getInstance(OrderService.class);
        
        // Create and process an order
        Order order = new Order("CUST-12345", 1500.0);
        Order processedOrder = orderService.processOrder(order);
        
        System.out.println("Original amount: $1500.00");
        System.out.println("Processed amount: $" + processedOrder.getTotalAmount());
    }
}
```

This approach simplifies your application bootstrap process and follows standard Guice practices for modular applications.

## Next Steps

Now that you have a basic understanding of how to set up and use Axiom, you can explore more advanced topics:

- [Rule Sets Overview](ruleset-overview.md) - Learn more about rule sets and their structure
- [Rule Context Overview](rule-context-overview.md) - Understand how to work with rule contexts
- [Business Actions & Checks Overview](business-components-overview.md) - Dive deeper into business actions and checks
- [Rule Orchestrators Overview](rule-orchestrator-overview.md) - Explore advanced orchestrator features

By following this guide, you should now have a working Axiom integration in your application. You can expand on this foundation by adding more complex rules, checks, and actions to meet your business requirements.

[← Back to Introduction](introduction.md) 
