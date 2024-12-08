package org.synyx.urlaubsverwaltung.tenancy.tenant;

public class MissingTenantException extends RuntimeException {
    MissingTenantException(String message) {
        super(message);
    }
}
