package org.synyx.urlaubsverwaltung;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

public abstract class TestContainersBase {

    static final TestPostgreSQLContainer postgre = new TestPostgreSQLContainer();

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        postgre.start();
        postgre.configureSpringDataSource(registry);
    }
}
