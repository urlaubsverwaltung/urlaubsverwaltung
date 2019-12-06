package org.synyx.urlaubsverwaltung.dev;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("uv.development.testdata")
public class TestDataProperties {

    /**
     * Enables the creation of test data
     */
    private boolean create = true;

    public boolean isCreate() {
        return create;
    }

    public void setCreate(boolean create) {
        this.create = create;
    }
}
