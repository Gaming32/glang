package glang.compiler.tree;

import glang.compiler.SourceLocation;
import glang.compiler.error.CompileFailedException;
import glang.compiler.error.ErrorCollector;
import glang.compiler.token.GlangTokenizer;
import glang.compiler.token.Token;
import glang.compiler.token.TokenType;
import glang.compiler.token.TokenizeFailure;
import glang.compiler.tree.statement.BlockStatement;
import glang.compiler.tree.statement.StatementNode;
import glang.util.GlangStringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;

public class GlangTreeifier {
    private static final Token EOF = new Token.Basic(TokenType.EOF, new SourceLocation(1, 1));

    private final Token[] tokens;
    private final IntFunction<String> lineGetter;

    private int index;
    private ErrorCollector errorCollector = new ErrorCollector();

    public GlangTreeifier(List<Token> tokens, IntFunction<String> lineGetter) {
        this.tokens = tokens.toArray(Token[]::new);
        this.lineGetter = lineGetter;
    }

    public GlangTreeifier(String source) throws TokenizeFailure {
        this(GlangTokenizer.tokenize(source), i -> GlangStringUtils.getLine(source, i));
    }

    public static StatementList treeify(List<Token> tokens, IntFunction<String> lineGetter) throws CompileFailedException {
        return new GlangTreeifier(tokens, lineGetter).treeify();
    }

    public static StatementList treeify(String source) throws CompileFailedException {
        return new GlangTreeifier(source).treeify();
    }

    public StatementList treeify() throws CompileFailedException {
        reset();
        final StatementList result = statementList(TokenType.EOF);
        errorCollector.throwIfFailed();
        return result;
    }

    public void reset() {
        index = 0;
        if (!errorCollector.getErrors().isEmpty()) {
            errorCollector = new ErrorCollector();
        }
    }

    public ErrorCollector getErrorCollector() {
        return errorCollector;
    }

    public StatementList statementList(TokenType end) {
        final List<StatementNode> statements = new ArrayList<>();
        SourceLocation firstLocation = null;
        SourceLocation lastLocation = null;
        while (!check(end) && !check(TokenType.EOF)) {
            if (firstLocation == null) {
                firstLocation = peek().getLocation();
            }
            final StatementNode statement = statement();
            lastLocation = peekLast().getLocation();
            if (statement != null) {
                statements.add(statement);
            }
        }
        if (firstLocation == null || lastLocation == null) {
            firstLocation = lastLocation = peek().getLocation();
        }
        return new StatementList(statements, firstLocation, lastLocation);
    }

    public BlockStatement block() {
        if (!expectSafe(TokenType.LCURLY)) {
            final SourceLocation location = getSourceLocation();
            return new BlockStatement(new StatementList(List.of(), location, location), location, location);
        }

        final SourceLocation startLocation = getSourceLocation();
        final StatementList statements = statementList(TokenType.RCURLY);
        if (!check(TokenType.RCURLY)) {
            errorSafe("Unterminated block. Expected }.");
        }
        final SourceLocation endLocation = getSourceLocation();

        return new BlockStatement(statements, startLocation, endLocation);
    }

    /**
     * @apiNote Returns {@code null} if the expression is invalid.
     */
    public StatementNode statement() {
        try {
            return statement0();
        } catch (SkipStatement e) {
            while (!check(TokenType.SEMI) && !check(TokenType.EOF)) {
                next();
            }
            next();
            return null;
        }
    }

    private StatementNode statement0() {
        throw SkipStatement.INSTANCE; // TODO
    }

    private boolean check(TokenType tokenType) {
        return peek().getType() == tokenType;
    }

    private void expect(TokenType tokenType) {
        if (!expectSafe(tokenType)) {
            throw SkipStatement.INSTANCE;
        }
    }

    private boolean expectSafe(TokenType tokenType) {
        final Token next = next();
        if (next.getType() != tokenType) {
            errorSafe("Expected " + tokenType + ", found " + next.prettyPrint());
            return false;
        }
        return true;
    }

    private void error(String reason) {
        errorSafe(reason);
        throw SkipStatement.INSTANCE;
    }

    private void errorSafe(String reason) {
        final SourceLocation location = getSourceLocation();
        errorCollector.addError(reason, location, lineGetter.apply(location.line() - 1));
    }

    private SourceLocation getSourceLocation() {
        return peekLast().getLocation();
    }

    private Token peek() {
        return index < tokens.length ? tokens[index] : EOF;
    }

    private Token peek(int offset) {
        if (offset < 0) {
            throw new IllegalArgumentException("Cannot peek backwards. Use peekLast().");
        }
        return index + offset < tokens.length ? tokens[index + offset] : EOF;
    }

    public Token peekLast() {
        if (index == 0) {
            throw new IllegalArgumentException("Cannot peekLast() at index 0");
        }
        return tokens[index - 1];
    }

    private Token next() {
        if (index >= tokens.length) {
            return EOF;
        }
        return tokens[index++];
    }

    private void rewind(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Cannot rewind negative distance. Use skip().");
        }
        if (n > index) {
            throw new IllegalStateException(
                "Cannot rewind " + n + " tokens, as that would be before the start of the tokens list"
            );
        }
        index -= n;
    }

    private void skip(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Cannot skip negative distance. Use rewind().");
        }
        index = Math.min(index + n, tokens.length);
    }

    private static class SkipStatement extends RuntimeException {
        private static final SkipStatement INSTANCE = new SkipStatement();

        private SkipStatement() {
            if (INSTANCE != null) {
                throw new IllegalStateException("Use SkipStatement.INSTANCE instead of new SkipStatement().");
            }
        }

        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }
}
