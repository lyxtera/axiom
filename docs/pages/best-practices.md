# Best Practices for Axiom Rules

This guide covers best practices for developing, testing, and maintaining Axiom business rules in production environments.

## Development Best Practices

### 1. Rule Design Principles

#### Keep Rules Simple and Focused
- Each rule should have a single, clear purpose
- Avoid complex nested conditions that are hard to understand
- Use descriptive rule names that explain what the rule does

```yaml
# Good: Clear, focused rule
- name: "Apply Premium Customer Discount"
  description: "Apply 10% discount for premium customers with orders over $100"
  expression: isPremiumCustomer() and orderAmount() > 100 then applyDiscount(10)

# Avoid: Complex, multi-purpose rule
- name: "Complex Customer Processing"
  expression: (isPremiumCustomer() or (isRegularCustomer() and orderAmount() > 200 and hasLoyaltyPoints())) and not hasActivePromotion() then (applyDiscount(5) and updateLoyaltyPoints() and sendNotification())
```

#### Use Meaningful Names
- Business checks and actions should have descriptive names
- Context keys should clearly indicate what data they contain
- Rule names should explain business intent, not technical implementation

### 2. Code Organization

#### Separate Business Logic by Domain
```java
// Good: Organize by business domain
com.example.rules.customer.checks.IsHighValueCustomerCheck
com.example.rules.customer.actions.ApplyCustomerDiscountAction
com.example.rules.order.checks.HasMinimumOrderAmountCheck
com.example.rules.order.actions.ApplyShippingDiscountAction
```

#### Use Consistent Parameter Naming
```java
// Good: Consistent parameter names
@Arg("threshold") Value threshold
@Arg("percentage") Value percentage
@Arg("minAmount") Value minAmount

// Avoid: Inconsistent naming
@Arg("thresh") Value threshold
@Arg("pct") Value percentage
@Arg("minimum_amount") Value minAmount
```

### 3. Error Handling

#### Validate Input Parameters
```java
public Value execute(RuleContext<CustomerContextKey> ctx, @Arg("threshold") Value threshold) {
    // Validate parameters
    if (threshold == null) {
        throw new IllegalArgumentException("Threshold parameter cannot be null");
    }
    
    // Validate context data
    if (!ctx.has(CustomerContextKey.SPENDING_AMOUNT)) {
        throw new IllegalStateException("Required context key SPENDING_AMOUNT is missing");
    }
    
    // Business logic here...
}
```

#### Handle Edge Cases Gracefully
```java
public Value execute(RuleContext<CustomerContextKey> ctx) {
    // Handle potential null or missing data
    Optional<LocalDateTime> regDate = ctx.get(CustomerContextKey.REGISTRATION_DATE, LocalDateTime.class);
    if (regDate.isEmpty()) {
        // Return false for customers without registration date rather than throwing
        return Value.of(false);
    }
    
    // Continue with business logic...
}
```

## Testing Best Practices

### 1. Comprehensive Test Coverage

#### Test All Rule Scenarios
```java
@Test
@DisplayName("Should apply discount for high value customers")
void testHighValueCustomerDiscount() {
    // Given: High value customer
    RuleContext<CustomerContextKey> context = createContext(
        new BigDecimal("2500.00"), // High spending
        5,                         // High loyalty
        LocalDateTime.now().minusDays(35)
    );

    // When: Rules are executed
    RuleExecutionResult<CustomerContextKey> result = ruleOrchestrator.executeAllMatchingRules(context);

    // Then: Verify expected behavior
    assertThat(result.hasMatches()).isTrue();
    assertThat(result.hasFailed()).isFalse();
    
    BigDecimal discount = context.getRequired(CustomerContextKey.DISCOUNT_PERCENTAGE, BigDecimal.class);
    assertThat(discount).isGreaterThan(BigDecimal.ZERO);
}
```

#### Test Edge Cases
```java
@Test
@DisplayName("Should handle new customer scenario gracefully")
void testNewCustomer() {
    // Given: Brand new customer with minimal data
    RuleContext<CustomerContextKey> context = createContext(
        new BigDecimal("50.00"),  // Low spending
        1,                        // Low loyalty
        LocalDateTime.now()       // Just registered
    );

    // When: Rules are executed
    RuleExecutionResult<CustomerContextKey> result = ruleOrchestrator.executeAllMatchingRules(context);

    // Then: Should handle gracefully (no rules may match)
    if (result.hasFailed()) {
        assertThat(result.getFailureReason()).hasValue("No rules matched the context");
    } else {
        assertThat(result.hasMatches()).isTrue();
    }
}
```

### 2. Integration Testing

#### Test End-to-End Scenarios
```java
@DisplayName("Axiom Integration Tests")
class AxiomIntegrationTest {
    
    @Inject
    @Named("customer_discount")
    private RuleOrchestrator<CustomerContextKey> ruleOrchestrator;
    
    @BeforeEach
    void setUp() {
        // Initialize Guice injector with production configuration
        Injector injector = Guice.createInjector(new ApplicationMainModule());
        injector.injectMembers(this);
    }
    
    @Test
    @DisplayName("Should execute multiple rules in priority order")
    void testRulePriorityExecution() {
        // Test that rules execute in correct priority order
        // and produce expected cumulative results
    }
}
```

### 3. Performance Testing

#### Benchmark Rule Execution
```java
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class RulePerformanceBenchmark {
    
    @Benchmark
    public RuleExecutionResult<CustomerContextKey> benchmarkHighValueCustomerRules() {
        return ruleOrchestrator.executeAllMatchingRules(highValueCustomerContext);
    }
    
    @Benchmark
    public RuleExecutionResult<CustomerContextKey> benchmarkSimpleCustomerRules() {
        return ruleOrchestrator.executeAllMatchingRules(simpleCustomerContext);
    }
}
```

## Production Deployment Best Practices

### 1. Rule Versioning and Deployment

#### Use Effective Dates for Rule Changes
```yaml
rules:
  - name: "Holiday Discount 2024"
    description: "Special holiday discount for December 2024"
    expression: orderAmount() > 100 then applyDiscount(15)
    effectiveFrom: "2024-12-01T00:00:00Z"
    effectiveTo: "2024-12-31T23:59:59Z"
    priority: 10
```

#### Gradual Rule Rollout
- Deploy new rules with future effective dates
- Test in staging environment first
- Monitor rule execution results after deployment
- Have rollback plan for problematic rules

### 2. Monitoring and Observability

#### Log Rule Execution Results
```java
public class OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    
    public void processOrder(Order order) {
        RuleExecutionResult<OrderContextKey> result = orchestrator.executeAllMatchingRules(context);
        
        // Log execution summary
        logger.info("Executed {} rules for order {}, {} matched, {} failed", 
            result.getExecutedRules().size(), 
            order.getId(), 
            result.getMatchedRules().size(),
            result.hasFailed() ? 1 : 0);
            
        // Log individual rule results for debugging
        result.getExecutedRules().forEach((rule, success) -> 
            logger.debug("Rule '{}' executed with result: {}", rule.getName(), success));
    }
}
```

#### Monitor Performance Metrics
- Track rule execution times
- Monitor rule match rates
- Alert on execution failures
- Track context data completeness

### 3. Configuration Management

#### Externalize Rule Configuration
```properties
# application.properties
axiom.rules.customer-discount.file=customer_discount_rules.yaml
axiom.rules.customer-discount.reload-interval=300s
axiom.rules.performance.enable-caching=true
axiom.rules.performance.cache-size=1000
```

#### Environment-Specific Rules
```yaml
# development.yaml
rules:
  - name: "Debug Discount"
    description: "Special discount for testing"
    expression: alwaysTrue() then applyDiscount(50)
    priority: 1

# production.yaml  
rules:
  # Only production-appropriate rules
```

## Security Best Practices

### 1. Input Validation

#### Validate All Context Data
```java
public Value execute(RuleContext<CustomerContextKey> ctx, @Arg("amount") Value amount) {
    // Validate numeric ranges
    BigDecimal amountValue = amount.asNumber();
    if (amountValue.compareTo(BigDecimal.ZERO) < 0) {
        throw new IllegalArgumentException("Amount cannot be negative");
    }
    if (amountValue.compareTo(new BigDecimal("1000000")) > 0) {
        throw new IllegalArgumentException("Amount exceeds maximum allowed value");
    }
    
    // Continue with business logic...
}
```

### 2. Access Control

#### Limit Rule Modification Access
- Control who can deploy rule changes
- Require code review for rule modifications
- Use audit logging for rule deployments
- Implement approval workflows for production changes

## Performance Optimization

### 1. Rule Design for Performance

#### Order Rules by Frequency and Cost
```yaml
rules:
  # High frequency, low cost rules first
  - name: "Simple Amount Check"
    expression: orderAmount() > 10 then flagForReview()
    priority: 1
    
  # Lower frequency, higher cost rules later  
  - name: "Complex Customer Analysis"
    expression: isHighRiskCustomer() and hasComplexHistory() then escalateToManager()
    priority: 100
```

#### Minimize Expensive Operations
```java
// Good: Cache expensive operations
private final Map<String, Boolean> customerCache = new ConcurrentHashMap<>();

public Value execute(RuleContext<CustomerContextKey> ctx) {
    String customerId = ctx.getRequired(CustomerContextKey.CUSTOMER_ID, String.class);
    
    // Use cache to avoid repeated expensive calls
    Boolean isHighRisk = customerCache.computeIfAbsent(customerId, 
        id -> expensiveRiskCalculation(id));
    
    return Value.of(isHighRisk);
}
```

### 2. Context Optimization

#### Provide Only Necessary Data
```java
// Good: Minimal context with only required data
RuleContext<OrderContextKey> context = new RuleContext<>(OrderContextKey.class);
context.add(OrderContextKey.ORDER_AMOUNT, order.getAmount());
context.add(OrderContextKey.CUSTOMER_TYPE, customer.getType());

// Avoid: Loading unnecessary data
// context.add(OrderContextKey.FULL_CUSTOMER_HISTORY, loadEntireHistory(customer));
```

## Maintenance Best Practices

### 1. Documentation

#### Document Business Rules
- Maintain clear documentation for each rule's business purpose
- Document rule interactions and dependencies
- Keep examples up-to-date with current implementation
- Document known limitations and edge cases

### 2. Rule Lifecycle Management

#### Regular Rule Review
- Periodically review rule effectiveness
- Remove obsolete or unused rules
- Update rules when business requirements change
- Monitor rule performance and optimize as needed

#### Version Control
- Store rule files in version control
- Tag rule releases
- Maintain release notes for rule changes
- Use branching strategy for rule development

[← Back to Testing](rule-testing.md) 
