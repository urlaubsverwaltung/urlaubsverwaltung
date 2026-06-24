package org.synyx.urlaubsverwaltung;

import java.util.function.Supplier;

/**
 * A simple caching decorator for {@link Supplier} that stores the result of the first
 * invocation and returns the cached value on subsequent calls.
 *
 * <p>This is useful when a costly operation (e.g., database access, settings lookup) needs
 * to be performed only once but the API requires a {@code Supplier}. By wrapping the actual
 * provider in a {@code CachedSupplier}, the expensive computation is deferred until first use
 * and avoided on all following calls.</p>
 *
 * <p><b>Note:</b> This class is not thread-safe. Concurrent access may result in multiple
 * evaluations of the underlying supplier.</p>
 *
 * @param <T> the type of the value supplied
 */
public class CachedSupplier<T> implements Supplier<T> {

    private T cachedValue;
    private final Supplier<T> supplier;

    /**
     * Creates a cached supplier wrapping the given delegate.
     *
     * @param supplier the underlying supplier whose result will be cached after first invocation
     */
    public CachedSupplier(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    /**
     * Returns the cached value, computing it from the underlying supplier on the first call.
     *
     * <p>If the underlying supplier returns {@code null}, that {@code null} will be cached and
     * returned on subsequent calls.</p>
     *
     * @return the value supplied by the delegate, cached after the first invocation
     */
    @Override
    public T get() {
        if (cachedValue == null) {
            cachedValue = supplier.get();
        }
        return cachedValue;
    }
}
