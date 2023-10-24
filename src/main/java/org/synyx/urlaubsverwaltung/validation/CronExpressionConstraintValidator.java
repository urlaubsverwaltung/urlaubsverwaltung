package org.synyx.urlaubsverwaltung.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CronExpressionConstraintValidator implements ConstraintValidator<CronExpression, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return org.springframework.scheduling.support.CronExpression.isValidExpression(value);
    }
}
