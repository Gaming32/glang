package glang.compiler.tree.expression;

import glang.compiler.SourceLocation;

import java.util.List;

public class CallExpression extends ExpressionNode {
    private final ExpressionNode target;
    private final List<ExpressionNode> args;

    public CallExpression(
        ExpressionNode target, List<ExpressionNode> args,
        SourceLocation startLocation, SourceLocation endLocation
    ) {
        super(startLocation, endLocation);
        this.target = target;
        this.args = List.copyOf(args);
    }

    public ExpressionNode getTarget() {
        return target;
    }

    public List<ExpressionNode> getArgs() {
        return args;
    }

    @Override
    public StringBuilder print(StringBuilder result, int currentIndent, int indent) {
        target.print(result, currentIndent, indent);
        result.append('(');
        if (!args.isEmpty()) {
            args.get(0).print(result, currentIndent, indent);
            for (int i = 1; i < args.size(); i++) {
                result.append(", ");
                args.get(i).print(result, currentIndent, indent);
            }
        }
        return result.append(')');
    }
}
