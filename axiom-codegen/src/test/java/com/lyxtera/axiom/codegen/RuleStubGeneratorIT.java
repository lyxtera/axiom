package com.lyxtera.axiom.codegen;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Integration test for {@link RuleStubGenerator} using a real ruleset YAML file.
 */
public class RuleStubGeneratorIT {

    private static final String TEST_RULESET_PATH = "src/test/resources/high_value_approval_ruleset.yaml";
    private static final String OUTPUT_PACKAGE = "com.lyxtera.axiom.stubs";
    private static final String OUTPUT_DIR = "target/test-generated-sources";
    
    @TempDir
    Path tempDir;
    
    private Path outputDir;
    
    @BeforeEach
    void setup() {
	// Create output directory
	outputDir = Paths.get(OUTPUT_DIR);
	File dir = outputDir.toFile();
	if (!dir.exists()) {
	    dir.mkdirs();
	}
    }
    
    @Test
    void shouldGenerateStubsFromHighValueApprovalRuleset() throws Exception {
	// Given
	RuleStubGenerator generator = RuleStubGenerator.builder()
	    .withBasePackage(OUTPUT_PACKAGE)
	    .withOutputDirectory(OUTPUT_DIR)
	    .addRuleSet(TEST_RULESET_PATH)
	    .overwriteExisting(true)
	    .build();
	
	// When
	int filesGenerated = generator.generateStubs();
	
	// Then
	assertThat(filesGenerated).isEqualTo(3); // 2 checks + 1 action
	
	// Verify generated check files
	Path isEnterpriseCompanyCheck = outputDir.resolve(
		"com/lyxtera/axiom/stubs/checks/IsEnterpriseCompanyCheck.java");
	Path hasRevenueAboveThresholdCheck = outputDir.resolve(
		"com/lyxtera/axiom/stubs/checks/HasRevenueAboveThresholdCheck.java");
	Path requireApprovalAction = outputDir.resolve(
		"com/lyxtera/axiom/stubs/actions/RequireApprovalAction.java");
	
	assertThat(isEnterpriseCompanyCheck).exists();
	assertThat(hasRevenueAboveThresholdCheck).exists();
	assertThat(requireApprovalAction).exists();
	
	// Verify content of generated files
	String isEnterpriseCompanyContent = Files.readString(isEnterpriseCompanyCheck);
	String hasRevenueAboveThresholdContent = Files.readString(hasRevenueAboveThresholdCheck);
	String requireApprovalContent = Files.readString(requireApprovalAction);
	
	// Check package declaration
	assertThat(isEnterpriseCompanyContent).contains("package com.lyxtera.axiom.stubs.checks");
	assertThat(hasRevenueAboveThresholdContent).contains("package com.lyxtera.axiom.stubs.checks");
	assertThat(requireApprovalContent).contains("package com.lyxtera.axiom.stubs.actions");
	
	// Check class names
	assertThat(isEnterpriseCompanyContent).contains("public class IsEnterpriseCompanyCheck");
	assertThat(hasRevenueAboveThresholdContent).contains("public class HasRevenueAboveThresholdCheck");
	assertThat(requireApprovalContent).contains("public class RequireApprovalAction");
	
	// Check descriptions from YAML are included
	assertThat(isEnterpriseCompanyContent).contains(
		"Determines if the organization is classified as an enterprise company");
	assertThat(hasRevenueAboveThresholdContent).contains(
		"Checks if the company's revenue is above a specified threshold");
	assertThat(requireApprovalContent).contains(
		"Marks the suspension decision as requiring manual approval before proceeding");
	
	// Check that the parameter for hasRevenueAboveThreshold is included
	assertThat(hasRevenueAboveThresholdContent).contains("thresholdAmount");
    }
} 