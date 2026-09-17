package org.synyx.urlaubsverwaltung.settings;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.tenancy.tenant.TenantContextHolder;
import org.synyx.urlaubsverwaltung.tenancy.tenant.TenantId;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SettingsCacheTest {

    private final AdjustableClock clock = new AdjustableClock(Instant.parse("2026-09-17T10:00:00Z"));

    @Test
    void ensureSettingsAreLoadedOnlyOnceForRepeatedAccess() {

        final SettingsCache sut = new SettingsCache(new TestTenantContextHolder("default"), clock);
        final CountingLoader loader = new CountingLoader();

        final Settings firstSettings = sut.get(loader);
        final Settings secondSettings = sut.get(loader);

        assertThat(firstSettings).isSameAs(secondSettings);
        assertThat(loader.count()).isOne();
    }

    @Test
    void ensureSettingsAreLoadedAgainAfterInvalidate() {

        final SettingsCache sut = new SettingsCache(new TestTenantContextHolder("default"), clock);
        final CountingLoader loader = new CountingLoader();

        final Settings firstSettings = sut.get(loader);
        sut.invalidate();
        final Settings secondSettings = sut.get(loader);

        assertThat(firstSettings).isNotSameAs(secondSettings);
        assertThat(loader.count()).isEqualTo(2);
    }

    @Test
    void ensureSettingsAreLoadedAgainWhenTheCachedEntryIsTooOld() {

        final SettingsCache sut = new SettingsCache(new TestTenantContextHolder("default"), clock);
        final CountingLoader loader = new CountingLoader();

        sut.get(loader);
        // MAX_AGE is one minute
        clock.advance(Duration.ofSeconds(61));
        sut.get(loader);

        assertThat(loader.count()).isEqualTo(2);
    }

    @Test
    void ensureSettingsAreServedFromTheCacheWhileTheEntryIsYoungEnough() {

        final SettingsCache sut = new SettingsCache(new TestTenantContextHolder("default"), clock);
        final CountingLoader loader = new CountingLoader();

        sut.get(loader);
        clock.advance(Duration.ofSeconds(59));
        sut.get(loader);

        assertThat(loader.count()).isOne();
    }

    @Test
    void ensureSettingsAreCachedPerTenant() {

        final TestTenantContextHolder tenantContextHolder = new TestTenantContextHolder("first");
        final SettingsCache sut = new SettingsCache(tenantContextHolder, clock);
        final CountingLoader loader = new CountingLoader();

        final Settings settingsOfFirstTenant = sut.get(loader);

        tenantContextHolder.setTenantId(new TenantId("second"));
        final Settings settingsOfSecondTenant = sut.get(loader);

        tenantContextHolder.setTenantId(new TenantId("first"));
        final Settings settingsOfFirstTenantAgain = sut.get(loader);

        assertThat(settingsOfSecondTenant).isNotSameAs(settingsOfFirstTenant);
        assertThat(settingsOfFirstTenantAgain).isSameAs(settingsOfFirstTenant);
        assertThat(loader.count()).isEqualTo(2);
    }

    @Test
    void ensureInvalidateAffectsOnlyTheCurrentTenant() {

        final TestTenantContextHolder tenantContextHolder = new TestTenantContextHolder("first");
        final SettingsCache sut = new SettingsCache(tenantContextHolder, clock);
        final CountingLoader loader = new CountingLoader();

        final Settings settingsOfFirstTenant = sut.get(loader);

        tenantContextHolder.setTenantId(new TenantId("second"));
        sut.get(loader);
        sut.invalidate();

        tenantContextHolder.setTenantId(new TenantId("first"));
        assertThat(sut.get(loader)).isSameAs(settingsOfFirstTenant);
    }

    @Test
    void ensureSettingsAreNotCachedWithoutATenant() {

        final SettingsCache sut = new SettingsCache(new TestTenantContextHolder(null), clock);
        final CountingLoader loader = new CountingLoader();

        final Settings firstSettings = sut.get(loader);
        final Settings secondSettings = sut.get(loader);

        assertThat(firstSettings).isNotSameAs(secondSettings);
        assertThat(loader.count()).isEqualTo(2);
    }

    @Test
    void ensureInvalidateWithoutATenantDoesNotFail() {

        final SettingsCache sut = new SettingsCache(new TestTenantContextHolder(null), clock);

        sut.invalidate();

        final CountingLoader loader = new CountingLoader();
        assertThat(sut.get(loader)).isNotNull();
    }

    @Test
    void ensureNothingIsCachedWhenLoadingFails() {

        final SettingsCache sut = new SettingsCache(new TestTenantContextHolder("default"), clock);

        assertThatThrownBy(() -> sut.get(() -> {
            throw new IllegalStateException("No settings found in database!");
        })).isInstanceOf(IllegalStateException.class);

        final CountingLoader loader = new CountingLoader();
        assertThat(sut.get(loader)).isNotNull();
        assertThat(loader.count()).isOne();
    }

    private static final class CountingLoader implements Supplier<Settings> {

        private final AtomicInteger count = new AtomicInteger();

        @Override
        public Settings get() {
            count.incrementAndGet();
            return new Settings();
        }

        int count() {
            return count.get();
        }
    }

    private static final class TestTenantContextHolder implements TenantContextHolder {

        private TenantId tenantId;

        TestTenantContextHolder(String tenantId) {
            this.tenantId = tenantId == null ? null : new TenantId(tenantId);
        }

        @Override
        public Optional<TenantId> getCurrentTenantId() {
            return Optional.ofNullable(tenantId);
        }

        @Override
        public void setTenantId(TenantId tenantId) {
            this.tenantId = tenantId;
        }

        @Override
        public void clear() {
            this.tenantId = null;
        }
    }

    private static final class AdjustableClock extends Clock {

        private Instant instant;

        AdjustableClock(Instant instant) {
            this.instant = instant;
        }

        void advance(Duration duration) {
            this.instant = this.instant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
