# Rule Orchestrators Overview

The `RuleOrchestrator` is a central component in the Axiom framework that coordinates the evaluation and execution of business rules. It serves as the main entry point for rule execution and provides methods to apply rules from a rule set against a given context.

## Key Responsibilities

The `RuleOrchestrator` has several key responsibilities:

1. **Rule Execution**: Evaluates rule conditions and executes rule actions
2. **Rule Selection**: Determines which rules match a given context
3. **Result Handling**: Provides detailed results of rule execution
4. **Execution Strategies**: Supports different execution strategies (first match, all matches)

## Creating a Rule Orchestrator

A `RuleOrchestrator` is created with a reference to a `RuleSet`:

```java
// Create a rule orchestrator for a rule set
RuleOrchestrator<MyContextKey> orchestrator = new RuleOrchestrator<>(ruleSet);
```

In a dependency injection environment like Guice, you would typically inject the orchestrator:

```java
@Inject
@Named("fraud_detection_ruleset")
private RuleOrchestrator<MyContextKey> fraudDetectionOrchestrator;
```

## Execution Methods

The `RuleOrchestrator` provides several methods for executing rules:

### Execute First Match

Executes the first matching rule in priority order:

```java
// Create a context for rule evaluation
RuleContext<MyContextKey> context = new RuleContext<>(MyContextKey.class);
context.add(MyContextKey.TRANSACTION_AMOUNT, 5000.0);
context.add(MyContextKey.RISK_SCORE, 95);

// Execute the first matching rule
RuleExecutionResult<MyContextKey> result = orchestrator.executeFirstMatch(context);
if (result.hasMatch()) {
    BusinessRule<MyContextKey> matchedRule = result.getMatchedRule();
    System.out.println("Rule applied: " + matchedRule.getName());
}
```

### Execute All Matches

Executes all matching rules in priority order:

```java
// Execute all matching rules
RuleExecutionResult<MyContextKey> result = orchestrator.executeAllMatches(context);
if (result.hasMatches()) {
    List<BusinessRule<MyContextKey>> matchedRules = result.getMatchedRules();
    System.out.println("Number of rules applied: " + matchedRules.size());
    
    // Print the names of all matched rules
    matchedRules.forEach(rule -> System.out.println("Rule applied: " + rule.getName()));
}
```

### Get Matching Rules

Retrieves all matching rules without executing their actions:

```java
// Get all matching rules without executing them
List<BusinessRule<MyContextKey>> matchingRules = orchestrator.getMatchingRules(context);
System.out.println("Number of matching rules: " + matchingRules.size());
```

### Get First Matching Rule

Retrieves the first matching rule without executing its actions:

```java
// Get the first matching rule without executing it
Optional<BusinessRule<MyContextKey>> firstMatchingRule = orchestrator.getFirstMatchingRule(context);
if (firstMatchingRule.isPresent()) {
    System.out.println("First matching rule: " + firstMatchingRule.get().getName());
}
```

## Working with Rule Execution Results

The `RuleExecutionResult` class provides detailed information about the execution of rules:

```java
RuleExecutionResult<MyContextKey> result = orchestrator.executeFirstMatch(context);

// Check if any rule matched
boolean hasMatch = result.hasMatch();

// Get the matched rule (for executeFirstMatch)
BusinessRule<MyContextKey> matchedRule = result.getMatchedRule();

// Get all matched rules (for executeAllMatches)
List<BusinessRule<MyContextKey>> matchedRules = result.getMatchedRules();

// Get the execution context (which may have been modified by rule actions)
RuleContext<MyContextKey> resultContext = result.getContext();
```

## Integration with Dependency Injection

The `RuleOrchestrator` is typically integrated with a dependency injection framework like Guice using the `AxiomModule`:

```java
public class MyAxiomModule extends AxiomModule<MyContextKey> {
    
    @Override
    protected Map<String, RuleSetLoader<MyContextKey>> getRegisteredLoaders() {
        Map<String, RuleSetLoader<MyContextKey>> loaders = new HashMap<>();
        loaders.put("fraud_detection", new YamlRuleSetLoader<>("fraud_detection_ruleset.yaml"));
        loaders.put("high_value_approval", new YamlRuleSetLoader<>("high_value_approval_ruleset.yaml"));
        return loaders;
    }
    
    @Override
    protected void configureBusinessRules(
            MapBinder<String, BusinessCheck<MyContextKey>> checks,
            MapBinder<String, BusinessAction<MyContextKey>> actions) {
        // Register business checks
        checks.addBinding("hasFraudSignals").to(HasFraudSignalsCheck.class);
        checks.addBinding("hasRiskScore").to(HasRiskScoreCheck.class);
        
        // Register business actions
        actions.addBinding("blockTransaction").to(BlockTransactionAction.class);
        actions.addBinding("flagForReview").to(FlagForReviewAction.class);
    }
}
```

Then, in your application code, you can inject the orchestrators:

```java
@Inject
@Named("fraud_detection")
private RuleOrchestrator<MyContextKey> fraudDetectionOrchestrator;

@Inject
@Named("high_value_approval")
private RuleOrchestrator<MyContextKey> highValueApprovalOrchestrator;
```

## Best Practices

1. **Use Named Orchestrators**: When working with multiple rule sets, use named orchestrators to clearly identify which rule set is being used.

2. **Handle No Matches**: Always check if a rule matched before accessing the matched rule to avoid `NoSuchElementException`.

3. **Consider Context Modifications**: Remember that rule actions can modify the context, so the context after rule execution may be different from the input context.

4. **Choose the Right Execution Strategy**: Use `executeFirstMatch` when you want only one rule to apply, and `executeAllMatches` when multiple rules should apply.

5. **Separate Orchestrators by Domain**: Create separate orchestrators for different domains or aspects of your application to keep rule execution focused.

## Related Sections

- [Rule Orchestrator Injection](rule-orchestrator-injection.md) - How to inject rule orchestrators into your application
- [Rule Orchestrator Operations](rule-orchestrator-operations.md) - Detailed information about rule orchestrator operations
- [Rule Sets Overview](ruleset-overview.md) - How rule sets are used with rule orchestrators
- [Rule Context Overview](rule-context-overview.md) - How rule contexts are used with rule orchestrators

[← Back to Previous Section](ruleset-structure.md) 
