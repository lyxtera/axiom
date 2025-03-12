# Code Generation

This page describes how to use the code generation tools provided by Axiom to generate Java stub classes for business checks and actions from YAML rule set files.

## RuleStubGenerator

The `RuleStubGenerator` is a utility class that reads Axiom rule set YAML files and generates Java stub classes for the business checks and actions defined in those files. These generated classes provide a starting point for implementing your business logic.

### Command Line Usage

You can run the `RuleStubGenerator` directly from the command line using the following syntax:

```bash
java -cp my-classpath com.lyxtera.axiom.codegen.RuleStubGenerator \
  --basePackage=com.example.rules \
  --outputDirectory=src/main/java \
  --ruleSet=src/main/resources/rule-set1.yaml \
  --ruleSet=src/main/resources/rule-set2.yaml \
  --overwriteExisting=true
```

### Configuration Parameters

The `RuleStubGenerator` accepts the following command-line arguments:

- `--basePackage=<package>`: Base package for generated classes (required)
- `--outputDirectory=<dir>`: Output directory (default: src/main/java)
- `--ruleSet=<path>`: Path to rule set YAML file (can be specified multiple times)
- `--overwriteExisting=<true|false>`: Whether to overwrite existing files (default: false)

## Maven Integration

To integrate the `RuleStubGenerator` into your Maven build process, you can use the `exec-maven-plugin` to run the generator during the build. This approach allows you to automatically generate stub classes whenever you build your project.

Add the following plugin configuration to your POM file:

```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>exec-maven-plugin</artifactId>
    <version>3.1.0</version>
    <executions>
        <execution>
            <id>generate-rule-stubs</id>
            <phase>generate-sources</phase>
            <goals>
                <goal>java</goal>
            </goals>
            <configuration>
                <mainClass>com.lyxtera.axiom.codegen.RuleStubGenerator</mainClass>
                <arguments>
                    <argument>--basePackage=com.example.rules</argument>
                    <argument>--outputDirectory=src/main/java</argument>
                    <argument>--ruleSet=src/main/resources/rule-set1.yaml</argument>
                    <argument>--ruleSet=src/main/resources/rule-set2.yaml</argument>
                    <argument>--overwriteExisting=false</argument>
                </arguments>
                <includePluginDependencies>true</includePluginDependencies>
            </configuration>
        </execution>
    </executions>
    <dependencies>
        <dependency>
            <groupId>com.lyxtera</groupId>
            <artifactId>axiom</artifactId>
            <version>${axiom.version}</version>
        </dependency>
    </dependencies>
</plugin>
```

### Adding Generated Sources to Build

To ensure that the generated Java classes are included in the compilation process, you'll need to add the output directory to the build path. You can do this using the `build-helper-maven-plugin`:

```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>build-helper-maven-plugin</artifactId>
    <version>3.3.0</version>
    <executions>
        <execution>
            <id>add-source</id>
            <phase>generate-sources</phase>
            <goals>
                <goal>add-source</goal>
            </goals>
            <configuration>
                <sources>
                    <source>src/main/java</source>
                </sources>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## Generated Code Structure

The `RuleStubGenerator` creates two types of stub classes:

1. **Business Check Classes** - Placed in the `checks` package under your base package
2. **Business Action Classes** - Placed in the `actions` package under your base package

For example, given the following YAML ruleset:

```yaml
rulesetName: "High Value Approval Ruleset"
rulesetDescription: "Rules for determining whether suspensions require approval for high-value accounts"

businessChecks:
  - name: isEnterpriseCompany
    description: Determines if the organization is classified as an enterprise company
  - name: hasRevenueAboveThreshold
    description: Checks if the company's revenue is above a specified threshold
    params:
      - thresholdAmount

businessActions:
  - name: requireApproval
    description: Marks the suspension decision as requiring manual approval before proceeding
```

The generator will create the following Java classes:

1. `com.example.rules.checks.IsEnterpriseCompanyCheck.java`
2. `com.example.rules.checks.HasRevenueAboveThresholdCheck.java`
3. `com.example.rules.actions.RequireApprovalAction.java`

Each generated class includes:

- Proper package declaration
- Necessary imports
- Rule metadata annotations
- Empty implementation stubs for the business logic
- Javadoc comments based on the descriptions in the YAML file

[← Back to Rule Set Structure](ruleset-structure.md) 