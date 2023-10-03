package glang.util;

public class GlangStringUtils {
    public static String escape(String string, boolean escapeSingleQuotes) {
        final StringBuilder result = new StringBuilder(string.length() + 4);
        for (int i = 0; i < string.length(); i++) {
            final char c = string.charAt(i);
            switch (c) {
                case '\0' -> result.append("\\0");
                case '\t' -> result.append("\\t");
                case '\b' -> result.append("\\b");
                case '\n' -> result.append("\\n");
                case '\r' -> result.append("\\r");
                case '\f' -> result.append("\\f");
                case '\'' -> result.append(escapeSingleQuotes ? "\\'" : "'");
                case '"' -> result.append(escapeSingleQuotes ? "\"" : "\\\"");
                case '\\' -> result.append("\\\\");
                default -> {
                    final int type = Character.getType(c);
                    if (type != Character.UNASSIGNED && type != Character.CONTROL && type != Character.SURROGATE) {
                        result.append(c);
                    } else if (c < 0x10) {
                        result.append("\\x0").append(Character.forDigit(c, 16));
                    } else {
                        final String hex = Integer.toHexString(c);
                        if (c < 0x100) {
                            result.append("\\x").append(hex);
                        } else if (c < 0x1000) {
                            result.append("\\u0").append(hex);
                        } else {
                            result.append("\\u").append(hex);
                        }
                    }
                }
            }
        }
        return result.toString();
    }

    public static String getLine(String text, int index) {
        return text.lines().skip(index).findFirst().orElse("");
    }
}
