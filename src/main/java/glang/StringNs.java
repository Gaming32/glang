package glang;

import glang.runtime.ExtensionMethod;
import glang.util.GlangStringUtils;

public class StringNs {
    @ExtensionMethod
    public static String escapeGlang(String thiz, boolean escapeSingleQuotes) {
        return GlangStringUtils.escape(thiz, escapeSingleQuotes);
    }

    @ExtensionMethod
    public static String getLine(String thiz, int index) {
        return GlangStringUtils.getLine(thiz, index);
    }
}
