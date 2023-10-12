package glang.compiler.token;

import glang.compiler.SourceLocation;
import glang.compiler.util.SymbolMap;
import glang.util.GlangStringUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.BiFunction;

public final class GlangTokenizer {
    private static final char EOF = '\0';

    private final char[] source;
    private String sourceString;

    private int index, line, column;
    private boolean wasNewLine, advancedPastEof;

    public GlangTokenizer(String source) {
        this.source = source.toCharArray();
        this.sourceString = source;
    }

    public GlangTokenizer(char[] source) {
        this.source = Arrays.copyOf(source, source.length);
    }

    public static List<Token> tokenize(String source) throws TokenizeFailure {
        return new GlangTokenizer(source).tokenize();
    }

    public String getSource() {
        if (sourceString == null) {
            sourceString = new String(source);
        }
        return sourceString;
    }

    public List<Token> tokenize() throws TokenizeFailure {
        reset();
        final List<Token> result = new ArrayList<>();
        final StringBuilder tokenBuilder = new StringBuilder();
        char c;
        while ((c = next()) != EOF) {
            if (Character.isWhitespace(c)) continue;
            switch (c) {
                case '"':
                case '\'':
                    handleString(result, tokenBuilder, c, Token.Str::new);
                    continue;
                case '`':
                    handleString(result, tokenBuilder, '`', Token.Identifier::new);
                    continue;
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    handleNumber(result, tokenBuilder, c);
                    continue;
                case '.': {
                    final char peeked = peek();
                    if (peeked >= '0' && peeked <= '9') {
                        handleNumber(result, tokenBuilder, c);
                        continue;
                    }
                    break;
                }
                case '/': {
                    char peeked = peek();
                    if (peeked == '/') {
                        next();
                        do {
                            peeked = next();
                        } while (peeked != '\n' && peeked != EOF);
                        continue;
                    }
                    if (peeked == '*') {
                        next();
                        final int startIndex = index;
                        final int startLine = line;
                        final int startColumn = column;
                        while (true) {
                            peeked = next();
                            if (peeked == '*' && peek() == '/') {
                                next();
                                break;
                            } else if (peeked == EOF) {
                                index = startIndex;
                                line = startLine;
                                column = startColumn;
                                throw error("Unterminated /*", 2);
                            }
                        }
                        continue;
                    }
                }
            }
            if (Character.isJavaIdentifierStart(c)) {
                handleIdentifier(result, tokenBuilder, c);
                continue;
            }
            handleSimple(result, tokenBuilder, c);
        }
        return List.copyOf(result);
    }

    private void handleIdentifier(List<Token> result, StringBuilder tokenBuilder, char firstChar) {
        tokenBuilder.setLength(0);
        tokenBuilder.append(firstChar);
        while (peek() != EOF && Character.isJavaIdentifierPart(peek())) {
            tokenBuilder.append(next());
        }
        final String token = tokenBuilder.toString();
        final TokenType keyword = TokenType.KEYWORDS.get(token);
        if (keyword != null) {
            result.add(new Token.Basic(keyword, getSourceLocation(token.length())));
            return;
        }
        result.add(new Token.Identifier(token, getSourceLocation(token.length())));
    }

    // NOTE: This has an issue where if ab and abcd are valid, but abc isn't, abcab will cause an error instead of
    // becoming [ab, c, ab]. However, no existing tokens match have this interaction, so this is fine for now.
    private void handleSimple(List<Token> result, StringBuilder tokenBuilder, char firstChar) throws TokenizeFailure {
        tokenBuilder.setLength(0);
        SymbolMap<TokenType> symbolMap = TokenType.SIMPLE_TOKENS.getNext(firstChar);
        if (symbolMap == null) {
            throw error("Unknown token '" + firstChar + "'");
        }
        tokenBuilder.append(firstChar);
        while (true) {
            final char peeked = peek(tokenBuilder.length() - 1);
            final SymbolMap<TokenType> next = symbolMap.getNext(peeked);
            if (next == null) break;
            symbolMap = next;
            tokenBuilder.append(peeked);
        }
        skipFast(tokenBuilder.length() - 1);
        if (symbolMap.getValue() == null) {
            throw error("Unknown token '" + tokenBuilder + "'", tokenBuilder.length());
        }
        result.add(new Token.Basic(symbolMap.getValue(), getSourceLocation(tokenBuilder.length())));
    }

    private void handleString(
        List<Token> result, StringBuilder tokenBuilder, char terminator,
        BiFunction<String, SourceLocation, Token> tokenType
    ) throws TokenizeFailure {
        tokenBuilder.setLength(0);
        final int startColumn = column;
        char c;
        while ((c = next()) != terminator) {
            if (c == '\\') {
                final char escapeCode = next();
                switch (escapeCode) {
                    case EOF -> throw error("Expected escape code");
                    case '0' -> tokenBuilder.append('\0');
                    case 't' -> tokenBuilder.append('\t');
                    case 'b' -> tokenBuilder.append('\b');
                    case 'n' -> tokenBuilder.append('\n');
                    case 'r' -> tokenBuilder.append('\r');
                    case 'f' -> tokenBuilder.append('\f');
                    case '\'' -> tokenBuilder.append('\'');
                    case '"' -> tokenBuilder.append('"');
                    case '`' -> tokenBuilder.append('`');
                    case '\\' -> tokenBuilder.append('\\');
                    case 'x', 'u', 'U' -> {
                        final int digitCount = switch (escapeCode) {
                            case 'x' -> 2;
                            case 'u' -> 4;
                            case 'U' -> 8;
                            default -> throw new AssertionError();
                        };
                        final char[] digits = new char[digitCount];
                        for (int i = 0; i < digitCount; i++) {
                            final char digit = next();
                            if (digit == '\n' || digit == EOF) {
                                throw error("Unfinished \\" + escapeCode + " escape");
                            }
                            if (Character.digit(digit, 16) == -1) {
                                throw error("'" + digit + "' not a digit in \\" + escapeCode + " escape");
                            }
                            digits[i] = digit;
                        }
                        final int codepoint = Integer.parseUnsignedInt(new String(digits), 16);
                        if (!Character.isValidCodePoint(codepoint)) {
                            throw error(
                                "Invalid codepoint U+" +
                                    Integer.toHexString(codepoint).toUpperCase(Locale.ROOT),
                                digitCount
                            );
                        }
                        tokenBuilder.appendCodePoint(codepoint);
                    }
                    default -> throw error("Unknown escape code \\" + escapeCode);
                }
                continue;
            }
            if (c == '\n' || c == EOF) {
                throw error("Unterminated string literal");
            }
            tokenBuilder.append(c);
        }
        result.add(tokenType.apply(tokenBuilder.toString(), getSourceLocation(column - startColumn + 1)));
    }

    private void handleNumber(List<Token> result, StringBuilder tokenBuilder, char firstChar) throws TokenizeFailure {
        tokenBuilder.setLength(0);
        tokenBuilder.append(firstChar);
        final int start = index;

        int radix = 10;
        int radixSkip = 0;
        if (firstChar == '0') {
            final char indicator = peek();
            if (indicator >= '1' && indicator <= '9') {
                radixSkip = 1;
                radix = 8;
            } else if (indicator == 'o' || indicator == 'O') {
                radixSkip = 2;
                radix = 8;
            } else if (indicator == 'x' || indicator == 'X') {
                radixSkip = 2;
                radix = 16;
            } else if (indicator == 'b' || indicator == 'B') {
                radixSkip = 2;
                radix = 2;
            }
        }
        if (radixSkip > 1) {
            skip(radixSkip - 1);
        }

        boolean hasDecimal = firstChar == '.';
        while (true) {
            final char c = peek();
            if (Character.digit(c, radix) != -1) {
                next();
                continue;
            }
            if (c == '.') {
                next();
                final char peeked = peek();
                if (hasDecimal || radix != 10 || peeked < '0' || peeked > '9') {
                    rewind(1);
                    break;
                } else {
                    hasDecimal = true;
                }
                continue;
            }
            if (c == 'e' || c == 'E') {
                next();
                hasDecimal = true;
                final char peeked = peek();
                if (peeked == '-' || peeked == '+') {
                    next();
                }
                continue;
            }
            break;
        }
        tokenBuilder.append(source, start, index - start);

        final String token = tokenBuilder.substring(radixSkip);
        if (token.isEmpty()) {
            next();
            throw error("Expected number");
        }

        final Number number = switch (peek()) {
            case 'D', 'd' -> {
                next();
                try {
                    yield Double.parseDouble(token);
                } catch (NumberFormatException e) {
                    throw error("Invalid D number: " + e.getMessage(), tokenBuilder.length() + 1);
                }
            }
            case 'L', 'l' -> {
                next();
                try {
                    yield Long.parseLong(token, radix);
                } catch (NumberFormatException e) {
                    throw error("Invalid L number: " + e.getMessage(), tokenBuilder.length() + 1);
                }
            }
            case 'B', 'b' -> {
                next();
                try {
                    yield new BigInteger(token, radix);
                } catch (NumberFormatException e) {
                    throw error("Invalid B number: " + e.getMessage(), tokenBuilder.length() + 1);
                }
            }
            default -> {
                if (hasDecimal) {
                    try {
                        yield Double.parseDouble(token);
                    } catch (NumberFormatException e) {
                        throw error("Invalid decimal: " + e.getMessage(), tokenBuilder.length());
                    }
                } else {
                    try {
                        yield Integer.parseInt(token, radix);
                    } catch (NumberFormatException e1) {
                        try {
                            yield Long.parseLong(token, radix);
                        } catch (NumberFormatException e2) {
                            try {
                                yield new BigInteger(token, radix);
                            } catch (NumberFormatException e3) {
                                throw error("Invalid integer: " + e3.getMessage(), tokenBuilder.length());
                            }
                        }
                    }
                }
            }
        };

        result.add(new Token.Num(number, getSourceLocation(tokenBuilder.length())));
    }

    private TokenizeFailure error(String reason) {
        return error(reason, 1);
    }

    private TokenizeFailure error(String reason, int length) {
        return new TokenizeFailure(
            reason, getSourceLocation(length),
            GlangStringUtils.getLine(getSource(), line - 1)
        );
    }

    private SourceLocation getSourceLocation() {
        return new SourceLocation(line, column);
    }

    private SourceLocation getSourceLocation(int length) {
        return new SourceLocation(line, column - length + 1, length);
    }

    private void reset() {
        index = 0;
        line = 1;
        column = 0; // 0 means next() has never been called
        wasNewLine = false;
    }

    private char peek() {
        return index < source.length ? source[index] : EOF;
    }

    private char peek(int offset) {
        if (offset < 0) {
            throw new IllegalArgumentException("Cannot peek backwards. Use peekLast().");
        }
        return index + offset < source.length ? source[index + offset] : EOF;
    }

    private char last() {
        if (index == 0) {
            throw new IllegalStateException("Cannot peekLast() at index 0");
        }
        return source[index - 1];
    }

    private char next() {
        if (index >= source.length) {
            if (!advancedPastEof) {
                column++;
                advancedPastEof = true;
            }
            return EOF;
        }
        if (wasNewLine) {
            line++;
            column = 0;
            wasNewLine = false;
        }
        final char c = source[index++];
        if (c == '\n') {
            wasNewLine = true;
        }
        column++;
        return c;
    }

    private void rewind(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Cannot rewind negative distance. Use skip().");
        }
        if (n > index) {
            throw new IllegalStateException(
                "Cannot rewind " + n + " chars, as that would be before the start of the source"
            );
        }
        if (n > column) {
            throw new IllegalStateException(
                "Cannot rewind " + n + " chars, as that would be before the start of the current line"
            );
        }
        index -= n;
        column -= n;
    }

    private void skip(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Cannot skip negative distance. Use rewind().");
        }
        // TODO: Optimize?
        for (int i = 0; i < n; i++) {
            next();
        }
    }

    private void skipFast(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Cannot skip negative distance. Use rewind().");
        }
        if (index + n > source.length) {
            throw new IllegalArgumentException("Cannot skipFast past the end of the source.");
        }
        index += n;
        column += n;
    }
}
