package com.lyxtera.axiom.codegen;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration test for {@link RuleStubGenerator} using a real ruleset YAML file.
 */
class RuleStubGeneratorIT {

    private static final String TEST_RULESET_PATH = "src/test/resources/high_value_customer_ruleset.yaml";
    private static final String OUTPUT_PACKAGE = "com.lyxtera.axiom.stubs";
    private static final String OUTPUT_DIR = "target/test-generated-sources";
    private static final String TEST_CONTEXT_KEY_ENUM = "com.lyxtera.axiom.stubs.CustomerContextKey";
	private static final String STUBS_OUTPUT_PATH = "com/lyxtera/axiom/stubs/";

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
			.withContextKeyEnum(TEST_CONTEXT_KEY_ENUM)
			.build();
		
		// When
		int filesGenerated = generator.generateStubs();
		
		// Then
		assertThat(filesGenerated).isEqualTo(3); // 2 checks + 1 action
		
		// Verify generated check files
		Path isHighValueCustomerCheck = outputDir.resolve(STUBS_OUTPUT_PATH + "checks/IsHighValueCustomerCheck.java");
		Path hasLoyaltyStatusCheck = outputDir.resolve(STUBS_OUTPUT_PATH + "checks/HasLoyaltyStatusCheck.java");
		Path applyDiscountAction = outputDir.resolve(STUBS_OUTPUT_PATH + "actions/ApplyDiscountAction.java");
		
		assertThat(isHighValueCustomerCheck).exists();
		assertThat(hasLoyaltyStatusCheck).exists();
		assertThat(applyDiscountAction).exists();
		
		// Verify content of generated files
		String isHighValueCustomerContent = Files.readString(isHighValueCustomerCheck);
		String hasLoyaltyStatusContent = Files.readString(hasLoyaltyStatusCheck);
		String applyDiscountContent = Files.readString(applyDiscountAction);
		
		// Check package declaration
		assertThat(isHighValueCustomerContent).contains("package com.lyxtera.axiom.stubs.checks");
		assertThat(hasLoyaltyStatusContent).contains("package com.lyxtera.axiom.stubs.checks");
		assertThat(applyDiscountContent).contains("package com.lyxtera.axiom.stubs.actions");
		
		// Check class names
		assertThat(isHighValueCustomerContent).contains("public class IsHighValueCustomerCheck");
		assertThat(hasLoyaltyStatusContent).contains("public class HasLoyaltyStatusCheck");
		assertThat(applyDiscountContent).contains("public class ApplyDiscountAction");
		
		// Check descriptions from YAML are included
		assertThat(isHighValueCustomerContent)
			.contains("Determines if the customer has high spending patterns")
			.contains("thresholdAmount");

		assertThat(hasLoyaltyStatusContent)
			.contains("Checks if the customer has loyalty status");
			
		assertThat(applyDiscountContent)
			.contains("Applies a discount to the customer's order");
		
		// Check context key enum is properly included
		assertThat(isHighValueCustomerContent).contains("import " + TEST_CONTEXT_KEY_ENUM + ";");
		assertThat(hasLoyaltyStatusContent).contains("import " + TEST_CONTEXT_KEY_ENUM + ";");
		assertThat(applyDiscountContent).contains("import " + TEST_CONTEXT_KEY_ENUM + ";");
		
		// Check that BusinessCheck and BusinessAction use the correct context key type
		String contextKeySimpleName = TEST_CONTEXT_KEY_ENUM.substring(TEST_CONTEXT_KEY_ENUM.lastIndexOf('.') + 1);
		assertThat(isHighValueCustomerContent).contains("implements BusinessCheck<" + contextKeySimpleName + ">");
		assertThat(hasLoyaltyStatusContent).contains("implements BusinessCheck<" + contextKeySimpleName + ">");
		assertThat(applyDiscountContent).contains("implements BusinessAction<" + contextKeySimpleName + ">");
    }
} 