package org.synyx.urlaubsverwaltung;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.PostgreSQLContainer;

public class TestPostgreSQLContainer extends PostgreSQLContainer<TestPostgreSQLContainer> {

    private static final String VERSION = "15.3";

    public TestPostgreSQLContainer() {
        super(IMAGE + ":" + VERSION);
        withCommand("--max_connections=1000", "--shared_buffers=240MB");
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
    public void configureSpringDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", this::getJdbcUrl);
        registry.add("spring.datasource.username", this::getUsername);
        registry.add("spring.datasource.password", this::getPassword);
    }
}
