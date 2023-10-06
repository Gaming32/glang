package glang;

import glang.util.GlangStringUtils;

public class StringNs {
//    @ExtensionMethod
    public static String escapeGlang(String thiz, String escapeQuotes) {
        return GlangStringUtils.escape(thiz, escapeQuotes);
    }

//    @ExtensionMethod
    public static String escapeGlang(String thiz) {
        return escapeGlang(thiz, "\"'`");
    }

//    @ExtensionMethod
    public static String getLine(String thiz, int index) {
        return GlangStringUtils.getLine(thiz, index);
    }
}
