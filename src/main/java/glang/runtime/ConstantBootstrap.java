package glang.runtime;

import java.lang.invoke.MethodHandles;
import java.math.BigInteger;

public final class ConstantBootstrap {
    private ConstantBootstrap() {
    }

    public static Integer intWrapper(MethodHandles.Lookup lookup, String name, Class<Integer> type, int value) {
        return value;
    }

    public static Double doubleWrapper(MethodHandles.Lookup lookup, String name, Class<Double> type, double value) {
        return value;
    }

    public static Long longWrapper(MethodHandles.Lookup lookup, String name, Class<Long> type, long value) {
        return value;
    }

    public static BigInteger bigInteger(MethodHandles.Lookup lookup, String name, Class<BigInteger> type, String value) {
        return new BigInteger(value);
    }
}
