package glang.compiler.tree.expression;

import glang.compiler.SourceLocation;

import java.math.BigInteger;

public class NumberExpression extends LiteralExpression<Number> {
    private static final BigInteger LONG_MIN = BigInteger.valueOf(Long.MIN_VALUE);
    private static final BigInteger LONG_MAX = BigInteger.valueOf(Long.MAX_VALUE);

    public NumberExpression(Number value, SourceLocation location) {
        super(value, location);
    }

    @Override
    public String toString() {
        final Number num = getValue();
        if (num instanceof Long l && l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE) {
            return l + "L";
        }
        if (num instanceof BigInteger b && b.compareTo(LONG_MIN) >= 0 && b.compareTo(LONG_MAX) <= 0) {
            return b + "B";
        }
        return num.toString();
    }
}
