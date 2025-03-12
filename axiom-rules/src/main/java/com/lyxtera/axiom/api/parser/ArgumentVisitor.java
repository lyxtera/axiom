package com.lyxtera.axiom.api.parser;

import com.lyxtera.axiom.antlr.BusinessRuleBaseVisitor;
import com.lyxtera.axiom.antlr.BusinessRuleParser;
import com.lyxtera.axiom.api.model.Value;

/**
 * Specialized visitor for parsing arguments in function calls.
 * <p>
 * This visitor traverses the parse tree for literal values and constructs
 * {@link Value} objects that can be used as arguments to business checks and actions.
 * It handles different types of literals such as numbers and strings.
 *
 * @param <K> The enum type to be used as context keys
 */
public class ArgumentVisitor<K extends Enum<K>> extends BusinessRuleBaseVisitor<Value> {
    
    /**
     * Visits a number literal node in the parse tree and constructs a {@link Value} object.
     * <p>
     * This method parses the number literal and determines whether it's an integer or a decimal.
     *
     * @param ctx The number literal context from the parse tree
     * @return A Value object representing the number literal
     */
    @Override
    public Value visitNumberLiteral(BusinessRuleParser.NumberLiteralContext ctx) {
        String text = ctx.NUMBER().getText();
        if (text.contains(".")) {
            return new Value(Double.parseDouble(text), Value.Type.DECIMAL);
        } else {
            return new Value(Integer.parseInt(text), Value.Type.INTEGER);
        }
    }
    
    /**
     * Visits a string literal node in the parse tree and constructs a {@link Value} object.
     * <p>
     * This method parses the string literal and removes the surrounding quotes.
     *
     * @param ctx The string literal context from the parse tree
     * @return A Value object representing the string literal
     */
    @Override
    public Value visitStringLiteral(BusinessRuleParser.StringLiteralContext ctx) {
        String text = ctx.STRING().getText();
        // Remove quotes from string literal
        String value = text.substring(1, text.length() - 1);
        return new Value(value, Value.Type.STRING);
    }
} 