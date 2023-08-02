package org.synyx.urlaubsverwaltung.config;

import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockingTaskExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ScheduleLockingTest {

    private ScheduleLocking sut;

    @Mock
    private LockingTaskExecutor lockingTaskExecutor;

    @Captor
    private ArgumentCaptor<LockConfiguration> argumentCaptor;

    @BeforeEach
    void setUp() {
        sut = new ScheduleLocking(lockingTaskExecutor);
    }

    @Test
    void ensureWithLockCallsLockingTaskExecutor() {
        final Runnable runnable = mock(Runnable.class);
        final Runnable preparedRunnable = sut.withLock("someName", runnable);
        preparedRunnable.run();

        verify(lockingTaskExecutor).executeWithLock(eq(runnable), argumentCaptor.capture());
        final LockConfiguration lockConfiguration = argumentCaptor.getValue();
        assertThat(lockConfiguration.getLockAtLeastFor()).isEqualTo(Duration.ofMinutes(2));
        assertThat(lockConfiguration.getLockAtMostFor()).isEqualTo(Duration.ofMinutes(15));
        assertThat(lockConfiguration.getName()).isEqualTo("someName");
        assertThat(lockConfiguration.getUnlockTime())
            .isAfter(Instant.now())
            .isBefore(Instant.now().plusSeconds(120));
    }

    @Test
    void ensureWithLockCallsLockingTaskExecutorWithGivenParameter() {
        final Duration lockAtMostFor = Duration.ofMinutes(20);
        final Duration lockAtLeastFor = Duration.ofMinutes(12);

        final Runnable runnable = mock(Runnable.class);
        final Runnable preparedRunnable = sut.withLock("someName", runnable, lockAtMostFor, lockAtLeastFor);
        preparedRunnable.run();

        verify(lockingTaskExecutor).executeWithLock(eq(runnable), argumentCaptor.capture());
        final LockConfiguration lockConfiguration = argumentCaptor.getValue();
        assertThat(lockConfiguration.getLockAtLeastFor()).isEqualTo(lockAtLeastFor);
        assertThat(lockConfiguration.getLockAtMostFor()).isEqualTo(lockAtMostFor);
        assertThat(lockConfiguration.getName()).isEqualTo("someName");
        assertThat(lockConfiguration.getUnlockTime())
            .isAfter(Instant.now())
            .isBefore(Instant.now().plusSeconds(12 * 60));
    }
}
