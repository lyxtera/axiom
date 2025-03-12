# Rule Orchestrator Operations

Rule orchestrators are the core execution components in Axiom. This document explains the various operations available on rule orchestrators and how to use them effectively.

## Rule Orchestrator Overview

A rule orchestrator is responsible for:

1. Managing a specific rule set
2. Executing rules against a context
3. Applying rule priority and effective date filtering
4. Returning execution results

Each rule set is associated with a dedicated rule orchestrator, which is automatically created and registered in your dependency injection container.

## Key Methods

Rule orchestrators provide several execution methods to meet different needs:

### executeAllMatches

```java
RuleExecutionResult<T> executeAllMatches(RuleContext<T> context);
```

This method:
- Evaluates all rules in the rule set
- Executes actions for all rules whose conditions match
- Returns a result containing all matched rules

Use this when:
- You want to apply all matching rules (e.g., applying multiple discounts)
- You need a cumulative effect of multiple rules

Example:

```java
// Create and populate context
RuleContext<OrderContextKey> context = new RuleContext<>(OrderContextKey.class);
context.add(OrderContextKey.ORDER_ID, "ORD-12345");
context.add(OrderContextKey.ORDER_AMOUNT, 150.0);
context.add(OrderContextKey.CUSTOMER_TYPE, "PREMIUM");

// Execute all matching rules
RuleExecutionResult<OrderContextKey> result = discountOrchestrator.executeAllMatches(context);

// Get the discounted amount after all rules applied
if (result.hasMatches()) {
    Double finalAmount = context.getRequired(OrderContextKey.ORDER_AMOUNT, Double.class);
    System.out.println("Final amount after all discounts: " + finalAmount);
}
```

### executeFirstMatchingRule

```java
RuleExecutionResult<T> executeFirstMatchingRule(RuleContext<T> context);
```

This method:
- Evaluates rules in priority order
- Stops after finding and executing the first matching rule
- Returns a result containing only the first matched rule

Use this when:
- You want a single rule to be applied (e.g., choose one discount only)
- You need an exclusive behavior (only one rule should win)

Example:

```java
// Create and populate context
RuleContext<UserContextKey> context = new RuleContext<>(UserContextKey.class);
context.add(UserContextKey.USER_ID, "USR-123");
context.add(UserContextKey.IP_ADDRESS, "203.0.113.42");
context.add(UserContextKey.LOGIN_ATTEMPTS, 5);

// Execute first matching rule
RuleExecutionResult<UserContextKey> result = fraudDetectionOrchestrator.executeFirstMatchingRule(context);

// Handle the result
if (result.hasMatches()) {
    Rule<UserContextKey> matchedRule = result.getFirstMatchedRule().get();
    System.out.println("Fraud rule matched: " + matchedRule.getName());
    
    // Take action based on the matched rule
    Boolean blockUser = context.getOptional(UserContextKey.BLOCK_USER, Boolean.class).orElse(false);
    if (blockUser) {
        userService.blockUser(context.getRequired(UserContextKey.USER_ID, String.class));
    }
}
```

### executeAllByPriority

```java
RuleExecutionResult<T> executeAllByPriority(RuleContext<T> context);
```

This method:
- Evaluates rules in priority order
- Executes all matching rules, but maintains priority order
- Returns a result with all matched rules in priority order

Use this when:
- You want to apply multiple rules but need to ensure they're applied in a specific order
- The order of operations matters (e.g., apply base discounts before promotional ones)

Example:

```java
// Create and populate context
RuleContext<DocumentContextKey> context = new RuleContext<>(DocumentContextKey.class);
context.add(DocumentContextKey.DOCUMENT_ID, "DOC-456");
context.add(DocumentContextKey.CONTENT, documentContent);
context.add(DocumentContextKey.METADATA, metadata);

// Execute all rules in priority order
RuleExecutionResult<DocumentContextKey> result = documentProcessingOrchestrator.executeAllByPriority(context);

// Process the results in the order they were applied
if (result.hasMatches()) {
    List<Rule<DocumentContextKey>> matchedRules = result.getMatchedRules();
    System.out.println("Applied processing rules in this order:");
    for (Rule<DocumentContextKey> rule : matchedRules) {
        System.out.println("- " + rule.getName() + " (priority: " + rule.getPriority() + ")");
    }
    
    // Get the processed document
    String processedContent = context.getRequired(DocumentContextKey.CONTENT, String.class);
    documentRepository.save(processedContent);
}
```

### evaluateConditions

```java
List<Rule<T>> evaluateConditions(RuleContext<T> context);
```

This method:
- Only evaluates the conditions of rules, without executing any actions
- Returns a list of rules whose conditions match
- Does not modify the context

Use this when:
- You want to preview which rules would match but don't want any actions to be executed
- You need to make decisions based on which rules would match
- You're implementing a rule authoring UI that shows which rules would fire

Example:

```java
// Create and populate context
RuleContext<PolicyContextKey> context = new RuleContext<>(PolicyContextKey.class);
context.add(PolicyContextKey.POLICY_ID, "POL-789");
context.add(PolicyContextKey.COVERAGE_AMOUNT, 500000);
context.add(PolicyContextKey.CUSTOMER_AGE, 35);
context.add(PolicyContextKey.CUSTOMER_REGION, "WEST");

// Evaluate which rules would match
List<Rule<PolicyContextKey>> matchingRules = policyRulesOrchestrator.evaluateConditions(context);

// Show preview to user
System.out.println("The following rules would apply:");
for (Rule<PolicyContextKey> rule : matchingRules) {
    System.out.println("- " + rule.getName() + ": " + rule.getDescription());
}
```

## Working with RuleExecutionResult

The `RuleExecutionResult` class provides access to:

1. **Matched Rules**: The rules that matched and were executed
2. **Context**: The final rule context after execution
3. **Execution Statistics**: Data about the execution process

### Checking for Matches

```java
boolean hasMatches();
Optional<Rule<T>> getFirstMatchedRule();
List<Rule<T>> getMatchedRules();
```

These methods help you determine if any rules matched and which ones.

### Accessing the Context

```java
RuleContext<T> getContext();
```

This gives you access to the context after rule execution, which may have been modified by actions.

### Execution Statistics

```java
RuleExecutionStats getStats();
```

This provides execution statistics:

```java
// Get execution statistics
RuleExecutionStats stats = result.getStats();
System.out.println("Execution time: " + stats.getExecutionTimeMs() + "ms");
System.out.println("Rules evaluated: " + stats.getRulesEvaluated());
System.out.println("Actions executed: " + stats.getActionsExecuted());
```

## Advanced Operations

### Rule Filtering

You can filter rules before execution:

```java
// Create a custom rule filter
RuleFilter<OrderContextKey> highPriorityFilter = rule -> rule.getPriority() < 100;

// Apply the filter when executing
RuleExecutionResult<OrderContextKey> result = orderRulesOrchestrator
    .withFilter(highPriorityFilter)
    .executeAllMatches(context);
```

Common filter scenarios:

```java
// Filter by tag
RuleFilter<T> tagFilter = rule -> rule.getTags().contains("promotion");

// Filter by name pattern
RuleFilter<T> nameFilter = rule -> rule.getName().startsWith("Discount-");

// Composite filter
RuleFilter<T> compositeFilter = rule -> 
    rule.getPriority() < 50 && rule.getTags().contains("critical");
```

### Context Preprocessing

You can preprocess the context before rule execution:

```java
// Create a context preprocessor
ContextPreprocessor<OrderContextKey> currencyConverter = context -> {
    if (context.hasValue(OrderContextKey.CURRENCY) && 
        !context.getRequired(OrderContextKey.CURRENCY, String.class).equals("USD")) {
        
        // Convert currency to USD
        String currency = context.getRequired(OrderContextKey.CURRENCY, String.class);
        Double amount = context.getRequired(OrderContextKey.ORDER_AMOUNT, Double.class);
        Double convertedAmount = currencyService.convertToUSD(amount, currency);
        
        // Update context with converted amount
        context.add(OrderContextKey.ORDER_AMOUNT, convertedAmount);
        context.add(OrderContextKey.CURRENCY, "USD");
        context.add(OrderContextKey.ORIGINAL_CURRENCY, currency);
        context.add(OrderContextKey.ORIGINAL_AMOUNT, amount);
    }
    return context;
};

// Apply the preprocessor when executing
RuleExecutionResult<OrderContextKey> result = orderRulesOrchestrator
    .withContextPreprocessor(currencyConverter)
    .executeAllMatches(context);
```

### Custom Execution Listeners

You can add listeners to track rule execution:

```java
// Create an execution listener
RuleExecutionListener<OrderContextKey> loggingListener = new RuleExecutionListener<>() {
    @Override
    public void onRuleEvaluationStart(Rule<OrderContextKey> rule, RuleContext<OrderContextKey> context) {
        logger.debug("Evaluating rule: {}", rule.getName());
    }
    
    @Override
    public void onRuleMatched(Rule<OrderContextKey> rule, RuleContext<OrderContextKey> context) {
        logger.info("Rule matched: {}", rule.getName());
    }
    
    @Override
    public void onRuleActionExecuted(Rule<OrderContextKey> rule, RuleContext<OrderContextKey> context) {
        logger.info("Rule action executed: {}", rule.getName());
    }
    
    @Override
    public void onRuleEvaluationEnd(Rule<OrderContextKey> rule, boolean matched, RuleContext<OrderContextKey> context) {
        logger.debug("Rule evaluation completed: {} (matched: {})", rule.getName(), matched);
    }
};

// Apply the listener when executing
RuleExecutionResult<OrderContextKey> result = orderRulesOrchestrator
    .withExecutionListener(loggingListener)
    .executeAllMatches(context);
```

## Performance Considerations

### Context Reuse

When executing multiple rule sets, reuse the same context to avoid creating new objects:

```java
// Create a single context
RuleContext<OrderContextKey> context = new RuleContext<>(OrderContextKey.class);
context.add(OrderContextKey.ORDER_ID, orderId);
// ... add more data

// Execute multiple rule sets with the same context
RuleExecutionResult<OrderContextKey> fraudResult = 
    fraudDetectionOrchestrator.executeFirstMatchingRule(context);
    
if (!fraudResult.hasMatches()) {
    RuleExecutionResult<OrderContextKey> discountResult = 
        discountOrchestrator.executeAllMatches(context);
        
    RuleExecutionResult<OrderContextKey> shippingResult = 
        shippingOrchestrator.executeAllMatches(context);
}
```

### Parallel Execution

For independent rule sets, consider parallel execution:

```java
// Create executor service
ExecutorService executor = Executors.newFixedThreadPool(3);

// Execute rule sets in parallel
Future<RuleExecutionResult<OrderContextKey>> discountFuture = 
    executor.submit(() -> discountOrchestrator.executeAllMatches(context.copy()));
    
Future<RuleExecutionResult<OrderContextKey>> taxFuture = 
    executor.submit(() -> taxOrchestrator.executeAllMatches(context.copy()));
    
Future<RuleExecutionResult<OrderContextKey>> shippingFuture = 
    executor.submit(() -> shippingOrchestrator.executeAllMatches(context.copy()));

// Get results
RuleExecutionResult<OrderContextKey> discountResult = discountFuture.get();
RuleExecutionResult<OrderContextKey> taxResult = taxFuture.get();
RuleExecutionResult<OrderContextKey> shippingResult = shippingFuture.get();

// Merge contexts if needed
context.add(OrderContextKey.ORDER_AMOUNT, 
    discountResult.getContext().getRequired(OrderContextKey.ORDER_AMOUNT, Double.class));
context.add(OrderContextKey.TAX_AMOUNT, 
    taxResult.getContext().getRequired(OrderContextKey.TAX_AMOUNT, Double.class));
context.add(OrderContextKey.SHIPPING_AMOUNT, 
    shippingResult.getContext().getRequired(OrderContextKey.SHIPPING_AMOUNT, Double.class));

// Clean up
executor.shutdown();
```

## Best Practices

1. **Choose the Right Execution Method**: Select the execution method that matches your business requirements.

2. **Error Handling**: Always add proper error handling around rule execution:

   ```java
   try {
       RuleExecutionResult<T> result = orchestrator.executeAllMatches(context);
       // Process result
   } catch (RuleExecutionException e) {
       logger.error("Error executing rules: {}", e.getMessage(), e);
       // Implement fallback behavior
   }
   ```

3. **Context Management**: Be careful with context data, especially when sharing across different rule sets:

   ```java
   // Create a safe copy when needed
   RuleContext<T> safeCopy = context.copy();
   ```

4. **Performance Monitoring**: Track rule execution performance:

   ```java
   RuleExecutionStats stats = result.getStats();
   if (stats.getExecutionTimeMs() > 100) {
       logger.warn("Slow rule execution: {}ms for ruleset {}", 
           stats.getExecutionTimeMs(), orchestrator.getRuleSetName());
   }
   ```

5. **Logging and Auditing**: Implement proper logging for rule executions:

   ```java
   logger.info("Executed ruleset: {}, matches: {}, execution time: {}ms", 
       orchestrator.getRuleSetName(),
       result.getMatchedRules().size(),
       result.getStats().getExecutionTimeMs());
       
   // Log individual matches for audit purposes
   result.getMatchedRules().forEach(rule -> 
       logger.info("Rule matched: {}, priority: {}", rule.getName(), rule.getPriority()));
   ```

6. **Testing**: Write comprehensive tests for your orchestrator operations:

   ```java
   @Test
   public void testDiscountRules() {
       // Setup test context
       RuleContext<OrderContextKey> context = new RuleContext<>(OrderContextKey.class);
       context.add(OrderContextKey.CUSTOMER_TYPE, "PREMIUM");
       context.add(OrderContextKey.ORDER_AMOUNT, 200.0);
       
       // Execute rules
       RuleExecutionResult<OrderContextKey> result = discountOrchestrator.executeAllMatches(context);
       
       // Verify results
       assertTrue(result.hasMatches());
       assertEquals(2, result.getMatchedRules().size());
       assertEquals(180.0, context.getRequired(OrderContextKey.ORDER_AMOUNT, Double.class), 0.01);
   }
   ```

[← Back to Previous Section](ruleset-structure.md) 
