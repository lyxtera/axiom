package com.lyxtera.axiom.api.parser;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import com.lyxtera.axiom.antlr.BusinessRuleLexer;
import com.lyxtera.axiom.antlr.BusinessRuleParser;
import com.lyxtera.axiom.antlr.BusinessRuleParser.BusinessRuleContext;
import com.lyxtera.axiom.api.exception.RuleParserException;
import com.lyxtera.axiom.api.model.BusinessAction;
import com.lyxtera.axiom.api.model.BusinessCheck;
import com.lyxtera.axiom.api.model.BusinessRule;
import com.lyxtera.axiom.api.model.Condition;
import com.lyxtera.axiom.api.model.RuleFunction;
import com.lyxtera.axiom.engine.RuleSet;

/**
 * Default implementation of the Parser interface.
 * <p>
 * This class uses ANTLR to parse business rule expressions into executable
 * {@link BusinessRule} objects. It leverages the generated ANTLR lexer and parser
 * to tokenize and parse the rule expressions, then uses specialized visitors to
 * build the rule model.
 *
 * @param <K> The enum type to be used as context keys
 */
@Singleton
public class DefaultParser<K extends Enum<K>> implements Parser<K> {
    
    private final Map<String, BusinessCheck<K>> businessChecks;
    private final Map<String, BusinessAction<K>> businessActions;
    
    /**
     * Creates a new DefaultParser with the specified business checks and actions.
     * <p>
     * The maps of business checks and actions are injected and are used
     * to resolve references to checks and actions in rule expressions.
     *
     * @param businessChecks Map of business check implementations keyed by name
     * @param businessActions Map of business action implementations keyed by name
     */
    @Inject
    public DefaultParser(Map<String, BusinessCheck<K>> businessChecks, Map<String, BusinessAction<K>> businessActions) {
        this.businessChecks = businessChecks;
        this.businessActions = businessActions;
    }
    
    /**
     * Parses a business rule from its text representation.
     * <p>
     * This method tokenizes the rule expression using the ANTLR lexer, then
     * parses it using the ANTLR parser, and finally builds a {@link BusinessRule}
     * object using the {@link BusinessRuleVisitor}.
     *
     * @param metadata The rule set metadata
     * @param ruleName The name of the rule
     * @param expression The rule expression to parse
     * @return The parsed rule
     * @throws RuleParserException if the expression is invalid
     */
    @Override
    public BusinessRule<K> parseRule(RuleSet.Metadata metadata, String ruleName, String expression) {
        BusinessRuleLexer lexer = new BusinessRuleLexer(CharStreams.fromString(expression));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        BusinessRuleParser parser = new BusinessRuleParser(tokens);
        
        ParseTree tree = parser.businessRule();
        return new BusinessRuleVisitor<K>(ruleName, businessChecks, businessActions, metadata).visitBusinessRule((BusinessRuleContext) tree);
    }
} 