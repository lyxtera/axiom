# Introduction to Axiom Business Rules

Welcome to the Axiom Business Rules Developer Guide. This comprehensive documentation will help you understand, develop, and maintain business rules within the Axiom framework.

## What is Axiom?

Axiom provides a framework for implementing business rules in a way that separates them from your core application logic. This separation makes rules more maintainable, testable, and accessible to business stakeholders. The framework is designed to be:

- **Flexible**: Rules can be loaded from various sources, including YAML files or databases
- **Type-safe**: Uses Java generics to provide strong typing for rule contexts
- **Thread-safe**: Supports concurrent rule evaluation across multiple threads
- **Extensible**: Allows custom business checks and actions to be implemented and registered

Axiom is particularly useful for systems that need to apply complex business rules that may change frequently or require business stakeholder input.

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
RuleExecutionResult<TestCtxKey> result = orchestrator.executeFirstMatch(context);
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
        fraudDetectionOrchestrator.executeFirstMatch(context);
        
    assertThat(result.hasMatch()).isTrue();
    assertThat(result.getMatchedRule().getName()).isEqualTo("Fraud Detection Rule");
}
```

## Purpose of This Guide

This developer guide aims to provide you with:

1. A clear understanding of Axiom business rules concepts
2. Step-by-step instructions for creating and testing rules
3. Best practices for rule development
4. Real-world examples based on actual implementations
5. Troubleshooting tips

## Target Audience

This guide is designed for:

- **Developers** working with the Axiom framework
- **Business Analysts** who need to understand how business logic is implemented
- **QA Engineers** responsible for testing rule-based systems
- **System Architects** designing solutions using business rules engines

## Architecture Overview

Axiom is built on a flexible architecture with the following key components:

### Rule Sets

Collections of business rules with metadata and priority. Rule sets group related rules together and maintain rule priority ordering. They are typically loaded from YAML files.

### Business Checks

Functions that evaluate conditions. Business checks are implemented as Java classes that implement the `BusinessCheck<K>` interface. They can take parameters and return boolean values indicating whether a condition is met.

```java
@RuleMetadata(name = "hasRiskScore", description = "Checks if the risk score is above a specified threshold")
public class HasRiskScoreCheck implements BusinessCheck<ContextKey> {
    public Value execute(RuleContext<ContextKey> context, @Arg("threshold") Value threshold) {
        Integer riskScore = context.getRequired(ContextKey.RISK_SCORE, Integer.class);
        Integer thresholdValue = threshold.asInteger();
        return Value.of(riskScore >= thresholdValue);
    }
}
```

### Business Actions

Functions that perform actions when rules match. Business actions are implemented as Java classes that implement the `BusinessAction<K>` interface. They are executed when a rule's condition is met.

```java
@RuleMetadata(name = "blockRequest", description = "Blocks the suspension request entirely")
public class BlockRequestAction implements BusinessAction<ContextKey> {
    @Override
    public Value execute(RuleContext<ContextKey> context) {
        context.add(ContextKey.REQUEST_BLOCKED, true);
        return Value.of(true);
    }
}
```

### Rule Context

A thread-safe container for data being processed by rules. The rule context provides a type-safe way to store and retrieve data during rule evaluation.

```java
RuleContext<ContextKey> context = new RuleContext<>(ContextKey.class);
context.add(ContextKey.CUSTOMER_ID, "C12345");
context.add(ContextKey.TRANSACTION_AMOUNT, 9999.99);
```

### Rule Orchestrator

Coordinates rule evaluation and execution. The rule orchestrator applies rules from a rule set against a given context and provides methods to execute rules and retrieve results.

```java
RuleOrchestrator<ContextKey> orchestrator = new RuleOrchestrator<>(ruleSet);
RuleExecutionResult<ContextKey> result = orchestrator.executeFirstMatch(context);
```

### Rule Parser

Converts rule definitions from YAML to executable objects. The rule parser interprets the rule expressions in YAML files and creates the appropriate Java objects.

This modular design allows for flexible integration with existing Java applications using dependency injection frameworks like Guice.

## How to Use This Guide

The guide is organized in a logical progression, starting with basic concepts and moving toward more advanced topics. If you're new to Axiom, we recommend starting with the Getting Started section. Experienced users may want to jump directly to specific topics using the navigation menu.

Each section includes practical examples based on real-world use cases and actual implementations of the Axiom framework.

Let's begin your journey with Axiom Business Rules!
