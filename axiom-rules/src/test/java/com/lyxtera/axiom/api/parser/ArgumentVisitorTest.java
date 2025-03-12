package com.lyxtera.axiom.api.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.antlr.v4.runtime.tree.TerminalNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.lyxtera.axiom.antlr.BusinessRuleParser.NumberLiteralContext;
import com.lyxtera.axiom.antlr.BusinessRuleParser.StringLiteralContext;
import com.lyxtera.axiom.api.model.Value;

/**
 * Test class for {@link ArgumentVisitor}.
 */
public class ArgumentVisitorTest {

    private ArgumentVisitor<ArgumentVisitorTest.TestKey> visitor;
    
    public enum TestKey {
        AGE,
        IS_PREMIUM,
        NAME
    }
    
    @BeforeEach
    public void setUp() {
        visitor = new ArgumentVisitor<>();
    }
    
    @Test
    public void testVisitNumberLiteral() {
        // Create mock context for an integer literal
        NumberLiteralContext intCtx = mock(NumberLiteralContext.class);
        TerminalNode intNode = mock(TerminalNode.class);
        when(intCtx.NUMBER()).thenReturn(intNode);
        when(intNode.getText()).thenReturn("42");
        
        // Visit the mock context
        Value result = visitor.visitNumberLiteral(intCtx);
        
        // Verify the result
        assertNotNull(result);
        assertEquals(42, result.getValue());
        assertEquals(Value.Type.INTEGER, result.getType());
        
        // Create mock context for a decimal literal
        NumberLiteralContext decimalCtx = mock(NumberLiteralContext.class);
        TerminalNode decimalNode = mock(TerminalNode.class);
        when(decimalCtx.NUMBER()).thenReturn(decimalNode);
        when(decimalNode.getText()).thenReturn("3.14");
        
        // Visit the mock context
        result = visitor.visitNumberLiteral(decimalCtx);
        
        // Verify the result
        assertNotNull(result);
        assertEquals(3.14, result.getValue());
        assertEquals(Value.Type.DECIMAL, result.getType());
    }
    
    @Test
    public void testVisitStringLiteral() {
        // Create mock context for a string literal
        StringLiteralContext ctx = mock(StringLiteralContext.class);
        TerminalNode stringNode = mock(TerminalNode.class);
        when(ctx.STRING()).thenReturn(stringNode);
        when(stringNode.getText()).thenReturn("\"Hello, world!\"");
        
        // Visit the mock context
        Value result = visitor.visitStringLiteral(ctx);
        
        // Verify the result
        assertNotNull(result);
        assertEquals("Hello, world!", result.getValue());
        assertEquals(Value.Type.STRING, result.getType());
    }
} 