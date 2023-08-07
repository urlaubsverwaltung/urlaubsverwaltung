package org.synyx.urlaubsverwaltung.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.scheduling.support.CronSequenceGenerator;

public class CronExpressionConstraintValidator implements ConstraintValidator<CronExpression, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return CronSequenceGenerator.isValidExpression(value);
    }
}
