package glang.compiler.token;

import java.util.List;

public class TokenSourcePrinter {
    public static String print(List<Token> tokens) {
        return print(tokens, new StringBuilder()).toString();
    }

    public static StringBuilder print(List<Token> tokens, StringBuilder result) {
        if (tokens.isEmpty()) {
            return result;
        }
        result.append(tokens.get(0).prettyPrint());
        for (int i = 1; i < tokens.size(); i++) {
            final Token prevToken = tokens.get(i - 1);
            final Token token = tokens.get(i);
            final int newLines = token.getLocation().line() - prevToken.getLocation().line();
            if (newLines > 0) {
                result.append("\n".repeat(newLines));
                final int spaces = token.getLocation().column() - 1;
                if (spaces > 0) {
                    result.append(" ".repeat(spaces));
                }
            } else if (newLines == 0) {
                final int spaces = token.getLocation().column() - prevToken.getLocation().column() - prevToken.getLocation().length();
                if (spaces > 0) {
                    result.append(" ".repeat(spaces));
                }
            }
            result.append(token.prettyPrint());
        }
        return result;
    }
}
