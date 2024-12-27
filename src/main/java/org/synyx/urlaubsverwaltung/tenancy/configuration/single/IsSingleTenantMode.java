package org.synyx.urlaubsverwaltung.tenancy.configuration.single;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class IsSingleTenantMode implements Condition {

    protected static final String URLAUBSVERWALTUNG_TENANT_MODE = "uv.tenant.mode";
    protected static final String SINGLE_MODE = "single";

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        final String property = context.getEnvironment().getProperty(URLAUBSVERWALTUNG_TENANT_MODE);
        return property == null || property.isBlank();
    }
}
