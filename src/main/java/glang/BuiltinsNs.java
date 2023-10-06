package glang;

import glang.runtime.OptionalParameter;

import java.util.Arrays;

public class BuiltinsNs {
    public static void println() {
        System.out.println();
    }

    public static void println(Object value) {
        System.out.println(value);
    }

    public static void println(Object... values) {
        if (values.length == 0) {
            System.out.println();
            return;
        }
        if (values.length == 1) {
            System.out.println(values[0]);
            return;
        }
        final StringBuilder toPrint = new StringBuilder().append(values[0]);
        for (int i = 1; i < values.length; i++) {
            toPrint.append(' ').append(values[i]);
        }
        System.out.println(toPrint);
    }

    public static String optionalArgTest(String a, OptionalParameter b, OptionalParameter c, int... extras) {
        return a + " " + b + " " + c + " " + Arrays.toString(extras);
    }
}
