package com.lyxtera.axiom.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.lyxtera.axiom.api.exception.AxiomEngineException;
import com.lyxtera.axiom.api.model.Value;

class CtxGetTest {

    enum TestKey {
        STRING_KEY,
        INTEGER_KEY,
        BOOLEAN_KEY,
        MISSING_KEY
    }
    
    private RuleContext<TestKey> context;
    private CtxGet<TestKey> ctxGet;
    
    @BeforeEach
    void setUp() {
        context = new RuleContext<>(TestKey.class);
        context.add(TestKey.STRING_KEY, "test value");
        context.add(TestKey.INTEGER_KEY, 42);
        context.add(TestKey.BOOLEAN_KEY, true);
        
        ctxGet = new CtxGet<>();
    }
    
    @Test
    void testExecute_WithStringValue() {
        Value result = ctxGet.execute(context, Value.of("STRING_KEY"));
        assertThat(result.getType()).isEqualTo(Value.Type.STRING);
        assertThat(result.getValue()).isEqualTo("test value");
    }
    
    @Test
    void testExecute_WithIntegerValue() {
        // Test that we get the correct value, not comparing exact numeric type
        Value result = ctxGet.execute(context, Value.of("INTEGER_KEY"));
        assertThat(result.getType()).isEqualTo(Value.Type.NUMBER);
        assertThat(result.getValue().toString()).isEqualTo("42");
    }
    
    @Test
    void testExecute_WithBooleanValue() {
        Value result = ctxGet.execute(context, Value.of("BOOLEAN_KEY"));
        assertThat(result.getType()).isEqualTo(Value.Type.BOOLEAN);
        assertThat(result.getValue()).isEqualTo(true);
    }
    
    @Test
    void testExecute_WithMissingKey() {
        // Execute the function with a missing key - should return empty value
        Value result = ctxGet.execute(context, Value.of("MISSING_KEY"));
        assertThat(result).isEqualTo(Value.EMPTY);
    }
    
    @Test
    void testExecute_WithNonStringKey() {
        assertThatThrownBy(() -> ctxGet.execute(context, Value.of(42)))
            .isInstanceOf(AxiomEngineException.class)
            .hasMessageContaining(AxiomEngineException.MSG_CTXGET_REQUIRES_STRING);
    }
    
    @Test
    void testExecute_WithNullKey() {
        // Null key should throw a NullPointerException
        assertThatThrownBy(() -> ctxGet.execute(context, null))
            .isInstanceOf(NullPointerException.class);
    }
} 