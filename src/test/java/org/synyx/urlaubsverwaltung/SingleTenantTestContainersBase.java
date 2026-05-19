package org.synyx.urlaubsverwaltung;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;

public abstract class SingleTenantTestContainersBase {

    @Container
    @ServiceConnection
    static final SingleTenantTestPostgreSQLContainer postgre = new SingleTenantTestPostgreSQLContainer();
}
