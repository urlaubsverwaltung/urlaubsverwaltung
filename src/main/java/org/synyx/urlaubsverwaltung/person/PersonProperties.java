package org.synyx.urlaubsverwaltung.person;

import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("uv.person")
@Validated
public class PersonProperties {

    /**
     * Indicates if persons can be deleted via the user interface. By default, this is enabled.
     * Disable it when persons are deleted by an external system, e.g. via an event of the extension.
     */
    private boolean uiDeletionEnabled = true;

    /**
     * Indicates if persons can be deactivated via the user interface. By default, this is enabled.
     * Disable it when persons are deactivated by an external system, e.g. via an event of the extension.
     * Already inactive persons can still be reactivated via the user interface.
     */
    private boolean uiDeactivationEnabled = true;

    /**
     * URL of the customer portal where persons are deactivated. Linked on the permissions page of active persons
     * when the deactivation via the user interface is disabled.
     */
    @URL
    private String customerPortalUrl;

    public boolean isUiDeletionEnabled() {
        return uiDeletionEnabled;
    }

    public void setUiDeletionEnabled(boolean uiDeletionEnabled) {
        this.uiDeletionEnabled = uiDeletionEnabled;
    }

    public boolean isUiDeactivationEnabled() {
        return uiDeactivationEnabled;
    }

    public void setUiDeactivationEnabled(boolean uiDeactivationEnabled) {
        this.uiDeactivationEnabled = uiDeactivationEnabled;
    }

    public String getCustomerPortalUrl() {
        return customerPortalUrl;
    }

    public void setCustomerPortalUrl(String customerPortalUrl) {
        this.customerPortalUrl = customerPortalUrl;
    }
}
