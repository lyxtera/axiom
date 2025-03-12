// Generated from BusinessRule.g4 by ANTLR 4.13.1
package com.lyxtera.axiom.antlr;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link BusinessRuleParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface BusinessRuleVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link BusinessRuleParser#businessRule}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBusinessRule(BusinessRuleParser.BusinessRuleContext ctx);
	/**
	 * Visit a parse tree produced by {@link BusinessRuleParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(BusinessRuleParser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code orExpression}
	 * labeled alternative in {@link BusinessRuleParser#subExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrExpression(BusinessRuleParser.OrExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code groupingExpression}
	 * labeled alternative in {@link BusinessRuleParser#subExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroupingExpression(BusinessRuleParser.GroupingExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code andExpression}
	 * labeled alternative in {@link BusinessRuleParser#subExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAndExpression(BusinessRuleParser.AndExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code notExpression}
	 * labeled alternative in {@link BusinessRuleParser#subExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNotExpression(BusinessRuleParser.NotExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code comparisonExpression}
	 * labeled alternative in {@link BusinessRuleParser#subExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparisonExpression(BusinessRuleParser.ComparisonExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code comparisonOperation}
	 * labeled alternative in {@link BusinessRuleParser#comparison}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparisonOperation(BusinessRuleParser.ComparisonOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code businessBooleanExpression}
	 * labeled alternative in {@link BusinessRuleParser#comparison}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBusinessBooleanExpression(BusinessRuleParser.BusinessBooleanExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code businessCheckExpression}
	 * labeled alternative in {@link BusinessRuleParser#businessCheck}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBusinessCheckExpression(BusinessRuleParser.BusinessCheckExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BusinessRuleParser#arguments}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArguments(BusinessRuleParser.ArgumentsContext ctx);
	/**
	 * Visit a parse tree produced by {@link BusinessRuleParser#comparisonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparisonOperator(BusinessRuleParser.ComparisonOperatorContext ctx);
	/**
	 * Visit a parse tree produced by the {@code numberLiteral}
	 * labeled alternative in {@link BusinessRuleParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumberLiteral(BusinessRuleParser.NumberLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code stringLiteral}
	 * labeled alternative in {@link BusinessRuleParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringLiteral(BusinessRuleParser.StringLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code businessActionList}
	 * labeled alternative in {@link BusinessRuleParser#outcome}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBusinessActionList(BusinessRuleParser.BusinessActionListContext ctx);
	/**
	 * Visit a parse tree produced by the {@code businessActionCall}
	 * labeled alternative in {@link BusinessRuleParser#businessAction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBusinessActionCall(BusinessRuleParser.BusinessActionCallContext ctx);
}