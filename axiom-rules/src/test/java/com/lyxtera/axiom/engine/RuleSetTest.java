package com.lyxtera.axiom.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.ZonedDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.lyxtera.axiom.api.model.BusinessRule;

/**
 * Tests for the RuleSet class.
 */
public class RuleSetTest {
    
    private enum TestKey {
        TEST_KEY
    }
    
    private RuleSet<TestKey> ruleSet;
    private BusinessRule<TestKey> rule1;
    private BusinessRule<TestKey> rule2;
    private BusinessRule<TestKey> rule3;
    
    @BeforeEach
    public void setUp() {
        ruleSet = new RuleSet<>();
        
        // Create mock rules
        rule1 = mock(BusinessRule.class);
        rule2 = mock(BusinessRule.class);
        rule3 = mock(BusinessRule.class);
    }
    
    @Test
    public void testRulesInPriorityOrder() {
        // Add rules with different priorities
        ruleSet.addRule(rule1, 10, ZonedDateTime.now().minusDays(1));
        ruleSet.addRule(rule2, 5, ZonedDateTime.now().minusDays(1));
        ruleSet.addRule(rule3, 20, ZonedDateTime.now().minusDays(1));
        
        // Get rules in priority order
        List<BusinessRule<TestKey>> rules = ruleSet.getRulesInPriorityOrder();
        
        // Verify rules are in correct order (lowest priority number first)
        assertThat(rules)
            .hasSize(3)
            .containsExactly(rule2, rule1, rule3);
    }
    
    @Test
    public void testEffectiveFromFiltering() {
        // Add rules with different effective dates
        ruleSet.addRule(rule1, 10, ZonedDateTime.now().minusDays(1)); // Active
        ruleSet.addRule(rule2, 5, ZonedDateTime.now().plusDays(1));  // Not yet active
        ruleSet.addRule(rule3, 20, ZonedDateTime.now().minusDays(2)); // Active
        
        // Get rules in priority order
        List<BusinessRule<TestKey>> rules = ruleSet.getRulesInPriorityOrder();
        
        // Verify only active rules are included
        assertThat(rules)
            .hasSize(2)
            .contains(rule1, rule3);
    }
    
    @Test
    public void testEffectiveToFiltering() {
        // Add rules with different effective date ranges
        ruleSet.addRule(rule1, 10, ZonedDateTime.now().minusDays(2), ZonedDateTime.now().plusDays(1)); // Active
        ruleSet.addRule(rule2, 5, ZonedDateTime.now().minusDays(2), ZonedDateTime.now().minusDays(1)); // Expired
        ruleSet.addRule(rule3, 20, ZonedDateTime.now().minusDays(2), null); // Active (no end date)
        
        // Get rules in priority order
        List<BusinessRule<TestKey>> rules = ruleSet.getRulesInPriorityOrder();
        
        // Verify only active rules are included
        assertThat(rules)
            .hasSize(2)
            .contains(rule1, rule3);
    }
    
    @Test
    public void testEffectiveDateRangeOverlap() {
        // Create rules with overlapping effective date ranges
        ZonedDateTime now = ZonedDateTime.now();
        
        // Rule 1: Active from yesterday to tomorrow
        ruleSet.addRule(rule1, 10, now.minusDays(1), now.plusDays(1));
        
        // Rule 2: Active from 2 days ago to yesterday (just expired)
        ruleSet.addRule(rule2, 5, now.minusDays(2), now.minusDays(1));
        
        // Rule 3: Active starting tomorrow
        ruleSet.addRule(rule3, 20, now.plusDays(1), null);
        
        // Get rules in priority order
        List<BusinessRule<TestKey>> rules = ruleSet.getRulesInPriorityOrder();
        
        // Verify only currently active rules are included
        assertThat(rules)
            .hasSize(1)
            .containsExactly(rule1);
    }
} 