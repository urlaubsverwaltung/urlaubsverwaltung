package org.synyx.urlaubsverwaltung.tenancy.configuration.single;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a component is only eligible for registration when single tenant mode is enabled.
 *
 * @see Conditional
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(IsSingleTenantMode.class)
public @interface ConditionalOnSingleTenantMode {
}
