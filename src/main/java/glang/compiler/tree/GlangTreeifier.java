package glang.compiler.tree;

import glang.compiler.SourceLocation;
import glang.compiler.error.CompileFailedException;
import glang.compiler.error.ErrorCollector;
import glang.compiler.token.*;
import glang.compiler.tree.expression.*;
import glang.compiler.tree.statement.BlockStatement;
import glang.compiler.tree.statement.ExpressionStatement;
import glang.compiler.tree.statement.ImportStatement;
import glang.compiler.tree.statement.StatementNode;
import glang.util.GlangStringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.IntFunction;

public class GlangTreeifier {
    private final Token[] tokens;
    private final IntFunction<String> lineGetter;
    private final Token eof;

    private int index;
    private ErrorCollector errorCollector = new ErrorCollector();

    public GlangTreeifier(List<Token> tokens, IntFunction<String> lineGetter) {
        this.tokens = tokens.toArray(Token[]::new);
        this.lineGetter = lineGetter;

        if (!tokens.isEmpty()) {
            final SourceLocation end = this.tokens[this.tokens.length - 1].getLocation();
            this.eof = new Token.Basic(TokenType.EOF, new SourceLocation(end.line(), end.column() + end.length()));
        } else {
            this.eof = new Token.Basic(TokenType.EOF, SourceLocation.NULL);
        }
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
        reset(null);
        final StatementList result = statementList(TokenType.EOF);
        errorCollector.throwIfFailed();
        return result;
    }

    public StatementList treeify(ErrorCollector errorCollector) {
        reset(errorCollector);
        return statementList(TokenType.EOF);
    }

    public void reset(ErrorCollector newErrorCollector) {
        index = 0;
        if (newErrorCollector != null) {
            errorCollector = newErrorCollector;
        } else if (!errorCollector.getErrors().isEmpty()) {
            errorCollector = new ErrorCollector();
        }
    }

    public StatementList statementList(TokenType end) {
        final List<StatementNode> statements = new ArrayList<>();
        final SourceLocation startLocation = peek().getLocation();
        boolean foundAny = false;
        while (!check(end) && !check(TokenType.EOF)) {
            foundAny = true;
            final StatementNode statement = statement();
            if (statement != null) {
                statements.add(statement);
            }
        }
        final SourceLocation endLocation = foundAny ? getSourceLocation() : startLocation;
        return new StatementList(statements, startLocation, endLocation);
    }

    public BlockStatement block() {
        if (expectSafe(TokenType.LCURLY) == null) {
            final SourceLocation location = getSourceLocation();
            return new BlockStatement(new StatementList(List.of(), location, location), location, location);
        }

        final SourceLocation startLocation = getSourceLocation();
        final StatementList statements = statementList(TokenType.RCURLY);
        expect(TokenType.RCURLY);
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
            while (!check(TokenGroup.SAVEPOINT)) {
                next();
            }
            if (!check(TokenType.RCURLY)) {
                next();
            }
            return null;
        }
    }

    private StatementNode statement0() {
        if (check(TokenType.IMPORT)) {
            return importStatement();
        }
        if (check(TokenType.LCURLY)) {
            return block();
        }
        return expressionStatement();
    }

    private ImportStatement importStatement() {
        expect(TokenType.IMPORT);
        final SourceLocation startLocation = getSourceLocation();
        final List<String> parts = new ArrayList<>();
        while (true) {
            parts.add(((Token.Identifier)expect(TokenType.IDENTIFIER)).getIdentifier());
            if (check(TokenType.SEMI)) break;
            expect(TokenType.DOT);
        }
        final SourceLocation endLocation = getSourceLocation();
        return new ImportStatement(
            parts.subList(0, parts.size() - 1), parts.get(parts.size() - 1),
            startLocation, endLocation
        );
    }

    private ExpressionStatement expressionStatement() {
        final SourceLocation startLocation = peek().getLocation();
        final ExpressionNode expression = expression();
        expect(TokenType.SEMI);
        final SourceLocation endLocation = getSourceLocation();
        return new ExpressionStatement(expression, startLocation, endLocation);
    }

    private ExpressionNode expression() {
        return assignment();
    }

    // TODO: Specify source locations
    private ExpressionNode assignment() {
        final ExpressionNode variable = or();
        if (check(TokenGroup.ASSIGNMENT)) {
            final TokenType operator = next().getType();
            if (!(variable instanceof AssignableExpression)) {
                throw error(variable.getClass().getSimpleName() + " is not assignable");
            }
            final ExpressionNode value = assignment();
            return new AssignmentExpression(variable, operator, value, SourceLocation.NULL, SourceLocation.NULL);
        }
        return variable;
    }

    private ExpressionNode or() {
        ExpressionNode left = and();
        while (match(TokenType.OR_OR)) {
            final ExpressionNode right = and();
            left = new BinaryExpression(left, Operator.OR, right, SourceLocation.NULL, SourceLocation.NULL);
        }
        return left;
    }

    private ExpressionNode and() {
        ExpressionNode left = comparison();
        while (match(TokenType.AND_AND)) {
            final ExpressionNode right = comparison();
            left = new BinaryExpression(left, Operator.AND, right, SourceLocation.NULL, SourceLocation.NULL);
        }
        return left;
    }

    private ExpressionNode comparison() {
        ExpressionNode left = bitwiseOr();
        while (check(TokenGroup.COMPARISON)) {
            final Operator operator = Operator.binary(next().getType());
            final ExpressionNode right = bitwiseOr();
            left = new BinaryExpression(left, operator, right, SourceLocation.NULL, SourceLocation.NULL);
        }
        return left;
    }

    private ExpressionNode bitwiseOr() {
        ExpressionNode left = bitwiseXor();
        while (match(TokenType.OR)) {
            final ExpressionNode right = bitwiseXor();
            left = new BinaryExpression(left, Operator.BITWISE_OR, right, SourceLocation.NULL, SourceLocation.NULL);
        }
        return left;
    }

    private ExpressionNode bitwiseXor() {
        ExpressionNode left = bitwiseAnd();
        while (match(TokenType.CARET)) {
            final ExpressionNode right = bitwiseAnd();
            left = new BinaryExpression(left, Operator.BITWISE_XOR, right, SourceLocation.NULL, SourceLocation.NULL);
        }
        return left;
    }

    private ExpressionNode bitwiseAnd() {
        ExpressionNode left = bitShift();
        while (match(TokenType.AND)) {
            final ExpressionNode right = bitShift();
            left = new BinaryExpression(left, Operator.BITWISE_AND, right, SourceLocation.NULL, SourceLocation.NULL);
        }
        return left;
    }

    private ExpressionNode bitShift() {
        ExpressionNode left = term();
        while (check(TokenGroup.BIT_SHIFT)) {
            final Operator operator = Operator.binary(next().getType());
            final ExpressionNode right = term();
            left = new BinaryExpression(left, operator, right, SourceLocation.NULL, SourceLocation.NULL);
        }
        return left;
    }

    private ExpressionNode term() {
        ExpressionNode left = factor();
        while (check(TokenGroup.TERM)) {
            final Operator operator = Operator.binary(next().getType());
            final ExpressionNode right = factor();
            left = new BinaryExpression(left, operator, right, SourceLocation.NULL, SourceLocation.NULL);
        }
        return left;
    }

    private ExpressionNode factor() {
        ExpressionNode left = unary();
        if (check(TokenGroup.FACTOR)) {
            final Operator operator = Operator.binary(next().getType());
            final ExpressionNode right = unary();
            left = new BinaryExpression(left, operator, right, SourceLocation.NULL, SourceLocation.NULL);
        }
        return left;
    }

    private ExpressionNode unary() {
        if (check(TokenGroup.UNARY)) {
            final Operator operator = Operator.unary(next().getType());
            final ExpressionNode operand = unary();
            return new UnaryExpression(operator, operand, SourceLocation.NULL, SourceLocation.NULL);
        }
        return call();
    }

    private ExpressionNode call() {
        ExpressionNode target = primary();
        while (true) {
            if (match(TokenType.LPAREN)) {
                target = finishCall(target);
            } else if (match(TokenType.DOT)) {
                final String member = ((Token.Identifier)expect(TokenType.IDENTIFIER)).getIdentifier();
                target = new AccessExpression(target, member, SourceLocation.NULL, SourceLocation.NULL);
            } else {
                return target;
            }
        }
    }

    private CallExpression finishCall(ExpressionNode target) {
        if (match(TokenType.RPAREN)) {
            return new CallExpression(target, List.of(), SourceLocation.NULL, SourceLocation.NULL);
        }
        final List<ExpressionNode> args = new ArrayList<>();
        while (true) {
            final ExpressionNode arg = expression();
            args.add(arg);
            if (match(TokenType.RPAREN)) break;
            expect(TokenType.COMMA);
        }
        if (args.size() > 255) {
            throw error("Maximum number of args is 255. " + args.size() + " were passed");
        }
        return new CallExpression(target, args, SourceLocation.NULL, SourceLocation.NULL);
    }

    private ExpressionNode primary() {
        if (match(TokenType.TRUE)) {
            return new BooleanExpression(true, getSourceLocation());
        }
        if (match(TokenType.FALSE)) {
            return new BooleanExpression(false, getSourceLocation());
        }
        if (match(TokenType.NULL)) {
            return new NullExpression(getSourceLocation());
        }
        if (check(TokenType.IDENTIFIER)) {
            return new IdentifierExpression(((Token.Identifier)next()).getIdentifier(), getSourceLocation());
        }
        if (check(TokenType.STRING)) {
            return new StringExpression(((Token.Str)next()).getValue(), getSourceLocation());
        }
        if (check(TokenType.NUMBER)) {
            return new NumberExpression(((Token.Num)next()).getValue(), getSourceLocation());
        }
        if (match(TokenType.LPAREN)) {
            final ExpressionNode result = expression();
            expect(TokenType.RPAREN);
            return result;
        }
        throw error("Expected expression, found " + next().prettyPrint());
    }

    private boolean check(TokenType tokenType) {
        return peek().getType() == tokenType;
    }

    private boolean check(Set<TokenType> tokenTypes) {
        return tokenTypes.contains(peek().getType());
    }

    private boolean match(TokenType tokenType) {
        if (check(tokenType)) {
            next();
            return true;
        }
        return false;
    }

    @NotNull
    private <T extends Token> T expect(TokenType tokenType) {
        final T result = expectSafe(tokenType);
        if (result == null) {
            throw SkipStatement.INSTANCE;
        }
        return result;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private <T extends Token> T expectSafe(TokenType tokenType) {
        final Token next = next();
        if (next.getType() != tokenType) {
            errorSafe("Expected '" + tokenType + "', found '" + next.prettyPrint() + "'");
            return null;
        }
        return (T)next;
    }

    private SkipStatement error(String reason) {
        errorSafe(reason);
        throw SkipStatement.INSTANCE;
    }

    private void errorSafe(String reason) {
        final SourceLocation location = getSourceLocation();
        errorCollector.addError(reason, location, lineGetter.apply(location.line() - 1));
    }

    private SourceLocation getSourceLocation() {
        return last().getLocation();
    }

    private Token peek() {
        return index < tokens.length ? tokens[index] : eof;
    }

    private Token peek(int offset) {
        if (offset < 0) {
            throw new IllegalArgumentException("Cannot peek backwards. Use peekLast().");
        }
        return index + offset < tokens.length ? tokens[index + offset] : eof;
    }

    public Token last() {
        if (index == 0) {
            throw new IllegalArgumentException("Cannot peekLast() at index 0");
        }
        return tokens[index - 1];
    }

    private Token next() {
        if (index >= tokens.length) {
            return eof;
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
