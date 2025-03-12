# Rule Effective Dates

Effective dates are a powerful feature in Axiom that allow you to control when rules are active without changing code or deploying new configurations. By using effective dates, you can set rules to automatically activate or deactivate at specific points in time.

## How Effective Dates Work

Each rule in Axiom can have:

1. **Effect From Date**: The timestamp when the rule becomes active
2. **Effect To Date**: (Optional) The timestamp when the rule becomes inactive

Rules are only considered during evaluation if the current time falls within this range. If no "effective to" date is specified, the rule remains active indefinitely once it reaches its "effective from" date.

## Specifying Effective Dates in YAML

In a rule set YAML file, effective dates are specified using ISO-8601 format:

```yaml
rules:
  - name: "Permanent Rule"
    description: "This rule is permanent once activated"
    expression: isPermanentCondition() then performPermanentAction()
    priority: 10
    effectiveFrom: "2023-01-01T00:00:00Z"  # Active from January 1, 2023
    
  - name: "Temporary Promotion Rule"
    description: "This rule is only active for a specific period"
    expression: isEligibleForPromotion() then applyPromotion()
    priority: 20
    effectiveFrom: "2023-06-01T00:00:00Z"  # Active from June 1, 2023
    effectiveTo: "2023-06-30T23:59:59Z"    # Until June 30, 2023
    
  - name: "Future Rule"
    description: "This rule will become active in the future"
    expression: isFutureCondition() then performFutureAction()
    priority: 30
    effectiveFrom: "2024-01-01T00:00:00Z"  # Won't be active until January 1, 2024
```

## Implementation in the RuleSet Class

The effective date filtering happens in the `RuleSet` class. Here's the implementation of the `isEffectiveNow()` method from the `PrioritizedRule` inner class:

```java
/**
 * Checks if the rule is effective at the current time.
 * 
 * @return true if the rule is effective, false otherwise
 */
boolean isEffectiveNow() {
    ZonedDateTime now = ZonedDateTime.now();
    boolean afterStart = effectiveFrom == null || effectiveFrom.isBefore(now) || effectiveFrom.isEqual(now);
    boolean beforeEnd = effectiveTo == null || effectiveTo.isAfter(now) || effectiveTo.isEqual(now);
    return afterStart && beforeEnd;
}
```

This method is called when retrieving rules from a rule set:

```java
/**
 * Gets all rules in priority order (lowest numbers first)
 *
 * @return Unmodifiable list of rules in priority order
 */
public List<BusinessRule<K>> getRulesInPriorityOrder() {
    return rules.stream()
        .filter(PrioritizedRule::isEffectiveNow)  // Filter based on effective dates
        .map(PrioritizedRule::getRule)
        .collect(Collectors.toUnmodifiableList());
}
```

## Programmatically Setting Effective Dates

You can also set effective dates programmatically when adding rules to a rule set:

```java
// Create a rule set
RuleSet<MyContextKey> ruleSet = new RuleSet<>();

// Create business rules
BusinessRule<MyContextKey> permanentRule = createPermanentRule();
BusinessRule<MyContextKey> temporaryRule = createTemporaryRule();
BusinessRule<MyContextKey> futureRule = createFutureRule();

// Add a permanent rule (effective from a specific date, with no end date)
ruleSet.addRule(
    permanentRule, 
    10, 
    ZonedDateTime.parse("2023-01-01T00:00:00Z")
);

// Add a temporary rule (effective for a specific date range)
ruleSet.addRule(
    temporaryRule, 
    20, 
    ZonedDateTime.parse("2023-06-01T00:00:00Z"),
    ZonedDateTime.parse("2023-06-30T23:59:59Z")
);

// Add a rule that will become active in the future
ruleSet.addRule(
    futureRule, 
    30, 
    ZonedDateTime.parse("2024-01-01T00:00:00Z")
);
```

## Advanced Usage Patterns

### 1. Seasonal Rules

Use effective dates to implement seasonal business rules:

```yaml
# Summer promotion
- name: "Summer Discount Rule"
  description: "Apply summer discount to all orders"
  expression: isOrder() then applyDiscount(15, "SUMMER2023")
  priority: 100
  effectiveFrom: "2023-06-21T00:00:00Z"  # Summer solstice
  effectiveTo: "2023-09-22T23:59:59Z"    # Autumn equinox

# Holiday promotion
- name: "Holiday Season Discount Rule"
  description: "Apply holiday discount to all orders"
  expression: isOrder() then applyDiscount(20, "HOLIDAY2023")
  priority: 100
  effectiveFrom: "2023-11-24T00:00:00Z"  # Black Friday
  effectiveTo: "2023-12-31T23:59:59Z"    # New Year's Eve
```

### 2. Rule Versions and Transitions

Effective dates can be used to manage transitions between rule versions:

```yaml
# Version 1 of the rule (being phased out)
- name: "Risk Evaluation Rule v1"
  description: "Original risk evaluation algorithm"
  expression: evaluateRiskV1() then assignRiskScore()
  priority: 50
  effectiveFrom: "2022-01-01T00:00:00Z"
  effectiveTo: "2023-02-28T23:59:59Z"    # Expires end of February 2023

# Version 2 of the rule (replacing v1)
- name: "Risk Evaluation Rule v2"
  description: "Improved risk evaluation algorithm"
  expression: evaluateRiskV2() then assignRiskScore()
  priority: 50
  effectiveFrom: "2023-02-01T00:00:00Z"  # Overlap period in February for testing
```

This creates a one-month overlap period where both rules are active, allowing for A/B testing or gradual transition.

### 3. Time-based Rule Activation

For complex time-based activation, you can combine effective dates with time checks in your rule conditions:

```yaml
# Regular business hours rule
- name: "Business Hours Service Level"
  description: "Apply standard SLA during business hours"
  expression: isBusinessHours() then applyStandardSLA()
  priority: 100
  effectiveFrom: "2023-01-01T00:00:00Z"
  
# After-hours rule
- name: "After Hours Service Level"
  description: "Apply extended SLA outside business hours"
  expression: not isBusinessHours() then applyExtendedSLA()
  priority: 100
  effectiveFrom: "2023-01-01T00:00:00Z"
```

Where `isBusinessHours()` is a business check that examines the current time:

```java
@RuleMetadata(name = "isBusinessHours", description = "Checks if the current time is within business hours")
public class BusinessHoursCheck implements BusinessCheck<MyContextKey> {
    
    @Override
    public Value execute(RuleContext<MyContextKey> context) {
        ZonedDateTime now = ZonedDateTime.now();
        int hour = now.getHour();
        DayOfWeek day = now.getDayOfWeek();
        
        boolean isWeekday = day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
        boolean isDuringBusinessHours = hour >= 9 && hour < 17; // 9 AM to 5 PM
        
        return Value.of(isWeekday && isDuringBusinessHours);
    }
}
```

### 4. Regulatory Compliance Rules

Use effective dates to implement regulatory compliance rules that must activate on specific dates:

```yaml
# GDPR Compliance Rule (effective from GDPR enforcement date)
- name: "GDPR Data Processing Rule"
  description: "Apply GDPR requirements to European user data processing"
  expression: isEuropeanUserData() then applyGDPRRequirements()
  priority: 5  # High priority for compliance rules
  effectiveFrom: "2018-05-25T00:00:00Z"  # GDPR enforcement date
  
# California Consumer Privacy Act (CCPA) Rule
- name: "CCPA Data Processing Rule"
  description: "Apply CCPA requirements to California user data processing"
  expression: isCaliforniaUserData() then applyCCPARequirements()
  priority: 5  # High priority for compliance rules
  effectiveFrom: "2020-01-01T00:00:00Z"  # CCPA enforcement date
```

## Testing Rules with Effective Dates

When testing rules with effective dates, you may need to simulate different points in time. Here's an example of how to test a rule with future effective dates:

```java
@Test
void testRuleWithFutureEffectiveDate() {
    // Create a rule set
    RuleSet<MyContextKey> ruleSet = new RuleSet<>();
    
    // Create a rule with a future effective date
    BusinessRule<MyContextKey> futureRule = createTestRule();
    ZonedDateTime futureDate = ZonedDateTime.now().plusDays(30); // 30 days in the future
    ruleSet.addRule(futureRule, 10, futureDate);
    
    // Create an orchestrator
    RuleOrchestrator<MyContextKey> orchestrator = new RuleOrchestrator<>(ruleSet);
    
    // Create a context
    RuleContext<MyContextKey> context = new RuleContext<>(MyContextKey.class);
    context.add(MyContextKey.TEST_VALUE, "test");
    
    // Execute rules - should not match because the rule is not effective yet
    RuleExecutionResult<MyContextKey> result = orchestrator.executeFirstMatch(context);
    assertThat(result.hasMatch()).isFalse();
    
    // Now, we'll test with a FixedClock to simulate that it's 31 days in the future
    // Note: This requires modifying the PrioritizedRule class to accept a Clock parameter
    // which is beyond the scope of this explanation
}
```

## Best Practices

1. **Use UTC Times**: Always use UTC (Zulu) time zone for effective dates to avoid daylight saving time issues.

2. **Specify End Times Precisely**: When setting an `effectiveTo` date, use the end of the day (23:59:59) to ensure the rule is active for the entire last day.

3. **Plan for Transitions**: When replacing an existing rule with a new version, consider an overlap period to ensure smooth transitions.

4. **Use Time-Based Activations Carefully**: For time-of-day specific rules, consider using business checks that examine the current time rather than using multiple rules with different effective dates.

5. **Test with Different Time Points**: When testing rules with effective dates, test with times before, during, and after the effective period.

6. **Document Effective Dates**: Include clear documentation about why particular effective dates were chosen, especially for regulatory or business-critical rules.

7. **Audit Effective Date Changes**: Keep an audit trail of changes to effective dates, especially for compliance-related rules.

## Related Sections

- [Rule Overview](rule-overview.md) - General information about rules in Axiom
- [Rule Priority](rule-priority.md) - How rule priority interacts with effective dates
- [Rule Testing](rule-testing.md) - How to test rules with effective dates 
