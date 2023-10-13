package glang.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.Cleaner;
import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * @author WagYourTail
 */
public class ConcurrentCache<K, V> {
    public static final Cleaner CLEANER = Cleaner.create();

    private final Function<K, V> defaultComputeIfAbsent;
    private final ConcurrentHashMap<K, SoftReference<V>> map = new ConcurrentHashMap<>();

    public ConcurrentCache(Function<K, V> defaultComputeIfAbsent) {
        this.defaultComputeIfAbsent = defaultComputeIfAbsent;
    }

    public ConcurrentCache() {
        this(null);
    }

    /**
     *
     * @return null if key does not exist
     */
    @Nullable
    public V get(@NotNull K key) {
        SoftReference<V> ref = map.get(key);
        if (ref != null) {
            return ref.get();

        }
        if (defaultComputeIfAbsent != null) {
            V v = defaultComputeIfAbsent.apply(key);
            put(key, v);
            return v;
        }
        return null;
    }

    public V get(@NotNull K key, Function<K, V> cache) {
        V v = get(key);
        if (v == null) {
            v = cache.apply(key);
            put(key, v);
        }
        return v;
    }

    /**
     *
     * @return previous value or null if key did not exist
     */
    public V put(@NotNull K k, @NotNull V v) {
        SoftReference<V> prev = map.remove(k);
        SoftReference<V> current = new SoftReference<>(v);
        map.put(k, current);
        CLEANER.register(v, () -> map.remove(k, current));
        if (prev != null) {
            return prev.get();
        }
        return null;
    }

    /**
     * @return previous value or null if key did not exist
     */
    public V remove(@NotNull K k) {
        SoftReference<V> prev = map.remove(k);
        if (prev != null) {
            return prev.get();
        }
        return null;
    }

    /**
     * size could change due to weak/soft before it's used...
     * @return size of map.
     */
    public int size() {
        return map.size();
    }
}
