package org.synyx.urlaubsverwaltung.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CronExpressionConstraintValidator.class)
public @interface CronExpression {

    String message() default "Invalid cron expression";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
