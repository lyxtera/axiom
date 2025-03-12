# Rule Set Validations

Axiom provides a robust validation framework to ensure rule sets are correctly configured before they're loaded into your application. This document covers the validation process, error types, and how to handle validation failures.

## Validation Process

Validation happens automatically when a rule set is loaded. The validation process includes:

1. **Syntax validation**: Checks the YAML structure and syntax
2. **Reference validation**: Verifies references to business checks and actions
3. **Expression validation**: Validates rule expressions syntax and parameters
4. **Semantic validation**: Checks for logical consistency in the rule set

## Types of Validations

### Structural Validations

These validations ensure the rule set YAML has the correct structure:

- Required fields (rulesetName, business check names, rule names, etc.)
- Unique identifiers within their scope (rule names, check names, etc.)
- Proper data types (priority as a number, dates in ISO format, etc.)

### Reference Validations

These validations ensure all references are valid:

- Business checks referenced in rules exist in the registered checks
- Business actions referenced in rules exist in the registered actions
- Parameters match those defined in the business checks and actions

### Expression Validations

These validations focus on rule expressions:

- Syntax correctness (proper use of operators, parentheses, etc.)
- Correct parameter count for checks and actions
- Parameter type compatibility

### Semantic Validations

These validations check for logical issues:

- Non-circular dependencies
- Effective date ranges are valid (from date is before to date)
- Priority values are valid

## Validation Error Types

Axiom produces specific error types for different validation failures:

| Error Type | Description | Example |
|------------|-------------|---------|
| `MissingRequiredFieldError` | A required field is missing | Missing rule name or priority |
| `InvalidReferenceError` | Reference to a non-existent component | Check reference not found in registered checks |
| `ParameterMismatchError` | Parameter count or names don't match | Too many or too few parameters for a check |
| `ExpressionSyntaxError` | Syntax error in a rule expression | Missing 'then' keyword or unbalanced parentheses |
| `DateFormatError` | Invalid date format in effective dates | Date not in ISO-8601 format |
| `DuplicateIdentifierError` | Duplicate name within scope | Two rules with the same name |

## Error Handling

When validation errors occur, Axiom throws a `RuleSetValidationException` that contains detailed information about all validation failures. Here's how to handle it:

```java
try {
    RuleSet<MyContextKey> ruleSet = ruleSetLoader.load();
    // Use the rule set...
} catch (RuleSetValidationException e) {
    // Get all validation errors
    List<ValidationError> errors = e.getValidationErrors();
    
    // Log detailed information about each error
    errors.forEach(error -> {
        logger.error("Validation error: {} - {}",
            error.getErrorType(),
            error.getMessage());
            
        // If the error has a location in the YAML, log that too
        if (error.hasLocation()) {
            logger.error("  Location: line {}, column {}",
                error.getLocation().getLine(),
                error.getLocation().getColumn());
        }
    });
    
    // Take appropriate action (e.g., fail application startup)
    throw new ApplicationStartupException("Rule set validation failed", e);
}
```

## Validation Examples

### Example 1: Missing Check Reference

If a rule references a check that doesn't exist:

```yaml
rules:
  - name: "Invalid Rule"
    description: "References a non-existent check"
    expression: nonExistentCheck() then validAction()
    priority: 10
```

Validation error:
```
InvalidReferenceError: Check 'nonExistentCheck' referenced in rule 'Invalid Rule' not found in registered checks
```

### Example 2: Parameter Count Mismatch

If a check is called with the wrong number of parameters:

```yaml
businessChecks:
  - name: checkCustomerAge
    description: "Checks if customer is above age threshold"
    params:
      - ageThreshold

rules:
  - name: "Invalid Parameters"
    description: "Calls check with wrong parameter count"
    expression: checkCustomerAge(18, "extra") then validAction()
    priority: 10
```

Validation error:
```
ParameterMismatchError: Check 'checkCustomerAge' in rule 'Invalid Parameters' expects 1 parameters but was called with 2
```

### Example 3: Invalid Expression Syntax

If the rule expression has syntax errors:

```yaml
rules:
  - name: "Syntax Error"
    description: "Has syntax error in expression"
    expression: validCheck() validAction()  # Missing 'then' keyword
    priority: 10
```

Validation error:
```
ExpressionSyntaxError: Missing 'then' keyword in rule 'Syntax Error'
```

## Custom Validators

You can extend Axiom's validation framework with custom validators:

```java
public class CustomRuleSetValidator<T extends Enum<T>> implements RuleSetValidator<T> {
    
    @Override
    public List<ValidationError> validate(RuleSet<T> ruleSet, 
                                          Map<String, BusinessCheck<T>> checks,
                                          Map<String, BusinessAction<T>> actions) {
        List<ValidationError> errors = new ArrayList<>();
        
        // Custom validation logic
        if (ruleSet.getRules().size() > 100) {
            errors.add(new ValidationError(
                "TooManyRulesError",
                "Rule set contains more than 100 rules, which may impact performance",
                null  // No specific location
            ));
        }
        
        return errors;
    }
}
```

Register your custom validator:

```java
RuleSetLoader<MyContextKey> loader = new YamlRuleSetLoader<>("ruleset.yaml")
    .addValidator(new CustomRuleSetValidator<>());
```

## Best Practices

1. **Validate Early**: Validate rule sets during application startup to fail fast
2. **Detailed Logging**: Log detailed validation errors to help identify issues
3. **Testing**: Create tests that verify your rule sets pass validation
4. **CI/CD Integration**: Add validation to your CI/CD pipeline to catch issues before deployment
5. **Custom Validators**: Create custom validators for domain-specific requirements

[← Back to Rule Set Structure](ruleset-structure.md) 
