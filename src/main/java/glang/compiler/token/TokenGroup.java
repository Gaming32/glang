package glang.compiler.token;

import java.util.EnumSet;
import java.util.Set;

import static glang.compiler.token.TokenType.*;

public class TokenGroup {
    public static final Set<TokenType> ASSIGNMENT = EnumSet.range(EQUAL, SHR_EQUAL);
    public static final Set<TokenType> COMPARISON = EnumSet.range(LESS, NOT_EQUAL_EQUAL);
    public static final Set<TokenType> BIT_SHIFT = EnumSet.of(SHL, SHR);
    public static final Set<TokenType> TERM = EnumSet.of(PLUS, MINUS);
    public static final Set<TokenType> FACTOR = EnumSet.of(STAR, SLASH, PERCENT);
    public static final Set<TokenType> UNARY = EnumSet.of(BANG, PLUS, MINUS, TILDE);
    public static final Set<TokenType> ACCESS = EnumSet.range(DOT, COLON_COLON_BANG);
    public static final Set<TokenType> SAVEPOINT = EnumSet.of(SEMI, RCURLY, EOF);
}
