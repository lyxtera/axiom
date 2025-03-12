// Generated from BusinessRule.g4 by ANTLR 4.13.1
package com.lyxtera.axiom.antlr;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class BusinessRuleLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.13.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, NOT=9, 
		AND=10, OR=11, IDENTIFIER=12, NUMBER=13, STRING=14, WS=15, ILLEGAL_CHAR=16;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "NOT", 
			"AND", "OR", "IDENTIFIER", "NUMBER", "STRING", "WS", "ILLEGAL_CHAR"
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


	public BusinessRuleLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "BusinessRule.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\u0004\u0000\u0010g\u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002\u0001"+
		"\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004"+
		"\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007"+
		"\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b"+
		"\u0007\u000b\u0002\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002"+
		"\u000f\u0007\u000f\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001"+
		"\u0000\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002\u0001\u0003\u0001"+
		"\u0003\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005\u0001\u0006\u0001"+
		"\u0006\u0001\u0007\u0001\u0007\u0001\b\u0001\b\u0001\b\u0001\b\u0001\t"+
		"\u0001\t\u0001\t\u0001\t\u0001\n\u0001\n\u0001\n\u0001\u000b\u0001\u000b"+
		"\u0005\u000bB\b\u000b\n\u000b\f\u000bE\t\u000b\u0001\f\u0004\fH\b\f\u000b"+
		"\f\f\fI\u0001\f\u0001\f\u0004\fN\b\f\u000b\f\f\fO\u0003\fR\b\f\u0001\r"+
		"\u0001\r\u0001\r\u0001\r\u0005\rX\b\r\n\r\f\r[\t\r\u0001\r\u0001\r\u0001"+
		"\u000e\u0004\u000e`\b\u000e\u000b\u000e\f\u000ea\u0001\u000e\u0001\u000e"+
		"\u0001\u000f\u0001\u000f\u0000\u0000\u0010\u0001\u0001\u0003\u0002\u0005"+
		"\u0003\u0007\u0004\t\u0005\u000b\u0006\r\u0007\u000f\b\u0011\t\u0013\n"+
		"\u0015\u000b\u0017\f\u0019\r\u001b\u000e\u001d\u000f\u001f\u0010\u0001"+
		"\u0000\u0005\u0003\u0000AZ__az\u0004\u000009AZ__az\u0001\u000009\u0002"+
		"\u0000\"\"\\\\\u0003\u0000\t\n\r\r  m\u0000\u0001\u0001\u0000\u0000\u0000"+
		"\u0000\u0003\u0001\u0000\u0000\u0000\u0000\u0005\u0001\u0000\u0000\u0000"+
		"\u0000\u0007\u0001\u0000\u0000\u0000\u0000\t\u0001\u0000\u0000\u0000\u0000"+
		"\u000b\u0001\u0000\u0000\u0000\u0000\r\u0001\u0000\u0000\u0000\u0000\u000f"+
		"\u0001\u0000\u0000\u0000\u0000\u0011\u0001\u0000\u0000\u0000\u0000\u0013"+
		"\u0001\u0000\u0000\u0000\u0000\u0015\u0001\u0000\u0000\u0000\u0000\u0017"+
		"\u0001\u0000\u0000\u0000\u0000\u0019\u0001\u0000\u0000\u0000\u0000\u001b"+
		"\u0001\u0000\u0000\u0000\u0000\u001d\u0001\u0000\u0000\u0000\u0000\u001f"+
		"\u0001\u0000\u0000\u0000\u0001!\u0001\u0000\u0000\u0000\u0003&\u0001\u0000"+
		"\u0000\u0000\u0005(\u0001\u0000\u0000\u0000\u0007*\u0001\u0000\u0000\u0000"+
		"\t,\u0001\u0000\u0000\u0000\u000b.\u0001\u0000\u0000\u0000\r0\u0001\u0000"+
		"\u0000\u0000\u000f2\u0001\u0000\u0000\u0000\u00114\u0001\u0000\u0000\u0000"+
		"\u00138\u0001\u0000\u0000\u0000\u0015<\u0001\u0000\u0000\u0000\u0017?"+
		"\u0001\u0000\u0000\u0000\u0019G\u0001\u0000\u0000\u0000\u001bS\u0001\u0000"+
		"\u0000\u0000\u001d_\u0001\u0000\u0000\u0000\u001fe\u0001\u0000\u0000\u0000"+
		"!\"\u0005t\u0000\u0000\"#\u0005h\u0000\u0000#$\u0005e\u0000\u0000$%\u0005"+
		"n\u0000\u0000%\u0002\u0001\u0000\u0000\u0000&\'\u0005(\u0000\u0000\'\u0004"+
		"\u0001\u0000\u0000\u0000()\u0005)\u0000\u0000)\u0006\u0001\u0000\u0000"+
		"\u0000*+\u0005,\u0000\u0000+\b\u0001\u0000\u0000\u0000,-\u0005=\u0000"+
		"\u0000-\n\u0001\u0000\u0000\u0000./\u0005<\u0000\u0000/\f\u0001\u0000"+
		"\u0000\u000001\u0005>\u0000\u00001\u000e\u0001\u0000\u0000\u000023\u0005"+
		";\u0000\u00003\u0010\u0001\u0000\u0000\u000045\u0005n\u0000\u000056\u0005"+
		"o\u0000\u000067\u0005t\u0000\u00007\u0012\u0001\u0000\u0000\u000089\u0005"+
		"a\u0000\u00009:\u0005n\u0000\u0000:;\u0005d\u0000\u0000;\u0014\u0001\u0000"+
		"\u0000\u0000<=\u0005o\u0000\u0000=>\u0005r\u0000\u0000>\u0016\u0001\u0000"+
		"\u0000\u0000?C\u0007\u0000\u0000\u0000@B\u0007\u0001\u0000\u0000A@\u0001"+
		"\u0000\u0000\u0000BE\u0001\u0000\u0000\u0000CA\u0001\u0000\u0000\u0000"+
		"CD\u0001\u0000\u0000\u0000D\u0018\u0001\u0000\u0000\u0000EC\u0001\u0000"+
		"\u0000\u0000FH\u0007\u0002\u0000\u0000GF\u0001\u0000\u0000\u0000HI\u0001"+
		"\u0000\u0000\u0000IG\u0001\u0000\u0000\u0000IJ\u0001\u0000\u0000\u0000"+
		"JQ\u0001\u0000\u0000\u0000KM\u0005.\u0000\u0000LN\u0007\u0002\u0000\u0000"+
		"ML\u0001\u0000\u0000\u0000NO\u0001\u0000\u0000\u0000OM\u0001\u0000\u0000"+
		"\u0000OP\u0001\u0000\u0000\u0000PR\u0001\u0000\u0000\u0000QK\u0001\u0000"+
		"\u0000\u0000QR\u0001\u0000\u0000\u0000R\u001a\u0001\u0000\u0000\u0000"+
		"SY\u0005\"\u0000\u0000TX\b\u0003\u0000\u0000UV\u0005\\\u0000\u0000VX\t"+
		"\u0000\u0000\u0000WT\u0001\u0000\u0000\u0000WU\u0001\u0000\u0000\u0000"+
		"X[\u0001\u0000\u0000\u0000YW\u0001\u0000\u0000\u0000YZ\u0001\u0000\u0000"+
		"\u0000Z\\\u0001\u0000\u0000\u0000[Y\u0001\u0000\u0000\u0000\\]\u0005\""+
		"\u0000\u0000]\u001c\u0001\u0000\u0000\u0000^`\u0007\u0004\u0000\u0000"+
		"_^\u0001\u0000\u0000\u0000`a\u0001\u0000\u0000\u0000a_\u0001\u0000\u0000"+
		"\u0000ab\u0001\u0000\u0000\u0000bc\u0001\u0000\u0000\u0000cd\u0006\u000e"+
		"\u0000\u0000d\u001e\u0001\u0000\u0000\u0000ef\t\u0000\u0000\u0000f \u0001"+
		"\u0000\u0000\u0000\b\u0000CIOQWYa\u0001\u0006\u0000\u0000";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}