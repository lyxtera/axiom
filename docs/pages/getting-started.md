# Getting Started with Axiom Business Rules

This guide aims to provide you with:

1. A clear understanding of Axiom business rules concepts
2. Step-by-step instructions for creating and testing rules
3. Best practices for rule development
4. Real-world examples based on actual implementations
5. Troubleshooting tips


## Architecture Overview

### Business Rule

A “rule” is a small, self-contained piece of logic that says: “When certain conditions are met, perform these action(s).”
Each rule has two main parts:
- Condition(s) – A Boolean check (or set of checks combined using logical operators) that determins if the rule should fire.
- Action(s) – One or more pieces of code to execute when the conditions evaluate to true.

### Rule Sets

Collections of business rules combined with metadata and priority. Rule-sets group related rules together and maintain rule priority ordering. By default loaded from YAML files.

### Rule Parser

Converts rule definitions to executable objects. The rule parser interprets the rule expressions and creates the appropriate Java objects.

### Business Checks

Functions that represent declared conditions and are implemented as the `BusinessCheck<K>` interface. They can take (0..N) parameters and return `Value` objects of the type `Value.Type.BOOLEAN` indicating whether a condition is met.

```java
@RuleMetadata(name = "hasRiskScore", description = "Checks if the risk score is above a specified threshold")
public class HasRiskScoreCheck implements BusinessCheck<ContextKey> {
    public Value execute(RuleContext<ContextKey> context, @Arg("threshold") Value threshold) {
        Integer riskScore = context.getRequired(ContextKey.RISK_SCORE, Integer.class);
        Integer thresholdValue = threshold.asNumber().intValue();
        return Value.of(riskScore >= thresholdValue);
    }
}
```

### Business Actions

Functions that perform actions when rules match. Business actions are implemented as Java classes that implement the `BusinessAction<K>` interface. They are executed when a rule's condition is met.

```java
@RuleMetadata(name = "blockRequest", description = "Blocks the suspension request entirely")
public class BlockRequestAction implements BusinessAction<ContextKey> {
    @Override
    public Value execute(RuleContext<ContextKey> context) {
        context.add(ContextKey.REQUEST_BLOCKED, true);
        return Value.of(true);
    }
}
```

### Rule Context

A thread-safe container for data being processed by rules. The rule context provides a type-safe way to store and retrieve data during rule evaluation.

```java
RuleContext<ContextKey> context = new RuleContext<>(ContextKey.class);
context.add(ContextKey.CUSTOMER_ID, "C12345");
context.add(ContextKey.TRANSACTION_AMOUNT, 9999.99);
```

### Rule Orchestrator

Coordinates rule evaluation and execution. The rule orchestrator applies rules from a rule set against a given context and provides methods to execute rules and retrieve results.

```java
RuleOrchestrator<ContextKey> orchestrator = new RuleOrchestrator<>(ruleSet);
RuleExecutionResult<ContextKey> result = orchestrator.executeFirstMatchingRule(context);
```



This modular design allows for flexible integration with existing Java applications using dependency injection frameworks like Guice.

## How to Use This Guide

The guide is organized in a logical progression, starting with basic concepts and moving toward more advanced topics. If you're new to Axiom, we recommend starting with the Getting Started section. Experienced users may want to jump directly to specific topics using the navigation menu.

Each section includes practical examples based on real-world use cases and actual implementations of the Axiom framework.

Let's begin your journey with Axiom Business Rules!

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
    <artifactId>axiom-rules</artifactId>
    <version>1.0.1</version>
</dependency>

<!-- For Spring Boot integration (optional) -->
<dependency>
    <groupId>com.lyxtera</groupId>
    <artifactId>axiom-spring-boot-starter</artifactId>
    <version>1.0.1</version>
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
    implementation 'com.lyxtera:axiom-rules:1.0.1'
    // For Spring Boot integration (optional)
    implementation 'com.lyxtera:axiom-spring-boot-starter:1.0.1'
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
        Double thresholdValue = threshold.asNumber().doubleValue();
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
        Double discountPercent = percent.asNumber().doubleValue();
        
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
        RuleExecutionResult<OrderContextKey> result = discountOrchestrator.executeAllMatchingRules(context);
        
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
