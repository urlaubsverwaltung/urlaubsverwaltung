package org.synyx.urlaubsverwaltung.settings;

import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.tenancy.tenant.TenantContextHolder;
import org.synyx.urlaubsverwaltung.tenancy.tenant.TenantId;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * Caches the {@link Settings} of a tenant in memory. The settings are a single row that changes
 * only when someone edits them in the settings pages, but they are read in every layer of every
 * request. An entry is dropped when the settings are written and additionally when it is older
 * than {@link #MAX_AGE}, which bounds how long another instance of the application can serve
 * settings that it did not write itself.
 */
@Component
class SettingsCache {

    private static final Duration MAX_AGE = Duration.ofMinutes(1);

    private final TenantContextHolder tenantContextHolder;
    private final Clock clock;
    private final Map<TenantId, CachedSettings> cache = new ConcurrentHashMap<>();

    // intentionally global rather than per tenant: a write for one tenant may then suppress
    // another tenant's concurrent cache fill, which only costs one extra load
    private final AtomicLong invalidations = new AtomicLong();

    SettingsCache(TenantContextHolder tenantContextHolder, Clock clock) {
        this.tenantContextHolder = tenantContextHolder;
        this.clock = clock;
    }

    /**
     * @param loader loads the settings if they are not cached
     * @return the cached settings, or freshly loaded settings. The returned instance is shared,
     * it may only be modified to be saved right away.
     */
    Settings get(Supplier<Settings> loader) {

        final Optional<TenantId> maybeTenantId = tenantContextHolder.getCurrentTenantId();
        if (maybeTenantId.isEmpty()) {
            return loader.get();
        }

        final TenantId tenantId = maybeTenantId.get();
        final Instant now = clock.instant();

        final CachedSettings cachedSettings = cache.get(tenantId);
        if (cachedSettings != null && !isTooOld(cachedSettings, now)) {
            return cachedSettings.settings();
        }

        final long invalidationsBeforeLoad = invalidations.get();
        final Settings settings = loader.get();

        // cached first and dropped again afterwards, never the other way around: an invalidation either bumps the
        // counter before the check below, or it removes the entry that was put here - checking before the put would
        // leave a window in which neither happens and these already stale settings stay cached
        final CachedSettings loadedSettings = new CachedSettings(settings, now);
        cache.put(tenantId, loadedSettings);

        if (invalidations.get() != invalidationsBeforeLoad) {
            // compared by identity, so that settings another thread cached meanwhile survive
            cache.computeIfPresent(tenantId, (tenant, cached) -> cached == loadedSettings ? null : cached);
        }

        return settings;
    }

    /**
     * Drops the cached settings of the current tenant.
     */
    void invalidate() {
        invalidations.incrementAndGet();
        tenantContextHolder.getCurrentTenantId().ifPresent(cache::remove);
    }

    private boolean isTooOld(CachedSettings cachedSettings, Instant now) {
        return cachedSettings.loadedAt().plus(MAX_AGE).isBefore(now);
    }

    private record CachedSettings(Settings settings, Instant loadedAt) {
    }
}
