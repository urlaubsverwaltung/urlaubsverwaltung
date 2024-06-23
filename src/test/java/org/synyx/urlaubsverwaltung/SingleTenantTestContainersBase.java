package org.synyx.urlaubsverwaltung;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

public abstract class SingleTenantTestContainersBase {

    static final SingleTenantTestPostgreSQLContainer postgre = new SingleTenantTestPostgreSQLContainer();

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        postgre.start();
        postgre.configureSpringDataSource(registry);
    }
}
