# Code Generation

This page describes how to use the code generation maven plugin to generate Java stub classes for business checks and actions from the YAML rule-set files.

## Axiom Codegen Maven Plugin

The `axiom-codegen` Maven plugin provides a simple way to generate Java stub classes for your business checks and actions defined in Axiom rule set YAML files. This plugin will automatically:

1. Generate Java classes for all business checks and actions in your rule set YAML files
2. Place them in appropriate packages based on your configuration
3. Add the generated sources to your project's compilation path

### Adding the Plugin to Your Project

To use the axiom-codegen plugin in your project, add the following configuration to your `pom.xml` file:

```xml
<plugin>
    <groupId>com.lyxtera</groupId>
    <artifactId>axiom-codegen</artifactId>
    <version>${axiom.version}</version>
    <executions>
        <execution>
            <id>generate-stubs</id>
            <goals>
                <goal>generate-stubs</goal>
            </goals>
            <configuration>
                <packageName>com.example.rules</packageName>
                <contextKeyEnum>com.example.rules.MyContextKey</contextKeyEnum>
                <ruleSets>${project.basedir}/src/main/resources/my_ruleset.yaml</ruleSets>
                <outputDirectory>src/main/java/</outputDirectory>
                <overwriteExisting>true</overwriteExisting>
                <skip>false</skip>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### Plugin Configuration Parameters

The plugin accepts the following configuration parameters:

| Parameter | Property | Description | Default | Required |
|-----------|----------|-------------|---------|----------|
| `packageName` | `axiom.stubs.package` | Base package for generated classes | - | Yes |
| `contextKeyEnum` | `axiom.stubs.contextKeyEnum` | Fully qualified name of the context enum class | - | Yes |
| `ruleSets` | `axiom.stubs.ruleSets` | Comma-separated list of rule set YAML files to process | - | Yes |
| `outputDirectory` | `axiom.stubs.outputDirectory` | Directory to output generated sources to | `${project.build.directory}/generated-sources/axiom` | No |
| `overwriteExisting` | `axiom.stubs.overwriteExisting` | Whether to overwrite existing files | `false` | No |
| `skip` | `axiom.stubs.skip` | Skip the rule stub generation | `false` | No |

### Example Configuration

Here's a complete example from the `axiom-examples` project:

```xml
<plugin>
    <groupId>com.lyxtera</groupId>
    <artifactId>axiom-codegen</artifactId>
    <version>${project.version}</version>
    <executions>
        <execution>
            <id>generate-axiom-stubs</id>
            <phase>generate-sources</phase>
            <goals>
                <goal>generate-stubs</goal>
            </goals>
            <configuration>
                <packageName>com.lyxtera.axiom.examples.rules</packageName>
                <contextKeyEnum>com.lyxtera.axiom.examples.rules.CustomerContextKey</contextKeyEnum>
                <ruleSets>${project.basedir}/src/main/resources/customer_discount_ruleset.yaml</ruleSets>
                <outputDirectory>src/main/java/</outputDirectory>
                <overwriteExisting>true</overwriteExisting>
                <skip>false</skip>
            </configuration>
        </execution>
    </executions>
</plugin>
```

This configuration will process the `customer_discount_ruleset.yaml` file and generate Java stub classes in the `com.lyxtera.axiom.examples.rules` package.

### Using Properties in Configuration

You can also use Maven properties to configure the plugin, making it easier to manage configurations across different environments:

```xml
<properties>
    <axiom.stubs.skip>false</axiom.stubs.skip>
    <axiom.stubs.package>com.example.rules</axiom.stubs.package>
    <axiom.stubs.contextKeyEnum>com.example.rules.MyContextKey</axiom.stubs.contextKeyEnum>
</properties>

<plugin>
    <groupId>com.lyxtera</groupId>
    <artifactId>axiom-codegen</artifactId>
    <version>${axiom.version}</version>
    <executions>
        <execution>
            <id>generate-axiom-stubs</id>
            <phase>generate-sources</phase>
            <goals>
                <goal>generate-stubs</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

## Generated Code Structure

The `axiom-codegen` plugin creates two types of stub classes:

1. **Business Check Classes** - Placed in the `checks` package under your base package
2. **Business Action Classes** - Placed in the `actions` package under your base package

For example, given the following YAML ruleset:

```yaml
rulesetName: "Customer Discount Ruleset"
rulesetDescription: "Rules for applying discounts to customers"

businessChecks:
  - name: isHighValueCustomer
    description: Determines if the customer has high spending for the past N days
    params:
      - spendingThreshold
      - days
  - name: hasLoyaltyStatus
    description: Checks if the customer has loyalty status
    params:
      - loyaltyLevel

businessActions:
  - name: applyDiscount
    description: Applies a discount to the customer's order
    params:
      - percentage
```

The generator will create the following Java classes:

1. `com.example.rules.checks.IsHighValueCustomerCheck.java`
2. `com.example.rules.checks.HasLoyaltyStatusCheck.java`
3. `com.example.rules.actions.ApplyDiscountAction.java`

Each generated class includes:

- Proper package declaration
- Necessary imports
- Rule metadata annotations
- Empty implementation stubs for the business logic
- Javadoc comments based on the descriptions in the YAML file

[← Back to Rule Set Structure](ruleset-structure.md) 