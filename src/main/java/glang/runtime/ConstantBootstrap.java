package glang.runtime;

import java.lang.invoke.MethodHandles;
import java.math.BigInteger;

public final class ConstantBootstrap {
    private ConstantBootstrap() {
    }

    public static BigInteger bigInteger(MethodHandles.Lookup lookup, String name, Class<BigInteger> type, String value) {
        return new BigInteger(value);
    }
}
