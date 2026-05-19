package org.synyx.urlaubsverwaltung;

import org.testcontainers.postgresql.PostgreSQLContainer;

public class SingleTenantTestPostgreSQLContainer extends PostgreSQLContainer {

    private static final String VERSION = "18.3";

    public SingleTenantTestPostgreSQLContainer() {
        super(IMAGE + ":" + VERSION);
        this.withCommand("--max_connections=1000", "--shared_buffers=240MB");
    }
}
