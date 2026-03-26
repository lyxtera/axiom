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

class RuleStubGeneratorTest {

    @TempDir
    Path tempDir;

    private Path testRuleSetPath;
    private Path outputDir;
    private static final String TEST_PACKAGE = "com.example.rules";
    private static final String TEST_CONTEXT_KEY_ENUM = "com.example.rules.TestContextKey";

    @BeforeEach
    void setUp() throws IOException {
        // Create a temporary directory for output
        outputDir = tempDir.resolve("output");
        Files.createDirectories(outputDir);
        testRuleSetPath = Paths.get("high_value_customer_ruleset.yaml");
    }

    @Test
    void testGenerateStubs_CreatesExpectedFiles() throws IOException {
        // Arrange
        RuleStubGenerator generator = RuleStubGenerator.builder()
                .withBasePackage(TEST_PACKAGE)
                .withOutputDirectory(outputDir.toString())
                .addRuleSet(testRuleSetPath.toString())
                .withContextKeyEnum(TEST_CONTEXT_KEY_ENUM)
                .failBuildOnDivergedMetadata(false)
                .build();

        // Act
        int filesGenerated = generator.generateStubs();

        // Assert
        assertThat(filesGenerated).as("Should generate 3 files (2 checks, 1 action)").isEqualTo(3);

        // Check that expected files exist
        Path checksDir = outputDir.resolve(TEST_PACKAGE.replace('.', File.separatorChar) + File.separator + "checks");
        Path actionsDir = outputDir.resolve(TEST_PACKAGE.replace('.', File.separatorChar) + File.separator + "actions");

        Path highValueCustomerCheckFile = checksDir.resolve("IsHighValueCustomerCheck.java");
        Path hasLoyaltyStatusCheckFile = checksDir.resolve("HasLoyaltyStatusCheck.java");
        Path applyDiscountActionFile = actionsDir.resolve("ApplyDiscountAction.java");

        assertThat(highValueCustomerCheckFile).as("IsHighValueCustomerCheck.java should exist").exists();
        assertThat(hasLoyaltyStatusCheckFile).as("HasLoyaltyStatusCheck.java should exist").exists();
        assertThat(applyDiscountActionFile).as("ApplyDiscountAction.java should exist").exists();

        // Verify content of one file
        String checkContent = new String(Files.readAllBytes(highValueCustomerCheckFile));
        System.out.println("Generated file content:");
        System.out.println(checkContent);

        assertThat(checkContent)
                .as("Should have correct package")
                .contains("package " + TEST_PACKAGE + ".checks");
        assertThat(checkContent)
                .as("Should implement BusinessCheck")
                .contains("implements BusinessCheck<TestContextKey>");
        assertThat(checkContent)
                .as("Should have correct name in metadata")
                .contains("name = \"isHighValueCustomer\"");
        assertThat(checkContent)
                .as("Should have correct parameter annotation")
                .contains("@Arg(\"thresholdAmount\")");

        // The import in the generated code has "import
        // com.example.rules.TestTestContextKey;"
        // but our assertion checks for "import com.example.rules.TestContextKey;"
        String expectedImport = "import " + TEST_CONTEXT_KEY_ENUM + ";";
        assertThat(checkContent)
                .as("Should import the context key enum")
                .contains(expectedImport);
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
                .withContextKeyEnum(TEST_CONTEXT_KEY_ENUM)
                .failBuildOnDivergedMetadata(false)
                .build();

        // Act
        int filesGenerated = generator.generateStubs();

        // Assert
        assertThat(filesGenerated).as("Should generate 2 files, skipping the existing one").isEqualTo(2);

        // Verify the existing file wasn't changed
        String fileContent = new String(Files.readAllBytes(existingFile));
        assertThat(fileContent).as("Existing file should not be modified").isEqualTo(existingContent);
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
                .withContextKeyEnum(TEST_CONTEXT_KEY_ENUM)
                .failBuildOnDivergedMetadata(false)
                .build();

        // Act
        int filesGenerated = generator.generateStubs();

        // Assert
        assertThat(filesGenerated).as("Should generate all 3 files, overwriting the existing one").isEqualTo(3);

        // Verify the existing file was changed
        String fileContent = new String(Files.readAllBytes(existingFile));
        assertThat(fileContent)
                .as("Existing file should be modified")
                .isNotEqualTo(existingContent)
                .contains("@RuleMetadata");
    }

    @Test
    void testBuilder_RequiresBasePackage() {
        // Act & Assert
        assertThatThrownBy(() -> RuleStubGenerator.builder()
                .addRuleSet(testRuleSetPath.toString())
                .withContextKeyEnum(TEST_CONTEXT_KEY_ENUM)
                .failBuildOnDivergedMetadata(false)
                .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Base package must be specified");
    }

    @Test
    void testBuilder_RequiresRuleSet() {
        // Act & Assert
        assertThatThrownBy(() -> RuleStubGenerator.builder()
                .withBasePackage(TEST_PACKAGE)
                .withContextKeyEnum(TEST_CONTEXT_KEY_ENUM)
                .failBuildOnDivergedMetadata(false)
                .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("At least one rule set path must be specified");
    }

    @Test
    void testBuilder_RequiresContextKeyEnum() {
        // Act & Assert
        assertThatThrownBy(() -> RuleStubGenerator.builder()
                .withBasePackage(TEST_PACKAGE)
                .addRuleSet(testRuleSetPath.toString())
                .failBuildOnDivergedMetadata(false)
                .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Context key enum must be specified");
    }

    @Test
    void testGenerateStubs_WithCrossCheckEnabled_FailsOnDivergence() throws IOException {
        // Arrange
        RuleStubGenerator generator = RuleStubGenerator.builder()
                .withBasePackage(TEST_PACKAGE)
                .withOutputDirectory(outputDir.toString())
                .addRuleSet(testRuleSetPath.toString())
                .withContextKeyEnum(TEST_CONTEXT_KEY_ENUM)
                .failBuildOnDivergedMetadata(true) // Enable cross-check
                .build();

        // Act & Assert - this will trigger MetadataCrossChecker
        // It should fail since the implementations don't exist yet in the test
        // environment classpath
        assertThatThrownBy(() -> generator.generateStubs())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Metadata divergence");
    }
}