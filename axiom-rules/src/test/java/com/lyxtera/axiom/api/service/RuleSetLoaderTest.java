package com.lyxtera.axiom.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.lyxtera.axiom.api.exception.RuleLoadException;
import com.lyxtera.axiom.api.model.BusinessAction;
import com.lyxtera.axiom.api.model.BusinessCheck;
import com.lyxtera.axiom.api.parser.DefaultParser;
import com.lyxtera.axiom.api.parser.Parser;
import com.lyxtera.axiom.engine.RuleSet;
import com.lyxtera.axiom.engine.RuleSetLoader.YamlRuleSetLoader;

@DisplayName("RuleSetLoader")
class RuleSetLoaderTest {

    private enum TestKey {
        TEST_KEY
    }

    @Test
    void loadRuleSet_supportsGateRulesAndLoadsChildRulesets() {
        RuleSet<TestKey> ruleSet = new YamlRuleSetLoader<TestKey>("gate_parent_ruleset.yaml").loadRuleSet(parser());

        assertThat(ruleSet.getRulesInPriorityOrder()).hasSize(2);
        assertThat(ruleSet.getRulesInPriorityOrder().get(0).isGateRule()).isTrue();
        assertThat(ruleSet.getRulesInPriorityOrder().get(0).getOnMatchForwardTo())
            .hasValue("/gate_child_ruleset.yaml");
        assertThat(ruleSet.getRulesInPriorityOrder().get(0).getChildRuleSet()).isPresent();
        assertThat(ruleSet.getRulesInPriorityOrder().get(0).getChildRuleSet().orElseThrow()
            .getRulesInPriorityOrder()).hasSize(1);
    }

    @Test
    void loadRuleSet_rejectsMixedActionAndForwardRule() {
        assertThatThrownBy(() -> new YamlRuleSetLoader<TestKey>("invalid_mixed_gate_ruleset.yaml").loadRuleSet(parser()))
            .isInstanceOf(RuleLoadException.class)
            .hasMessageContaining("cannot define both actions and onMatchForwardTo");
    }

    @Test
    void loadRuleSet_rejectsGateRuleWithoutForwardReference() {
        assertThatThrownBy(() -> new YamlRuleSetLoader<TestKey>("invalid_gate_no_ref_ruleset.yaml").loadRuleSet(parser()))
            .isInstanceOf(RuleLoadException.class)
            .hasMessageContaining("must define a non-empty onMatchForwardTo");
    }

    @Test
    void loadRuleSet_rejectsBlankForwardReference() {
        assertThatThrownBy(() -> new YamlRuleSetLoader<TestKey>("invalid_gate_blank_ref_ruleset.yaml").loadRuleSet(parser()))
            .isInstanceOf(RuleLoadException.class)
            .hasMessageContaining("must define a non-empty onMatchForwardTo");
    }

    @Test
    void loadRuleSet_rejectsCyclicForwarding() {
        assertThatThrownBy(() -> new YamlRuleSetLoader<TestKey>("cyclic_forward_a.yaml").loadRuleSet(parser()))
            .isInstanceOf(RuleLoadException.class)
            .hasMessageContaining("Cyclic ruleset forwarding detected");
    }

    private Parser<TestKey> parser() {
        Map<String, BusinessCheck<TestKey>> checks = new HashMap<>();
        checks.put("alwaysTrue", mock(BusinessCheck.class));
        checks.put("alwaysFalse", mock(BusinessCheck.class));

        Map<String, BusinessAction<TestKey>> actions = new HashMap<>();
        BusinessAction<TestKey> action = mock(BusinessAction.class);
        org.mockito.Mockito.when(action.getName()).thenReturn("doAction");
        actions.put("doAction", action);
        return new DefaultParser<>(checks, actions);
    }
}
