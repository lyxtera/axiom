# Rules Overview

Rules are the core building blocks of the Axiom framework. They define the conditions under which business actions should be performed, forming the foundation of your business logic.

## Anatomy of a Rule

A business rule in Axiom consists of:

1. **Name**: A unique identifier for the rule
2. **Description**: A human-readable explanation of the rule's purpose
3. **Expression**: A condition and action pair that defines when and what to execute
4. **Priority**: A numeric value determining evaluation order (lower values = higher priority)
5. **Effective Dates**: Optional time boundaries for when the rule is active

In code, a rule is represented by the `BusinessRule<K>` class:

```java
public class BusinessRule<K extends Enum<K>> {
    private final String name;
    private final Condition<K> condition;
    private final List<RuleFunction<K>> actions;
    
    // Constructor and methods...
    
    public boolean evaluate(RuleContext<K> context) {
        if (condition == null || TRUE.equals(condition.evaluate(context))) {
            for (RuleFunction<K> action : actions) {
                action.execute(context);
            }
            return true;
        }
        return false;
    }
}
```

## Rule Expression Syntax

Rule expressions in YAML follow this pattern:

```
<condition> then <action>
```

Where:
- `<condition>` is a boolean expression that determines if the rule matches
- `then` is the keyword separating condition from action
- `<action>` is the business action to execute when the condition is true

### Condition Syntax

Conditions can be:

1. **Simple checks**: `hasRiskScore(90)`
2. **Negated checks**: `not hasFraudSignals()`
3. **Compound expressions**:
   - AND: `isHighValueOrder(1000) and isNewCustomer()`
   - OR: `hasFraudSignals() or hasRiskScore(95)`
4. **Complex expressions with parentheses**:
   - `isEnterpriseCustomer() and (hasHighRiskScore(90) or hasRecentFraudActivity())`

### Examples of Rule Expressions

```yaml
# Simple rule
isHighValueOrder(1000) then applyDiscount(10)

# Negated condition
not hasDiscountAlready() then applyDiscount(5)

# Compound condition with AND
isRepeatCustomer() and isPremiumMember() then applyDiscount(15)

# Compound condition with OR
hasFraudSignals() or hasRiskScore(95) then blockTransaction()

# Complex condition with parentheses
isHighValueOrder(5000) and (not hasApproval() or isNewCustomer()) then requireManualReview()
```

## Rule Definition in YAML

Here's how rules are defined in a YAML rule set file:

```yaml
rules:
  - name: "High Value Order Discount"
    description: "Apply 10% discount to orders over $1000"
    expression: isHighValueOrder(1000) then applyDiscount(10)
    priority: 10
    effectiveFrom: "2023-01-01T00:00:00Z"
    
  - name: "Repeat Customer Discount"
    description: "Apply 5% discount to repeat customers"
    expression: isRepeatCustomer() then applyDiscount(5)
    priority: 20
    effectiveFrom: "2023-01-01T00:00:00Z"
    effectiveTo: "2023-12-31T23:59:59Z"  # This rule expires at the end of 2023
    
  - name: "Premium Membership Discount"
    description: "Apply 15% discount to premium members"
    expression: isPremiumMember() then applyDiscount(15)
    priority: 15
    effectiveFrom: "2023-03-01T00:00:00Z"
```

## Rule Evaluation Process

When a context is evaluated against a rule set, the following process occurs:

1. Rules are filtered by their effective dates, keeping only currently active rules
2. Remaining rules are sorted by priority (lower number = higher priority)
3. For each rule (in priority order):
   a. The condition is evaluated against the context
   b. If the condition is true, the rule's actions are executed
   c. For `executeFirstMatchingRule()`, processing stops after the first match
   d. For `executeAllMatchingRules()`, all matching rules are processed

Here's a sequence diagram illustrating the rule evaluation process:

```
┌─────────┐          ┌────────────────┐          ┌────────┐          ┌───────────────┐
│ Service │          │ RuleOrchestrator│          │ RuleSet│          │ BusinessRule  │
└────┬────┘          └───────┬─────────┘          └───┬────┘          └───────┬───────┘
     │                       │                        │                       │
     │ executeFirstMatchingRule(ctx)│                        │                       │
     │──────────────────────>│                        │                       │
     │                       │                        │                       │
     │                       │ getRulesInPriorityOrder│                       │
     │                       │───────────────────────>│                       │
     │                       │                        │                       │
     │                       │ [filtered active rules]│                       │
     │                       │<───────────────────────│                       │
     │                       │                        │                       │
     │                       │                  ┌─────┴────┐                  │
     │                       │                  │ Sort by  │                  │
     │                       │                  │ priority │                  │
     │                       │                  └─────┬────┘                  │
     │                       │                        │                       │
     │                       │                  ┌─────┴────┐                  │
     │                       │                  │For each  │                  │
     │                       │                  │  rule    │                  │
     │                       │                  └─────┬────┘                  │
     │                       │                        │                       │
     │                       │                        │    evaluate(context)  │
     │                       │                        │────────────────────────>
     │                       │                        │                       │
     │                       │                        │    [true/false]       │
     │                       │                        │<───────────────────────
     │                       │                        │                       │
     │                       │                  ┌─────┴────┐                  │
     │                       │                  │If matched│                  │
     │                       │                  │break loop│                  │
     │                       │                  └─────┬────┘                  │
     │                       │                        │                       │
     │ RuleExecutionResult   │                        │                       │
     │<──────────────────────│                        │                       │
     │                       │                        │                       │
```

## Advanced Rule Patterns

### Parameter-Based Rules

Rules can accept parameters to make them more flexible:

```yaml
# A rule with a configurable threshold
- name: "Dynamic Risk Threshold Rule"
  description: "Block transactions above a configurable risk threshold"
  expression: hasRiskScore($RISK_THRESHOLD) then blockTransaction()
  priority: 5
  effectiveFrom: "2023-01-01T00:00:00Z"
```

The parameter `$RISK_THRESHOLD` can be provided at runtime.

### Multi-Action Rules

Rules can perform multiple actions:

```yaml
# A rule that performs multiple actions
- name: "Suspicious Transaction Rule"
  description: "Flag and log suspicious transactions for review"
  expression: isSuspiciousTransaction() then flagForReview() and logTransaction()
  priority: 30
  effectiveFrom: "2023-01-01T00:00:00Z"
```

### Rules with Complex Conditions

You can create complex conditions using AND, OR, NOT, and parentheses:

```yaml
# A rule with a complex condition
- name: "Complex Approval Rule"
  description: "Require approval for high-value orders from new customers in high-risk regions"
  expression: >
    isHighValueOrder(5000) and 
    (isNewCustomer() or 
     (isFromHighRiskRegion() and not hasApprovedKYC()))
    then requireManualApproval()
  priority: 5
  effectiveFrom: "2023-01-01T00:00:00Z"
```

## Working with Rule Results

When rules are executed, they produce a `RuleExecutionResult` that contains information about which rules matched and the final state of the context:

```java
// Execute all matching rules
RuleExecutionResult<MyContextKey> result = orchestrator.executeAllMatchingRules(context);

// Check if any rules matched
if (result.hasMatches()) {
    // Get the matched rules
    List<BusinessRule<MyContextKey>> matchedRules = result.getMatchedRules();
    
    // Get the names of matched rules
    List<String> matchedRuleNames = matchedRules.stream()
                                     .map(BusinessRule::getName)
                                     .collect(Collectors.toList());
    
    // Log the matched rule names
    System.out.println("Matched rules: " + String.join(", ", matchedRuleNames));
    
    // Get the context after rule execution (which may have been modified by rule actions)
    RuleContext<MyContextKey> resultContext = result.getContext();
    
    // Check if a specific action was performed
    if (resultContext.get(MyContextKey.REQUEST_BLOCKED, Boolean.class).orElse(false)) {
        System.out.println("Request was blocked by a rule");
    }
}
```

## Rule Performance Considerations

1. **Rule Priority**: Carefully assign priorities to ensure the most frequently matching rules have higher priority (lower numbers) to reduce the average number of rule evaluations.

2. **Condition Complexity**: Complex conditions with many subconditions can impact performance. Consider breaking complex rules into multiple simpler rules when possible.

3. **Rule Count**: The number of rules in a rule set directly impacts evaluation time. Keep rule sets focused and consider using multiple specialized rule sets instead of one large generic rule set.

4. **Context Size**: Large contexts with many values can impact serialization/deserialization performance. Include only necessary data in the context.

## Related Sections

- [Rule Priority](rule-priority.md) - Learn more about rule priority and evaluation order
- [Rule Effective Dates](rule-effective-dates.md) - Understanding rule activation and expiration
- [Business Actions & Checks Overview](business-components-overview.md) - Details on the components used in rule expressions
- [Rule Testing](rule-testing.md) - How to test rules effectively

[← Back to Rule Set Validations](ruleset-validation.md) 
