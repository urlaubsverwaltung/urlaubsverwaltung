package org.synyx.urlaubsverwaltung.config;

import net.javacrumbs.shedlock.core.DefaultLockingTaskExecutor;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.sql.DataSource;

@EnableScheduling
@Configuration
class SchedulerConfiguration {

    @Bean
    LockProvider lockProvider(final DataSource dataSource) {
        return new JdbcTemplateLockProvider(dataSource);
    }

    @Bean
    ScheduleLocking scheduleLocking(final LockProvider lockProvider) {
        return new ScheduleLocking(new DefaultLockingTaskExecutor(lockProvider));
    }
}
