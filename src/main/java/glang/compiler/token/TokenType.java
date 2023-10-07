package glang.compiler.token;

import glang.compiler.util.SymbolMap;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public enum TokenType {
    // Non-keywords
    PLUS_PLUS("++"),
    MINUS_MINUS("--"),
    PLUS("+"),
    MINUS("-"),
    TILDE("~"),
    BANG("!"),
    STAR("*"),
    SLASH("/"),
    PERCENT("%"),
    SHL("<<"),
    SHR(">>"),
    LESS("<"),
    GREATER(">"),
    LESS_EQUAL("<="),
    GREATER_EQUAL(">="),
    EQUAL_EQUAL("=="),
    EQUAL_EQUAL_EQUAL("==="),
    NOT_EQUAL("!="),
    NOT_EQUAL_EQUAL("!=="),
    AND("&"),
    CARET("^"),
    OR("|"),
    AND_AND("&&"),
    OR_OR("||"),
    QUESTION("?"),
    COLON(":"),
    EQUAL("="),
    PLUS_EQUAL("+="),
    MINUS_EQUAL("-="),
    STAR_EQUAL("*="),
    SLASH_EQUAL("/="),
    PERCENT_EQUAL("%="),
    AND_EQUAL("&="),
    CARET_EQUAL("^="),
    OR_EQUAL("|="),
    SHL_EQUAL("<<="),
    SHR_EQUAL(">>="),
    COMMA(","),
    SEMI(";"),
    LBRACKET("["),
    RBRACKET("]"),
    LPAREN("("),
    RPAREN(")"),
    LCURLY("{"),
    RCURLY("}"),
    DOT("."),
    DOT_BANG(".!"),
    COLON_COLON("::"),
    COLON_COLON_BANG("::!"),
    // Keywords
    IMPORT("import"),
    VAR("var"),
    FN("fn"),
    PUBLIC("public"),
    PRIVATE("private"),
    STATIC("static"),
    INSTANCEOF("instanceof"),
    FINAL("final"),
    CLASS("class"),
    TRUE("true"),
    FALSE("false"),
    NULL("null"),
    // Generic
    IDENTIFIER(Token.Identifier.class),
    STRING(Token.Str.class),
    NUMBER(Token.Num.class),
    EOF("<<EOF>>")
    ;

    public static final SymbolMap<TokenType> SIMPLE_TOKENS = new SymbolMap<>();
    public static final Map<String, TokenType> KEYWORDS = new HashMap<>();

    static {
        for (final TokenType type : values()) {
            if (type.basicText == null || type == EOF) continue;
            if (type.isKeyword()) {
                KEYWORDS.put(type.basicText, type);
            } else {
                SIMPLE_TOKENS.put(type.basicText, type);
            }
        }
    }

    private final Class<? extends Token> tokenClass;
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
        this.basicText = null;
    }

    TokenType(String basicText) {
        this.tokenClass = Token.Basic.class;
        this.basicText = basicText;
    }

    public Class<? extends Token> getTokenClass() {
        return tokenClass;
    }

    public boolean isKeyword() {
        return basicText != null && Character.isJavaIdentifierStart(basicText.charAt(0));
    }

    public String getBasicText() {
        return basicText;
    }

    @Override
    public String toString() {
        return basicText != null ? basicText : name().toLowerCase(Locale.ROOT);
    }
}
