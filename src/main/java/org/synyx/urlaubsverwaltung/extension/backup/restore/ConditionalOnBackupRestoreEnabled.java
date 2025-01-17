package org.synyx.urlaubsverwaltung.extension.backup.restore;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a component is only eligible for registration when backup restore is enabled.
 *
 * @see Conditional
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ConditionalOnProperty(prefix = "uv.backup.restore-configuration", name = "enabled", havingValue = "true")
public @interface ConditionalOnBackupRestoreEnabled {
}

