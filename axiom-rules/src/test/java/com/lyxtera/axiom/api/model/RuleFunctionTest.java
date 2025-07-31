package com.lyxtera.axiom.api.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.lyxtera.axiom.api.exception.RuleFunctionException;
import com.lyxtera.axiom.config.RuleMetadata;
import com.lyxtera.axiom.engine.RuleContext;
import com.lyxtera.axiom.api.exception.AxiomEngineException;

class RuleFunctionTest {

    public enum TestKey {
        TEST_KEY
    }

    @Test
    void testGetName_WithAnnotation() {
        // Create a rule function with annotation
        TestRuleFunction function = new TestRuleFunction();
        
        // Verify that getName() returns the name from the annotation
        assertThat(function.getName()).isEqualTo("testFunction");
    }
    
    @Test
    void testGetName_WithoutAnnotation() {
        // Create a rule function without annotation
        RuleFunction<TestKey> function = new RuleFunction<TestKey>() {};
        
        // Verify that getName() throws an exception
        assertThatThrownBy(() -> function.getName())
            .isInstanceOf(RuleFunctionException.class)
            .hasMessageContaining(AxiomEngineException.MSG_MISSING_METADATA.split("%s")[0]);
    }
    
    @Test
    void testExecute_DefaultImplementation() {
        // Create a rule function using default implementation
        RuleFunction<TestKey> function = new RuleFunction<TestKey>() {};
        
        // Verify that execute() throws an exception
        assertThatThrownBy(() -> function.execute(new RuleContext<>(TestKey.class)))
            .isInstanceOf(RuleFunctionException.class)
            .hasMessageContaining(AxiomEngineException.MSG_FUNCTION_NOT_IMPLEMENTED);
    }
    
    @RuleMetadata(name = "testFunction", description = "Test function for unit tests")
    private static class TestRuleFunction implements RuleFunction<TestKey> {
        @Override
        public Value execute(RuleContext<TestKey> context) {
            return Value.of(true);
        }
    }
} 