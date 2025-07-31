package com.lyxtera.axiom.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.lyxtera.axiom.api.exception.AxiomEngineException;
import com.lyxtera.axiom.api.exception.RuleException;
import com.lyxtera.axiom.api.model.BusinessRule;
import com.lyxtera.axiom.api.model.Condition;
import com.lyxtera.axiom.api.model.RuleFunction;
import com.lyxtera.axiom.api.model.RuleSetDescriptor.BusinessActionDescriptor;
import com.lyxtera.axiom.api.model.RuleSetDescriptor.BusinessCheckDescriptor;

/**
 * Tests for the RuleSet class.
 */
public class RuleSetTest {
    
    public enum TestKey {
        TEST_KEY
    }
    
    private RuleSet<TestKey> ruleSet;
    private BusinessRule<TestKey> rule1;
    private BusinessRule<TestKey> rule2;
    private BusinessRule<TestKey> rule3;
    private BusinessRule<TestKey> expiredRule;
    private BusinessRule<TestKey> futureRule;
    private ZonedDateTime now;
    private ZonedDateTime past;
    private ZonedDateTime future;
    
    @BeforeEach
    void setUp() {
        // Create a rule set
        ruleSet = new RuleSet<>();
        
        // Create mock rules
        rule1 = createMockRule("Rule1");
        rule2 = createMockRule("Rule2");
        rule3 = createMockRule("Rule3");
        expiredRule = createMockRule("ExpiredRule");
        futureRule = createMockRule("FutureRule");
        
        // Create dates for testing
        now = ZonedDateTime.now();
        past = now.minusDays(30);
        future = now.plusDays(30);
    }
    
    @Test
    void testAddRuleWithPriority() {
        // Add rules with different priorities
        ruleSet.addRule(rule1, 10, now);
        ruleSet.addRule(rule2, 20, now);
        ruleSet.addRule(rule3, 5, now);
        
        // Get rules in priority order
        List<BusinessRule<TestKey>> rules = ruleSet.getRulesInPriorityOrder();
        
        // Verify that rules are in priority order (lowest first)
        assertThat(rules).hasSize(3);
        assertThat(rules.get(0)).isEqualTo(rule3);
        assertThat(rules.get(1)).isEqualTo(rule1);
        assertThat(rules.get(2)).isEqualTo(rule2);
    }
    
    @Test
    void testAddRuleWithInvalidPriority() {
        // Attempt to add a rule with invalid priority
        assertThatThrownBy(() -> ruleSet.addRule(rule1, 0, now))
            .isInstanceOf(RuleException.class)
            .hasMessageContaining(AxiomEngineException.MSG_INVALID_PRIORITY);
        
        assertThatThrownBy(() -> ruleSet.addRule(rule1, -1, now))
            .isInstanceOf(RuleException.class)
            .hasMessageContaining(AxiomEngineException.MSG_INVALID_PRIORITY);
    }
    
    @Test
    void testAddRuleWithEffectiveDates() {
        // Add a rule effective now
        ruleSet.addRule(rule1, 10, now);
        
        // Add a rule that expired in the past
        ruleSet.addRule(expiredRule, 20, past, past.plusDays(1));
        
        // Add a rule effective in the future
        ruleSet.addRule(futureRule, 30, future);
        
        // Get effective rules
        List<BusinessRule<TestKey>> rules = ruleSet.getRulesInPriorityOrder();
        
        // Verify that only the current rule is included
        assertThat(rules).hasSize(1);
        assertThat(rules.get(0)).isEqualTo(rule1);
    }
    
    @Test
    void testGetMetadata() {
        // Create metadata
        RuleSet.Metadata metadata = new RuleSet.Metadata();
        metadata.setRuleSetName("TestRuleSet");
        metadata.setRuleSetDescription("A test rule set");
        
        // Add business check descriptors
        BusinessCheckDescriptor checkDescriptor = new BusinessCheckDescriptor();
        checkDescriptor.setName("testCheck");
        metadata.setBusinessCheckDescriptors(List.of(checkDescriptor));
        
        // Add business action descriptors
        BusinessActionDescriptor actionDescriptor = new BusinessActionDescriptor();
        actionDescriptor.setName("testAction");
        metadata.setBusinessActionDescriptors(List.of(actionDescriptor));
        
        // Set metadata
        ruleSet.setMetadata(metadata);
        
        // Verify that metadata is set correctly
        assertThat(ruleSet.getMetadata()).isEqualTo(metadata);
        assertThat(ruleSet.getMetadata().getRuleSetName()).isEqualTo("TestRuleSet");
        assertThat(ruleSet.getMetadata().getRuleSetDescription()).isEqualTo("A test rule set");
        assertThat(ruleSet.getMetadata().getBusinessCheckDescriptor("testCheck")).isEqualTo(checkDescriptor);
        assertThat(ruleSet.getMetadata().getBusinessActionDescriptor("testAction")).isEqualTo(actionDescriptor);
    }
    
    @Test
    void testValidate() {
        // Create a rule set with metadata
        RuleSet.Metadata metadata = new RuleSet.Metadata();
        metadata.setRuleSetName("TestRuleSet");
        
        // Add business action descriptors for the rule's actions
        BusinessActionDescriptor actionDescriptor = new BusinessActionDescriptor();
        actionDescriptor.setName("testAction");
        metadata.setBusinessActionDescriptors(List.of(actionDescriptor));
        
        ruleSet.setMetadata(metadata);
        
        // Add at least one rule to make validation pass
        ruleSet.addRule(rule1, 10, now);
        
        // Validate the rule set (should not throw an exception)
        ruleSet.validate();
        
        // Test for a rule set with empty ruleset name
        RuleSet<TestKey> emptyNameRuleSet = new RuleSet<>();
        RuleSet.Metadata emptyNameMetadata = new RuleSet.Metadata();
        emptyNameRuleSet.setMetadata(emptyNameMetadata);
        
        // Add a rule to avoid "empty rule set" error
        emptyNameRuleSet.addRule(rule2, 10, now);
        
        // Attempt to validate the rule set with empty name
        assertThatThrownBy(() -> emptyNameRuleSet.validate())
            .isInstanceOf(RuleException.class)
            .hasMessageContaining("Rule set name is required");
        
        // Test for completely empty ruleset (no rules)
        RuleSet<TestKey> emptyRuleSet = new RuleSet<>();
        
        // Attempt to validate a completely empty ruleset
        assertThatThrownBy(() -> emptyRuleSet.validate())
            .isInstanceOf(RuleException.class)
            .hasMessageContaining("Rule set is empty");
    }
    
    // Helper method to create a mock rule
    private BusinessRule<TestKey> createMockRule(String name) {
        @SuppressWarnings("unchecked")
        BusinessRule<TestKey> rule = mock(BusinessRule.class);
        when(rule.getName()).thenReturn(name);
        
        // Add a condition to pass validation
        @SuppressWarnings("unchecked")
        Condition<TestKey> condition = mock(Condition.class);
        when(rule.getCondition()).thenReturn(condition);
        
        // Add actions to pass the validation
        @SuppressWarnings("unchecked")
        RuleFunction<TestKey> action = mock(RuleFunction.class);
        when(action.getName()).thenReturn("testAction");
        when(rule.getActions()).thenReturn(List.of(action));
        
        return rule;
    }
} 