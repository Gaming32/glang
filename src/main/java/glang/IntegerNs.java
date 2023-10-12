package glang;

import glang.runtime.extension.ExtensionMethod;

public class IntegerNs {
    @ExtensionMethod
    public static int subtract(int a, int b) {
        return a - b;
    }

    @ExtensionMethod
    public static int multiply(int a, int b) {
        return a * b;
    }

    @ExtensionMethod
    public static int divide(int a, int b) {
        return a / b;
    }

    @ExtensionMethod
    public static int modulo(int a, int b) {
        return Math.floorMod(a, b);
    }

    @ExtensionMethod
    public static int remainder(int a, int b) {
        return a % b;
    }
}
