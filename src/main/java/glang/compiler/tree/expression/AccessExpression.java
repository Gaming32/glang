package glang.compiler.tree.expression;

import glang.compiler.SourceLocation;
import glang.compiler.token.Token;
import glang.compiler.token.TokenType;

import java.util.Map;

public class AccessExpression extends ExpressionNode implements AssignableExpression {
    private final ExpressionNode target;
    private final String member;
    private final Operator operator;

    public AccessExpression(
        ExpressionNode target, String member, Operator operator,
        SourceLocation startLocation, SourceLocation endLocation
    ) {
        super(startLocation, endLocation);
        this.target = target;
        this.member = member;
        this.operator = operator;
    }

    public ExpressionNode getTarget() {
        return target;
    }

    public String getMember() {
        return member;
    }

    public Operator getOperator() {
        return operator;
    }

    @Override
    public StringBuilder print(StringBuilder result, int currentIndent, int indent) {
        result.append('(');
        target.print(result, currentIndent, indent);
        return result.append(')').append(operator).append(Token.Identifier.prettyPrint(member));
    }

    public enum Operator {
        SIMPLE(".", false),
        DIRECT(".!", false),
        METHOD("::", true),
        DIRECT_METHOD("::!", true);

        public static final Map<TokenType, Operator> BY_TOKEN = TokenType.byToken(values(), Operator::getText);

        private final String text;
        private final boolean methodAccess;

        Operator(String text, boolean methodAccess) {
            this.text = text;
            this.methodAccess = methodAccess;
        }

        public String getText() {
            return text;
        }

        public boolean isMethodAccess() {
            return methodAccess;
        }

        @Override
        public String toString() {
            return text;
        }

        public Operator toMethodAccess() {
            return switch (this) {
                case SIMPLE -> METHOD;
                case DIRECT -> DIRECT_METHOD;
                default -> this;
            };
        }
    }
}
