package org.synyx.urlaubsverwaltung.validation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CronExpressionConstraintValidatorTest {

    @Test
    void returnsFalseForEmptyString() {
        final CronExpressionConstraintValidator validator = new CronExpressionConstraintValidator();

        boolean actual = validator.isValid("", null);
        assertThat(actual).isFalse();
    }

    @Test
    void returnsFalseForNull() {
        final CronExpressionConstraintValidator validator = new CronExpressionConstraintValidator();

        boolean actual = validator.isValid(null, null);
        assertThat(actual).isFalse();
    }

    @Test
    void returnsFalseForRandomString() {
        final CronExpressionConstraintValidator validator = new CronExpressionConstraintValidator();

        boolean actual = validator.isValid("random", null);
        assertThat(actual).isFalse();
    }

    @Test
    void returnsTrueForCronString() {
        final CronExpressionConstraintValidator validator = new CronExpressionConstraintValidator();

        boolean actual = validator.isValid("0 0 7 * * *", null);
        assertThat(actual).isTrue();
    }
}
