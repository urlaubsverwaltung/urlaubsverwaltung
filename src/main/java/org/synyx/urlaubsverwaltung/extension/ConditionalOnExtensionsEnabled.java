package org.synyx.urlaubsverwaltung.extension;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a component is only eligible for registration when extensions are enabled.
 *
 * @see ConditionalOnProperty
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ConditionalOnProperty(value = "uv.extensions.enabled", havingValue = "true")
public @interface ConditionalOnExtensionsEnabled {
}
