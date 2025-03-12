package com.lyxtera.axiom.api.parser;

import static java.lang.Boolean.TRUE;
import static java.util.Optional.ofNullable;
import static com.lyxtera.axiom.api.exception.AxiomEngineException.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.lyxtera.axiom.antlr.BusinessRuleBaseVisitor;
import com.lyxtera.axiom.antlr.BusinessRuleParser.AndExpressionContext;
import com.lyxtera.axiom.antlr.BusinessRuleParser.BusinessBooleanExpressionContext;
import com.lyxtera.axiom.antlr.BusinessRuleParser.BusinessCheckExpressionContext;
import com.lyxtera.axiom.antlr.BusinessRuleParser.ComparisonContext;
import com.lyxtera.axiom.antlr.BusinessRuleParser.ComparisonExpressionContext;
import com.lyxtera.axiom.antlr.BusinessRuleParser.ComparisonOperationContext;
import com.lyxtera.axiom.antlr.BusinessRuleParser.ExpressionContext;
import com.lyxtera.axiom.antlr.BusinessRuleParser.GroupingExpressionContext;
import com.lyxtera.axiom.antlr.BusinessRuleParser.LiteralContext;
import com.lyxtera.axiom.antlr.BusinessRuleParser.NotExpressionContext;
import com.lyxtera.axiom.antlr.BusinessRuleParser.NumberLiteralContext;
import com.lyxtera.axiom.antlr.BusinessRuleParser.OrExpressionContext;
import com.lyxtera.axiom.antlr.BusinessRuleParser.StringLiteralContext;
import com.lyxtera.axiom.antlr.BusinessRuleParser.SubExpressionContext;
import com.lyxtera.axiom.api.exception.RuleParserException;
import com.lyxtera.axiom.api.model.BusinessCheck;
import com.lyxtera.axiom.api.model.Condition;
import com.lyxtera.axiom.api.model.Operator;
import com.lyxtera.axiom.api.model.RuleFunction;
import com.lyxtera.axiom.api.model.Value;
import com.lyxtera.axiom.engine.ArgAwareRuleFunction;
import com.lyxtera.axiom.engine.RuleSet;

/**
 * Specialized visitor for parsing conditions in business rules.
 * <p>
 * This visitor traverses the parse tree for condition expressions and constructs
 * {@link Condition} objects that can be evaluated at runtime. It handles logical
 * operations (AND, OR, NOT), comparisons, and business check function calls.
 *
 * @param <K> The enum type to be used as context keys
 */
public class ConditionVisitor<K extends Enum<K>> extends BusinessRuleBaseVisitor<Condition<K>> {

    private final ArgumentVisitor<K> argumentVisitor;
    private final BusinessCheckVisitor businessCheckVisitor;
    private final Map<String, BusinessCheck<K>> businessChecks;
    private final RuleSet.Metadata metadata;

    /**
     * Creates a new ConditionVisitor with the specified business checks.
     *
     * @param businessChecks Map of business check implementations keyed by name
     */
    public ConditionVisitor(Map<String, BusinessCheck<K>> businessChecks, RuleSet.Metadata metadata) {
        this.businessChecks = businessChecks;
        this.metadata = metadata;

        this.argumentVisitor = new ArgumentVisitor<>();
        this.businessCheckVisitor = new BusinessCheckVisitor();
    }
    
    /**
     * Visits an expression node in the parse tree and constructs a {@link Condition} object.
     *
     * @param ctx The expression context from the parse tree
     * @return A condition representing the expression
     */
    @Override
    public Condition<K> visitExpression(ExpressionContext ctx) {
        return visitSubExpression(ctx.subExpression());
    }
    
    /**
     * Visits a sub-expression node in the parse tree and constructs a {@link Condition} object.
     * <p>
     * This method dispatches to the appropriate visitor method based on the type of sub-expression.
     *
     * @param ctx The sub-expression context from the parse tree
     * @return A condition representing the sub-expression
     * @throws RuleParserException if the sub-expression type is unknown
     */
    public Condition<K> visitSubExpression(SubExpressionContext ctx) {
        if (ctx instanceof AndExpressionContext) {
            return visitAndExpression((AndExpressionContext) ctx);
        } else if (ctx instanceof OrExpressionContext) {
            return visitOrExpression((OrExpressionContext) ctx);
        } else if (ctx instanceof NotExpressionContext) {
            return visitNotExpression((NotExpressionContext) ctx);
        } else if (ctx instanceof GroupingExpressionContext) {
            return visitGroupingExpression((GroupingExpressionContext) ctx);
        } else if (ctx instanceof ComparisonExpressionContext) {
            return visitComparisonExpression((ComparisonExpressionContext) ctx);
        } else {
            throw RuleParserException.unknownExpressionType(ctx.getClass().getName());
        }
    }
    
    /**
     * Visits an AND expression node in the parse tree and constructs a logical AND condition.
     *
     * @param ctx The AND expression context from the parse tree
     * @return A condition representing the logical AND of the left and right sub-expressions
     */
    @Override
    public Condition<K> visitAndExpression(AndExpressionContext ctx) {
        Condition<K> left = visitSubExpression(ctx.subExpression(0));
        Condition<K> right = visitSubExpression(ctx.subExpression(1));
        return Condition.asLogicalExpression(left, Operator.AND, right);
    }
    
    /**
     * Visits an OR expression node in the parse tree and constructs a logical OR condition.
     *
     * @param ctx The OR expression context from the parse tree
     * @return A condition representing the logical OR of the left and right sub-expressions
     */
    @Override
    public Condition<K> visitOrExpression(OrExpressionContext ctx) {
        Condition<K> left = visitSubExpression(ctx.subExpression(0));
        Condition<K> right = visitSubExpression(ctx.subExpression(1));
        return Condition.asLogicalExpression(left, Operator.OR, right);
    }
    
    /**
     * Visits a NOT expression node in the parse tree and constructs a logical NOT condition.
     *
     * @param ctx The NOT expression context from the parse tree
     * @return A condition representing the logical negation of the sub-expression
     */
    @Override
    public Condition<K> visitNotExpression(NotExpressionContext ctx) {
        return Condition.asNegation(visitSubExpression(ctx.subExpression()));
    }
    
    /**
     * Visits a grouping expression node in the parse tree and constructs a condition.
     * <p>
     * A grouping expression is a sub-expression enclosed in parentheses.
     *
     * @param ctx The grouping expression context from the parse tree
     * @return A condition representing the sub-expression
     */
    @Override
    public Condition<K> visitGroupingExpression(GroupingExpressionContext ctx) {
        return visitSubExpression(ctx.subExpression());
    }
    
    /**
     * Visits a comparison expression node in the parse tree and constructs a comparison condition.
     *
     * @param ctx The comparison expression context from the parse tree
     * @return A condition representing the comparison
     */
    @Override
    public Condition<K> visitComparisonExpression(ComparisonExpressionContext ctx) {
        return visitComparison(ctx.comparison());
    }
    
    /**
     * Visits a comparison node in the parse tree and constructs a comparison condition.
     * <p>
     * This method dispatches to the appropriate visitor method based on the type of comparison.
     *
     * @param ctx The comparison context from the parse tree
     * @return A condition representing the comparison
     * @throws RuleParserException if the comparison type is unknown
     */
    public Condition<K> visitComparison(ComparisonContext ctx) {
        if (ctx instanceof ComparisonOperationContext) {
            return visitComparisonOperation((ComparisonOperationContext) ctx);
        } else if (ctx instanceof BusinessBooleanExpressionContext) {
            return visitBusinessBooleanExpression((BusinessBooleanExpressionContext) ctx);
        }
        throw RuleParserException.unknownComparisonType(ctx.getClass().getName());
    }
    
    /**
     * Visits a comparison operation node in the parse tree and constructs a comparison condition.
     * <p>
     * A comparison operation compares the result of a business check function with a literal value
     * using a comparison operator (=, <, >).
     *
     * @param ctx The comparison operation context from the parse tree
     * @return A condition representing the comparison operation
     * @throws RuleParserException if the comparison operator is unknown
     */
    @Override
    public Condition<K> visitComparisonOperation(ComparisonOperationContext ctx) {
        RuleFunction<K> left = businessCheckVisitor.visitBusinessCheckExpression(
            (BusinessCheckExpressionContext) ctx.businessCheck());

        Value value;
        
        if (ctx.literal() instanceof NumberLiteralContext) {
            value = argumentVisitor.visitNumberLiteral((NumberLiteralContext) ctx.literal());
        } else {
            value = argumentVisitor.visitStringLiteral((StringLiteralContext) ctx.literal());
        }
        
        Operator operator = Operator.UNKNOWN;

        switch (ctx.comparisonOperator().getText()) {
            case "=":
                operator = Operator.EQUALS;
                break;
            case "<":
                operator = Operator.LESS_THAN;
                break;
            case ">":
                operator = Operator.GREATER_THAN;
                break;
            default:
                throw RuleParserException.unknownOperator(ctx.comparisonOperator().getText());
        }
        
        return Condition.asComparison(left, operator, value);
    }

    /**
     * Visits a business boolean expression node in the parse tree and constructs a boolean condition.
     * <p>
     * A business boolean expression is a business check function call that returns a boolean value.
     *
     * @param ctx The business boolean expression context from the parse tree
     * @return A condition representing the boolean expression
     */
    @Override
    public Condition<K> visitBusinessBooleanExpression(BusinessBooleanExpressionContext ctx) {
        RuleFunction<K> booleanFunction = businessCheckVisitor.visitBusinessCheckExpression(
            (BusinessCheckExpressionContext) ctx.businessCheck());

        return Condition.asBoolean(k -> booleanFunction.execute(k).getValue().equals(TRUE));
    }

    /**
     * Inner visitor class for business check function calls.
     */
    private class BusinessCheckVisitor extends BusinessRuleBaseVisitor<RuleFunction<K>> {
        
        /**
         * Visits a business check expression node in the parse tree and constructs a {@link BusinessCheck} object.
         * <p>
         * A business check expression is a function call with a name and optional arguments.
         *
         * @param ctx The business check expression context from the parse tree
         * @return A business check function with the specified name and arguments
         * @throws RuleParserException if the business check function is unknown
         */
        @Override
        public RuleFunction<K> visitBusinessCheckExpression(BusinessCheckExpressionContext ctx) {
            String functionName = ctx.IDENTIFIER().getText();
            List<Value> arguments = new ArrayList<>();
            
            if (ctx.arguments() != null) {
                for (LiteralContext literalCtx : ctx.arguments().literal()) {
                    if (literalCtx instanceof NumberLiteralContext) {
                        arguments.add(argumentVisitor.visitNumberLiteral((NumberLiteralContext) literalCtx));
                    } else {
                        arguments.add(argumentVisitor.visitStringLiteral((StringLiteralContext) literalCtx));
                    }
                }
            }
            
            return ofNullable(businessChecks.get(functionName))
                .map(check -> ArgAwareRuleFunction.of(check, arguments, metadata))
                .orElseThrow(() -> RuleParserException.of(MSG_UNKNOWN_BUSINESS_CHECK, functionName));
        }
    }
} 