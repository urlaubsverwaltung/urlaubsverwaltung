package org.synyx.urlaubsverwaltung.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class CronExpressionConstraintValidatorTest {

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " ", "random"})
    void ensureFalse(String givenString) {
        final CronExpressionConstraintValidator validator = new CronExpressionConstraintValidator();

        boolean actual = validator.isValid(givenString, null);
        assertThat(actual).isFalse();
    }

    @Test
    void returnsTrueForCronString() {
        final CronExpressionConstraintValidator validator = new CronExpressionConstraintValidator();

        boolean actual = validator.isValid("0 0 7 * * *", null);
        assertThat(actual).isTrue();
    }
}
