package org.synyx.urlaubsverwaltung.validation;

import org.springframework.scheduling.support.CronSequenceGenerator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CronExpressionConstraintValidator implements ConstraintValidator<CronExpression, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return CronSequenceGenerator.isValidExpression(value);
    }
}
