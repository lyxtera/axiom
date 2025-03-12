# Testing Rules

Effective testing is essential for ensuring that your Axiom business rules work as expected. This guide covers strategies and techniques for testing rules at different levels, from unit testing individual components to integration testing the entire rule system.

## Testing Approach

A comprehensive testing strategy for Axiom rules should include:

1. **Unit Tests**: Testing individual business checks and actions
2. **Rule Tests**: Testing individual rules with mock contexts
3. **Rule Set Tests**: Testing sets of rules together
4. **Integration Tests**: Testing the entire rule system in a realistic environment

## Unit Testing Business Checks and Actions

Before testing rules, it's important to test the building blocks: business checks and actions.

### Testing Business Checks

Business checks should be tested to ensure they correctly evaluate conditions based on the context:

```java
@Test
void testHighRiskScoreCheck() {
    // Create the business check
    HasRiskScoreCheck check = new HasRiskScoreCheck();
    
    // Create a context with a high risk score
    RuleContext<TestCtxKey> context = new RuleContext<>(TestCtxKey.class);
    context.add(TestCtxKey.RISK_SCORE, 95);
    
    // Execute the check with a threshold of 90
    Value result = check.execute(context, Value.of(90));
    
    // Verify the result is true (risk score exceeds threshold)
    assertThat(result.asBoolean()).isTrue();
    
    // Create another context with a lower risk score
    RuleContext<TestCtxKey> lowRiskContext = new RuleContext<>(TestCtxKey.class);
    lowRiskContext.add(TestCtxKey.RISK_SCORE, 85);
    
    // Execute the check again
    Value lowRiskResult = check.execute(lowRiskContext, Value.of(90));
    
    // Verify the result is false (risk score does not exceed threshold)
    assertThat(lowRiskResult.asBoolean()).isFalse();
}
```

### Testing Business Actions

Business actions should be tested to ensure they modify the context or perform external operations correctly:

```java
@Test
void testBlockRequestAction() {
    // Create the business action
    BlockRequestAction action = new BlockRequestAction();
    
    // Create a context
    RuleContext<TestCtxKey> context = new RuleContext<>(TestCtxKey.class);
    context.add(TestCtxKey.REQUEST_ID, "REQ-12345");
    
    // Execute the action
    Value result = action.execute(context);
    
    // Verify the action succeeded
    assertThat(result.asBoolean()).isTrue();
    
    // Verify the context was modified correctly
    assertThat(context.get(TestCtxKey.REQUEST_BLOCKED, Boolean.class))
        .isPresent()
        .hasValue(true);
}
```

For actions with external dependencies, you might need to use mocks:

```java
@Test
void testNotificationAction() {
    // Create mock notification service
    NotificationService mockService = mock(NotificationService.class);
    
    // Create the action with the mock service
    SendNotificationAction action = new SendNotificationAction(mockService);
    
    // Create a context
    RuleContext<TestCtxKey> context = new RuleContext<>(TestCtxKey.class);
    context.add(TestCtxKey.CUSTOMER_ID, "CUST-12345");
    context.add(TestCtxKey.NOTIFICATION_MESSAGE, "Test message");
    
    // Execute the action
    action.execute(context);
    
    // Verify the notification service was called correctly
    verify(mockService).sendNotification(
        eq("CUST-12345"), 
        eq("Test message")
    );
}
```

## Testing Individual Rules

Once you've tested the individual checks and actions, you can test complete rules:

```java
@Test
void testFraudDetectionRule() {
    // Create a business rule
    Condition<TestCtxKey> condition = new HasFraudSignalsCondition();
    List<RuleFunction<TestCtxKey>> actions = List.of(new BlockRequestAction());
    BusinessRule<TestCtxKey> rule = new BusinessRule<>("Fraud Detection Rule", condition, actions);
    
    // Create a context with fraud signals
    RuleContext<TestCtxKey> context = new RuleContext<>(TestCtxKey.class);
    context.add(TestCtxKey.HAS_FRAUD_SIGNALS, true);
    context.add(TestCtxKey.REQUEST_ID, "REQ-12345");
    
    // Evaluate the rule
    boolean matched = rule.evaluate(context);
    
    // Verify the rule matched and the action was performed
    assertThat(matched).isTrue();
    assertThat(context.get(TestCtxKey.REQUEST_BLOCKED, Boolean.class))
        .isPresent()
        .hasValue(true);
    
    // Test with a context that doesn't match the condition
    RuleContext<TestCtxKey> noFraudContext = new RuleContext<>(TestCtxKey.class);
    noFraudContext.add(TestCtxKey.HAS_FRAUD_SIGNALS, false);
    noFraudContext.add(TestCtxKey.REQUEST_ID, "REQ-67890");
    
    // Evaluate again
    boolean noMatch = rule.evaluate(noFraudContext);
    
    // Verify the rule didn't match and no action was performed
    assertThat(noMatch).isFalse();
    assertThat(noFraudContext.get(TestCtxKey.REQUEST_BLOCKED, Boolean.class))
        .isEmpty();
}
```

## Testing Rule Sets

Testing entire rule sets allows you to verify the interaction between multiple rules, especially the priority ordering:

```java
@Test
void testRuleSetPriorityOrder() {
    // Create a rule set
    RuleSet<TestCtxKey> ruleSet = new RuleSet<>();
    
    // Add rules with different priorities
    BusinessRule<TestCtxKey> highPriorityRule = createHighPriorityRule();
    BusinessRule<TestCtxKey> lowPriorityRule = createLowPriorityRule();
    
    ruleSet.addRule(highPriorityRule, 10, ZonedDateTime.now());
    ruleSet.addRule(lowPriorityRule, 20, ZonedDateTime.now());
    
    // Create an orchestrator
    RuleOrchestrator<TestCtxKey> orchestrator = new RuleOrchestrator<>(ruleSet);
    
    // Create a context that matches both rules
    RuleContext<TestCtxKey> context = new RuleContext<>(TestCtxKey.class);
    context.add(TestCtxKey.MATCHES_HIGH_PRIORITY, true);
    context.add(TestCtxKey.MATCHES_LOW_PRIORITY, true);
    
    // Execute first match
    RuleExecutionResult<TestCtxKey> result = orchestrator.executeFirstMatch(context);
    
    // Verify the high priority rule matched
    assertThat(result.hasMatch()).isTrue();
    assertThat(result.getMatchedRule().getName()).isEqualTo("High Priority Rule");
}
```

You can also test rule sets with effective dates:

```java
@Test
void testRuleSetEffectiveDates() {
    // Create a rule set
    RuleSet<TestCtxKey> ruleSet = new RuleSet<>();
    
    // Create rules
    BusinessRule<TestCtxKey> activeRule = createTestRule("Active Rule");
    BusinessRule<TestCtxKey> futureRule = createTestRule("Future Rule");
    BusinessRule<TestCtxKey> expiredRule = createTestRule("Expired Rule");
    
    // Add rules with different effective dates
    ZonedDateTime now = ZonedDateTime.now();
    ZonedDateTime yesterday = now.minusDays(1);
    ZonedDateTime tomorrow = now.plusDays(1);
    ZonedDateTime lastWeek = now.minusWeeks(1);
    ZonedDateTime lastMonth = now.minusMonths(1);
    
    ruleSet.addRule(activeRule, 10, lastWeek); // Active from last week
    ruleSet.addRule(futureRule, 20, tomorrow); // Active from tomorrow
    ruleSet.addRule(expiredRule, 30, lastMonth, yesterday); // Expired yesterday
    
    // Get rules in priority order (should filter out inactive rules)
    List<BusinessRule<TestCtxKey>> activeRules = ruleSet.getRulesInPriorityOrder();
    
    // Verify only the active rule is returned
    assertThat(activeRules).hasSize(1);
    assertThat(activeRules.get(0).getName()).isEqualTo("Active Rule");
}
```

## Integration Testing with Guice

For integration testing with Guice, you can create a test module that configures the Axiom framework:

```java
public class TestAxiomModule extends AxiomModule<TestCtxKey> {
    
    public TestAxiomModule() {
        super(TestCtxKey.class);
    }
    
    @Override
    protected Map<String, RuleSetLoader<TestCtxKey>> getRegisteredLoaders() {
        Map<String, RuleSetLoader<TestCtxKey>> loaders = new HashMap<>();
        
        // Add test rule set loaders
        loaders.put("test_rules", new YamlRuleSetLoader<>("test_rules.yaml"));
        
        return loaders;
    }
    
    @Override
    protected void configureBusinessRules(
            MapBinder<String, BusinessCheck<TestCtxKey>> checks,
            MapBinder<String, BusinessAction<TestCtxKey>> actions) {
        // Register test business checks
        checks.addBinding("isTestCondition").to(TestConditionCheck.class);
        
        // Register test business actions
        actions.addBinding("performTestAction").to(TestAction.class);
    }
}
```

Then you can write integration tests that use the module:

```java
@Test
void testIntegrationWithGuice() {
    // Create Guice injector with the test module
    Injector injector = Guice.createInjector(new TestAxiomModule());
    
    // Get the rule orchestrator
    RuleOrchestrator<TestCtxKey> orchestrator = 
        injector.getInstance(Key.get(
            new TypeLiteral<RuleOrchestrator<TestCtxKey>>() {}, 
            Names.named("test_rules")
        ));
    
    // Create a test context
    RuleContext<TestCtxKey> context = new RuleContext<>(TestCtxKey.class);
    context.add(TestCtxKey.TEST_CONDITION, true);
    
    // Execute rules
    RuleExecutionResult<TestCtxKey> result = orchestrator.executeFirstMatch(context);
    
    // Verify the rule matched and action was performed
    assertThat(result.hasMatch()).isTrue();
    assertThat(result.getMatchedRule().getName()).isEqualTo("Test Rule");
    assertThat(context.get(TestCtxKey.TEST_ACTION_PERFORMED, Boolean.class))
        .isPresent()
        .hasValue(true);
}
```

## Testing Real YAML Rule Sets

Testing with actual YAML rule sets allows you to verify the complete rule definition process:

```java
@Test
void testYamlRuleSet() throws IOException {
    // Create a temporary YAML file
    Path tempFile = Files.createTempFile("test_rules", ".yaml");
    Files.write(tempFile, Arrays.asList(
        "rulesetName: \"Test Rule Set\"",
        "rulesetDescription: \"Rules for testing\"",
        "",
        "businessChecks:",
        "  - name: isTestCondition",
        "    description: A test condition",
        "",
        "businessActions:",
        "  - name: performTestAction",
        "    description: A test action",
        "",
        "rules:",
        "  - name: \"Test Rule\"",
        "    description: \"A rule for testing\"",
        "    expression: isTestCondition() then performTestAction()",
        "    priority: 10",
        "    effectiveFrom: \"2023-01-01T00:00:00Z\""
    ));
    
    try {
        // Create a loader for the YAML file
        RuleSetLoader<TestCtxKey> loader = 
            new YamlRuleSetLoader<>(tempFile.toString());
        
        // Create an orchestrator test helper (see below)
        OrchestratorTestHelper<TestCtxKey> helper = 
            new OrchestratorTestHelper<>(loader);
        
        // Register the required business checks and actions
        helper.registerCheck("isTestCondition", new TestConditionCheck());
        helper.registerAction("performTestAction", new TestAction());
        
        // Create a test context
        RuleContext<TestCtxKey> context = new RuleContext<>(TestCtxKey.class);
        context.add(TestCtxKey.TEST_CONDITION, true);
        
        // Execute the rule
        RuleExecutionResult<TestCtxKey> result = 
            helper.getOrchestrator().executeFirstMatch(context);
        
        // Verify the rule matched
        assertThat(result.hasMatch()).isTrue();
        assertThat(result.getMatchedRule().getName()).isEqualTo("Test Rule");
    } finally {
        // Clean up
        Files.deleteIfExists(tempFile);
    }
}
```

Where `OrchestratorTestHelper` is a test utility class:

```java
public class OrchestratorTestHelper<K extends Enum<K>> {
    private final RuleSetLoader<K> loader;
    private final Map<String, BusinessCheck<K>> checks = new HashMap<>();
    private final Map<String, BusinessAction<K>> actions = new HashMap<>();
    private RuleOrchestrator<K> orchestrator;
    
    public OrchestratorTestHelper(RuleSetLoader<K> loader) {
        this.loader = loader;
    }
    
    public void registerCheck(String name, BusinessCheck<K> check) {
        checks.put(name, check);
    }
    
    public void registerAction(String name, BusinessAction<K> action) {
        actions.put(name, action);
    }
    
    public RuleOrchestrator<K> getOrchestrator() {
        if (orchestrator == null) {
            RuleParser<K> parser = new RuleParser<>(checks, actions);
            RuleSet<K> ruleSet = loader.loadRuleSet(parser);
            orchestrator = new RuleOrchestrator<>(ruleSet);
        }
        return orchestrator;
    }
}
```

## Advanced Testing Techniques

### Testing with Mocked Time

For testing rules with effective dates, you can mock the time:

```java
@Test
void testEffectiveDateHandling() {
    // Create a rule set
    RuleSet<TestCtxKey> ruleSet = new RuleSet<>();
    
    // Add a rule with a future effective date
    BusinessRule<TestCtxKey> rule = createTestRule();
    ZonedDateTime futureDate = ZonedDateTime.now().plusDays(7);
    ruleSet.addRule(rule, 10, futureDate);
    
    // Create an orchestrator
    RuleOrchestrator<TestCtxKey> orchestrator = new RuleOrchestrator<>(ruleSet);
    
    // Create a context
    RuleContext<TestCtxKey> context = new RuleContext<>(TestCtxKey.class);
    
    // Execute rules - should not match because the rule is not effective yet
    RuleExecutionResult<TestCtxKey> result = orchestrator.executeFirstMatch(context);
    assertThat(result.hasMatch()).isFalse();
    
    // Now, we need to mock time to be after the effective date
    // This would require modifying Axiom's code to use a Clock that can be mocked
    // For demonstration purposes, we'll just show the concept
    
    // Assume we've modified RuleSet to use a Clock
    Clock mockClock = Clock.fixed(
        futureDate.plusDays(1).toInstant(), 
        ZoneId.systemDefault()
    );
    
    // Assuming we can set the clock on the rule set
    // ruleSet.setClock(mockClock);
    
    // Execute rules again - now should match
    // RuleExecutionResult<TestCtxKey> resultAfterTimeChange = orchestrator.executeFirstMatch(context);
    // assertThat(resultAfterTimeChange.hasMatch()).isTrue();
}
```

### Testing Rule Execution Performance

For performance-critical applications, you can measure rule execution time:

```java
@Test
void testRuleExecutionPerformance() {
    // Create a rule set with multiple rules
    RuleSet<TestCtxKey> ruleSet = createLargeRuleSet(100); // 100 rules
    
    // Create an orchestrator
    RuleOrchestrator<TestCtxKey> orchestrator = new RuleOrchestrator<>(ruleSet);
    
    // Create a context
    RuleContext<TestCtxKey> context = new RuleContext<>(TestCtxKey.class);
    context.add(TestCtxKey.PERFORMANCE_TEST, true);
    
    // Measure execution time
    long startTime = System.nanoTime();
    
    orchestrator.executeFirstMatch(context);
    
    long endTime = System.nanoTime();
    long durationMs = (endTime - startTime) / 1_000_000; // Convert to milliseconds
    
    // Assert that execution time is within acceptable limits
    assertThat(durationMs).isLessThan(100); // Less than 100ms
}
```

### Testing Rule Execution Results

For more complex rule evaluations, you can test the execution results thoroughly:

```java
@Test
void testComplexRuleExecutionResults() {
    // Create or load a rule set with multiple rules
    RuleSet<TestCtxKey> ruleSet = loadTestRuleSet();
    
    // Create an orchestrator
    RuleOrchestrator<TestCtxKey> orchestrator = new RuleOrchestrator<>(ruleSet);
    
    // Create a context that should trigger specific rules
    RuleContext<TestCtxKey> context = new RuleContext<>(TestCtxKey.class);
    context.add(TestCtxKey.CUSTOMER_TYPE, "PREMIUM");
    context.add(TestCtxKey.ORDER_AMOUNT, 5000.0);
    
    // Execute all matching rules
    RuleExecutionResult<TestCtxKey> result = orchestrator.executeAllMatches(context);
    
    // Verify multiple aspects of the result
    assertThat(result.hasMatches()).isTrue();
    assertThat(result.getMatchedRules()).hasSize(3); // Expecting 3 matching rules
    
    // Verify specific rules matched
    List<String> matchedRuleNames = result.getMatchedRules().stream()
        .map(BusinessRule::getName)
        .collect(Collectors.toList());
    
    assertThat(matchedRuleNames).containsExactly(
        "Premium Customer Rule",
        "High Value Order Rule",
        "Premium High Value Rule"
    );
    
    // Verify the context was modified as expected
    RuleContext<TestCtxKey> resultContext = result.getContext();
    
    // Verify discount was applied
    assertThat(resultContext.get(TestCtxKey.DISCOUNT_APPLIED, Boolean.class))
        .isPresent()
        .hasValue(true);
    
    // Verify the discount amount
    assertThat(resultContext.get(TestCtxKey.DISCOUNT_PERCENTAGE, Double.class))
        .isPresent()
        .hasValue(15.0); // Expecting 15% discount
}
```

## Best Practices for Testing Rules

1. **Test Each Component**: Test business checks, actions, and rules individually before testing them together.

2. **Use Descriptive Test Names**: Name your tests clearly to describe what's being tested and the expected outcome.

3. **Create Test Utilities**: Develop helper classes to simplify rule testing, especially for complex rule sets.

4. **Test Edge Cases**: Test with extreme values, empty contexts, and other edge cases.

5. **Test Rule Interactions**: Ensure that rules with overlapping conditions and different priorities work correctly together.

6. **Mock External Dependencies**: Use mocking frameworks to isolate rule testing from external systems.

7. **Test Performance**: For large rule sets, test performance to ensure rules evaluate efficiently.

8. **Test Effective Dates**: Verify that rules are only active during their specified date ranges.

9. **Automate Tests**: Include rule tests in your CI/CD pipeline to catch regressions early.

10. **Document Test Scenarios**: Maintain clear documentation of what each test is verifying.

## Related Sections

- [Rule Overview](rule-overview.md) - Understanding the basics of rules
- [Rule Effective Dates](rule-effective-dates.md) - Testing rules with effective dates
- [Rule Priority](rule-priority.md) - Testing rule priority ordering

[← Back to Previous Section](ruleset-structure.md) 
