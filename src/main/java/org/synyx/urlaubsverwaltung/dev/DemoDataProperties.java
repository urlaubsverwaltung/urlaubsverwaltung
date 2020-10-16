package org.synyx.urlaubsverwaltung.dev;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("uv.development.demodata")
public class DemoDataProperties {

    /**
     * Enables the creation of demo data
     */
    private boolean create = true;

    public boolean isCreate() {
        return create;
    }

    public void setCreate(boolean create) {
        this.create = create;
    }
}
