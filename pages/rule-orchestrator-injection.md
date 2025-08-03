# Rule Orchestrator Injection

Integrating rule orchestrators into your application requires proper dependency injection. This document covers how to inject and use rule orchestrators in different application components.

## Understanding Rule Orchestrator Injection

Rule orchestrators are the entry points for executing rules in your application. They're created automatically by the Axiom module based on your rule set configurations. To use them, you need to:

1. Configure the Axiom module with your rule sets
2. Inject the appropriate rule orchestrators into your application components
3. Invoke the orchestrators at the right points in your business logic

## Basic Injection Pattern

The most common pattern is to inject a rule orchestrator directly into a service class:

```java
public class OrderProcessingService {
    
    private final RuleOrchestrator<OrderContextKey> discountRuleOrchestrator;
    
    @Inject
    public OrderProcessingService(
            @Named("order_discounts") RuleOrchestrator<OrderContextKey> discountRuleOrchestrator) {
        this.discountRuleOrchestrator = discountRuleOrchestrator;
    }
    
    public Order processOrder(Order order) {
        // Create context
        RuleContext<OrderContextKey> context = createOrderContext(order);
        
        // Execute rules
        RuleExecutionResult<OrderContextKey> result = 
            discountRuleOrchestrator.executeAllMatchingRules(context);
        
        // Process results
        if (result.hasMatches()) {
            // Extract data from the context and update the order
            updateOrderWithDiscounts(order, result.getContext());
        }
        
        return order;
    }
    
    private RuleContext<OrderContextKey> createOrderContext(Order order) {
        // Create and populate context
        RuleContext<OrderContextKey> context = new RuleContext<>(OrderContextKey.class);
        context.add(OrderContextKey.ORDER_ID, order.getId());
        context.add(OrderContextKey.CUSTOMER_ID, order.getCustomerId());
        context.add(OrderContextKey.ORDER_AMOUNT, order.getTotalAmount());
        // ... add more data as needed
        return context;
    }
    
    private void updateOrderWithDiscounts(Order order, RuleContext<OrderContextKey> context) {
        // Get updated data from context
        Double discountedAmount = context.getRequired(OrderContextKey.ORDER_AMOUNT, Double.class);
        
        // Update order
        order.setTotalAmount(discountedAmount);
        
        // Check for additional data that might have been added by rules
        context.getOptional(OrderContextKey.DISCOUNT_REASON, String.class)
               .ifPresent(order::setDiscountReason);
    }
}
```

## Named Orchestrators

Each rule set is exposed as a named rule orchestrator. The name comes from the key used when registering the rule set loader:

```java
// In your Axiom module configuration
AxiomModule.buildForKey(OrderContextKey.class)
    .withRuleLoaders(loaders -> loaders
        .loader("order_discounts", new YamlRuleSetLoader<>("discount_rules.yaml"))
        .loader("fraud_detection", new YamlRuleSetLoader<>("fraud_rules.yaml"))
    )
    // ... other configuration
    .build();
```

This creates two named rule orchestrators that can be injected using the `@Named` annotation:

```java
@Inject
@Named("order_discounts")
private RuleOrchestrator<OrderContextKey> discountOrchestrator;

@Inject
@Named("fraud_detection")
private RuleOrchestrator<OrderContextKey> fraudDetectionOrchestrator;
```

## Injecting Multiple Orchestrators

Sometimes you need to inject multiple orchestrators into a service:

```java
public class OrderService {
    
    private final RuleOrchestrator<OrderContextKey> discountOrchestrator;
    private final RuleOrchestrator<OrderContextKey> fraudDetectionOrchestrator;
    private final RuleOrchestrator<OrderContextKey> shippingRulesOrchestrator;
    
    @Inject
    public OrderService(
            @Named("order_discounts") RuleOrchestrator<OrderContextKey> discountOrchestrator,
            @Named("fraud_detection") RuleOrchestrator<OrderContextKey> fraudDetectionOrchestrator,
            @Named("shipping_rules") RuleOrchestrator<OrderContextKey> shippingRulesOrchestrator) {
        this.discountOrchestrator = discountOrchestrator;
        this.fraudDetectionOrchestrator = fraudDetectionOrchestrator;
        this.shippingRulesOrchestrator = shippingRulesOrchestrator;
    }
    
    public OrderProcessingResult processOrder(Order order) {
        // Create a shared context
        RuleContext<OrderContextKey> context = createOrderContext(order);
        
        // First check for fraud
        RuleExecutionResult<OrderContextKey> fraudResult = 
            fraudDetectionOrchestrator.executeFirstMatchingRuleingRule(context);
        
        // If fraud detected, reject the order
        if (fraudResult.hasMatches()) {
            return OrderProcessingResult.rejected(
                context.getOptional(OrderContextKey.REJECTION_REASON, String.class)
                      .orElse("Potential fraud detected"));
        }
        
        // Apply discounts
        RuleExecutionResult<OrderContextKey> discountResult = 
            discountOrchestrator.executeAllMatchingRules(context);
        
        // Apply shipping rules
        RuleExecutionResult<OrderContextKey> shippingResult = 
            shippingRulesOrchestrator.executeAllMatchingRules(context);
        
        // Update order with all rule results
        updateOrderFromRuleResults(order, context);
        
        return OrderProcessingResult.approved(order);
    }
    
    // Helper methods...
}
```

## Injecting All Orchestrators

Sometimes you might want to inject all orchestrators as a map:

```java
public class RuleExecutionService {
    
    private final Map<String, RuleOrchestrator<OrderContextKey>> orchestrators;
    
    @Inject
    public RuleExecutionService(Map<String, RuleOrchestrator<OrderContextKey>> orchestrators) {
        this.orchestrators = orchestrators;
    }
    
    public RuleExecutionResult<OrderContextKey> executeRuleSet(
            String ruleSetName, RuleContext<OrderContextKey> context) {
        
        RuleOrchestrator<OrderContextKey> orchestrator = orchestrators.get(ruleSetName);
        if (orchestrator == null) {
            throw new IllegalArgumentException("No rule set found with name: " + ruleSetName);
        }
        
        return orchestrator.executeAllMatchingRules(context);
    }
}
```

To make this work, you need to configure your module to allow multibinding:

```java
@Override
protected void configure() {
    // Configure multibinding for rule orchestrators
    MapBinder<String, RuleOrchestrator<OrderContextKey>> orchestratorBinder = 
        MapBinder.newMapBinder(
            binder(), 
            new TypeLiteral<String>() {}, 
            new TypeLiteral<RuleOrchestrator<OrderContextKey>>() {}
        );
    
    // Your other configuration...
}
```

## Lazy Injection

For better performance, especially with many rule sets, you might want to use lazy injection:

```java
public class OrderService {
    
    private final Provider<RuleOrchestrator<OrderContextKey>> discountOrchestratorProvider;
    
    @Inject
    public OrderService(
            @Named("order_discounts") Provider<RuleOrchestrator<OrderContextKey>> discountOrchestratorProvider) {
        this.discountOrchestratorProvider = discountOrchestratorProvider;
    }
    
    public Order processOrder(Order order) {
        // Create context
        RuleContext<OrderContextKey> context = createOrderContext(order);
        
        // Get the orchestrator only when needed
        RuleOrchestrator<OrderContextKey> orchestrator = discountOrchestratorProvider.get();
        
        // Execute rules
        RuleExecutionResult<OrderContextKey> result = orchestrator.executeAllMatchingRules(context);
        
        // Process results...
        return order;
    }
}
```

## Rule Orchestrators in Spring Applications

If you're using Spring instead of Guice, you can still use Axiom. Here's how to configure rule orchestrators in a Spring context:

```java
@Configuration
public class AxiomConfig {
    
    @Bean
    public Module axiomModule() {
        return AxiomModule.buildForKey(OrderContextKey.class)
            .withRuleLoaders(loaders -> loaders
                .loader("order_discounts", new YamlRuleSetLoader<>("discount_rules.yaml"))
                .loader("fraud_detection", new YamlRuleSetLoader<>("fraud_rules.yaml"))
            )
            .withChecks(checks -> checks
                .check("isHighValueOrder", HighValueOrderCheck.class)
                // ... other checks
            )
            .withActions(actions -> actions
                .action("applyDiscount", ApplyDiscountAction.class)
                // ... other actions
            )
            .build();
    }
    
    @Bean
    public AbstractModule springIntegrationModule() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                // Empty, just for binding the injector
            }
        };
    }
    
    @Bean
    public Injector guiceInjector(Module axiomModule, AbstractModule springIntegrationModule) {
        return Guice.createInjector(axiomModule, springIntegrationModule);
    }
    
    @Bean
    @Qualifier("order_discounts")
    public RuleOrchestrator<OrderContextKey> discountOrchestrator(Injector injector) {
        return injector.getInstance(
            Key.get(
                new TypeLiteral<RuleOrchestrator<OrderContextKey>>() {}, 
                Names.named("order_discounts")
            )
        );
    }
    
    @Bean
    @Qualifier("fraud_detection")
    public RuleOrchestrator<OrderContextKey> fraudDetectionOrchestrator(Injector injector) {
        return injector.getInstance(
            Key.get(
                new TypeLiteral<RuleOrchestrator<OrderContextKey>>() {}, 
                Names.named("fraud_detection")
            )
        );
    }
}
```

Then in your Spring services:

```java
@Service
public class OrderService {
    
    private final RuleOrchestrator<OrderContextKey> discountOrchestrator;
    
    @Autowired
    public OrderService(
            @Qualifier("order_discounts") RuleOrchestrator<OrderContextKey> discountOrchestrator) {
        this.discountOrchestrator = discountOrchestrator;
    }
    
    // Service methods...
}
```

## Best Practices

1. **Name Consistently**: Use consistent naming for your rule sets and orchestrators.

2. **Single Responsibility**: Each orchestrator should handle a specific domain or function.

3. **Reuse Contexts**: When using multiple orchestrators in sequence, reuse the same context to accumulate results.

4. **Error Handling**: Add robust error handling around rule execution.

5. **Testing**: Mock orchestrators in unit tests to isolate service logic.

6. **Documentation**: Document the purpose and expected behavior of each orchestrator.

7. **Performance**: Be mindful of initialization costs, especially with many rule sets.

8. **Context Lifecycle**: Carefully manage the lifecycle of rule contexts to prevent memory leaks.

[← Back to Previous Section](ruleset-structure.md) 
