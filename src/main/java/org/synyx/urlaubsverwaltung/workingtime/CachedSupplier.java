package org.synyx.urlaubsverwaltung.workingtime;

import java.util.function.Supplier;

class CachedSupplier<T> implements Supplier<T> {

    private T cachedValue;
    private final Supplier<T> supplier;

    CachedSupplier(Supplier<T> supplier) {
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
