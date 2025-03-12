# Rule Set Structure

Rule sets are the core configuration unit in Axiom, defining the collection of business rules that operate together. This document explains the structure of rule set configuration files and provides guidance on creating well-structured rule sets.

## YAML Structure Overview

Axiom rule sets are defined in YAML files with a specific structure. Here's a comprehensive overview of a rule set YAML file:

```yaml
rulesetName: "Example Rule Set"
rulesetDescription: "A comprehensive example of a rule set structure"

businessChecks:
  - name: checkOne
    description: "Description of the first check"
    params:
      - paramOne
      - paramTwo
  - name: checkTwo
    description: "Description of the second check"
    params:
      - threshold

businessActions:
  - name: actionOne
    description: "Description of the first action"
    params:
      - amount
  - name: actionTwo
    description: "Description of the second action"
    params:
      - reason
      - severity

rules:
  - name: "Rule One"
    description: "Description of the first rule"
    expression: checkOne(paramOne, paramTwo) then actionOne(100)
    priority: 10
    effectiveFrom: "2023-01-01T00:00:00Z"
    effectiveTo: "2025-01-01T00:00:00Z"
    
  - name: "Rule Two"
    description: "Description of the second rule"
    expression: checkTwo(500) then actionTwo("reason", "HIGH")
    priority: 20
    effectiveFrom: "2023-02-01T00:00:00Z"
```

## Required and Optional Fields

### Rule Set Level Fields

| Field | Required | Description |
|-------|----------|-------------|
| `rulesetName` | Required | The name of the rule set. Should be descriptive and unique within your application. |
| `rulesetDescription` | Optional | A detailed description of the rule set's purpose and function. |

### Business Check Definition Fields

| Field | Required | Description |
|-------|----------|-------------|
| `name` | Required | The identifier used to reference this check in rule expressions. Must match the name used in the Java implementation. |
| `description` | Required | A description of what the check evaluates. |
| `params` | Optional | A list of parameter names that the check accepts. These must match the parameter names used in the Java implementation. |

### Business Action Definition Fields

| Field | Required | Description |
|-------|----------|-------------|
| `name` | Required | The identifier used to reference this action in rule expressions. Must match the name used in the Java implementation. |
| `description` | Required | A description of what the action does. |
| `params` | Optional | A list of parameter names that the action accepts. These must match the parameter names used in the Java implementation. |

### Rule Definition Fields

| Field | Required | Description |
|-------|----------|-------------|
| `name` | Required | A descriptive name for the rule. Should be unique within the rule set. |
| `description` | Required | A detailed description of the rule's purpose and conditions. |
| `expression` | Required | The rule expression that defines the check and action parts. More details below. |
| `priority` | Required | A numeric value determining the rule's execution priority. Lower numbers indicate higher priority. |
| `effectiveFrom` | Optional | The ISO-8601 date-time from which the rule becomes active. If omitted, the rule is active immediately. |
| `effectiveTo` | Optional | The ISO-8601 date-time after which the rule becomes inactive. If omitted, the rule never expires. |
| `tags` | Optional | A list of string tags for categorizing and organizing rules. |

## Rule Expressions

Rule expressions follow a specific syntax:

```
check_condition(parameters) then action(parameters)
```

The expression consists of two parts:
1. **Check condition**: Evaluates to true or false, determining if the rule matches
2. **Action**: The action to perform when the check condition is true

Multiple check conditions can be combined using logical operators:

```
checkOne(param1) AND checkTwo(param2) then actionOne(100)
checkOne(param1) OR checkTwo(param2) then actionOne(100)
NOT checkOne(param1) then actionTwo("reason")
```

## Minimal Valid Rule Set Example

Here's an example of a minimal valid rule set:

```yaml
rulesetName: "Minimal Rule Set"

businessChecks:
  - name: isTrue
    description: "Always returns true"

businessActions:
  - name: doNothing
    description: "Does nothing"

rules:
  - name: "Simple Rule"
    description: "A simple rule that always triggers"
    expression: isTrue() then doNothing()
    priority: 10
```

## Best Practices for Rule Set Structure

1. **Use Descriptive Names**: Give your rule set, checks, actions, and rules clear, descriptive names that indicate their purpose.

2. **Organize by Business Domain**: Group rules that relate to the same business domain in the same rule set.

3. **Keep Rule Sets Focused**: Each rule set should have a single responsibility or domain focus.

4. **Document Thoroughly**: Use the description fields to thoroughly document the purpose and behavior of each component.

5. **Use Consistent Priority Schemes**: Establish a consistent approach to rule priorities. For example:
   - Use priority bands (e.g., 1-10 for critical rules, 11-20 for important rules, etc.)
   - Leave gaps between priorities to allow for future insertions (e.g., 10, 20, 30, etc.)

6. **Leverage Effective Dates**: Use effective dates to manage rule lifecycle, particularly for time-limited promotions or policy changes.

7. **Use Tags for Organization**: Apply consistent tags to help categorize and filter rules, especially in large rule sets.

8. **Version Control**: Keep rule set files in version control along with your application code.

9. **Validate Before Deployment**: Use the built-in validation features to validate rule sets before deploying them to production.

## Common Gotchas and Troubleshooting

- **Parameter Names**: Ensure parameter names in the YAML file match those expected by your Java implementation. Mismatches will cause validation errors.

- **Date Formats**: Effective dates must be in ISO-8601 format (`YYYY-MM-DDThh:mm:ssZ`). Incorrect formats will cause validation errors.

- **Case Sensitivity**: All names (checks, actions, parameters) are case-sensitive and must match exactly between YAML and Java implementations.

- **Expression Syntax**: Rule expressions must follow the exact syntax, including spaces between operators. The validation will catch syntax errors, but they can be tricky to spot manually.

[← Back to Rule Sets Overview](ruleset-overview.md)

[Next: Code Generation →](code-generation.md)
