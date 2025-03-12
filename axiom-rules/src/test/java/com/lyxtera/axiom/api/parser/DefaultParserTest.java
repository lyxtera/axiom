package com.lyxtera.axiom.api.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.reflect.Method;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.lyxtera.axiom.api.model.BusinessAction;
import com.lyxtera.axiom.api.model.BusinessCheck;
import com.lyxtera.axiom.api.model.BusinessRule;
import com.lyxtera.axiom.api.model.Condition;
import com.lyxtera.axiom.api.model.Value;
import com.lyxtera.axiom.engine.RuleSet;
import com.lyxtera.axiom.engine.RuleContext;

/**
 * Test class for {@link DefaultParser}.
 */
public class DefaultParserTest {

    public enum TestKey {
        AGE,
        IS_PREMIUM,
        NAME
    }
    
    private Map<String, BusinessCheck<TestKey>> businessChecks;
    private Map<String, BusinessAction<TestKey>> businessActions;
    private DefaultParser<TestKey> parser;
    
    @BeforeEach
    public void setUp() {
        businessChecks = new HashMap<>();
        businessActions = new HashMap<>();
        
        // Set up a mock business check for age
        BusinessCheck<TestKey> ageCheck = mock(BusinessCheck.class);
        when(ageCheck.getName()).thenReturn("age");
        // Mock the execute method with an argument
        try {
            Method executeMethod = BusinessCheck.class.getMethod("execute", RuleContext.class);
            when(executeMethod.invoke(ageCheck, any(RuleContext.class))).thenReturn(new Value(30, Value.Type.INTEGER));
        } catch (Exception e) {
            // Ignore for test setup
        }
        businessChecks.put("age", ageCheck);
        
        // Set up a mock business check for isPremium
        BusinessCheck<TestKey> isPremiumCheck = mock(BusinessCheck.class);
        when(isPremiumCheck.getName()).thenReturn("isPremium");
        // Mock the execute method without arguments
        try {
            Method executeMethod = BusinessCheck.class.getMethod("execute", RuleContext.class);
            when(executeMethod.invoke(isPremiumCheck, any(RuleContext.class))).thenReturn(new Value(true, Value.Type.BOOLEAN));
        } catch (Exception e) {
            // Ignore for test setup
        }
        businessChecks.put("isPremium", isPremiumCheck);
        
        // Set up a mock business action for notify
        BusinessAction<TestKey> notifyAction = mock(BusinessAction.class);
        when(notifyAction.getName()).thenReturn("notify");
        // Mock the execute method without arguments
        try {
            Method executeMethod = BusinessAction.class.getMethod("execute", RuleContext.class);
            when(executeMethod.invoke(notifyAction, any(RuleContext.class))).thenReturn(new Value(true, Value.Type.BOOLEAN));
        } catch (Exception e) {
            // Ignore for test setup
        }
        businessActions.put("notify", notifyAction);
        
        parser = new DefaultParser<>(businessChecks, businessActions);
    }
    
    private RuleSet.Metadata createMockMetadata() {
        RuleSet.Metadata metadata = new RuleSet.Metadata();
        try {
            // Use reflection to set the ruleset name since it has a package-private setter
            java.lang.reflect.Method setRuleSetNameMethod = RuleSet.Metadata.class.getDeclaredMethod("setRuleSetName", String.class);
            setRuleSetNameMethod.setAccessible(true);
            setRuleSetNameMethod.invoke(metadata, "TestRuleSet");
        } catch (Exception e) {
            // In case reflection fails, use mocking as a fallback
            metadata = mock(RuleSet.Metadata.class);
            when(metadata.getRuleSetName()).thenReturn("TestRuleSet");
        }
        return metadata;
    }
    
    @Test
    public void testParseSimpleRule() {
        String ruleName = "testRule";
        String ruleExpression = "isPremium() then notify()";
        
        RuleSet.Metadata metadata = createMockMetadata();
        
        BusinessRule<TestKey> rule = parser.parseRule(metadata, ruleName, ruleExpression);
        
        assertThat(rule).isNotNull();
        assertThat(rule.getName()).isEqualTo(ruleName);
        assertThat(rule.getCondition()).isNotNull();
        assertThat(rule.getActions()).hasSize(1);
    }
    
    @Test
    public void testParseComplexRule() {
        String ruleName = "complexRule";
        String ruleExpression = "isPremium() and isPremium() then notify()";
        
        RuleSet.Metadata metadata = createMockMetadata();
        
        BusinessRule<TestKey> rule = parser.parseRule(metadata, ruleName, ruleExpression);
        
        assertThat(rule).isNotNull();
        assertThat(rule.getName()).isEqualTo(ruleName);
        assertThat(rule.getCondition()).isNotNull();
        assertThat(rule.getActions()).hasSize(1);
    }
    
    @Test
    public void testParseRuleWithInvalidSyntax() {
        String ruleName = "invalidRule";
        String ruleExpression = "this is not a valid rule expression";
        
        RuleSet.Metadata metadata = createMockMetadata();
        assertThrows(RuntimeException.class, () -> parser.parseRule(metadata, ruleName, ruleExpression));
    }
} 