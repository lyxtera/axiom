package com.lyxtera.axiom.codegen;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MetadataCrossCheckTest {

    @TempDir
    Path tempDir;
    
    private Path testRuleSetPath;
    private Path outputDir;
    private static final String TEST_PACKAGE = "com.example.rules";
    private static final String TEST_CONTEXT_KEY_ENUM = "com.example.rules.TestContextKey";
    
    @BeforeEach
    void setUp() throws IOException {
        outputDir = tempDir.resolve("output");
        Files.createDirectories(outputDir);
        testRuleSetPath = Paths.get("high_value_customer_ruleset.yaml");
        
        // Ensure directories exist
        Path checksDir = outputDir.resolve(TEST_PACKAGE.replace('.', File.separatorChar) + File.separator + "checks");
        Path actionsDir = outputDir.resolve(TEST_PACKAGE.replace('.', File.separatorChar) + File.separator + "actions");
        Files.createDirectories(checksDir);
        Files.createDirectories(actionsDir);
    }

    private void writeJavaFile(String type, String className, String metadataName, String arguments) throws IOException {
        Path dir = outputDir.resolve(TEST_PACKAGE.replace('.', File.separatorChar) + File.separator + type);
        Files.createDirectories(dir);
        Path file = dir.resolve(className + ".java");
        String content = "package " + TEST_PACKAGE + "." + type + ";\n"
            + "import com.lyxtera.axiom.config.RuleMetadata;\n"
            + "import com.lyxtera.axiom.config.Arg;\n"
            + "@RuleMetadata(name = \"" + metadataName + "\", description = \"test\")\n"
            + "public class " + className + " {\n"
            + "    public void execute(" + arguments + ") {}\n"
            + "}\n";
        Files.writeString(file, content);
    }
    
    @Test
    void testCrossCheck_PassesWhenSynchronized() throws IOException {
        // High value ruleset has: isHighValueCustomer (check), hasLoyaltyStatus (check), applyDiscount (action)
        writeJavaFile("checks", "IsHighValueCustomerCheck", "isHighValueCustomer", "@Arg(\"thresholdAmount\") Value thresholdAmount");
        writeJavaFile("checks", "HasLoyaltyStatusCheck", "hasLoyaltyStatus", "");
        writeJavaFile("actions", "ApplyDiscountAction", "applyDiscount", "@Arg(\"percentage\") Value percentage");

        RuleStubGenerator generator = RuleStubGenerator.builder()
            .withBasePackage(TEST_PACKAGE)
            .withOutputDirectory(outputDir.toString())
            .addRuleSet(testRuleSetPath.toString())
            .withContextKeyEnum(TEST_CONTEXT_KEY_ENUM)
            .failBuildOnDivergedMetadata(true)
            .build();
            
        // Act - should not throw
        generator.generateStubs();
    }
    
    @Test
    void testCrossCheck_FailsWhenMissingImplementation() throws IOException {
        // Missing applyDiscount
        writeJavaFile("checks", "IsHighValueCustomerCheck", "isHighValueCustomer", "@Arg(\"thresholdAmount\") Value thresholdAmount");
        writeJavaFile("checks", "HasLoyaltyStatusCheck", "hasLoyaltyStatus", "");
        
        RuleStubGenerator generator = RuleStubGenerator.builder()
            .withBasePackage(TEST_PACKAGE)
            .withOutputDirectory(outputDir.toString())
            .addRuleSet(testRuleSetPath.toString())
            .withContextKeyEnum(TEST_CONTEXT_KEY_ENUM)
            .failBuildOnDivergedMetadata(true)
            .build();
            
        // Assert
        assertThatThrownBy(() -> generator.generateStubs())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("missing implementation in code");
    }

    @Test
    void testCrossCheck_FailsWhenOrphanImplementation() throws IOException {
        writeJavaFile("checks", "IsHighValueCustomerCheck", "isHighValueCustomer", "@Arg(\"thresholdAmount\") Value thresholdAmount");
        writeJavaFile("checks", "HasLoyaltyStatusCheck", "hasLoyaltyStatus", "");
        writeJavaFile("actions", "ApplyDiscountAction", "applyDiscount", "@Arg(\"percentage\") Value percentage");
        // Extra orphaned check
        writeJavaFile("checks", "GhostCheck", "ghostCheck", "");

        RuleStubGenerator generator = RuleStubGenerator.builder()
            .withBasePackage(TEST_PACKAGE)
            .withOutputDirectory(outputDir.toString())
            .addRuleSet(testRuleSetPath.toString())
            .withContextKeyEnum(TEST_CONTEXT_KEY_ENUM)
            .failBuildOnDivergedMetadata(true)
            .build();
            
        // Assert
        assertThatThrownBy(() -> generator.generateStubs())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("exists in code but is missing in ruleset");
    }

    @Test
    void testCrossCheck_FailsWhenArgumentsDiverge() throws IOException {
        // Wrong argument name "wrongArg" instead of "thresholdAmount"
        writeJavaFile("checks", "IsHighValueCustomerCheck", "isHighValueCustomer", "@Arg(\"wrongArg\") Value thresholdAmount");
        writeJavaFile("checks", "HasLoyaltyStatusCheck", "hasLoyaltyStatus", "");
        writeJavaFile("actions", "ApplyDiscountAction", "applyDiscount", "@Arg(\"percentage\") Value percentage");

        RuleStubGenerator generator = RuleStubGenerator.builder()
            .withBasePackage(TEST_PACKAGE)
            .withOutputDirectory(outputDir.toString())
            .addRuleSet(testRuleSetPath.toString())
            .withContextKeyEnum(TEST_CONTEXT_KEY_ENUM)
            .failBuildOnDivergedMetadata(true)
            .build();
            
        // Assert
        assertThatThrownBy(() -> generator.generateStubs())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("do not match");
    }

    @Test
    void testCrossCheck_IgnoredWhenFlagDisabled() throws IOException {
        // Missing applyDiscount but flag is disabled
        writeJavaFile("checks", "IsHighValueCustomerCheck", "isHighValueCustomer", "@Arg(\"thresholdAmount\") Value thresholdAmount");
        
        RuleStubGenerator generator = RuleStubGenerator.builder()
            .withBasePackage(TEST_PACKAGE)
            .withOutputDirectory(outputDir.toString())
            .addRuleSet(testRuleSetPath.toString())
            .withContextKeyEnum(TEST_CONTEXT_KEY_ENUM)
            .failBuildOnDivergedMetadata(false)
            .build();
            
        // Act - should not throw!
        int generated = generator.generateStubs();
        assertThat(generated).isGreaterThan(0);
    }
}
