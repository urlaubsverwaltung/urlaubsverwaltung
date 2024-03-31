package org.synyx.urlaubsverwaltung;

import java.util.function.Supplier;

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
