package glang;

import glang.runtime.extension.ExtensionMethod;

@SuppressWarnings({"rawtypes", "unchecked"}) // glang has no concept of generics
public class ComparableNs {
    @ExtensionMethod
    public static boolean lessThan(Comparable a, Object b) {
        return a.compareTo(b) < 0;
    }

    @ExtensionMethod
    public static boolean greaterThan(Comparable a, Object b) {
        return a.compareTo(b) > 0;
    }

    @ExtensionMethod
    public static boolean lessThanEqual(Comparable a, Object b) {
        return a.compareTo(b) <= 0;
    }

    @ExtensionMethod
    public static boolean greaterThanEqual(Comparable a, Object b) {
        return a.compareTo(b) >= 0;
    }
}
