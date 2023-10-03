import glang.compiler.token.GlangTokenizer;
import glang.compiler.token.TokenizeFailure;

public class TestMain {

    private static final String SOURCE = """
        println("Hello, world!"); // Comment!
        println("a, b\\n\\f\\U0001f60a".escapeGlang(false));
        /*
            Block comment
         */
        a == b;
        a >>>= b;
        """;

    public static void main(String[] args) {
        try {
            System.out.println(GlangTokenizer.tokenize(SOURCE));
        } catch (TokenizeFailure e) {
            System.err.println(e.getMessage());
        }
    }
}
