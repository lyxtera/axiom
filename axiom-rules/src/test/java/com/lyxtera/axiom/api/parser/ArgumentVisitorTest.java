package com.lyxtera.axiom.api.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.antlr.v4.runtime.tree.TerminalNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.lyxtera.axiom.antlr.BusinessRuleParser.NumberLiteralContext;
import com.lyxtera.axiom.antlr.BusinessRuleParser.StringLiteralContext;
import com.lyxtera.axiom.api.model.Value;

/**
 * Test class for {@link ArgumentVisitor}.
 */
class ArgumentVisitorTest {

    private ArgumentVisitor<ArgumentVisitorTest.TestKey> visitor;
    
    public enum TestKey {
        AGE,
        IS_PREMIUM,
        NAME
    }
    
    @BeforeEach
    void setUp() {
        visitor = new ArgumentVisitor<>();
    }
    
    @Test
    void testVisitNumberLiteral() {
        // Create mock context for an integer literal
        NumberLiteralContext intCtx = mock(NumberLiteralContext.class);
        TerminalNode intNode = mock(TerminalNode.class);
        when(intCtx.NUMBER()).thenReturn(intNode);
        when(intNode.getText()).thenReturn("42");
        
        // Visit the mock context
        Value result = visitor.visitNumberLiteral(intCtx);
        
        // Verify the result
        assertThat(result).isNotNull();
        assertThat(result.getValue()).isEqualTo(BigDecimal.valueOf(42));
        assertThat(result.getType()).isEqualTo(Value.Type.NUMBER);
        
        // Create mock context for a decimal literal
        NumberLiteralContext decimalCtx = mock(NumberLiteralContext.class);
        TerminalNode decimalNode = mock(TerminalNode.class);
        when(decimalCtx.NUMBER()).thenReturn(decimalNode);
        when(decimalNode.getText()).thenReturn("3.14");
        
        // Visit the mock context
        result = visitor.visitNumberLiteral(decimalCtx);
        
        // Verify the result
        assertThat(result).isNotNull();
        assertThat(result.getValue()).isEqualTo(BigDecimal.valueOf(3.14));
        assertThat(result.getType()).isEqualTo(Value.Type.NUMBER);
    }
    
    @Test
    void testVisitStringLiteral() {
        // Create mock context for a string literal
        StringLiteralContext ctx = mock(StringLiteralContext.class);
        TerminalNode stringNode = mock(TerminalNode.class);
        when(ctx.STRING()).thenReturn(stringNode);
        when(stringNode.getText()).thenReturn("\"Hello, world!\"");
        
        // Visit the mock context
        Value result = visitor.visitStringLiteral(ctx);
        
        // Verify the result
        assertThat(result).isNotNull();
        assertThat(result.getValue()).isEqualTo("Hello, world!");
        assertThat(result.getType()).isEqualTo(Value.Type.STRING);
    }
} 