// Generated from BusinessRule.g4 by ANTLR 4.13.1
package com.lyxtera.axiom.antlr;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class BusinessRuleParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, NOT=9, 
		AND=10, OR=11, IDENTIFIER=12, NUMBER=13, STRING=14, WS=15, ILLEGAL_CHAR=16;
	public static final int
		RULE_businessRule = 0, RULE_expression = 1, RULE_subExpression = 2, RULE_comparison = 3, 
		RULE_businessCheck = 4, RULE_arguments = 5, RULE_comparisonOperator = 6, 
		RULE_literal = 7, RULE_outcome = 8, RULE_businessAction = 9;
	private static String[] makeRuleNames() {
		return new String[] {
			"businessRule", "expression", "subExpression", "comparison", "businessCheck", 
			"arguments", "comparisonOperator", "literal", "outcome", "businessAction"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'then'", "'('", "')'", "','", "'='", "'<'", "'>'", "';'", "'not'", 
			"'and'", "'or'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, "NOT", "AND", "OR", 
			"IDENTIFIER", "NUMBER", "STRING", "WS", "ILLEGAL_CHAR"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "BusinessRule.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public BusinessRuleParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class BusinessRuleContext extends ParserRuleContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public OutcomeContext outcome() {
			return getRuleContext(OutcomeContext.class,0);
		}
		public TerminalNode EOF() { return getToken(BusinessRuleParser.EOF, 0); }
		public BusinessRuleContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_businessRule; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BusinessRuleListener ) ((BusinessRuleListener)listener).enterBusinessRule(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BusinessRuleListener ) ((BusinessRuleListener)listener).exitBusinessRule(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BusinessRuleVisitor ) return ((BusinessRuleVisitor<? extends T>)visitor).visitBusinessRule(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BusinessRuleContext businessRule() throws RecognitionException {
		BusinessRuleContext _localctx = new BusinessRuleContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_businessRule);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(20);
			expression();
			setState(21);
			match(T__0);
			setState(22);
			outcome();
			setState(23);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExpressionContext extends ParserRuleContext {
		public SubExpressionContext subExpression() {
			return getRuleContext(SubExpressionContext.class,0);
		}
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BusinessRuleListener ) ((BusinessRuleListener)listener).enterExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BusinessRuleListener ) ((BusinessRuleListener)listener).exitExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BusinessRuleVisitor ) return ((BusinessRuleVisitor<? extends T>)visitor).visitExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionContext expression() throws RecognitionException {
		ExpressionContext _localctx = new ExpressionContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_expression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(25);
			subExpression(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SubExpressionContext extends ParserRuleContext {
		public SubExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_subExpression; }
	 
		public SubExpressionContext() { }
		public void copyFrom(SubExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class OrExpressionContext extends SubExpressionContext {
		public List<SubExpressionContext> subExpression() {
			return getRuleContexts(SubExpressionContext.class);
		}
		public SubExpressionContext subExpression(int i) {
			return getRuleContext(SubExpressionContext.class,i);
		}
		public TerminalNode OR() { return getToken(BusinessRuleParser.OR, 0); }
		public OrExpressionContext(SubExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BusinessRuleListener ) ((BusinessRuleListener)listener).enterOrExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BusinessRuleListener ) ((BusinessRuleListener)listener).exitOrExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BusinessRuleVisitor ) return ((BusinessRuleVisitor<? extends T>)visitor).visitOrExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class GroupingExpressionContext extends SubExpressionContext {
		public SubExpressionContext subExpression() {
			return getRuleContext(SubExpressionContext.class,0);
		}
		public GroupingExpressionContext(SubExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BusinessRuleListener ) ((BusinessRuleListener)listener).enterGroupingExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BusinessRuleListener ) ((BusinessRuleListener)listener).exitGroupingExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BusinessRuleVisitor ) return ((BusinessRuleVisitor<? extends T>)visitor).visitGroupingExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class AndExpressionContext extends SubExpressionContext {
		public List<SubExpressionContext> subExpression() {
			return getRuleContexts(SubExpressionContext.class);
		}
		public SubExpressionContext subExpression(int i) {
			return getRuleContext(SubExpressionContext.class,i);
		}
		public TerminalNode AND() { return getToken(BusinessRuleParser.AND, 0); }
		public AndExpressionContext(SubExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BusinessRuleListener ) ((BusinessRuleListener)listener).enterAndExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BusinessRuleListener ) ((BusinessRuleListener)listener).exitAndExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BusinessRuleVisitor ) return ((BusinessRuleVisitor<? extends T>)visitor).visitAndExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NotExpressionContext extends SubExpressionContext {
		public TerminalNode NOT() { return getToken(BusinessRuleParser.NOT, 0); }
		public SubExpressionContext subExpression() {
			return getRuleContext(SubExpressionContext.class,0);
		}
		public NotExpressionContext(SubExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BusinessRuleListener ) ((BusinessRuleListener)listener).enterNotExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BusinessRuleListener ) ((BusinessRuleListener)listener).exitNotExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BusinessRuleVisitor ) return ((BusinessRuleVisitor<? extends T>)visitor).visitNotExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ComparisonExpressionContext extends SubExpressionContext {
		public ComparisonContext comparison() {
			return getRuleContext(ComparisonContext.class,0);
		}
		public ComparisonExpressionContext(SubExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BusinessRuleListener ) ((BusinessRuleListener)listener).enterComparisonExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BusinessRuleListener ) ((BusinessRuleListener)listener).exitComparisonExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BusinessRuleVisitor ) return ((BusinessRuleVisitor<? extends T>)visitor).visitComparisonExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SubExpressionContext subExpression() throws RecognitionException {
		return subExpression(0);
	}

	private SubExpressionContext subExpression(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		SubExpressionContext _localctx = new SubExpressionContext(_ctx, _parentState);
		SubExpressionContext _prevctx = _localctx;
		int _startState = 4;
		enterRecursionRule(_localctx, 4, RULE_subExpression, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(35);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NOT:
				{
				_localctx = new NotExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(28);
				match(NOT);
				setState(29);
				subExpression(3);
				}
				break;
			case T__1:
				{
				_localctx = new GroupingExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(30);
				match(T__1);
				setState(31);
				subExpression(0);
				setState(32);
				match(T__2);
				}
				break;
			case IDENTIFIER:
				{
				_localctx = new ComparisonExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(34);
				comparison();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(45);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(43);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
					case 1:
						{
						_localctx = new AndExpressionContext(new SubExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_subExpression);
						setState(37);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(38);
						match(AND);
						setState(39);
						subExpression(6);
						}
						break;
					case 2:
						{
						_localctx = new OrExpressionContext(new SubExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_subExpression);
						setState(40);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(41);
						match(OR);
						setState(42);
						subExpression(5);
						}
						break;
					}
					} 
				}
				setState(47);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ComparisonContext extends ParserRuleContext {
		public ComparisonContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_comparison; }
	 
		public ComparisonContext() { }
		public void copyFrom(ComparisonContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class BusinessBooleanExpressionContext extends ComparisonContext {
		public BusinessCheckContext businessCheck() {
			return getRuleContext(BusinessCheckContext.class,0);
		}
		public BusinessBooleanExpressionContext(ComparisonContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BusinessRuleListener ) ((BusinessRuleListener)listener).enterBusinessBooleanExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BusinessRuleListener ) ((BusinessRuleListener)listener).exitBusinessBooleanExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BusinessRuleVisitor ) return ((BusinessRuleVisitor<? extends T>)visitor).visitBusinessBooleanExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ComparisonOperationContext extends ComparisonContext {
		public BusinessCheckContext businessCheck() {
			return getRuleContext(BusinessCheckContext.class,0);
		}
		public ComparisonOperatorContext comparisonOperator() {
			return getRuleContext(ComparisonOperatorContext.class,0);
		}
		public LiteralContext literal() {
			return getRuleContext(LiteralContext.class,0);
		}
		public ComparisonOperationContext(ComparisonContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BusinessRuleListener ) ((BusinessRuleListener)listener).enterComparisonOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BusinessRuleListener ) ((BusinessRuleListener)listener).exitComparisonOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BusinessRuleVisitor ) return ((BusinessRuleVisitor<? extends T>)visitor).visitComparisonOperation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ComparisonContext comparison() throws RecognitionException {
		ComparisonContext _localctx = new ComparisonContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_comparison);
		try {
			setState(53);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,3,_ctx) ) {
			case 1:
				_localctx = new ComparisonOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(48);
				businessCheck();
				setState(49);
				comparisonOperator();
				setState(50);
				literal();
				}
				break;
			case 2:
				_localctx = new BusinessBooleanExpressionContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(52);
				businessCheck();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class BusinessCheckContext extends ParserRuleContext {
		public BusinessCheckContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_businessCheck; }
	 
		public BusinessCheckContext() { }
		public void copyFrom(BusinessCheckContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class BusinessCheckExpressionContext extends BusinessCheckContext {
		public TerminalNode IDENTIFIER() { return getToken(BusinessRuleParser.IDENTIFIER, 0); }
		public ArgumentsContext arguments() {
			return getRuleContext(ArgumentsContext.class,0);
		}
		public BusinessCheckExpressionContext(BusinessCheckContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BusinessRuleListener ) ((BusinessRuleListener)listener).enterBusinessCheckExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BusinessRuleListener ) ((BusinessRuleListener)listener).exitBusinessCheckExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BusinessRuleVisitor ) return ((BusinessRuleVisitor<? extends T>)visitor).visitBusinessCheckExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BusinessCheckContext businessCheck() throws RecognitionException {
		BusinessCheckContext _localctx = new BusinessCheckContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_businessCheck);
		int _la;
		try {
			_localctx = new BusinessCheckExpressionContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(55);
			match(IDENTIFIER);
			setState(56);
			match(T__1);
			setState(58);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==NUMBER || _la==STRING) {
				{
				setState(57);
				arguments();
				}
			}

			setState(60);
			match(T__2);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ArgumentsContext extends ParserRuleContext {
		public List<LiteralContext> literal() {
			return getRuleContexts(LiteralContext.class);
		}
		public LiteralContext literal(int i) {
			return getRuleContext(LiteralContext.class,i);
		}
		public ArgumentsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arguments; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BusinessRuleListener ) ((BusinessRuleListener)listener).enterArguments(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BusinessRuleListener ) ((BusinessRuleListener)listener).exitArguments(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BusinessRuleVisitor ) return ((BusinessRuleVisitor<? extends T>)visitor).visitArguments(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArgumentsContext arguments() throws RecognitionException {
		ArgumentsContext _localctx = new ArgumentsContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_arguments);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(62);
			literal();
			setState(67);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__3) {
				{
				{
				setState(63);
				match(T__3);
				setState(64);
				literal();
				}
				}
				setState(69);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ComparisonOperatorContext extends ParserRuleContext {
		public ComparisonOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_comparisonOperator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BusinessRuleListener ) ((BusinessRuleListener)listener).enterComparisonOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BusinessRuleListener ) ((BusinessRuleListener)listener).exitComparisonOperator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BusinessRuleVisitor ) return ((BusinessRuleVisitor<? extends T>)visitor).visitComparisonOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ComparisonOperatorContext comparisonOperator() throws RecognitionException {
		ComparisonOperatorContext _localctx = new ComparisonOperatorContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_comparisonOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(70);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 224L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LiteralContext extends ParserRuleContext {
		public LiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_literal; }
	 
		public LiteralContext() { }
		public void copyFrom(LiteralContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StringLiteralContext extends LiteralContext {
		public TerminalNode STRING() { return getToken(BusinessRuleParser.STRING, 0); }
		public StringLiteralContext(LiteralContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BusinessRuleListener ) ((BusinessRuleListener)listener).enterStringLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BusinessRuleListener ) ((BusinessRuleListener)listener).exitStringLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BusinessRuleVisitor ) return ((BusinessRuleVisitor<? extends T>)visitor).visitStringLiteral(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NumberLiteralContext extends LiteralContext {
		public TerminalNode NUMBER() { return getToken(BusinessRuleParser.NUMBER, 0); }
		public NumberLiteralContext(LiteralContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BusinessRuleListener ) ((BusinessRuleListener)listener).enterNumberLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BusinessRuleListener ) ((BusinessRuleListener)listener).exitNumberLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BusinessRuleVisitor ) return ((BusinessRuleVisitor<? extends T>)visitor).visitNumberLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LiteralContext literal() throws RecognitionException {
		LiteralContext _localctx = new LiteralContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_literal);
		try {
			setState(74);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NUMBER:
				_localctx = new NumberLiteralContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(72);
				match(NUMBER);
				}
				break;
			case STRING:
				_localctx = new StringLiteralContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(73);
				match(STRING);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class OutcomeContext extends ParserRuleContext {
		public OutcomeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_outcome; }
	 
		public OutcomeContext() { }
		public void copyFrom(OutcomeContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class BusinessActionListContext extends OutcomeContext {
		public List<BusinessActionContext> businessAction() {
			return getRuleContexts(BusinessActionContext.class);
		}
		public BusinessActionContext businessAction(int i) {
			return getRuleContext(BusinessActionContext.class,i);
		}
		public BusinessActionListContext(OutcomeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BusinessRuleListener ) ((BusinessRuleListener)listener).enterBusinessActionList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BusinessRuleListener ) ((BusinessRuleListener)listener).exitBusinessActionList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BusinessRuleVisitor ) return ((BusinessRuleVisitor<? extends T>)visitor).visitBusinessActionList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OutcomeContext outcome() throws RecognitionException {
		OutcomeContext _localctx = new OutcomeContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_outcome);
		int _la;
		try {
			_localctx = new BusinessActionListContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(76);
			businessAction();
			setState(81);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__7) {
				{
				{
				setState(77);
				match(T__7);
				setState(78);
				businessAction();
				}
				}
				setState(83);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class BusinessActionContext extends ParserRuleContext {
		public BusinessActionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_businessAction; }
	 
		public BusinessActionContext() { }
		public void copyFrom(BusinessActionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class BusinessActionCallContext extends BusinessActionContext {
		public TerminalNode IDENTIFIER() { return getToken(BusinessRuleParser.IDENTIFIER, 0); }
		public ArgumentsContext arguments() {
			return getRuleContext(ArgumentsContext.class,0);
		}
		public BusinessActionCallContext(BusinessActionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BusinessRuleListener ) ((BusinessRuleListener)listener).enterBusinessActionCall(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BusinessRuleListener ) ((BusinessRuleListener)listener).exitBusinessActionCall(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BusinessRuleVisitor ) return ((BusinessRuleVisitor<? extends T>)visitor).visitBusinessActionCall(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BusinessActionContext businessAction() throws RecognitionException {
		BusinessActionContext _localctx = new BusinessActionContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_businessAction);
		int _la;
		try {
			_localctx = new BusinessActionCallContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(84);
			match(IDENTIFIER);
			setState(85);
			match(T__1);
			setState(87);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==NUMBER || _la==STRING) {
				{
				setState(86);
				arguments();
				}
			}

			setState(89);
			match(T__2);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 2:
			return subExpression_sempred((SubExpressionContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean subExpression_sempred(SubExpressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 5);
		case 1:
			return precpred(_ctx, 4);
		}
		return true;
	}

	public static final String _serializedATN =
		"\u0004\u0001\u0010\\\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000"+
		"\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002\u0001\u0002"+
		"\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0003\u0002"+
		"$\b\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002"+
		"\u0001\u0002\u0005\u0002,\b\u0002\n\u0002\f\u0002/\t\u0002\u0001\u0003"+
		"\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0003\u00036\b\u0003"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0003\u0004;\b\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0005\u0001\u0005\u0001\u0005\u0005\u0005B\b\u0005"+
		"\n\u0005\f\u0005E\t\u0005\u0001\u0006\u0001\u0006\u0001\u0007\u0001\u0007"+
		"\u0003\u0007K\b\u0007\u0001\b\u0001\b\u0001\b\u0005\bP\b\b\n\b\f\bS\t"+
		"\b\u0001\t\u0001\t\u0001\t\u0003\tX\b\t\u0001\t\u0001\t\u0001\t\u0000"+
		"\u0001\u0004\n\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012\u0000\u0001"+
		"\u0001\u0000\u0005\u0007[\u0000\u0014\u0001\u0000\u0000\u0000\u0002\u0019"+
		"\u0001\u0000\u0000\u0000\u0004#\u0001\u0000\u0000\u0000\u00065\u0001\u0000"+
		"\u0000\u0000\b7\u0001\u0000\u0000\u0000\n>\u0001\u0000\u0000\u0000\fF"+
		"\u0001\u0000\u0000\u0000\u000eJ\u0001\u0000\u0000\u0000\u0010L\u0001\u0000"+
		"\u0000\u0000\u0012T\u0001\u0000\u0000\u0000\u0014\u0015\u0003\u0002\u0001"+
		"\u0000\u0015\u0016\u0005\u0001\u0000\u0000\u0016\u0017\u0003\u0010\b\u0000"+
		"\u0017\u0018\u0005\u0000\u0000\u0001\u0018\u0001\u0001\u0000\u0000\u0000"+
		"\u0019\u001a\u0003\u0004\u0002\u0000\u001a\u0003\u0001\u0000\u0000\u0000"+
		"\u001b\u001c\u0006\u0002\uffff\uffff\u0000\u001c\u001d\u0005\t\u0000\u0000"+
		"\u001d$\u0003\u0004\u0002\u0003\u001e\u001f\u0005\u0002\u0000\u0000\u001f"+
		" \u0003\u0004\u0002\u0000 !\u0005\u0003\u0000\u0000!$\u0001\u0000\u0000"+
		"\u0000\"$\u0003\u0006\u0003\u0000#\u001b\u0001\u0000\u0000\u0000#\u001e"+
		"\u0001\u0000\u0000\u0000#\"\u0001\u0000\u0000\u0000$-\u0001\u0000\u0000"+
		"\u0000%&\n\u0005\u0000\u0000&\'\u0005\n\u0000\u0000\',\u0003\u0004\u0002"+
		"\u0006()\n\u0004\u0000\u0000)*\u0005\u000b\u0000\u0000*,\u0003\u0004\u0002"+
		"\u0005+%\u0001\u0000\u0000\u0000+(\u0001\u0000\u0000\u0000,/\u0001\u0000"+
		"\u0000\u0000-+\u0001\u0000\u0000\u0000-.\u0001\u0000\u0000\u0000.\u0005"+
		"\u0001\u0000\u0000\u0000/-\u0001\u0000\u0000\u000001\u0003\b\u0004\u0000"+
		"12\u0003\f\u0006\u000023\u0003\u000e\u0007\u000036\u0001\u0000\u0000\u0000"+
		"46\u0003\b\u0004\u000050\u0001\u0000\u0000\u000054\u0001\u0000\u0000\u0000"+
		"6\u0007\u0001\u0000\u0000\u000078\u0005\f\u0000\u00008:\u0005\u0002\u0000"+
		"\u00009;\u0003\n\u0005\u0000:9\u0001\u0000\u0000\u0000:;\u0001\u0000\u0000"+
		"\u0000;<\u0001\u0000\u0000\u0000<=\u0005\u0003\u0000\u0000=\t\u0001\u0000"+
		"\u0000\u0000>C\u0003\u000e\u0007\u0000?@\u0005\u0004\u0000\u0000@B\u0003"+
		"\u000e\u0007\u0000A?\u0001\u0000\u0000\u0000BE\u0001\u0000\u0000\u0000"+
		"CA\u0001\u0000\u0000\u0000CD\u0001\u0000\u0000\u0000D\u000b\u0001\u0000"+
		"\u0000\u0000EC\u0001\u0000\u0000\u0000FG\u0007\u0000\u0000\u0000G\r\u0001"+
		"\u0000\u0000\u0000HK\u0005\r\u0000\u0000IK\u0005\u000e\u0000\u0000JH\u0001"+
		"\u0000\u0000\u0000JI\u0001\u0000\u0000\u0000K\u000f\u0001\u0000\u0000"+
		"\u0000LQ\u0003\u0012\t\u0000MN\u0005\b\u0000\u0000NP\u0003\u0012\t\u0000"+
		"OM\u0001\u0000\u0000\u0000PS\u0001\u0000\u0000\u0000QO\u0001\u0000\u0000"+
		"\u0000QR\u0001\u0000\u0000\u0000R\u0011\u0001\u0000\u0000\u0000SQ\u0001"+
		"\u0000\u0000\u0000TU\u0005\f\u0000\u0000UW\u0005\u0002\u0000\u0000VX\u0003"+
		"\n\u0005\u0000WV\u0001\u0000\u0000\u0000WX\u0001\u0000\u0000\u0000XY\u0001"+
		"\u0000\u0000\u0000YZ\u0005\u0003\u0000\u0000Z\u0013\u0001\u0000\u0000"+
		"\u0000\t#+-5:CJQW";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}