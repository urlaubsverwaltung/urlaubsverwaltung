package org.synyx.urlaubsverwaltung;

import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.testcontainers.containers.MariaDBContainer.IMAGE;

@Testcontainers
@DirtiesContext
public abstract class TestContainersBase {

    static MariaDBContainer<?> mariaDB = new MariaDBContainer<>(IMAGE + ":10.4");

    @DynamicPropertySource
    static void mariaDBProperties(DynamicPropertyRegistry registry) {
        mariaDB.start();
        registry.add("spring.datasource.url", mariaDB::getJdbcUrl);
        registry.add("spring.datasource.username", mariaDB::getUsername);
        registry.add("spring.datasource.password", mariaDB::getPassword);
    }
}
