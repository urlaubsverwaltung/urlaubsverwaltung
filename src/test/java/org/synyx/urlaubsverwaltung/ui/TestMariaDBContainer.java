package org.synyx.urlaubsverwaltung.ui;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.MariaDBContainer;

class TestMariaDBContainer extends MariaDBContainer<TestMariaDBContainer> {

    private static final String VERSION = "10.6.11";

    TestMariaDBContainer() {
        super(MariaDBContainer.NAME + ":" + VERSION);
        withCommand("--character-set-server=utf8mb4", "--collation-server=utf8mb4_unicode_ci");
    }

    /**
     * Sets the spring datasource configuration properties.
     *
     * <p>Usage:</p>
     * <pre><code>
     * static final TestMariaDBContainer mariaDB = new TestMariaDBContainer();
     * &#64;DynamicPropertySource
     * static void setupDataSource(DynamicPropertySource registry) {
     *     mariaDB.start();
     *     mariaDB.configureSpringDataSource(registry);
     * }
     * </code></pre>
     *
     * @param registry
     */
    void configureSpringDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", this::getJdbcUrl);
        registry.add("spring.datasource.username", this::getUsername);
        registry.add("spring.datasource.password", this::getPassword);
    }
}
