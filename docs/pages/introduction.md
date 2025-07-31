# Introduction to Axiom Business Rules

## What is Axiom?

Axiom is a lightweight rule engine designed to simplify complex “if-this-then-that” business logic. In real-world applications—especially microservices and modular systems—developers often end up with conditionals scattered throughout their codebase. Over time, these if-else chains become fragile, difficult to maintain, and nearly impossible to extend without risking regressions. Larger, enterprise-grade rule engines can solve this, but they’re often too heavyweight for everyday needs.

---

## How Axiom Helps

- **Lightweight, Focused Rule Engine**  
  Axiom doesn’t pretend to solve every rule-related problem but focuses on making common patterns like “if X then do Y” easier and more maintainable, without requiring hours of configuration or specialized DSLs.

- **Separation of Concerns**  
  By extracting business rules into discrete “axioms,” you keep your logic out of tangled if-else blocks. That separation makes rules easier to read, reason about, and modify—no more rummaging through complex conditional trees.

- **Extensible & Modular**  
  Axiom’s approach to rules is modular: you can add, remove, or update them without rewriting core code. This makes it simpler to support new features or business changes.

- **Simplicity**  
  You don’t have to install a large, enterprise-grade rules server or learn a complicated syntax. Axiom’s API aims for clarity and minimal overhead, so teams can adopt it quickly.

---

## Where Axiom Fits In Your Architecture

Consider how often you need to write logic like:

```java
if (order.isPriority() && !order.isFlagged()) {
    // expedite shipping
} else if (order.isInternational()) {
    // handle customs
} else if (order.hasGiftWrap()) {
    // add gift wrap processing
}
```

With Axiom, you extract these conditions into reusable business checks and actions, making your code more maintainable and testable.

---

## Key Benefits

### Separation of Concerns

Business rules are maintained separately from application code, allowing business analysts and developers to work collaboratively. Rules are stored in YAML files that can be managed independently of the application code.

```yaml
rules:
  - name: "High Risk Score Rule"
    description: "Block requests with very high risk scores"
    expression: hasRiskScore(90) then blockRequest()
    priority: 80
    effectiveFrom: "2023-05-15T00:00:00Z"
```

### Flexibility

Rules can be modified without redeploying the entire application. The rule engine can load rule definitions from various sources including file systems and databases. You can update business logic by simply modifying YAML files.

### Traceability

Each decision made by the system can be traced back to specific rules. Axiom provides detailed execution results including which rules were evaluated and why they matched or didn't match.

```java
RuleExecutionResult<TestCtxKey> result = orchestrator.executeFirstMatchingRule(context);
if (result.hasMatch()) {
    BusinessRule<TestCtxKey> matchedRule = result.getMatchedRule();
    System.out.println("Rule applied: " + matchedRule.getName());
}
```

### Maintainability

Rules are expressed in a clear, readable format using a business-friendly syntax. This makes them easier to understand, audit, and maintain over time.

### Testability

Rules can be tested independently from the application logic. Axiom provides a comprehensive testing framework to verify rule behavior.

```java
@Test
void testFraudSignalsRule() {
    RuleContext<TestCtxKey> context = new RuleContext<>(TestCtxKey.class);
    context.add(TestCtxKey.HAS_FRAUD_SIGNALS, true);
    
    RuleExecutionResult<TestCtxKey> result = 
        fraudDetectionOrchestrator.executeFirstMatchingRule(context);
        
    assertThat(result.hasMatch()).isTrue();
    assertThat(result.getMatchedRule().getName()).isEqualTo("Fraud Detection Rule");
}
```


