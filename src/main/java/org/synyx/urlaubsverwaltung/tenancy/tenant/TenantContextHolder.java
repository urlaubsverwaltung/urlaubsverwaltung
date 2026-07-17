package org.synyx.urlaubsverwaltung.tenancy.tenant;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface TenantContextHolder {

    default Optional<TenantId> getCurrentTenantId() {
        return Optional.empty();
    }

    default void setTenantId(TenantId tenantId) {

    }

    default void clear() {

    }

    default void runInTenantIdContext(String tenantId, Consumer<String> function) {
        runInTenantIdContext(new TenantId(tenantId), function);
    }

    default void runInTenantIdContext(TenantId tenantId, Consumer<String> function) {
        try {
            setTenantId(tenantId);
            function.accept(tenantId.tenantId());
        } finally {
            clear();
        }
    }

    default void runInTenantIdContext(TenantId tenantId, Runnable function) {
        try {
            setTenantId(tenantId);
            function.run();
        } finally {
            clear();
        }
    }

    default <T> T runInTenantIdContext(TenantId tenantId, Supplier<T> function) {
        try {
            setTenantId(tenantId);
            return function.get();
        } finally {
            clear();
        }
    }
}
