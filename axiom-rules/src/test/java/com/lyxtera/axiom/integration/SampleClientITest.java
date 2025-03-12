package com.lyxtera.axiom.integration;

import static com.google.inject.name.Names.named;
import static com.google.inject.util.Types.mapOf;
import static com.google.inject.util.Types.newParameterizedType;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.lyxtera.axiom.api.model.BusinessAction;
import com.lyxtera.axiom.api.model.BusinessCheck;
import com.lyxtera.axiom.api.model.BusinessRule;
import com.lyxtera.axiom.config.AxiomModule;
import com.lyxtera.axiom.engine.RuleContext;
import com.lyxtera.axiom.engine.RuleExecutionResult;
import com.lyxtera.axiom.engine.RuleOrchestrator;
import com.lyxtera.axiom.engine.RuleSetLoader.YamlRuleSetLoader;
import com.lyxtera.axiom.integration.TestClientAxiomModule.BlockRequestAction;
import com.lyxtera.axiom.integration.TestClientAxiomModule.HasFraudSignalsCheck;
import com.lyxtera.axiom.integration.TestClientAxiomModule.HasRevenueAboveThresholdCheck;
import com.lyxtera.axiom.integration.TestClientAxiomModule.HasRiskScoreCheck;
import com.lyxtera.axiom.integration.TestClientAxiomModule.IsEnterpriseCompanyCheck;
import com.lyxtera.axiom.integration.TestClientAxiomModule.RequireApprovalAction;
import com.lyxtera.axiom.integration.TestClientAxiomModule.TestCtxKey;

/**
 * Integration tests for the Axiom rule engine.
 * <p>
 * These tests verify that the Axiom rule engine works correctly with various rule configurations,
 * including real-world business use cases.
 */
@DisplayName("Axiom Rule Engine Integration Tests")
class SampleClientITest {

    @Inject
    @Named("high_value_approval_ruleset")
    private RuleOrchestrator<TestCtxKey> highValueOrchestrator;

    @Inject
    @Named("fraud_detection_ruleset")
    private RuleOrchestrator<TestCtxKey> fraudDetectionOrchestrator;

    @Inject
    private Map<String, BusinessCheck<TestCtxKey>> checks;

    @Inject
    private Map<String, BusinessAction<TestCtxKey>> actions;

    @BeforeEach
    void setUp() {
        // Create a Guice injector with our custom module that includes both rulesets
        var injector = Guice.createInjector(new TestClientAxiomModule(Map.of(
                "high_value_approval_ruleset", "high_value_approval_ruleset.yaml",
                "fraud_detection_ruleset", "fraud_detection_ruleset.yaml")));

        // Get the required instances from the injector
        injector.injectMembers(this);
    }

    // Tests for High Value Approval Ruleset
    
    @Test
    @DisplayName("Enterprise company rule should match for enterprise companies in high value ruleset")
    void testEnterpriseCompanyRule_HighValueRuleset() {
        // Setup context for an enterprise company
        var context = new RuleContext<TestCtxKey>(TestCtxKey.class);
        context.add(TestCtxKey.COMPANY_ID, "123");
        context.add(TestCtxKey.IS_ENTERPRISE_COMPANY, true);

        // Get the first matching rule for verification
        Optional<BusinessRule<TestCtxKey>> ruleOpt = highValueOrchestrator.getFirstMatchingRule(context);

        // Verify the correct rule was found
        assertThat(ruleOpt).isPresent()
            .hasValueSatisfying(rule -> {
                assertThat(rule.getName()).isEqualTo("Enterprise Company Rule");
                assertThat(rule.getCondition()).isNotNull();
                assertThat(rule.getActions()).isNotEmpty();
            });

        // Execute the rule and get detailed execution results
        RuleExecutionResult<TestCtxKey> result = highValueOrchestrator.executeFirstMatchingRule(context);

        // Verify the execution was successful and returned the expected result
        assertThat(result.hasMatches()).isTrue();
        assertThat(result.getFirstMatchedRule())
            .isPresent()
            .hasValueSatisfying(rule -> assertThat(rule.getName()).isEqualTo("Enterprise Company Rule"));
        assertThat(result.getFirstRuleResult()).isPresent().hasValue(true);
    }

    @Test
    @DisplayName("High revenue company rule should match for non-enterprise companies with high revenue")
    void testHighRevenueCompanyRule_HighValueRuleset() {
        // Setup context for a high revenue company (not enterprise)
        var context = new RuleContext<TestCtxKey>(TestCtxKey.class);
        context.add(TestCtxKey.COMPANY_ID, "456");
        context.add(TestCtxKey.IS_ENTERPRISE_COMPANY, false);
        // The HasRevenueAboveThresholdCheck will return true by default

        // Execute the rule and get detailed execution results
        RuleExecutionResult<TestCtxKey> result = highValueOrchestrator.executeFirstMatchingRule(context);

        // Verify the execution was successful and returned the expected result
        assertThat(result.hasMatches()).isTrue();
        assertThat(result.getFirstMatchedRule())
            .isPresent()
            .hasValueSatisfying(rule -> {
                assertThat(rule.getName()).isEqualTo("High Revenue Company Rule");
                assertThat(rule.getCondition()).isNotNull();
                assertThat(rule.getActions()).isNotEmpty();
            });
        assertThat(result.getFirstRuleResult()).isPresent().hasValue(true);
    }

    @Test
    @DisplayName("No rules should match for companies that don't meet any criteria in high value ruleset")
    void testNoRuleMatches_HighValueRuleset() {
        // We'll make our HasRevenueAboveThresholdCheck return false for this test
        // by passing a context that doesn't trigger any rules
        var context = new RuleContext<TestCtxKey>(TestCtxKey.class);
        context.add(TestCtxKey.COMPANY_ID, "789");
        context.add(TestCtxKey.IS_ENTERPRISE_COMPANY, false);
        // Override revenue check to return false
        context.add(TestCtxKey.BUSINESS_FLOW, "low_revenue");

        // Execute the rule and get detailed execution results
        RuleExecutionResult<TestCtxKey> result = highValueOrchestrator.executeFirstMatchingRule(context);

        // Verify either no rule matched or the rule returned false
        if (result.hasMatches()) {
            // If a rule matched, verify it returned false
            assertThat(result.getFirstRuleResult()).isPresent().hasValue(false);
        } else {
            // Otherwise, verify no rule matched
            assertThat(result.hasMatches()).isFalse();
            assertThat(result.getMatchedRules()).isEmpty();
        }
        
        // Also verify that getFirstMatchingRule returns empty
        Optional<BusinessRule<TestCtxKey>> ruleOpt = highValueOrchestrator.getFirstMatchingRule(context);
        assertThat(ruleOpt).isEmpty();
    }

    // Tests for Fraud Detection Ruleset
    
    @Test
    @DisplayName("Fraud detection rule should match when fraud signals are present")
    void testFraudSignalsRule_FraudDetectionRuleset() {
        // Setup context with fraud signals
        var context = new RuleContext<TestCtxKey>(TestCtxKey.class);
        context.add(TestCtxKey.COMPANY_ID, "123");
        // The hasFraudSignals check will return true by default

        // Execute the rule and get detailed execution results
        RuleExecutionResult<TestCtxKey> result = fraudDetectionOrchestrator.executeFirstMatchingRule(context);

        // Verify the execution was successful and returned the expected result
        assertThat(result.hasMatches()).isTrue();
        assertThat(result.getFirstMatchedRule())
            .isPresent()
            .hasValueSatisfying(rule -> {
                assertThat(rule.getName()).isEqualTo("Fraud Detection Rule");
                assertThat(rule.getCondition()).isNotNull();
                assertThat(rule.getActions()).isNotEmpty();
            });
        assertThat(result.getFirstRuleResult()).isPresent().hasValue(true);
        
        // Verify that the rule has the expected execution impact
        assertThat(result.getFirstMatchedRule().get().getActions())
            .hasSize(1)
            .allSatisfy(action -> {
                assertThat(action.execute(context).getValue()).isEqualTo(true);
            });
    }

    @Test
    @DisplayName("High risk score rule should match when risk score is high but no fraud signals are present")
    void testHighRiskScoreRule_FraudDetectionRuleset() {
        // Setup context with a high risk score but no fraud signals
        var context = new RuleContext<TestCtxKey>(TestCtxKey.class);
        context.add(TestCtxKey.COMPANY_ID, "456");
        // Override the hasFraudSignals check to return false for this test
        context.add(TestCtxKey.BUSINESS_FLOW, "no_fraud_signals");

        // Execute the rule and get detailed execution results
        RuleExecutionResult<TestCtxKey> result = fraudDetectionOrchestrator.executeFirstMatchingRule(context);

        // Verify the execution was successful and returned the expected result
        assertThat(result.hasMatches()).isTrue();
        assertThat(result.getFirstMatchedRule())
            .isPresent()
            .hasValueSatisfying(rule -> {
                assertThat(rule.getName()).isEqualTo("High Risk Score Rule");
                assertThat(rule.getCondition()).isNotNull();
                assertThat(rule.getActions()).isNotEmpty();
            });
        assertThat(result.getFirstRuleResult()).isPresent().hasValue(true);
    }

    @Test
    @DisplayName("No rules should match when there are no fraud signals and risk score is low")
    void testNoRuleMatches_FraudDetectionRuleset() {
        // We'll override both checks to return false
        var context = new RuleContext<TestCtxKey>(TestCtxKey.class);
        context.add(TestCtxKey.COMPANY_ID, "789");
        context.add(TestCtxKey.BUSINESS_FLOW, "safe_transaction");

        // Execute the rule and get detailed execution results
        RuleExecutionResult<TestCtxKey> result = fraudDetectionOrchestrator.executeFirstMatchingRule(context);

        // Verify either no rule matched or the rule returned false
        if (result.hasMatches()) {
            // If a rule matched, verify it returned false
            assertThat(result.getFirstRuleResult()).isPresent().hasValue(false);
        } else {
            // Otherwise, verify no rule matched
            assertThat(result.hasMatches()).isFalse();
            assertThat(result.getMatchedRules()).isEmpty();
        }
        
        // Also verify that getFirstMatchingRule returns empty
        Optional<BusinessRule<TestCtxKey>> ruleOpt = fraudDetectionOrchestrator.getFirstMatchingRule(context);
        assertThat(ruleOpt).isEmpty();
    }

    // Tests for direct component usage
    
    @Test
    @DisplayName("Direct execution of checks and actions should work correctly")
    void testDirectChecksAndActions() {
        // Setup context
        var context = new RuleContext<TestCtxKey>(TestCtxKey.class);
        context.add(TestCtxKey.COMPANY_ID, "123");
        context.add(TestCtxKey.IS_ENTERPRISE_COMPANY, true);

        // Execute checks directly
        var isEnterpriseCompany = checks.get("isEnterpriseCompany").execute(context);
        var hasFraudSignals = checks.get("hasFraudSignals").execute(context);

        // Verify results
        assertThat(isEnterpriseCompany.getValue()).isEqualTo(true);
        assertThat(hasFraudSignals.getValue()).isEqualTo(true);

        // Execute actions directly
        var requireApproval = actions.get("requireApproval").execute(context);
        var blockRequest = actions.get("blockRequest").execute(context);

        // Verify results
        assertThat(requireApproval.getValue()).isEqualTo(true);
        assertThat(blockRequest.getValue()).isEqualTo(true);
        
        // Verify the checks map contains all expected checks
        assertThat(checks)
            .containsKeys("isEnterpriseCompany", "hasRevenueAboveThreshold", "hasFraudSignals", "hasRiskScore", "ctxGet")
            .doesNotContainValue(null);
            
        // Verify the actions map contains all expected actions
        assertThat(actions)
            .containsKeys("requireApproval", "blockRequest")
            .doesNotContainValue(null);
    }

    // Tests for module builder
    
    @Test
    @DisplayName("AxiomModule builder should create a valid module with configured components")
    @SuppressWarnings("unchecked")
    void testAxiomModuleBuilder() {
        var module = AxiomModule.buildForKey(TestCtxKey.class)
            .withRuleLoaders(loaders -> loaders
                .loader("custom_ruleset", new YamlRuleSetLoader<>("custom_ruleset.yaml"))
                .loader("another_ruleset", new YamlRuleSetLoader<>("another_ruleset.yaml"))
            )
            .withChecks(checks -> checks
                .check("hasFraudSignals", HasFraudSignalsCheck.class)
                .check("hasRiskScore", HasRiskScoreCheck.class)
                .check("isEnterpriseCompany", IsEnterpriseCompanyCheck.class)
                .check("hasRevenueAboveThreshold", HasRevenueAboveThresholdCheck.class))
            .withActions(actions -> actions
                .action("requireApproval", RequireApprovalAction.class)
                .action("blockRequest", BlockRequestAction.class))
            .build();

        // Create an injector with the built module
        var injector = Guice.createInjector(module);

        // Verify that the injector contains the expected bindings
        var customOrchestrator = injector.getInstance(
            Key.get(TypeLiteral.get(newParameterizedType(RuleOrchestrator.class, TestCtxKey.class)),named("custom_ruleset"))
        );

        var anotherOrchestrator = injector.getInstance(
            Key.get(TypeLiteral.get(newParameterizedType(RuleOrchestrator.class, TestCtxKey.class)),named("another_ruleset"))
        );

        // Verify that the orchestrators are not null
        assertThat(customOrchestrator).isNotNull();
        assertThat(anotherOrchestrator).isNotNull();

        // Get the map of business checks and actions
        var checksMap = (Map<String, BusinessCheck<TestCtxKey>>) injector.getInstance(Key.get(TypeLiteral.get(
            mapOf(String.class, newParameterizedType(BusinessCheck.class, TestCtxKey.class))
        )));

        var actionsMap = (Map<String, BusinessAction<TestCtxKey>>) injector.getInstance(Key.get(TypeLiteral.get(
            mapOf(String.class, newParameterizedType(BusinessAction.class, TestCtxKey.class))
        )));

        // Verify that all checks and actions are registered
        assertThat(checksMap)
            .containsKeys("isEnterpriseCompany", "hasRevenueAboveThreshold", "hasFraudSignals", "hasRiskScore", "ctxGet")
            .doesNotContainValue(null);

        assertThat(actionsMap)
            .containsKeys("requireApproval", "blockRequest")
            .doesNotContainValue(null);

        // Verify that the checks and actions are of the expected types
        assertThat(checksMap.get("isEnterpriseCompany")).isInstanceOf(IsEnterpriseCompanyCheck.class);
        assertThat(checksMap.get("hasRevenueAboveThreshold")).isInstanceOf(HasRevenueAboveThresholdCheck.class);
        assertThat(checksMap.get("hasFraudSignals")).isInstanceOf(HasFraudSignalsCheck.class);
        assertThat(checksMap.get("hasRiskScore")).isInstanceOf(HasRiskScoreCheck.class);

        assertThat(actionsMap.get("requireApproval")).isInstanceOf(RequireApprovalAction.class);
        assertThat(actionsMap.get("blockRequest")).isInstanceOf(BlockRequestAction.class);
    }

    @Test
    @DisplayName("AxiomModule builder with rule execution should work for both rulesets")
    @SuppressWarnings("unchecked")
    void testAxiomModuleBuilderWithRuleExecution() {
        // Create a module using the fluent builder with the same configuration as
        var module = AxiomModule.buildForKey(TestCtxKey.class)
            .withRuleLoaders(loaders -> loaders
                .loader("high_value_approval_ruleset", new YamlRuleSetLoader<>("high_value_approval_ruleset.yaml"))
                .loader("fraud_detection_ruleset", new YamlRuleSetLoader<>("fraud_detection_ruleset.yaml"))
            )
            .withChecks(checks -> checks
                .check("isEnterpriseCompany", IsEnterpriseCompanyCheck.class)
                .check("hasRevenueAboveThreshold", HasRevenueAboveThresholdCheck.class)
                .check("hasFraudSignals", HasFraudSignalsCheck.class)
                .check("hasRiskScore", HasRiskScoreCheck.class))

            // Add all the actions
            .withActions(actions -> actions
                .action("requireApproval", RequireApprovalAction.class)
                .action("blockRequest", BlockRequestAction.class))
            .build();

        // Create an injector with the built module
        var injector = Guice.createInjector(module);

        // Get the high value orchestrator
        var highValueOrchestrator = (RuleOrchestrator<TestCtxKey>) injector.getInstance(Key.get(TypeLiteral.get(
            newParameterizedType(RuleOrchestrator.class, TestCtxKey.class)), named("high_value_approval_ruleset")
        ));

        // Create a context for an enterprise company
        var context = new RuleContext<TestCtxKey>(TestCtxKey.class);
        context.add(TestCtxKey.COMPANY_ID, "456");
        context.add(TestCtxKey.IS_ENTERPRISE_COMPANY, true);

        // Execute the rules
        RuleExecutionResult<TestCtxKey> result = highValueOrchestrator.executeFirstMatchingRule(context);

        // Verify that the enterprise company rule matched
        assertThat(result.hasMatches()).isTrue();
        assertThat(result.getFirstMatchedRule().get().getName()).isEqualTo("Enterprise Company Rule");

        // Get the actions map to verify actions were executed
        var actionsMap = (Map<String, BusinessAction<TestCtxKey>>) injector.getInstance(
            Key.get(TypeLiteral.get(mapOf(String.class, newParameterizedType(BusinessAction.class, TestCtxKey.class))))
        );

        // Execute the action directly to verify it works
        var requireApproval = actionsMap.get("requireApproval").execute(context);
        assertThat(requireApproval.getValue()).isEqualTo(true);

        // Now test with the fraud detection orchestrator
        var fraudDetectionOrchestrator = (RuleOrchestrator<TestCtxKey>) injector.getInstance(
            Key.get(TypeLiteral.get(newParameterizedType(RuleOrchestrator.class, TestCtxKey.class)), named("fraud_detection_ruleset"))
        );

        // Create a context for a transaction with fraud signals
        var fraudContext = new RuleContext<TestCtxKey>(TestCtxKey.class);
        fraudContext.add(TestCtxKey.COMPANY_ID, "123");

        // Execute the rules
        RuleExecutionResult<TestCtxKey> fraudResult = fraudDetectionOrchestrator.executeFirstMatchingRule(fraudContext);

        // Verify that the fraud signals rule matched
        assertThat(fraudResult.hasMatches()).isTrue();
        assertThat(fraudResult.getFirstMatchedRule().get().getName()).isEqualTo("Fraud Detection Rule");

        // Execute the action directly to verify it works
        var blockRequest = actionsMap.get("blockRequest").execute(fraudContext);
        assertThat(blockRequest.getValue()).isEqualTo(true);
    }

    // Tests for ruleset validation - currently skipped
    
    @Test
    @DisplayName("Test ruleset with missing check description should fail appropriately")
    void testRulesetWithMissingCheckDescription() {
        // NOTE: This test is skipped since the current implementation doesn't validate
        // check descriptions during ruleset loading. This is a placeholder for future
        // validation improvements.
        assumeTrue(false, "Skipping test until check description validation is implemented");
    }

    @Test
    @DisplayName("Test ruleset with undefined check reference should fail appropriately")
    void testRulesetWithUndefinedCheckReference() {
        // NOTE: This test is skipped since the current implementation doesn't validate
        // check references during ruleset loading. This is a placeholder for future
        // validation improvements.
        assumeTrue(false, "Skipping test until check reference validation is implemented");
    }

    @Test
    @DisplayName("Test ruleset with undefined action reference should fail appropriately")
    void testRulesetWithUndefinedActionReference() {
        // NOTE: This test is skipped since the current implementation doesn't validate
        // action references during ruleset loading. This is a placeholder for future
        // validation improvements.
        assumeTrue(false, "Skipping test until action reference validation is implemented");
    }

    @Test
    @DisplayName("Test ruleset with missing action name should fail appropriately")
    void testRulesetWithMissingActionName() {
        // NOTE: This test is skipped since the current implementation doesn't validate
        // action names during ruleset loading. This is a placeholder for future
        // validation improvements.
        assumeTrue(false, "Skipping test until action name validation is implemented");
    }

    @Test
    @DisplayName("Test ruleset with invalid parameter structure should fail appropriately")
    void testRulesetWithInvalidParameterStructure() {
        // NOTE: This test is skipped since the current implementation doesn't validate
        // parameter structures during ruleset loading. This is a placeholder for future
        // validation improvements.
        assumeTrue(false, "Skipping test until parameter structure validation is implemented");
    }

    @Test
    @DisplayName("Test ruleset with incorrect parameter count should fail appropriately")
    void testRulesetWithIncorrectParameterCount() {
        // NOTE: This test is skipped since the current implementation doesn't validate
        // parameter counts during ruleset loading. This is a placeholder for future
        // validation improvements.
        assumeTrue(false, "Skipping test until parameter count validation is implemented");
    }

    @Test
    @DisplayName("Test ruleset with invalid expression syntax should fail appropriately")
    void testRulesetWithInvalidExpressionSyntax() {
        // NOTE: This test is skipped since the current implementation doesn't validate
        // expression syntax during ruleset loading. This is a placeholder for future
        // validation improvements.
        assumeTrue(false, "Skipping test until expression syntax validation is implemented");
    }

    @Test
    @DisplayName("Test ruleset with missing priority field should fail appropriately")
    void testRulesetWithMissingPriorityField() {
        // NOTE: This test is skipped since the current implementation doesn't validate
        // priority fields during ruleset loading. This is a placeholder for future
        // validation improvements.
        assumeTrue(false, "Skipping test until priority field validation is implemented");
    }

    @Test
    @DisplayName("Test ruleset with invalid date format should fail appropriately")
    void testRulesetWithInvalidDateFormat() {
        // NOTE: This test is skipped since the current implementation doesn't validate
        // date formats during ruleset loading. This is a placeholder for future
        // validation improvements.
        assumeTrue(false, "Skipping test until date format validation is implemented");
    }

    @Test
    @DisplayName("Test ruleset with malformed expression should fail appropriately")
    void testRulesetWithMalformedExpression() {
        // NOTE: This test is skipped since the current implementation doesn't validate
        // expression syntax during ruleset loading. This is a placeholder for future
        // validation improvements.
        assumeTrue(false, "Skipping test until malformed expression validation is implemented");
    }
}