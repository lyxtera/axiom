package com.lyxtera.axiom.api.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Mockito;

import com.lyxtera.axiom.engine.RuleSet;
import com.lyxtera.axiom.api.exception.AxiomEngineException;

/**
 * Test class for {@link RuleSet}.
 */
class RuleSetTest {

    public enum TestKey {
        TEST_KEY
    }

    @Mock
    private BusinessRule<TestKey> rule1;

    @Mock
    private BusinessRule<TestKey> rule2;

    @Mock
    private BusinessRule<TestKey> rule3;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(rule1.getName()).thenReturn("Rule1");
        when(rule2.getName()).thenReturn("Rule2");
        when(rule3.getName()).thenReturn("Rule3");
    }

    @Test
    void testConstructor() {
        RuleSet<TestKey> ruleSet = new RuleSet<>();
        assertThat(ruleSet.getRulesInPriorityOrder()).isEmpty();
    }

    @Test
    void testAddRule() {
        RuleSet<TestKey> ruleSet = new RuleSet<>();
        
        // Get access to the package-private addRule method via reflection
        try {
            java.lang.reflect.Method addRuleMethod = RuleSet.class.getDeclaredMethod("addRule", 
                BusinessRule.class, int.class, java.time.ZonedDateTime.class, java.time.ZonedDateTime.class);
            addRuleMethod.setAccessible(true);
            
            // Add rules with different priorities
            addRuleMethod.invoke(ruleSet, rule1, 1, java.time.ZonedDateTime.now().minusDays(1), null);
            addRuleMethod.invoke(ruleSet, rule2, 2, java.time.ZonedDateTime.now().minusDays(1), null);
            addRuleMethod.invoke(ruleSet, rule3, 3, java.time.ZonedDateTime.now().minusDays(1), null);
            
            // Verify that rules are added and sorted by priority
            List<BusinessRule<TestKey>> rules = ruleSet.getRulesInPriorityOrder();
            assertThat(rules).hasSize(3);
            assertThat(rules.get(0)).isEqualTo(rule1); // Priority 1 (highest)
            assertThat(rules.get(1)).isEqualTo(rule2); // Priority 2
            assertThat(rules.get(2)).isEqualTo(rule3); // Priority 3 (lowest)
        } catch (Exception e) {
            throw new RuntimeException("Failed to access addRule method", e);
        }
    }

    @Test
    public void testAddRuleWithInvalidPriority() {
        RuleSet<TestKey> ruleSet = new RuleSet<>();
        BusinessRule<TestKey> rule1 = Mockito.mock(BusinessRule.class);
        
        try {
            java.lang.reflect.Method addRuleMethod = RuleSet.class.getDeclaredMethod("addRule", 
                BusinessRule.class, int.class, java.time.ZonedDateTime.class, java.time.ZonedDateTime.class);
            addRuleMethod.setAccessible(true);
            
            // Try to add a rule with priority 0
            assertThatThrownBy(() -> {
                try {
                    addRuleMethod.invoke(ruleSet, rule1, 0, java.time.ZonedDateTime.now(), null);
                } catch (java.lang.reflect.InvocationTargetException e) {
                    throw e.getCause();
                }
            }).isInstanceOf(AxiomEngineException.class)
              .hasMessageContaining("Priority must be at least 1");
            
            // Try to add a rule with negative priority
            assertThatThrownBy(() -> {
                try {
                    addRuleMethod.invoke(ruleSet, rule1, -1, java.time.ZonedDateTime.now(), null);
                } catch (java.lang.reflect.InvocationTargetException e) {
                    throw e.getCause();
                }
            }).isInstanceOf(AxiomEngineException.class)
              .hasMessageContaining("Priority must be at least 1");
        } catch (Exception e) {
            throw new RuntimeException("Failed to access addRule method", e);
        }
    }

    @Test
    void testRuleOrderWithSamePriority() {
        RuleSet<TestKey> ruleSet = new RuleSet<>();
        
        try {
            java.lang.reflect.Method addRuleMethod = RuleSet.class.getDeclaredMethod("addRule", 
                BusinessRule.class, int.class, java.time.ZonedDateTime.class, java.time.ZonedDateTime.class);
            addRuleMethod.setAccessible(true);
            
            // Add rules with the same priority
            addRuleMethod.invoke(ruleSet, rule1, 1, java.time.ZonedDateTime.now().minusDays(1), null);
            addRuleMethod.invoke(ruleSet, rule2, 1, java.time.ZonedDateTime.now().minusDays(1), null);
            addRuleMethod.invoke(ruleSet, rule3, 1, java.time.ZonedDateTime.now().minusDays(1), null);
            
            // Verify that rules with the same priority maintain their insertion order
            List<BusinessRule<TestKey>> rules = ruleSet.getRulesInPriorityOrder();
            assertThat(rules).hasSize(3);
            assertThat(rules.get(0)).isEqualTo(rule1);
            assertThat(rules.get(1)).isEqualTo(rule2);
            assertThat(rules.get(2)).isEqualTo(rule3);
        } catch (Exception e) {
            throw new RuntimeException("Failed to access addRule method", e);
        }
    }

    @Test
    void testRuleOrderWithReversePriority() {
        RuleSet<TestKey> ruleSet = new RuleSet<>();
        
        try {
            java.lang.reflect.Method addRuleMethod = RuleSet.class.getDeclaredMethod("addRule", 
                BusinessRule.class, int.class, java.time.ZonedDateTime.class, java.time.ZonedDateTime.class);
            addRuleMethod.setAccessible(true);
            
            // Add rules in reverse priority order
            addRuleMethod.invoke(ruleSet, rule1, 3, java.time.ZonedDateTime.now().minusDays(1), null);
            addRuleMethod.invoke(ruleSet, rule2, 2, java.time.ZonedDateTime.now().minusDays(1), null);
            addRuleMethod.invoke(ruleSet, rule3, 1, java.time.ZonedDateTime.now().minusDays(1), null);
            
            // Verify that rules are sorted by priority regardless of insertion order
            List<BusinessRule<TestKey>> rules = ruleSet.getRulesInPriorityOrder();
            assertThat(rules).hasSize(3);
            assertThat(rules.get(0)).isEqualTo(rule3); // Priority 1 (highest)
            assertThat(rules.get(1)).isEqualTo(rule2); // Priority 2
            assertThat(rules.get(2)).isEqualTo(rule1); // Priority 3 (lowest)
        } catch (Exception e) {
            throw new RuntimeException("Failed to access addRule method", e);
        }
    }

    @Test
    void testGetRulesReturnsUnmodifiableList() {
        RuleSet<TestKey> ruleSet = new RuleSet<>();
        
        try {
            java.lang.reflect.Method addRuleMethod = RuleSet.class.getDeclaredMethod("addRule", 
                BusinessRule.class, int.class, java.time.ZonedDateTime.class, java.time.ZonedDateTime.class);
            addRuleMethod.setAccessible(true);
            
            // Add a rule
            addRuleMethod.invoke(ruleSet, rule1, 1, java.time.ZonedDateTime.now().minusDays(1), null);
            
            // Get the rules list
            List<BusinessRule<TestKey>> rules = ruleSet.getRulesInPriorityOrder();
            
            // Verify that the list is unmodifiable
            assertThatThrownBy(() -> rules.add(rule2))
                .isInstanceOf(UnsupportedOperationException.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to access addRule method", e);
        }
    }
} 