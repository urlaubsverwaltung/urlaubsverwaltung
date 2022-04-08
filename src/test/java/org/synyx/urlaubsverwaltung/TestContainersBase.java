package org.synyx.urlaubsverwaltung;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;

import static org.testcontainers.containers.MariaDBContainer.NAME;

public abstract class TestContainersBase {

    static final MariaDBContainer<?> mariaDB = new MariaDBContainer<>(NAME + ":10.5")
        .withCommand("--character-set-server=utf8mb4", "--collation-server=utf8mb4_unicode_ci", "--max-connections=300");

    @DynamicPropertySource
    static void mariaDBProperties(DynamicPropertyRegistry registry) {
        mariaDB.start();
        registry.add("spring.datasource.url", mariaDB::getJdbcUrl);
        registry.add("spring.datasource.username", mariaDB::getUsername);
        registry.add("spring.datasource.password", mariaDB::getPassword);
    }
}
