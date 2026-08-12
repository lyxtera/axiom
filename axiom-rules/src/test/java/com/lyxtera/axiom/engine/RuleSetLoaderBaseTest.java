package com.lyxtera.axiom.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.lyxtera.axiom.api.exception.RuleLoadException;
import com.lyxtera.axiom.api.model.BusinessRule;
import com.lyxtera.axiom.api.model.Condition;
import com.lyxtera.axiom.api.model.RuleSetDescriptor;
import com.lyxtera.axiom.api.parser.Parser;

@DisplayName("RuleSetLoader (base loadRuleSet)")
class RuleSetLoaderBaseTest {

    private enum TestKey {
        KEY
    }

    /**
     * Concrete loader that only provides a descriptor, so the inherited (base)
     * {@code loadRuleSet(Parser)} implementation is exercised.
     */
    private static class StubLoader<K extends Enum<K>> extends RuleSetLoader<K> {
        private final RuleSetDescriptor descriptor;

        StubLoader(RuleSetDescriptor descriptor) {
            this.descriptor = descriptor;
        }

        @Override
        public RuleSetDescriptor loadDescriptor() {
            return descriptor;
        }
    }

    @Test
    void loadRuleSet_populatesMetadataAndAddsRulesInPriorityOrder() {
        RuleSetDescriptor descriptor = descriptorWithRules("Rule A", "Rule B");

        RuleSet<TestKey> ruleSet = new StubLoader<TestKey>(descriptor).loadRuleSet(parserReturningNamedRules());

        // Metadata is populated from the descriptor
        assertThat(ruleSet.getMetadata().getRuleSetName()).isEqualTo("Test Rule Set");
        assertThat(ruleSet.getMetadata().getRuleSetDescription()).isEqualTo("Description");
        assertThat(ruleSet.getMetadata().isAllowDynamicExecution()).isTrue();
        assertThat(ruleSet.getMetadata().getBusinessCheckDescriptors()).containsKey("isHighValue");
        assertThat(ruleSet.getMetadata().getBusinessActionDescriptors()).containsKey("doAction");
        assertThat(ruleSet.getMetadata().getEntityPermissions()).isEmpty();

        // Rules are added and sorted ascending by priority (Rule A priority 1 first)
        List<BusinessRule<TestKey>> rules = ruleSet.getRulesInPriorityOrder();
        assertThat(rules).hasSize(2);
        assertThat(rules.get(0).getName()).isEqualTo("Rule A");
        assertThat(rules.get(1).getName()).isEqualTo("Rule B");
    }

    @Test
    void loadRuleSet_wrapsParseFailuresInRuleLoadException() {
        RuleSetDescriptor descriptor = descriptorWithRules("Broken Rule");
        Parser<TestKey> parser = mock(Parser.class);
        when(parser.parseRule(any(), anyString(), anyString())).thenThrow(new RuntimeException("boom"));

        assertThatThrownBy(() -> new StubLoader<TestKey>(descriptor).loadRuleSet(parser))
            .isInstanceOf(RuleLoadException.class)
            .hasMessageContaining("Failed to parse rule 'Broken Rule'")
            .hasCauseInstanceOf(RuntimeException.class);
    }

    private Parser<TestKey> parserReturningNamedRules() {
        Parser<TestKey> parser = mock(Parser.class);
        when(parser.parseRule(any(), anyString(), anyString())).thenAnswer(invocation -> {
            String name = invocation.getArgument(1, String.class);
            return new BusinessRule<>(name, mock(Condition.class), Collections.emptyList());
        });
        return parser;
    }

    private RuleSetDescriptor descriptorWithRules(String... ruleNames) {
        RuleSetDescriptor descriptor = new RuleSetDescriptor();
        descriptor.setRulesetName("Test Rule Set");
        descriptor.setRulesetDescription("Description");
        descriptor.setAllowDynamicExecution(true);

        RuleSetDescriptor.BusinessCheckDescriptor check = new RuleSetDescriptor.BusinessCheckDescriptor();
        check.setName("isHighValue");
        descriptor.setBusinessChecks(List.of(check));

        RuleSetDescriptor.BusinessActionDescriptor action = new RuleSetDescriptor.BusinessActionDescriptor();
        action.setName("doAction");
        descriptor.setBusinessActions(List.of(action));

        descriptor.setEntityPermissions(Collections.emptyList());

        int priority = 1;
        for (String ruleName : ruleNames) {
            RuleSetDescriptor.RuleDescriptor rule = new RuleSetDescriptor.RuleDescriptor();
            rule.setName(ruleName);
            rule.setExpression(ruleName + " => true");
            rule.setPriority(priority);
            rule.setEffectiveFrom(ZonedDateTime.now());
            descriptor.getRules().add(rule);
            priority++;
        }
        return descriptor;
    }
}
