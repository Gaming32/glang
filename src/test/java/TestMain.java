import glang.compiler.error.CompileFailedException;
import glang.compiler.tree.GlangTreeifier;

public class TestMain {

    private static final String SOURCE = """
        println(5);
        println(5.5);
        println(.5);
        println(5.5e3);
        println(5.5e-11);
        println(5.5e+11);
        println(1234567890);
        println(123456789012345678901234);
        println(0xdeadbeef);
        println(0x123h);
        println(0b101);
        println(010);
        
        // println("Hello, world!"); // Comment!
        // println("a, b\\n\\f\\U0001f60a".escapeGlang(false));
        // /*
        //     Block comment
        //  */
        //     a == b;
        // a >>>= b;
        // println(`%*\\\\\\``)
        """;

    public static void main(String[] args) {
//        final List<Token> tokens;
//        try {
//            tokens = GlangTokenizer.tokenize(SOURCE);
//        } catch (TokenizeFailure e) {
//            System.err.println(e.getMessage());
//            return;
//        }
//        tokens.forEach(System.out::println);
//        System.out.println();
//        System.out.println();
//        System.out.println(TokenSourcePrinter.print(tokens));

        try {
            System.out.println(GlangTreeifier.treeify(SOURCE));
        } catch (CompileFailedException e) {
            System.err.println(e.getMessage());
        }
    }
}
