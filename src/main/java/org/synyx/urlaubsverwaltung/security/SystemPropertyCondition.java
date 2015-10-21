package org.synyx.urlaubsverwaltung.security;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;

import org.springframework.core.type.AnnotatedTypeMetadata;

import org.springframework.util.StringUtils;


/**
 * Condition matching if a certain system property is set to a certain value.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class SystemPropertyCondition implements Condition {

    private final String propertyKey;
    private final String propertyValue;

    public SystemPropertyCondition(String propertyKey, String propertyValue) {

        if (!StringUtils.hasText(propertyKey)) {
            throw new IllegalArgumentException("Property key must be given.");
        }

        if (!StringUtils.hasText(propertyValue)) {
            throw new IllegalArgumentException("Property value must be given.");
        }

        this.propertyKey = propertyKey;
        this.propertyValue = propertyValue;
    }

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {

        String property = System.getProperties().getProperty(propertyKey);

        return property != null && property.toLowerCase().equals(propertyValue.toLowerCase());
    }
}
