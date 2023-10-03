import glang.compiler.token.GlangTokenizer;
import glang.compiler.token.Token;
import glang.compiler.token.TokenSourcePrinter;
import glang.compiler.token.TokenizeFailure;

import java.util.List;

public class TestMain {

    private static final String SOURCE = """
        println("Hello, world!"); // Comment!
        println("a, b\\n\\f\\U0001f60a".escapeGlang(false));
        /*
            Block comment
         */
            a == b;
        a >>>= b;
        println(`%*\\\\\\``)
        """;

    public static void main(String[] args) {
        final List<Token> tokens;
        try {
            tokens = GlangTokenizer.tokenize(SOURCE);
        } catch (TokenizeFailure e) {
            System.err.println(e.getMessage());
            return;
        }
        System.out.println(tokens);
        System.out.println();
        System.out.println(TokenSourcePrinter.print(tokens));
    }
}
