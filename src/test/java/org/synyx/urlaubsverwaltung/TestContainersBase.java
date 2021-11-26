package org.synyx.urlaubsverwaltung;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

public abstract class TestContainersBase {

    static final TestPostgreContainer postgre = new TestPostgreContainer();

    @DynamicPropertySource
    static void postgreDBProperties(DynamicPropertyRegistry registry) {
        postgre.start();
        postgre.configureSpringDataSource(registry);
    }
}
