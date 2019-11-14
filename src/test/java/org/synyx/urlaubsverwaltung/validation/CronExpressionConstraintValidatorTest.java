package org.synyx.urlaubsverwaltung.validation;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CronExpressionConstraintValidatorTest {

    @Test
    public void returnsFalseForEmptyString() {
        final CronExpressionConstraintValidator validator = new CronExpressionConstraintValidator();

        boolean actual = validator.isValid("", null);
        assertThat(actual).isFalse();
    }

    @Test
    public void returnsFalseForNull() {
        final CronExpressionConstraintValidator validator = new CronExpressionConstraintValidator();

        boolean actual = validator.isValid(null, null);
        assertThat(actual).isFalse();
    }

    @Test
    public void returnsFalseForRandomString() {
        final CronExpressionConstraintValidator validator = new CronExpressionConstraintValidator();

        boolean actual = validator.isValid("random", null);
        assertThat(actual).isFalse();
    }

    @Test
    public void returnsTrueForCronString() {
        final CronExpressionConstraintValidator validator = new CronExpressionConstraintValidator();

        boolean actual = validator.isValid("0 0 7 * * *", null);
        assertThat(actual).isTrue();
    }
}
