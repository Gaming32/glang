package glang.compiler.token;

import glang.compiler.SourceLocation;
import glang.util.GlangStringUtils;

public abstract sealed class Token {
    protected final TokenType type;
    protected final SourceLocation location;

    protected Token(TokenType type, SourceLocation location) {
        if (!getClass().isAssignableFrom(type.getTokenClass())) {
            throw new IllegalArgumentException(
                "TokenType " + type + " requires " + type.getTokenClass().getCanonicalName() +
                    ", not " + getClass().getCanonicalName()
            );
        }
        this.type = type;
        this.location = location;
    }

    public TokenType getType() {
        return type;
    }

    public SourceLocation getLocation() {
        return location;
    }

    @Override
    public final String toString() {
        return "Token." + getClass().getSimpleName() + "[" + type + ": " + prettyPrint() + "]";
    }

    public abstract String prettyPrint();

    public static final class Basic extends Token {
        public Basic(TokenType type, SourceLocation location) {
            super(type, location);
        }

        @Override
        public String prettyPrint() {
            return type.getBasicText();
        }
    }

    public static final class Identifier extends Token {
        private final String identifier;

        public Identifier(String identifier, SourceLocation location) {
            super(TokenType.IDENTIFIER, location);
            this.identifier = identifier;
        }

        public String getIdentifier() {
            return identifier;
        }

        @Override
        public String prettyPrint() {
            return prettyPrint(identifier);
        }

        public static String prettyPrint(String identifier) {
            if (identifier.isEmpty()) {
                return "``";
            }
            if (!Character.isJavaIdentifierStart(identifier.charAt(0))) {
                return '`' + GlangStringUtils.escape(identifier, "`") + '`';
            }
            for (int i = 1; i < identifier.length(); i++) {
                if (!Character.isJavaIdentifierPart(identifier.charAt(i))) {
                    return '`' + GlangStringUtils.escape(identifier, "`") + '`';
                }
            }
            return identifier;
        }
    }

    public static final class Str extends Token {
        private final String value;

        public Str(String value, SourceLocation location) {
            super(TokenType.STRING, location);
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String prettyPrint() {
            return prettyPrint(value);
        }

        public static String prettyPrint(String value) {
            if (value.indexOf('"') >= 0) {
                return '\'' + GlangStringUtils.escape(value, "'") + '\'';
            }
            return '"' + GlangStringUtils.escape(value, "\"") + '"';
        }
    }

    public static final class Num extends Token {
        private final Number value;

        public Num(Number value, SourceLocation location) {
            super(TokenType.NUMBER, location);
            this.value = value;
        }

        public Number getValue() {
            return value;
        }

        @Override
        public String prettyPrint() {
            return value.toString();
        }
    }
}
