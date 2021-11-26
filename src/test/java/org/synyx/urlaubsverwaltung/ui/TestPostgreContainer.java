package org.synyx.urlaubsverwaltung.ui;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.PostgreSQLContainer;

class TestPostgreContainer extends PostgreSQLContainer<TestPostgreContainer> {

    private static final String VERSION = "14.1";

    TestPostgreContainer() {
        super(IMAGE + ":" + VERSION);
    }

    /**
     * Sets the spring datasource configuration properties.
     *
     * <p>Usage:</p>
     * <pre><code>
     * static final TestPostgreContainer postgre = new TestPostgreContainer();
     * &#64;DynamicPropertySource
     * static void setupDataSource(DynamicPropertySource registry) {
     *     postgre.start();
     *     postgre.configureSpringDataSource(registry);
     * }
     * </code>
     * </pre>
     *
     * @param registry
     */
    void configureSpringDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", this::getJdbcUrl);
        registry.add("spring.datasource.username", this::getUsername);
        registry.add("spring.datasource.password", this::getPassword);
    }
}
