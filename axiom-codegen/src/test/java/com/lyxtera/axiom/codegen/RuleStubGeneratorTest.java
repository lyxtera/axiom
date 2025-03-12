package com.lyxtera.axiom.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for {@link RuleStubGenerator}.
 */
class RuleStubGeneratorTest {

    @TempDir
    Path tempDir;
    
    private Path testRuleSetPath;
    private Path outputDir;
    private static final String TEST_PACKAGE = "com.example.rules";
    
    @BeforeEach
    void setUp() throws IOException {
        // Create a temporary directory for output
        outputDir = tempDir.resolve("output");
        Files.createDirectories(outputDir);
        
        // Create a test rule set file
        testRuleSetPath = tempDir.resolve("test_rules.yaml");
        String testRuleSet = "" +
            "rulesetName: \"Test Rule Set\"\n" +
            "rulesetDescription: \"Rule set for testing stub generator\"\n" +
            "\n" +
            "businessChecks:\n" +
            "  - name: isHighValueCustomer\n" +
            "    description: \"Checks if a customer is high value\"\n" +
            "    params:\n" +
            "      - threshold\n" +
            "  - name: hasLoyaltyStatus\n" +
            "    description: \"Checks if customer has a specific loyalty status\"\n" +
            "    params:\n" +
            "      - status\n" +
            "\n" +
            "businessActions:\n" +
            "  - name: applyDiscount\n" +
            "    description: \"Applies a discount to the order\"\n" +
            "    params:\n" +
            "      - percent\n" +
            "\n" +
            "rules:\n" +
            "  - name: \"Premium Customer Discount\"\n" +
            "    description: \"Apply discount to high value customers\"\n" +
            "    expression: isHighValueCustomer(500) then applyDiscount(10)\n" +
            "    priority: 10\n" +
            "  - name: \"Gold Status Discount\"\n" +
            "    description: \"Apply discount to gold status customers\"\n" +
            "    expression: hasLoyaltyStatus(\"GOLD\") then applyDiscount(5)\n" +
            "    priority: 20\n";
        
        Files.write(testRuleSetPath, testRuleSet.getBytes());
    }
    
    @Test
    void testGenerateStubs_CreatesExpectedFiles() throws IOException {
        // Arrange
        RuleStubGenerator generator = RuleStubGenerator.builder()
                .withBasePackage(TEST_PACKAGE)
                .withOutputDirectory(outputDir.toString())
                .addRuleSet(testRuleSetPath.toString())
                .build();
        
        // Act
        int filesGenerated = generator.generateStubs();
        
        // Assert
        assertEquals(3, filesGenerated, "Should generate 3 files (2 checks, 1 action)");
        
        // Check that expected files exist
        Path checksDir = outputDir.resolve(TEST_PACKAGE.replace('.', File.separatorChar) + File.separator + "checks");
        Path actionsDir = outputDir.resolve(TEST_PACKAGE.replace('.', File.separatorChar) + File.separator + "actions");
        
        Path highValueCustomerCheckFile = checksDir.resolve("IsHighValueCustomerCheck.java");
        Path hasLoyaltyStatusCheckFile = checksDir.resolve("HasLoyaltyStatusCheck.java");
        Path applyDiscountActionFile = actionsDir.resolve("ApplyDiscountAction.java");
        
        assertTrue(Files.exists(highValueCustomerCheckFile), "IsHighValueCustomerCheck.java should exist");
        assertTrue(Files.exists(hasLoyaltyStatusCheckFile), "HasLoyaltyStatusCheck.java should exist");
        assertTrue(Files.exists(applyDiscountActionFile), "ApplyDiscountAction.java should exist");
        
        // Verify content of one file
        String checkContent = new String(Files.readAllBytes(highValueCustomerCheckFile));
        assertTrue(checkContent.contains("package " + TEST_PACKAGE + ".checks"), "Should have correct package");
        assertTrue(checkContent.contains("implements BusinessCheck<ContextKey>"), "Should implement BusinessCheck");
        assertTrue(checkContent.contains("name = \"isHighValueCustomer\""), "Should have correct name in metadata");
        assertTrue(checkContent.contains("@Arg(\"threshold\")"), "Should have correct parameter annotation");
    }
    
    @Test
    void testGenerateStubs_SkipsExistingFiles() throws IOException {
        // Arrange
        // Create a directory and a file that already exists
        Path checksDir = outputDir.resolve(TEST_PACKAGE.replace('.', File.separatorChar) + File.separator + "checks");
        Files.createDirectories(checksDir);
        
        Path existingFile = checksDir.resolve("IsHighValueCustomerCheck.java");
        String existingContent = "// Existing custom implementation";
        Files.write(existingFile, existingContent.getBytes());
        
        RuleStubGenerator generator = RuleStubGenerator.builder()
                .withBasePackage(TEST_PACKAGE)
                .withOutputDirectory(outputDir.toString())
                .addRuleSet(testRuleSetPath.toString())
                .overwriteExisting(false) // Don't overwrite
                .build();
        
        // Act
        int filesGenerated = generator.generateStubs();
        
        // Assert
        assertEquals(2, filesGenerated, "Should generate 2 files, skipping the existing one");
        
        // Verify the existing file wasn't changed
        String fileContent = new String(Files.readAllBytes(existingFile));
        assertEquals(existingContent, fileContent, "Existing file should not be modified");
    }
    
    @Test
    void testGenerateStubs_OverwritesExistingFilesWhenConfigured() throws IOException {
        // Arrange
        // Create a directory and a file that already exists
        Path checksDir = outputDir.resolve(TEST_PACKAGE.replace('.', File.separatorChar) + File.separator + "checks");
        Files.createDirectories(checksDir);
        
        Path existingFile = checksDir.resolve("IsHighValueCustomerCheck.java");
        String existingContent = "// Existing custom implementation";
        Files.write(existingFile, existingContent.getBytes());
        
        RuleStubGenerator generator = RuleStubGenerator.builder()
                .withBasePackage(TEST_PACKAGE)
                .withOutputDirectory(outputDir.toString())
                .addRuleSet(testRuleSetPath.toString())
                .overwriteExisting(true) // Set to overwrite
                .build();
        
        // Act
        int filesGenerated = generator.generateStubs();
        
        // Assert
        assertEquals(3, filesGenerated, "Should generate all 3 files, overwriting the existing one");
        
        // Verify the existing file was changed
        String fileContent = new String(Files.readAllBytes(existingFile));
        assertNotEquals(existingContent, fileContent, "Existing file should be modified");
        assertTrue(fileContent.contains("@RuleMetadata"), "File should have been replaced with the generated stub");
    }
    
    @Test
    void testBuilder_RequiresBasePackage() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> RuleStubGenerator.builder()
                        .addRuleSet(testRuleSetPath.toString())
                        .build()
        );
        
        assertTrue(exception.getMessage().contains("Base package must be specified"));
    }
    
    @Test
    void testBuilder_RequiresRuleSet() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> RuleStubGenerator.builder()
                        .withBasePackage(TEST_PACKAGE)
                        .build()
        );
        
        assertTrue(exception.getMessage().contains("At least one rule set path must be specified"));
    }
} 