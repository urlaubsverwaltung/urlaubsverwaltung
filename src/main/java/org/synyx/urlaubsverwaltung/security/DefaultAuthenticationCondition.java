package org.synyx.urlaubsverwaltung.security;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;

import org.springframework.core.type.AnnotatedTypeMetadata;


/**
 * Condition matching if default authentication is used.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class DefaultAuthenticationCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {

        String authentication = System.getProperties().getProperty(Authentication.PROPERTY_KEY);

        return authentication == null || authentication.equalsIgnoreCase(Authentication.Type.DEFAULT.getName());
    }
}
