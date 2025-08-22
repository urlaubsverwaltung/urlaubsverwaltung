package org.synyx.urlaubsverwaltung;

import java.util.function.Supplier;

/**
 * A cached supplier that caches the value returned by the underlying supplier.
 * The value is computed only once and subsequent calls return the cached value.
 *
 * @param <T> the type of the value supplied
 */
public class CachedSupplier<T> implements Supplier<T> {

    private T cachedValue;
    private final Supplier<T> supplier;

    public CachedSupplier(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public T get() {
        if (cachedValue == null) {
            cachedValue = supplier.get();
        }
        return cachedValue;
    }
}
