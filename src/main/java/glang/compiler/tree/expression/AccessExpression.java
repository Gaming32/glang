package glang.compiler.tree.expression;

import glang.compiler.SourceLocation;
import glang.compiler.token.Token;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AccessExpression extends ExpressionNode implements AssignableExpression {
    private final ExpressionNode target;
    private final String member;
    private final Type type;

    public AccessExpression(
        ExpressionNode target, String member, Type type,
        SourceLocation startLocation, SourceLocation endLocation
    ) {
        super(startLocation, endLocation);
        this.target = target;
        this.member = member;
        this.type = type;
    }

    public ExpressionNode getTarget() {
        return target;
    }

    public String getMember() {
        return member;
    }

    public Type getType() {
        return type;
    }

    @Override
    public StringBuilder print(StringBuilder result, int currentIndent, int indent) {
        result.append('(');
        target.print(result, currentIndent, indent);
        return result.append(')').append(type).append(Token.Identifier.prettyPrint(member));
    }

    public enum Type {
        SIMPLE(".", false),
        DIRECT(".!", false),
        METHOD("::", true),
        DIRECT_METHOD("::!", true);

        public static final Map<String, Type> BY_TEXT = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(Type::getText, Function.identity()));

        private final String text;
        private final boolean methodAccess;

        Type(String text, boolean methodAccess) {
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

        public Type toMethodAccess() {
            return switch (this) {
                case SIMPLE -> METHOD;
                case DIRECT -> DIRECT_METHOD;
                default -> this;
            };
        }
    }
}
