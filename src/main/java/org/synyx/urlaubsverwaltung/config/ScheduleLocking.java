package org.synyx.urlaubsverwaltung.config;

import net.javacrumbs.shedlock.core.ClockProvider;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockingTaskExecutor;
import net.javacrumbs.shedlock.support.annotation.NonNull;

import java.time.Duration;

public class ScheduleLocking {

    private final LockingTaskExecutor lockingTaskExecutor;

    public ScheduleLocking(LockingTaskExecutor lockingTaskExecutor) {
        this.lockingTaskExecutor = lockingTaskExecutor;
    }

    public Runnable withLock(final String name, final Runnable runnable) {
        return withLock(name, runnable, Duration.ofMinutes(15), Duration.ofMinutes(2));
    }

    public Runnable withLock(@NonNull final String name, @NonNull final Runnable runnable,
                             @NonNull final Duration lockAtMostFor, @NonNull final Duration lockAtLeastFor) {
        return () -> lockingTaskExecutor.executeWithLock(
            runnable,
            new LockConfiguration(ClockProvider.now(), name, lockAtMostFor, lockAtLeastFor)
        );
    }
}
