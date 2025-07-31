package com.lyxtera.axiom.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.lyxtera.axiom.api.exception.RuleFunctionException;
import com.lyxtera.axiom.api.model.BusinessAction;
import com.lyxtera.axiom.api.model.RuleFunction;
import com.lyxtera.axiom.api.model.Value;
import com.lyxtera.axiom.config.Arg;
import com.lyxtera.axiom.config.RuleMetadata;

class ArgAwareRuleFunctionTest {

    public enum TestKey {
        TEST_KEY
    }

    private RuleSet.Metadata metadata;
    private RuleContext<TestKey> context;

    @BeforeEach
    void setUp() {
        metadata = new RuleSet.Metadata();
        metadata.setRuleSetName("TestRuleSet");
        context = new RuleContext<>(TestKey.class);
    }

    @Test
    void testExecute_WithNoArgs() {
        // Create a delegate function
        NoArgsFunction delegate = new NoArgsFunction();
        
        // Create an ArgAwareRuleFunction with no args
        ArgAwareRuleFunction<TestKey> function = ArgAwareRuleFunction.of(
            delegate, Collections.emptyList(), metadata);
        
        // Execute the function
        Value result = function.execute(context);
        
        // Verify that the delegate was called
        assertThat(result.getValue()).isEqualTo("noargs");
    }
    
    @Test
    void testExecute_WithOneArg() {
        // Create a delegate function
        OneArgFunction delegate = new OneArgFunction();
        
        // Create an ArgAwareRuleFunction with one arg
        ArgAwareRuleFunction<TestKey> function = ArgAwareRuleFunction.of(
            delegate, List.of(Value.of("test")), metadata);
        
        // Execute the function
        Value result = function.execute(context);
        
        // Verify that the delegate was called with the correct argument
        assertThat(result.getValue()).isEqualTo("test");
    }
    
    @Test
    void testExecute_WithMultipleArgs() {
        // Create a delegate function
        MultiArgFunction delegate = new MultiArgFunction();
        
        // Create an ArgAwareRuleFunction with multiple args
        ArgAwareRuleFunction<TestKey> function = ArgAwareRuleFunction.of(
            delegate, Arrays.asList(Value.of(10), Value.of("test")), metadata);
        
        // Execute the function
        Value result = function.execute(context);
        
        // Verify that the delegate was called with the correct arguments
        assertThat(result.getValue()).isEqualTo("10-test");
    }
    
    @Test
    void testExecute_WithDelegateException() {
        // Create a delegate function that throws an exception
        RuleFunction<TestKey> delegate = mock(RuleFunction.class);
        RuntimeException testException = new RuntimeException("Test exception");
        when(delegate.execute(context)).thenThrow(testException);
        
        // Create an ArgAwareRuleFunction
        ArgAwareRuleFunction<TestKey> function = ArgAwareRuleFunction.of(
            delegate, Collections.emptyList(), metadata);
        
        // Execute the function and verify it throws an exception
        assertThatThrownBy(() -> function.execute(context))
            .hasCause(testException)
            .hasMessageContaining("Test exception");
    }
    
    @Test
    void testOf_WithNullDelegate() {
        // Verify that creating an ArgAwareRuleFunction with null delegate throws an exception
        assertThatThrownBy(() -> ArgAwareRuleFunction.of(null, Collections.emptyList(), metadata))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("Delegate cannot be null");
    }
    
    @Test
    void testOf_WithNullArgs() {
        // Create a delegate function
        NoArgsFunction delegate = new NoArgsFunction();
        
        // Verify that creating an ArgAwareRuleFunction with null args throws an exception
        assertThatThrownBy(() -> ArgAwareRuleFunction.of(delegate, null, metadata))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("Args cannot be null");
    }
    
    @Test
    void testOf_WithNullMetadata() {
        // Create a delegate function
        NoArgsFunction delegate = new NoArgsFunction();
        
        // Verify that creating an ArgAwareRuleFunction with null metadata throws an exception
        assertThatThrownBy(() -> ArgAwareRuleFunction.of(delegate, Collections.emptyList(), null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("Metadata cannot be null");
    }
    
    // Test function implementations
    
    @RuleMetadata(name = "noArgsFunction", description = "Test function with no arguments")
    private static class NoArgsFunction implements BusinessAction<TestKey> {
        @Override
        public Value execute(RuleContext<TestKey> context) {
            return Value.of("noargs");
        }
    }
    
    @RuleMetadata(name = "oneArgFunction", description = "Test function with one argument")
    private static class OneArgFunction implements RuleFunction<TestKey> {
        public Value execute(RuleContext<TestKey> context, @Arg("param") Value param) {
            return param;
        }
        
        @Override
        public Value execute(RuleContext<TestKey> context) {
            throw new UnsupportedOperationException("This method should not be called directly");
        }
    }
    
    @RuleMetadata(name = "multiArgFunction", description = "Test function with multiple arguments")
    private static class MultiArgFunction implements RuleFunction<TestKey> {
        public Value execute(RuleContext<TestKey> context, @Arg("num") Value num, @Arg("str") Value str) {
            return Value.of(num.getValue() + "-" + str.getValue());
        }
        
        @Override
        public Value execute(RuleContext<TestKey> context) {
            throw new UnsupportedOperationException("This method should not be called directly");
        }
    }
} 