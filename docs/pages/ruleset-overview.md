# Rule Sets Overview

Rule Sets are collections of related business rules organized for a specific purpose. This section explains how rule sets are structured, loaded, and managed in the Axiom framework.

## What is a Rule Set?

A Rule Set (`RuleSet<K>`) in Axiom is a container for business rules that:

- Groups related rules together
- Maintains rule priority ordering
- Handles effective date filtering
- Provides metadata about the contained rules

Rule Sets are typically loaded from YAML files that define the ruleset name, description, business checks, business actions, and the rules themselves.

## Key Responsibilities of a Rule Set

### Rule Storage

The Rule Set maintains a collection of business rules, each with its own priority and effective date range. This collection is managed internally and can be accessed via methods like `getRulesInPriorityOrder()`.

### Rule Ordering

Rules are ordered by priority, with lower numbers indicating higher priority. For example, a rule with priority 10 will be considered before a rule with priority 20. This ordering is maintained internally by the Rule Set.

```java
// The rule with priority 10 will be evaluated before the rule with priority 20
ruleSet.addRule(highPriorityRule, 10, effectiveFrom);
ruleSet.addRule(lowerPriorityRule, 20, effectiveFrom);
```

### Effective Date Filtering

Rule Sets filter rules based on their effective date ranges. This allows rules to be time-bound, activating and deactivating automatically based on the current date. 

```java
// This rule is only effective from January 1, 2023 to December 31, 2023
ZonedDateTime effectiveFrom = ZonedDateTime.parse("2023-01-01T00:00:00Z");
ZonedDateTime effectiveTo = ZonedDateTime.parse("2023-12-31T23:59:59Z");
ruleSet.addRule(seasonalRule, 30, effectiveFrom, effectiveTo);

// This rule is effective from January 1, 2023 with no end date
ruleSet.addRule(permanentRule, 40, effectiveFrom);
```

### Rule Selection

Rule Sets provide methods to retrieve rules for evaluation based on their priority and effective date range.

```java
// Get all rules that are currently effective, in priority order
List<BusinessRule<MyContextKey>> activeRules = ruleSet.getRulesInPriorityOrder();
```

## Creating a Rule Set

Rule Sets are typically created by a `RuleSetLoader`, but can also be created programmatically:

```java
// Create an empty rule set
RuleSet<MyContextKey> ruleSet = new RuleSet<>();

// Add a rule to the rule set
BusinessRule<MyContextKey> rule = createRule();
ruleSet.addRule(rule, 10, ZonedDateTime.parse("2023-01-01T00:00:00Z"));

// Add a rule with both effective from and to dates
BusinessRule<MyContextKey> anotherRule = createAnotherRule();
ruleSet.addRule(
    anotherRule, 
    20, 
    ZonedDateTime.parse("2023-01-01T00:00:00Z"),
    ZonedDateTime.parse("2023-12-31T23:59:59Z")
);
```

## Rule Set Loaders

The more common way to create Rule Sets is using a `RuleSetLoader`, which loads rule definitions from an external source. The standard implementation is `YamlRuleSetLoader`:

```java
// Create a loader for a YAML file
RuleSetLoader<MyContextKey> loader = 
    new YamlRuleSetLoader<>("order_approval_rules.yaml");

// Load the rule set
RuleSet<MyContextKey> ruleSet = loader.loadRuleSet();
```

The `YamlRuleSetLoader` is the standard implementation, but you can create custom loaders for other sources like databases, remote APIs, or other file formats by implementing the `RuleSetLoader` interface.

When a rule uses `onMatchForwardTo`, the loader resolves that child ruleset during initialization, validates it like any other ruleset, and rejects cyclic forwarding chains.

## Rule Set YAML Format

Rule Sets are typically defined in YAML files, which provide a human-readable format for expressing rules. Here's an example of a complete ruleset YAML file:

```yaml
rulesetName: "Fraud Detection Ruleset"
rulesetDescription: "Rules for detecting fraudulent transactions"

businessChecks:
  - name: hasFraudSignals
    description: Determines if the transaction contains signals that indicate potential fraudulent activity
  - name: hasRiskScore
    description: Checks if the risk score is above a specified threshold
    params:
      - threshold
  - name: isNewCustomer
    description: Checks if the customer is new (less than 30 days)

businessActions:
  - name: blockTransaction
    description: Blocks the transaction entirely
  - name: flagForReview
    description: Flags the transaction for manual review
  - name: addVerificationStep
    description: Adds an additional verification step to the transaction process

rules:
  - name: "Known Fraud Signals Rule"
    description: "Block transactions with known fraud signals"
    expression: hasFraudSignals() then blockTransaction()
    priority: 10  # Highest priority
    effectiveFrom: "2023-01-01T00:00:00Z"
    
  - name: "High Risk Score Rule"
    description: "Flag transactions with very high risk scores for review"
    expression: hasRiskScore(90) then flagForReview()
    priority: 20
    effectiveFrom: "2023-01-01T00:00:00Z"
    
  - name: "New Customer with High Value Rule"
    description: "Add verification for new customers with large transactions"
    expression: isNewCustomer() and hasTransactionAmount(1000) then addVerificationStep()
    priority: 30
    effectiveFrom: "2023-01-01T00:00:00Z"
    effectiveTo: "2023-12-31T23:59:59Z"  # This rule expires at the end of 2023
```

The YAML file consists of:

1. **Metadata**: Rule set name and description
2. **Business Checks**: Definitions of the checks that can be used in rule expressions
3. **Business Actions**: Definitions of the actions that can be performed when rules match
4. **Rules**: Definitions of the actual business rules, including their expressions, priorities, and effective dates

Rules can be mixed within the same flat `rules` list:

- Action rule: `expression` contains `then` and executes actions directly
- Gate rule: `expression` is condition-only and forwards into a child ruleset through `onMatchForwardTo`

## Rule Set Metadata

Rule Sets contain metadata that describes the contents of the rule set, including the business checks, business actions, and rules. This metadata is useful for documentation, validation, and introspection.

```java
// Access the metadata of a rule set
RuleSet.Metadata metadata = ruleSet.getMetadata();

// Get the name and description of the rule set
String name = metadata.getRuleSetName();
String description = metadata.getRuleSetDescription();

// Get information about a business check
BusinessCheckDescriptor checkDescriptor = metadata.getBusinessCheckDescriptor("hasRiskScore");
if (checkDescriptor != null) {
    String checkDescription = checkDescriptor.getDescription();
    List<String> paramNames = checkDescriptor.getParams();
}

// Get information about a business action
BusinessActionDescriptor actionDescriptor = metadata.getBusinessActionDescriptor("blockTransaction");
if (actionDescriptor != null) {
    String actionDescription = actionDescriptor.getDescription();
}
```

## Integration with Rule Orchestrator

Rule Sets are typically used with a Rule Orchestrator, which handles the evaluation of rules against a given context:

```java
// Create a rule orchestrator for a rule set
RuleOrchestrator<MyContextKey> orchestrator = new RuleOrchestrator<>(ruleSet);

// Create a context for rule evaluation
RuleContext<MyContextKey> context = new RuleContext<>(MyContextKey.class);
context.add(MyContextKey.TRANSACTION_AMOUNT, 5000.0);
context.add(MyContextKey.RISK_SCORE, 95);

// Execute the first matching rule
RuleExecutionResult<MyContextKey> result = orchestrator.executeFirstMatchingRule(context);
if (result.hasMatch()) {
    BusinessRule<MyContextKey> matchedRule = result.getMatchedRule();
    System.out.println("Rule applied: " + matchedRule.getName());
}
```

## Best Practices

1. **Organize Rules by Domain**: Create separate rule sets for different domains or aspects of your application.

2. **Use Clear Priorities**: Assign clear priorities to rules, with lower numbers for more important rules.

3. **Leverage Effective Dates**: Use effective date ranges to automatically activate and deactivate rules based on time.

4. **Provide Clear Metadata**: Include clear descriptions for your rule set, business checks, business actions, and rules.

5. **Validate Rule Sets**: Validate rule sets at load time to ensure they are well-formed and reference valid business checks and actions.

6. **Keep Rule Sets Focused**: Each rule set should have a clear, focused purpose. Avoid creating "catch-all" rule sets.

7. **Consider Versioning**: If you need to maintain multiple versions of rules, consider using separate rule set files or effective dates.

## Related Sections

- [Rule Set Minimal Structure](ruleset-structure.md) - Details on the minimal structure required for rule sets
- [Rule Set Validations](ruleset-validation.md) - Information about validating rule sets
- [Rule Context Overview](rule-context-overview.md) - How rule contexts are used with rule sets
- [Rule Orchestrators Overview](rule-orchestrator-overview.md) - How rule orchestrators use rule sets to evaluate rules
