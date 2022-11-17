package org.synyx.urlaubsverwaltung;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

public abstract class TestContainersBase {

    static final TestMariaDBContainer mariaDB = new TestMariaDBContainer();

    @DynamicPropertySource
    static void mariaDBProperties(DynamicPropertyRegistry registry) {
        mariaDB.start();
        mariaDB.configureSpringDataSource(registry);
    }
}
