# Axiom - Lightweight Rule Engine for Java

[![Build Status](https://github.com/lyxtera/axiom/actions/workflows/ci.yml/badge.svg)](https://github.com/lyxtera/axiom/actions/workflows/ci.yml)
[![Test Coverage](https://raw.githubusercontent.com/lyxtera/axiom/main/.github/badges/coverage.svg)](https://github.com/lyxtera/axiom/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.lyxtera/axiom-parent.svg)](https://central.sonatype.com/artifact/com.lyxtera/axiom-parent)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

Axiom is a lightweight rule engine designed to simplify complex "if-this-then-that" business logic in Java applications. It separates business rules from application code using YAML files and provides automatic code generation for type-safe rule implementation.

## ✨ Key Features

- **🚀 Lightweight & Fast** - Minimal overhead with high performance and less convoluted code
- **📝 YAML-based Rules** - Define rules in human-readable YAML rule-set files
- **🔧 Code Generation** - On-demain automatic stub generation for business checks and actions
- **💉 Dependency Injection** - Built-in Google Guice integration
- **🎯 Type Safety** - Compile-time type checking with generated stubs
- **📊 Expression Language** - ANTLR-based rule expression parser
- **🔄 Hot Reloading** - Update rules without application restart
- **⚡ Dynamic Rule Execution** - Execute caller-supplied rule expressions at runtime with entity-based permission control

## 🚀 Quick Start

### Maven Dependency

Add Axiom to your project:

```xml
<dependencies>
    <dependency>
        <groupId>com.lyxtera</groupId>
        <artifactId>axiom-rules</artifactId>
        <version>1.0.1</version>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>com.lyxtera</groupId>
            <artifactId>axiom-codegen</artifactId>
            <version>1.0.1</version>
            <executions>
                <execution>
                    <id>generate-axiom-stubs</id>
                    <phase>generate-sources</phase>
                    <goals>
                        <goal>generate-stubs</goal>
                    </goals>
                    <configuration>
                        <packageName>com.example.rules</packageName>
                        <contextKeyEnum>com.example.rules.MyContextKey</contextKeyEnum>
                        <ruleSets>
                            <ruleSet>${project.basedir}/src/main/resources/my_rules.yaml</ruleSet>
                        </ruleSets>
                        <outputDirectory>${project.basedir}/src/main/java</outputDirectory>
                        <overwriteExisting>true</overwriteExisting>
                        <failBuildOnDivergedMetadata>true</failBuildOnDivergedMetadata>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

### Basic Usage Example

1. **Define your context keys enum:**

```java
public enum OrderContextKey {
    ORDER_AMOUNT,
    CUSTOMER_TYPE,
    HAS_DISCOUNT_APPLIED
}
```

2. **Create a rule set YAML file (`src/main/resources/order_rules.yaml`):**

```yaml
rulesetName: "Order Processing Rules"
rulesetDescription: "Rules for processing customer orders"

businessChecks:
  - name: isHighValueOrder
    description: Checks if order exceeds threshold
    params:
      - threshold
  - name: isPremiumCustomer
    description: Checks if customer has premium status

businessActions:
  - name: applyDiscount
    description: Applies discount to order
    params:
      - percentage

rules:
  - name: "Premium Customer Discount"
    description: "Apply 10% discount for premium customers"
    expression: isPremiumCustomer() then applyDiscount(10)
    priority: 10
    
  - name: "High Value Order Discount" 
    description: "Apply 5% discount for orders over $1000"
    expression: isHighValueOrder(1000) then applyDiscount(5)
    priority: 20

  - name: "Enterprise Order Gate"
    description: "Forward enterprise customers into a dedicated child ruleset"
    expression: isPremiumCustomer()
    onMatchForwardTo: "/enterprise_order_rules.yaml"
    priority: 30
```

3. **Run code generation:**

```bash
mvn generate-sources
```

4. **Implement the generated business logic:**

```java
@RuleMetadata(name = "isPremiumCustomer", description = "Checks if customer has premium status")
public class IsPremiumCustomerCheck implements BusinessCheck<OrderContextKey> {
    public Value execute(RuleContext<OrderContextKey> context) {
        String customerType = context.getRequired(OrderContextKey.CUSTOMER_TYPE, String.class);
        return Value.of("PREMIUM".equals(customerType));
    }
}

@RuleMetadata(name = "applyDiscount", description = "Applies discount to order")
public class ApplyDiscountAction implements BusinessAction<OrderContextKey> {
    public Value execute(RuleContext<OrderContextKey> context, @Arg("percentage") Value percentage) {
        Double orderAmount = context.getRequired(OrderContextKey.ORDER_AMOUNT, Double.class);
        Double discountPercent = percentage.asNumber().doubleValue();
        
        Double discountedAmount = orderAmount * (1 - (discountPercent / 100));
        context.add(OrderContextKey.ORDER_AMOUNT, discountedAmount);
        context.add(OrderContextKey.HAS_DISCOUNT_APPLIED, true);
        
        return Value.of(true);
    }
}
```

5. **Configure and use the rule engine:**

```java
public class OrderProcessingModule extends AbstractModule {
    @Override
    protected void configure() {
        install(AxiomModule.builder()
            .withContextKeyEnum(OrderContextKey.class)
            .addRuleSet("order-processing", "order_rules.yaml")
            .addBusinessCheck("isPremiumCustomer", IsPremiumCustomerCheck.class)
            .addBusinessAction("applyDiscount", ApplyDiscountAction.class)
            .build());
    }
}

// Usage in your application
@Inject
@Named("order-processing") 
private RuleOrchestrator<OrderContextKey> ruleOrchestrator;

public void processOrder(Order order) {
    RuleContext<OrderContextKey> context = new RuleContext<>(OrderContextKey.class);
    context.add(OrderContextKey.ORDER_AMOUNT, order.getAmount());
    context.add(OrderContextKey.CUSTOMER_TYPE, order.getCustomerType());
    
    RuleExecutionResult<OrderContextKey> result = ruleOrchestrator.executeAllMatchingRules(context);
    
    if (result.hasMatches()) {
        Double finalAmount = context.getRequired(OrderContextKey.ORDER_AMOUNT, Double.class);
        Boolean hasDiscount = context.getRequired(OrderContextKey.HAS_DISCOUNT_APPLIED, Boolean.class);
        
        System.out.println("Final order amount: $" + finalAmount);
        System.out.println("Discount applied: " + hasDiscount);
    }
}
```

## 📖 Documentation

### Rule Expression Language

Axiom supports a powerful expression language for defining rule conditions:

- **Boolean Logic**: `and`, `or`, `not`
- **Comparisons**: `=`, `<`, `>`
- **Grouping**: `( )`
- **Function Calls**: `functionName(arg1, arg2)`

**Examples:**
```yaml
# Simple boolean check
expression: isPremiumCustomer() then applyDiscount(10)

# Comparison with threshold
expression: orderAmount() > 1000 then applyShipping()

# Complex boolean logic
expression: (isPremiumCustomer() or isHighValueOrder(500)) and not hasExistingDiscount() then applyDiscount(15)
```

Gate rules reuse the same expression syntax, but omit the `then` clause and add `onMatchForwardTo`:

```yaml
expression: isPremiumCustomer()
onMatchForwardTo: "/enterprise_order_rules.yaml"
```

### Rule Set Structure

```yaml
rulesetName: "My Rule Set"
rulesetDescription: "Description of what this rule set does"

# Opt-in to allow external entities to submit rule expressions at runtime (default: false)
allowDynamicExecution: false

businessChecks:
  - name: checkName
    description: "Human readable description"
    params: [param1, param2]  # Optional parameters

businessActions:
  - name: actionName
    description: "Human readable description" 
    params: [param1, param2]  # Optional parameters

# Required when allowDynamicExecution is true — defines per-entity function permissions
entityPermissions:
  - name: myService
    allowedFunctions: [checkName, actionName]  # Functions this entity may use
    deniedFunctions:  [dangerousAction]         # Denied takes precedence over allowed

rules:
  - name: "Rule Name"
    description: "What this rule does"
    expression: condition then action
    onMatchForwardTo: "/child_rules.yaml"  # Required for gate rules, omitted for action rules
    priority: 10              # Lower numbers execute first
    effectiveFrom: "2023-01-01T00:00:00Z"  # Optional
    effectiveTo: "2024-01-01T00:00:00Z"    # Optional
```

Each rule must be exactly one of:

- Action rule: `expression` contains `then`, `onMatchForwardTo` is absent
- Gate rule: `expression` contains no `then`, `onMatchForwardTo` points to a child ruleset

### Code Generation Plugin

The `axiom-codegen` Maven plugin generates type-safe stub classes:

```xml
<plugin>
    <groupId>com.lyxtera</groupId>
    <artifactId>axiom-codegen</artifactId>
    <version>1.0.1</version>
    <configuration>
        <packageName>com.example.rules</packageName>
        <contextKeyEnum>com.example.rules.MyContextKey</contextKeyEnum>
        <ruleSets>
            <ruleSet>src/main/resources/rules.yaml</ruleSet>
        </ruleSets>
        <outputDirectory>${project.basedir}/src/main/java</outputDirectory>
        <overwriteExisting>true</overwriteExisting>
        <failBuildOnDivergedMetadata>true</failBuildOnDivergedMetadata>
        <skip>false</skip>
    </configuration>
</plugin>
```

**Plugin Parameters:**

| Parameter | Description | Default | Required |
|-----------|-------------|---------|----------|
| `packageName` | Base package for generated classes | - | Yes |
| `contextKeyEnum` | Fully qualified context enum class name | - | Yes |
| `ruleSets` | List of rule YAML files | - | Yes |
| `outputDirectory` | Output directory for generated sources | `${project.basedir}/src/main/java` | No |
| `overwriteExisting` | Whether to overwrite existing files | `false` | No |
| `skip` | Skip code generation | `false` | No |
| `failBuildOnDivergedMetadata` | Fails the build if ruleset metadata diverges from code implementation | `true` | No |


### Writing Tests for Rules

```java
@Test
void testCustomerDiscountRule() {
    RuleContext<CustomerContextKey> context = new RuleContext<>(CustomerContextKey.class);
    context.add(CustomerContextKey.CUSTOMER_TYPE, "PREMIUM");
    context.add(CustomerContextKey.ORDER_AMOUNT, 500.0);
    
    RuleExecutionResult<CustomerContextKey> result = ruleOrchestrator.executeAllMatchingRules(context);
    
    assertThat(result.hasMatches()).isTrue();
    assertThat(context.getRequired(CustomerContextKey.HAS_DISCOUNT_APPLIED, Boolean.class)).isTrue();
}
```

**Coverage badge (maintainers):** When test coverage changes meaningfully, refresh the badge from the repository root and commit [`.github/badges/coverage.svg`](.github/badges/coverage.svg) with your changes:

```bash
mvn -pl axiom-rules verify -Pcoverage-badge -Dmaven.thrift.skip=true
```

### Dynamic Rule Execution

Dynamic rule execution lets external services submit rule expressions at runtime and have them evaluated against a pre-loaded ruleset — without requiring application redeployment. Each ruleset controls who can do this, and which functions each caller is permitted to use.

#### 1. Enable dynamic execution in your ruleset YAML

```yaml
rulesetName: "Checkout Discount Ruleset"
rulesetDescription: "Metadata for dynamically applying discounts to customer orders"

allowDynamicExecution: true

businessChecks:
  - name: successfulOrders
    description: "How many successful orders the customer has placed"
  - name: isFirstTimeCustomer
    description: "Checks if this is the customer's first order"

businessActions:
  - name: applyDiscount
    description: "Applies a percentage discount to the order"
    params: [percentage]
  - name: updateNote
    description: "Attaches a note to the order"
    params: [noteText]
  - name: updateTotalAmount
    description: "Directly modifies the order total — sensitive operation"
    params: [amount]

entityPermissions:
  - name: checkoutService
    allowedFunctions: [successfulOrders, isFirstTimeCustomer, applyDiscount, updateNote]
    deniedFunctions:  [updateTotalAmount]   # denied takes precedence

  - name: adminService
    allowedFunctions: [successfulOrders, isFirstTimeCustomer, applyDiscount, updateNote, updateTotalAmount]
    deniedFunctions:  []
```

#### 2. Create the orchestrator with a parser

Dynamic rule execution requires parsing rule expressions at runtime. Pass a `Parser` instance to the orchestrator:

```java
Parser<CheckoutContextKey> parser = new DefaultParser<>();
RuleOrchestrator<CheckoutContextKey> orchestrator = new RuleOrchestrator<>(ruleSet, parser);
```

#### 3. Execute dynamic rules

**Simple form** — pass a list of expressions and an entity name:

```java
List<String> expressions = Arrays.asList(
    "successfulOrders() > 15 then applyDiscount(10), updateNote(\"VIP\")",
    "successfulOrders() > 5  then applyDiscount(5),  updateNote(\"Returning customer\")",
    "isFirstTimeCustomer()   then applyDiscount(3),  updateNote(\"Welcome discount\")"
);

RuleContext<CheckoutContextKey> context = new RuleContext<>(CheckoutContextKey.class);
context.add(CheckoutContextKey.ORDER_COUNT, 20);
context.add(CheckoutContextKey.ORDER_VALUE, 150.0);

RuleExecutionResult<CheckoutContextKey> result =
    orchestrator.executeDynamicRules(context, expressions, "checkoutService");
```

**Advanced form** — use `DynamicRuleRequest` for full control:

```java
DynamicRuleRequest<CheckoutContextKey> request = new DynamicRuleRequest<>(
    "checkoutService", "Checkout Discount Ruleset", expressions);

RuleExecutionResult<CheckoutContextKey> result =
    orchestrator.executeDynamicRuleSet(context, request);
```

#### 4. Permission model

| Scenario | Result |
|---|---|
| Entity not listed in `entityPermissions` | `DynamicRuleValidationException` – no permissions defined |
| Function not in `allowedFunctions` | `DynamicRuleValidationException` – function not permitted |
| Function in both allowed and `deniedFunctions` | Denied — `deniedFunctions` takes precedence |
| Function not defined in the ruleset at all | `DynamicRuleValidationException` – function does not exist |
| `allowDynamicExecution: false` | `DynamicRuleValidationException` – dynamic execution not allowed |
| Parse error in an expression | `DynamicRuleValidationException` – rule parsing failed |

#### 5. Handling validation failures

```java
try {
    RuleExecutionResult<CheckoutContextKey> result =
        orchestrator.executeDynamicRules(context, expressions, "orderService");
} catch (DynamicRuleValidationException e) {
    System.err.println("Validation failed: " + e.getMessage());

    if (e.hasPermissionViolations()) {
        e.getViolations().forEach(v -> System.err.println("  - " + v));
    }
}
```

## 🚀 Performance
Axiom is designed for high performance:

- **Method Handle Optimization**: Uses Java method handles for fast invocation
- **Lazy Loading**: Rules loaded on demand
- **Concurrent Execution**: Thread-safe rule execution
- **Minimal Overhead**: Lightweight abstraction layer

### Project Structure

```
axiom/
├── axiom-rules/          # Core rule engine
├── axiom-codegen/        # Maven plugin for code generation  
├── axiom-examples/       # Example applications
└── docs/                 # Documentation
```

## 📝 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

- **Documentation**: [Full documentation](https://lyxtera.github.io/axiom/)
- **Examples**: [Example applications](axiom-examples/)
- **Issues**: [GitHub Issues](https://github.com/lyxtera/axiom/issues)

## 🗓️ Changelog

### Version 1.0.1 (Current)
- ✅ Core rule engine implementation
- ✅ ANTLR-based expression parser
- ✅ Maven plugin for code generation
- ✅ Example applications
- ✅ Documentation
- ✅ Dynamic rule execution with entity-based permission control

### Upcoming Features
- 🔄 Spring Boot starter
- 🔧 Admin dashboard
- 📈 Metrics integration
