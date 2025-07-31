# Rule Priority

Rule priority is a critical aspect of the Axiom framework that determines the order in which rules are evaluated. This page explains how rule priorities work, how they affect rule execution, and best practices for setting priorities effectively.

## How Priority Works in Axiom

In Axiom, rule priority is expressed as a numeric value, where **lower values indicate higher priority**. For example, a rule with priority 10 is considered more important than a rule with priority 100 and will be evaluated first.

When a rule set is evaluated against a context, the rules are sorted by priority. This means:

1. Rules with the lowest priority numbers are evaluated first
2. When two rules have the same priority, their relative order is undefined

Here's how priorities are defined in a rule set YAML file:

```yaml
rules:
  - name: "Critical Security Rule"
    description: "Block requests with critical security violations"
    expression: hasCriticalSecurityViolation() then blockRequest()
    priority: 1  # Highest priority
    effectiveFrom: "2023-01-01T00:00:00Z"
    
  - name: "High Risk Score Rule" 
    description: "Block requests with very high risk scores"
    expression: hasRiskScore(90) then blockRequest()
    priority: 10  # Medium-high priority
    effectiveFrom: "2023-01-01T00:00:00Z"
    
  - name: "New Customer Additional Verification"
    description: "Add verification for new customers"
    expression: isNewCustomer() then requireAdditionalVerification()
    priority: 100  # Lower priority
    effectiveFrom: "2023-01-01T00:00:00Z"
```

## Priority in the Rule Execution Flow

When rules are executed using a `RuleOrchestrator`, the priority order affects the execution flow:

```java
// Get the rule set from the loader
RuleSetLoader<MyContextKey> loader = new YamlRuleSetLoader<>("risk_rules.yaml");
RuleSet<MyContextKey> ruleSet = loader.loadRuleSet();

// Create an orchestrator for the rule set
RuleOrchestrator<MyContextKey> orchestrator = new RuleOrchestrator<>(ruleSet);

// Create a context for rule evaluation
RuleContext<MyContextKey> context = new RuleContext<>(MyContextKey.class);
context.add(MyContextKey.RISK_SCORE, 95);
context.add(MyContextKey.IS_NEW_CUSTOMER, true);

// Execute the first matching rule
RuleExecutionResult<MyContextKey> result = orchestrator.executeFirstMatchingRule(context);
```

In this example:
1. The orchestrator retrieves all rules from the rule set
2. Rules are filtered by their effective dates (only currently active rules are considered)
3. Remaining rules are sorted by priority (lowest to highest number)
4. Rules are evaluated in priority order
5. With `executeFirstMatchingRule()`, once a rule matches (its condition evaluates to true), its actions are executed and no further rules are evaluated
6. With `executeAllMatchingRules()`, all matching rules are executed in priority order

## Code Example: Priority Order Handling

The following code demonstrates how rule priorities are handled in the `RuleSet` class:

```java
/**
 * Gets all rules in priority order (lowest numbers first)
 *
 * @return Unmodifiable list of rules in priority order
 */
public List<BusinessRule<K>> getRulesInPriorityOrder() {
    return rules.stream()
        .filter(PrioritizedRule::isEffectiveNow)
        .map(PrioritizedRule::getRule)
        .collect(Collectors.toUnmodifiableList());
}

/**
 * Adds a rule to the set with the specified priority and effective date range
 *
 * @param rule The rule to add
 * @param priority Lower values indicate higher priority (priority 1 is higher than priority 2)
 * @param effectiveFrom The date and time from which the rule is effective
 * @param effectiveTo The date and time until which the rule is effective (null for indefinite)
 * @throws RuleException if priority is less than 1
 */
void addRule(BusinessRule<K> rule, int priority, ZonedDateTime effectiveFrom, ZonedDateTime effectiveTo) {
    if (priority < 1) {
        throw RuleException.invalidPriority();
    }
    rules.add(new PrioritizedRule<>(rule, priority, effectiveFrom, effectiveTo));
    // Sort in ascending order (lowest priority number first = highest priority)
    Collections.sort(rules);
}
```

The `PrioritizedRule` inner class implements `Comparable` to enable sorting:

```java
private static class PrioritizedRule<K extends Enum<K>> implements Comparable<PrioritizedRule<K>> {
    private final BusinessRule<K> rule;
    private final int priority;
    private final ZonedDateTime effectiveFrom;
    private final ZonedDateTime effectiveTo;
    
    // Constructor...

    @Override
    public int compareTo(PrioritizedRule<K> other) {
        // Lower priority number means higher priority, so use natural ordering
        return Integer.compare(this.priority, other.priority);
    }
}
```

## When Rule Priority Matters Most

Rule priority is particularly important in several scenarios:

### 1. Exception Handling Rules

When you have general rules and specific exception cases, you want the exceptions to have higher priority:

```yaml
# Exception rule (higher priority)
- name: "VIP Customer Exception"
  description: "Skip fraud checks for VIP customers"
  expression: isVipCustomer() then skipFraudChecks()
  priority: 5
  effectiveFrom: "2023-01-01T00:00:00Z"

# General rule (lower priority)
- name: "Standard Fraud Check"
  description: "Apply standard fraud checks to all transactions"
  expression: isTransaction() then applyStandardFraudChecks()
  priority: 50
  effectiveFrom: "2023-01-01T00:00:00Z"
```

### 2. Rules with Overlapping Conditions

When multiple rules could match the same input, priority determines which one takes precedence:

```yaml
# More specific rule (higher priority)
- name: "High-Value International Transfer Rule"
  description: "Apply enhanced scrutiny to high-value international transfers"
  expression: isInternationalTransfer() and isHighValue(10000) then applyEnhancedScrutiny()
  priority: 10
  effectiveFrom: "2023-01-01T00:00:00Z"

# More general rule (lower priority)
- name: "International Transfer Rule"
  description: "Apply basic scrutiny to all international transfers"
  expression: isInternationalTransfer() then applyBasicScrutiny()
  priority: 20
  effectiveFrom: "2023-01-01T00:00:00Z"
```

### 3. Security vs. Business Rules

Security and compliance rules typically take precedence over business/operational rules:

```yaml
# Security rule (highest priority)
- name: "Security Block Rule"
  description: "Block requests with security threats"
  expression: hasSecurityThreat() then blockRequest()
  priority: 1
  effectiveFrom: "2023-01-01T00:00:00Z"

# Compliance rule (high priority)
- name: "Compliance Approval Rule"
  description: "Require compliance approval for high-risk transactions"
  expression: isHighRiskTransaction() then requireComplianceApproval()
  priority: 10
  effectiveFrom: "2023-01-01T00:00:00Z"

# Business rule (lower priority)
- name: "Discount Rule"
  description: "Apply discount for eligible transactions"
  expression: isEligibleForDiscount() then applyDiscount(10)
  priority: 100
  effectiveFrom: "2023-01-01T00:00:00Z"
```

## Advanced Priority Strategies

### 1. Priority Ranges by Rule Type

A common approach is to assign priority ranges based on rule categories:

| Priority Range | Rule Category           | Examples                                      |
|----------------|-------------------------|--------------------------------------------|
| 1-9            | Security & Fraud       | Blocking known attacks, critical security   |
| 10-99          | Compliance & Risk      | KYC checks, regulatory requirements         |
| 100-499        | Business Logic         | Approvals, workflows, business decisions    |
| 500-999        | Customer Experience    | Discounts, promotions, personalization      |
| 1000+          | Operational/Analytics  | Data collection, metrics, optimization      |

This approach makes it clear which types of rules take precedence and allows for easy insertion of new rules within a category.

### 2. Dynamic Priority Assignment

In some advanced scenarios, you may want to assign rule priorities dynamically:

```java
// Create a rule set with dynamically assigned priorities
RuleSet<MyContextKey> ruleSet = new RuleSet<>();

// Security rules have highest priority (1-9)
ruleSet.addRule(createSecurityRule("Block Known Attackers"), 
                1, ZonedDateTime.now());

// Compliance rules have next highest priority (10-99)
ruleSet.addRule(createComplianceRule("High Risk Country Check"), 
                10, ZonedDateTime.now());

// Business rules have lower priority (100+)
ruleSet.addRule(createBusinessRule("New Customer Experience"), 
                100, ZonedDateTime.now());

// The priority could come from configuration or database
Integer discountRulePriority = configService.getRulePriority("SeasonalDiscount");
ruleSet.addRule(createDiscountRule("Seasonal Discount"), 
                discountRulePriority, ZonedDateTime.now());
```

### 3. Staggered Priorities for Future Rules

Leave gaps in your priority numbering to accommodate future rules:

```yaml
rules:
  - name: "Critical Security Rule"
    priority: 1  # Highest priority
    
  - name: "Important Security Rule" 
    priority: 10  # Gap allows for rules 2-9 to be added later
    
  - name: "Standard Security Rule"
    priority: 20  # Gap allows for rules 11-19 to be added later
```

This approach allows you to insert new rules between existing ones without having to reassign priorities.

## Best Practices

1. **Lower Numbers for Higher Priority**: Always remember that lower numbers indicate higher priority in Axiom.

2. **Establish a Priority System**: Define a clear system for assigning priorities (like the ranges shown above) and document it.

3. **Leave Gaps Between Priorities**: Don't use consecutive numbers (1, 2, 3) for priorities. Instead, use increments (10, 20, 30) to allow for future rules to be inserted.

4. **Prioritize Security & Compliance**: Always give security and compliance rules higher priority than business or operational rules.

5. **Be Cautious with Priority 1**: Reserve the absolute highest priorities (1, 2, etc.) for truly critical rules that must trump all others.

6. **Document Priority Decisions**: When assigning priorities, document the reasoning to help future maintainers understand why certain priorities were chosen.

7. **Review Priority Order Regularly**: As rule sets grow, review the priority order regularly to ensure it still makes logical sense.

## Related Sections

- [Rule Overview](rule-overview.md) - General information about rules in Axiom
- [Rule Effective Dates](rule-effective-dates.md) - How effective dates interact with priorities
- [Rule Orchestrator Operations](rule-orchestrator-operations.md) - How orchestrators use rule priorities

[← Back to Previous Section](ruleset-structure.md) 
