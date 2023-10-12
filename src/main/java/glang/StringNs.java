package glang;

import glang.runtime.extension.ExtensionMethod;
import glang.util.GlangStringUtils;

public class StringNs {
    @ExtensionMethod
    public static String escapeGlang(String thiz, String escapeQuotes) {
        return GlangStringUtils.escape(thiz, escapeQuotes);
    }

    @ExtensionMethod
    public static String escapeGlang(String thiz) {
        return escapeGlang(thiz, "\"'`");
    }

    @ExtensionMethod
    public static String getLine(String thiz, int index) {
        return GlangStringUtils.getLine(thiz, index);
    }

    @ExtensionMethod
    public static String add(String thiz, Object other) {
        return thiz + other;
    }

    @ExtensionMethod
    public static String multiply(String thiz, int count) {
        return thiz.repeat(count);
    }
}
