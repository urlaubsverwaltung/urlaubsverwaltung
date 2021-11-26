package org.synyx.urlaubsverwaltung;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.testcontainers.containers.PostgreSQLContainer.IMAGE;

public abstract class TestContainersBase {

    static final PostgreSQLContainer<?> postgre = new PostgreSQLContainer<>(IMAGE + ":14.1");

    @DynamicPropertySource
    static void postgreDBProperties(DynamicPropertyRegistry registry) {
        postgre.start();
        registry.add("spring.datasource.url", postgre::getJdbcUrl);
        registry.add("spring.datasource.username", postgre::getUsername);
        registry.add("spring.datasource.password", postgre::getPassword);
    }
}
