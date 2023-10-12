package glang;

import glang.runtime.extension.ExtensionMethod;

public class NumberNs {
    //region subtract
    @ExtensionMethod
    public static int subtract(int a, int b) {
        return a - b;
    }

    @ExtensionMethod
    public static long subtract(long a, long b) {
        return a - b;
    }

    @ExtensionMethod
    public static float subtract(float a, float b) {
        return a - b;
    }

    @ExtensionMethod
    public static double subtract(double a, double b) {
        return a - b;
    }
    //endregion

    //region multiply
    @ExtensionMethod
    public static int multiply(int a, int b) {
        return a * b;
    }

    @ExtensionMethod
    public static long multiply(long a, long b) {
        return a * b;
    }

    @ExtensionMethod
    public static float multiply(float a, float b) {
        return a * b;
    }

    @ExtensionMethod
    public static double multiply(double a, double b) {
        return a * b;
    }
    //endregion

    //region divide
    @ExtensionMethod
    public static int divide(int a, int b) {
        return a / b;
    }

    @ExtensionMethod
    public static long divide(long a, long b) {
        return a / b;
    }

    @ExtensionMethod
    public static float divide(float a, float b) {
        return a / b;
    }

    @ExtensionMethod
    public static double divide(double a, double b) {
        return a / b;
    }
    //endregion

    //region modulo
    @ExtensionMethod
    public static int modulo(int a, int b) {
        return Math.floorMod(a, b);
    }

    @ExtensionMethod
    public static long modulo(long a, long b) {
        return Math.floorMod(a, b);
    }

    // @ExtensionMethod
    // public static float modulo(float a, float b) {
    //     return Math.floorMod(a, b);
    // }

    // @ExtensionMethod
    // public static double modulo(double a, double b) {
    //     return Math.floorMod(a, b);
    // }
    //endregion

    //region remainder
    @ExtensionMethod
    public static int remainder(int a, int b) {
        return a % b;
    }

    @ExtensionMethod
    public static long remainder(long a, long b) {
        return a % b;
    }

    @ExtensionMethod
    public static float remainder(float a, float b) {
        return a % b;
    }

    @ExtensionMethod
    public static double remainder(double a, double b) {
        return a % b;
    }
    //endregion
}
