# Axiom Code Classes Overview

This section provides a comprehensive overview of the key classes in the Axiom framework and how they interact with each other.

## Core Classes Diagram

Below is a conceptual diagram showing the main components of the Axiom framework and their relationships:

```
┌─────────────────┐      ┌───────────────────┐      ┌────────────────────┐
│ RuleSetLoader   │─────▶│ RuleSet           │◀─────│ BusinessRule       │
└─────────────────┘      └───────────────────┘      └────────────────────┘
                                   ▲                          ▲
                                   │                          │
                                   │                          │
┌─────────────────┐      ┌───────────────────┐      ┌────────────────────┐
│ RuleOrchestrator│─────▶│ RuleContext       │◀─────│ RuleExpression     │
└─────────────────┘      └───────────────────┘      └────────────────────┘
        ▲                          ▲                          ▲
        │                          │                          │
        │                          │                          │
┌─────────────────┐      ┌───────────────────┐      ┌────────────────────┐
│ AxiomModule     │      │ BusinessCheck     │      │ BusinessAction     │
└─────────────────┘      └───────────────────┘      └────────────────────┘
```

## Package Structure

The Axiom framework is organized into the following main packages:

| Package | Description | Key Classes |
|---------|-------------|-------------|
| `com.lyxtera.axiom.api.model` | Core API interfaces and models | `BusinessRule`, `BusinessCheck`, `BusinessAction`, `Value` |
| `com.lyxtera.axiom.engine` | Rule execution engine components | `RuleSet`, `RuleOrchestrator`, `RuleContext`, `RuleSetLoader` |
| `com.lyxtera.axiom.parser` | Rule expression parsing | `RuleExpressionParser`, `RuleExpression`, `ConditionVisitor` |
| `com.lyxtera.axiom.config` | Configuration and dependency injection | `AxiomModule`, `RuleMetadata` |
| `com.lyxtera.axiom.api.exception` | Exception classes | `AxiomEngineException`, `RuleLoadException` |

## Key Classes Overview

### Rule Modeling Classes

- **BusinessRule\<K\>**: The core interface for business rules, with methods for rule metadata, condition evaluation, and action execution.
- **BusinessCheck\<K\>**: Interface for implementing condition evaluation functions.
- **BusinessAction\<K\>**: Interface for implementing action execution functions.
- **Value**: A class representing values passed to and from business checks and actions, with support for different types (Boolean, Number, String, etc.).

### Rule Execution Classes

- **RuleContext\<K\>**: A thread-safe container for data being processed by rules, using an enum for keys.
- **RuleSet\<K\>**: A collection of business rules, prioritized and ordered.
- **RuleOrchestrator\<K\>**: The main entry point for rule execution, which evaluates and executes rules based on context.
- **RuleExecutionResult\<K\>**: Captures the result of rule execution, including the executed rule and success status.

### Rule Loading and Parsing

- **RuleSetLoader\<K\>**: Abstract class for loading rule sets from various sources.
- **YamlRuleSetLoader\<K\>**: Implementation of RuleSetLoader that loads rules from YAML files.
- **RuleExpressionParser\<K\>**: Parses rule expressions in the Axiom DSL format.
- **RuleExpression\<K\>**: Represents a parsed and executable rule expression.

### Configuration Classes

- **AxiomModule\<K\>**: Base Guice module for configuring Axiom components.
- **RuleMetadata**: Annotation for business checks and actions to provide metadata.
- **RuleSetDescriptor**: Describes a rule set and its components in YAML.

## Class Inheritance Hierarchy

The following diagram illustrates the inheritance hierarchy of the main Axiom classes:

```
            BusinessRule<K> (Interface)
                   ▲
                   │
                   │
     ┌─────────────┴──────────────┐
     │                            │
DefaultBusinessRule<K>     CompoundBusinessRule<K>


   BusinessCheck<K> (Interface)
            ▲
            │
            │
┌───────────┴────────────┐
│                        │
User Implementations    AbstractParameterizedCheck<K>


  BusinessAction<K> (Interface)
            ▲
            │
            │
┌───────────┴────────────┐
│                        │
User Implementations    AbstractParameterizedAction<K>


     RuleSetLoader<K> (Abstract)
              ▲
              │
              │
        YamlRuleSetLoader<K>


      AxiomModule<K> (Abstract)
               ▲
               │
               │
       User Implementation
```

## Typical Workflow

The typical workflow when using Axiom involves:

1. **Configuration**: Create an `AxiomModule<K>` to register rule sets, business checks, and business actions.
2. **Rule Definition**: Define rules in YAML files with conditions and actions.
3. **Rule Loading**: Use `RuleSetLoader<K>` to load rule sets from YAML files.
4. **Context Preparation**: Create a `RuleContext<K>` with the necessary data for rule evaluation.
5. **Rule Execution**: Use `RuleOrchestrator<K>` to evaluate and execute rules based on the context.
6. **Result Processing**: Handle the `RuleExecutionResult<K>` to determine what action was taken.

## Related Sections

For more detailed information about each component, refer to the following sections:

- [Rule Sets](#ruleset-overview) - Detailed information about rule set organization and structure
- [Rules](#rule-overview) - In-depth explanation of rule definition and properties
- [Rule Context](#rule-context-overview) - How to work with the rule context
- [Business Actions & Checks](#business-components-overview) - Creating condition and action components
- [Rule Orchestrators](#rule-orchestrator-overview) - Using the orchestrator to execute rules
