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
        SIMPLE("."),
        DIRECT(".!"),
        METHOD("::"),
        DIRECT_METHOD("::!");

        public static final Map<String, Type> BY_TEXT = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(Type::getText, Function.identity()));

        private final String text;

        Type(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
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
