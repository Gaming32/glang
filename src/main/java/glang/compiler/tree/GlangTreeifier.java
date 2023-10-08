package glang.compiler.tree;

import glang.compiler.SourceLocation;
import glang.compiler.error.CompileFailedException;
import glang.compiler.error.ErrorCollector;
import glang.compiler.token.*;
import glang.compiler.tree.expression.*;
import glang.compiler.tree.statement.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.IntFunction;

public class GlangTreeifier {
    private static final List<Class<? extends StatementNode>> BLOCKED_BODY_STATEMENTS = List.of(
        ImportStatement.class, VariableDeclaration.class
    );

    private final Token[] tokens;
    private final ErrorCollector errorCollector;
    private final Token eof;

    private int index;

    public GlangTreeifier(List<Token> tokens, ErrorCollector errorCollector) {
        this.tokens = tokens.toArray(Token[]::new);
        this.errorCollector = errorCollector;

        if (!tokens.isEmpty()) {
            final SourceLocation end = this.tokens[this.tokens.length - 1].getLocation();
            this.eof = new Token.Basic(TokenType.EOF, new SourceLocation(end.line(), end.column() + end.length()));
        } else {
            this.eof = new Token.Basic(TokenType.EOF, SourceLocation.NULL);
        }
    }

    public GlangTreeifier(List<Token> tokens, IntFunction<String> lineGetter) {
        this(tokens, new ErrorCollector(lineGetter));
    }

    public GlangTreeifier(String source) throws TokenizeFailure {
        this(GlangTokenizer.tokenize(source), new ErrorCollector(source));
    }

    public static StatementList treeify(String source) throws CompileFailedException {
        final GlangTreeifier treeifier = new GlangTreeifier(source);
        final StatementList result = treeifier.treeify();
        treeifier.errorCollector.throwIfFailed();
        return result;
    }

    public StatementList treeify() {
        reset();
        return statementList(TokenType.EOF);
    }

    public ErrorCollector getErrorCollector() {
        return errorCollector;
    }

    public void reset() {
        index = 0;
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
        if (check(TokenType.LCURLY)) {
            return block();
        }
        if (check(TokenType.IMPORT)) {
            return importStatement();
        }
        if (check(TokenType.VAR)) {
            return variableDeclaration();
        }
        if (check(TokenType.IF)) {
            return ifStatement();
        }
        return expressionStatement();
    }

    private ImportStatement importStatement() {
        expect(TokenType.IMPORT);
        final SourceLocation startLocation = getSourceLocation();
        final List<String> parts = new ArrayList<>();
        do {
            if (match(TokenType.STAR)) {
                parts.add(null);
                break;
            }
            parts.add(((Token.Identifier)expect(TokenType.IDENTIFIER)).getIdentifier());
        } while (match(TokenType.DOT));
        endOfStatement();
        final SourceLocation endLocation = getSourceLocation();
        return new ImportStatement(
            parts.subList(0, parts.size() - 1), parts.get(parts.size() - 1),
            startLocation, endLocation
        );
    }

    private VariableDeclaration variableDeclaration() {
        expect(TokenType.VAR);
        final SourceLocation startLocation = getSourceLocation();
        final String name = ((Token.Identifier)expect(TokenType.IDENTIFIER)).getIdentifier();
        ExpressionNode initializer = null;
        if (match(TokenType.EQUAL)) {
            initializer = expression();
        }
        endOfStatement();
        final SourceLocation endLocation = getSourceLocation();
        return new VariableDeclaration(name, initializer, startLocation, endLocation);
    }

    private IfStatement ifStatement() {
        expect(TokenType.IF);
        final SourceLocation startLocation = getSourceLocation();
        final ExpressionNode condition = expression();
        final StatementNode body = conditionalBody("if");
        final StatementNode elseBody;
        if (match(TokenType.ELSE)) {
            elseBody = conditionalBody("else");
        } else {
            elseBody = null;
        }
        final SourceLocation endLocation = getSourceLocation();
        return new IfStatement(condition, body, elseBody, startLocation, endLocation);
    }

    private StatementNode conditionalBody(String statementType) {
        final StatementNode body = statement();
        for (final var blocked : BLOCKED_BODY_STATEMENTS) {
            if (blocked.isInstance(body)) {
                errorSafe("Statement not allowed in " + statementType + " body");
            }
        }
        return body;
    }

    private ExpressionStatement expressionStatement() {
        final SourceLocation startLocation = peek().getLocation();
        final ExpressionNode expression = expression();
        endOfStatement();
        final SourceLocation endLocation = getSourceLocation();
        return new ExpressionStatement(expression, startLocation, endLocation);
    }

    private void endOfStatement() {
        if (
            !match(TokenType.SEMI) && !check(TokenType.RCURLY) && !check(TokenType.EOF) &&
                peek().getLocation().line() == last().getLocation().line()
        ) {
            next();
            errorSafe("Multiple statements on one line should be separated with a semicolon");
            rewind(1);
        }
    }

    private ExpressionNode expression() {
        return assignment();
    }

    // TODO: Specify source locations
    private ExpressionNode assignment() {
        final SourceLocation startLocation = peek().getLocation();
        final ExpressionNode variable = or();
        if (check(TokenGroup.ASSIGNMENT)) {
            final TokenType operator = next().getType();
            if (!(variable instanceof AssignableExpression)) {
                throw error(variable.getClass().getSimpleName() + " is not assignable");
            }
            final ExpressionNode value = assignment();
            final SourceLocation endLocation = getSourceLocation();
            return new AssignmentExpression(variable, operator, value, startLocation, endLocation);
        }
        return variable;
    }

    private ExpressionNode or() {
        final SourceLocation startLocation = peek().getLocation();
        ExpressionNode left = and();
        while (match(TokenType.OR_OR)) {
            final ExpressionNode right = and();
            final SourceLocation endLocation = getSourceLocation();
            left = new BinaryExpression(left, Operator.OR, right, startLocation, endLocation);
        }
        return left;
    }

    private ExpressionNode and() {
        final SourceLocation startLocation = peek().getLocation();
        ExpressionNode left = comparison();
        while (match(TokenType.AND_AND)) {
            final ExpressionNode right = comparison();
            final SourceLocation endLocation = getSourceLocation();
            left = new BinaryExpression(left, Operator.AND, right, startLocation, endLocation);
        }
        return left;
    }

    private ExpressionNode comparison() {
        final SourceLocation startLocation = peek().getLocation();
        ExpressionNode left = bitwiseOr();
        while (check(TokenGroup.COMPARISON)) {
            final Operator operator = Operator.binary(next().getType());
            final ExpressionNode right = bitwiseOr();
            final SourceLocation endLocation = getSourceLocation();
            left = new BinaryExpression(left, operator, right, startLocation, endLocation);
        }
        return left;
    }

    private ExpressionNode bitwiseOr() {
        final SourceLocation startLocation = peek().getLocation();
        ExpressionNode left = bitwiseXor();
        while (match(TokenType.OR)) {
            final ExpressionNode right = bitwiseXor();
            final SourceLocation endLocation = getSourceLocation();
            left = new BinaryExpression(left, Operator.BITWISE_OR, right, startLocation, endLocation);
        }
        return left;
    }

    private ExpressionNode bitwiseXor() {
        final SourceLocation startLocation = peek().getLocation();
        ExpressionNode left = bitwiseAnd();
        while (match(TokenType.CARET)) {
            final ExpressionNode right = bitwiseAnd();
            final SourceLocation endLocation = getSourceLocation();
            left = new BinaryExpression(left, Operator.BITWISE_XOR, right, startLocation, endLocation);
        }
        return left;
    }

    private ExpressionNode bitwiseAnd() {
        final SourceLocation startLocation = peek().getLocation();
        ExpressionNode left = bitShift();
        while (match(TokenType.AND)) {
            final ExpressionNode right = bitShift();
            final SourceLocation endLocation = getSourceLocation();
            left = new BinaryExpression(left, Operator.BITWISE_AND, right, startLocation, endLocation);
        }
        return left;
    }

    private ExpressionNode bitShift() {
        final SourceLocation startLocation = peek().getLocation();
        ExpressionNode left = term();
        while (check(TokenGroup.BIT_SHIFT)) {
            final Operator operator = Operator.binary(next().getType());
            final ExpressionNode right = term();
            final SourceLocation endLocation = getSourceLocation();
            left = new BinaryExpression(left, operator, right, startLocation, endLocation);
        }
        return left;
    }

    private ExpressionNode term() {
        final SourceLocation startLocation = peek().getLocation();
        ExpressionNode left = factor();
        while (check(TokenGroup.TERM)) {
            final Operator operator = Operator.binary(next().getType());
            final ExpressionNode right = factor();
            final SourceLocation endLocation = getSourceLocation();
            left = new BinaryExpression(left, operator, right, startLocation, endLocation);
        }
        return left;
    }

    private ExpressionNode factor() {
        final SourceLocation startLocation = peek().getLocation();
        ExpressionNode left = unary();
        if (check(TokenGroup.FACTOR)) {
            final Operator operator = Operator.binary(next().getType());
            final ExpressionNode right = unary();
            final SourceLocation endLocation = getSourceLocation();
            left = new BinaryExpression(left, operator, right, startLocation, endLocation);
        }
        return left;
    }

    private ExpressionNode unary() {
        final SourceLocation startLocation = peek().getLocation();
        if (check(TokenGroup.UNARY)) {
            final Operator operator = Operator.unary(next().getType());
            final ExpressionNode operand = unary();
            final SourceLocation endLocation = getSourceLocation();
            return new UnaryExpression(operator, operand, startLocation, endLocation);
        }
        return call();
    }

    private ExpressionNode call() {
        final SourceLocation startLocation = peek().getLocation();
        ExpressionNode target = primary();
        while (true) {
            if (match(TokenType.LPAREN)) {
                target = finishCall(startLocation, target);
            } else if (check(TokenGroup.ACCESS)) {
                final AccessExpression.Type type = AccessExpression.Type.BY_TEXT.get(next().getType().toString());
                final String member = ((Token.Identifier)expect(TokenType.IDENTIFIER)).getIdentifier();
                final SourceLocation endLocation = getSourceLocation();
                target = new AccessExpression(target, member, type, startLocation, endLocation);
            } else {
                return target;
            }
        }
    }

    private CallExpression finishCall(SourceLocation startLocation, ExpressionNode target) {
        if (match(TokenType.RPAREN)) {
            return new CallExpression(target, List.of(), startLocation, getSourceLocation());
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
        final SourceLocation endLocation = getSourceLocation();
        return new CallExpression(target, args, startLocation, endLocation);
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
        errorCollector.addError(reason, location);
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
