package com.lyxtera.axiom.api.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.lyxtera.axiom.engine.RuleContext;
import com.lyxtera.axiom.engine.RuleSet;

/**
 * Test class for {@link BusinessRule}.
 */
class BusinessRuleTest {

    public enum TestKey {
        TEST_KEY
    }

    @Mock
    private RuleContext<TestKey> context;

    @Mock
    private Condition<TestKey> condition;

    @Mock
    private BusinessAction<TestKey> action1;

    @Mock
    private BusinessAction<TestKey> action2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetName() {
        BusinessRule<TestKey> rule = new BusinessRule<>("TestRule", null, null);
        assertThat(rule.getName()).isEqualTo("TestRule");
    }

    @Test
    void testConstructorAndGetters() {
        // Create a business rule
        List<RuleFunction<TestKey>> actions = Arrays.asList(action1, action2);
        BusinessRule<TestKey> rule = new BusinessRule<>("test-rule", condition, actions);

        // Verify getters
        assertThat(rule.getName()).isEqualTo("test-rule");
        assertThat(rule.getCondition()).isEqualTo(condition);
        assertThat(rule.getActions()).isEqualTo(actions);
    }

    @Test
    void testEvaluateWithTrueCondition() {
        // Set up condition to return true
        when(condition.evaluate(context)).thenReturn(true);

        // Create a business rule
        List<RuleFunction<TestKey>> actions = Arrays.asList(action1, action2);
        BusinessRule<TestKey> rule = new BusinessRule<>("test-rule", condition, actions);

        // Evaluate the rule
        boolean result = rule.evaluate(context);

        // Verify that the rule evaluates to true and actions are executed
        assertThat(result).isTrue();
        verify(action1, times(1)).execute(context);
        verify(action2, times(1)).execute(context);
    }

    @Test
    void testEvaluateWithFalseCondition() {
        // Set up condition to return false
        when(condition.evaluate(context)).thenReturn(false);

        // Create a business rule
        List<RuleFunction<TestKey>> actions = Arrays.asList(action1, action2);
        BusinessRule<TestKey> rule = new BusinessRule<>("test-rule", condition, actions);

        // Evaluate the rule
        boolean result = rule.evaluate(context);

        // Verify that the rule evaluates to false and actions are not executed
        assertThat(result).isFalse();
        verify(action1, never()).execute(context);
        verify(action2, never()).execute(context);
    }

    @Test
    void testEvaluateWithNullCondition() {
        // Create a business rule with null condition
        List<RuleFunction<TestKey>> actions = Arrays.asList(action1, action2);
        BusinessRule<TestKey> rule = new BusinessRule<>("test-rule", null, actions);

        // Evaluate the rule
        boolean result = rule.evaluate(context);

        // Verify that the rule evaluates to true and actions are executed
        assertThat(result).isTrue();
        verify(action1, times(1)).execute(context);
        verify(action2, times(1)).execute(context);
    }

    @Test
    void testEvaluateWithEmptyActions() {
        // Set up condition to return true
        when(condition.evaluate(context)).thenReturn(true);

        // Create a business rule with empty actions
        List<RuleFunction<TestKey>> emptyActions = Collections.emptyList();
        BusinessRule<TestKey> rule = new BusinessRule<>("test-rule", condition, emptyActions);

        // Evaluate the rule
        boolean result = rule.evaluate(context);

        // Verify that the rule evaluates to true
        assertThat(result).isTrue();
    }

    @Test
    void testGateRuleMetadata() {
        BusinessRule<TestKey> rule = new BusinessRule<>("gate-rule", condition, Collections.emptyList(), "/child.yaml");
        RuleSet<TestKey> childRuleSet = new RuleSet<>();
        rule.setChildRuleSet(childRuleSet);

        assertThat(rule.isGateRule()).isTrue();
        assertThat(rule.isActionRule()).isFalse();
        assertThat(rule.getOnMatchForwardTo()).hasValue("/child.yaml");
        assertThat(rule.getChildRuleSet()).hasValue(childRuleSet);
    }
}
