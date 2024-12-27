package org.synyx.urlaubsverwaltung.dev;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("uv.development.demodata")
public class DemoDataProperties {

    /**
     * Enables the creation of demo data based on person created events
     */
    private boolean create = false;

    /**
     * Enabled the creation of demo data persons for local development
     */
    private boolean localDevelopment = false;

    /**
     * Number of additional inactive users to create
     */
    private int additionalInactiveUser = 0;

    /**
     * Number of additional active users to create
     */
    private int additionalActiveUser = 0;

    public boolean isCreate() {
        return create;
    }

    public void setCreate(boolean create) {
        this.create = create;
    }

    public boolean isLocalDevelopment() {
        return localDevelopment;
    }

    public void setLocalDevelopment(boolean localDevelopment) {
        this.localDevelopment = localDevelopment;
    }

    public int getAdditionalInactiveUser() {
        return additionalInactiveUser;
    }

    public void setAdditionalInactiveUser(int additionalInactiveUser) {
        this.additionalInactiveUser = additionalInactiveUser;
    }

    public int getAdditionalActiveUser() {
        return additionalActiveUser;
    }

    public void setAdditionalActiveUser(int additionalActiveUser) {
        this.additionalActiveUser = additionalActiveUser;
    }
}
