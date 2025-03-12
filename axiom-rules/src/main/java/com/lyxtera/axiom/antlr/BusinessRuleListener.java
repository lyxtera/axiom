// Generated from BusinessRule.g4 by ANTLR 4.13.1
package com.lyxtera.axiom.antlr;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link BusinessRuleParser}.
 */
public interface BusinessRuleListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link BusinessRuleParser#businessRule}.
	 * @param ctx the parse tree
	 */
	void enterBusinessRule(BusinessRuleParser.BusinessRuleContext ctx);
	/**
	 * Exit a parse tree produced by {@link BusinessRuleParser#businessRule}.
	 * @param ctx the parse tree
	 */
	void exitBusinessRule(BusinessRuleParser.BusinessRuleContext ctx);
	/**
	 * Enter a parse tree produced by {@link BusinessRuleParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(BusinessRuleParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BusinessRuleParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(BusinessRuleParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code orExpression}
	 * labeled alternative in {@link BusinessRuleParser#subExpression}.
	 * @param ctx the parse tree
	 */
	void enterOrExpression(BusinessRuleParser.OrExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code orExpression}
	 * labeled alternative in {@link BusinessRuleParser#subExpression}.
	 * @param ctx the parse tree
	 */
	void exitOrExpression(BusinessRuleParser.OrExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code groupingExpression}
	 * labeled alternative in {@link BusinessRuleParser#subExpression}.
	 * @param ctx the parse tree
	 */
	void enterGroupingExpression(BusinessRuleParser.GroupingExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code groupingExpression}
	 * labeled alternative in {@link BusinessRuleParser#subExpression}.
	 * @param ctx the parse tree
	 */
	void exitGroupingExpression(BusinessRuleParser.GroupingExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code andExpression}
	 * labeled alternative in {@link BusinessRuleParser#subExpression}.
	 * @param ctx the parse tree
	 */
	void enterAndExpression(BusinessRuleParser.AndExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code andExpression}
	 * labeled alternative in {@link BusinessRuleParser#subExpression}.
	 * @param ctx the parse tree
	 */
	void exitAndExpression(BusinessRuleParser.AndExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code notExpression}
	 * labeled alternative in {@link BusinessRuleParser#subExpression}.
	 * @param ctx the parse tree
	 */
	void enterNotExpression(BusinessRuleParser.NotExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code notExpression}
	 * labeled alternative in {@link BusinessRuleParser#subExpression}.
	 * @param ctx the parse tree
	 */
	void exitNotExpression(BusinessRuleParser.NotExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code comparisonExpression}
	 * labeled alternative in {@link BusinessRuleParser#subExpression}.
	 * @param ctx the parse tree
	 */
	void enterComparisonExpression(BusinessRuleParser.ComparisonExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code comparisonExpression}
	 * labeled alternative in {@link BusinessRuleParser#subExpression}.
	 * @param ctx the parse tree
	 */
	void exitComparisonExpression(BusinessRuleParser.ComparisonExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code comparisonOperation}
	 * labeled alternative in {@link BusinessRuleParser#comparison}.
	 * @param ctx the parse tree
	 */
	void enterComparisonOperation(BusinessRuleParser.ComparisonOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code comparisonOperation}
	 * labeled alternative in {@link BusinessRuleParser#comparison}.
	 * @param ctx the parse tree
	 */
	void exitComparisonOperation(BusinessRuleParser.ComparisonOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code businessBooleanExpression}
	 * labeled alternative in {@link BusinessRuleParser#comparison}.
	 * @param ctx the parse tree
	 */
	void enterBusinessBooleanExpression(BusinessRuleParser.BusinessBooleanExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code businessBooleanExpression}
	 * labeled alternative in {@link BusinessRuleParser#comparison}.
	 * @param ctx the parse tree
	 */
	void exitBusinessBooleanExpression(BusinessRuleParser.BusinessBooleanExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code businessCheckExpression}
	 * labeled alternative in {@link BusinessRuleParser#businessCheck}.
	 * @param ctx the parse tree
	 */
	void enterBusinessCheckExpression(BusinessRuleParser.BusinessCheckExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code businessCheckExpression}
	 * labeled alternative in {@link BusinessRuleParser#businessCheck}.
	 * @param ctx the parse tree
	 */
	void exitBusinessCheckExpression(BusinessRuleParser.BusinessCheckExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BusinessRuleParser#arguments}.
	 * @param ctx the parse tree
	 */
	void enterArguments(BusinessRuleParser.ArgumentsContext ctx);
	/**
	 * Exit a parse tree produced by {@link BusinessRuleParser#arguments}.
	 * @param ctx the parse tree
	 */
	void exitArguments(BusinessRuleParser.ArgumentsContext ctx);
	/**
	 * Enter a parse tree produced by {@link BusinessRuleParser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void enterComparisonOperator(BusinessRuleParser.ComparisonOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link BusinessRuleParser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void exitComparisonOperator(BusinessRuleParser.ComparisonOperatorContext ctx);
	/**
	 * Enter a parse tree produced by the {@code numberLiteral}
	 * labeled alternative in {@link BusinessRuleParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterNumberLiteral(BusinessRuleParser.NumberLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code numberLiteral}
	 * labeled alternative in {@link BusinessRuleParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitNumberLiteral(BusinessRuleParser.NumberLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stringLiteral}
	 * labeled alternative in {@link BusinessRuleParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterStringLiteral(BusinessRuleParser.StringLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stringLiteral}
	 * labeled alternative in {@link BusinessRuleParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitStringLiteral(BusinessRuleParser.StringLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code businessActionList}
	 * labeled alternative in {@link BusinessRuleParser#outcome}.
	 * @param ctx the parse tree
	 */
	void enterBusinessActionList(BusinessRuleParser.BusinessActionListContext ctx);
	/**
	 * Exit a parse tree produced by the {@code businessActionList}
	 * labeled alternative in {@link BusinessRuleParser#outcome}.
	 * @param ctx the parse tree
	 */
	void exitBusinessActionList(BusinessRuleParser.BusinessActionListContext ctx);
	/**
	 * Enter a parse tree produced by the {@code businessActionCall}
	 * labeled alternative in {@link BusinessRuleParser#businessAction}.
	 * @param ctx the parse tree
	 */
	void enterBusinessActionCall(BusinessRuleParser.BusinessActionCallContext ctx);
	/**
	 * Exit a parse tree produced by the {@code businessActionCall}
	 * labeled alternative in {@link BusinessRuleParser#businessAction}.
	 * @param ctx the parse tree
	 */
	void exitBusinessActionCall(BusinessRuleParser.BusinessActionCallContext ctx);
}