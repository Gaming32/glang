package glang.compiler.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SymbolMap<V> {
    private final Map<Character, SymbolMap<V>> subMaps = new HashMap<>();
    private int size;
    private V value;

    public V get(String key) {
        return get(key, 0, key.length());
    }

    private V get(String key, int start, int length) {
        if (length == 0) {
            return value;
        }
        final SymbolMap<V> next = subMaps.get(key.charAt(start));
        if (next == null) {
            return null;
        }
        return next.get(key, start + 1, length - 1);
    }

    public V put(String key, V value) {
        return put(key, 0, key.length(), Objects.requireNonNull(value, "value"));
    }

    private V put(String key, int start, int length, V newValue) {
        if (length == 0) {
            final V oldValue = value;
            value = newValue;
            if (oldValue == null) {
                size++;
            }
            return oldValue;
        }
        final V oldValue = subMaps.computeIfAbsent(key.charAt(start), c -> new SymbolMap<>())
            .put(key, start + 1, length - 1, newValue);
        if (oldValue == null) {
            size++;
        }
        return oldValue;
    }

    public SymbolMap<V> getNext(char c) {
        return subMaps.get(c);
    }

    public V getValue() {
        return value;
    }

    public int size() {
        return size;
    }
}
