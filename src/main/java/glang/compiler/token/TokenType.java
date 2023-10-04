package glang.compiler.token;

import glang.compiler.util.SymbolMap;

import java.util.HashMap;
import java.util.Map;

public enum TokenType {
    // Non-keywords
    PLUS_PLUS(false, "++"),
    MINUS_MINUS(false, "--"),
    PLUS(false, "+"),
    MINUS(false, "-"),
    TILDE(false, "~"),
    BANG(false, "!"),
    STAR(false, "*"),
    SLASH(false, "/"),
    PERCENT(false, "%"),
    SHL(false, "<<"),
    SHR(false, ">>"),
    USHR(false, ">>>"),
    LESS(false, "<"),
    GREATER(false, ">"),
    LESS_EQUAL(false, "<="),
    GREATER_EQUAL(false, ">="),
    EQUAL_EQUAL(false, "=="),
    NOT_EQUAL(false, "!="),
    AND(false, "&"),
    CARET(false, "^"),
    OR(false, "|"),
    AND_AND(false, "&&"),
    OR_OR(false, "||"),
    QUESTION(false, "?"),
    COLON(false, ":"),
    EQUAL(false, "="),
    PLUS_EQUAL(false, "+="),
    MINUS_EQUAL(false, "-="),
    STAR_EQUAL(false, "*="),
    SLASH_EQUAL(false, "/="),
    PERCENT_EQUAL(false, "%="),
    AND_EQUAL(false, "&="),
    CARET_EQUAL(false, "^="),
    OR_EQUAL(false, "|="),
    SHL_EQUAL(false, "<<="),
    SHR_EQUAL(false, ">>="),
    USHR_EQUAL(false, ">>>="),
    COMMA(false, ","),
    SEMI(false, ";"),
    LBRACKET(false, "["),
    RBRACKET(false, "]"),
    LPAREN(false, "("),
    RPAREN(false, ")"),
    LCURLY(false, "{"),
    RCURLY(false, "}"),
    DOT(false, "."),
    // Keywords
    VAR(true, "var"),
    FN(true, "fn"),
    PUBLIC(true, "public"),
    PRIVATE(true, "private"),
    STATIC(true, "static"),
    INSTANCEOF(true, "instanceof"),
    FINAL(true, "final"),
    CLASS(true, "class"),
    TRUE(true, "true"),
    FALSE(true, "false"),
    NULL(true, "null"),
    // Generic
    IDENTIFIER(Token.Identifier.class),
    STRING(Token.Str.class),
    NUMBER(Token.Num.class),
    EOF(false, "<<EOF>>")
    ;

    public static final SymbolMap<TokenType> SIMPLE_TOKENS = new SymbolMap<>();
    public static final Map<String, TokenType> KEYWORDS = new HashMap<>();

    static {
        for (final TokenType type : values()) {
            if (type.tokenClass != Token.Basic.class || type == EOF) continue;
            if (type.isKeyword) {
                KEYWORDS.put(type.basicText, type);
            } else {
                SIMPLE_TOKENS.put(type.basicText, type);
            }
        }
    }

    private final Class<? extends Token> tokenClass;
    private final boolean isKeyword;
    private final String basicText;

    TokenType(Class<? extends Token> tokenClass) {
        if (tokenClass == Token.class) {
            throw new IllegalArgumentException("tokenClass must be a subclass of Token, not Token");
        } else if (tokenClass == Token.Basic.class) {
            throw new IllegalArgumentException(
                "The constructor TokenType(Class) must not be used for Token.Basic. Use TokenType(boolean, String)."
            );
        }
        this.tokenClass = tokenClass;
        this.isKeyword = false;
        this.basicText = null;
    }

    TokenType(boolean isKeyword, String basicText) {
        this.tokenClass = Token.Basic.class;
        this.isKeyword = isKeyword;
        this.basicText = basicText;
    }

    public Class<? extends Token> getTokenClass() {
        return tokenClass;
    }

    public boolean isKeyword() {
        return isKeyword;
    }

    public String getBasicText() {
        return basicText;
    }

    @Override
    public String toString() {
        return tokenClass == Token.Basic.class ? basicText : name();
    }

    public static TokenType getSimple(String text) {
        return SIMPLE_TOKENS.get(text);
    }

    public static TokenType getKeyword(String text) {
        return KEYWORDS.get(text);
    }
}
