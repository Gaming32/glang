package glang.compiler.tree;

import glang.compiler.token.GlangTokenizer;
import glang.compiler.token.Token;
import glang.compiler.token.TokenizeFailure;
import glang.util.GlangStringUtils;

import java.util.List;
import java.util.function.IntFunction;

public class GlangTreeifier {
    private final List<Token> tokens;
    private final IntFunction<String> lineGetter;

    public GlangTreeifier(List<Token> tokens, IntFunction<String> lineGetter) {
        this.tokens = List.copyOf(tokens);
        this.lineGetter = lineGetter;
    }

    public GlangTreeifier(String source) throws TokenizeFailure {
        this(GlangTokenizer.tokenize(source), i -> GlangStringUtils.getLine(source, i));
    }
}
