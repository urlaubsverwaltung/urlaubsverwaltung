package org.synyx.urlaubsverwaltung.calendar;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CalendarPropertiesTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
            validator = validatorFactory.getValidator();
        }
    }

    @Test
    void organizerIsAnEmail() {
        final CalendarProperties calendarProperties = new CalendarProperties();
        calendarProperties.setOrganizer("email@example.org");
        final Set<ConstraintViolation<CalendarProperties>> violations = validator.validate(calendarProperties);

        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "NotAnEmail"})
    @NullSource
    void organizerIsWrong(String input) {
        final CalendarProperties calendarProperties = new CalendarProperties();
        calendarProperties.setOrganizer(input);
        final Set<ConstraintViolation<CalendarProperties>> violations = validator.validate(calendarProperties);

        assertThat(violations.size()).isOne();
    }
}
